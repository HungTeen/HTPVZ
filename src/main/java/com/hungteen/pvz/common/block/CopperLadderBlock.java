package com.hungteen.pvz.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;

/**
 * A ladder which combines the traits of ladders and vines.
 * It can be attached to the side of a block like a ladder, and it can also
 * hang in the air as long as there is another copper ladder or a solid
 * ground below it. Whenever it loses its support, it will drop itself.
 */
public class CopperLadderBlock extends LadderBlock {

    public CopperLadderBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        return this.hasWallSupport(state, level, pos) || this.hasGroundSupport(level, pos);
    }

    /**Attached to the side of a sturdy block face, just like a vanilla ladder.*/
    private boolean hasWallSupport(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = state.getValue(FACING);
        BlockPos wallPos = pos.relative(direction.getOpposite());
        return level.getBlockState(wallPos).isFaceSturdy(level, wallPos, direction);
    }

    /**Floating in the air: supported by a copper ladder or the ground below.*/
    private boolean hasGroundSupport(LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        return below.is(this) || below.isFaceSturdy(level, belowPos, Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!this.canSurvive(state, level, pos)) {
            //no support anymore, so drop itself and break.
            if (!level.isClientSide()) {
                dropResources(state, level, pos, null);
            }
            return Blocks.AIR.defaultBlockState();
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (!context.replacingClickedOnBlock()) {
            BlockState behind = level.getBlockState(pos.relative(context.getClickedFace().getOpposite()));
            if (behind.is(this) && behind.getValue(FACING) == context.getClickedFace()) {
                return null;
            }
        }
        FluidState fluidState = level.getFluidState(pos);
        boolean waterlogged = fluidState.getType() == Fluids.WATER;
        List<Direction> directions = Arrays.stream(context.getNearestLookingDirections())
                .filter(direction -> direction.getAxis().isHorizontal()).toList();
        //attached to a wall first, same as the vanilla ladder.
        for (Direction direction : directions) {
            BlockState state = this.defaultBlockState().setValue(FACING, direction.getOpposite());
            if (this.hasWallSupport(state, level, pos)) {
                return state.setValue(WATERLOGGED, waterlogged);
            }
        }
        //hanging in the air above another copper ladder or the ground.
        if (!directions.isEmpty() && this.hasGroundSupport(level, pos)) {
            return this.defaultBlockState().setValue(FACING, directions.get(0).getOpposite())
                    .setValue(WATERLOGGED, waterlogged);
        }
        return null;
    }
}
