package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class PoleVaultingZombie extends PVZZombie {
    public static final UUID SPEED_MODIFIER_UUID = UUID.fromString("6e7b1022-c83a-d406-effb-ebded074d30a");
    protected static final EntityDataAccessor<Boolean> HAS_POLE = SynchedEntityData.defineId(PoleVaultingZombie.class, EntityDataSerializers.BOOLEAN);
    public boolean renderPole = true;

    public PoleVaultingZombie(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        SpawnGroupData spawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, data, tag);
        this.setLeftHanded(false);
        this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        return spawnGroupData;
    }

    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.addGoal(1, new PoleVaultGoal(this));
    }
    @Override
    public void tick() {
        super.tick();
        if (! level.isClientSide()) {
            AttributeInstance attribute = this.getAttribute(Attributes.MOVEMENT_SPEED);
            if (this.hasPole()) {
                if (attribute.getModifier(SPEED_MODIFIER_UUID) == null && ! this.isBaby()) {
                    attribute.addPermanentModifier(new AttributeModifier(SPEED_MODIFIER_UUID, "running with pole", 0.1, AttributeModifier.Operation.ADDITION));
                }
            } else {
                attribute.removeModifier(SPEED_MODIFIER_UUID);
            }
        }
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(HAS_POLE, true);
    }
    public boolean hasPole() {
        return this.entityData.get(HAS_POLE);
    }
    public void setHasPole(boolean hasPole) {
        this.entityData.set(HAS_POLE, hasPole);
    }
    public boolean canHoldItem(ItemStack itemStack) {
        return ! hasPole() && super.canHoldItem(itemStack);
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("has_pole", this.hasPole());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("has_pole")) {
            this.setHasPole(tag.getBoolean("has_pole"));
        }
    }

    public static class PoleVaultGoal extends Goal {
        public final PoleVaultingZombie zombie;
        public PoleVaultGoal(PoleVaultingZombie zombie) {
            this.zombie = zombie;
        }
        @Override
        public boolean canUse() {
            if (! zombie.hasPole()) return false;
            var lookAngle = this.zombie.getViewVector(0).normalize();
            List<Entity> entities = zombie.level.getEntities(zombie,
                    zombie.getBoundingBox().inflate(1.5, 0, 1.5).move(lookAngle.scale(2)),
                    (entity) -> entity instanceof LivingEntity && EntityUtil.checkCanEntityBeAttack(this.zombie, entity));
            return ! entities.isEmpty();
        }

        @Override
        public void tick() {
            var lookAngle = this.zombie.getViewVector(0).normalize();
            zombie.addEffect(new MobEffectInstance(MobEffects.JUMP, 1, 2));
            zombie.jumpControl.jump();
            zombie.setDeltaMovement(zombie.getDeltaMovement().add(lookAngle));
            //to reset path when landing.
            zombie.getNavigation().timeLastRecompute = this.zombie.level.getGameTime() - 13;
            zombie.getNavigation().recomputePath();
            zombie.setHasPole(false);
        }
    }

    //TODO add a navigator.
}
