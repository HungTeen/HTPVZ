package com.hungteen.pvz.common.block;

import com.hungteen.pvz.common.block.entity.SilverSwordOrnamentBlockEntity;
import com.hungteen.pvz.common.register.PVZBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class SilverSwordOrnamentBlock extends BaseEntityBlock {
    private static final VoxelShape AABB = Block.box(4D, 0.0D, 4D, 12.0D, 8.0D, 12.0D);
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter getter, BlockPos blockPos, CollisionContext collisionContext) {
        return AABB;
    }
    public SilverSwordOrnamentBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
    }
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_153687_) {
        p_153687_.add(FACING);
    }
    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }
    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState blockState) {
        return new SilverSwordOrnamentBlockEntity(pos, blockState);
    }
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState p_153274_, BlockEntityType<T> p_153275_) {
        return createTickerHelper(p_153275_, PVZBlockEntities.SILVER_SWORD_SCULPTURE.get(), SilverSwordOrnamentBlockEntity::tick);
    }
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext p_48689_) {
        return this.defaultBlockState().setValue(FACING, p_48689_.getHorizontalDirection());
    }
}
