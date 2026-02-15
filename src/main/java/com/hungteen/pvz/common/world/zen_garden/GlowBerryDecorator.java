package com.hungteen.pvz.common.world.zen_garden;

import com.hungteen.pvz.common.register.OtherRegisters;
import com.mojang.serialization.Codec;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecorator;
import net.minecraft.world.level.levelgen.feature.treedecorators.TreeDecoratorType;

import static net.minecraft.world.level.block.CaveVines.BERRIES;

public class GlowBerryDecorator extends TreeDecorator {
    public static final Codec<GlowBerryDecorator> CODEC = Codec.unit(() -> GlowBerryDecorator.INSTANCE);
    public static final GlowBerryDecorator INSTANCE = new GlowBerryDecorator();

    protected TreeDecoratorType<?> type() {
        return OtherRegisters.GLOW_BERRY_DECORATOR.get();
    }

    public void place(TreeDecorator.Context context) {
        RandomSource randomsource = context.random();
        context.logs().forEach((p_226075_) -> {
            if (randomsource.nextInt(3) <= 0) {
                BlockPos blockPos = p_226075_.below();
                if (context.isAir(blockPos)) {
                    if (randomsource.nextInt(3) <= 1){
                        context.setBlock(blockPos, Blocks.CAVE_VINES.defaultBlockState().setValue(BERRIES, Boolean.valueOf(true)));
                    } else {
                        context.setBlock(blockPos, Blocks.CAVE_VINES.defaultBlockState());
                    }
                }
            }

        });
    }
}
