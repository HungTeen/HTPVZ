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
 *
 * @program: HTPVZ2
 * @author: HungTeen
 * @create: 2026-10-01
 */
public class CopperLadderBlock extends LadderBlock {

    /**
     * How far the placement searches around the clicked ladder.
     */
    public static final int MAX_SEARCH_DISTANCE = 8;

    public CopperLadderBlock(Properties properties) {
        super(properties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        // 当前位置靠谱
        if(isStableLadderPosition(level, pos)) {
            return true;
        }
        // 上下搜索是否有靠谱的位置
        BlockPos.MutableBlockPos upPos = pos.mutable();
        BlockPos.MutableBlockPos downPos = pos.mutable();
        for (int i = 1; i <= MAX_SEARCH_DISTANCE; ++i) {
            if(isStableLadderPosition(level, upPos.move(Direction.UP))
                || isStableLadderPosition(level, downPos.move(Direction.DOWN))) {
                return true;
            }
        }
        return false;
    }

    private boolean isStableLadderPosition(LevelReader level, BlockPos pos) {
        // 要么靠墙，要么靠在地上
        return hasWallSupport(level.getBlockState(pos), level, pos) || this.hasGroundSupport(level, pos);
    }

    /**
     * Holding a ladder lets it be replaced by the stack placement, same as scaffolding.
     */
    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return context.getItemInHand().is(this.asItem());
    }

    /**
     * Attached to the side of a sturdy block face, just like a vanilla ladder.
     */
    private boolean hasWallSupport(BlockState state, LevelReader level, BlockPos pos) {
        if (!state.is(this)) {
            return false;
        }
        Direction direction = state.getValue(FACING);
        BlockPos wallPos = pos.relative(direction.getOpposite());
        return level.getBlockState(wallPos).isFaceSturdy(level, wallPos, direction);
    }

    /**
     * Floating in the air: supported by a copper ladder or the ground below.
     */
    private boolean hasGroundSupport(LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        return below.isFaceSturdy(level, belowPos, Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!this.canSurvive(state, level, pos)) {
            // no support anymore, so drop itself and break.
            // 掉落由 Block.updateOrDestroy -> Level.destroyBlock 负责，不能手动再 drop 一次
            return Blocks.AIR.defaultBlockState();
        }
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return state;
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
        // attached to a wall first, same as the vanilla ladder.
        for (Direction direction : directions) {
            BlockState state = this.defaultBlockState().setValue(FACING, direction.getOpposite());
            if (this.hasWallSupport(state, level, pos)) {
                return state.setValue(WATERLOGGED, waterlogged);
            }
        }
        // hanging in the air above another copper ladder or the ground.
        if (!directions.isEmpty() && this.canSurvive(this.defaultBlockState(), level, pos)) {
            BlockState below = level.getBlockState(pos.below());
            BlockState above = level.getBlockState(pos.above());
            //keep the column straight when stacking ladders.
            Direction facing;
            if (below.is(this)) {
                facing = below.getValue(FACING);
            } else if (above.is(this)) {
                facing = above.getValue(FACING);
            } else {
                facing = directions.get(0).getOpposite();
            }
            return this.defaultBlockState().setValue(FACING, facing)
                    .setValue(WATERLOGGED, waterlogged);
        }
        return null;
    }
}
