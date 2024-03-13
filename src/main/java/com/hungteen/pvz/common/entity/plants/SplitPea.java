package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.bullet.BaseBullet;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;
import java.util.Set;

public class SplitPea extends PeaShooter{
    protected static final EntityDataAccessor<Byte> TARGET = SynchedEntityData.defineId(SplitPea.class, EntityDataSerializers.BYTE);

    public AnimationState forwardAnimationState = new AnimationState();
    public AnimationState backwardAnimationState = new AnimationState();
    LivingEntity backwardTarget = null;
    public static List<Skill> staticSkillList = List.of(
);
    public SplitPea(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TARGET, (byte) 0);
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.removeGoal(shooterAttackGoal);
        this.goalSelector.addGoal(1, new SplitPeaAttackGoal(this));
        this.targetSelector.addGoal(1, new SplitPeaHandleTargetGoal(this));
    }
    public LivingEntity getBackTarget() {
        return backwardTarget;
    }
    public Set<Integer> shootTimes() {
        return Set.of(6, 8, 12);
    }
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_219422_) {
        super.onSyncedDataUpdated(p_219422_);
        if (POSE.equals(p_219422_)) {
            if (entityData.get(POSE)) {
                this.idleAnimationState.start(this.tickCount);
                if (entityData.get(TARGET) % 2 == 0) {
                    this.forwardAnimationState.start(this.tickCount);
                } else if (entityData.get(TARGET) < 2) {
                    this.backwardAnimationState.start(this.tickCount);
                }
            } else {
                this.forwardAnimationState.stop();
                this.backwardAnimationState.stop();
            }
        }
    }
    @Override
    public void setTarget(LivingEntity entity) {
        if (entity != null) {
            Vec3 vec1 = this.getLookAngle();
            Vec3 vec2 = entity.position().subtract(this.position());
            if (vec2.x * vec1.x + vec2.y * vec1.y + vec2.z * vec1.z <= 0) {
                if (this.backwardTarget == null) {
                    this.backwardTarget = entity;
                }
            } else if (entity != backwardTarget) {
                super.setTarget(entity);
            }
        }
    }

    @Override
    public void performShoot(double forwardOffset, double rightOffset, double heightOffset, boolean needSound, double randomAngle) {
        if (getAttackTime() > 10) {
            if (EntityUtil.isEntityValid(getTarget())) {
                super.performShoot(forwardOffset, rightOffset, heightOffset, needSound, randomAngle);
            }
        } else {
            LivingEntity target = this.getBackTarget();
            if (EntityUtil.isEntityValid(target)) {
                //create bullet
                final Vec3 vec = EntityUtil.getNormalisedVector2d(this, target);
                final double deltaY = this.getDimensions(getPose()).height * 0.7F + heightOffset;
                final double deltaX = forwardOffset * vec.x - rightOffset * vec.z;
                final double deltaZ = forwardOffset * vec.z + rightOffset * vec.x;
                Projectile bullet = this.createBullet();
                bullet.setPos(this.getX() + deltaX, this.getY() + deltaY, this.getZ() + deltaZ);
                //predict
                float speed = this.getBulletSpeed();
                Vec3 deltaPos;
                Vec3 targetSpeed = target.getDeltaMovement().add(0, 0.08, 0);
                int time = Math.round(distanceTo(target) / speed);
                deltaPos = new Vec3(target.getX() + targetSpeed.x * time - bullet.getX(),
                        target.getY() + targetSpeed.y * time + target.getBbHeight() / 2 - bullet.getY(),//angle limit move to targeting goals.
                        target.getZ() + targetSpeed.z * time - bullet.getZ());
                for (int tmp = 0; tmp < 3; tmp ++) {
                    //recurse to increase accuracy.
                    time = (int) Math.round(Math.sqrt(deltaPos.x * deltaPos.x + deltaPos.y * deltaPos.y + deltaPos.z * deltaPos.z) / speed);
                    deltaPos = new Vec3(target.getX() + targetSpeed.x * time - bullet.getX(),
                            target.getY() + targetSpeed.y * time + target.getBbHeight() / 2 - bullet.getY(),
                            target.getZ() + targetSpeed.z * time - bullet.getZ());
                }
                double horizontal = Math.sqrt(deltaPos.x * deltaPos.x + deltaPos.z * deltaPos.z);
                double vertical = deltaPos.y;
                if (vertical > horizontal * getMaxShootAngleTangent()) {
                    deltaPos = new Vec3 (deltaPos.x, horizontal * getMaxShootAngleTangent(), deltaPos.z);
                } else if (vertical < - horizontal * getMaxShootAngleTangent()) {
                    deltaPos = new Vec3 (deltaPos.x, - horizontal * getMaxShootAngleTangent(), deltaPos.z);
                }
                //shoot
                bullet.shoot(deltaPos.x, deltaPos.y, deltaPos.z, speed, (float) randomAngle);
                if (needSound) {
                    EntityUtil.playSound(this, this.getShootSound());
                }
                bullet.setOwner(this);
                if (bullet instanceof BaseBullet bullet1) {
                    bullet1.setAttackDamage(this.getAttackDamage());
                }
                this.level.addFreshEntity(bullet);
            }
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (this.backwardTarget != null) {
            tag.putUUID("BackwardTarget", backwardTarget.getUUID());
        }
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("BackwardTarget")) {
            Entity entity = ((ServerLevel) this.level).getEntity(tag.getUUID("BackwardTarget"));
            if (entity instanceof LivingEntity entity1) {
                backwardTarget = entity1;
            }
        }
    }

    private static class SplitPeaHandleTargetGoal extends Goal {
        SplitPea splitPea;
        public SplitPeaHandleTargetGoal(SplitPea splitPea) {
            this.splitPea = splitPea;
        }

        @Override
        public boolean canUse() {
            byte target = 0;
            if (! EntityUtil.isEntityValid(splitPea.backwardTarget)) {
                splitPea.backwardTarget = null;
            } else {
                Vec3 vec1 = splitPea.getLookAngle();
                Vec3 vec2 = splitPea.getBackTarget().position().subtract(splitPea.position());
                if (vec2.x * vec1.x + vec2.y * vec1.y + vec2.z * vec1.z > 0) {
                    splitPea.backwardTarget = null;
                } else {
                    target += 1;
                }
            }
            if (EntityUtil.isEntityValid(splitPea.getTarget())) {
                Vec3 vec1 = splitPea.getLookAngle();
                Vec3 vec2 = splitPea.getTarget().position().subtract(splitPea.position());
                if (vec2.x * vec1.x + vec2.y * vec1.y + vec2.z * vec1.z <= 0) {
                    splitPea.setTarget(null);
                } else {
                    target += 2;
                }
            }
            this.splitPea.entityData.set(TARGET, target);
            return this.splitPea.backwardTarget != null ^ this.splitPea.getTarget() != null;
        }

        @Override
        public void tick() {
            double range = this.splitPea.getAttribute(Attributes.FOLLOW_RANGE).getValue();
            Vec3 vec = this.splitPea.getLookAngle().normalize();
            AABB area = this.splitPea.getBoundingBox().inflate(range / 2, 4.0, range / 2);
            if (this.splitPea.backwardTarget != null) {
                area = area.move(vec.x * range, vec.y * range, vec.z * range);
            } else {
                area = area.move(- vec.x * range, - vec.y * range, - vec.z * range);
            }
            LivingEntity target = this.splitPea.level.getNearestEntity(this.splitPea.level.getEntitiesOfClass(LivingEntity.class, area,
                    (entity) -> EntityUtil.checkCanEntityBeAttack(splitPea, entity)), TargetingConditions.forCombat().range(range).selector(null),
                    this.splitPea, this.splitPea.getX(), this.splitPea.getEyeY(), this.splitPea.getZ());
            if (EntityUtil.isEntityValid(target)) {
                if (this.splitPea.backwardTarget != null) {
                    this.splitPea.setTarget(target);
                } else {
                    this.splitPea.backwardTarget = target;
                }
            }
        }
    }

    public static class SplitPeaAttackGoal extends ShooterAttackGoal {
        public SplitPeaAttackGoal(SplitPea shooter) {
            super(shooter);
        }
        @Override
        public boolean canUse() {
            //looking control.
            LivingEntity target = this.shooter.getTarget();
            LivingEntity backTarget = ((SplitPea) this.shooter).getBackTarget();
            if (EntityUtil.isEntityValid(backTarget) && ! EntityUtil.isEntityValid(target)) {
                this.shooter.getLookControl().setLookAt(2 * shooter.getX() - backTarget.getX(),
                        2 * shooter.getY() - backTarget.getY(),
                        2 * shooter.getZ() - backTarget.getZ());
            } else if (EntityUtil.isEntityValid(target)) {
                this.shooter.getLookControl().setLookAt(target.getX(), target.getY(), target.getZ());
            }
            //countdown.
            final int time = this.shooter.getAttackTime();
            if (time != this.shooter.shootAnimLength() || (this.shooter.canShoot() && (EntityUtil.isEntityValid(target) || EntityUtil.isEntityValid(backTarget)))) {
                this.shooter.setAttackTime(time > 0 ? time - 1 : this.shooter.getShootCD());
                if (time <= 1 && EntityUtil.isEntityValid(target)) {
                    shooter.aimTime = 0;
                    shooter.storedEnemyPos = target.position();
                }
            }
            shooter.getEntityData().set(POSE, (this.shooter.getAttackTime() < this.shooter.shootAnimLength()));
            //can shoot.
            return this.shooter.canShoot();
        }
    }
}
