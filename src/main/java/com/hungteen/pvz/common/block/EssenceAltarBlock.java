package com.hungteen.pvz.common.block;

import com.hungteen.pvz.common.block.entity.EssenceAltarBlockEntity;
import com.hungteen.pvz.common.menu.EssenceAltarMenu;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.*;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class EssenceAltarBlock extends BaseEntityBlock {
    private static final VoxelShape AABB = Shapes.or(Block.box(0.0D, 0.0D, 0.0D, 16.0D, 4.0D, 16.0D)
            , Block.box(2.0D, 4.0D, 2.0D, 14.0D, 8.0D, 14.0D));
    public EssenceAltarBlock(Properties p_49224_) {
        super(p_49224_);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState blockState) {
        return new EssenceAltarBlockEntity(pos, blockState);
    }
    @Override
    public InteractionResult use(BlockState blockState, Level level, BlockPos blockPos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            player.openMenu(blockState.getMenuProvider(level, blockPos));
            return InteractionResult.CONSUME;
        }
    }

    @Override
    public MenuProvider getMenuProvider(BlockState blockState, Level level, BlockPos blockPos) {
        BlockEntity blockentity = level.getBlockEntity(blockPos);
        if (blockentity instanceof Nameable) {
            Component component = ((Nameable) blockentity).getDisplayName();
            return new SimpleMenuProvider(
                    (id, inventory, player) -> new EssenceAltarMenu(inventory, id, ContainerLevelAccess.create(level, blockPos)),
                    component);
        } else {
            return null;
        }
    }
    @Override
    public void setPlacedBy(Level level, BlockPos blockPos, BlockState blockState, LivingEntity livingEntity, ItemStack stack) {
        if (stack.hasCustomHoverName()) {
            BlockEntity blockentity = level.getBlockEntity(blockPos);
            if (blockentity instanceof EssenceAltarBlockEntity) {
                ((EssenceAltarBlockEntity) blockentity).setCustomName(stack.getHoverName());
            }
        }
    }
    @Override
    public RenderShape getRenderShape(BlockState blockState) {
        return RenderShape.MODEL;
    }
    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter getter, BlockPos blockPos, CollisionContext collisionContext) {
        return AABB;
    }

}
