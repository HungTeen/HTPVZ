package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.material.Material;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.hungteen.pvz.common.register.PVZItemTabs.PVZ_BLOCKS;

public class PVZBlocks {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, PVZMod.MODID);

    public static final RegistryObject<Block> NUT_PLANKS = init().block("nut_planks");
//    public static final RegistryObject<Block> BLOCK1 = block("block1");
//    public static final RegistryObject<Block> BLOCK2 = block("block2");



    //definitions
    private static CreativeModeTab storedTab;
    private static Supplier<Block> storedSup;
    private static Model storedModel;
    private static PVZItems.Model storedItemModel;
    public static Map<RegistryObject<Block>, Model> modelMap;
    private static final PVZBlocks tmpObj = new PVZBlocks();

    private static PVZBlocks init(){
        storedTab = PVZ_BLOCKS;
        storedSup = () -> new Block(BlockBehaviour.Properties.of(Material.STONE));
        storedModel = Model.Simple;
        storedItemModel = PVZItems.Model.Block;
        modelMap = new HashMap<>();
        return tmpObj;
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
    private static PVZBlocks model(Model tomodel){
        storedModel = tomodel;
        return tmpObj;
    }

    private static PVZBlocks itemModel(PVZItems.Model tomodel){
        storedItemModel = tomodel;
        return tmpObj;
    }

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
            modelMap.put(BlockObj, storedModel);
        }
        storedModel = Model.Simple;
        storedItemModel = PVZItems.Model.Block;
        return BlockObj;
    }

    private static Map<String, RegistryObject<?>> wood(String name){
        return null;
    }

    public enum Model {
        Simple, Modeled
    }
}
