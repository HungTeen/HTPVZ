package com.hungteen.pvz.common.world.zen_garden;

import com.google.common.collect.ImmutableList;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.Util;
import net.minecraft.core.Holder;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.WeightedPlacedFeature;
import net.minecraft.world.level.levelgen.feature.configurations.RandomFeatureConfiguration;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaJungleFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.MegaJungleTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;
import net.minecraft.world.level.levelgen.placement.PlacedFeature;

import java.util.List;

import static com.hungteen.pvz.common.register.PVZBlocks.NUT;
import static com.hungteen.pvz.common.register.PVZBlocks.WoodSet.Leaves;
import static com.hungteen.pvz.common.register.PVZBlocks.WoodSet.Log;
import static net.minecraft.data.worldgen.placement.VegetationPlacements.treePlacement;

public class NutTreeGrower extends AbstractMegaTreeGrower {
    public static Holder<ConfiguredFeature<TreeConfiguration, ?>> tree = null;
    public static Holder<ConfiguredFeature<TreeConfiguration, ?>> megaTree = null;
    public static Holder<PlacedFeature> MEGA_NUT_TREE_CHECKED = null;
    public static Holder<ConfiguredFeature<?, ?>> TREES_NUT_CF = null;
    public static Holder<PlacedFeature> TREES_NUT_PF = null;

    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource p_222929_, boolean p_222930_) {
        return tree;
    }

    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource p_222927_) {
        return megaTree;
    }

    public static void init(){
        tree = FeatureUtils.register("pvz:nut_tree", Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(NUT.get(Log).get()),
                new StraightTrunkPlacer(10, 2, 0),
                new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                        .add(NUT.get(Leaves).get().defaultBlockState(), 8)
                        .add(PVZBlocks.NUT_LEAVES_WITH_NUTS.get().defaultBlockState(), 1)),
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 2),
                new TwoLayersFeatureSize(1, 0, 1))
                .build()
        );
        megaTree = FeatureUtils.register("pvz:mega_nut_tree", Feature.TREE, (new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(NUT.get(Log).get()),
                new MegaJungleTrunkPlacer(15, 10, 10),
                new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                        .add(NUT.get(Leaves).get().defaultBlockState(), 8)
                        .add(PVZBlocks.NUT_LEAVES_WITH_NUTS.get().defaultBlockState(), 1)),
                new MegaJungleFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 2),
                new TwoLayersFeatureSize(1, 1, 2)))
                .decorators(ImmutableList.of(GlowBerryDecorator.INSTANCE))
                .build()
        );
        MEGA_NUT_TREE_CHECKED = BuiltinRegistries.register(BuiltinRegistries.PLACED_FEATURE, Util.prefix("mega_nut_tree_checked"),
                new PlacedFeature(Holder.hackyErase(NutTreeGrower.megaTree), List.of(PlacementUtils.filteredByBlockSurvival(PVZBlocks.NUT.get(PVZBlocks.WoodSet.Sapling).get()))));
        TREES_NUT_CF = BuiltinRegistries.register(BuiltinRegistries.CONFIGURED_FEATURE, Util.prefix("trees_nut"),
                new ConfiguredFeature<>(Feature.RANDOM_SELECTOR, new RandomFeatureConfiguration(List.of(new WeightedPlacedFeature(MEGA_NUT_TREE_CHECKED, 0.01F)), MEGA_NUT_TREE_CHECKED)));
        TREES_NUT_PF = BuiltinRegistries.register(BuiltinRegistries.PLACED_FEATURE, Util.prefix("trees_nut"),
                new PlacedFeature(Holder.hackyErase(TREES_NUT_CF), List.copyOf(treePlacement(PlacementUtils.countExtra(1, 0.1F, 1)))));
    }
}
