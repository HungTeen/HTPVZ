package com.hungteen.pvz.common.world.zen_garden;

import com.google.common.collect.Lists;
import com.hungteen.pvz.common.register.OtherRegisters;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.LevelSimulatedReader;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.feature.configurations.TreeConfiguration;
import net.minecraft.world.level.levelgen.feature.foliageplacers.FoliagePlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.GiantTrunkPlacer;
import net.minecraft.world.level.levelgen.feature.trunkplacers.TrunkPlacerType;

import java.util.List;
import java.util.function.BiConsumer;

public class MegaNutTrunkPlacer extends GiantTrunkPlacer {
   public static final Codec<MegaNutTrunkPlacer> CODEC = RecordCodecBuilder.create((p_70206_) ->
           trunkPlacerParts(p_70206_).apply(p_70206_, MegaNutTrunkPlacer::new));

   public MegaNutTrunkPlacer(int p_70193_, int p_70194_, int p_70195_) {
      super(p_70193_, p_70194_, p_70195_);
   }

   protected TrunkPlacerType<?> type() {
      return OtherRegisters.NUT_TREE_TRUNK_PLACER.get();
   }

   public List<FoliagePlacer.FoliageAttachment> placeTrunk(LevelSimulatedReader p_226140_, BiConsumer<BlockPos, BlockState> p_226141_, RandomSource random, int maxY, BlockPos p_226144_, TreeConfiguration p_226145_) {
      List<FoliagePlacer.FoliageAttachment> list = Lists.newArrayList();
      list.addAll(super.placeTrunk(p_226140_, p_226141_, random, maxY, p_226144_, p_226145_));
      list.add(addBranch(p_226140_, p_226141_, random, maxY, p_226144_, p_226145_, 1 + random.nextInt(2), 0));
      list.add(addBranch(p_226140_, p_226141_, random, maxY, p_226144_, p_226145_, 1 + random.nextInt(2), (float) Math.PI));
      list.add(addBranch(p_226140_, p_226141_, random, maxY, p_226144_, p_226145_, 1 + random.nextInt(2), (float) Math.PI * 1.5F));
      list.add(addBranch(p_226140_, p_226141_, random, maxY, p_226144_, p_226145_, 1 + random.nextInt(2), (float) Math.PI * .5F));
      for (int i = 3; i < Math.min(12, maxY / 2); i += Math.max(2, i / 2)) {
         list.add(addBranch(p_226140_, p_226141_, random, maxY, p_226144_, p_226145_, i, random.nextFloat() * ((float) Math.PI * 2F)));
      }

      return list;
   }

   private FoliagePlacer.FoliageAttachment addBranch(LevelSimulatedReader p_226140_, BiConsumer<BlockPos, BlockState> p_226141_, RandomSource random, int maxY, BlockPos p_226144_, TreeConfiguration p_226145_, int distToTop, float angle) {
      int x = 0;
      int z = 0;
      int len = (12 - distToTop) / 2 + random.nextInt(1 + (12 - distToTop) / 3);
      double sin = Math.sin(angle);
      double cos = Math.cos(angle);
      boolean towX = sin * sin > cos * cos;

      for(int l = 0; l < len; ++ l) {
         x = (int)(1.5F + (towX ? (cos > 0 ? l : -l) : 0));
         z = (int)(1.5F + (towX ? 0 : (sin > 0 ? l : -l)));
         for (int y = l == len - 1 ? 1 : 0; y <= (l == len - 1 ? 2 : 0); y ++) {
            BlockPos blockpos = p_226144_.offset(x, maxY - distToTop - 3 + y, z);
            this.placeLog(p_226140_, p_226141_, random, blockpos, p_226145_);
         }
      }

      return new FoliagePlacer.FoliageAttachment(p_226144_.offset(x, maxY - distToTop, z), - 1 - distToTop / 5, false);
   }
}