package com.hungteen.pvz.common.tags;

import com.hungteen.pvz.Util;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;

public class PVZBlockTags {

    public static final TagKey<Block> UNPLANTABLE_DIRT = pvzTag("unplantable_dirt");
    public static final TagKey<Block> PLANTABLE_BLOCKS = pvzTag("plantable_dirt");

    //definition

    public static TagKey<Block> pvzTag(String name){
        return BlockTags.create(Util.prefix(name));
    }
}
