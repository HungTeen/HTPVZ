package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.ai.goal.DisperseEnemyTargetGoal;
import com.hungteen.pvz.common.entity.bullet.BaseBullet;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.MathUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.event.entity.living.LivingChangeTargetEvent;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;

public class SplitPea extends PeaShooter {
    protected static final EntityDataAccessor<Byte> TARGET = SynchedEntityData.defineId(SplitPea.class, EntityDataSerializers.BYTE);

    public AnimationState forwardAnimationState = new AnimationState();
    public AnimationState backwardAnimationState = new AnimationState();
    LivingEntity backwardTarget = null;
    int backwardAimTime = 0;
    Vec3 storedBackWardEnemyPos = null;

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
        boolean flag = EntityUtil.isEntityValid(this.getBackTarget()) || this.backwardAnimationState.isStarted();
        return (EntityUtil.isEntityValid(this.getTarget()) || this.forwardAnimationState.isStarted()) ?
                (flag ? Set.of(6, 8, 12) : Set.of(12)) : (flag ? Set.of(6, 8) : Set.of());
    }
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_219422_) {
        super.onSyncedDataUpdated(p_219422_);
        if (POSE.equals(p_219422_)) {
            if (entityData.get(POSE)) {
                this.idleAnimationState.start(this.tickCount);
                if (entityData.get(TARGET) > 1) {
                    this.forwardAnimationState.start(this.tickCount);
                }
                if (entityData.get(TARGET) % 2 != 0) {
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
        if (entity != null && getEntityRelativeAngle(entity) < - 0.5/*120 degrees backward*/) {
            LivingChangeTargetEvent changeTargetEvent = ForgeHooks.onLivingChangeTarget(this, entity, LivingChangeTargetEvent.LivingTargetType.MOB_TARGET);
            if (!changeTargetEvent.isCanceled()) {
                this.backwardTarget = changeTargetEvent.getNewTarget();
            }
        } else if (entity != backwardTarget) {
            super.setTarget(entity);
        }
    }
    public double/*cos of vectors*/ getEntityRelativeAngle(Entity entity) {
        Vec3 vec1 = getViewVector(0);
        Vec3 vec2 = entity.position().subtract(this.position()).multiply(1, 0, 1).normalize();
        PVZMod.LOGGER.info(xRot + "(" + xRotO + ") : " + yRot + "(" + yRotO + ")\n" + getViewVector(0) + "\n" + getLookAngle());
        return vec2.x * vec1.x + vec2.z * vec1.z;
    }
    @Override
    public void tick() {
        super.tick();
        if (EntityUtil.isEntityValid(getBackTarget())) {
            if (storedBackWardEnemyPos == null || backwardAimTime % 50 == 0) {
                storedBackWardEnemyPos = getBackTarget().position();
                backwardAimTime = 0;
            }
            backwardAimTime ++;
        } else {
            backwardAimTime = 0;
        }
    }

    public Vec3 getShootAngle(Entity target, double forwardOffset, double rightOffset, double heightOffset) {
        if (target != null) {
            Vec3 vec = EntityUtil.getNormalisedVector2d(this, target);
            final double deltaY = this.getDimensions(getPose()).height * 0.7F + heightOffset;
            final double deltaX = forwardOffset * vec.x - rightOffset * vec.z;
            final double deltaZ = forwardOffset * vec.z + rightOffset * vec.x;
            Vec3 bulletPos = new Vec3(this.getX() + deltaX, this.getY() + deltaY, this.getZ() + deltaZ);
            double speed = this.getBulletSpeed();
            Vec3 deltaPos;
            Vec3 targetSpeed;
            if (target == this.getTarget()) {
                if (storedEnemyPos != null) {
                    targetSpeed = target.position().subtract(storedEnemyPos)
                            .multiply(1 / (float) aimTime, 1 / (float) aimTime, 1 / (float) aimTime);
                } else {
                    targetSpeed = target.getDeltaMovement();
                }
            } else {
                if (storedBackWardEnemyPos != null) {
                    targetSpeed = target.position().subtract(storedBackWardEnemyPos)
                            .multiply(1 / (float) backwardAimTime, 1 / (float) backwardAimTime, 1 / (float) backwardAimTime);
                } else {
                    targetSpeed = target.getDeltaMovement();
                }
            }
            int time = (int) Math.round(distanceTo(target) / speed);
            deltaPos = new Vec3(target.getX() + targetSpeed.x * time - bulletPos.x,
                    target.getY() + targetSpeed.y * time + target.getBbHeight() / 2 - bulletPos.y,//angle limit move to targeting goals.
                    target.getZ() + targetSpeed.z * time - bulletPos.z);
            for (int tmp = 0; tmp < 3; tmp ++) {
                //recurse to increase accuracy.
                time = (int) Math.round(Math.sqrt(deltaPos.x * deltaPos.x + deltaPos.y * deltaPos.y + deltaPos.z * deltaPos.z) / speed);
                deltaPos = new Vec3(target.getX() + targetSpeed.x * time - bulletPos.x,
                        target.getY() + targetSpeed.y * time + target.getBbHeight() / 2 - bulletPos.y,
                        target.getZ() + targetSpeed.z * time - bulletPos.z);
            }
            return deltaPos;
        } else if (this.getAttackTime() > 10) {
            if (storedEnemyPos != null) {
                return storedEnemyPos.add(0, 1, 0).subtract(this.position().add(0, this.getEyeHeight(), 0));
            } else {
                return this.getViewVector(0).normalize();
            }
        } else if (storedBackWardEnemyPos != null && this.getAttackTime() <= 10) {
            return storedBackWardEnemyPos.add(0, 1, 0).subtract(this.position().add(0, this.getEyeHeight(), 0));
        } else {
            return this.getViewVector(0).scale(-1);
        }
    }
    @Override
    public @Nullable Projectile performShoot(double forwardOffset, double rightOffset, double heightOffset, boolean needSound, double randomAngle) {
        if (getAttackTime() > 10) {
            return super.performShoot(forwardOffset, rightOffset, heightOffset, needSound, randomAngle);
        } else {
            LivingEntity target = this.getBackTarget();
            //create bullet
            Vec3 deltaPos = getShootAngle(target, forwardOffset, rightOffset, heightOffset);
            Vec3 normalized = deltaPos.normalize();
            final double deltaY = this.getDimensions(getPose()).height * 0.7F + heightOffset;
            final double deltaX = forwardOffset * normalized.x - rightOffset * normalized.z;
            final double deltaZ = forwardOffset * normalized.z + rightOffset * normalized.x;
            Projectile bullet = this.createBullet();
            bullet.setPos(this.getX() + deltaX, this.getY() + deltaY, this.getZ() + deltaZ);
            double horizontal = Math.sqrt(deltaPos.x * deltaPos.x + deltaPos.z * deltaPos.z);
            double vertical = deltaPos.y;
            if (vertical > horizontal * getMaxShootAngleTangent()) {
                deltaPos = new Vec3(deltaPos.x, horizontal * getMaxShootAngleTangent(), deltaPos.z);
            } else if (vertical < - horizontal * getMaxShootAngleTangent()) {
                deltaPos = new Vec3(deltaPos.x, - horizontal * getMaxShootAngleTangent(), deltaPos.z);
            }
            if (MathUtil.horizontalDistSqrOf(deltaPos) == 0) {
                deltaPos = new Vec3(this.random.nextFloat(), 0, this.random.nextFloat());
            }
            //shoot
            bullet.shoot(deltaPos.x, deltaPos.y, deltaPos.z, getBulletSpeed(), (float) randomAngle);
            if (needSound) {
                EntityUtil.playSound(this, this.getShootSound());
            }
            bullet.setOwner(this);
            if (bullet instanceof BaseBullet bullet1) {
                bullet1.setAttackDamage(this.getAttackDamage());
            }
            this.level.addFreshEntity(bullet);
            return bullet;
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
            if (! DisperseEnemyTargetGoal.getDefaultPredicate(splitPea).and(splitPea::isHeightAvailable).test(splitPea.backwardTarget)) {
                splitPea.backwardTarget = null;
            } else {
                if (splitPea.getEntityRelativeAngle(splitPea.getBackTarget()) > -0.5) {
                    splitPea.backwardTarget = null;
                } else {
                    target += 1;
                }
            }
            if (EntityUtil.isEntityValid(splitPea.getTarget())) {
                if (splitPea.getEntityRelativeAngle(splitPea.getTarget()) < 0) {
                    splitPea.setTarget(null);
                } else {
                    target += 2;
                }
            } else {
                splitPea.setTarget(null);
            }
            this.splitPea.entityData.set(TARGET, target);
            return this.splitPea.backwardTarget != null ^ this.splitPea.getTarget() != null;
        }

        @Override
        public void tick() {
            double range = this.splitPea.getAttribute(Attributes.FOLLOW_RANGE).getValue();
            Vec3 vec = this.splitPea.getViewVector(0).multiply(1, 0, 1).normalize();
            AABB area = this.splitPea.getBoundingBox().inflate(range / 2, 4.0, range / 2);
            if (this.splitPea.backwardTarget != null) {
                area = area.move(vec.x * range / 2, 0, vec.z * range / 2);
            } else {
                area = area.move(- vec.x * range / 2, 0, - vec.z * range / 2);
            }
            LivingEntity target = this.splitPea.level.getNearestEntity(this.splitPea.level.getEntitiesOfClass(LivingEntity.class, area,
                    (entity) -> EntityUtil.checkCanEntityBeAttack(splitPea, entity)), TargetingConditions.forCombat().range(range).selector(null),
                    this.splitPea, this.splitPea.getX(), this.splitPea.getEyeY(), this.splitPea.getZ());
            if (EntityUtil.isEntityValid(target)) {
                if (this.splitPea.backwardTarget != null && splitPea.getEntityRelativeAngle(target) > 0) {
                    this.splitPea.setTarget(target);
                } else if (this.splitPea.getTarget() != null && splitPea.getEntityRelativeAngle(target) < -0.5) {
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
            }
            shooter.getEntityData().set(POSE, (this.shooter.getAttackTime() < this.shooter.shootAnimLength()));
            //can shoot.
            return this.shooter.canShoot();
        }

        @Override
        public void tick() {
            int time = this.shooter.getAttackTime();
            if (this.shooter.shootTimes().contains(time)) {
                this.shooter.shootBullet();
                if (time > 10) {
                    if (EntityUtil.isEntityValid(this.shooter.getTarget())) {
                        shooter.aimTime = 0;
                        shooter.storedEnemyPos = this.shooter.getTarget().position();
                    }
                } else if (EntityUtil.isEntityValid(((SplitPea) this.shooter).getBackTarget())) {
                    ((SplitPea) shooter).backwardAimTime = 0;
                    ((SplitPea) shooter).storedBackWardEnemyPos = ((SplitPea) this.shooter).getBackTarget().position();
                }
            }
        }
    }
}
