package com.hungteen.pvz.common.block;

import com.hungteen.pvz.common.register.PVZBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.LeavesBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

public class PattraLeavesBlock extends LeavesBlock {
    public PattraLeavesBlock(Properties p_54422_) {
        super(p_54422_);
    }

    public boolean isRandomlyTicking(BlockState p_54449_) {
        return !p_54449_.getValue(PERSISTENT);
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlockAndUpdate(pos, updateDistance(blockState, level, pos));
        if (this.decaying(blockState)) {
            dropResources(blockState, level, pos);
            level.removeBlock(pos, false);
        } else if (blockState.getValue(DISTANCE) < 3) {
            Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
            if (level.getBlockState(pos.relative(direction)).isAir()) {
                level.setBlockAndUpdate(pos.relative(direction), PVZBlocks.PATTRA_LEAVES.get().defaultBlockState()
                        .setValue(DISTANCE, blockState.getValue(DISTANCE) + 1));
            }
        }
    }
    @Override
    protected boolean decaying(BlockState blockState) {
        return !blockState.getValue(PERSISTENT) && blockState.getValue(DISTANCE) >= 4;
    }

    //about distance
    public static int getDistanceAt(BlockState blockState) {
        if (blockState.is(PVZBlocks.WISDOM_TREE_LOG.get()) && blockState.getValue(WisdomTreeLogBlock.DISTANCE) == 0) {
            return -1;
        } else if (! blockState.is(PVZBlocks.PATTRA_LEAVES.get())) {
            return 4;
        } else {
            return blockState.is(PVZBlocks.PATTRA_LEAVES.get()) ? blockState.getValue(DISTANCE) : 4;
        }
    }
    private static BlockState updateDistance(BlockState blockState, LevelAccessor level, BlockPos pos) {
        int i = 7;
        BlockPos.MutableBlockPos mutableblockpos = new BlockPos.MutableBlockPos();
        for(Direction direction : Direction.values()) {
            mutableblockpos.setWithOffset(pos, direction);
            int j = getDistanceAt(level.getBlockState(mutableblockpos));
            i = Math.min(i, j == -1 ? blockState.getValue(DISTANCE) : j + 1);
            if (i == 1) {
                break;
            }
        }
        return blockState.setValue(DISTANCE, i);
    }

    @Override
    public BlockState updateShape(BlockState p_54440_, Direction p_54441_, BlockState p_54442_, LevelAccessor p_54443_, BlockPos p_54444_, BlockPos p_54445_) {
        if (p_54440_.getValue(WATERLOGGED)) {
            p_54443_.scheduleTick(p_54444_, Fluids.WATER, Fluids.WATER.getTickDelay(p_54443_));
        }
        int i = getDistanceAt(p_54442_) + 1;
        if (i != 1 || p_54440_.getValue(DISTANCE) != i) {
            p_54443_.scheduleTick(p_54444_, this, 1);
        }
        return p_54440_;
    }
    @Override
    public void tick(BlockState p_221369_, ServerLevel p_221370_, BlockPos p_221371_, RandomSource p_221372_) {
        //no update.
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_54424_) {
        FluidState fluidstate = p_54424_.getLevel().getFluidState(p_54424_.getClickedPos());
        BlockState blockstate = this.defaultBlockState().setValue(PERSISTENT, Boolean.valueOf(true)).setValue(WATERLOGGED, Boolean.valueOf(fluidstate.getType() == Fluids.WATER));
        return updateDistance(blockstate, p_54424_.getLevel(), p_54424_.getClickedPos());
    }
}
