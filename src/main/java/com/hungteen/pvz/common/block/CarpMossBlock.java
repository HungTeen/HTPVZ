package com.hungteen.pvz.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.CarpetBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

public class CarpMossBlock extends CarpetBlock {

    public static final IntegerProperty AGE = BlockStateProperties.AGE_3;


    public CarpMossBlock(Properties p_153822_) {
        super(p_153822_);
        this.registerDefaultState(this.stateDefinition.any().setValue(AGE, 0));
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(AGE);
    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(blockState, level, pos, random);
        if (random.nextInt(6) == 0){
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for(Direction direction : Direction.values()) {
                blockpos$mutableblockpos.setWithOffset(pos, direction);
                BlockState blockstate = level.getBlockState(blockpos$mutableblockpos);
                if (blockstate.is(this) && !this.slightlyMelt(blockstate, level, blockpos$mutableblockpos)) {
                    level.scheduleTick(blockpos$mutableblockpos, this, Mth.nextInt(random, 20, 40));
                }
            }
        }
    }

    private boolean slightlyMelt(BlockState blockState, Level level, BlockPos pos) {
        int i = blockState.getValue(AGE);
        if (i < 3) {
            level.setBlock(pos, blockState.setValue(AGE, Integer.valueOf(i + 1)), 2);
            return false;
        } else {
            level.removeBlock(pos, false);
            return true;
        }
    }

}
