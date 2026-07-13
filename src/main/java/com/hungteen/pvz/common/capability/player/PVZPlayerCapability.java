package com.hungteen.pvz.common.capability.player;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.interfaces.IMaxSunExpander;
import com.hungteen.pvz.client.PVZKeyBindings;
import com.hungteen.pvz.common.entity.Sun;
import com.hungteen.pvz.common.entity.npcs.Penny;
import com.hungteen.pvz.common.item.EnderSeedBundleItem;
import com.hungteen.pvz.common.item.PumpkinHelmetItem;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.network.EnderSeedBundleContainerPacket;
import com.hungteen.pvz.common.network.PlayerContinueCoolDownPacket;
import com.hungteen.pvz.common.network.ServerInfoPacket;
import com.hungteen.pvz.common.register.*;
import com.hungteen.pvz.common.world.invasion.InvasionTeam;
import com.hungteen.pvz.common.world.zen_garden.ZenGardenChunkGenerator;
import com.hungteen.pvz.common.world.zen_garden.ZenGardenTeleporter;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.MathUtil;
import com.hungteen.pvz.util.Util;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.village.poi.PoiManager;
import net.minecraft.world.entity.ai.village.poi.PoiTypes;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class PVZPlayerCapability implements ICapabilitySerializable<CompoundTag> {

    private PVZPlayerCapStats nbt = null;
    private final Player player;
    public static final Capability<PVZPlayerCapability> CAP = CapabilityManager.get(new CapabilityToken<>(){});
    public int syncCount = 0;

    /**Pair of garden teleporting positions. The first is overworld pos, the second is garden pos.*/
    private Pair<Vec3, Vec3> gardenPos = new Pair<>(null, null);

    /**Container of {@link com.hungteen.pvz.common.item.EnderSeedBundleItem EnderSeedBundle}.*/
    private Container enderSeedBundleContainer = new SimpleContainer(9);
    public int clientBundleContainerDirty = -2;

    /**gamerules*/
    public boolean isTeamBattleOn = false;
    public int advancedPlantsExtraCostRange = 30;

    //client variables
    static Vec3 playerPos = null;

    //TODO combine this class with PVZPlayerCapNBT.

    public PVZPlayerCapability(Player player) {
        this.player = player;
        LazyOptional<PVZPlayerCapStats> opt = LazyOptional.of(this::createNBT);
        opt.ifPresent(cap -> cap.setPlayer(player));
    }

    private PVZPlayerCapStats createNBT(){
        if (nbt == null) {
            nbt = new PVZPlayerCapStats();
        }
        return nbt;
    }

    public static void tick(@NotNull TickEvent.ServerTickEvent ev) {
        for (ServerPlayer player : ev.getServer().getPlayerList().getPlayers()) {
            player.getCapability(CAP).ifPresent(cap -> {
                var nbt = cap.getPlayerData();
                //timed sync
                if (++ cap.syncCount % 10 == 0) {
                    cap.sync(false);
                    if (cap.syncCount == 100) cap.syncCount = 0;
                }
                //functional
                if (! player.isSpectator()) {
                    //team
                    if (player.getTeam() == null && PVZConfig.PVZGameRules.getBoolean(player.level, PVZConfig.Common.joinDefaultTeam)) {
                        PlayerTeam playerTeam = player.getServer().getScoreboard().getPlayerTeam(PVZMod.FRIENDLY_TEAM);
                        String name = player.getScoreboardName();
                        if (playerTeam != null) {
                            player.getServer().getScoreboard().addPlayerToTeam(name, playerTeam);
                        }
                    }
                    //garden border
                    if (PVZConfig.PVZGameRules.getBoolean(player.level, PVZConfig.Common.gardenBorder) && player.level.dimension().location().equals(Util.prefix("zen_garden"))) {
                        Vec3 gardenCenter = player.position();
                        gardenCenter = gardenCenter.subtract(Vec3.atCenterOf(
                                ZenGardenChunkGenerator.getMainIslandPos(gardenCenter.x / 16, gardenCenter.z / 16)));
                        int distSqr = (int) (gardenCenter.x * gardenCenter.x + gardenCenter.z * gardenCenter.z);
                        if (distSqr >= 160000) {
                            int strength = (distSqr - 160000) / 40000;
                            player.addEffect(new MobEffectInstance(PVZMobEffects.DISTANCE_EFFECT.get(), 50, strength));
                        }
                        if (player.tickCount % 20 == 0 && player.hasEffect(PVZMobEffects.DISTANCE_EFFECT.get())) {
                            int amplifier = player.getEffect(PVZMobEffects.DISTANCE_EFFECT.get()).getAmplifier();
                            if (amplifier >= 4) {
                                if (! player.hasEffect(PVZMobEffects.BRIGHTNESS.get())) {
                                    player.addEffect(new MobEffectInstance(MobEffects.DARKNESS,50));
                                }
                            }
                            List<Phantom> phantoms = player.level.getEntitiesOfClass(Phantom.class, player.getBoundingBox().inflate(32, 256, 32));
                            if (phantoms.size() < amplifier / 3) {
                                Phantom phantom = EntityType.PHANTOM.create(player.level);
                                if (phantom != null) {
                                    phantom.moveTo(player.position().add(0, 30, 0));
                                    player.level.addFreshEntity(phantom);
                                    phantom.addEffect(new MobEffectInstance(MobEffects.DAMAGE_BOOST, 5000, amplifier / 5));
                                }
                            }
                        }
                    }
                    //sun related mob effects.
                    ++ nbt.sunCountDown;
                    if (player.hasEffect(PVZMobEffects.BRIGHTNESS.get())) {
                        if (player.hasEffect(MobEffects.DARKNESS)) {
                            player.removeEffect(MobEffects.DARKNESS);
                        }
                        if (nbt.sunCountDown >= 45 / (player.getEffect(PVZMobEffects.BRIGHTNESS.get()).getAmplifier() + 1)) {
                            int limitSun = Util.getDarknessSunThreshold(player);
                            int curSun = nbt.getValue(PVZPlayerCapStats.SUN);
                            if (curSun < limitSun) {
                                nbt.addValue(PVZPlayerCapStats.SUN, 5);
                            }
                            nbt.sunCountDown = 0;
                        }
                    } else if (player.hasEffect(MobEffects.DARKNESS) && nbt.sunCountDown >= 5 / (player.getEffect(MobEffects.DARKNESS).getAmplifier() + 1)) {
                        int limitSun = Util.getDarknessSunThreshold(player);
                        int curSun = nbt.getValue(PVZPlayerCapStats.SUN);
                        if (curSun > limitSun) {
                            nbt.addValue(PVZPlayerCapStats.SUN, -5);
                        }
                        nbt.sunCountDown = 0;
                    }
                    //naturally sun regain.
                    int interval = PVZConfig.PVZGameRules.getInt(player.level, PVZConfig.Common.naturallyRegainSunInterval);
                    if (interval > 0 && player.tickCount % interval == 0 && ! player.hasEffect(MobEffects.DARKNESS) && EntityUtil.isEntityPeace(player,100)) {
                        int limitSun = nbt.getValueLimit(PVZPlayerCapStats.SUN).getSecond();
                        int curSun = nbt.getValue(PVZPlayerCapStats.SUN);
                        if (curSun < limitSun) {
                            nbt.addValue(PVZPlayerCapStats.SUN, 5);
                        }
                    }
                    //max sun calculation.
                    AttributeInstance maxSun = player.getAttribute(PVZAttributes.MAX_SUN.get());
                    if (! PVZConfig.PVZGameRules.getBoolean(player.level, PVZConfig.Common.dynamicSunRule)) {
                        //delete entities & blocks caused modifiers.
                        maxSun.getModifiers().forEach((modifier) -> {
                            Entity entity = ((ServerLevel) player.level).getEntity(modifier.getId());
                            if (! EntityUtil.isEntityValid(entity)) {
                                BlockPos pos = MathUtil.posFromUuid(modifier.getId());
                                if (pos != null) {
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
                                BlockPos pos = MathUtil.posFromUuid(modifier.getId());
                                if (pos != null) {
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
                                if (maxSun.modifierById.keySet().stream().noneMatch(uuid1 -> uuid1.equals(entity.getUUID()))) {
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
                    int overFlow = nbt.getValue(PVZPlayerCapStats.SUN) - toMax;
                    while (overFlow > 25) {
                        Sun.spawnSunWithEffects(player.level, 25, player.blockPosition(), 0.3F);
                        overFlow -= 25;
                    }
                    if (overFlow > 0) {
                        Sun.spawnSunWithEffects(player.level, overFlow, player.blockPosition(), 0.3F);
                    }
                    nbt.setValueLimit(PVZPlayerCapStats.SUN, 0, toMax);
                }
                //cool down effects.
                if (nbt.getValue(PVZPlayerCapStats.PLANT_HAVE_CD) == 0) {
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
                //auto set sun cost and cd.
                if (nbt.getValue(PVZPlayerCapStats.AUTO_SET_COST_AND_CD) == 1) {
                    nbt.setValue(PVZPlayerCapStats.PLANT_HAVE_COST, player.isCreative() ? 0 : 1);
                    nbt.setValue(PVZPlayerCapStats.PLANT_HAVE_CD, player.isCreative() ? 0 : 1);
                }
                //invasion spawn
                int interval = PVZConfig.PVZGameRules.getInt(player.level, PVZConfig.Common.naturallySpawnInvasionsInterval) / 100;
                if (player.tickCount % 100 == 0 && interval > 0) {
                    int lastInvasion = nbt.getValue(PVZPlayerCapStats.LAST_INVASION);
                    if (player.getRandom().nextInt(lastInvasion + 1) > interval) {
                        if (InvasionTeam.spawnFor(player)) nbt.setValue(PVZPlayerCapStats.LAST_INVASION, 0);
                    }
                    nbt.addValue(PVZPlayerCapStats.LAST_INVASION, 1);
                }
                //penny spawn
                if (player.level.dimension().location().equals(PVZDimensions.ZEN_GARDEN)) {
                    interval = PVZConfig.PVZGameRules.getInt(player.level, PVZConfig.Common.naturallySpawnPennyInterval);
                    int gameTime = Math.toIntExact(player.level.getGameTime() % interval);
                    if (gameTime > 500 && gameTime < interval / 2) {
                        if (nbt.getValue(PVZPlayerCapStats.SUMMONED_PENNY) == 0 && spawnPenny(player)) {
                            nbt.setValue(PVZPlayerCapStats.SUMMONED_PENNY, 1);
                        }
                    } else if (gameTime < interval) {
                        nbt.setValue(PVZPlayerCapStats.SUMMONED_PENNY, 0);
                    }
                }
                //pumpkin helmet
                ItemStack itemStack = player.containerMenu.getCarried();
                if (itemStack.getItem() instanceof PumpkinHelmetItem pumpkinHelmet) {
                    pumpkinHelmet.changeToPumpkin(itemStack, player.level, player.position().add(0, player.getBbHeight() / 2, 0), player.getXRot(), player.getYRot(), player.getDeltaMovement());
                    player.containerMenu.setCarried(ItemStack.EMPTY);
                }
                //ender seed bundle ticking
                int slot = EnderSeedBundleItem.getHotBarEnderSeedBundleSlot(player);
                for (int i = 0; i < 9; i ++) {
                    cap.enderSeedBundleContainer.getItem(i).inventoryTick(player.getLevel(), player, 41 + i, slot >= 0);
                }
            });
        }
    }

    public void sync(boolean syncAll) {
        if (player instanceof ServerPlayer serverPlayer) {
            if (syncCount % 20 == 0) PlayerContinueCoolDownPacket.sync(serverPlayer);
            ServerInfoPacket.sync(serverPlayer, ((ServerLevel) serverPlayer.level), syncAll || syncCount == 20);
            getPlayerData(player).ifPresent(data -> data.sync(syncAll || syncCount == 40));
            if ((syncAll || syncCount == 10 || syncCount == 60) && (! serverPlayer.isCreative() || player.tickCount < 20)) EnderSeedBundleContainerPacket.syncToClient(serverPlayer, -1);
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void clientTick(TickEvent.ClientTickEvent ev) {
        if (ClientProxy.MC.level == null) {
            return;
        }
        if (PVZKeyBindings.keyEnderSeedBundle.isDown()) {
            EnderSeedBundleItem.selectEnderSeedBundle(ClientProxy.getPlayer());
        }
        ClientProxy.MC.level.getEntities().getAll().forEach(entity -> entity.getCapability(CAP).ifPresent((cap) -> {
            Player player = ClientProxy.getPlayer();
            //hypnotized effect
            if (player instanceof LocalPlayer p && player.getAttribute(Attributes.ARMOR).getModifier(PVZMobEffects.HYPNOTIZED_EFFECT_UUID) != null && p.portalTime <= 0) {
                p.portalTime = player.getRandom().nextFloat() / 2;
                p.oPortalTime = player.getRandom().nextFloat() / 2;
            }
            //garden border
            if (player != null) {
                if (player.level.dimension().location().equals(Util.prefix("zen_garden")) && player.hasEffect(PVZMobEffects.DISTANCE_EFFECT.get())) {
                    int amplifier = player.getEffect(PVZMobEffects.DISTANCE_EFFECT.get()).getAmplifier();
                    Vec3 cenVec = player.position();
                    if (playerPos != null && cenVec.distanceToSqr(playerPos) < 4) {
                        cenVec = cenVec.subtract(Vec3.atCenterOf(
                                ZenGardenChunkGenerator.getMainIslandPos(cenVec.x / 16, cenVec.z / 16)));
                        Vec3 posVec = player.position().subtract(playerPos).multiply(1, 0, 1);
                        Vec3 movVec = player.getDeltaMovement().multiply(1, 0, 1);
                        Vec3 posMod = posVec.add(cenVec);
                        Vec3 movMod = movVec.add(cenVec);
                        double dist = cenVec.length();
                        double tmp = posMod.length();
                        posMod = posMod.normalize().multiply(tmp - dist, 0, tmp - dist);
                        tmp = movMod.length();
                        movMod = movMod.normalize().multiply(tmp - dist, 0, tmp - dist);
                        tmp = Math.min(1, (amplifier + 1) / 5);
                        //player's position is far from center, so regard the mod vector has the same angle as cenVec do.
                        if (cenVec.x * posMod.x + cenVec.z * posMod.z > 0) {
                            player.move(MoverType.SELF, posMod.multiply(- tmp, 0, - tmp));
                        }
                        if (cenVec.x * movMod.x + cenVec.z * movMod.z > 0) {
                            player.setDeltaMovement(player.getDeltaMovement().subtract(movMod.multiply(tmp, 0, tmp)));
                        }
                    }
                    playerPos = player.position();
                } else {
                    playerPos = null;
                }
            }
            //ender seed bundle
            if (player.isCreative()) {
                if (cap.clientBundleContainerDirty > -2) {
                    EnderSeedBundleContainerPacket.syncToServer(cap.clientBundleContainerDirty);
                    cap.clientBundleContainerDirty = -2;
                }
            }
        }));
    }

    private static AttributeModifier getBlockModifier(BlockPos pos, IMaxSunExpander sunExpander, Player player) {
        return new AttributeModifier(
                MathUtil.posToUuid(pos), "extra_max_sun", sunExpander.extraMaxSun(pos, player), AttributeModifier.Operation.ADDITION);
    }

    private static AttributeModifier getEntityModifier(Entity entity, Player player) {
        return new AttributeModifier(entity.getUUID(), "extra_max_sun", ((IMaxSunExpander) entity).extraMaxSun(entity.getOnPos(), player), AttributeModifier.Operation.ADDITION);
    }

    private static boolean spawnPenny(ServerPlayer player) {
        if (player.getRandom().nextInt(10) == 0) {
            Penny oldPenny = player.level.getNearestEntity(Penny.class, TargetingConditions.DEFAULT
                    , player, player.getX(), player.getY(), player.getZ()
                    , player.getBoundingBox().inflate(128));
            if (oldPenny != null) {
                return false;
            }
            BlockPos blockpos = player.blockPosition();
            PoiManager poimanager = ((ServerLevel) player.level).getPoiManager();
            Optional<BlockPos> optional = poimanager.find((p_219713_) -> p_219713_.is(PoiTypes.MEETING), (p_219711_) -> true, blockpos, 48, PoiManager.Occupancy.ANY);
            BlockPos blockpos1 = optional.orElse(blockpos);
            BlockPos blockpos2 = findSpawnPositionNear(blockpos1, 48, player);
            if (blockpos2 != null && hasEnoughSpace(player.level, blockpos2)) {
                oldPenny = player.level.getNearestEntity(Penny.class, TargetingConditions.DEFAULT
                        , player, blockpos2.getX(), blockpos2.getY(), blockpos2.getZ()
                        , player.getBoundingBox().inflate(72));
                if (oldPenny != null) {
                    return false;
                }
                Penny penny = PVZEntities.PENNY.get().spawn((ServerLevel) player.level
                        , null, null, null, blockpos2, MobSpawnType.EVENT, false, false);
                if (penny != null) {
                    penny.playSound(PVZSoundEvents.PENNY_SUMMON.get());
                    player.displayClientMessage(Component.translatable("hint.pvz.penny"), true);
                    return true;
                }
            }
        }
        return false;
    }
    @Nullable
    private static BlockPos findSpawnPositionNear(BlockPos p_35930_, int p_35931_, ServerPlayer player) {
        BlockPos blockpos = null;
        for(int i = 0; i < 10; ++i) {
            int j = p_35930_.getX() + player.getRandom().nextInt(p_35931_ * 2) - p_35931_;
            int k = p_35930_.getZ() + player.getRandom().nextInt(p_35931_ * 2) - p_35931_;
            int l = player.level.getHeight(Heightmap.Types.WORLD_SURFACE, j, k);
            BlockPos blockpos1;
            if (l > 100) {
                l = 100;
                while (l > 75) {
                    l --;
                    blockpos1 = new BlockPos(j, l, k);
                    if (! player.level.getBlockState(blockpos1).isAir()) {
                        break;
                    }
                }
            }
            blockpos1 = new BlockPos(j, l, k);
            if (NaturalSpawner.isSpawnPositionOk(SpawnPlacements.Type.ON_GROUND, player.level, blockpos1, EntityType.WANDERING_TRADER)) {
                blockpos = blockpos1;
                break;
            }
        }
        return blockpos;
    }
    private static boolean hasEnoughSpace(BlockGetter p_35926_, BlockPos p_35927_) {
        for(BlockPos blockpos : BlockPos.betweenClosed(p_35927_, p_35927_.offset(3, 3, 3))) {
            if (!p_35926_.getBlockState(blockpos).getCollisionShape(p_35926_, blockpos).isEmpty()) {
                return false;
            }
        }

        return true;
    }


    public static Vec3 getTeleportPos(Player player, Level destWorld) {
        AtomicReference<Vec3> vec3 = new AtomicReference<>();
        player.getCapability(CAP).ifPresent(cap -> vec3.set(destWorld.dimension().equals(ZenGardenTeleporter.GARDEN) ?
                cap.gardenPos.getSecond() : cap.gardenPos.getFirst()));
        return vec3.get();
    }

    public static void setTeleportPos(Player player, Vec3 pos, ResourceKey<Level> currentWorldKey) {
        player.getCapability(CAP).ifPresent(cap -> {
            if (currentWorldKey.equals(Level.OVERWORLD)) {
                cap.gardenPos = new Pair<>(pos, cap.gardenPos.getSecond());
            } else if (currentWorldKey.equals(ZenGardenTeleporter.GARDEN)) {
                if (cap.gardenPos.getSecond() == null || cap.gardenPos.getSecond().distanceToSqr(pos) <= 1048576) {
                    cap.gardenPos = new Pair<>(cap.gardenPos.getFirst(), pos);
                }
            }
        });
    }

    public static void setEnderSeedBundleSlot(Player player, int slot, ItemStack itemStack) {
        player.getCapability(CAP).ifPresent(cap -> {
            cap.enderSeedBundleContainer.setItem(slot, itemStack);
            cap.clientBundleContainerDirty = cap.clientBundleContainerDirty == -2 ? slot : -1;
        });
        if (player instanceof ServerPlayer serverPlayer) {
            EnderSeedBundleContainerPacket.syncToClient(serverPlayer, slot);
        }
    }

    public static ItemStack getEnderSeedBundleSlot(Player player, int slot) {
        AtomicReference<ItemStack> itemStack = new AtomicReference<>(ItemStack.EMPTY);
        player.getCapability(CAP).ifPresent(cap -> {
            itemStack.set(cap.enderSeedBundleContainer.getItem(slot));
        });
        return itemStack.get();
    }

    private static Container getEnderSeedBundleData(Player player) {
        AtomicReference<Container> reference = new AtomicReference<>();
        player.getCapability(CAP).ifPresent(cap -> reference.set(cap.enderSeedBundleContainer));
        return reference.get();
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CAP ? LazyOptional.of(() -> (T) this) : LazyOptional.empty();
    }

    public PVZPlayerCapStats getPlayerData() {
        return nbt;
    }

    public static Optional<PVZPlayerCapStats> getPlayerData(Player player) {
        AtomicReference<PVZPlayerCapability> cap = new AtomicReference<>();
        player.getCapability(CAP).ifPresent(cap::set);
        return cap.get() == null ? Optional.empty() : Optional.of(cap.get().getPlayerData());
    }

    public static int getValue(Player player, String key){
        AtomicInteger value = new AtomicInteger(0);
        PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> value.set(nbt.getValue(key)));
        return value.get();
    }

    public static Pair<Integer, Integer> getValueLimit(Player player, String key){
        AtomicReference<Pair<Integer, Integer>> value = new AtomicReference<>(Pair.of(0, 0));
        PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> value.set(nbt.getValueLimit(key)));
        return value.get();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.put("PVZStats", getPlayerData().serializeNBT());
        {
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
        }{
            CompoundTag posTag = new CompoundTag();
            if (this.gardenPos.getFirst() != null) {
                posTag.putDouble("overworld_x", this.gardenPos.getFirst().x);
                posTag.putDouble("overworld_y", this.gardenPos.getFirst().y);
                posTag.putDouble("overworld_z", this.gardenPos.getFirst().z);
            }
            if (this.gardenPos.getSecond() != null) {
                posTag.putDouble("garden_x", this.gardenPos.getSecond().x);
                posTag.putDouble("garden_y", this.gardenPos.getSecond().y);
                posTag.putDouble("garden_z", this.gardenPos.getSecond().z);
            }
            tag.put("garden_pos", posTag);
        }{
            ListTag seedBundleTag = new ListTag();
            for(int i = 0; i < enderSeedBundleContainer.getContainerSize(); ++i) {
                ItemStack itemstack = enderSeedBundleContainer.getItem(i);
                if (!itemstack.isEmpty()) {
                    CompoundTag compoundtag = new CompoundTag();
                    compoundtag.putByte("Slot", (byte)i);
                    itemstack.save(compoundtag);
                    seedBundleTag.add(compoundtag);
                }
                if (!seedBundleTag.isEmpty()) {
                    tag.put("EnderSeedBundle", seedBundleTag);
                }
            }
        }
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("PVZStats")) {
            getPlayerData().deserializeNBT(tag.getCompound("PVZStats"));
        }
        if (tag.contains("CoolDowns")) {
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
        if (tag.contains("garden_pos")) {
            CompoundTag posTag = tag.getCompound("garden_pos");
            this.gardenPos = new Pair<>(posTag.contains("overworld_x") ? new Vec3(
                    posTag.getDouble("overworld_x"),
                    posTag.getDouble("overworld_y"),
                    posTag.getDouble("overworld_z")
            ) : null, posTag.contains("garden_x") ? new Vec3(
                    posTag.getDouble("garden_x"),
                    posTag.getDouble("garden_y"),
                    posTag.getDouble("garden_z")
            ) : null);
        }
        if (tag.contains("EnderSeedBundle")) {
            ListTag listtag = tag.getList("EnderSeedBundle", 10);
            for(int i = 0; i < listtag.size(); ++i) {
                CompoundTag compoundtag = listtag.getCompound(i);
                int j = compoundtag.getByte("Slot") & 255;
                if (j < enderSeedBundleContainer.getContainerSize()) {
                    enderSeedBundleContainer.setItem(j, ItemStack.of(compoundtag));
                }
            }
        }
    }

    public static class EnderSeedBundleSlotAccess implements SlotAccess {
        final Player player;
        final int number;

        public EnderSeedBundleSlotAccess(Player player, int number) {
            this.player = player;
            this.number = number;
        }

        @Override
        public ItemStack get() {
            return PVZPlayerCapability.getEnderSeedBundleSlot(player, number);
        }

        @Override
        public boolean set(ItemStack itemStack) {
            if (! (itemStack.getItem() instanceof SeedPacketItem<?>)) return false;
            PVZPlayerCapability.setEnderSeedBundleSlot(player, number, itemStack);
            return true;
        }
    }
}
