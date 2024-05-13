package com.hungteen.pvz.common.block;

import com.hungteen.pvz.api.interfaces.IGardenPlant;
import com.hungteen.pvz.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.SlabType;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class GardenFlowerPotBlock extends SlabBlock {
    public GardenFlowerPotBlock(Properties p_56359_) {
        super(p_56359_);
        this.registerDefaultState(this.defaultBlockState()
                .setValue(TYPE, SlabType.BOTTOM)
                .setValue(WATERLOGGED, Boolean.FALSE)
                .setValue(IGardenPlant.WATER, Boolean.FALSE));
    }
    public VoxelShape getShape(BlockState p_56390_, BlockGetter p_56391_, BlockPos p_56392_, CollisionContext p_56393_) {
        SlabType slabtype = p_56390_.getValue(TYPE);
        switch (slabtype) {
            case DOUBLE:
                return Block.box(1.0D, 0.0D, 1.0D, 15.0D, 15.0D, 15.0D);
            case TOP:
                return Block.box(1.0D, 8.0D, 1.0D, 15.0D, 15.0D, 15.0D);
            default:
                return Block.box(1.0D, 0.0D, 1.0D, 15.0D, 7.0D, 15.0D);
        }
    }
    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource random) {
        if ((long)level.random.nextInt(30) <= level.getGameTime() % 30L && level.dimension().location().equals(Util.prefix("zen_garden"))) {
            level.addParticle(ParticleTypes.COMPOSTER.getType(), blockPos.getX() + random.nextFloat(), blockPos.getY() + random.nextFloat(), blockPos.getZ() + random.nextFloat(), 0, 0, 0);
        }
    }
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_56388_) {
        p_56388_.add(TYPE, WATERLOGGED, IGardenPlant.WATER);
    }
    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (!blockState.getValue(BlockStateProperties.WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
            if (!level.isClientSide()) {
                level.setBlock(blockPos, blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.valueOf(true)), 3);
                level.scheduleTick(blockPos, fluidState.getType(), fluidState.getType().getTickDelay(level));
            }
            return true;
        } else {
            return false;
        }
    }
    @Override
    public boolean canPlaceLiquid(BlockGetter level, BlockPos blockPos, BlockState blockState, Fluid fluidState) {
        return !blockState.getValue(BlockStateProperties.WATERLOGGED) && fluidState == Fluids.WATER;
    }
}
