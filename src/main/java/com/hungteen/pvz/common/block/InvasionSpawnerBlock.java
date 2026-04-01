package com.hungteen.pvz.common.block;

import com.hungteen.pvz.common.block.entity.InvasionSpawnerBlockEntity;
import com.hungteen.pvz.common.register.PVZBlockEntities;
import com.hungteen.pvz.common.register.PVZBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import org.jetbrains.annotations.Nullable;

public class InvasionSpawnerBlock extends BaseEntityBlock {
    public static final BooleanProperty TRIGGERED = BlockStateProperties.TRIGGERED;
    public InvasionSpawnerBlock(Properties p_49224_) {
        super(p_49224_);
        this.registerDefaultState(this.stateDefinition.any().setValue(TRIGGERED, Boolean.FALSE));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_48725_) {
        p_48725_.add(TRIGGERED);
    }

    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        BlockState blockState = level.getBlockState(pos);
        if (blockState.is(PVZBlocks.INVASION_SPAWNER.get())) {
            boolean triggered = blockState.getValue(InvasionSpawnerBlock.TRIGGERED);
            if (triggered || random.nextBoolean()) {
                for (int i = 0; i < (triggered ? 3 : 1); i ++ ) {
                    level.addParticle(
                            ParticleTypes.ELECTRIC_SPARK
                            , pos.getX() + random.nextFloat() * 1.2 - 0.1
                            , pos.getY() + random.nextFloat()
                            , pos.getZ() + random.nextFloat() * 1.2 - 0.1
                            , 0, random.nextFloat() * (triggered ? 1.5 : 0.5), 0);
                }
            }
        }
    }
    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState p_153274_, BlockEntityType<T> p_153275_) {
        return createTickerHelper(p_153275_, PVZBlockEntities.INVASION_SPAWNER.get(), InvasionSpawnerBlockEntity::tick);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new InvasionSpawnerBlockEntity(pos, state);
    }
}
