package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.properties.WoodType;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.hungteen.pvz.common.register.PVZItemTabs.PVZ_BLOCKS;
import static net.minecraft.world.level.block.Blocks.*;

public class PVZBlocks {

    //init
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PVZMod.MODID);
    public static List<RegistryObject<Block>> blockList = new ArrayList<>();
    private static CreativeModeTab storedTab = PVZ_BLOCKS;
    private static Supplier<Block> storedSup = () -> new Block(BlockBehaviour.Properties.of(Material.STONE));
    private static final PVZBlocks tmpObj = new PVZBlocks();
    //model
    private static Model storedModel = Model.Simple;
    private static List<ResourceLocation> storedModelTexture = List.of();
    private static Pair<PVZItems.Model, List<ResourceLocation>> storedItemModel = Pair.of(PVZItems.Model.Block, new ArrayList<>());
    public static List<Pair<RegistryObject<Block>, Pair<Model, List<ResourceLocation>>>> modelList = new ArrayList<>();
    //tag
    public static Map<RegistryObject<Block>, List<TagKey<Block>>> tagMap = new HashMap<>();
    private static List<TagKey<Block>> storedTag = new ArrayList<>();


    //registry
    public static final RegistryObject<Block> NUT_LEAVES_WITH_NUTS = block("nut_leaves_with_nuts", () -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));
    public static final Map<WoodSet, RegistryObject<Block>> NUT = wood("nut");


    //definitions
    private static RegistryObject<Block> block(String name){
        return block(name, storedSup);
    }

    private static RegistryObject<Block> block(String name, Supplier<Block> sup){
        RegistryObject<Block> BlockObj = BLOCKS.register(name, sup);
        blockList.add(BlockObj);
        //model
        CreativeModeTab tmpTab = storedTab;
        PVZItems.modelList.add(Pair.of(
                PVZItems.ITEMS.register(name, () -> new BlockItem(BlockObj.get(), storedTab == null ? new Item.Properties() : new Item.Properties().tab(tmpTab))),
                storedItemModel));
        if (storedModel != Model.Modeled){
            modelList.add(Pair.of(BlockObj, Pair.of(storedModel, storedModelTexture)));
        }
        storedModel = Model.Simple;
        storedModelTexture = List.of();
        storedItemModel = Pair.of(PVZItems.Model.Block, new ArrayList<>());
        //tag
        tagMap.put(BlockObj, storedTag);
        storedTag = List.of();
        return BlockObj;
    }

    private static Map<WoodSet, RegistryObject<Block>> wood(String name){
        // Related items contained.
        Map<WoodSet, RegistryObject<Block>> set = new HashMap();
        set.put(WoodSet.Plank, tag(BlockTags.PLANKS).block(name+"_planks"));
//        set.put(WoodSet.Sampling, tag(BlockTags.SAPLINGS).model(Model.Cross).block(name+"_sampling", new SaplingBlock()));
        set.put(WoodSet.Leaves, tag(BlockTags.LEAVES).block(name+"_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES))));
            set.put(WoodSet.Wood, tag(BlockTags.LOGS).model(Model.Column, res("nut_log"), res("nut_log")).block(name+"_wood", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD))));
        set.put(WoodSet.StWood, tag(BlockTags.LOGS).model(Model.Column, res("stripped_nut_log"), res("stripped_nut_log")).block("stripped_"+name+"_wood", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_WOOD))));
        set.put(WoodSet.Log, tag(BlockTags.LOGS).model(Model.Column).block(name+"_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG))));
        set.put(WoodSet.StLog, tag(BlockTags.LOGS).model(Model.Column, res("stripped_nut_log"), res("nut_log_top")).block("stripped_"+name+"_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG))));
        set.put(WoodSet.Slab, tag(BlockTags.WOODEN_SLABS).model(Model.Slab, res("nut_planks")).block(name+"_slab", () -> new SlabBlock(BlockBehaviour.Properties.copy(OAK_SLAB))));
        set.put(WoodSet.Stairs, tag(BlockTags.WOODEN_STAIRS).model(Model.Stairs, res("nut_planks")).block(name+"_stairs", () -> new StairBlock(() -> set.get(WoodSet.Plank).get().defaultBlockState(), BlockBehaviour.Properties.copy(OAK_STAIRS))));
        set.put(WoodSet.Door, tag(BlockTags.WOODEN_DOORS).model(Model.Door).itemModel(PVZItems.Model.Simple).block(name+"_door", () -> new DoorBlock(BlockBehaviour.Properties.copy(OAK_DOOR))));
        set.put(WoodSet.Trapdoor, tag(BlockTags.WOODEN_TRAPDOORS).model(Model.Trapdoor).itemModel(PVZItems.Model.Block, res("nut_trapdoor_bottom")).block(name+"_trapdoor", () -> new TrapDoorBlock(BlockBehaviour.Properties.copy(OAK_TRAPDOOR))));
        set.put(WoodSet.Fence, tag(BlockTags.WOODEN_FENCES).model(Model.Fence, res("nut_planks")).itemModel(PVZItems.Model.Block, res("nut_fence_inventory")).block(name+"_fence", () -> new FenceBlock(BlockBehaviour.Properties.copy(OAK_FENCE))));
        set.put(WoodSet.Gate, tag(BlockTags.FENCE_GATES).model(Model.Gate, res("nut_planks")).block(name+"_fence_gate", () -> new FenceGateBlock(BlockBehaviour.Properties.copy(OAK_FENCE_GATE))));
        set.put(WoodSet.Button, tag(BlockTags.WOODEN_BUTTONS).model(Model.Button, res("nut_planks")).itemModel(PVZItems.Model.Block, res("nut_button_inventory")).block(name+"_button", () -> new WoodButtonBlock(BlockBehaviour.Properties.copy(OAK_BUTTON))));
        set.put(WoodSet.Plate, tag(BlockTags.WOODEN_PRESSURE_PLATES).model(Model.Plate, res("nut_planks")).block(name+"_pressure_plate", () -> new PressurePlateBlock(PressurePlateBlock.Sensitivity.EVERYTHING, BlockBehaviour.Properties.copy(OAK_PRESSURE_PLATE))));
        set.put(WoodSet.Sign, tag(BlockTags.STANDING_SIGNS).model(Model.Sign).itemModel(PVZItems.Model.Simple).block(name+"_sign", () -> new StandingSignBlock(BlockBehaviour.Properties.copy(OAK_SIGN), WoodType.OAK)));
        set.put(WoodSet.WallSign, tab(null).tag(BlockTags.WALL_SIGNS).model(Model.WallSign, res("nut_planks")).block(name+"_wall_sign", () -> new WallSignBlock(BlockBehaviour.Properties.copy(OAK_WALL_SIGN), WoodType.OAK)));
        tab(PVZ_BLOCKS);
        return set;
    }

    private static PVZBlocks tab(CreativeModeTab tab){
        storedTab = tab;
        return tmpObj;
    }

    private static PVZBlocks material(Material material){
        storedSup = () -> new Block(BlockBehaviour.Properties.of(material));
        return tmpObj;
    }

    private static PVZBlocks material(Block block){
        storedSup = () -> new Block(BlockBehaviour.Properties.copy(block));
        return tmpObj;
    }

    private static PVZBlocks sup(Supplier<Block> sup){
        storedSup = sup;
        return tmpObj;
    }
    private static PVZBlocks model(Model model){
        storedModel = model;
        storedModelTexture = List.of();
        return tmpObj;
    }
    private static PVZBlocks model(Model model, ResourceLocation... res){
        return model(model, List.of(res));
    }
    private static PVZBlocks model(Model model, List<ResourceLocation> list){
        storedModel = model;
        storedModelTexture = list;
        return tmpObj;
    }
    private static PVZBlocks tag(TagKey<Block>... tags){
        return tag(List.of(tags));
    }
    private static PVZBlocks tag(List<TagKey<Block>> list){
        storedTag = list;
        return tmpObj;
    }
    private static ResourceLocation res(String path){
        return new ResourceLocation(PVZMod.MODID, ModelProvider.BLOCK_FOLDER + "/" + path);
    }

    private static PVZBlocks itemModel(PVZItems.Model model, ResourceLocation... res){
        storedItemModel = Pair.of(model, List.of(res));
        return tmpObj;
    }

    private static PVZBlocks itemModel(PVZItems.Model model, List<ResourceLocation> res){
        storedItemModel = Pair.of(model, res);
        return tmpObj;
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
        Simple, Column, Box, Cross,
        Slab, Stairs, Door, Trapdoor,
        Plate, Button, Sign, WallSign,
        Fence, Gate,
        Modeled
    }

}
