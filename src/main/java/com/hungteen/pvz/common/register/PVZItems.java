package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import static com.hungteen.pvz.common.register.PVZItemTabs.PVZ_MISC;

public class PVZItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PVZMod.MODID);

    public static final RegistryObject<Item> NUT = init().item("nut");
    public static final RegistryObject<Item> PEA = item("pea");



    //definitions
    public static Supplier<Item> storedSup;
    private static Model storedModel;
    public static Map<RegistryObject<Item>, PVZItems.Model> modelMap;
    private static final PVZItems tmpObj = new PVZItems();

    public static PVZItems init(){
        storedSup = () -> new Item(new Item.Properties().tab(PVZ_MISC));
        storedModel = Model.Simple;
        modelMap = new HashMap<>();
        return tmpObj;
    }
    private static PVZItems model(Model model){
        storedModel = model;
        return tmpObj;
    }

    private static PVZItems sup(Supplier<Item> sup){
        storedSup = sup;
        return tmpObj;
    }


    public static RegistryObject<Item> item(String name){
        return item(name, storedSup);
    }
    public static RegistryObject<Item> item(String name, Supplier<Item> sup){
        RegistryObject<Item> item = ITEMS.register(name, sup);
        if (storedModel != Model.Modeled){
            modelMap.put(item, storedModel);
        }
        storedModel = Model.Simple;
        return item;
    }

    public enum Model {
        Simple, Block, Modeled
    }
}
