package com.hungteen.pvz.common.block.entity;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZBlockEntities;
import com.hungteen.pvz.common.register.PVZDamageSource;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.List;

public class SilverSwordOrnamentBlockEntity extends BlockEntity {

    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState attackAnimationState = new AnimationState();
    int damageCount = 0;
    
    public SilverSwordOrnamentBlockEntity(BlockPos p_155229_, BlockState p_155230_) {
        super(PVZBlockEntities.SILVER_SWORD_SCULPTURE.get(), p_155229_, p_155230_);
        this.idleAnimationState.start((int) (PVZMod.clientTime));
    }
    public static void tick(Level level, BlockPos pos, BlockState blockState, SilverSwordOrnamentBlockEntity blockEntity) {
            boolean signal = level.hasNeighborSignal(blockEntity.getBlockPos());
            if (signal) {
                blockEntity.damageCount ++;
                if (blockEntity.damageCount == 20) {
                    blockEntity.damageCount = 0;
                }
                if (! blockEntity.attackAnimationState.isStarted()) {
                    blockEntity.attackAnimationState.start((int) PVZMod.clientTime);
                    blockEntity.idleAnimationState.stop();
                    blockEntity.damageCount = 0;
                }
                if (! level.isClientSide) {
                    if (blockEntity.damageCount == 3) {
                        List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class
                                , new AABB(pos.offset(-1, 0, -1), pos.offset(2, 1, 2))
                                , entity -> true);
                        entities.forEach(entity -> entity.hurt(PVZDamageSource.SILVER_SWORD, 1));
                    }
                }
            } else if (! blockEntity.idleAnimationState.isStarted()) {
                blockEntity.idleAnimationState.start((int) PVZMod.clientTime);
                blockEntity.attackAnimationState.stop();
            }
    }
}
