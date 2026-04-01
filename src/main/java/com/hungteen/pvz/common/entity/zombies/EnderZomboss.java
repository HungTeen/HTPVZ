package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.LavaGhastling;
import com.hungteen.pvz.common.register.PVZAttributes;
import com.hungteen.pvz.common.register.PVZEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.monster.Shulker;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public class EnderZomboss extends Shulker {
    public BlockPos homePos = null;
    private final ServerBossEvent bossEvent = (ServerBossEvent)(new ServerBossEvent(this.getDisplayName(), BossEvent.BossBarColor.PURPLE, BossEvent.BossBarOverlay.PROGRESS)).setDarkenScreen(false);


    public EnderZomboss(EntityType<? extends Shulker> p_33404_, Level p_33405_) {
        super(p_33404_, p_33405_);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        return super.hurt(damageSource, 1);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 100D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 100D)
                .add(Attributes.ARMOR, 20D)
                .add(Attributes.FOLLOW_RANGE, 40D)
                .add(PVZAttributes.PLANT_HURT_RESISTANCE.get(), 0.6D);
    }
    @Override
    public void tick() {
        super.tick();
        if (! level.isClientSide) {
            if (this.isAlive()) {
                if (this.homePos == null) {
                    this.homePos = this.blockPosition();
                }
            }
            this.bossEvent.setProgress(this.isPassenger() && this.getVehicle() instanceof LavaGhastling lavaGhastling ?
                    lavaGhastling.getHealth() / lavaGhastling.getMaxHealth() : this.getHealth() / this.getMaxHealth());
            this.bossEvent.setColor(this.isPassenger() ? BossEvent.BossBarColor.RED : BossEvent.BossBarColor.PURPLE);

            if (this.blockPosition().distSqr(homePos) > 400 || getY() < homePos.getY()) {
                this.teleportTo(homePos.getX(), homePos.getY(), homePos.getZ());
            }
            if (tickCount % 200 == 0 && this.getTarget() != null && isEffectiveAi()) {
                EntityType<?> entityType = switch (random.nextInt(20)) {
                    case 0 -> PVZEntities.PEA_SHOOTER_ZOMBIE.get();
                    case 1 -> PVZEntities.WALL_NUT_ZOMBIE.get();
                    case 2 -> PVZEntities.TALL_NUT_ZOMBIE.get();
                    case 3 -> PVZEntities.JALAPENO_ZOMBIE.get();
                    case 4 -> PVZEntities.SNOW_PEA_ZOMBIE.get();
                    case 5 -> PVZEntities.GATLING_PEA_ZOMBIE.get();
                    case 6 -> PVZEntities.PUMPKIN_ZOMBIE.get();
                    default -> PVZEntities.ZOMBIE.get();
                };
                Entity entity = entityType.create(this.level);
                if (entity != null) {
                    entity.teleportTo(this.getX(), this.getY() + 2, this.getZ());
                    this.level.addFreshEntity(entity);
                    entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.setOwner(this));
                }
            }
        }
    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.homePos != null) {
            CompoundTag posTag = new CompoundTag();
            posTag.putInt("x", this.homePos.getX());
            posTag.putInt("y", this.homePos.getY());
            posTag.putInt("z", this.homePos.getZ());
            tag.put("HomePos", posTag);
        }
    }

    @Override
    public void setCustomName(@Nullable Component p_31476_) {
        super.setCustomName(p_31476_);
        this.bossEvent.setName(this.getDisplayName());
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("HomePos")) {
            CompoundTag posTag = tag.getCompound("HomePos");
            try {
                this.homePos = new BlockPos(posTag.getInt("x"), posTag.getInt("y"), posTag.getInt("z"));
            } catch (Exception ignored) {
                this.homePos = this.blockPosition();
            }
        }
        if (this.hasCustomName()) {
            this.bossEvent.setName(this.getDisplayName());
        }
    }

    @Override
    public void startSeenByPlayer(ServerPlayer p_31483_) {
        super.startSeenByPlayer(p_31483_);
        this.bossEvent.addPlayer(p_31483_);
    }

    @Override
    public void stopSeenByPlayer(ServerPlayer p_31488_) {
        super.stopSeenByPlayer(p_31488_);
        this.bossEvent.removePlayer(p_31488_);
    }

    @Override
    public boolean removeWhenFarAway(double p_27598_) {
        return false;
    }
}
