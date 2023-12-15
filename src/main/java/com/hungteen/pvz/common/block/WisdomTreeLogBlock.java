package com.hungteen.pvz.common.block;

import com.hungteen.pvz.common.capability.pvzRules.PVZRulesCapability;
import com.hungteen.pvz.common.register.PVZBlocks;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class WisdomTreeLogBlock extends RotatedPillarBlock {
    public static final IntegerProperty LV = IntegerProperty.create("level", 0/*deliberately.*/, 10);
    public static final IntegerProperty ROOT_LV = IntegerProperty.create("root_level", 1, 10);
    public static final IntegerProperty DISTANCE = IntegerProperty.create("distance", 1, 10);
    public WisdomTreeLogBlock(Properties p_55926_) {
        super(p_55926_);
        this.registerDefaultState(this.stateDefinition.any().setValue(DISTANCE, 1));
        this.registerDefaultState(this.stateDefinition.any().setValue(LV, 0));
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return super.getStateForPlacement(context)
                .setValue(DISTANCE, 1)
                .setValue(LV, 0)
                .setValue(ROOT_LV, 1);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_51385_) {
        super.createBlockStateDefinition(p_51385_);
        p_51385_.add(DISTANCE);
        p_51385_.add(LV);
        p_51385_.add(ROOT_LV);
    }

    public void updateLevel(BlockState blockState, ServerLevel level, BlockPos pos) {

    }

    public boolean checkAndGrow(ServerLevel level, int lv, int rootLv, int dist, BlockPos pos, Direction direction, Direction.Axis axis) {
        BlockState grewFrom = level.getBlockState(pos.relative(direction.getOpposite()));
        BlockState growTo = level.getBlockState(pos.relative(direction));
        if (grewFrom.is(PVZBlocks.WISDOM_TREE_LOG.get()) &&
                ((grewFrom.getValue(LV) == lv && grewFrom.getValue(DISTANCE) == dist - 1) || (
                        grewFrom.getValue(LV) == lv + 1 && grewFrom.getValue(DISTANCE) == lv + 1
                        ))) {
            if (growTo.isAir() || growTo.is(PVZBlocks.PATTRA_LEAVES.get())) {
                if (! level.getBlockState(pos.relative(direction).relative(direction)).is(PVZBlocks.WISDOM_TREE_LOG.get())) {
                    level.setBlockAndUpdate(pos.relative(direction), PVZBlocks.WISDOM_TREE_LOG.get().defaultBlockState()
                            .setValue(WisdomTreeLogBlock.LV, lv)
                            .setValue(WisdomTreeLogBlock.ROOT_LV, rootLv)
                            .setValue(DISTANCE, dist + 1)
                            .setValue(RotatedPillarBlock.AXIS, axis));
                } else {
                    level.setBlockAndUpdate(pos.relative(direction), PVZBlocks.PATTRA_LEAVES.get().defaultBlockState()
                            .setValue(BlockStateProperties.DISTANCE, 1));
                }
            }
            return true;
        }
        return rootLv == lv;
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(blockState, level, pos, random);
        if (PVZRulesCapability.getBoolean("killWisdomTree")) {
            level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
            return;
        }
        if (random.nextInt(5) == 0) {
            int lv = blockState.getValue(LV);
            if (lv != 0) {
                int dist = blockState.getValue(DISTANCE);
                if (dist < lv) {
                    Direction.Axis axis = blockState.getValue(AXIS);
                    int rootLv = blockState.getValue(ROOT_LV);
                    switch (axis) {
                        case X -> {
                            Direction direction = random.nextBoolean() ? Direction.EAST : Direction.WEST;
                            if (! checkAndGrow(level, lv, rootLv, dist, pos, direction, Direction.Axis.X)) {
                                if (! checkAndGrow(level, lv, rootLv, dist, pos, direction.getOpposite(), Direction.Axis.X)) {
                                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                                }
                            }
                        }
                        case Y -> {
                            Direction direction = random.nextBoolean() ? Direction.UP : Direction.DOWN;
                            if (! checkAndGrow(level, lv, rootLv, dist, pos, direction, Direction.Axis.Y)) {
                                if (! checkAndGrow(level, lv, rootLv, dist, pos, direction.getOpposite(), Direction.Axis.Y)) {
                                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                                }
                            }
                        }
                        default -> {
                            Direction direction = random.nextBoolean() ? Direction.NORTH : Direction.SOUTH;
                            if (! checkAndGrow(level, lv, rootLv, dist, pos, direction, Direction.Axis.Z)) {
                                if (! checkAndGrow(level, lv, rootLv, dist, pos, direction.getOpposite(), Direction.Axis.Z)) {
                                    level.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                                }
                            }
                        }
                    }
                } else {
                    //random expand
                    int canGrow = 0;
                    BlockState blockState1;
                    for (Direction direction : Direction.values()) {
                        blockState1 = level.getBlockState(pos.relative(direction));
                        if (blockState1.is(PVZBlocks.WISDOM_TREE_LOG.get()) && blockState1.getValue(DISTANCE) == 1) {
                            canGrow += 1;
                        }
                    }
                    if (canGrow < 2) {
                        int rootLv = blockState.getValue(ROOT_LV);
                        Direction direction;
                        if (lv == rootLv || lv == 1) {
                            direction = Direction.UP;
                        } else {
                            direction = random.nextFloat() < ((float) lv * lv / (rootLv) / (rootLv)) ? Direction.UP : Direction.getRandom(random);
                            direction = direction == Direction.DOWN ? Direction.UP : direction;
                            if (direction.getAxis() == blockState.getValue(AXIS)) {
                                direction = random.nextFloat() < ((float) lv * lv / (rootLv + 1) / (rootLv + 1)) ? Direction.UP : Direction.getRandom(random);
                                direction = direction == Direction.DOWN ? Direction.UP : direction;
                            }
                        }

                        Direction.Axis axis = (direction == Direction.UP) ? Direction.Axis.Y :
                                ((direction == Direction.EAST || direction == Direction.WEST) ? Direction.Axis.X : Direction.Axis.Z);
                        if (level.getBlockState(pos.relative(direction)).isAir() ||
                                level.getBlockState(pos.relative(direction)).is(PVZBlocks.PATTRA_LEAVES.get())) {
                            if (! level.getBlockState(pos.relative(direction).relative(direction)).is(PVZBlocks.WISDOM_TREE_LOG.get())) {
                                level.setBlockAndUpdate(pos.relative(direction), PVZBlocks.WISDOM_TREE_LOG.get().defaultBlockState()
                                        .setValue(WisdomTreeLogBlock.LV, lv - 1)
                                        .setValue(WisdomTreeLogBlock.ROOT_LV, rootLv)
                                        .setValue(DISTANCE, 1)
                                        .setValue(RotatedPillarBlock.AXIS, axis));
                            } else {
                                level.setBlockAndUpdate(pos.relative(direction), PVZBlocks.PATTRA_LEAVES.get().defaultBlockState()
                                        .setValue(BlockStateProperties.DISTANCE, 1));
                            }
                        }
                    }
                }
            } else {
                //grow leaves
                boolean canGrow = false;
                BlockState blockState1;
                for (Direction direction : Direction.values()) {
                    blockState1 = level.getBlockState(pos.relative(direction));
                    if (blockState1.is(PVZBlocks.WISDOM_TREE_LOG.get()) && blockState1.getValue(DISTANCE) == 1) {
                        canGrow = true;
                        break;
                    }
                }
                if (canGrow) {
                    BlockPos growToPos = pos.relative(Direction.Axis.getRandom(random), random.nextBoolean() ? -1 : 1);
                    if (level.getBlockState(growToPos).isAir()) {
                        level.setBlockAndUpdate(growToPos, PVZBlocks.PATTRA_LEAVES.get().defaultBlockState()
                                .setValue(BlockStateProperties.DISTANCE, 1));
                    }
                }
            }
        }
    }
}
