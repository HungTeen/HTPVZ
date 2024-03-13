package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.entity.ai.goal.DisperseEnemyTargetGoal;
import com.hungteen.pvz.common.register.PVZEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;


public class Anger extends FlyingMob {
    public int maxLife = 20;
    public Anger(EntityType<? extends FlyingMob> p_218310_, Level p_218311_) {
        super(p_218310_, p_218311_);
    }
    public Anger(Level p_218311_) {
        this(PVZEntities.ANGER.get(), p_218311_);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.FLYING_SPEED, 1F)
                .add(Attributes.ATTACK_DAMAGE, 25.0D);
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new AngerLittingGoal(this));
        this.targetSelector.addGoal(1, new DisperseEnemyTargetGoal(this));
    }
    public void tick() {
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
        if (level.isClientSide) {
            Vec3 movement = this.getDeltaMovement();
            level.addParticle(ParticleTypes.LAVA,
                    getX(), getY() + 0.2, getZ(),
                    - movement.x * 0.25 + random.nextFloat() * 0.15 - 0.075,
                    - movement.y * 0.25 + random.nextFloat() * 0.15,
                    - movement.z * 0.25 + random.nextFloat() * 0.15 - 0.075);

            int i = 0;
            while (i < 3) {
                i ++;
                level.addParticle(ParticleTypes.FLAME,
                        getX(), getY() + 0.2, getZ(),
                        - movement.x * 0.25 + random.nextFloat() * 0.15 - 0.075,
                        - movement.y * 0.25 + random.nextFloat() * 0.15,
                        - movement.z * 0.25 + random.nextFloat() * 0.15 - 0.075);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("Life", this.maxLife);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("Life")) {
            maxLife =tag.getInt("Life");
        }
    }

    protected void doPush(Entity entity) {
    }
    @Override
    public boolean fireImmune() {
        return true;
    }

    public class AngerLittingGoal extends Goal {
        private final Anger anger;
        public AngerLittingGoal(Anger anger) {
            this.anger = anger;
        }

        @Override
        public boolean canUse() {
            return true;
        }

        @Override
        public void tick() {
            LivingEntity target = anger.getTarget();
            if (target != null) {
                anger.lookAt(target, 10.0F, 10.0F);
            }
            anger.setDeltaMovement(anger.getLookAngle().normalize().scale(2).add(anger.getDeltaMovement().normalize()).normalize().scale(anger.getAttributeValue(Attributes.FLYING_SPEED)));
            if (anger.tickCount > anger.maxLife || level.getBlockState(this.anger.blockPosition()).isSuffocating(level, anger.blockPosition())) {
                anger.discard();
            }
            List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, anger.getBoundingBox().inflate(1.3, 1.3, 1.3));
            entities.forEach((entity) -> {
                if (! PVZOwnedCapability.isTeammate(anger, entity)) {
                    entity.hurt(DamageSource.ON_FIRE, (float) anger.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
                    entity.setRemainingFireTicks(300);
                } else {
                    entity.setRemainingFireTicks(80);
                }
            });
        }
    }
}
