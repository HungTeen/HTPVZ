package com.hungteen.pvz.common.world;

import com.hungteen.pvz.common.register.PVZBlocks;
import net.minecraft.core.Holder;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.features.OreFeatures;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.InclusiveRange;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.util.valueproviders.UniformInt;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.*;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.DualNoiseProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.placement.*;
import net.minecraft.world.level.levelgen.synth.NormalNoise;

import java.util.List;

public class OverWorldFeatures {
    public static Holder<ConfiguredFeature<OreConfiguration, ?>> ORE_LUNAR_STONE_CF;
    public static Holder<PlacedFeature> ORE_LUNAR_STONE_PF;
    public static Holder<ConfiguredFeature<SimpleBlockConfiguration, ?>> ORIGIN_VEGETATION;
    public static Holder<ConfiguredFeature<VegetationPatchConfiguration, ?>> ORE_ORIGIN_CF;
    public static Holder<PlacedFeature> ORE_ORIGIN_PF;
    public static Holder<ConfiguredFeature<RandomPatchConfiguration, ?>> PLANTERN_CF;
    public static Holder<PlacedFeature> PLANTERN_PF;

    public static void init() {
        ORE_LUNAR_STONE_CF = FeatureUtils.register("pvz:ore_lunar_stone", Feature.ORE,
                new OreConfiguration(List.of(
                        OreConfiguration.target(OreFeatures.STONE_ORE_REPLACEABLES, PVZBlocks.LUNAR_STONE.get().defaultBlockState()),
                        OreConfiguration.target(OreFeatures.DEEPSLATE_ORE_REPLACEABLES, PVZBlocks.LUNAR_STONE.get().defaultBlockState())),
                        18));
        ORE_LUNAR_STONE_PF = PlacementUtils.register("pvz:ore_lunar_stone", ORE_LUNAR_STONE_CF,
                List.of(CountPlacement.of(3),
                        InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top()),
                        BiomeFilter.biome()));
        ORIGIN_VEGETATION = FeatureUtils.register("pvz:origin_vegetation", Feature.SIMPLE_BLOCK,
                new SimpleBlockConfiguration(new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                        .add(Blocks.GRASS.defaultBlockState(), 50)
                        .add(Blocks.TALL_GRASS.defaultBlockState(), 10))));
        ORE_ORIGIN_CF = FeatureUtils.register("pvz:ore_origin", Feature.VEGETATION_PATCH,
                new VegetationPatchConfiguration(BlockTags.MOSS_REPLACEABLE, BlockStateProvider.simple(PVZBlocks.ORIGIN_ORE.get()),
                        PlacementUtils.inlinePlaced(ORIGIN_VEGETATION), CaveSurface.FLOOR,
                        ConstantInt.of(1), 0.0F, 2, 0.8F, UniformInt.of(1, 2), 0F));
        ORE_ORIGIN_PF = PlacementUtils.register("pvz:ore_origin", ORE_ORIGIN_CF,
                List.of(CountPlacement.of(1),
                        InSquarePlacement.spread(), HeightRangePlacement.uniform(VerticalAnchor.bottom(), VerticalAnchor.top()),
                        BiomeFilter.biome()));
        PLANTERN_CF = FeatureUtils.register("pvz:plantern", Feature.FLOWER,
                new RandomPatchConfiguration(1, 1, 1, PlacementUtils.onlyWhenEmpty(Feature.SIMPLE_BLOCK,
                        new SimpleBlockConfiguration(new DualNoiseProvider(new InclusiveRange<>(1, 3),
                                new NormalNoise.NoiseParameters(-10, 1.0D), 0.5F, 2345L,
                                new NormalNoise.NoiseParameters(-3, 1.0D), 0.5F,
                                List.of(PVZBlocks.PLANTERN.get().defaultBlockState()))))));
        PLANTERN_PF = PlacementUtils.register("pvz:plantern", PLANTERN_CF,
                InSquarePlacement.spread(), PlacementUtils.HEIGHTMAP, BiomeFilter.biome());
    }
}