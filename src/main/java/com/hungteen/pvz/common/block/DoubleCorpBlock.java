package com.hungteen.pvz.common.block;

import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class DoubleCorpBlock extends CropBlock {
    public static final EnumProperty<DoubleBlockHalf> HALF = BlockStateProperties.DOUBLE_BLOCK_HALF;
    private static final VoxelShape[] LOWER_SHAPE = new VoxelShape[]{
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 6.0D, 12.0D),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 10.0D, 12.0D),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 14.0D, 12.0D),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D),//4
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D)};
    private static final VoxelShape[] UPPER_SHAPE = new VoxelShape[]{
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 6.0D, 12.0D),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 6.0D, 12.0D),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 6.0D, 12.0D),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 6.0D, 12.0D),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 6.0D, 12.0D),//4
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 8.0D, 12.0D),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 12.0D, 12.0D),
            Block.box(4.0D, 0.0D, 4.0D, 12.0D, 16.0D, 12.0D)};
    public DoubleCorpBlock(Properties p_52247_) {
        super(p_52247_);
        this.registerDefaultState(this.stateDefinition.any().setValue(this.getAgeProperty(), 0).setValue(HALF, DoubleBlockHalf.LOWER));
    }
    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter p_52298_, BlockPos p_52299_, CollisionContext p_52300_) {
        int age = blockState.getValue(this.getAgeProperty());
        if (blockState.getValue(HALF) == DoubleBlockHalf.LOWER) {
            return LOWER_SHAPE[age];
        } else {
            return UPPER_SHAPE[age];
        }
    }
    public int getUpperStartAge() {
        return 4;
    }
    @Override
    public ItemLike getBaseSeedId() {
        return PVZItems.CORN_KERNELS.get();
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(HALF);
    }

    public boolean isUpper(BlockState blockState) {
        return blockState.getValue(HALF) == DoubleBlockHalf.UPPER;
    }

    //about growing.
    @Override
    public void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        if (! level.isAreaLoaded(pos, 1)) return; // Forge: prevent loading unloaded chunks when checking neighbor's light
        if (! isUpper(blockState)) {
            if (level.getRawBrightness(pos, 0) >= 9) {
                int age = this.getAge(blockState);
                boolean upperOccupied = ! level.getBlockState(pos.above()).isAir() && ! level.getBlockState(pos.above()).is(blockState.getBlock());
                if (age < (upperOccupied ? this.getUpperStartAge() - 1 : this.getMaxAge())) {
                    float f = getGrowthSpeed(this, level, pos);
                    if (net.minecraftforge.common.ForgeHooks.onCropsGrowPre(level, pos, blockState, random.nextInt((int)(25.0F / f) + 1) == 0)) {
                        level.setBlock(pos, this.getStateForAge(age + 1), 2);
                        if (age + 1 >= getUpperStartAge()) {
                            level.setBlock(pos.above(), this.getStateForAge(age + 1).setValue(HALF, DoubleBlockHalf.UPPER), 2);
                        }
                        net.minecraftforge.common.ForgeHooks.onCropsGrowPost(level, pos, blockState);
                    }
                }
            }
        }
    }
    @Override
    public void growCrops(Level level, BlockPos pos, BlockState blockState) {
        if (blockState.getValue(HALF) == DoubleBlockHalf.UPPER) {
            BlockState lowerState = level.getBlockState(pos.below());
            if (lowerState.getBlock() == blockState.getBlock()) {
                growCrops(level, pos, lowerState);
            }
        }
        int i = this.getAge(blockState) + this.getBonemealAgeIncrease(level);
        boolean upperOccupied = ! level.getBlockState(pos.above()).isAir() && ! level.getBlockState(pos.above()).is(blockState.getBlock());
        int j = (upperOccupied ? this.getUpperStartAge() - 1 : this.getMaxAge());
        if (i > j) {
            i = j;
        }

        level.setBlock(pos, this.getStateForAge(i), 2);
        if (i >= getUpperStartAge()) {
            level.setBlock(pos.above(), this.getStateForAge(i).setValue(HALF, DoubleBlockHalf.UPPER), 2);
        }
    }

    //updating.
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        DoubleBlockHalf thisHalf = state.getValue(HALF);
        if ((thisHalf == DoubleBlockHalf.LOWER && direction == Direction.UP && state.getValue(AGE) >= 4 && neighborState.getBlock() != state.getBlock()) ||
                (thisHalf == DoubleBlockHalf.UPPER && direction == Direction.DOWN && neighborState.getBlock() != state.getBlock())) {
            return Blocks.AIR.defaultBlockState();
        } else {
            return thisHalf == DoubleBlockHalf.LOWER && direction == Direction.DOWN && ! state.canSurvive(level, pos) ?
                    Blocks.AIR.defaultBlockState() : super.updateShape(state, direction, neighborState, level, pos, neighborPos);
        }
    }
    public boolean canSurvive(BlockState blockState, LevelReader level, BlockPos pos) {
        if (blockState.getValue(HALF) != DoubleBlockHalf.UPPER) {
            return super.canSurvive(blockState, level, pos);
        } else {
            BlockState bottom = level.getBlockState(pos.below());
            if (blockState.getBlock() != this) return super.canSurvive(blockState, level, pos);
            return bottom.is(this) && bottom.getValue(HALF) == DoubleBlockHalf.LOWER && bottom.getValue(AGE).equals(blockState.getValue(AGE));
        }
    }
    public void playerWillDestroy(Level p_52755_, BlockPos p_52756_, BlockState p_52757_, Player p_52758_) {
        if (!p_52755_.isClientSide && p_52758_.isCreative()) {
            preventCreativeDropFromBottomPart(p_52755_, p_52756_, p_52757_, p_52758_);
        }
        super.playerWillDestroy(p_52755_, p_52756_, p_52757_, p_52758_);
    }

    protected static void preventCreativeDropFromBottomPart(Level level, BlockPos pos, BlockState blockState, Player player) {
        DoubleBlockHalf doubleblockhalf = blockState.getValue(HALF);
        if (doubleblockhalf == DoubleBlockHalf.UPPER) {
            BlockPos blockpos = pos.below();
            BlockState blockstate = level.getBlockState(blockpos);
            if (blockstate.is(blockState.getBlock()) && blockstate.getValue(HALF) == DoubleBlockHalf.LOWER) {
                BlockState blockstate1 = blockstate.hasProperty(BlockStateProperties.WATERLOGGED) && blockstate.getValue(BlockStateProperties.WATERLOGGED) ? Blocks.WATER.defaultBlockState() : Blocks.AIR.defaultBlockState();
                level.setBlock(blockpos, blockstate1, 35);
                level.levelEvent(player, 2001, blockpos, Block.getId(blockstate));
            }
        }
    }
}
