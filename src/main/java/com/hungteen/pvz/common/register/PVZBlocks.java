package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.client.model.generators.ModelProvider;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static com.hungteen.pvz.common.register.PVZItemTabs.PVZ_BLOCKS;

public class PVZBlocks {

    //init
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PVZMod.MODID);
    private static CreativeModeTab storedTab = PVZ_BLOCKS;
    private static Supplier<Block> storedSup = () -> new Block(BlockBehaviour.Properties.of(Material.STONE));
    private static Model storedModel = Model.Simple;
    private static List<ResourceLocation> storedModelTexture = List.of();
    private static PVZItems.Model storedItemModel = PVZItems.Model.Block;
    public static Map<RegistryObject<Block>, Pair<Model, List<ResourceLocation>>> modelMap = new HashMap<>();
    private static final PVZBlocks tmpObj = new PVZBlocks();


    //registry
    public static final RegistryObject<Block> NUT_LEAVES_WITH_NUTS = block("nut_leaves_with_nuts", () -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES)));
    public static final Map<WoodSet, RegistryObject<Block>> NUT = wood("nut");


    //definitions
    private static RegistryObject<Block> block(String name){
        return block(name, storedSup);
    }

    private static RegistryObject<Block> block(String name, Supplier<Block> sup){
        RegistryObject<Block> BlockObj = BLOCKS.register(name, sup);
        CreativeModeTab tmpTab = storedTab;
        PVZItems.modelMap.put(
                PVZItems.ITEMS.register(name, () -> new BlockItem(BlockObj.get(), storedTab == null ? new Item.Properties() : new Item.Properties().tab(tmpTab))),
                storedItemModel);
        if (storedModel != Model.Modeled){
            modelMap.put(BlockObj, Pair.of(storedModel, storedModelTexture));
        }
        storedModel = Model.Simple;
        storedModelTexture = List.of();
        storedItemModel = PVZItems.Model.Block;
        return BlockObj;
    }

    private static Map<WoodSet, RegistryObject<Block>> wood(String name){
        Map<WoodSet, RegistryObject<Block>> set = new HashMap();
        set.put(WoodSet.Plank, block(name+"_planks"));
//        set.put(WoodSet.Sampling, model(Model.Cross).block(name+"_sampling"));
        set.put(WoodSet.Leaves, block(name+"_leaves", () -> new LeavesBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LEAVES))));
        set.put(WoodSet.Wood, model(Model.Column, res("nut_log"), res("nut_log")).block(name+"_wood", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_WOOD))));
        set.put(WoodSet.StWood, model(Model.Column, res("stripped_nut_log"), res("stripped_nut_log")).block("stripped_"+name+"_wood", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_WOOD))));
        set.put(WoodSet.Log, model(Model.Column).block(name+"_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.OAK_LOG))));
        set.put(WoodSet.StLog, model(Model.Column, res("stripped_nut_log"), res("nut_log_top")).block("stripped_"+name+"_log", () -> new RotatedPillarBlock(BlockBehaviour.Properties.copy(Blocks.STRIPPED_OAK_LOG))));
//        set.put(WoodSet.Slab, model(Model.Slab).block(name+"_slab"));
//        set.put(WoodSet.Stairs, model(Model.Stairs).block(name+"_stairs"));
//        set.put(WoodSet.Door, model(Model.Door).block(name+"_door"));
//        set.put(WoodSet.Trapdoor, model(Model.Trapdoor).block(name+"_trapdoor"));
//        set.put(WoodSet.Fence, model(Model.Fence).block(name+"_fence"));
//        set.put(WoodSet.Gate, model(Model.Gate).block(name+"_fence_gate"));
//        set.put(WoodSet.Button, model(Model.Button).block(name+"_button"));
//        set.put(WoodSet.Plate, model(Model.Plate).block(name+"_pressure_plate"));
//        set.put(WoodSet.Sign, model(Model.Sign).block(name+"_sign"));
//        set.put(WoodSet.WallSign, tab(null).model(Model.WallSign).block(name+"_wall_sign"));
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
    private static ResourceLocation res(String path){
        return new ResourceLocation(PVZMod.MODID, ModelProvider.BLOCK_FOLDER + "/" + path);
    }

    private static PVZBlocks itemModel(PVZItems.Model model){
        storedItemModel = model;
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
