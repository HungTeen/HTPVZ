package com.hungteen.pvz.common.world.zen_garden;

import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZFeatures;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.FeaturePlaceContext;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.OreConfiguration;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;

public class LunarStoneFeature extends Feature<NoneFeatureConfiguration> {
    public static Holder<ConfiguredFeature<NoneFeatureConfiguration, ?>> LUNAR_STONE_CF;
    public static Holder<PlacedFeature> LUNAR_STONE_PF;
    public static Holder<ConfiguredFeature<OreConfiguration, ?>> ORE_LUNAR_STONE_CF;
    public static Holder<PlacedFeature> ORE_LUNAR_STONE_PF;

    public LunarStoneFeature(Codec<NoneFeatureConfiguration> codec) {
        super(codec);
    }

    public boolean place(FeaturePlaceContext<NoneFeatureConfiguration> context) {
        BlockPos blockPos = context.origin();
        RandomSource randomsource = context.random();

        WorldGenLevel level = context.level();
        while (level.isEmptyBlock(blockPos)) {
            blockPos = blockPos.below();
            if (blockPos.getY() < level.getMinBuildHeight() + 2 || !level.isEmptyBlock(blockPos.above())) {
                return false;
            }
        }
        Block oriBlock = level.getBlockState(blockPos).getBlock();
        if (oriBlock != Blocks.GRASS_BLOCK && oriBlock != Blocks.DIRT && oriBlock != Blocks.MOSS_BLOCK) {
            return false;
        }

        int height = 4 + randomsource.nextInt(2);
        int width = 1 + (randomsource.nextBoolean() ? 0 : 1);
        float offset = randomsource.nextFloat() * 2 - 1;

        for (int y = -2; y < 6; y ++) {
            for (int x = -2; x <= 2; x ++) {
                for (int z = -2; z <= 2; z ++) {
                    if ((x - offset) * (x - offset) + (z - offset) * (z - offset) - width * width + y * y / height/ height - 1 < 0) {
                        this.setBlock(level, blockPos.offset(x, y, z), PVZBlocks.LUNAR_STONE.get().defaultBlockState());
                    }
                }
            }
        }


        return true;
    }

    public static void init() {
        LUNAR_STONE_CF = FeatureUtils.register("pvz:lunar_stone", PVZFeatures.LUNAR_STONE.get());
        LUNAR_STONE_PF = PlacementUtils.register("pvz:lunar_stone", LUNAR_STONE_CF, RarityFilter.onAverageOnceEvery(20),
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
        ORE_LUNAR_STONE_CF = FeatureUtils.register("pvz:ore_lunar_stone", Feature.ORE,
                new OreConfiguration(List.of(
                        OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, PVZBlocks.LUNAR_STONE.get().defaultBlockState()),
                        OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, PVZBlocks.LUNAR_STONE.get().defaultBlockState())),
                        18));
        ORE_LUNAR_STONE_PF = PlacementUtils.register("pvz:ore_lunar_stone", ORE_LUNAR_STONE_CF,
                List.of(CountPlacement.of(5),
                        InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top()),
                        BiomeFilter.biome()));
    }
}