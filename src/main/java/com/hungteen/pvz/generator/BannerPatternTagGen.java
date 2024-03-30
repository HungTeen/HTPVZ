package com.hungteen.pvz.generator;

import com.hungteen.pvz.common.register.PVZBannerPatterns;
import com.hungteen.pvz.common.tags.PVZBannerPatternTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BannerPatternTagsProvider;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class BannerPatternTagGen extends BannerPatternTagsProvider {
    public BannerPatternTagGen(DataGenerator p_236411_, String modId, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_236411_, modId, existingFileHelper);
    }

    protected void addTags() {
        this.tag(PVZBannerPatternTags.PATTERN_ITEM_BRAIN).add(PVZBannerPatterns.BRAIN.get());
        this.tag(PVZBannerPatternTags.PATTERN_ITEM_LEAF).add(PVZBannerPatterns.LEAF.get());
    }
}
