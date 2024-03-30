package com.hungteen.pvz.common.tags;

import com.hungteen.pvz.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.entity.BannerPattern;

public class PVZBannerPatternTags {

    public static final TagKey<BannerPattern> PATTERN_ITEM_BRAIN = pvzTag("pattern_item/brain");
    public static final TagKey<BannerPattern> PATTERN_ITEM_LEAF = pvzTag("pattern_item/leaf");
    public static TagKey<BannerPattern> pvzTag(String name){
        return TagKey.create(Registry.BANNER_PATTERN_REGISTRY, Util.prefix(name));
    }
}
