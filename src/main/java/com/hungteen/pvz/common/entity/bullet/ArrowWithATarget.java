package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.Nullable;

public class ArrowWithATarget extends AbstractArrow {
    public ArrowWithATarget(Level level, LivingEntity entity) {
        super(PVZEntities.ARROW_WITH_A_TARGET.get(), entity, level);
    }

    public ArrowWithATarget(EntityType<? extends AbstractArrow> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        if (! level.isClientSide()) {
            this.getCapability(PVZEntityCapability.CAP).orElse(null).setOwner(entity);
        }
        super.setOwner(entity);
    }

    @Override
    protected ItemStack getPickupItem() {
        return PVZItems.ARROW_WITH_A_TARGET.get().getDefaultInstance();
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (this.getOwner() != null) {
            this.getOwner().gameEvent(GameEvent.PROJECTILE_LAND, result.getEntity());
        }
        super.onHitEntity(result);
        if (this.isRemoved() && result.getEntity() instanceof LivingEntity living) {
            if (! this.level.isClientSide && this.getPierceLevel() <= 0 && living.getArrowCount() > 0) {
                living.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> {
                    int arrows = cap.getStuckArrowWithATarget();
                    if (arrows < 4) {
                        cap.setStuckArrowWithATarget(arrows + 1);
                        living.setArrowCount(living.getArrowCount() - 1);
                    }
                });
            }
        }
    }

    @Override
    protected void onHitBlock(BlockHitResult result) {
        //TODO activate redstone blocks.
//        BlockState state = level.getBlockState(result.getBlockPos().relative(result.getDirection()));
//        if (state.is(Blocks.REDSTONE_WIRE)) {
//            level.setBlock(result.getBlockPos().relative(result.getDirection()), state.setValue(BlockStateProperties.POWER, 8),3);
//            level.updateNeighborsAt(result.getBlockPos().relative(result.getDirection()), Blocks.REDSTONE_WIRE);
//        }
        super.onHitBlock(result);
    }
}
