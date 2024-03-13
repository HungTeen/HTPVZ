package com.hungteen.pvz.common.block;

import com.hungteen.pvz.common.capability.pvzRules.PVZRulesCapability;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class WisdomTreeLogBlock extends RotatedPillarBlock {
    public static final IntegerProperty LV = IntegerProperty.create("level", 0/*deliberately.*/, 10);
    public static final IntegerProperty ROOT_LV = IntegerProperty.create("root_level", 1, 10);
    public static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 0, 10);
    public static final BooleanProperty PERSISTENT = BlockStateProperties.PERSISTENT;
    public WisdomTreeLogBlock(Properties p_55926_) {
        super(p_55926_);
        this.registerDefaultState(this.stateDefinition.any().setValue(DISTANCE, 1));
        this.registerDefaultState(this.stateDefinition.any().setValue(LV, 0));
        this.registerDefaultState(this.stateDefinition.any().setValue(ROOT_LV, 1));
        this.registerDefaultState(this.stateDefinition.any().setValue(PERSISTENT, false));
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(DISTANCE, 1)
                .setValue(LV, 0)
                .setValue(ROOT_LV, 1)
                .setValue(PERSISTENT, true);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_51385_) {
        super.createBlockStateDefinition(p_51385_);
        p_51385_.add(DISTANCE);
        p_51385_.add(LV);
        p_51385_.add(ROOT_LV);
        p_51385_.add(PERSISTENT);
    }

    private Direction getGrownFrom(ServerLevel level, BlockPos pos) {
        BlockState to = level.getBlockState(pos);
        Direction.Axis axis = to.getValue(AXIS);
        for (Direction.AxisDirection axisDir : Direction.AxisDirection.values()) {
            Direction direction = Direction.fromAxisAndDirection(axis, axisDir);
            if (direction == Direction.UP) {
                continue;
            }
            BlockState from = level.getBlockState(pos.relative(direction));
            if (from.is(PVZBlocks.WISDOM_TREE_LOG.get()) && (
                    (from.getValue(DISTANCE) == to.getValue(DISTANCE) + 1 && from.getValue(LV) == to.getValue(LV)) ||
                            (from.getValue(DISTANCE) == 0 && from.getValue(LV) == to.getValue(LV) + 1))) {
                return direction;
            }
            if (from.is(PVZBlocks.WISDOM_TREE_CORE.get())) {
                return direction;
            }
        }
        return null;
    }

    private boolean hasGrownTo(ServerLevel level, BlockPos pos, Direction direction) {
        if (direction == Direction.DOWN) {
            return false;
        }
        BlockState to = level.getBlockState(pos.relative(direction));
        BlockState from = level.getBlockState(pos);
        return to.is(PVZBlocks.WISDOM_TREE_LOG.get()) && (
                (from.getValue(DISTANCE) == to.getValue(DISTANCE) + 1 && from.getValue(LV) == to.getValue(LV)) ||
                        (from.getValue(DISTANCE) == 0 && from.getValue(LV) == to.getValue(LV) + 1));
    }

    private boolean hasGrown(ServerLevel level, BlockPos pos) {
        for (Direction direction : Direction.values()) {
            if (direction == Direction.DOWN) {
                continue;
            }
            BlockState to = level.getBlockState(pos.relative(direction));
            BlockState from = level.getBlockState(pos);
            if (to.is(PVZBlocks.WISDOM_TREE_LOG.get()) && (
                    (from.getValue(DISTANCE) == to.getValue(DISTANCE) + 1 && from.getValue(LV) == to.getValue(LV)) ||
                            (from.getValue(DISTANCE) == 0 && from.getValue(LV) == to.getValue(LV) + 1))) {
                return true;
            }
        }
        return false;
    }

    private void tryGrowTo(ServerLevel level, BlockPos pos, Direction direction, int distance, int lv) {
        BlockState from = level.getBlockState(pos);
        if (level.getBlockState(pos.relative(direction)).isAir() || level.getBlockState(pos.relative(direction)).is(PVZBlockTags.WISDOM_TREE_REPLACEABLE)) {
            level.setBlockAndUpdate(pos.relative(direction), PVZBlocks.WISDOM_TREE_LOG.get().defaultBlockState()
                    .setValue(DISTANCE, distance)
                    .setValue(LV, lv)
                    .setValue(ROOT_LV, from.getValue(ROOT_LV))
                    .setValue(AXIS, direction.getAxis()));
        }
    }

    private void tryGrowLeaves(ServerLevel level, BlockPos pos, Direction direction) {
        if (level.getBlockState(pos.relative(direction)).isAir() || level.getBlockState(pos.relative(direction)).is(PVZBlocks.PATTRA_LEAVES.get())) {
            level.setBlockAndUpdate(pos.relative(direction), PVZBlocks.PATTRA_LEAVES.get().defaultBlockState().setValue(PattraLeavesBlock.DISTANCE, 1));
        }
    }

    private void updateLevel() {

    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(blockState, level, pos, random);
        if (level.getBlockState(pos).getValue(PERSISTENT)) {
            return;
        }
        if (PVZRulesCapability.getBoolean("killWisdomTree")) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            return;
        }
        int lv = blockState.getValue(LV);
        int distance = blockState.getValue(DISTANCE);
        float rootLv = blockState.getValue(ROOT_LV);
        if (getGrownFrom(level, pos) == null) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            return;
        }
        if (distance == 0) {
            if ((lv - 1) / rootLv > 0.6) {
                //trunk
                tryGrowTo(level, pos, Direction.UP, lv - (random.nextBoolean() ? 1 : 0), lv - 1);
            } else if ((lv - 1) / rootLv > 0.3) {
                //branch
                if (! hasGrown(level, pos)) {
                    Direction direction = (random.nextFloat() < ((lv - 1) / rootLv)) ? Direction.UP : Direction.Plane.HORIZONTAL.getRandomDirection(random);
                    tryGrowTo(level, pos, direction, direction == Direction.UP ? (int) (lv * 1.5) : (int) (rootLv - lv), lv - 1);
                }
            } else {
                if (lv >= 1) {
                    //leaves branch
                    tryGrowLeaves(level, pos, Direction.UP);
                    Direction direction = (lv % 2 == 0) ? Direction.UP : Direction.Plane.HORIZONTAL.getRandomDirection(random);
                    tryGrowTo(level, pos, direction, direction == Direction.UP ? (random.nextBoolean() ? 1 : 2) :
                            (int) ((rootLv - lv) / 2 > 1 ? (random.nextBoolean() ? (rootLv - lv) / 2 : (rootLv - lv) / 2 - 1) : 1), lv - 1);
                } else {
                    //leaves
                    if (random.nextBoolean()) {
                        tryGrowLeaves(level, pos, Direction.UP);
                    } else {
                        Direction direction = Direction.Plane.HORIZONTAL.getRandomDirection(random);
                        tryGrowLeaves(level, pos, direction);
                    }
                }
            }
        } else {
            //grow
            Direction direction = getGrownFrom(level, pos);
            if (direction != null && distance > 0) {
                tryGrowTo(level, pos, direction.getOpposite(), distance - 1, lv);
            }
        }
    }
}
