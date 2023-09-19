package com.hungteen.pvz.common.tags;

import com.hungteen.pvz.Util;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;

public class PVZItemTags {

    public static final TagKey<Item> ESSENCE = pvzTag("essence");

    //definition

    public static TagKey<Item> pvzTag(String name){
        return ItemTags.create(Util.prefix(name));
    }
}
