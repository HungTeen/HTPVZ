package com.hungteen.pvz.world;

import com.google.common.collect.ImmutableList;
import com.hungteen.pvz.common.register.PVZBlocks;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.data.worldgen.features.FeatureUtils;
import net.minecraft.util.RandomSource;
import net.minecraft.util.random.SimpleWeightedRandomList;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.level.block.grower.AbstractMegaTreeGrower;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.ConfiguredFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.featuresize.TwoLayersFeatureSize;
import net.minecraft.world.level.levelgen.feature.foliageplacers.BlobFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.foliageplacers.MegaJungleFoliagePlacer;
import net.minecraft.world.level.levelgen.feature.stateproviders.BlockStateProvider;
import net.minecraft.world.level.levelgen.feature.stateproviders.WeightedStateProvider;
import net.minecraft.world.level.levelgen.feature.trunkplacers.MegaJungleTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.StraightTrunkPlacer;

import static com.hungteen.pvz.common.register.PVZBlocks.NUT;
import static com.hungteen.pvz.common.register.PVZBlocks.WoodSet.Leaves;
import static com.hungteen.pvz.common.register.PVZBlocks.WoodSet.Log;

public class NutTreeGrower extends AbstractMegaTreeGrower {
    public static Holder<ConfiguredFeature<TreeConfiguration, ?>> tree = null;
    public static Holder<ConfiguredFeature<TreeConfiguration, ?>> megaTree = null;
    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredFeature(RandomSource p_222929_, boolean p_222930_) {
        if (tree == null){
            init();
        }
        return tree;
    }

    protected Holder<? extends ConfiguredFeature<?, ?>> getConfiguredMegaFeature(RandomSource p_222927_) {
        if (megaTree == null){
            init();
        }
        return megaTree;
    }

    private static void init(){
        tree = FeatureUtils.register("nut_tree", Feature.TREE, new TreeConfiguration.TreeConfigurationBuilder(
                BlockStateProvider.simple(NUT.get(Log).get()),
                new StraightTrunkPlacer(10, 2, 0),
                new WeightedStateProvider(SimpleWeightedRandomList.<BlockState>builder()
                        .add(NUT.get(Leaves).get().defaultBlockState(), 8)
                        .add(PVZBlocks.NUT_LEAVES_WITH_NUTS.get().defaultBlockState(), 1)),
                new BlobFoliagePlacer(ConstantInt.of(2), ConstantInt.of(0), 2),
                new TwoLayersFeatureSize(1, 0, 1))
                .build()
        );
        megaTree = FeatureUtils.register("mega_nut_tree", Feature.TREE, (new TreeConfiguration.TreeConfigurationBuilder(
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
    }
}
