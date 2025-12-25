package com.hungteen.pvz.common.tags;

import com.hungteen.pvz.util.Util;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class PVZBlockTags {

    public static final TagKey<Block> UNPLANTABLE_DIRT = pvzTag("unplantable_dirt");
    public static final TagKey<Block> PLANTABLE_DIRT = pvzTag("plantable_dirt");
    public static final TagKey<Block> SCULK = pvzTag("sculk");
    public static final TagKey<Block> PLANTABLE_STONE = pvzTag("plantable_stone");
    public static final TagKey<Block> WISDOM_TREE_REPLACEABLE = pvzTag("wisdom_tree_replaceable");
    public static final TagKey<Block> GARDEN_FLOWER_POT = pvzTag("garden_flower_pot");

    //definition

    public static TagKey<Block> pvzTag(String name){
        return BlockTags.create(Util.prefix(name));
    }
}
