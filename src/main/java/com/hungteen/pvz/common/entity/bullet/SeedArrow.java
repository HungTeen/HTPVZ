package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.register.PVZEntities;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;

import javax.annotation.Nullable;

public class SeedArrow <T extends Entity> extends Arrow {
    private final LivingEntity owner;
    private final ItemStack seedPacket;
    public SeedArrow(Level level, LivingEntity owner, ItemStack seedPacket) {
        super(PVZEntities.SEED_ARROW.get(), level);
        this.owner = owner;
        this.seedPacket = seedPacket;
        this.setOwner(owner);
        this.setBaseDamage(0);
        this.pickup = Pickup.DISALLOWED;
    }

    @Override
    public void setOwner(@Nullable Entity entity) {
        if (! level.isClientSide()) {
            this.getCapability(PVZOwnedCapability.CAP).orElse(null).setOwner(entity);
        }
        super.setOwner(entity);
    }

    public SeedArrow(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
        this.owner = null;
        this.seedPacket = null;
        this.setBaseDamage(0);
        this.pickup = Pickup.DISALLOWED;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (owner instanceof Player player && seedPacket != null && seedPacket.getItem() instanceof SeedPacketItem<?> item) {
            MutableComponent result1 = item.plantOnEntity(player, seedPacket, this.level, result.getEntity());
            if (result1 != null) {
                result1 = item.plantOnBlock(player, seedPacket, this.level, result.getEntity().blockPosition().below(), Direction.UP);
            }
            if (result1 != null) {
                player.displayClientMessage(result1, true);
            }
            this.discard();
        }
        super.onHitEntity(result);
    }
    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (owner instanceof Player player && seedPacket != null && seedPacket.getItem() instanceof SeedPacketItem<?> item) {
            MutableComponent result1 = item.plantOnBlock(player, seedPacket, this.level, result.getBlockPos(), result.getDirection());
            this.discard();
            if (result1 != null) {
                player.displayClientMessage(result1, true);
            }
        }
        super.onHitBlock(result);
    }

    @Override
    protected ItemStack getPickupItem() {
        return null;
    }
}
