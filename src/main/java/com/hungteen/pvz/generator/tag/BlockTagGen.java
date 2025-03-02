package com.hungteen.pvz.generator.tag;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import net.minecraft.core.Registry;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BlockTagsProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.common.Tags;
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
        this.tag(PVZBlockTags.UNPLANTABLE_DIRT).add(Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT, Blocks.SOUL_SAND, Blocks.SOUL_SOIL, Blocks.SAND, Blocks.RED_SAND);
        this.tag(PVZBlockTags.PLANTABLE_DIRT).add(
                Blocks.GRASS_BLOCK, Blocks.MYCELIUM, Blocks.PODZOL, Blocks.DIRT_PATH,
                Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM,
                Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET, PVZBlocks.CARP_GRASS.get(),
                Blocks.SCULK, Blocks.SCULK_CATALYST,
                Blocks.LILY_PAD
        );
        this.tag(PVZBlockTags.PLANTABLE_STONE).addTags(
                Tags.Blocks.STONE, Tags.Blocks.NETHERRACK, BlockTags.TERRACOTTA, Tags.Blocks.COBBLESTONE,
                Tags.Blocks.SANDSTONE, Tags.Blocks.SAND, Tags.Blocks.GRAVEL)
                .add(Blocks.BLACKSTONE, Blocks.PRISMARINE, Blocks.DRIPSTONE_BLOCK);
        this.tag(PVZBlockTags.WISDOM_TREE_REPLACEABLE).addTags(BlockTags.MOSS_REPLACEABLE, BlockTags.REPLACEABLE_PLANTS, BlockTags.LEAVES);
        this.tag(PVZBlockTags.SCULK).add(Blocks.SCULK, Blocks.SCULK_CATALYST, Blocks.SCULK_VEIN, Blocks.SCULK_SENSOR, Blocks.SCULK_SHRIEKER);
        this.tag(BlockTags.DIRT).add(PVZBlocks.ORIGIN_ORE.get());
    }

    public static final List<TagKey<Block>> LOGS = new ArrayList<>();

    private static TagKey<Block> tag(String path) {
        return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(PVZMod.MODID + ":" + path));
    }
}
