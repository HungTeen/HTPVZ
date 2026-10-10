package com.hungteen.pvz.common.block;

import com.google.common.base.Suppliers;
import com.google.common.collect.BiMap;
import com.google.common.collect.ImmutableBiMap;
import com.hungteen.pvz.common.item.CopperLadderBlockItem;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraftforge.common.ToolAction;
import net.minecraftforge.common.ToolActions;

import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * A ladder which combines the traits of ladders and vines.
 * It can be attached to the side of a block like a ladder, and it can also
 * hang in the air while connected to a stable copper ladder.
 *
 * @program: HTPVZ2
 * @author: HungTeen
 * @create: 2026-10-01
 */
public class CopperLadderBlock extends LadderBlock {

    /**
     * Maximum connected distance from a wall or ground anchor.
     */
    public static final int MAX_SEARCH_DISTANCE = 8;

    private static final Supplier<BiMap<Block, Block>> NEXT_BY_BLOCK = Suppliers.memoize(() -> ImmutableBiMap.<Block, Block>builder()
            .put(PVZBlocks.COPPER_LADDER.get(), PVZBlocks.EXPOSED_COPPER_LADDER.get())
            .put(PVZBlocks.EXPOSED_COPPER_LADDER.get(), PVZBlocks.WEATHERED_COPPER_LADDER.get())
            .put(PVZBlocks.WEATHERED_COPPER_LADDER.get(), PVZBlocks.OXIDIZED_COPPER_LADDER.get())
            .build());
    private static final Supplier<BiMap<Block, Block>> PREVIOUS_BY_BLOCK = Suppliers.memoize(() -> NEXT_BY_BLOCK.get().inverse());
    private static final Supplier<BiMap<Block, Block>> WAXABLES = Suppliers.memoize(() -> ImmutableBiMap.<Block, Block>builder()
            .put(PVZBlocks.COPPER_LADDER.get(), PVZBlocks.WAXED_COPPER_LADDER.get())
            .put(PVZBlocks.EXPOSED_COPPER_LADDER.get(), PVZBlocks.WAXED_EXPOSED_COPPER_LADDER.get())
            .put(PVZBlocks.WEATHERED_COPPER_LADDER.get(), PVZBlocks.WAXED_WEATHERED_COPPER_LADDER.get())
            .put(PVZBlocks.OXIDIZED_COPPER_LADDER.get(), PVZBlocks.WAXED_OXIDIZED_COPPER_LADDER.get())
            .build());
    private static final Supplier<BiMap<Block, Block>> WAX_OFF_BY_BLOCK = Suppliers.memoize(() -> WAXABLES.get().inverse());

    public CopperLadderBlock(Properties properties) {
        super(properties);
    }

    public static Optional<BlockState> getNextState(BlockState state) {
        return Optional.ofNullable(NEXT_BY_BLOCK.get().get(state.getBlock())).map(block -> block.withPropertiesOf(state));
    }

    public static Optional<BlockState> getPreviousState(BlockState state) {
        return Optional.ofNullable(PREVIOUS_BY_BLOCK.get().get(state.getBlock())).map(block -> block.withPropertiesOf(state));
    }

    public static Optional<BlockState> getWaxedState(BlockState state) {
        return Optional.ofNullable(WAXABLES.get().get(state.getBlock())).map(block -> block.withPropertiesOf(state));
    }

    public static Optional<BlockState> getWaxOffState(BlockState state) {
        return Optional.ofNullable(WAX_OFF_BY_BLOCK.get().get(state.getBlock())).map(block -> block.withPropertiesOf(state));
    }

    public static boolean isCopperLadder(BlockState state) {
        return state.is(PVZBlockTags.COPPER_LADDERS);
    }

    /**
     * Not every copper ladder in the tag is guaranteed to have the FACING property.
     */
    @Nullable
    public static Direction getCopperLadderFacing(BlockState state) {
        return isCopperLadder(state) && state.hasProperty(FACING) ? state.getValue(FACING) : null;
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader level, BlockPos pos) {
        if (this.isStableLadderPosition(state, level, pos)) {
            return true;
        }
        return this.hasConnectedSupport(level, pos, Direction.UP)
                || this.hasConnectedSupport(level, pos, Direction.DOWN);
    }

    private boolean hasConnectedSupport(LevelReader level, BlockPos pos, Direction direction) {
        BlockPos.MutableBlockPos checkPos = pos.mutable();
        for (int i = 1; i <= MAX_SEARCH_DISTANCE; ++i) {
            checkPos.move(direction);
            BlockState state = level.getBlockState(checkPos);
            if (! isCopperLadder(state)) {
                return false;
            }
            if (this.isStableLadderPosition(state, level, checkPos)) {
                return true;
            }
        }
        return false;
    }

    private boolean isStableLadderPosition(BlockState state, LevelReader level, BlockPos pos) {
        return this.hasWallSupport(state, level, pos) || this.hasGroundSupport(level, pos);
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return context.getItemInHand().getItem() instanceof CopperLadderBlockItem;
    }

    private boolean hasWallSupport(BlockState state, LevelReader level, BlockPos pos) {
        Direction direction = getCopperLadderFacing(state);
        if (direction == null) {
            return false;
        }
        BlockPos wallPos = pos.relative(direction.getOpposite());
        return level.getBlockState(wallPos).isFaceSturdy(level, wallPos, direction);
    }

    private boolean hasGroundSupport(LevelReader level, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState below = level.getBlockState(belowPos);
        return below.isFaceSturdy(level, belowPos, Direction.UP);
    }

    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (! this.canSurvive(state, level, pos)) {
            return Blocks.AIR.defaultBlockState();
        }
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        return state;
    }

    @Nullable
    @Override
    public BlockState getToolModifiedState(BlockState state, UseOnContext context, ToolAction toolAction, boolean simulate) {
        if (! context.getItemInHand().canPerformAction(toolAction)) {
            return null;
        }
        if (toolAction == ToolActions.AXE_SCRAPE) {
            return getPreviousState(state).orElse(null);
        }
        if (toolAction == ToolActions.AXE_WAX_OFF) {
            return getWaxOffState(state).orElse(null);
        }
        return null;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        LevelReader level = context.getLevel();
        BlockPos pos = context.getClickedPos();
        if (! context.replacingClickedOnBlock()) {
            BlockState behind = level.getBlockState(pos.relative(context.getClickedFace().getOpposite()));
            if (getCopperLadderFacing(behind) == context.getClickedFace()) {
                return null;
            }
        }
        FluidState fluidState = level.getFluidState(pos);
        boolean waterlogged = fluidState.getType() == Fluids.WATER;
        List<Direction> directions = Arrays.stream(context.getNearestLookingDirections())
                .filter(direction -> direction.getAxis().isHorizontal()).toList();
        for (Direction direction : directions) {
            BlockState state = this.defaultBlockState().setValue(FACING, direction.getOpposite());
            if (this.hasWallSupport(state, level, pos)) {
                return state.setValue(WATERLOGGED, waterlogged);
            }
        }
        if (! directions.isEmpty() && this.canSurvive(this.defaultBlockState(), level, pos)) {
            Direction belowFacing = getCopperLadderFacing(level.getBlockState(pos.below()));
            Direction aboveFacing = getCopperLadderFacing(level.getBlockState(pos.above()));
            Direction facing = belowFacing != null ? belowFacing
                    : aboveFacing != null ? aboveFacing
                    : directions.get(0).getOpposite();
            return this.defaultBlockState().setValue(FACING, facing)
                    .setValue(WATERLOGGED, waterlogged);
        }
        return null;
    }
}
