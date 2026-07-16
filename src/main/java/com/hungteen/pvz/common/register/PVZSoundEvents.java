package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.util.Util;
import net.minecraft.sounds.SoundEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("all")
public class PVZSoundEvents {

    public static final DeferredRegister<SoundEvent> SOUNDS = DeferredRegister.create(ForgeRegistries.SOUND_EVENTS, PVZMod.MODID);

    private static final PVZSoundEvents reflector = new PVZSoundEvents();

    private static Map<String, Integer> storedLocation = new HashMap<>();
    public static final Map<RegistryObject<SoundEvent>, Map<String, Integer>> locationMap = new HashMap<>();
    private static float storedVolume = 1f;
    public static final Map<RegistryObject<SoundEvent>, Float> volumeMap = new HashMap<>();
    private static boolean storedStream = false;
    public static final Set<RegistryObject<SoundEvent>> streamSet = new HashSet<>();
    private static boolean storedNoGeneration = false;
    public static final Set<RegistryObject<SoundEvent>> generationSet = new HashSet<>();

    public static final RegistryObject<SoundEvent> SPROUT_WATER = sound("entity.plant.sprout.water");
    public static final RegistryObject<SoundEvent> SPROUT_FERTILIZE = sound("entity.plant.sprout.fertilize");
    public static final RegistryObject<SoundEvent> SPROUT_GROW = loc(2).sound("entity.plant.sprout.grow", 24);
    public static final RegistryObject<SoundEvent> SPROUT_HARVEST = loc(2).sound("entity.plant.sprout.harvest", 24);
    public static final RegistryObject<SoundEvent> MARIGOLD_PRODUCE = loc(2).sound("entity.plant.marigold.produce", 24);
    public static final RegistryObject<SoundEvent> MARIGOLD_PRODUCE_GEMS = sound("entity.plant.marigold.produce_gems", 24);

    public static final RegistryObject<SoundEvent> GOLD_BLOOM_PRODUCE = loc(3).sound("entity.plant.gold_bloom.produce");
    public static final RegistryObject<SoundEvent> WALL_NUT_ROLL = sound("entity.plant.wall_nut.roll", 8);
    public static final RegistryObject<SoundEvent> WALL_NUT_HIT = loc(3).sound("entity.plant.wall_nut.hit", 32);
    public static final RegistryObject<SoundEvent> POTATO_MINE_EMERGE = sound("entity.plant.potato_mine.emerge");
//    public static final RegistryObject<SoundEvent> POTATO_MINE_EXPLODE = loc(2).sound("entity.plant.potato_mine.explode");
    public static final RegistryObject<SoundEvent> ICEBERG_LETTUCE_EXPLODE = sound("entity.plant.iceberg_lettuce.explode");
    public static final RegistryObject<SoundEvent> JALAPENO_EXPLODE = loc(2).sound("entity.plant.jalapeno.explode", 32);
    public static final RegistryObject<SoundEvent> TANGLE_KELP_ATTACK = loc(4).sound("entity.plant.tangle_kelp.attack", 4);
    public static final RegistryObject<SoundEvent> BUCKET_FILL_TANGLE_KELP = sound("item.bucket.fill_tangle_kelp");
    public static final RegistryObject<SoundEvent> SPIKE_WEED_ATTACK = sound("entity.plant.spike_weed.attack");
    public static final RegistryObject<SoundEvent> VELOCI_RADISH_ATTACK = loc(3).sound("entity.plant.veloci_radish.attack", 8);
    public static final RegistryObject<SoundEvent> VELOCI_RADISH_STRONG_ATTACK = loc(2).sound("entity.plant.veloci_radish.strong_attack");
    public static final RegistryObject<SoundEvent> UMBRELLA_LEAF_BOUNCE = loc(3).sound("entity.plant.umbrella_leaf.bounce");
    public static final RegistryObject<SoundEvent> TORCH_WOOD_IGNITE = loc(2).sound("entity.plant.torch_wood.ignite", 4);
    public static final RegistryObject<SoundEvent> HYPNO_SHROOM_TRANSFORM = loc(3).sound("entity.plant.hypno_shroom.transform", 24);
    public static final RegistryObject<SoundEvent> CHOMPER_END_BURROW = loc(2).sound("entity.plant.chomper.end_burrow");
    public static final RegistryObject<SoundEvent> CHOMPER_START_BURROW = sound("entity.plant.chomper.start_burrow");
    public static final RegistryObject<SoundEvent> CHOMPER_CHEW = loc(3).sound("entity.plant.chomper.chew", 4);
    public static final RegistryObject<SoundEvent> CHOMPER_ATTACK_END = sound("entity.plant.chomper.attack_end");
    public static final RegistryObject<SoundEvent> CHOMPER_ATTACK = sound("entity.plant.chomper.attack");
    public static final RegistryObject<SoundEvent> GATLING_PEA_CONTINUAL_SHOOT = sound("entity.plant.gatling_pea.continual_shoot");

    public static final RegistryObject<SoundEvent> PEA_SHOOT = loc(4).sound("entity.projectile.pea.shoot");
    public static final RegistryObject<SoundEvent> PEA_SNIPER_SHOOT = loc(4).sound("entity.projectile.pea.shoot_sniper", 40);
    public static final RegistryObject<SoundEvent> PEA_HIT = loc(3).sound("entity.projectile.pea.hit", 8);
    public static final RegistryObject<SoundEvent> SNOW_PEA_HIT = loc(3).sound("entity.projectile.pea.hit_snow", 8);
    public static final RegistryObject<SoundEvent> SNOW_PEA_FREEZE = sound("entity.projectile.pea.freeze");
    public static final RegistryObject<SoundEvent> FIRE_PEA_HIT = loc(2).sound("entity.projectile.pea.hit_fire");
    public static final RegistryObject<SoundEvent> CABBAGE_SHOOT = loc(2).sound("entity.projectile.cabbage.shoot");
    public static final RegistryObject<SoundEvent> CABBAGE_HIT = loc(6).sound("entity.projectile.cabbage.hit", 8);
    public static final RegistryObject<SoundEvent> CORN_SHOOT = loc(3).sound("entity.projectile.corn.shoot");
    public static final RegistryObject<SoundEvent> CORN_HIT = loc(4).sound("entity.projectile.corn.hit", 8);
    public static final RegistryObject<SoundEvent> BUTTER_SHOOT = loc(3).sound("entity.projectile.butter.shoot");
    public static final RegistryObject<SoundEvent> BUTTER_HIT = sound("entity.projectile.butter.hit");
    public static final RegistryObject<SoundEvent> MELON_SHOOT = loc(3).sound("entity.projectile.melon.shoot");
    public static final RegistryObject<SoundEvent> MELON_HIT = loc(2).sound("entity.projectile.melon.hit");
    public static final RegistryObject<SoundEvent> STAR_SHOOT = loc(3).sound("entity.projectile.starfruit.shoot");
    public static final RegistryObject<SoundEvent> STAR_HIT = loc(2).sound("entity.projectile.starfruit.hit", 8);
    public static final RegistryObject<SoundEvent> DANDELION_SHOOT = loc(3).sound("entity.projectile.dandelion.shoot");
    public static final RegistryObject<SoundEvent> DANDELION_HIT = loc(2).sound("entity.projectile.dandelion.hit");

    public static final RegistryObject<SoundEvent> COLLECT_SUN = loc(4).sound("entity.sun.collect", 4);

    public static final RegistryObject<SoundEvent> PLANT = loc(2).sound("entity.plant.plant");
    public static final RegistryObject<SoundEvent> PLANT_WATER = sound("entity.plant.plant_water");
    public static final RegistryObject<SoundEvent> SHOVEL_PLANT = sound("item.shovel.shovel_plant");

    public static final RegistryObject<SoundEvent> EQUIP_CONE = sound("item.armor.equip_cone");
    public static final RegistryObject<SoundEvent> EQUIP_LIFEBUOY = sound("item.armor.equip_lifebuoy");
    public static final RegistryObject<SoundEvent> EQUIP_PUMPKIN = sound("item.armor.equip_pumpkin");

    public static final RegistryObject<SoundEvent> DAMAGE_CONE = loc(2).sound("item.armor.damage_cone");
    public static final RegistryObject<SoundEvent> DAMAGE_METAL = loc(2).sound("item.armor.damage_metal");
    public static final RegistryObject<SoundEvent> DAMAGE_PUMPKIN = sound("item.armor.damage_pumpkin");

    public static final RegistryObject<SoundEvent> ZOMBIE_AMBIENT = loc(9).sound("entity.zombie.ambient");
    public static final RegistryObject<SoundEvent> ZOMBIE_EMERGE = sound("entity.zombie.emerge", 24);
    public static final RegistryObject<SoundEvent> POLE_VAULTING = sound("entity.zombie.pole_vaulting_zombie.polevault");
    public static final RegistryObject<SoundEvent> JACK_IN_A_BOX_ZOMBIE_SURPRISE = loc(2).sound("entity.zombie.jack_in_a_box_zombie.surprise");
    public static final RegistryObject<SoundEvent> DIGGER_ZOMBIE_DIG = sound("entity.zombie.digger_zombie.dig");
//    public static final RegistryObject<SoundEvent> HEAVY_ZOMBIE_AMBIENT = sound("entity.zombie.zombie.ambient_heavy");
//    public static final RegistryObject<SoundEvent> HEAVY_ZOMBIE_DEATH = sound("entity.zombie.death_heavy");
    public static final RegistryObject<SoundEvent> TACO_IMP_AMBIENT = loc(3).sound("entity.zombie.taco_imp.ambient");
    public static final RegistryObject<SoundEvent> GHAST_RIDER_SPELL = loc(4).sound("entity.zombie.ghast_rider.spell", 40);
//    public static final RegistryObject<SoundEvent> GHAST_RIDER_AMBIENT = sound("entity.ghast_rider.ambient");
//    public static final RegistryObject<SoundEvent> GHAST_RIDER_LINKED = sound("entity.ghast_rider.linked");
//    public static final RegistryObject<SoundEvent> GHAST_RIDER_TELEPORT = sound("entity.ghast_rider.teleport");
//    public static final RegistryObject<SoundEvent> GHAST_RIDER_WALK = sound("entity.ghast_rider.walk");
    public static final RegistryObject<SoundEvent> IMP_AMBIENT = loc(3).sound("entity.zombie.imp.ambient");
    public static final RegistryObject<SoundEvent> IMP_DEATH = loc(3).sound("entity.zombie.imp.death", 8);
    public static final RegistryObject<SoundEvent> IMP_THROWN = loc(3).sound("entity.zombie.imp.throw", 32);
    public static final RegistryObject<SoundEvent> GARGANTUAR_AMBIENT = loc(4).sound("entity.zombie.gargantuar.ambient");
    public static final RegistryObject<SoundEvent> GARGANTUAR_DEATH = sound("entity.zombie.gargantuar.death");
    public static final RegistryObject<SoundEvent> GARGANTUAR_STEP = loc(4).sound("entity.zombie.gargantuar.step", 32);
    public static final RegistryObject<SoundEvent> GARGANTUAR_ATTACK = loc(6).sound("entity.zombie.gargantuar.attack", 24);
    public static final RegistryObject<SoundEvent> BUNGEE_ZOMBIE_SCREAM = loc(3).sound("entity.zombie.bungee_zombie.scream", 24);
    public static final RegistryObject<SoundEvent> BUNGEE_ZOMBIE_STEAL = sound("entity.zombie.bungee_zombie.steal");
    public static final RegistryObject<SoundEvent> HOOK_HIT = loc(3).sound("entity.hook.hit", 32);
    public static final RegistryObject<SoundEvent> HOOK_LAUNCH = sound("entity.hook.launch");
//    public static final RegistryObject<SoundEvent> LAVA_GHASTLING_AMBIENT = sound("entity.lava_ghastling.ambient");
//    public static final RegistryObject<SoundEvent> LAVA_GHASTLING_HURT = sound("entity.lava_ghastling.hurt");
//    public static final RegistryObject<SoundEvent> LAVA_GHASTLING_DEATH = sound("entity.lava_ghastling.death");
//    public static final RegistryObject<SoundEvent> LAVA_GHASTLING_SCREAM = sound("entity.lava_ghastling.scream");
//    public static final RegistryObject<SoundEvent> LAVA_GHASTLING_SHOOT = sound("entity.lava_ghastling.shoot");
//    public static final RegistryObject<SoundEvent> LAVA_GHASTLING_WARN = sound("entity.lava_ghastling.warn");

    public static final RegistryObject<SoundEvent> ANVIL_HAMMER_CRASH = sound("item.anvil_hammer.crash");
    public static final RegistryObject<SoundEvent> JACK_IN_THE_BOX_MUSIC = sound("item.jack_in_the_box.music");
    public static final RegistryObject<SoundEvent> JACK_IN_THE_BOX_MUSIC_CHARGED = sound("item.jack_in_the_box.music_charged");
    public static final RegistryObject<SoundEvent> CHILI_CHAN_KNOCKBACK = sound("item.chili_chan.knockback");
    public static final RegistryObject<SoundEvent> SNAIL_GACHAPON_USE = sound("item.snail_gachapon.use");
    public static final RegistryObject<SoundEvent> LOOT_BAG_USE = sound("item.loot_bag.use");

    public static final RegistryObject<SoundEvent> INVASION_START = sound("zombie_event.invasion.start");
    public static final RegistryObject<SoundEvent> INVASION_BIG_WAVE = sound("zombie_event.invasion.big_wave");
    public static final RegistryObject<SoundEvent> INVASION_FINAL_WAVE = sound("zombie_event.invasion.final_wave");
//    public static final RegistryObject<SoundEvent> INVASION_SUCCESS = sound("zombie_event.invasion.success");
//    public static final RegistryObject<SoundEvent> INVASION_FAILURE = sound("zombie_event.invasion.failure");

    public static final RegistryObject<SoundEvent> GRASS_CARP_HURT = noGen().sound("entity.grass_carp.hurt");
    public static final RegistryObject<SoundEvent> GRASS_CARP_DEATH = noGen().sound("entity.grass_carp.death");
    public static final RegistryObject<SoundEvent> GRASS_CARP_IDLE_WATER = noGen().sound("entity.grass_carp.idle.water");
    public static final RegistryObject<SoundEvent> GRASS_CARP_IDLE_AIR = noGen().sound("entity.grass_carp.idle");
    public static final RegistryObject<SoundEvent> GRASS_CARP_SPLASH = noGen().sound("entity.grass_carp.splash");
    public static final RegistryObject<SoundEvent> GRASS_CARP_FLOP = noGen().sound("entity.grass_carp.flop");
    public static final RegistryObject<SoundEvent> GRASS_CARP_SWIM = noGen().sound("entity.grass_carp.swim");
    public static final RegistryObject<SoundEvent> GRASS_CARP_SHEAR = noGen().sound("entity.grass_carp.shear");
    public static final RegistryObject<SoundEvent> SNAIL_AMBIENT = noGen().sound("entity.snail.ambient");
    public static final RegistryObject<SoundEvent> SNAIL_RETREAT = sound("entity.snail.retreat");
//    public static final RegistryObject<SoundEvent> SNAIL_EMERGE = noGen().sound("entity.snail.emerge");
    public static final RegistryObject<SoundEvent> SNAIL_DAMAGE_BLOCKED = noGen().sound("entity.snail.block_damage");//TODO not used yet
    public static final RegistryObject<SoundEvent> SNAIL_HURT = noGen().sound("entity.snail.hurt");
    public static final RegistryObject<SoundEvent> PENNY_SUMMON = loc(2).sound("entity.penny.summon", 64);
    public static final RegistryObject<SoundEvent> PENNY_AMBIENT = loc(4).sound("entity.penny.ambient");
    public static final RegistryObject<SoundEvent> PENNY_TRADE = loc(5).sound("entity.penny.trade");

    public static final RegistryObject<SoundEvent> ESSENCE_ALTAR_USE = sound("block.essence_altar.use");
    public static final RegistryObject<SoundEvent> ESSENCE_FURNACE_AMBIENT = loc(3).sound("block.essence_furnace.ambient");
    public static final RegistryObject<SoundEvent> GARDEN_PORTAL_AMBIENT = sound("block.zen_garden_portal.ambient");
    public static final RegistryObject<SoundEvent> GARDEN_PORTAL_USE = sound("block.zen_garden_portal.use", 32);

    public static final RegistryObject<SoundEvent> MUSIC_DISC_ZEN_GARDEN = sound("music_disc.zen_garden");

    private static PVZSoundEvents loc(String location) {
        storedLocation.put(location, 5);
        return reflector;
    }
    private static PVZSoundEvents loc(String location, int weight) {
        storedLocation.put(location, weight);
        return reflector;
    }
    private static PVZSoundEvents loc(int num) {
        storedLocation.put("", num);
        return reflector;
    }
    private static PVZSoundEvents noGen() {
        storedNoGeneration = true;
        return reflector;
    }
    private static PVZSoundEvents volume(float volume) {
        storedVolume = volume;
        return reflector;
    }
    private static PVZSoundEvents stream() {
        storedStream = true;
        return reflector;
    }
    private static RegistryObject<SoundEvent> sound(String name, Supplier<SoundEvent> supplier) {
        RegistryObject<SoundEvent> result = SOUNDS.register(name, supplier);
        if (! storedNoGeneration) generationSet.add(result);
        if (storedStream) streamSet.add(result);
        volumeMap.put(result, storedVolume);
        if (! storedLocation.isEmpty()){
            if (storedLocation.containsKey("")) {
                int num = storedLocation.get("");
                storedLocation.remove("");
                String prefix = result.getId().getPath().replaceAll("\\.", "/");
                for (int i = 0; i < num; i ++) {
                    storedLocation.put(prefix + "_" + i, 5);
                }
            }
            locationMap.put(result, Map.copyOf(storedLocation));
        }
        storedVolume = 1;
        storedLocation.clear();
        storedNoGeneration = false;
        storedStream = false;
        return result;
    }
    private static RegistryObject<SoundEvent> sound(String name) {
        return sound(name, () -> new SoundEvent(Util.prefix(name)));
    }
    private static RegistryObject<SoundEvent> sound(String name, float range) {
        return sound(name, () -> new SoundEvent(Util.prefix(name), range));
    }
}
