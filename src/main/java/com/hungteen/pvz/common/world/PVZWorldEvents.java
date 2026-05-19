package com.hungteen.pvz.common.world;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapStats;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.FallenStar;
import com.hungteen.pvz.common.entity.Sun;
import com.hungteen.pvz.common.item.PVZShieldItem;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.register.PVZBiomes;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZDimensions;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.common.tags.PVZBiomeTags;
import com.hungteen.pvz.common.world.invasion.Invasion;
import com.hungteen.pvz.common.world.invasion.InvasionTypeManager;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.living.ZombieEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.*;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class PVZWorldEvents {
    @SubscribeEvent
    public static void treeGrowEventHandler(SaplingGrowTreeEvent ev) {
        if (ev.getRandomSource().nextInt(6) == 0) {
            ev.getLevel().setBlock(ev.getPos().below(), PVZBlocks.ORIGIN_ORE.get().defaultBlockState(), 2);
        }
    }
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void playerDestroyShield(PlayerDestroyItemEvent ev) {
        if (ev.getHand() != null && ev.getEntity().getItemInHand(ev.getHand()).getItem() instanceof PVZShieldItem item) {
            item.clientBroken(ev.getEntity().position(), ev.getEntity().level);
        }
    }
    @SubscribeEvent
    public static void checkEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        ResourceLocation location = ForgeRegistries.MOB_EFFECTS.getKey(event.getEffectInstance().getEffect());
        if (PVZMobEffects.unappliableMap.containsKey(location) && PVZMobEffects.unappliableMap.get(location).test(entity, event.getEffectInstance())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void playerRespawnCoolDown(PlayerEvent.PlayerRespawnEvent event) {
        if (! event.getEntity().level.isClientSide && ! event.isEndConquered()) {
            if (PVZPlayerCapability.getValue(event.getEntity(), PVZPlayerCapStats.PLANT_HAVE_CD) == 0) {
                return;
            }
            ItemCooldowns cooldowns = event.getEntity().getCooldowns();
            for (ItemStack item : event.getEntity().getInventory().items) {
                if (item.getItem() instanceof SeedPacketItem<?> item1) {
                    cooldowns.addCooldown(item.getItem(), item1.getBaseCoolDown(null));
                }
            }
            for (ItemStack item : event.getEntity().getInventory().offhand) {
                if (item.getItem() instanceof SeedPacketItem<?> item1) {
                    cooldowns.addCooldown(item.getItem(), item1.getBaseCoolDown(null));
                }
            }
        }
    }

    @SubscribeEvent
    public static void playerCapRespawnSync(PlayerEvent.PlayerRespawnEvent event) {
        event.getEntity().getCapability(PVZPlayerCapability.CAP).ifPresent(cap -> cap.sync(true));
    }

    @SubscribeEvent
    public static void playerCapRespawnSync(PlayerEvent.PlayerChangedDimensionEvent event) {
        event.getEntity().getCapability(PVZPlayerCapability.CAP).ifPresent(cap -> cap.sync(true));
    }

    @SubscribeEvent
    public static void addListener(AddReloadListenerEvent ev) {
        ev.addListener(new InvasionTypeManager());
        ev.addListener(new DataSkillManager());
    }

    @SubscribeEvent
    public static void disablePVZZombieReinforcement(ZombieEvent.SummonAidEvent event) {
        BlockPos pos = event.getEntity().getOnPos();
        event.getLevel().getCapability(PVZZombieEventCapability.CAP).ifPresent(cap -> {
            if (cap.getEventIn(Invasion.class, pos, T -> true, 25) != null) {
                event.setResult(Event.Result.DENY);
            }
        });
    }

    @SubscribeEvent
    public static void onEntityTick(LivingEvent.LivingTickEvent event) {
        LivingEntity entity = event.getEntity();
        Level level = entity.getLevel();
        if (! level.isClientSide && level.dimension().location().equals(PVZDimensions.ZEN_GARDEN)
                && level.getBiome(entity.blockPosition()).is(PVZBiomes.GARDEN_VOID.getKey())) {
            if (entity instanceof Player player) {
            }
        }
    }

    //called by PVZMod#onServerTick(ev).
    public static void tick(TickEvent.ServerTickEvent event) {
        //spawn player resources
        Map<ServerLevel, List<ServerPlayer>> playerMap = new HashMap<>();
        event.getServer().getPlayerList().getPlayers().forEach(player -> {
            ServerLevel level = player.getLevel();
            List<ServerPlayer> list = playerMap.getOrDefault(level, null);
            if (list == null) {
                list = new ArrayList<>();
                list.add(player);
                playerMap.put(level, list);
            }
        });
        for (ServerLevel level : playerMap.keySet()) {
            long time = level.getGameTime();
            int sunInt = PVZConfig.PVZGameRules.getInt(level, PVZConfig.Common.naturallySpawnSunInterval);
            List<ChunkPos> sunChunks = new ArrayList<>();
            int starInt = PVZConfig.PVZGameRules.getInt(level, PVZConfig.Common.naturallySpawnFallenStarInterval);
            List<ChunkPos> starChunks = new ArrayList<>();
            for (ServerPlayer player : playerMap.get(level)) {
                ChunkPos pos = player.chunkPosition();
                Random random = new Random(time + pos.x * 14 + pos.z * 37);
                for (int i = 0; i < 9; i ++) {
                    ChunkPos pos1 = new ChunkPos(pos.x - 1 + i % 3, pos.z - 1 + i / 3);
                    if (sunInt > 0 && ! level.getBiome(player.getOnPos()).is(PVZBiomeTags.UNABLE_SUN_FALLING)
                        && random.nextInt(sunInt) == Math.toIntExact(time % sunInt) && sunChunks.stream().noneMatch(c -> c.equals(pos1))) {
                        sunChunks.add(pos1);
                    }
                    if (level.isNight() && ! level.isRaining()) {
                        if (starInt > 0 && ! level.getBiome(player.getOnPos()).is(PVZBiomeTags.UNABLE_STAR_FALLING)
                                && random.nextInt(starInt) == Math.toIntExact(time % starInt) && sunChunks.stream().noneMatch(c -> c.equals(pos))) {
                            starChunks.add(pos);
                        }
                    }
                }
            }
            Random random = new Random();
            sunChunks.forEach(c -> {
                int x = c.x * 16 + random.nextInt(16);
                int z = c.z * 16 + random.nextInt(16);
                BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) + 10, z);
                int light = level.getBrightness(LightLayer.SKY, pos) - level.getSkyDarken();
                if (light > 9) {
                    Sun.spawnByAmount(level, light > 12 ? 50 : 25, pos, Vec3.ZERO);
                }
            });
            starChunks.forEach(c -> {
                int x = c.x * 16 + random.nextInt(16);
                int z = c.z * 16 + random.nextInt(16);
                BlockPos pos = new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z) + 30, z);
                FallenStar.spawnAt(level, pos);
            });
        }
    }

    //for test
//    @SubscribeEvent
//    public static void plantOnZombie(PVZPlantConditionMatchingEvent.OnEntity ev) {
//        if (ev.target instanceof Zombie) {
//            if (ev.isPlanting) {
//                ev.getEntity().startRiding(ev.target);
//            }
//            ev.result = null;
//        }
//    }
}
