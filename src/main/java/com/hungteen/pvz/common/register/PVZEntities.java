package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.model.MooBloomModel;
import com.hungteen.pvz.client.model.PennyModel;
import com.hungteen.pvz.client.model.SnailModel;
import com.hungteen.pvz.client.model.plants.*;
import com.hungteen.pvz.client.model.zombie.*;
import com.hungteen.pvz.client.renderer.EntityLifterRenderer;
import com.hungteen.pvz.client.renderer.ModelPartRenderer;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.client.renderer.SimpleMobRenderer;
import com.hungteen.pvz.client.renderer.bullet.*;
import com.hungteen.pvz.client.renderer.creatures.*;
import com.hungteen.pvz.client.renderer.misc.FallenStarRenderer;
import com.hungteen.pvz.client.renderer.misc.PVZBoatRenderer;
import com.hungteen.pvz.client.renderer.misc.SunRenderer;
import com.hungteen.pvz.client.renderer.plants.*;
import com.hungteen.pvz.client.renderer.zombies.*;
import com.hungteen.pvz.common.entity.*;
import com.hungteen.pvz.common.entity.bullet.*;
import com.hungteen.pvz.common.entity.creatures.*;
import com.hungteen.pvz.common.entity.npcs.Penny;
import com.hungteen.pvz.common.entity.plants.*;
import com.hungteen.pvz.common.entity.zombies.*;
import com.hungteen.pvz.common.entity.zombies.zombotany.*;
import com.hungteen.pvz.common.tags.PVZEntityTags;
import com.hungteen.pvz.generator.loot.EntityLootGen;
import com.hungteen.pvz.util.Util;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.ShulkerRenderer;
import net.minecraft.client.renderer.entity.ThrownItemRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.SpawnPlacements.SpawnPredicate;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Ghast;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.antlr.v4.runtime.misc.Triple;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.function.Supplier;

import static com.hungteen.pvz.util.Util.name;
import static com.hungteen.pvz.util.Util.prefix;

@SuppressWarnings("all")
public class PVZEntities {
    //init
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, PVZMod.MODID);
    public static final PVZEntities reflector = new PVZEntities();
    //client
    public static Map<EntityType<? extends Mob>,
            Triple<Function<ModelPart, ? extends EntityModel<? extends Mob>>, Supplier<LayerDefinition>, Float>> simpleRenderedMap
            = new HashMap<>();
    public static Map<EntityType<? extends Mob>, Function<Mob, ResourceLocation>> simpleTextureLocationMap = new HashMap<>();
    //collision
    private static Pair<Float, Float> storedCollision = Pair.of(0.6F, 1.95F);
    //update & client track
    private static int storedInterval = 3;
    private static int storedTrackingRange = 8;
    //spawn egg
    private static Pair<Integer, Integer> storedSpawnEgg = null;
    @Deprecated // will be cleared after register.
    public static Map<RegistryObject, Pair<Integer, Integer>> spawnEggMap = new HashMap<>();
    //spawn placements
    private static Triple<Type, Types, SpawnPredicate> storedSpawnPlacement = null;
    @Deprecated // will be cleared after register.
    public static Map<RegistryObject, Triple<Type, Types, SpawnPredicate>> spawnPlacementMap = new HashMap<>();
    //attributes
    private static Supplier<AttributeSupplier.Builder> storedAttribute = null;
    public static Map<RegistryObject, Supplier<AttributeSupplier.Builder>> attributesMap = new HashMap<>();
    //tag
    private static List<TagKey<EntityType<?>>> storedTags = null;
    @Deprecated // will be cleared after register.
    public static Map<RegistryObject, List<TagKey<EntityType<?>>>> tagMap = new HashMap<>();
    //save & summon
    public static boolean storedCanSave = true;
    public static boolean storedCanSummon = true;
    //fire immuine
    public static boolean storedFireImmuine = false;


    //registry
    /**
     * silly carp do not forget to add attributes. use {@link PVZEntities#attribute(Supplier)}.
     */
    public static final RegistryObject<EntityType<PVZBoat>> BOAT = collision(1.375F, 0.5625F).entity("pvz_boat", PVZBoat::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<PVZChestBoat>> CHEST_BOAT = collision(1.375F, 0.5625F).entity("pvz_chest_boat", PVZChestBoat::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<Sun>> SUN = collision(0.2F, 0.2F).entity("sun", Sun::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<FallenStar>> FALLEN_STAR = collision(0.4F, 0.4F).entity("fallen_star", FallenStar::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<MooBloom>> MOOBLOOM = summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, MooBloom::checkMooBloomSpawnRules)
            .spawnEgg(0xffc100, 0x88b830).attribute(MooBloom::createAttributes)
            .collision(0.9F, 1.4F).entity("moo_bloom", MooBloom::new, MobCategory.CREATURE);
    public static final RegistryObject<EntityType<GrassCarp>> GRASSCARP = summonRule(Type.IN_WATER, Types.MOTION_BLOCKING_NO_LEAVES, GrassCarp::checkGrassCarpSpawnRules)
            .spawnEgg(0x708849, 0xd4d78a).attribute(GrassCarp::createAttributes)
            .collision(0.4F, 0.4F).entity("grass_carp", GrassCarp::new, MobCategory.AXOLOTLS);
    public static final RegistryObject<EntityType<Snail>> SNAIL = summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Snail::checkSnailSpawnRules)
            .spawnEgg(0xa56f4a, 0xdad9b3).attribute(Snail::createAttributes)
            .collision(0.75F, 0.75F).entity("snail", Snail::new, MobCategory.CREATURE);
    public static final RegistryObject<EntityType<Snail>> WALL_NAIL = summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Snail::checkSnailSpawnRules)
            .spawnEgg(0xcda65d, 0xdad9b3).attribute(Snail::createAttributes)
            .collision(0.75F, 0.75F).entity("wallnail", Snail::new, MobCategory.CREATURE);
    public static final RegistryObject<EntityType<Snail>> FUNGICICOLIDAE = summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Snail::checkSnailSpawnRules)
            .spawnEgg(0xaa9395, 0xe5d3b5).attribute(Snail::createAttributes)
            .collision(0.75F, 0.75F).entity("fungicicolidae", Snail::new, MobCategory.CREATURE);
    public static final RegistryObject<EntityType<Anger>> ANGER = spawnEgg(0xff2f3b, 0xfff45b).attribute(Anger::createAttributes)
            .collision(0.4F, 0.4F).entity("anger", Anger::new, MobCategory.CREATURE);
    public static final RegistryObject<EntityType<Sprout>> SPROUT = attribute(Sprout::createAttributes).collision(0.4F, 0.4F)
            .entity("sprout", Sprout::new, MobCategory.CREATURE);
    public static final RegistryObject<EntityType<Penny>> PENNY = spawnEgg(0x737b85, 0xe24220).attribute(Penny::createAttributes)
            .collision(3f, 3f).entity("penny", Penny::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<EntityLifter>> ENTITY_LIFTER = collision(0.1F, 0.1F).noSummon()
            .entity("entity_lifter", EntityLifter::new, MobCategory.MISC);

    //client
    public static final RegistryObject<EntityType<ModelPartEntity>> MODEL_PART = collision(0.2F, 0.2F).noSummon().noSave()
            .entity("model_part", ModelPartEntity::new, MobCategory.MISC);

    //plants
    public static final RegistryObject<EntityType<WallNut>> WALL_NUT = attribute(WallNut::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, WallNut::checkSpawnRules)
            .collision(0.8F, 1F).entity("wall_nut", WallNut::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<SunFlower>> SUN_FLOWER = attribute(SunFlower::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, SunFlower::checkSpawnRules)
            .collision(0.75F, 1.1F).entity("sun_flower", SunFlower::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<MariGold>> MARIGOLD = attribute(MariGold::createAttributes).tag(PVZEntityTags.PLANT)
            .collision(0.75F, 1.0F).entity("marigold", MariGold::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<TallNut>> TALL_NUT = attribute(TallNut::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, TallNut::checkSpawnRules)
            .collision(1F, 1.9F).entity("tall_nut", TallNut::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<PeaShooter>> PEA_SHOOTER = attribute(PeaShooter::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, PeaShooter::checkSpawnRules)
            .collision(0.7F, 1.3F).entity("pea_shooter", PeaShooter::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<SplitPea>> SPLIT_PEA = attribute(SplitPea::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, SplitPea::checkSpawnRules)
            .collision(0.7F, 1.3F).entity("split_pea", SplitPea::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<Repeater>> REPEATER = attribute(Repeater::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Repeater::checkSpawnRules)
            .collision(0.7F, 1.3F).entity("repeater", Repeater::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<GatlingPea>> GATLING_PEA = attribute(GatlingPea::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, GatlingPea::checkSpawnRules)
            .collision(0.7F, 1.3F).entity("gatling_pea", GatlingPea::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<SnowPea>> SNOW_PEA = attribute(SnowPea::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, SnowPea::checkSpawnRules)
            .collision(0.7F, 1.3F).entity("snow_pea", SnowPea::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<TangleKelp>> TANGLE_KELP = attribute(TangleKelp::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, TangleKelp::checkSpawnRules)
            .collision(0.6F, 0.5F).entity("tangle_kelp", TangleKelp::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<Pumpkin>> PUMPKIN = attribute(Pumpkin::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Pumpkin::checkSpawnRules)
            .collision(1F, 0.5F).entity("pumpkin", Pumpkin::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<FlowerPot>> FLOWER_POT = attribute(FlowerPot::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, FlowerPot::checkSpawnRules).fireImmuine()
            .collision(0.75F, 0.475F).entity("flower_pot", FlowerPot::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<Starfruit>> STARFRUIT = attribute(Starfruit::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Starfruit::checkSpawnRules)
            .collision(0.8F, 0.55F).entity("starfruit", Starfruit::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<LilyPad>> LILY_PAD = attribute(LilyPad::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, LilyPad::checkSpawnRules)
            .collision(0.875F, 0.2F).entity("lily_pad", LilyPad::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<Plantern>> PLANTERN = attribute(Plantern::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Plantern::checkSpawnRules)
            .collision(0.8F, 2F).entity("plantern", Plantern::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<CabbagePult>> CABBAGE_PULT = attribute(CabbagePult::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, CabbagePult::checkSpawnRules)
            .collision(0.7F, 1F).entity("cabbage_pult", CabbagePult::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<MelonPult>> MELON_PULT = attribute(MelonPult::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, MelonPult::checkSpawnRules)
            .collision(0.9F, 1F).entity("melon_pult", MelonPult::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<UmbrellaLeaf>> UMBRELLA_LEAF = attribute(UmbrellaLeaf::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, UmbrellaLeaf::checkSpawnRules)
            .collision(0.8F, 0.8F).entity("umbrella_leaf", UmbrellaLeaf::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<PotatoMine>> POTATO_MINE = attribute(PotatoMine::createAttributes).tag(PVZEntityTags.PLANT, PVZEntityTags.MUST_PLANT_IN_DIRT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, PotatoMine::checkSpawnRules)
            .collision(0.4F, 0.4F).entity("potato_mine", PotatoMine::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<IcebergLettuce>> ICEBERG_LETTUCE = attribute(IcebergLettuce::createAttributes).tag(PVZEntityTags.PLANT, EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, IcebergLettuce::checkSpawnRules)
            .collision(0.4F, 0.4F).entity("iceberg_lettuce", IcebergLettuce::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<Jalapeno>> JALAPENO = attribute(Jalapeno::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Jalapeno::checkSpawnRules)
            .collision(0.6F, 1.6F).entity("jalapeno", Jalapeno::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<GoldBloom>> GOLD_BLOOM = attribute(GoldBloom::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, GoldBloom::checkSpawnRules)
            .collision(0.6F, 0.6F).entity("gold_bloom", GoldBloom::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<SpikeWeed>> SPIKE_WEED = attribute(SpikeWeed::createAttributes).tag(PVZEntityTags.PLANT, PVZEntityTags.MUST_PLANT_IN_DIRT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, SpikeWeed::checkSpawnRules)
            .collision(0.95F, 0.125F).entity("spike_weed", SpikeWeed::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<TorchWood>> TORCH_WOOD = attribute(TorchWood::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, TorchWood::checkSpawnRules)
            .collision(0.95F, 0.65F).entity("torch_wood", TorchWood::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<VelociRadish>> VELOCI_RADISH = attribute(VelociRadish::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, VelociRadish::checkSpawnRules)
            .collision(0.5F, 0.5825F).entity("veloci_radish", VelociRadish::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<KernelPult>> KERNEL_PULT = attribute(KernelPult::createAttributes).tag(PVZEntityTags.PLANT, PVZEntityTags.BUTTER_INVULNERABLE)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, KernelPult::checkSpawnRules)
            .collision(0.6F, 1F).entity("kernel_pult", KernelPult::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<Chomper>> CHOMPER = attribute(Chomper::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Chomper::checkSpawnRules)
            .collision(1.2F, 1.8F).entity("chomper", Chomper::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<HypnoShroom>> HYPNO_SHROOM = attribute(HypnoShroom::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, HypnoShroom::checkSpawnRules)
            .collision(0.8F, 1F).entity("hypno_shroom", HypnoShroom::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<Dandelion>> DANDELION = attribute(Dandelion::createAttributes).tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Dandelion::checkSpawnRules)
            .collision(0.7F, 1.3F).entity("dandelion", Dandelion::new, OtherRegisters.PVZPlantMobCategory);

    //zombies
    public static final RegistryObject<EntityType<PVZZombie>> ZOMBIE = attribute(Zombie::createAttributes).tag(PVZEntityTags.ZOMBIE, EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES)
            .spawnEgg(0xb97141, 0x799587)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, PVZZombie::checkSpawnRules)
            .entity("zombie", PVZZombie::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<PoleVaultingZombie>> POLE_VAULTING_ZOMBIE = attribute(PoleVaultingZombie::createAttributes).tag(PVZEntityTags.ZOMBIE, EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES)
            .spawnEgg(0xd1575b, 0x3d97dc)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, PVZZombie::checkSpawnRules)
            .entity("pole_vaulting_zombie", PoleVaultingZombie::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<SnorkelZombie>> SNORKEL_ZOMBIE = attribute(SnorkelZombie::createAttributes).tag(PVZEntityTags.ZOMBIE, EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES)
            .spawnEgg(0xffe300, 0xa03232)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, SnorkelZombie::checkSpawnRules)
            .entity("snorkel_zombie", SnorkelZombie::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<JackInABoxZombie>> JACK_IN_A_BOX_ZOMBIE = attribute(JackInABoxZombie::createAttributes).tag(PVZEntityTags.ZOMBIE, EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES)
            .spawnEgg(0xddd4d4, 0xcc4646)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, PVZZombie::checkSpawnRules)
            .entity("jack_in_a_box_zombie", JackInABoxZombie::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<DiggerZombie>> DIGGER_ZOMBIE = attribute(DiggerZombie::createAttributes).tag(PVZEntityTags.ZOMBIE, EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES)
            .spawnEgg(0x3e81bf, 0xff4834)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, PVZZombie::checkSpawnRules)
            .entity("digger_zombie", DiggerZombie::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<BungeeZombie>> BUNGEE_ZOMBIE = attribute(BungeeZombie::createAttributes).tag(PVZEntityTags.ZOMBIE, EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES)
            .spawnEgg(0x9b2e31, 0x8b7054)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, PVZZombie::checkSpawnRules)
            .entity("bungee_zombie", BungeeZombie::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<Gargantuar>> GARGANTUAR = attribute(Gargantuar::createAttributes).tag(PVZEntityTags.ZOMBIE, EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES)
            .spawnEgg(0x8a5949, 0x43446d)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, PVZZombie::checkSpawnRules)
            .collision(1.5F, 2.7F).entity("gargantuar", Gargantuar::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<Imp>> IMP = attribute(Imp::createAttributes).tag(PVZEntityTags.ZOMBIE, EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES)
            .spawnEgg(0xaa3a39, 0x454ab0)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, PVZZombie::checkSpawnRules)
            .entity("imp", Imp::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<LavaDiverZombie>> LAVA_DIVER_ZOMBIE = attribute(LavaDiverZombie::createAttributes).tag(PVZEntityTags.ZOMBIE, EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES)
            .spawnEgg(0x514b44, 0xffac00).fireImmuine()
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, LavaDiverZombie::checkSpawnRules)
            .entity("lava_diver_zombie", LavaDiverZombie::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<FireImp>> FIRE_IMP = attribute(FireImp::createAttributes).tag(PVZEntityTags.ZOMBIE, EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES)
            .spawnEgg(0x6e7a74, 0xffc000).fireImmuine()
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, FireImp::checkSpawnRules)
            .entity("fire_imp", FireImp::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<TacoImp>> TACO_IMP = attribute(TacoImp::createAttributes).tag(PVZEntityTags.ZOMBIE, EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES)
            .spawnEgg(0xfbc093, 0xa6de66)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, PVZZombie::checkSpawnRules)
            .entity("taco_imp", TacoImp::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<LavaGhastling>> LAVA_GHASTLING = attribute(LavaGhastling::createAttributes)
            .spawnEgg(0xfbc093, 0xa6de66).collision(0.95F, 0.95F).fireImmuine()
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, Ghast::checkGhastSpawnRules)
            .entity("lava_ghastling", LavaGhastling::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<PeaShooterZombie>> PEA_SHOOTER_ZOMBIE = attribute(PeaShooterZombie::createAttributes).tag(PVZEntityTags.ZOMBIE)
            .spawnEgg(0x799587, 0x90b030)
            .entity("pea_shooter_zombie", PeaShooterZombie::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<SnowPeaZombie>> SNOW_PEA_ZOMBIE = attribute(SnowPeaZombie::createAttributes).tag(PVZEntityTags.ZOMBIE)
            .spawnEgg(0x799587, 0x4bcecf)
            .entity("snow_pea_zombie", SnowPeaZombie::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<JalapenoZombie>> JALAPENO_ZOMBIE = attribute(JalapenoZombie::createAttributes).tag(PVZEntityTags.ZOMBIE, EntityTypeTags.AXOLOTL_ALWAYS_HOSTILES)
            .spawnEgg(0x799587, 0xff472a)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, PVZZombie::checkSpawnRules)
            .fireImmuine()
            .entity("jalapeno_zombie", JalapenoZombie::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<GatlingPeaZombie>> GATLING_PEA_ZOMBIE = attribute(GatlingPeaZombie::createAttributes).tag(PVZEntityTags.ZOMBIE)
            .spawnEgg(0x799587, 0x475d67)
            .entity("gatling_pea_zombie", GatlingPeaZombie::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<WallNutZombie>> WALL_NUT_ZOMBIE = attribute(WallNutZombie::createAttributes).tag(PVZEntityTags.ZOMBIE)
            .spawnEgg(0x799587, 0xd4b367)
            .entity("wall_nut_zombie", WallNutZombie::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<TallNutZombie>> TALL_NUT_ZOMBIE = attribute(TallNutZombie::createAttributes).tag(PVZEntityTags.ZOMBIE)
            .spawnEgg(0x799587, 0xcda65d)
            .entity("tall_nut_zombie", TallNutZombie::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<PumpkinZombie>> PUMPKIN_ZOMBIE = attribute(PumpkinZombie::createAttributes).tag(PVZEntityTags.ZOMBIE)
            .spawnEgg(0x799587, 0xdd854c)
            .entity("pumpkin_zombie", PumpkinZombie::new, MobCategory.MONSTER);


    //bosses
    public static final RegistryObject<EntityType<GhastRiderBoss>> GHAST_RIDER = attribute(GhastRiderBoss::createAttributes)
            .tag(PVZEntityTags.ZOMBIE, Tags.EntityTypes.BOSSES).fireImmuine()
            .entity("ghast_rider", GhastRiderBoss::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<EnderZomboss>> ENDER_ZOMBOSS = attribute(EnderZomboss::createAttributes)
            .tag(PVZEntityTags.ZOMBIE, Tags.EntityTypes.BOSSES).fireImmuine()
            .entity("ender_zomboss", EnderZomboss::new, MobCategory.MONSTER);

    //bullets
    public static final RegistryObject<EntityType<PeaBullet>> PEA = collision(0.4F, 0.4F)
            .trackRange(4).updateInterval(20)
            .entity("pea", PeaBullet::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<StarfruitBullet>> STARFRUIT_BULLET = collision(0.6F, 0.2F)
            .trackRange(4).updateInterval(20)
            .entity("starfruit_bullet", StarfruitBullet::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<CabbageBullet>> CABBAGE = collision(0.4F, 0.4F)
            .trackRange(4).updateInterval(20)
            .entity("cabbage", CabbageBullet::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<CornBullet>> CORN = collision(0.25F, 0.25F)
            .trackRange(4).updateInterval(20)
            .entity("corn", CornBullet::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<ButterBullet>> BUTTER = collision(0.5F, 0.5F)
            .trackRange(4).updateInterval(20)
            .entity("butter", ButterBullet::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<MelonBullet>> MELON = collision(0.8F, 0.8F)
            .trackRange(4).updateInterval(20)
            .entity("melon", MelonBullet::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<DandelionSeedBullet>> DANDELION_SEED = collision(0.4F, 0.4F)
            .trackRange(4).updateInterval(20)
            .entity("dandelion_seed", DandelionSeedBullet::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<SeedArrow>> SEED_ARROW = collision(0.2F, 0.2F).tag(EntityTypeTags.ARROWS)
            .trackRange(4).updateInterval(20)
            .entity("seed_arrow", SeedArrow::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<ArrowWithATarget>> ARROW_WITH_A_TARGET = collision(0.2F, 0.2F).tag(EntityTypeTags.ARROWS)
            .trackRange(4).updateInterval(20)
            .entity("arrow_with_a_target", ArrowWithATarget::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<Hook>> HOOK = collision(0.2F, 0.2F)
            .trackRange(4).updateInterval(20)
            .entity("hook", Hook::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<ThrownFogInBottle>> FOG_IN_BOTTLE = collision(0.25F, 0.25F)
            .trackRange(4).updateInterval(20)
            .entity("fog_in_bottle", ThrownFogInBottle::new, MobCategory.MISC);

    //client
    /** For simply rendered entities (accepts only Mob!), auto render at {@link PVZEntities#simpleRenderHandler()}.
     * <br>
     * <br> For other renderers, register renderer at {@link PVZEntities#registerRenderer(EntityRenderersEvent.RegisterRenderers)}. And then handle ModelLayers and LayerDefinitions in {@link PVZLayerHandler#createModelDefinitions(EntityRenderersEvent.RegisterLayerDefinitions)}.
     * <br>
     * <br> LootTables gen in {@link EntityLootGen#addTables()}.
     */

    @OnlyIn(Dist.CLIENT)
    public static void simpleRenderHandler() {
        rS(MOOBLOOM, MooBloomModel::new, MooBloomModel::createBodyLayer, 0.7F);
        rS(PEA_SHOOTER, PeaShooterModel::new, PeaShooterModel::createBodyLayer, 0.5F, "textures/entity/plants/pea_shooter/pea_shooter.png");
        rS(STARFRUIT, StarfruitModel::new, StarfruitModel::createBodyLayer, 0.5F, "textures/entity/plants/starfruit/starfruit.png");
        rS(SPLIT_PEA, SplitPeaModel::new, SplitPeaModel::createBodyLayer, 0.5F, "textures/entity/plants/split_pea/split_pea.png");
        rS(REPEATER, RepeaterModel::new, RepeaterModel::createBodyLayer, 0.5F, "textures/entity/plants/repeater/repeater.png");
        rS(CABBAGE_PULT, CabbagePultModel::new, CabbagePultModel::createBodyLayer, 0.5F, "textures/entity/plants/cabbage_pult/cabbage_pult.png");
        rS(VELOCI_RADISH, VelociTurnipModel::new, VelociTurnipModel::createBodyLayer, 0.5F, "textures/entity/plants/veloci_radish/veloci_radish.png");
        rS(LILY_PAD, LilyPadModel::new, LilyPadModel::createBodyLayer, 0.5F, "textures/entity/plants/lily_pad/lily_pad.png");
        rS(KERNEL_PULT, KernelPultModel::new, KernelPultModel::createBodyLayer, 0.5F, "textures/entity/plants/kernel_pult/kernel_pult.png");
        rS(UMBRELLA_LEAF, UmbrellaLeafModel::new, UmbrellaLeafModel::createBodyLayer, 0.5F, "textures/entity/plants/umbrella_leaf/umbrella_leaf.png");
        rS(MELON_PULT, MelonPultModel::new, MelonPultModel::createBodyLayer, 0.5F, "textures/entity/plants/melon_pult/melon_pult.png");
        rS(ICEBERG_LETTUCE, IcebergLettuceModel::new, IcebergLettuceModel::createBodyLayer, 0F, "textures/entity/plants/iceberg_lettuce/iceberg_lettuce.png");
        rS(PENNY, PennyModel::new, PennyModel::createBodyLayer, 1.5F);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers e) {
        r(e, BOAT, (c) -> new PVZBoatRenderer(c, false));
        r(e, CHEST_BOAT, (c) -> new PVZBoatRenderer(c, true));
        r(e, GRASSCARP, GrassCarpRenderer::new);
        r(e, SNAIL, c -> new SnailRenderer(c, SnailModel.Type.snail));
        r(e, WALL_NAIL, c -> new SnailRenderer(c, SnailModel.Type.wall_nail));
        r(e, FUNGICICOLIDAE, c -> new SnailRenderer(c, SnailModel.Type.fungicocilidae));
        r(e, ANGER, AngerRenderer::new);
        r(e, WALL_NUT, WallNutRenderer::new);
        r(e, PUMPKIN, PumpkinRenderer::new);
        r(e, SNOW_PEA, SnowPeaRenderer::new);
        r(e, SUN, SunRenderer::new);
        r(e, SUN_FLOWER, SunFlowerRenderer::new);
        r(e, TANGLE_KELP, TangleKelpRenderer::new);
        r(e, JALAPENO, JalapenoRenderer::new);
        r(e, MARIGOLD, MariGoldRenderer::new);
        r(e, SPIKE_WEED, SpikeWeedRenderer::new);
        r(e, TORCH_WOOD, TorchWoodRenderer::new);
        r(e, TALL_NUT, TallNutRenderer::new);
        r(e, PLANTERN, PlanternRenderer::new);
        r(e, FLOWER_POT, FlowerPotRenderer::new);
        r(e, GATLING_PEA, GatlingPeaRenderer::new);
        r(e, PEA, PeaBulletRenderer::new);
        r(e, FOG_IN_BOTTLE, ctx -> new ThrownItemRenderer(ctx, 1, false));
        r(e, STARFRUIT_BULLET, StarfruitBulletRenderer::new);
        r(e, CABBAGE, CabbageBulletRenderer::new);
        r(e, CORN, CornBulletRenderer::new);
        r(e, BUTTER, ButterBulletRenderer::new);
        r(e, MELON, MelonBulletRenderer::new);
        r(e, DANDELION_SEED, DandelionSeedBulletRenderer::new);
        r(e, POTATO_MINE, PotatoMineRenderer::new);
        r(e, CHOMPER, ChomperRenderer::new);
        r(e, GOLD_BLOOM, GoldBloomRenderer::new);
        r(e, DANDELION, DandelionRenderer::new);
        r(e, HYPNO_SHROOM, HypnoShroomRenderer::new);
        r(e, ZOMBIE, PVZZombieRenderer::new);
        r(e, POLE_VAULTING_ZOMBIE, PoleVaultingZombieRenderer::new);
        r(e, JACK_IN_A_BOX_ZOMBIE, JackInABoxZombieRenderer::new);
        r(e, DIGGER_ZOMBIE, DiggerZombieRenderer::new);
        r(e, BUNGEE_ZOMBIE, BungeeZombieRenderer::new);
        r(e, SNORKEL_ZOMBIE, SnorkelZombieRenderer::new);
        r(e, LAVA_DIVER_ZOMBIE, LavaDiverZombieRenderer::new);
        r(e, IMP, ImpRenderer::new);
        r(e, FIRE_IMP, FireImpRenderer::new);
        r(e, TACO_IMP, TacoImpRenderer::new);
        r(e, GARGANTUAR, GargantuarRenderer::new);
        r(e, LAVA_GHASTLING, LavaGhastlingRenderer::new);
        r(e, GHAST_RIDER, GhastRiderRenderer::new);
        r(e, ENDER_ZOMBOSS, ShulkerRenderer::new);
        r(e, SPROUT, SproutRenderer::new);
        r(e, SEED_ARROW, SeedArrowRenderer::new);
        r(e, ARROW_WITH_A_TARGET, ArrowWithATargetRenderer::new);
        r(e, HOOK, HookRenderer::new);
        r(e, MODEL_PART, ModelPartRenderer::new);
        r(e, ENTITY_LIFTER, EntityLifterRenderer::new);
        r(e, FALLEN_STAR, FallenStarRenderer::new);
        r(e, PEA_SHOOTER_ZOMBIE, ctx -> new ZombotanyRenderer(ctx, PeaShooterZombieModel.class));
        r(e, SNOW_PEA_ZOMBIE, ctx -> new ZombotanyRenderer(ctx, SnowPeaZombieModel.class));
        r(e, GATLING_PEA_ZOMBIE, ctx -> new ZombotanyRenderer(ctx, GatlingPeaZombieModel.class));
        r(e, JALAPENO_ZOMBIE, ctx -> new ZombotanyRenderer(ctx, JalapenoZombieModel.class));
        r(e, WALL_NUT_ZOMBIE, ctx -> new ZombotanyRenderer(ctx, WallNutZombieModel.class));
        r(e, TALL_NUT_ZOMBIE, ctx -> new ZombotanyRenderer(ctx, TallNutZombieModel.class));
        r(e, PUMPKIN_ZOMBIE, ctx -> new ZombotanyRenderer(ctx, ZombotanyModel.class));

        //enter here

        //auto works
        rendererSimple(e);
    }


    //definitions
    private static <T extends Entity> RegistryObject<EntityType<T>> entity(String name, EntityType.EntityFactory<T> factory, MobCategory classification) {
        float coh = storedCollision.getFirst();
        float cov = storedCollision.getSecond();
        EntityType.Builder<T> builder = EntityType.Builder.of(factory, classification).sized(coh, cov)
                .updateInterval(storedInterval).clientTrackingRange(storedTrackingRange);
        if (! storedCanSummon) {
            builder.noSummon();
        }
        if (! storedCanSave) {
            builder.noSave();
        }
        if (storedFireImmuine) {
            builder.fireImmune();
        }
        Supplier<EntityType<T>> supplier = () -> {
            return builder.build(Util.prefix(name).toString());
        };
        RegistryObject<EntityType<T>> entity = ENTITIES.register(name, supplier);
        storedCanSummon = true;
        storedCanSave = true;
        storedFireImmuine = false;
        storedCollision = Pair.of(0.6F, 1.95F);
        storedInterval = 3;
        storedTrackingRange = 8;
        //spawn egg
        if (storedSpawnEgg != null) {
            spawnEggMap.put(entity, storedSpawnEgg);
            storedSpawnEgg = null;
        }
        //summon rule
        if (storedSpawnPlacement != null) {
            spawnPlacementMap.put(entity, storedSpawnPlacement);
            storedSpawnPlacement = null;
        }
        //attributes
        if (storedAttribute != null) {
            attributesMap.put(entity, storedAttribute);
            storedAttribute = null;
        }
        //tag
        if (storedTags != null) {
            tagMap.put(entity, storedTags);
            storedTags = null;
        }

        return entity;
    }

    private static PVZEntities collision(float width, float height) {
        storedCollision = Pair.of(width, height);
        return reflector;
    }
    private static PVZEntities updateInterval(int interval) {
        storedInterval = interval;
        return reflector;
    }
    private static PVZEntities trackRange(int range) {
        storedTrackingRange = range;
        return reflector;
    }
    private static PVZEntities tag(TagKey<EntityType<?>>... tags){
        return tag(Arrays.asList(tags));
    }
    private static PVZEntities tag(List<TagKey<EntityType<?>>> list){
        storedTags = list;
        return reflector;
    }

    private static PVZEntities spawnEgg(Integer bgColor, Integer hlColor) {
        storedSpawnEgg = Pair.of(bgColor, hlColor);
        return reflector;
    }

    private static PVZEntities summonRule(Type type, Types types, SpawnPredicate predicate) {
        storedSpawnPlacement = new Triple<>(type, types, predicate);
        return reflector;
    }

    private static PVZEntities noSummon() {
        storedCanSummon = false;
        return reflector;
    }
    private static PVZEntities noSave() {
        storedCanSave = false;
        return reflector;
    }

    private static PVZEntities fireImmuine() {
        storedFireImmuine = true;
        return reflector;
    }

    private static PVZEntities attribute(Supplier<AttributeSupplier.Builder> attribute) {
        storedAttribute = attribute;
        return reflector;
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static <T extends Entity> void r(EntityRenderersEvent.RegisterRenderers event, RegistryObject<EntityType<T>> entity, EntityRendererProvider rendererMethod) {
        event.registerEntityRenderer(entity.get(), rendererMethod);
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void rendererSimple(EntityRenderersEvent.RegisterRenderers event) {
        for (EntityType<?> entity : simpleRenderedMap.keySet()) {
            event.registerEntityRenderer(entity, (context) -> new SimpleMobRenderer(context, entity));
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends Mob> void rS(RegistryObject<EntityType<T>> entity, Function<ModelPart, EntityModel<T>> model, Supplier<LayerDefinition> layer, float shadowSize, Function<T, ResourceLocation> textureDirectory) {
        simpleRenderedMap.put(entity.get(), new Triple<>(model, layer, shadowSize));
        simpleTextureLocationMap.put(entity.get(), (Function<Mob, ResourceLocation>) textureDirectory);
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends Mob> void rS(RegistryObject<EntityType<T>> entity, Function<ModelPart, EntityModel<T>> model, Supplier<LayerDefinition> layer, float shadowSize, String textureDirectory) {
        rS(entity, model, layer, shadowSize, (mob) -> prefix(textureDirectory));
    }

    @OnlyIn(Dist.CLIENT)
    public static <T extends Mob> void rS(RegistryObject<EntityType<T>> entity, Function<ModelPart, EntityModel<T>> model, Supplier<LayerDefinition> layer, float shadowSize) {
        rS(entity, model, layer, shadowSize, "textures/entity/" + name(entity) + "/" + name(entity.get()) + ".png");
    }

    @SubscribeEvent
    public static void addSummonRules(SpawnPlacementRegisterEvent e) {
        spawnPlacementMap.forEach((obj, triple) ->
                e.register((EntityType<? extends Entity>) obj.get(), triple.a, triple.b, triple.c,
                        SpawnPlacementRegisterEvent.Operation.OR
                )
        );
    }

    @SubscribeEvent
    public static void addEntityAttributes(EntityAttributeCreationEvent e) {
        attributesMap.forEach((obj, sup) -> e.put((EntityType<? extends LivingEntity>) obj.get(), sup.get().build()));
    }

    public static void release() {
        List.of(spawnEggMap, spawnPlacementMap, tagMap).forEach(Map::clear);
    }
}
