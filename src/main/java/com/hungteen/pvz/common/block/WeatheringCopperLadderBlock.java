package com.hungteen.pvz.common.block;

import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.WeatheringCopper;
import net.minecraft.world.level.block.state.BlockState;

import java.util.Optional;

/**
 * @program: HTPVZ2
 * @author: HungTeen
 * @create: 2026-10-03
 */
public class WeatheringCopperLadderBlock extends CopperLadderBlock implements WeatheringCopper {

    private final WeatherState weatherState;

    public WeatheringCopperLadderBlock(WeatherState weatherState, Properties properties) {
        super(properties);
        this.weatherState = weatherState;
    }

    @Override
    public void randomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        this.onRandomTick(state, level, pos, random);
    }


    @Override
    public void onRandomTick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (this.weatherState == WeatherState.OXIDIZED) {
            if (random.nextFloat() < 0.01F) {
                level.destroyBlock(pos, false);
            }
        } else {
            this.applyChangeOverTime(state, level, pos, random);
        }
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public Optional<BlockState> getNext(BlockState state) {
        return CopperLadderBlock.getNextState(state);
    }

    @Override
    public WeatherState getAge() {
        return this.weatherState;
    }
}
