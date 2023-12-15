package com.hungteen.pvz.generator;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

import static com.hungteen.pvz.common.register.PVZBlocks.WoodSet.*;
import static com.hungteen.pvz.common.register.PVZBlocks.woodList;
import static com.hungteen.pvz.common.register.PVZBlocks.woodTypeList;

public class BlockTagGen extends BlockTagsProvider {

    public BlockTagGen(DataGenerator p_126511_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_126511_, PVZMod.MODID, existingFileHelper);
    }
    @Override
    public void addTags(){
        //atBlockRegister
        PVZBlocks.tagMap.forEach((block, tagList)-> tagList.forEach((tag) -> this.tag(tag).add(block.get())));
        //woodSet
        for (int i = 0; i < woodList.size(); i ++){
            String name = woodTypeList.get(i).name();
            LOGS.add(tag(name + "_logs"));
            this.tag(LOGS.get(i)).add(woodList.get(i).get(Log).get(),
                    woodList.get(i).get(StLog).get(),
                    woodList.get(i).get(Wood).get(),
                    woodList.get(i).get(StWood).get()
            );
        }
        //others
        this.tag(PVZBlockTags.UNPLANTABLE_DIRT).add(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT);
        this.tag(PVZBlockTags.PLANTABLE_BLOCKS).add(
                Blocks.GRASS_BLOCK, Blocks.MYCELIUM, Blocks.PODZOL, Blocks.DIRT_PATH,
                Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM,
                Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET, PVZBlocks.CARP_GRASS.get(),
                Blocks.SCULK, Blocks.SCULK_CATALYST,
                Blocks.LILY_PAD, Blocks.FLOWER_POT
        );
        this.tag(PVZBlockTags.WATER).add(Blocks.WATER);
    }

    public static final List<TagKey<Block>> LOGS = new ArrayList<>();

    private static TagKey<Block> tag(String path) {
        return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(PVZMod.MODID + ":" + path));
    }
}
