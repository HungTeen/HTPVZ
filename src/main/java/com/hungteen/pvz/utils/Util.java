package com.hungteen.pvz.utils;

import com.hungteen.pvz.PVZMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.block.Block;

public class Util {
    public static ResourceLocation prefix(String name){
        return new ResourceLocation(PVZMod.MODID, name);
    }
    public static String name(EntityType<? extends Entity> obj){
        return obj.toString().substring(("entity."+PVZMod.MODID+".").length());
    }
    public static String name(Block obj){
        return obj.toString().substring(("block."+PVZMod.MODID+".").length());
    }
}
