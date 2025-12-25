package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
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
    private LivingEntity owner;
    private ItemStack seedPacket;
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
            PVZEntityCapability cap = this.getCapability(PVZEntityCapability.CAP).orElse(null);
            if (cap != null) {
                cap.setOwner(entity);
            }
        }
        if (entity instanceof LivingEntity living) {
            this.owner = living;
        } else {
            this.owner = null;
        }
        super.setOwner(entity);
    }

    public SeedArrow(EntityType<? extends Arrow> entityType, Level level) {
        super(entityType, level);
        this.setBaseDamage(0);
        this.pickup = Pickup.DISALLOWED;
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (owner instanceof Player player && seedPacket != null && seedPacket.getItem() instanceof SeedPacketItem<?> item) {
            if (player.getCooldowns().isOnCooldown(item)) {
                player.displayClientMessage(Component.translatable("hint.pvz.plant.on_cool_down", seedPacket.getDisplayName()), true);
            } else {
                MutableComponent plantResult = item.plantOnEntity(player, seedPacket, this.level, result.getEntity());
                if (plantResult != null && result.getEntity().getRootVehicle() != result.getEntity()) {
                    plantResult = item.plantOnEntity(player, seedPacket, this.level, result.getEntity());
                }
                if (plantResult != null) {
                    if (EntityUtil.isTeammate(owner, result.getEntity())) {
                        return;
                    }
                    plantResult = item.plantOnBlock(player, seedPacket, this.level, result.getEntity().getOnPos(), Direction.UP);
                    if (plantResult != null) {
                        plantResult = item.plantOnBlock(player, seedPacket, this.level, result.getEntity().getRootVehicle().getOnPos(), Direction.UP);
                    }
                }
                if (plantResult != null) {
                    player.displayClientMessage(plantResult, true);
                }
                this.discard();
            }
        }
        super.onHitEntity(result);
    }
    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (owner instanceof Player player && seedPacket != null && seedPacket.getItem() instanceof SeedPacketItem<?> item) {
            if (player.getCooldowns().isOnCooldown(item)) {
                player.displayClientMessage(Component.translatable("hint.pvz.plant.on_cool_down", seedPacket.getDisplayName()), true);
            } else {
                MutableComponent plantResult = item.plantOnBlock(player, seedPacket, this.level, result.getBlockPos(), result.getDirection());
                if (plantResult != null) {
                    player.displayClientMessage(plantResult, true);
                    this.setOwner(null);
                } else {
                    this.discard();
                }
            }
        }
        super.onHitBlock(result);
    }

    @Override
    protected ItemStack getPickupItem() {
        return null;
    }

    public void addAdditionalSaveData(CompoundTag p_36881_) {
        super.addAdditionalSaveData(p_36881_);
        if (this.owner != null) {
            p_36881_.putUUID("Owner", owner.getUUID());
        }
        if (this.seedPacket != null) {
            CompoundTag tag = new CompoundTag();
            seedPacket.save(tag);
            p_36881_.put("Item", tag);
        }

    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Owner") && this.level instanceof ServerLevel level) {
            this.owner = level.getPlayerByUUID(tag.getUUID("Owner"));
        }
        if (tag.contains("Item")) {
            CompoundTag tag1 = tag.getCompound("Item");
            this.seedPacket = ItemStack.of(tag1);
        }
    }
}
