package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.events.RegisterSeedPacketsEvent;
import com.hungteen.pvz.common.item.PVZBoatItem;
import com.hungteen.pvz.common.item.PVZSeedPackets;
import com.hungteen.pvz.common.item.SeedItem;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.tags.PVZItemTags;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.MobBucketItem;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.common.ForgeSpawnEggItem;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.function.Supplier;

import static com.hungteen.pvz.Util.name;

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


    //registry
    public static final RegistryObject<Item> PEA = item("pea");
    public static final RegistryObject<Item> NUT = item("nut");
    public static final RegistryObject<Item> ORIGIN_ESSENCE = tag(PVZItemTags.ESSENCE).item("origin_essence");
    public static final RegistryObject<Item> TERRA_ESSENCE = tag(PVZItemTags.ESSENCE).item("terra_essence");
    public static final RegistryObject<Item> AQUA_ESSENCE = tag(PVZItemTags.ESSENCE).item("aqua_essence");
    public static final RegistryObject<Item> IGNIS_ESSENCE = tag(PVZItemTags.ESSENCE).item("ignis_essence");
    public static final RegistryObject<Item> VENTUS_ESSENCE = tag(PVZItemTags.ESSENCE).item("ventus_essence");
    public static final RegistryObject<Item> GELUM_ESSENCE = tag(PVZItemTags.ESSENCE).item("gelum_essence");
    public static final RegistryObject<Item> LUX_ESSENCE = tag(PVZItemTags.ESSENCE).item("lux_essence");
    public static final RegistryObject<Item> FLOWER_SEED_PACKET = item("flower_seed_packet", () -> new Item(new Item.Properties().tab(PVZItemTabs.PVZ_PLANT_CARDS)));
    public static final RegistryObject<Item> NETHER_WART_SEED_PACKET = item("nether_wart_seed_packet", () -> new Item(new Item.Properties().tab(PVZItemTabs.PVZ_PLANT_CARDS)));
    public static final RegistryObject<Item> CHORUS_FRUIT_SEED_PACKET = item("chorus_fruit_seed_packet", () -> new Item(new Item.Properties().tab(PVZItemTabs.PVZ_PLANT_CARDS)));


    //spawners
    public static final RegistryObject<Item> GRASSCARP_BUCKET = item("grass_carp_bucket", () -> new MobBucketItem(PVZEntities.GRASSCARP, () -> Fluids.WATER, () -> SoundEvents.BUCKET_EMPTY_AXOLOTL, new Item.Properties().stacksTo(1).tab(CreativeModeTab.TAB_MISC)));

    static {
        createSpawnEggs();
        createSeedPackets();
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

    public static RegistryObject<Item> item(String name){
        return item(name, storedSup);
    }
    public static RegistryObject<Item> item(String name, Supplier<Item> sup){
        RegistryObject<Item> itemObj = ITEMS.register(name, sup);
        if (storedModel.getFirst() != Model.Modeled){
            modelList.add(Pair.of(itemObj, storedModel));
        }
        storedModel = Pair.of(Model.Simple, new ArrayList<>());
        return itemObj;
    }

    public static RegistryObject<Item> boat(boolean hasChest, WoodType woodType){
        RegistryObject<Item> itemObj = tag(ItemTags.BOATS).item(woodType.name() + (hasChest ? "_chest_boat" : "_boat"),
                () -> new PVZBoatItem(hasChest, woodType, (new Item.Properties()).stacksTo(1).tab(PVZItemTabs.PVZ_FUNCTIONAL)));
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

    public static void createSeedPackets(){
        PVZSeedPackets.seedPackets.forEach((data) -> {
            String name = data.entitySupplier instanceof RegistryObject<?> ? name((RegistryObject<?>) data.entitySupplier) : name((EntityType<?>) data.entitySupplier.get());
            if (data instanceof PVZSeedPackets.RecipeSeedPacketData<?> && ((PVZSeedPackets.RecipeSeedPacketData<?>)data).recipe != null) {
                model(Model.SeedPacket, res("seed_packets/" + name(((PVZSeedPackets.RecipeSeedPacketData<?>) data).getBackCard())), res("plants/" + name));
            }
            seedPacketMap.put(data,
                    item(name + "_seed_packet", () -> new SeedPacketItem(
                            new Item.Properties().stacksTo(1).defaultDurability(300).tab(PVZItemTabs.PVZ_PLANT_CARDS), data.entitySupplier, data.resource, data.cost, data.coolDown
                    )));
            if (data instanceof PVZSeedPackets.RecipeSeedPacketData<?> && ((PVZSeedPackets.RecipeSeedPacketData<?>)data).recipe != null) {
                model(Model.SeedPacket, res("seed_packets/seed"), res("plants/" + name));
            }
            seedMap.put(data,
                    item(name + "_seed", () -> new SeedItem(
                            new Item.Properties().stacksTo(16).tab(PVZItemTabs.PVZ_PLANT_CARDS), data.entitySupplier, data.resource, data.cost, data.coolDown
                    )));
        });
    }

    public static void release(){
        List.of(tagMap, seedMap, seedPacketMap).forEach(Map::clear);
        modelList.clear();
    }

    public enum Model {
        Simple, Block, SpawnEgg, SeedPacket, Modeled
    }
}
