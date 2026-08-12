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
import net.minecraftforge.common.Tags;
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
        PVZBlocks.tagMap.forEach((block, tagList)-> tagList.forEach((tag) -> this.tag(tag).add(block.get()).replace(false)));
        //woodSet
        for (int i = 0; i < woodList.size(); i ++){
            String name = woodTypeList.get(i).name();
            LOGS.add(tag(name + "_logs"));
            this.tag(LOGS.get(i)).add(woodList.get(i).get(Log).get(),
                    woodList.get(i).get(StLog).get(),
                    woodList.get(i).get(Wood).get(),
                    woodList.get(i).get(StWood).get()
            );
        }//others
        this.tag(PVZBlockTags.UNPLANTABLE_DIRT).add(
                Blocks.DIRT, Blocks.COARSE_DIRT, Blocks.ROOTED_DIRT,
                Blocks.SOUL_SAND, Blocks.SOUL_SOIL, PVZBlocks.FLOATING_SOUL_SOIL.get(),
                Blocks.SAND, Blocks.RED_SAND, Blocks.PACKED_MUD, Blocks.MUD
        );
        this.tag(PVZBlockTags.PLANTABLE_DIRT).add(
                Blocks.GRASS_BLOCK, Blocks.MYCELIUM, Blocks.PODZOL, Blocks.DIRT_PATH,
                Blocks.CRIMSON_NYLIUM, Blocks.WARPED_NYLIUM,
                Blocks.MOSS_BLOCK, Blocks.MOSS_CARPET, PVZBlocks.CARP_GRASS.get(),
                Blocks.SCULK, Blocks.SCULK_CATALYST,
                Blocks.LILY_PAD,
                PVZBlocks.GARDEN_FLOWER_POT.get()
        );
        this.tag(PVZBlockTags.PLANTABLE_STONE).addTags(
                Tags.Blocks.STONE, Tags.Blocks.NETHERRACK, BlockTags.TERRACOTTA, Tags.Blocks.COBBLESTONE,
                        Tags.Blocks.SANDSTONE, Tags.Blocks.SAND, Tags.Blocks.GRAVEL, Tags.Blocks.OBSIDIAN,
                        BlockTags.STONE_BRICKS, BlockTags.WALLS, Tags.Blocks.ORES)
                .add(Blocks.DRIPSTONE_BLOCK, Blocks.BASALT, Blocks.POLISHED_BASALT, Blocks.SMOOTH_BASALT, Blocks.BRICKS, Blocks.CALCITE, Blocks.CRYING_OBSIDIAN
                        , Blocks.ANDESITE_SLAB, Blocks.GRANITE_SLAB, Blocks.DIORITE_SLAB, Blocks.POLISHED_ANDESITE_SLAB, Blocks.POLISHED_GRANITE_SLAB, Blocks.POLISHED_DIORITE_SLAB
                        , Blocks.INFESTED_CHISELED_STONE_BRICKS, Blocks.INFESTED_CRACKED_STONE_BRICKS, Blocks.INFESTED_MOSSY_STONE_BRICKS, Blocks.INFESTED_STONE_BRICKS
                        , Blocks.PRISMARINE, Blocks.PRISMARINE_BRICKS, Blocks.DARK_PRISMARINE, Blocks.DARK_PRISMARINE_SLAB, Blocks.PRISMARINE_SLAB, Blocks.PRISMARINE_BRICK_SLAB
                        , Blocks.BRICK_SLAB, Blocks.STONE_BRICK_SLAB, Blocks.STONE_SLAB, Blocks.COBBLESTONE_SLAB
                        , Blocks.DEEPSLATE_BRICK_SLAB, Blocks.DEEPSLATE_BRICKS, Blocks.CRACKED_DEEPSLATE_BRICKS
                        , Blocks.DEEPSLATE_TILE_SLAB, Blocks.DEEPSLATE_TILES, Blocks.CRACKED_DEEPSLATE_TILES
                        , Blocks.POLISHED_DEEPSLATE_SLAB, Blocks.COBBLED_DEEPSLATE_SLAB, Blocks.CHISELED_DEEPSLATE, Blocks.REINFORCED_DEEPSLATE
                        , Blocks.SMOOTH_STONE, Blocks.SMOOTH_STONE_SLAB, Blocks.MOSSY_STONE_BRICK_SLAB, Blocks.MOSSY_COBBLESTONE_SLAB
                        , Blocks.SMOOTH_RED_SANDSTONE_SLAB, Blocks.SMOOTH_SANDSTONE_SLAB, Blocks.SANDSTONE_SLAB, Blocks.RED_SANDSTONE_SLAB, Blocks.CUT_RED_SANDSTONE_SLAB, Blocks.CUT_SANDSTONE_SLAB
                        , Blocks.NETHER_BRICK_SLAB, Blocks.NETHER_BRICKS, Blocks.CHISELED_NETHER_BRICKS, Blocks.CRACKED_NETHER_BRICKS, Blocks.RED_NETHER_BRICK_SLAB, Blocks.RED_NETHER_BRICKS
                        , Blocks.POLISHED_BLACKSTONE_BRICKS, Blocks.POLISHED_BLACKSTONE_BRICK_SLAB, Blocks.CRACKED_POLISHED_BLACKSTONE_BRICKS
                        , Blocks.BLACKSTONE, Blocks.BLACKSTONE_SLAB, Blocks.POLISHED_BLACKSTONE, Blocks.POLISHED_BLACKSTONE_SLAB, Blocks.CHISELED_POLISHED_BLACKSTONE, Blocks.GILDED_BLACKSTONE
                        , Blocks.QUARTZ_BLOCK, Blocks.QUARTZ_SLAB, Blocks.SMOOTH_QUARTZ_SLAB, Blocks.SMOOTH_QUARTZ, Blocks.QUARTZ_BRICKS, Blocks.QUARTZ_PILLAR, Blocks.CHISELED_QUARTZ_BLOCK
                        , Blocks.MUD_BRICKS, Blocks.MUD_BRICK_SLAB
                        , Blocks.PURPUR_SLAB, Blocks.PURPUR_BLOCK, Blocks.PURPUR_PILLAR
                        , Blocks.END_STONE, Blocks.END_STONE_BRICKS, Blocks.END_STONE_BRICK_SLAB);
        this.tag(PVZBlockTags.WISDOM_TREE_REPLACEABLE).addTags(BlockTags.MOSS_REPLACEABLE, BlockTags.REPLACEABLE_PLANTS, BlockTags.LEAVES);
        this.tag(PVZBlockTags.SCULK).add(Blocks.SCULK, Blocks.SCULK_CATALYST, Blocks.SCULK_VEIN, Blocks.SCULK_SENSOR, Blocks.SCULK_SHRIEKER);
        this.tag(BlockTags.DIRT).add(PVZBlocks.ORIGIN_ORE.get());
        this.tag(PVZBlockTags.SNAIL_SPAWNABLE_ON).addTags(BlockTags.ANIMALS_SPAWNABLE_ON).add(Blocks.MOSS_BLOCK);
        this.tag(PVZBlockTags.FUNGICICOLIDAE_SPAWNABLE_ON).add(Blocks.MYCELIUM);
        this.tag(PVZBlockTags.PLANT_PERMANENT_ON).add(PVZBlocks.GARDEN_FLOWER_POT.get());
    }

    public static final List<TagKey<Block>> LOGS = new ArrayList<>();

    private static TagKey<Block> tag(String path) {
        return TagKey.create(Registry.BLOCK_REGISTRY, new ResourceLocation(PVZMod.MODID + ":" + path));
    }
}
