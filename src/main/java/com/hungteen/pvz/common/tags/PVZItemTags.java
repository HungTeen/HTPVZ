package com.hungteen.pvz.common.tags;

import com.hungteen.pvz.util.Util;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class PVZItemTags {

    public static final TagKey<Item> ESSENCE = pvzTag("essence");
    public static final TagKey<Item> TO_TERRA_ESSENCE = pvzTag("to_terra_essence");
    public static final TagKey<Item> IRON = pvzTag("iron");

    //definition

    public static TagKey<Item> pvzTag(String name){
        return ItemTags.create(Util.prefix(name));
    }
}
