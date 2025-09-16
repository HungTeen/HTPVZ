package com.hungteen.pvz.common.capability.player;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.interfaces.IMaxSunExpander;
import com.hungteen.pvz.common.entity.FallenStar;
import com.hungteen.pvz.common.entity.Sun;
import com.hungteen.pvz.common.item.PumpkinHelmetItem;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.network.PlayerContinueCoolDownPacket;
import com.hungteen.pvz.common.register.PVZAttributes;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.common.tags.PVZBiomeTags;
import com.hungteen.pvz.common.world.invasion.InvasionTeam;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.Util;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

public class PVZPlayerCapability implements ICapabilitySerializable<CompoundTag> {

    private PVZPlayerCapNBT nbt = null;
    private final Player player;
    public static final Capability<PVZPlayerCapNBT> NBT = CapabilityManager.get(new CapabilityToken<>(){});
    private final LazyOptional<PVZPlayerCapNBT> opt = LazyOptional.of(this::createNBT);
    public static int syncCount = 0;

    //TODO combine this class with PVZPlayerCapNBT.

    public PVZPlayerCapability(Player player) {
        this.player = player;
        this.opt.ifPresent(cap -> cap.setPlayer(player));
    }

    private PVZPlayerCapNBT createNBT(){
        if(nbt == null){
            nbt = new PVZPlayerCapNBT();
        }
        return nbt;
    }
    public static void tick(TickEvent.ServerTickEvent ev) {
        for (ServerPlayer player : ev.getServer().getPlayerList().getPlayers()) {
            //timed sync
            if (++ syncCount > 20) {
                getPlayerData(player).ifPresent(PVZPlayerCapNBT::syncAll);
                syncCount = 0;
            }
            //functional
            getPlayerData(player).ifPresent((nbt) -> {
                if (! player.isSpectator()) {
                    //team
                    if (player.getTeam() == null && PVZConfig.PVZGameRules.getBoolean(player.level, PVZConfig.Common.joinDefaultTeam)) {
                        PlayerTeam playerTeam = player.getServer().getScoreboard().getPlayerTeam(PVZMod.FRIENDLY_TEAM);
                        String name = player.getScoreboardName();
                        if (playerTeam != null) {
                            player.getServer().getScoreboard().addPlayerToTeam(name, playerTeam);
                        }
                    }
                    //sun related mob effects.
                    ++ nbt.sunCountDown;
                    if (player.hasEffect(PVZMobEffects.BRIGHTNESS.get())) {
                        if (player.hasEffect(MobEffects.DARKNESS)) {
                            player.removeEffect(MobEffects.DARKNESS);
                        }
                        if (nbt.sunCountDown >= 15 / (player.getEffect(PVZMobEffects.BRIGHTNESS.get()).getAmplifier() + 1)) {
                            int limitSun = getDifficultyLimitSun(player);
                            int curSun = nbt.getValue(PVZPlayerCapNBT.SUN);
                            if (curSun < limitSun) {
                                nbt.setValue(PVZPlayerCapNBT.SUN, Math.min(curSun + 5, limitSun));
                            }
                            nbt.sunCountDown = 0;
                        }
                    } else if (player.hasEffect(MobEffects.DARKNESS) && nbt.sunCountDown >= 5 / (player.getEffect(MobEffects.DARKNESS).getAmplifier() + 1)) {
                        int limitSun = getDifficultyLimitSun(player);
                        int curSun = nbt.getValue(PVZPlayerCapNBT.SUN);
                        if (curSun > limitSun) {
                            nbt.setValue(PVZPlayerCapNBT.SUN, Math.max(curSun - 5, limitSun));
                        }
                        nbt.sunCountDown = 0;
                    }
                    //naturally sun regain.
                    int interval = PVZConfig.PVZGameRules.getInt(player.level, PVZConfig.Common.naturallyRegainSunInterval);
                    if (interval > 0 && player.tickCount % interval == 0 && ! player.hasEffect(MobEffects.DARKNESS) && EntityUtil.isEntityPeace(player,100)) {
                        int limitSun = nbt.getValueLimit(PVZPlayerCapNBT.SUN).getSecond();
                        int curSun = nbt.getValue(PVZPlayerCapNBT.SUN);
                        if (curSun < limitSun) {
                            nbt.addValue(PVZPlayerCapNBT.SUN, 5);
                        }
                    }
                    //max sun calculation.
                    AttributeInstance maxSun = player.getAttribute(PVZAttributes.MAX_SUN.get());
                    if (! PVZConfig.PVZGameRules.getBoolean(player.level, PVZConfig.Common.dynamicSunRule)) {
                        //delete entities & blocks caused modifiers.
                        maxSun.getModifiers().forEach((modifier) -> {
                            Entity entity = ((ServerLevel) player.level).getEntity(modifier.getId());
                            if (! EntityUtil.isEntityValid(entity)) {
                                if (modifier.getId().toString().startsWith("a975c974-")) {
                                    maxSun.removeModifier(modifier.getId());
                                }
                            } else if (entity.distanceToSqr(player) > 400) {
                                maxSun.removeModifier(modifier.getId());
                            }
                        });
                    } else if (player.tickCount % 5 == 0) {
                        //delete modifiers caused by entities & blocks out of region or requiring refresh.
                        List<BlockPos> refreshBlocks = new ArrayList<>();
                        List<Entity> refreshEntities = new ArrayList<>();
                        maxSun.getModifiers().forEach((modifier) -> {
                            Entity entity = ((ServerLevel) player.level).getEntity(modifier.getId());
                            if (! EntityUtil.isEntityValid(entity)) {
                                if (modifier.getId().toString().startsWith("a975c974-")) {
                                    String string = modifier.getId().toString();
                                    //stored a positive number to avoid errors.
                                    int x = Integer.parseInt(string.substring(9, 13) + string.substring(14, 18), 16) - 30000000;
                                    int y = Integer.parseInt(string.substring(19, 23) + string.substring(24, 28), 16) - 128;
                                    int z = Integer.parseInt(string.substring(28), 16) - 30000000;
                                    BlockPos pos = new BlockPos(x, y, z);
                                    if (pos.distSqr(player.getOnPos()) > 400) {
                                        maxSun.removeModifier(modifier.getId());
                                    } else if (player.level.getBlockState(pos).getBlock() instanceof IMaxSunExpander maxSunExpander && maxSunExpander.requireRefreshExtraMaxSun()) {
                                        maxSun.removeModifier(modifier.getId());
                                        refreshBlocks.add(pos);
                                    }
                                } else {
                                    maxSun.removeModifier(modifier.getId());
                                }
                            } else {
                                if (entity.distanceToSqr(player) > 400) {
                                    maxSun.removeModifier(modifier.getId());
                                } else if (entity instanceof IMaxSunExpander maxSunExpander && maxSunExpander.requireRefreshExtraMaxSun()) {
                                    maxSun.removeModifier(modifier.getId());
                                    refreshEntities.add(entity);
                                }
                            }
                        });
                        //add entity modifier.
                        List<Entity> entities = player.level.getEntities(player, player.getBoundingBox().inflate(6, 6, 6).move(0, -3, 0),
                                EntitySelector.NO_SPECTATORS.and((entity) -> entity instanceof IMaxSunExpander));
                        entities.addAll(refreshEntities);
                        entities.forEach((entity) -> {
                            if (entity instanceof IMaxSunExpander) {
                                if (! maxSun.modifierById.keySet().stream().anyMatch(uuid1 -> uuid1.equals(entity.getUUID()))) {
                                    maxSun.addTransientModifier(getEntityModifier(entity, player));
                                }
                            }
                        });
                        //add block modifier.
                        for (int x = -6; x < 6; x ++) {
                            for (int y = -6; y < 6; y ++) {
                                for (int z = -6; z < 6; z ++) {
                                    BlockPos pos = player.getOnPos().offset(x, y, z);
                                    if (player.level.getBlockState(pos).getBlock() instanceof IMaxSunExpander sunExpander) {
                                        maxSun.addTransientModifier( getBlockModifier(pos, sunExpander, player));
                                    }
                                }
                            }
                        }
                        refreshBlocks.forEach(pos -> {
                            if (player.level.getBlockState(pos).getBlock() instanceof IMaxSunExpander sunExpander) {
                                maxSun.addTransientModifier( getBlockModifier(pos, sunExpander, player));
                            }
                        });
                    }
                    //refresh player capability sun limit.
                    int toMax = (int) player.getAttributeValue(PVZAttributes.MAX_SUN.get());
                    int overFlow = nbt.getValue(PVZPlayerCapNBT.SUN) - toMax;
                    while (overFlow > 25) {
                        Sun.spawnSunWithEffects(player.level, 25, player.blockPosition(), 0.3F);
                        overFlow -= 25;
                    }
                    if (overFlow > 0) {
                        Sun.spawnSunWithEffects(player.level, overFlow, player.blockPosition(), 0.3F);
                    }
                    nbt.setValueLimit(PVZPlayerCapNBT.SUN, 0, toMax);
                    //natural sun spawn
                    interval = PVZConfig.PVZGameRules.getInt(player.level, PVZConfig.Common.naturallySpawnSunInterval);
                    if (interval > 0 && player.tickCount % interval == 0 && ! player.level.getBiome(player.getOnPos()).is(PVZBiomeTags.UNABLE_SUN_FALLING)) {
                        int x = player.blockPosition().getX() + player.getRandom().nextInt(20) - 10;
                        int z = player.blockPosition().getZ() + player.getRandom().nextInt(20) - 10;
                        BlockPos pos = new BlockPos(x,
                                Math.max(player.level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z),
                                        player.blockPosition().getY()) + 6, z);
                        int light = player.level.getBrightness(LightLayer.SKY, pos) - player.level.getSkyDarken();
                        if (light > 9) {
                            Sun.spawnByAmount(player.level, light > 12 ? 50 : 25, pos, Vec3.ZERO);
                        }
                    }
                    //natural fallen stars spawn
                    interval = PVZConfig.PVZGameRules.getInt(player.level, PVZConfig.Common.naturallySpawnFallenStarInterval);
                    if (interval > 0 && player.tickCount % interval == 0 && ! player.level.getBiome(player.getOnPos()).is(PVZBiomeTags.UNABLE_STAR_FALLING)) {
                        int x = player.blockPosition().getX() + player.getRandom().nextInt(50) - 25;
                        int z = player.blockPosition().getZ() + player.getRandom().nextInt(50) - 25;
                        BlockPos pos = new BlockPos(x,
                                Math.max(player.level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z),
                                        player.blockPosition().getY()) + 30, z);
                        if (player.level.isNight() && ! player.level.isRaining()) {
                            FallenStar.spawnAt(player.level, pos);
                        }
                    }
                }
                //cool down effects.
                if (! player.level.isClientSide) {
                    if (nbt.getValue(PVZPlayerCapNBT.PLANT_HAVE_CD) == 0) {
                        Set<Item> keyset = Set.copyOf(player.getCooldowns().cooldowns.keySet());
                        for (Item i : keyset) {
                            if (i instanceof SeedPacketItem) {
                                player.getCooldowns().removeCooldown(i);
                            }
                        }
                    }
                    if (player.hasEffect(PVZMobEffects.EXCITEMENT.get())) {
                        Util.coolDownItems(player, (1 + player.getEffect(PVZMobEffects.EXCITEMENT.get()).getAmplifier()) * 3);
                    }
                    if (player.tickCount % 20 == 19) {
                        Map<Item, ItemCooldowns.CooldownInstance> coolDowns = player.getCooldowns().cooldowns;
                        int cur = player.getCooldowns().tickCount;
                        coolDowns.keySet().forEach(item -> {
                            ItemCooldowns.CooldownInstance instance = coolDowns.get(item);
                            PlayerContinueCoolDownPacket.sync(player, item,
                                    instance.startTime - cur, instance.endTime - cur);
                        });
                    }
                }
                //auto set sun cost and cd.
                if (nbt.getValue(PVZPlayerCapNBT.AUTO_SET_COST_AND_CD) == 1) {
                    nbt.setValue(PVZPlayerCapNBT.PLANT_HAVE_COST, player.isCreative() ? 0 : 1);
                    nbt.setValue(PVZPlayerCapNBT.PLANT_HAVE_CD, player.isCreative() ? 0 : 1);
                }
                //invasion spawn
                int interval = PVZConfig.PVZGameRules.getInt(player.level, PVZConfig.Common.naturallySpawnInvasionsInterval);
                if (player.tickCount % 50 == 0 && interval > 0) {
                    int lastInvasion = nbt.getValue(PVZPlayerCapNBT.LAST_INVASION);
                    if (lastInvasion > interval && player.getRandom().nextInt(lastInvasion) > (lastInvasion * 0.9F + (float) interval / 10)) {
                        nbt.setValue(PVZPlayerCapNBT.LAST_INVASION, interval / 2);
                        InvasionTeam.spawnFor(player);
                    }
                    nbt.addValue(PVZPlayerCapNBT.LAST_INVASION, 1);
                }
                //pumpkin helmet
                ItemStack itemStack = player.containerMenu.getCarried();
                if (itemStack.getItem() instanceof PumpkinHelmetItem pumpkinHelmet) {
                    pumpkinHelmet.changeToPumpkin(itemStack, player.level, player.position().add(0, player.getBbHeight() / 2, 0), player.getXRot(), player.getYRot(), player.getDeltaMovement());
                    player.containerMenu.setCarried(ItemStack.EMPTY);
                }
            });
        }
    }

    private static AttributeModifier getBlockModifier(BlockPos pos, IMaxSunExpander sunExpander, Player player) {
        return new AttributeModifier(
                //get uuid from position. using a positive number to avoid errors.
                UUID.fromString("a975c974-" +
                        Integer.toHexString(pos.getX() + 30000000).substring(0, 4) + "-" + Integer.toHexString(pos.getX() + 30000000).substring(4, 8) +
                        "-" + Integer.toHexString(pos.getY() + 128).substring(0, 4) + "-" + Integer.toHexString(pos.getY() + 128).substring(4, 8) +
                        Integer.toHexString(pos.getZ() + 30000000))
                , "extra_max_sun", sunExpander.extraMaxSun(pos, player), AttributeModifier.Operation.ADDITION);
    }

    private static AttributeModifier getEntityModifier(Entity entity, Player player) {
        return new AttributeModifier(entity.getUUID(), "extra_max_sun", ((IMaxSunExpander) entity).extraMaxSun(entity.getOnPos(), player), AttributeModifier.Operation.ADDITION);
    }

    public static Vec3 getTeleportPos(Player player, Level destWorld) {
        AtomicReference<Vec3> vec3 = new AtomicReference<>();
        PVZPlayerCapability.getPlayerData(player).ifPresent((data) -> {
            vec3.set(data.getTransportPos(destWorld));
        });
        return vec3.get();
    }

    public static void setTeleportPos(Player player, Vec3 pos, Level destWorld) {
        PVZPlayerCapability.getPlayerData(player).ifPresent((data) -> {
            data.setTransportPos(destWorld, pos);
        });
    }

    public static int getDifficultyLimitSun(Player player) {
        Difficulty difficulty = player.getServer().getWorldData().getDifficulty();
        int limitSun;
        switch (difficulty) {
            case PEACEFUL -> limitSun = 300;
            case EASY -> limitSun = 200;
            case HARD -> limitSun = 50;
            default -> limitSun = 100;//normal difficulty or other possible situations.
        }
        return limitSun;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        if (cap == NBT){
            return opt.cast();
        }
        return LazyOptional.empty();
    }

    public PVZPlayerCapNBT getPlayerData() {
        return nbt;
    }

    public static LazyOptional<PVZPlayerCapNBT> getPlayerData(Player player){
        return player.getCapability(NBT);
    }

    public static int getValue(Player player, String key){
        AtomicInteger value = new AtomicInteger();
        PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> value.set(nbt.getValue(key)));
        return value.get();
    }

    public static Pair<Integer, Integer> getValueLimit(Player player, String key){
        AtomicReference<Pair<Integer, Integer>> value = new AtomicReference<>();
        PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> value.set(nbt.getValueLimit(key)));
        return value.get();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("PVZData", getPlayerData().serializeNBT());
        CompoundTag cdTag = new CompoundTag();
        ItemCooldowns coolDowns = player.getCooldowns();
        for (Item item : coolDowns.cooldowns.keySet()) {
            ItemCooldowns.CooldownInstance instance = coolDowns.cooldowns.get(item);
            int[] tmp = new int[2];
            tmp[0] = instance.startTime - coolDowns.tickCount;
            tmp[1] = instance.endTime - coolDowns.tickCount;
            cdTag.putIntArray(ForgeRegistries.ITEMS.getKey(item).toString(), tmp);
        }
        tag.put("CoolDowns", cdTag);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        getPlayerData().deserializeNBT(tag.getCompound("PVZData"));
        ItemCooldowns coolDowns = player.getCooldowns();
        CompoundTag cdTag = tag.getCompound("CoolDowns");
        for (String name : cdTag.getAllKeys()) {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
            if (item != null) {
                int[] tmp = cdTag.getIntArray(name);
                coolDowns.cooldowns.put(item, new ItemCooldowns.CooldownInstance(coolDowns.tickCount + tmp[0], coolDowns.tickCount + tmp[1]));
            }
        }
    }
}
