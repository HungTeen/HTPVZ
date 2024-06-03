package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.events.RegisterSeedPacketsEvent;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.common.item.*;
import com.hungteen.pvz.common.tags.PVZItemTags;
import com.hungteen.pvz.util.Util;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.function.Supplier;

import static com.hungteen.pvz.util.Util.name;

@SuppressWarnings("all")
public class PVZItems {

    //init
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PVZMod.MODID);
    public static Supplier<Item> storedSup = () -> new Item(new Item.Properties().tab(PVZItemTabs.PVZ_MISC));

    private static List<TagKey<Item>> storedTag = new ArrayList<>();
    @Deprecated // will be cleared after register.
    public static Map<RegistryObject<Item>, List<TagKey<Item>>> tagMap = new HashMap<>();

    private static Pair<Model, List<ResourceLocation>> storedModel = Pair.of(Model.Simple, new ArrayList<>());
    @Deprecated // will be cleared after register.
    public static List<Pair<RegistryObject<Item>, Pair<Model, List<ResourceLocation>>>> modelList = new ArrayList<>();

    public static Map<Pair<WoodType, /*hasChest*/Boolean>, RegistryObject<Item>> boatItemMap = new HashMap<>();
    private static final PVZItems reflector = new PVZItems();

    @Deprecated // will be cleared after register.
    public static Map<RegisterSeedPacketsEvent.SeedPacketData<?>, RegistryObject<Item>> seedPacketMap = new HashMap<>();
    @Deprecated // will be cleared after register.
    public static Map<RegisterSeedPacketsEvent.SeedPacketData<?>, RegistryObject<Item>> seedMap = new HashMap<>();

    @Deprecated
    public static Map<RegistryObject<?>, Float> composterMap = new HashMap<>();
    private static float storedComposterChance = 0;


    //registry
    public static final RegistryObject<Item> PEA = composter(0.3F).item("pea", () -> new BlockItem(PVZBlocks.PEA.get(), new Item.Properties().tab(PVZItemTabs.PVZ_MISC)));
    public static final RegistryObject<Item> SNOW_PEA = composter(0.3F).item("snow_pea");
    public static final RegistryObject<Item> FLAME_PEA = composter(0.3F).item("flame_pea");
    public static final RegistryObject<Item> NUT = composter(0.3F).item("nut");
    public static final RegistryObject<Item> PEPPER = composter(0.3F).item("pepper");
    public static final RegistryObject<Item> CABBAGE_SEED = composter(0.3F).item("cabbage_seeds", () -> new BlockItem(PVZBlocks.CABBAGE_SEEDS.get(), new Item.Properties().tab(PVZItemTabs.PVZ_MISC)));
    public static final RegistryObject<Item> CORN_KERNELS = composter(0.3F).item("corn_kernels", () -> new BlockItem(PVZBlocks.CORN_KERNELS.get(), new Item.Properties().tab(PVZItemTabs.PVZ_MISC)));
    public static final RegistryObject<Item> JEWEL = item("jewel");
    public static final RegistryObject<Item> ALAYA_RESIN = item("alaya_resin");
    public static final RegistryObject<Item> SPATIOTEMPORAL_UNIT = item("spatiotemporal_unit");
    public static final RegistryObject<Item> ORIGIN_ESSENCE = tag(PVZItemTags.ESSENCE).item("origin_essence");
    public static final RegistryObject<Item> TERRA_ESSENCE = tag(PVZItemTags.ESSENCE).item("terra_essence");
    public static final RegistryObject<Item> AQUA_ESSENCE = tag(PVZItemTags.ESSENCE).item("aqua_essence");
    public static final RegistryObject<Item> IGNIS_ESSENCE = tag(PVZItemTags.ESSENCE).item("ignis_essence");
    public static final RegistryObject<Item> VENTUS_ESSENCE = tag(PVZItemTags.ESSENCE).item("ventus_essence");
    public static final RegistryObject<Item> GELUM_ESSENCE = tag(PVZItemTags.ESSENCE).item("gelum_essence");
    public static final RegistryObject<Item> LUX_ESSENCE = tag(PVZItemTags.ESSENCE).item("lux_essence");

    public static final RegistryObject<Item> FERTILIZER = composter(1F).item("fertilizer", () -> new FertilizerItem(new Item.Properties().tab(PVZItemTabs.PVZ_MISC)));
    public static final RegistryObject<Item> TREE_FERTILIZER = composter(1F).item("tree_fertilizer", () -> new Item(new Item.Properties().tab(PVZItemTabs.PVZ_MISC)));
    public static final RegistryObject<Item> FLOWER_SEED_PACKET = item("flower_seed_packet", () -> new Item(new Item.Properties().tab(PVZItemTabs.PVZ_PLANT_CARDS)));
    public static final RegistryObject<Item> NETHER_WART_SEED_PACKET = item("nether_wart_seed_packet", () -> new Item(new Item.Properties().tab(PVZItemTabs.PVZ_PLANT_CARDS)));
    public static final RegistryObject<Item> CHORUS_FRUIT_SEED_PACKET = item("chorus_fruit_seed_packet", ()-> new Item(new Item.Properties().tab(PVZItemTabs.PVZ_PLANT_CARDS)));
    public static final RegistryObject<Item> FLUORESCENT_DAISY_SEED_PACKET = item("fluorescent_daisy_seed_packet", ()-> new Item(new Item.Properties().tab(PVZItemTabs.PVZ_PLANT_CARDS)));

    //food
    public static final RegistryObject<Item> POP_SMARTS = item("pop_smarts", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_FOOD).food((new FoodProperties.Builder()).nutrition(4).saturationMod(0.5F).build())));
    public static final RegistryObject<Item> CABBAGE = tag(PVZItemTags.CABBAGE).composter(0.85F).item("cabbage", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_FOOD).food((new FoodProperties.Builder()).nutrition(3).saturationMod(1F).build())));
    public static final RegistryObject<Item> CORN = tag(PVZItemTags.CORN).composter(0.75F).item("corn", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_FOOD).food((new FoodProperties.Builder()).nutrition(4).saturationMod(0.2F).build())));
    public static final RegistryObject<Item> POPCORN = item("popcorn", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_FOOD).food((new FoodProperties.Builder()).nutrition(1).saturationMod(0.5F).build())));
    public static final RegistryObject<Item> CHOCOLATE = item("chocolate", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_FOOD).food((new FoodProperties.Builder()).nutrition(4).saturationMod(1F).effect(() -> new MobEffectInstance(PVZMobEffects.EXCITEMENT.get(), 30), 1).build())));
    public static final RegistryObject<Item> TACO = item("taco", () -> new Item((new Item.Properties()).tab(CreativeModeTab.TAB_FOOD).rarity(Rarity.RARE).food((new FoodProperties.Builder()).nutrition(1).saturationMod(0.5F).effect(() -> new MobEffectInstance(PVZMobEffects.EXCITEMENT.get(), 200), 1).effect(() -> new MobEffectInstance(MobEffects.REGENERATION, 100), 1).effect(() -> new MobEffectInstance(MobEffects.SATURATION, 30), 1).alwaysEat().build())));


    //spawners
    public static final RegistryObject<Item> SPROUT = item("sprout", () -> new SproutItem(new Item.Properties().stacksTo(1).tab(PVZItemTabs.PVZ_FUNCTIONAL), false));
    public static final RegistryObject<Item> MARIGOLD_SPROUT = item("marigold_sprout", () -> new SproutItem(new Item.Properties().stacksTo(1).tab(PVZItemTabs.PVZ_FUNCTIONAL), true));
    public static final RegistryObject<Item> GRASSCARP_BUCKET = item("grass_carp_bucket", () -> new MobBucketItem(PVZEntities.GRASSCARP, () -> Fluids.WATER, () -> SoundEvents.BUCKET_EMPTY_AXOLOTL, new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> TANGLE_KELP_BUCKET = item("tangle_kelp_bucket", () -> new MobBucketItem(PVZEntities.TANGLE_KELP, () -> Fluids.WATER, () -> SoundEvents.BUCKET_EMPTY_AXOLOTL, new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> OVERWORLD_FLAG_ZOMBIE_SPAWN_EGG = model(Model.SpawnEgg).item("overworld_flag_zombie_spawn_egg", () -> new ModifiedSpawnEggItem(PVZEntities.ZOMBIE, PVZZombie.OVERWORLD_FLAG_ZOMBIE_CONSUMER,0x4657e1, 0x799587, (new Item.Properties()).tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> NETHER_FLAG_ZOMBIE_SPAWN_EGG = model(Model.SpawnEgg).item("nether_flag_zombie_spawn_egg", () -> new ModifiedSpawnEggItem(PVZEntities.ZOMBIE, PVZZombie.NETHER_FLAG_ZOMBIE_CONSUMER,0xed5b34, 0x799587, (new Item.Properties()).tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> END_FLAG_ZOMBIE_SPAWN_EGG = model(Model.SpawnEgg).item("end_flag_zombie_spawn_egg", () -> new ModifiedSpawnEggItem(PVZEntities.ZOMBIE, PVZZombie.END_FLAG_ZOMBIE_CONSUMER,0xc756dc, 0x799587, (new Item.Properties()).tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> CONEHEAD_ZOMBIE_SPAWN_EGG = model(Model.SpawnEgg).item("conehead_zombie_spawn_egg", () -> new ModifiedSpawnEggItem(PVZEntities.ZOMBIE, PVZZombie.CONEHEAD_ZOMBIE_CONSUMER,0xff9c03, 0x799587, (new Item.Properties()).tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> BUCKETHEAD_ZOMBIE_SPAWN_EGG = model(Model.SpawnEgg).item("buckethead_zombie_spawn_egg", () -> new ModifiedSpawnEggItem(PVZEntities.ZOMBIE, PVZZombie.BUCKET_ZOMBIE_CONSUMER,0xe1d6d6, 0x799587, (new Item.Properties()).tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> DUCK_LIFEBUOY_ZOMBIE_SPAWN_EGG = model(Model.SpawnEgg).item("duck_lifebuoy_zombie_spawn_egg", () -> new ModifiedSpawnEggItem(PVZEntities.ZOMBIE, PVZZombie.DUCK_LIFEBUOY_ZOMBIE_CONSUMER,0xffe000, 0x799587, (new Item.Properties()).tab(CreativeModeTab.TAB_MISC)));
    public static final RegistryObject<Item> SCREEN_DOOR_ZOMBIE_SPAWN_EGG = model(Model.SpawnEgg).item("screen_door_zombie_spawn_egg", () -> new ModifiedSpawnEggItem(PVZEntities.ZOMBIE, PVZZombie.SCREEN_DOOR_CONSUMER,0xc8bbbd, 0x799587, (new Item.Properties()).tab(CreativeModeTab.TAB_MISC)));


    //equipments
    public static final RegistryObject<Item> CONE_HELMET = item("cone_helmet", () -> new ExtraHealthArmorItem(PVZArmorMaterials.CONE, new Item.Properties().tab(PVZItemTabs.PVZ_FUNCTIONAL).durability(30), EquipmentSlot.HEAD));
    public static final RegistryObject<Item> BUCKET_HELMET = tag(PVZItemTags.IRON).item("bucket_helmet", () -> new ExtraHealthArmorItem(PVZArmorMaterials.BUCKET, new Item.Properties().tab(PVZItemTabs.PVZ_FUNCTIONAL).durability(100), EquipmentSlot.HEAD));
    public static final RegistryObject<Item> DUCK_LIFEBUOY = item("duck_lifebuoy", () -> new DuckLifebuoyItem(new Item.Properties().stacksTo(1).tab(PVZItemTabs.PVZ_FUNCTIONAL)));
    public static final RegistryObject<Item> SCREEN_DOOR_SHIELD = tag(PVZItemTags.IRON, PVZItemTags.ENTITY_DAMAGEABLE_SHIELDS).model(Model.Modeled).item("screen_door_shield", () -> new PVZShieldItem((new Item.Properties()).durability(150).tab(PVZItemTabs.PVZ_FUNCTIONAL)));
    public static final RegistryObject<Item> JACK_IN_THE_BOX = tag(PVZItemTags.IRON).model(Model.Modeled).item("jack_in_the_box", () -> new JackInTheBoxItem((new Item.Properties()).tab(PVZItemTabs.PVZ_FUNCTIONAL).stacksTo(1)));

    //tools
    public static final RegistryObject<Item> SEED_CROSSBOW = model(Model.Modeled).item("seed_crossbow", () -> new SeedCrossbowItem( new Item.Properties().stacksTo(1).durability(465).tab(PVZItemTabs.PVZ_FUNCTIONAL)));
    public static final RegistryObject<Item> PEA_GUN = model(Model.Modeled).item("pea_gun", () -> new PeaGunItem( new Item.Properties().stacksTo(1).durability(400).tab(PVZItemTabs.PVZ_FUNCTIONAL)));
    public static final RegistryObject<Item> CHILI_CHAN = model(Model.Handheld).item("chili_chan", () -> new ChiliChanItem( new Item.Properties().stacksTo(1).durability(233).tab(PVZItemTabs.PVZ_FUNCTIONAL)));
    public static final RegistryObject<Item> SEED_DISPENSARY = item("seed_dispensary", () -> new SeedDispensaryItem(new Item.Properties().stacksTo(16).tab(PVZItemTabs.PVZ_FUNCTIONAL)));
    public static final RegistryObject<Item> ARROW_WITH_A_TARGET = tag(ItemTags.ARROWS).item("arrow_with_a_target", () -> new ArrowWithATargetItem(new Item.Properties().tab(PVZItemTabs.PVZ_FUNCTIONAL)));
    public static final RegistryObject<Item> WATERING_POT = model(Model.Modeled).item("watering_pot", () -> new WateringPotItem(new Item.Properties().stacksTo(1).durability(5).tab(PVZItemTabs.PVZ_BLOCKS)));
    public static final RegistryObject<Item> ZEN_GARDEN_PORTAL = model(Model.Modeled).item("zen_garden_portal", () -> new BlockItem(PVZBlocks.ZEN_GARDEN_PORTAL.get(), new Item.Properties().stacksTo(1).tab(PVZItemTabs.PVZ_BLOCKS)));

    static {
        createBannerPatterns();
        createSpawnEggs();
        createSeedPackets();
    }


    public static void registerProperties() {
        SeedCrossbowItem.registerProperties();
        WateringPotItem.registerProperties();
        PVZShieldItem.registerProperties();
        JackInTheBoxItem.registerProperties();
    }

    //definitions
    private static PVZItems model(Model model, ResourceLocation... res){
        storedModel = Pair.of(model, List.of(res));
        return reflector;
    }

    private static PVZItems model(Model model, List<ResourceLocation> res){
        storedModel = Pair.of(model, res);
        return reflector;
    }

    private static ResourceLocation res(String path){
        return new ResourceLocation(PVZMod.MODID, ModelProvider.ITEM_FOLDER + "/" + path);
    }

    private static PVZItems sup(Supplier<Item> sup){
        storedSup = sup;
        return reflector;
    }
    private static PVZItems tag(TagKey<Item>... tags){
        return tag(Arrays.asList(tags));
    }
    private static PVZItems tag(List<TagKey<Item>> list){
        storedTag.addAll(list);
        return reflector;
    }
    private static PVZItems composter(float chance){
        storedComposterChance = chance;
        return reflector;
    }

    public static RegistryObject<Item> item(String name){
        return item(name, storedSup);
    }
    public static RegistryObject<Item> item(String name, Supplier<Item> sup){
        RegistryObject<Item> itemObj = ITEMS.register(name, sup);
        if (storedModel.getFirst() != Model.Modeled){
            modelList.add(Pair.of(itemObj, storedModel));
        }
        if (! storedTag.isEmpty()) {
            tagMap.put(itemObj, List.copyOf(storedTag));
            storedTag.clear();
        }
        storedModel = Pair.of(Model.Simple, new ArrayList<>());
        if (storedComposterChance > 0) {
            composterMap.put(itemObj, storedComposterChance);
        }
        return itemObj;
    }

    public static RegistryObject<Item> boat(boolean hasChest, WoodType woodType){
        RegistryObject<Item> itemObj = tag(ItemTags.BOATS).item(woodType.name() + (hasChest ? "_chest_boat" : "_boat"),
                () -> new PVZBoatItem(hasChest, woodType, (new Item.Properties()).stacksTo(1).tab(CreativeModeTab.TAB_TRANSPORTATION)));
        boatItemMap.put(Pair.of(woodType, hasChest), itemObj);
        return itemObj;
    }
    public static RegistryObject<Item> spawnEgg(RegistryObject<EntityType<? extends Mob>> entity, Integer bgColor, Integer hlColor){
        RegistryObject<Item> itemObj = model(Model.SpawnEgg).item(name(entity) + "_spawn_egg",
                () -> new ForgeSpawnEggItem(entity, bgColor, hlColor, (new Item.Properties()).tab(CreativeModeTab.TAB_MISC)));
        return itemObj;
    }

    public static void createSpawnEggs(){
        PVZEntities.spawnEggMap.forEach((entity, pair) -> spawnEgg((RegistryObject<EntityType<? extends Mob>>) entity, pair.getFirst(), pair.getSecond()));
    }

    public static void createBannerPatterns() {
        PVZBannerPatterns.bannerMap.forEach((obj, tag) -> item(Util.name(obj) + "_banner_pattern", () -> new BannerPatternItem(tag, (new Item.Properties()).stacksTo(1).tab(CreativeModeTab.TAB_MISC))));
    }

    public static void createSeedPackets() {
        PVZSeedPackets.seedPackets.forEach((data) -> {
            String name = data.entitySupplier instanceof RegistryObject<?> ? name((RegistryObject<?>) data.entitySupplier) : name((EntityType<?>) data.entitySupplier.get());
            if (data instanceof PVZSeedPackets.RecipeSeedPacketData<?> && ((PVZSeedPackets.RecipeSeedPacketData<?>)data).recipe != null) {
                model(Model.SeedPacket, res("seed_packets/" + name(((PVZSeedPackets.RecipeSeedPacketData<?>) data).getBackCard())), res("plants/" + name));
            }
            seedPacketMap.put(data,
                    item(name + "_seed_packet", () -> new SeedPacketItem(
                            new Item.Properties().stacksTo(1).defaultDurability(150).tab(PVZItemTabs.PVZ_PLANT_CARDS), data.entitySupplier, data.skillList, data.resource, data.cost, data.coolDown, data.creativeOnly
                    )));
        });

        PVZSeedPackets.seedPackets.forEach((data) -> {
            String name = data.entitySupplier instanceof RegistryObject<?> ? name((RegistryObject<?>) data.entitySupplier) : name((EntityType<?>) data.entitySupplier.get());
            if (data instanceof PVZSeedPackets.RecipeSeedPacketData<?> && ((PVZSeedPackets.RecipeSeedPacketData<?>)data).recipe != null) {
                model(Model.SeedPacket, res("seed_packets/seed"), res("plants/" + name));
            }
            seedMap.put(data,
                    item(name + "_seed", () -> new SeedItem(
                            new Item.Properties().stacksTo(16).tab(PVZItemTabs.PVZ_PLANT_CARDS), data.entitySupplier, data.resource, data.cost, data.coolDown, data.creativeOnly
                    )));
        });
    }

    public static void release(){
        List.of(tagMap, seedMap, seedPacketMap, composterMap).forEach(Map::clear);
        modelList.clear();
    }

    public enum Model {
        Simple, Handheld, Block, SpawnEgg, SeedPacket, Modeled
    }
}
