package com.hungteen.pvz.common.block;

import com.hungteen.pvz.common.block.entity.EssenceFurnaceBlockEntity;
import com.hungteen.pvz.common.menu.EssenceFurnaceMenu;
import com.hungteen.pvz.common.register.PVZBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraftforge.network.NetworkHooks;

public class EssenceFurnaceBlock extends BaseEntityBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final BooleanProperty LIT = BlockStateProperties.LIT;
    public EssenceFurnaceBlock(Properties p_49224_) {
        super(p_49224_);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH).setValue(LIT, Boolean.FALSE));
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_48725_) {
        p_48725_.add(FACING, LIT);
    }
    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide && player instanceof ServerPlayer p) {
            NetworkHooks.openScreen(p, blockState.getMenuProvider(level, blockPos), blockPos);
            return InteractionResult.CONSUME;
        }
        return InteractionResult.SUCCESS;
    }

    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState p_153274_, BlockEntityType<T> p_153275_) {
        return createTickerHelper(p_153275_, PVZBlockEntities.ESSENCE_FURNACE.get(), EssenceFurnaceBlockEntity::tick);
    }

//    @Override
//    public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
//        BlockEntity blockentity = level.getBlockEntity(blockPos);
//        if (blockentity instanceof EssenceFurnaceBlockEntity be) {
//            Component component = be.getDisplayName();
//            return new SimpleMenuProvider(
//                    (id, inventory, player) -> new EssenceFurnaceMenu(inventory, id, be.dataAccess, ContainerLevelAccess.create(player.level, blockPos)),
//                    component);
//        } else {
//            return null;
//        }
//    }

    public void onRemove(BlockState p_48713_, Level level, BlockPos pos, BlockState p_48716_, boolean p_48717_) {
        if (!p_48713_.is(p_48716_.getBlock())) {
            BlockEntity blockentity = level.getBlockEntity(pos);
            if (blockentity instanceof EssenceFurnaceBlockEntity be) {
                if (level instanceof ServerLevel) {
                    NonNullList<ItemStack> list = NonNullList.create();
                    for (int i = 0; i < be.handler.getSlots(); i ++){
                        list.add(be.handler.getStackInSlot(i));
                    }
                    Containers.dropContents(level, pos, list);
                }

                level.updateNeighbourForOutputSignal(pos, this);
            }

            super.onRemove(p_48713_, level, pos, p_48716_, p_48717_);
        }
    }

    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            BlockEntity blockentity = level.getBlockEntity(blockPos);
            if (blockentity instanceof EssenceFurnaceBlockEntity) {
                ((EssenceFurnaceBlockEntity) blockentity).setCustomName(stack.getHoverName());
            }
        }
    }
    @Override
    public RenderShape getRenderShape(BlockState p_49232_) {
        return RenderShape.MODEL;
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState blockState) {
        return new EssenceFurnaceBlockEntity(pos, blockState);
    }

    public BlockState rotate(BlockState p_48722_, Rotation p_48723_) {
        return p_48722_.setValue(FACING, p_48723_.rotate(p_48722_.getValue(FACING)));
    }

    public BlockState mirror(BlockState p_48719_, Mirror p_48720_) {
        return p_48719_.rotate(p_48720_.getRotation(p_48719_.getValue(FACING)));
    }

    public BlockState getStateForPlacement(BlockPlaceContext p_48689_) {
        return this.defaultBlockState().setValue(FACING, p_48689_.getHorizontalDirection().getOpposite());
    }
}
