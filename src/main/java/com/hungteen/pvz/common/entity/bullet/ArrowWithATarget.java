package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
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
            this.getCapability(PVZOwnedCapability.CAP).orElse(null).setOwner(entity);
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
    }
    @Override
    protected void onHitBlock(BlockHitResult result) {
        //TODO activate redstone blocks.
        super.onHitBlock(result);
    }
}
