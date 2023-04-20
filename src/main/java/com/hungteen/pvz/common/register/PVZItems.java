package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.mojang.datafixers.util.Pair;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.item.BoatItem;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

import static com.hungteen.pvz.common.register.PVZItemTabs.PVZ_MISC;

public class PVZItems {

    //init
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, PVZMod.MODID);
    public static Supplier<Item> storedSup = () -> new Item(new Item.Properties().tab(PVZ_MISC));
    private static Pair<Model, List<ResourceLocation>> storedModel = Pair.of(Model.Simple, new ArrayList<>());
    public static List<Pair<RegistryObject<Item>, Pair<Model, List<ResourceLocation>>>> modelList = new ArrayList<>();
    private static final PVZItems tmpObj = new PVZItems();


    //registry
    public static final RegistryObject<Item> NUT = item("nut");
    public static final RegistryObject<Item> PEA = item("pea");
    //public static final RegistryObject<Item> NUT_BOAT = item("nut_boat", () -> new BoatItem(false, Boat.Type., (new Item.Properties()).stacksTo(1).tab(CreativeModeTab.TAB_TRANSPORTATION)));


    //definitions
    private static PVZItems model(Model model, ResourceLocation... res){
        storedModel = Pair.of(model, List.of(res));
        return tmpObj;
    }

    private static PVZItems model(Model model, List<ResourceLocation> res){
        storedModel = Pair.of(model, res);
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
        if (storedModel.getFirst() != Model.Modeled){
            modelList.add(Pair.of(item, storedModel));
        }
        storedModel = Pair.of(Model.Simple, new ArrayList<ResourceLocation>());
        return item;
    }

    public enum Model {
        Simple, Block, Modeled
    }
}
