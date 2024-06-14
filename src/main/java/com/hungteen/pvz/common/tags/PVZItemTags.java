package com.hungteen.pvz.common.tags;

import com.hungteen.pvz.util.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

public class PVZItemTags {

    public static final TagKey<Item> ESSENCE = pvzTag("essence");
    public static final TagKey<Item> IRON = pvzTag("iron");
    public static final TagKey<Item> GIANT_HAMMER = pvzTag("giant_hammer");
    public static final TagKey<Item> CABBAGE = forgeTag("crops/cabbage");
    public static final TagKey<Item> CORN = forgeTag("crops/kernel");

    public static final TagKey<Item> ENTITY_DAMAGEABLE_SHIELDS = pvzTag("entity_damageable_shields");

    //definition

    public static TagKey<Item> pvzTag(String name){
        return ItemTags.create(Util.prefix(name));
    }
    private static TagKey<Item> forgeTag(String name){
        return ItemTags.create(new ResourceLocation("forge", name));
    }
}
