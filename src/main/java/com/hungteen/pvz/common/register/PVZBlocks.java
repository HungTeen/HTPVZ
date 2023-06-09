package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.block.CarpMossBlock;
import com.hungteen.pvz.common.block.PVZStandingSignBlock;
import com.hungteen.pvz.common.block.PVZWallSignBlock;
import com.hungteen.pvz.generator.loot.BlockLootGen;
import com.hungteen.pvz.world.NutTreeGrower;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.grower.AbstractTreeGrower;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.function.Supplier;

import static com.hungteen.pvz.common.register.PVZItemTabs.PVZ_BLOCKS;
import static net.minecraft.world.level.block.Blocks.*;

public class PVZBlocks {

    //init
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PVZMod.MODID);
    private static CreativeModeTab storedTab = PVZ_BLOCKS;
    private static Supplier<Block> storedSup = () -> new Block(BlockBehaviour.Properties.of(Material.STONE));
    private static final PVZBlocks reflector = new PVZBlocks();
    public static List<WoodType> woodTypeList = new ArrayList<>();
    public static List<Map<WoodSet, RegistryObject<Block>>> woodList = new ArrayList<>();
    //model
    private static Model storedModel = Model.Simple;
    private static List<ResourceLocation> storedModelTexture = List.of();
    private static Pair<PVZItems.Model, List<ResourceLocation>> storedItemModel = Pair.of(PVZItems.Model.Block, new ArrayList<>());
    private static boolean hasItem = true;
    public static List<Pair<RegistryObject<Block>, Pair<Model, List<ResourceLocation>>>> modelList = new ArrayList<>();
    //tag
    private static List<TagKey<Block>> storedTag = new ArrayList<>();
    public static Map<RegistryObject<Block>, List<TagKey<Block>>> tagMap = new HashMap<>();
    //renderType
    private static String storedRenderType = null;
    public static Map<RegistryObject<Block>, String> renderTypeMap = new HashMap<>();
    //blockEntity
    private static String storedBlockEntity = null;
    public static Map<String, List<RegistryObject<Block>>> blockEntityMap = new HashMap<>();
    //flammable
    private static Pair<Integer, Integer> storedFlammable = new Pair<>(0, 0);
    public static Map<RegistryObject<Block>, Pair<Integer, Integer>> flammableMap = new HashMap<>();
    //loot
    private static Boolean storedLoot = true;
    public static List<RegistryObject<Block>> lootedList = new ArrayList<>();



    //registry
    public static final RegistryObject<Block> NUT_LEAVES_WITH_NUTS = tag(BlockTags.LEAVES).renderType("cutout").flammable(30, 60).loot(false).block("nut_leaves_with_nuts", () -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));
    public static final Map<WoodSet, RegistryObject<Block>> NUT = wood("nut", new NutTreeGrower());
    public static final RegistryObject<Block> CARP_MOSS = tag(BlockTags.REPLACEABLE_PLANTS).model(Model.Modeled).renderType("cutout").flammable(5, 5).loot(false).block("carp_moss", () -> new CarpMossBlock(BlockBehaviour.Properties.copy(GLOW_LICHEN).randomTicks()));
    /**Loot data has no auto generator: {@link BlockLootGen #addTables()}*/



    //definitions
    private static RegistryObject<Block> block(String name){
        return block(name, storedSup);
    }

    private static RegistryObject<Block> block(String name, Supplier<Block> sup){
        RegistryObject<Block> blockObj = BLOCKS.register(name, sup);
        //model
        if (hasItem){
            CreativeModeTab tmpTab = storedTab;
            PVZItems.modelList.add(Pair.of(
                    PVZItems.ITEMS.register(name, () -> new BlockItem(blockObj.get(), storedTab == null ? new Item.Properties() : new Item.Properties().tab(tmpTab))),
                    storedItemModel));
        }
        if (storedModel != Model.Modeled){
            modelList.add(Pair.of(blockObj, Pair.of(storedModel, storedModelTexture)));
        }
        hasItem = true;
        storedModel = Model.Simple;
        storedModelTexture = List.of();
        storedItemModel = Pair.of(PVZItems.Model.Block, new ArrayList<>());
        //tag
        tagMap.put(blockObj, storedTag);
        storedTag = new ArrayList<>();
        //renderType
        if (storedRenderType != null){
            renderTypeMap.put(blockObj, storedRenderType);
            storedRenderType = null;
        }
        //blockEntity
        if (storedBlockEntity != null){
            if (! blockEntityMap.containsKey(storedBlockEntity)){
                blockEntityMap.put(storedBlockEntity, List.of(blockObj));
            } else {
                List<RegistryObject<Block>> list = new ArrayList<>(List.copyOf(blockEntityMap.get(storedBlockEntity)));
                list.add(blockObj);
                blockEntityMap.put(storedBlockEntity, list);
            }
            storedBlockEntity = null;
        }
        //flammable
        if (!storedFlammable.equals(Pair.of(0, 0))){
            flammableMap.put(blockObj, storedFlammable);
            storedFlammable = Pair.of(0, 0);
        }
        //loot
        if (!storedLoot){
            storedLoot = true;
            lootedList.add(blockObj);
        }
        //return
        return blockObj;
    }

    private static Map<WoodSet, RegistryObject<Block>> wood(String name, AbstractTreeGrower treeGrower, TagKey<Block>... tags){
        /**Boats need to be registered separately in {@link PVZItems}*/
        //init
        Map<WoodSet, RegistryObject<Block>> set = new HashMap<>();
        WoodType woodtype = WoodType.create(name);
        WoodType.register(woodtype);
        woodTypeList.add(woodtype);
        CreativeModeTab tmpTab = storedTab;
        short flame = 1;
        if (Arrays.stream(tags).toList().contains(BlockTags.NON_FLAMMABLE_WOOD)){
            flame = 0;
        }
        //register
        set.put(WoodSet.Plank, tag(BlockTags.PLANKS).tag(tags).flammable(flame*5, flame*20).block(name+"_planks"));
        set.put(WoodSet.Sampling, tag(BlockTags.SAPLINGS).tag(tags).model(Model.Cross).renderType("cutout").itemModel(PVZItems.Model.Simple, res(name+"_sapling")).block(name+"_sapling", () -> new SaplingBlock(treeGrower, BlockBehaviour.Properties.copy(OAK_SAPLING))));
        set.put(WoodSet.Leaves, tag(BlockTags.LEAVES).tag(tags).renderType("cutout").flammable(flame*5, flame*60).loot(false).block(name+"_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES))));
        if (flame == 1) {
            set.put(WoodSet.Wood, tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN).tag(tags).model(Model.Column, res(name + "_log"), res(name + "_log")).flammable(5, 5).block(name + "_wood", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD))));
            set.put(WoodSet.StWood, tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN).tag(tags).model(Model.Column, res("stripped_" + name + "_log"), res("stripped_" + name + "_log")).flammable(5, 5).block("stripped_" + name + "_wood", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_WOOD))));
            set.put(WoodSet.Log, tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN).tag(tags).model(Model.Column).flammable(5, 5).block(name + "_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG))));
            set.put(WoodSet.StLog, tag(BlockTags.LOGS, BlockTags.LOGS_THAT_BURN).tag(tags).model(Model.Column, res("stripped_" + name + "_log"), res(name + "_log_top")).flammable(5, 5).block("stripped_" + name + "_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG))));
        } else {
            set.put(WoodSet.Wood, tag(BlockTags.LOGS).tag(tags).model(Model.Column, res(name + "_log"), res(name + "_log")).block(name + "_wood", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD))));
            set.put(WoodSet.StWood, tag(BlockTags.LOGS).tag(tags).model(Model.Column, res("stripped_" + name + "_log"), res("stripped_" + name + "_log")).block("stripped_" + name + "_wood", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_WOOD))));
            set.put(WoodSet.Log, tag(BlockTags.LOGS).tag(tags).model(Model.Column).block(name + "_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG))));
            set.put(WoodSet.StLog, tag(BlockTags.LOGS).tag(tags).model(Model.Column, res("stripped_" + name + "_log"), res(name + "_log_top")).block("stripped_" + name + "_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG))));
        }
        set.put(WoodSet.Slab, tag(BlockTags.WOODEN_SLABS).tag(tags).model(Model.Slab, res(name+"_planks")).flammable(flame*5, flame*5).block(name+"_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(OAK_SLAB))));
        set.put(WoodSet.Stairs, tag(BlockTags.WOODEN_STAIRS).tag(tags).model(Model.Stairs, res(name+"_planks")).flammable(flame*5, flame*20).block(name+"_stairs", () -> new StairBlock(() -> set.get(WoodSet.Plank).get().defaultBlockState(), BlockBehaviour.Properties.copy(OAK_STAIRS))));
        set.put(WoodSet.Door, tag(BlockTags.WOODEN_DOORS).tag(tags).model(Model.Door).itemModel(PVZItems.Model.Simple).block(name+"_door", () -> new DoorBlock(BlockBehaviour.Properties.copy(OAK_DOOR))));
        set.put(WoodSet.Trapdoor, tag(BlockTags.WOODEN_TRAPDOORS).tag(tags).model(Model.Trapdoor).itemModel(PVZItems.Model.Block, res(name+"_trapdoor_bottom")).block(name+"_trapdoor", () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(OAK_TRAPDOOR))));
        set.put(WoodSet.Fence, tag(BlockTags.WOODEN_FENCES).tag(tags).model(Model.Fence, res(name+"_planks")).itemModel(PVZItems.Model.Block, res(name+"_fence_inventory")).flammable(flame*5, flame*20).block(name+"_fence", () -> new FenceBlock(BlockBehaviour.Properties.copy(OAK_FENCE))));
        set.put(WoodSet.Gate, tag(BlockTags.FENCE_GATES).tag(tags).model(Model.Gate, res(name+"_planks")).flammable(flame*5, flame*20).block(name+"_fence_gate", () -> new FenceGateBlock(BlockBehaviour.Properties.copy(OAK_FENCE_GATE))));
        set.put(WoodSet.Button, tag(BlockTags.WOODEN_BUTTONS).tag(tags).model(Model.Button, res(name+"_planks")).itemModel(PVZItems.Model.Block, res(name+"_button_inventory")).block(name+"_button", () -> new WoodButtonBlock(BlockBehaviour.Properties.copy(OAK_BUTTON))));
        set.put(WoodSet.Plate, tag(BlockTags.WOODEN_PRESSURE_PLATES).tag(tags).model(Model.Plate, res(name+"_planks")).block(name+"_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(OAK_PRESSURE_PLATE))));
        set.put(WoodSet.Sign, noItem().tag(BlockTags.STANDING_SIGNS).tag(tags).model(Model.Sign).blockEntity("pvz_sign").block(name+"_sign", () -> new PVZStandingSignBlock(BlockBehaviour.Properties.copy(OAK_SIGN), woodtype)));
        set.put(WoodSet.WallSign, noItem().tag(BlockTags.WALL_SIGNS).tag(tags).model(Model.WallSign, res(name+"_planks")).blockEntity("pvz_sign").block(name+"_wall_sign", () -> new PVZWallSignBlock(BlockBehaviour.Properties.copy(OAK_WALL_SIGN), woodtype)));
        tab(tmpTab);
        PVZItems.modelList.add(Pair.of(
                PVZItems.ITEMS.register(name+"_sign", () -> new SignItem(storedTab == null ? new Item.Properties() : new Item.Properties().tab(tmpTab), set.get(WoodSet.Sign).get(), set.get(WoodSet.WallSign).get())),
                Pair.of(PVZItems.Model.Simple, List.of())));
        woodList.add(set);
        PVZItems.boat(false, woodtype);
        PVZItems.boat(true, woodtype);
        /**Boats added to {@link PVZItems#boatItemMap}.*/
        return set;
    }

    private static PVZBlocks tab(CreativeModeTab tab){
        storedTab = tab;
        return reflector;
    }
    private static PVZBlocks material(Material material){
        storedSup = () -> new Block(BlockBehaviour.Properties.of(material));
        return reflector;
    }
    private static PVZBlocks material(Block block){
        storedSup = () -> new Block(BlockBehaviour.Properties.copy(block));
        return reflector;
    }
    private static PVZBlocks sup(Supplier<Block> sup){
        storedSup = sup;
        return reflector;
    }
    private static PVZBlocks model(Model model){
        storedModel = model;
        storedModelTexture = List.of();
        return reflector;
    }
    private static PVZBlocks model(Model model, ResourceLocation... res){
        return model(model, List.of(res));
    }
    private static PVZBlocks model(Model model, List<ResourceLocation> list){
        storedModel = model;
        storedModelTexture = list;
        return reflector;
    }
    private static PVZBlocks tag(TagKey<Block>... tags){
        return tag(Arrays.asList(tags));
    }
    private static PVZBlocks tag(List<TagKey<Block>> list){
        storedTag.addAll(list);
        return reflector;
    }
    private static ResourceLocation res(String path){
        return new ResourceLocation(PVZMod.MODID, ModelProvider.BLOCK_FOLDER + "/" + path);
    }
    private static PVZBlocks noItem(){
        hasItem = false;
        return reflector;
    }
    private static PVZBlocks itemModel(PVZItems.Model model, ResourceLocation... res){
        return itemModel(model, List.of(res));
    }
    private static PVZBlocks itemModel(PVZItems.Model model, List<ResourceLocation> res){
        storedItemModel = Pair.of(model, res);
        return reflector;
    }
    private static PVZBlocks renderType(String type){
        storedRenderType = type;
        return reflector;
    }
    private static PVZBlocks blockEntity(String blockentity){
        storedBlockEntity = blockentity;
        return reflector;
    }
    private static PVZBlocks flammable(Integer ignite, Integer burn){
        storedFlammable = Pair.of(ignite, burn);
        return reflector;
    }
    private static PVZBlocks loot(Boolean loot){
        storedLoot = loot;
        return reflector;
    }

    public static void release(){
        List.of(woodList, modelList, lootedList).forEach(List::clear);
        List.of(renderTypeMap, tagMap, blockEntityMap, flammableMap).forEach(Map::clear);
    }

    public enum WoodSet {
        Plank, Slab, Stairs,
        Door, Trapdoor, Fence, Gate,
        Log, Wood, StLog, StWood,
        Button, Plate, Sign, WallSign,
        Boat, ChestBoat,
        Sampling, Leaves
    }

    public enum Model {
        Simple, Column, Box, Cross, Carpet,
        Slab, Stairs, Door, Trapdoor,
        Plate, Button, Sign, WallSign,
        Fence, Gate,
        Modeled
    }
}
