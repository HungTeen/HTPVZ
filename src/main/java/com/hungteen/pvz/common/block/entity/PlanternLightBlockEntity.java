package com.hungteen.pvz.common.block.entity;

import com.hungteen.pvz.common.block.PlanternLightBlock;
import com.hungteen.pvz.common.register.PVZBlockEntities;
import com.hungteen.pvz.common.register.PVZBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;


public class PlanternLightBlockEntity extends BlockEntity {
    public PlanternLightBlockEntity(BlockPos pos, BlockState blockState) {
        super(PVZBlockEntities.PLANTERN_LIGHT.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState blockState, PlanternLightBlockEntity blockEntity) {
        if (! level.isClientSide() && blockState.is(PVZBlocks.PLANTERN_LIGHT.get())) {
            if (! blockState.getValue(PlanternLightBlock.HAS_SOURCE)) {
                level.setBlock(pos, (blockState.getValue(PlanternLightBlock.WATERLOGGED) ? Blocks.WATER : Blocks.AIR).defaultBlockState(), 2);
            } else {
                level.setBlock(pos, blockState.setValue(PlanternLightBlock.HAS_SOURCE, false), 3);
            }
        }
    }

}
