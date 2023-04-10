package com.hungteen.pvz.generator;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZBlocks;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.Nullable;

public class BlockTagGen extends BlockTagsProvider {

    public BlockTagGen(DataGenerator p_126511_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_126511_, PVZMod.MODID, existingFileHelper);
    }
    @Override
    public void addTags(){
        PVZBlocks.tagMap.forEach((block, tagList)-> {
            tagList.forEach((tag) -> {
                this.tag(tag).add(block.get());
            });
        });
    }
}
