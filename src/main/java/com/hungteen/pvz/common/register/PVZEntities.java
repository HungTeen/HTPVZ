package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.model.MooBloomModel;
import com.hungteen.pvz.client.model.plants.*;
import com.hungteen.pvz.client.renderer.ModelPartRenderer;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.client.renderer.SimpleMobRenderer;
import com.hungteen.pvz.client.renderer.bullet.*;
import com.hungteen.pvz.client.renderer.creatures.AngerRenderer;
import com.hungteen.pvz.client.renderer.creatures.GrassCarpRenderer;
import com.hungteen.pvz.client.renderer.creatures.SproutRenderer;
import com.hungteen.pvz.client.renderer.misc.PVZBoatRenderer;
import com.hungteen.pvz.client.renderer.misc.SunRenderer;
import com.hungteen.pvz.client.renderer.plants.*;
import com.hungteen.pvz.client.renderer.zombie.*;
import com.hungteen.pvz.common.entity.*;
import com.hungteen.pvz.common.entity.bullet.*;
import com.hungteen.pvz.common.entity.plants.*;
import com.hungteen.pvz.common.entity.zombies.*;
import com.hungteen.pvz.common.tags.PVZEntityTags;
import com.hungteen.pvz.generator.loot.EntityLootGen;
import com.hungteen.pvz.util.Util;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.EntityTypeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.SpawnPlacements.SpawnPredicate;
import net.minecraft.world.entity.SpawnPlacements.Type;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.levelgen.Heightmap.Types;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.event.entity.SpawnPlacementRegisterEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
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
    public static Map<EntityType<? extends Entity>, List</*0:model, 1:layerDefinition, 2:shadowSize*/?>> simpleRenderedMap = new HashMap<>();
    public static Map<EntityType<? extends Entity>, Function<Mob, ResourceLocation>> simpleTextureLocationMap = new HashMap<>();
    //collision
    private static Pair<Float, Float> storedCollision = Pair.of(0.6F, 1.8F);
    //spawn egg
    private static Pair<Integer, Integer> storedSpawnEgg = null;
    @Deprecated // will be cleared after register.
    public static Map<RegistryObject, Pair<Integer, Integer>> spawnEggMap = new HashMap<>();
    //spawn placements
    private static List</*0:SpawnPlacements.type, 1:HeightMap.types, 2:checkSpawnRulesMethod*/?> storedSpawnPlacement = null;
    @Deprecated // will be cleared after register.
    public static Map<RegistryObject, List</*0:SpawnPlacements.type, 1:HeightMap.types, 2:checkSpawnRulesMethod*/?>> spawnPlacementMap = new HashMap<>();
    //attributes
    private static Supplier<AttributeSupplier.Builder> storedAttribute = null;
    public static Map<RegistryObject, Supplier<AttributeSupplier.Builder>> attributesMap = new HashMap<>();
    //loot
    private static boolean storedNoLoot = false;
    @Deprecated // will be cleared after register.
    public static List<RegistryObject> noLootList = new ArrayList<>();
    //tag
    private static List<TagKey<EntityType<?>>> storedTags = null;
    @Deprecated // will be cleared after register.
    public static Map<RegistryObject, List<TagKey<EntityType<?>>>> tagMap = new HashMap<>();
    //no summon
    public static boolean storedCanSummon = true;



    //registry
    /**
     * silly carp do not forget to add attributes. use {@link PVZEntities#attribute(Supplier)}.
     */
    public static final RegistryObject<EntityType<PVZBoat>> BOAT = collision(1.375F, 0.5625F).entity("pvz_boat", PVZBoat::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<PVZChestBoat>> CHEST_BOAT = collision(1.375F, 0.5625F).entity("pvz_chest_boat", PVZChestBoat::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<Sun>> SUN = collision(0.2F, 0.2F).entity("sun", Sun::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<MooBloom>> MOOBLOOM = summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, MooBloom::checkMooBloomSpawnRules)
            .spawnEgg(0xffc100, 0x88b830).attribute(MooBloom::createAttributes)
            .collision(0.9F, 1.4F).entity("moo_bloom", MooBloom::new, MobCategory.CREATURE);
    public static final RegistryObject<EntityType<GrassCarp>> GRASSCARP = summonRule(Type.IN_WATER, Types.MOTION_BLOCKING_NO_LEAVES, GrassCarp::checkGrassCarpSpawnRules)
            .spawnEgg(0x708849, 0xd4d78a).attribute(GrassCarp::createAttributes)
            .collision(0.4F, 0.4F).entity("grass_carp", GrassCarp::new, MobCategory.WATER_AMBIENT);
    public static final RegistryObject<EntityType<Anger>> ANGER = spawnEgg(0xff2f3b, 0xfff45b).attribute(Anger::createAttributes)
            .collision(0.4F, 0.4F).noLoot().entity("anger", Anger::new, MobCategory.CREATURE);
    public static final RegistryObject<EntityType<Sprout>> SPROUT = attribute(Sprout::createAttributes).collision(0.4F, 0.4F).noLoot()
            .entity("sprout", Sprout::new, MobCategory.CREATURE);

    //client
    public static final RegistryObject<EntityType<ModelPartEntity>> MODEL_PART = collision(0.2F, 0.2F).noSummon()
            .entity("model_part", ModelPartEntity::new, MobCategory.MISC);

    //plants
    public static final RegistryObject<EntityType<WallNut>> WALL_NUT = attribute(WallNut::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, WallNut::checkSpawnRules)
            .collision(0.8F, 1F).entity("wall_nut", WallNut::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<SunFlower>> SUN_FLOWER = attribute(SunFlower::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, SunFlower::checkSpawnRules)
            .collision(0.75F, 1.1F).entity("sun_flower", SunFlower::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<MariGold>> MARIGOLD = attribute(MariGold::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .collision(0.75F, 1.0F).entity("marigold", MariGold::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<TallNut>> TALL_NUT = attribute(TallNut::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, TallNut::checkSpawnRules)
            .collision(0.9F, 1.9F).entity("tall_nut", TallNut::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<PeaShooter>> PEA_SHOOTER = attribute(PeaShooter::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, PeaShooter::checkSpawnRules)
            .collision(0.7F, 1.3F).entity("pea_shooter", PeaShooter::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<SplitPea>> SPLIT_PEA = attribute(SplitPea::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .collision(0.7F, 1.3F).entity("split_pea", SplitPea::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<Repeater>> REPEATER = attribute(Repeater::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .collision(0.7F, 1.3F).entity("repeater", Repeater::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<GatlingPea>> GATLING_PEA = attribute(GatlingPea::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .collision(0.7F, 1.3F).entity("gatling_pea", GatlingPea::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<SnowPea>> SNOW_PEA = attribute(SnowPea::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .collision(0.7F, 1.3F).entity("snow_pea", SnowPea::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<TangleKelp>> TANGLE_KELP = attribute(TangleKelp::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .collision(0.6F, 0.5F).entity("tangle_kelp", TangleKelp::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<Pumpkin>> PUMPKIN = attribute(Pumpkin::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .collision(1F, 0.5F).entity("pumpkin", Pumpkin::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<FlowerPot>> FLOWER_POT = attribute(FlowerPot::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .collision(0.75F, 0.475F).entity("flower_pot", FlowerPot::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<LilyPad>> LILY_PAD = attribute(LilyPad::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .collision(0.875F, 0.2F).entity("lily_pad", LilyPad::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<Plantern>> PLANTERN = attribute(Plantern::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .collision(0.8F, 2F).entity("plantern", Plantern::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<CabbagePult>> CABBAGE_PULT = attribute(CabbagePult::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .collision(0.7F, 1F).entity("cabbage_pult", CabbagePult::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<MelonPult>> MELON_PULT = attribute(MelonPult::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .collision(0.9F, 1F).entity("melon_pult", MelonPult::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<UmbrellaLeaf>> UMBRELLA_LEAF = attribute(UmbrellaLeaf::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .collision(0.8F, 1F).entity("umbrella_leaf", UmbrellaLeaf::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<PotatoMine>> POTATO_MINE = attribute(PotatoMine::createAttributes).noLoot().tag(PVZEntityTags.PLANT, PVZEntityTags.MUST_PLANT_IN_DIRT)
            .collision(0.4F, 0.4F).entity("potato_mine", PotatoMine::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<IcebergLettuce>> ICEBERG_LETTUCE = attribute(IcebergLettuce::createAttributes).noLoot().tag(PVZEntityTags.PLANT, EntityTypeTags.POWDER_SNOW_WALKABLE_MOBS)
            .collision(0.4F, 0.4F).entity("iceberg_lettuce", IcebergLettuce::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<Jalapeno>> JALAPENO = attribute(Jalapeno::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .collision(0.6F, 1.6F).entity("jalapeno", Jalapeno::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<GoldBloom>> GOLD_BLOOM = attribute(GoldBloom::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .collision(0.6F, 0.6F).entity("gold_bloom", GoldBloom::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<SpikeWeed>> SPIKE_WEED = attribute(SpikeWeed::createAttributes).noLoot().tag(PVZEntityTags.PLANT, PVZEntityTags.MUST_PLANT_IN_DIRT)
            .collision(0.95F, 0.125F).entity("spike_weed", SpikeWeed::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<TorchWood>> TORCH_WOOD = attribute(TorchWood::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .collision(0.95F, 0.65F).entity("torch_wood", TorchWood::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<VelociRadish>> VELOCI_RADISH = attribute(VelociRadish::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .summonRule(Type.ON_GROUND, Types.MOTION_BLOCKING_NO_LEAVES, VelociRadish::checkSpawnRules)
            .collision(0.5F, 0.5825F).entity("veloci_radish", VelociRadish::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<KernelPult>> KERNEL_PULT = attribute(KernelPult::createAttributes).noLoot().tag(PVZEntityTags.PLANT, PVZEntityTags.BUTTER_INVULNERABLE)
            .collision(0.6F, 1F).entity("kernel_pult", KernelPult::new, OtherRegisters.PVZPlantMobCategory);
    public static final RegistryObject<EntityType<Chomper>> CHOMPER = attribute(Chomper::createAttributes).noLoot().tag(PVZEntityTags.PLANT)
            .collision(1.2F, 1.8F).entity("chomper", Chomper::new, OtherRegisters.PVZPlantMobCategory);

    //zombies
    public static final RegistryObject<EntityType<PVZZombie>> ZOMBIE = attribute(Zombie::createAttributes).tag(PVZEntityTags.ZOMBIE)
            .spawnEgg(0xb97141, 0x799587)
            .entity("zombie", PVZZombie::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<PoleVaultingZombie>> POLE_VAULTING_ZOMBIE = attribute(PoleVaultingZombie::createAttributes).tag(PVZEntityTags.ZOMBIE)
            .spawnEgg(0xd1575b, 0x3d97dc)
            .entity("pole_vaulting_zombie", PoleVaultingZombie::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<JackInABoxZombie>> JACK_IN_A_BOX_ZOMBIE = attribute(Zombie::createAttributes).tag(PVZEntityTags.ZOMBIE)
            .spawnEgg(0xddd4d4, 0xcc4646)
            .entity("jack_in_a_box_zombie", JackInABoxZombie::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<Imp>> IMP = attribute(Imp::createAttributes).tag(PVZEntityTags.ZOMBIE)
            .spawnEgg(0xaa3a39, 0x454ab0)
            .entity("imp", Imp::new, MobCategory.MONSTER);
    public static final RegistryObject<EntityType<Gargantuar>> GARGANTUAR = attribute(Gargantuar::createAttributes).tag(PVZEntityTags.ZOMBIE)
            .spawnEgg(0xaa3a39, 0x454ab0)
            .collision(1.5F, 2.7F).entity("gargantuar", Gargantuar::new, MobCategory.MONSTER);
    //bullets
    public static final RegistryObject<EntityType<PeaBullet>> PEA = collision(0.4F, 0.4F).entity("pea", PeaBullet::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<CabbageBullet>> CABBAGE = collision(0.4F, 0.4F).entity("cabbage", CabbageBullet::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<CornBullet>> CORN = collision(0.25F, 0.25F).entity("corn", CornBullet::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<ButterBullet>> BUTTER = collision(0.5F, 0.5F).entity("butter", ButterBullet::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<MelonBullet>> MELON = collision(0.8F, 0.8F).entity("melon", MelonBullet::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<SeedArrow>> SEED_ARROW = collision(0.2F, 0.2F).entity("seed_arrow", SeedArrow::new, MobCategory.MISC);
    public static final RegistryObject<EntityType<ArrowWithATarget>> ARROW_WITH_A_TARGET = collision(0.2F, 0.2F).entity("arrow_with_a_target", ArrowWithATarget::new, MobCategory.MISC);


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
        rS(SPLIT_PEA, SplitPeaModel::new, SplitPeaModel::createBodyLayer, 0.5F, "textures/entity/plants/split_pea/split_pea.png");
        rS(REPEATER, RepeaterModel::new, RepeaterModel::createBodyLayer, 0.5F, "textures/entity/plants/repeater/repeater.png");
        rS(GATLING_PEA, GatlingPeaModel::new, GatlingPeaModel::createBodyLayer, 0.5F, "textures/entity/plants/gatling_pea/gatling_pea.png");
        rS(CABBAGE_PULT, CabbagePultModel::new, CabbagePultModel::createBodyLayer, 0.5F, "textures/entity/plants/cabbage_pult/cabbage_pult.png");
        rS(VELOCI_RADISH, VelociTurnipModel::new, VelociTurnipModel::createBodyLayer, 0.5F, "textures/entity/plants/veloci_radish/veloci_radish.png");
        rS(LILY_PAD, LilyPadModel::new, LilyPadModel::createBodyLayer, 0.5F, "textures/entity/plants/lily_pad/lily_pad.png");
        rS(KERNEL_PULT, KernelPultModel::new, KernelPultModel::createBodyLayer, 0.5F, "textures/entity/plants/kernel_pult/kernel_pult.png");
        rS(UMBRELLA_LEAF, UmbrellaLeafModel::new, UmbrellaLeafModel::createBodyLayer, 0.5F, "textures/entity/plants/umbrella_leaf/umbrella_leaf.png");
        rS(MELON_PULT, MelonPultModel::new, MelonPultModel::createBodyLayer, 0.5F, "textures/entity/plants/melon_pult/melon_pult.png");
        rS(ICEBERG_LETTUCE, IcebergLettuceModel::new, IcebergLettuceModel::createBodyLayer, 0F, "textures/entity/plants/iceberg_lettuce/iceberg_lettuce.png");
    }

    @OnlyIn(Dist.CLIENT)
    @SubscribeEvent
    public static void registerRenderer(EntityRenderersEvent.RegisterRenderers e) {
        r(e, BOAT, (c) -> new PVZBoatRenderer(c, false));
        r(e, CHEST_BOAT, (c) -> new PVZBoatRenderer(c, true));
        r(e, GRASSCARP, GrassCarpRenderer::new);
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
        r(e, PEA, PeaBulletRenderer::new);
        r(e, CABBAGE, CabbageBulletRenderer::new);
        r(e, CORN, CornBulletRenderer::new);
        r(e, BUTTER, ButterBulletRenderer::new);
        r(e, MELON, MelonBulletRenderer::new);
        r(e, POTATO_MINE, PotatoMineRenderer::new);
        r(e, CHOMPER, ChomperRenderer::new);
        r(e, GOLD_BLOOM, GoldBloomRenderer::new);
        r(e, ZOMBIE, PVZZombieRenderer::new);
        r(e, POLE_VAULTING_ZOMBIE, PoleVaultingZombieRenderer::new);
        r(e, JACK_IN_A_BOX_ZOMBIE, JackInABoxZombieRenderer::new);
        r(e, IMP, ImpRenderer::new);
        r(e, GARGANTUAR, GargantuarRenderer::new);
        r(e, SPROUT, SproutRenderer::new);
        r(e, SEED_ARROW, SeedArrowRenderer::new);
        r(e, ARROW_WITH_A_TARGET, ArrowWithATargetRenderer::new);
        r(e, MODEL_PART, ModelPartRenderer::new);

        //enter here

        //auto works
        rendererSimple(e);
    }


    //definitions
    private static <T extends Entity> RegistryObject<EntityType<T>> entity(String name, EntityType.EntityFactory<T> factory, MobCategory classification) {
        float coh = storedCollision.getFirst();
        float cov = storedCollision.getSecond();
        Supplier<EntityType<T>> supplier = storedCanSummon ? () -> EntityType.Builder.of(factory, classification).sized(coh, cov).build(Util.prefix(name).toString()) :
                () -> EntityType.Builder.of(factory, classification).sized(coh, cov).noSummon().build(Util.prefix(name).toString());
        storedCanSummon = true;
        RegistryObject<EntityType<T>> entity = ENTITIES.register(name, supplier);
        storedCollision = Pair.of(0.6F, 1.8F);
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
        //noloot
        if (storedNoLoot) {
            noLootList.add(entity);
            storedNoLoot = false;
        }
        //tag
        if (storedTags != null) {
            tagMap.put(entity, storedTags);
            storedTags = null;
        }

        return entity;
    }

    private static PVZEntities collision(Float width, Float height) {
        storedCollision = Pair.of(width, height);
        return reflector;
    }
    private static PVZEntities noLoot() {
        storedNoLoot = true;
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
        storedSpawnPlacement = List.of(type, types, predicate);
        return reflector;
    }

    private static PVZEntities noSummon() {
        storedCanSummon = false;
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
        simpleRenderedMap.put(entity.get(), List.of(model, layer, shadowSize));
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
        spawnPlacementMap.forEach((obj, list) ->
                e.register((EntityType<? extends Entity>) obj.get(), (Type) list.get(0),
                        (Types) list.get(1), (SpawnPredicate) list.get(2),
                        SpawnPlacementRegisterEvent.Operation.OR
                )
        );
    }

    @SubscribeEvent
    public static void addEntityAttributes(EntityAttributeCreationEvent e) {
        attributesMap.forEach((obj, sup) -> e.put((EntityType<? extends LivingEntity>) obj.get(), sup.get().build()));
    }

    public static void release() {
        noLootList.clear();
        List.of(spawnEggMap, spawnPlacementMap, tagMap).forEach(Map::clear);
    }
}
