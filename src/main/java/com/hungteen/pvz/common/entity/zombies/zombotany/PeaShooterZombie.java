package com.hungteen.pvz.common.entity.zombies.zombotany;

import com.hungteen.pvz.api.interfaces.IShooter;
import com.hungteen.pvz.common.entity.bullet.BaseBullet;
import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.hungteen.pvz.util.MathUtil;
import com.hungteen.pvz.util.Util;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;
import java.util.UUID;

public class PeaShooterZombie extends AbstractZombotanyZombie implements IShooter {

    protected static final UUID ATTRIBUTE_MODIFIER_UUID = UUID.fromString("fa202025-b0e7-65AE-8bc3-546a895a193d");
    protected static final EntityDataAccessor<Boolean> POSE = SynchedEntityData.defineId(PeaShooterZombie.class, EntityDataSerializers.BOOLEAN);
    protected static final double SHOOT_OFFSET = -0.3D;
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState shootAnimationState = new AnimationState();
    private boolean isInSniperMode = false;
    public Vec3 storedEnemyPos = null;
    public int aimTime = 0;

    private static final EntityDataAccessor<Integer> ATTACK_TIME = SynchedEntityData.defineId(PeaShooterZombie.class, EntityDataSerializers.INT);


    public PeaShooterZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }


    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACK_TIME, 0);
        this.entityData.define(POSE, false);
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_219422_) {
        if (POSE.equals(p_219422_)) {
            if (entityData.get(POSE)) {
                this.idleAnimationState.stop();
                this.shootAnimationState.start(this.tickCount);
            } else {
                this.shootAnimationState.stop();
                this.idleAnimationState.start(this.tickCount);
            }
        }
        super.onSyncedDataUpdated(p_219422_);
    }

    @Override
    public void tick() {
        super.tick();
        
        // 更新动画状态
        if (this.level.isClientSide()) {
            // 空闲动画始终播放
            this.idleAnimationState.startIfStopped(this.tickCount);
        }

        // 更新目标位置
        if (EntityUtil.isEntityValid(getTarget())) {
            if (storedEnemyPos == null || aimTime % 50 == 0) {
                storedEnemyPos = getTarget().position();
                aimTime = 0;
            }
            aimTime++;
        } else {
            aimTime = 0;
        }
        this.shooterAttackGoalTick();
    }

    /**
     * use to check horizontal shoot path.
     * {@link #isHeightAvailable(Entity)}
     */
    public double getMaxShootAngleTangent() {
        return 0.15;
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
            if (storedEnemyPos != null && aimTime > 0) {
                targetSpeed = target.position().subtract(storedEnemyPos)
                        .multiply(1 / (float) aimTime, 1 / (float) aimTime, 1 / (float) aimTime);
            } else {
                targetSpeed = target.getDeltaMovement().add(0, 0.1/*minus gravity*/, 0);
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
        } else if (storedEnemyPos != null) {
            return storedEnemyPos.add(0, 1, 0).subtract(this.position().add(0, this.getEyeHeight(), 0));
        } else {
            return this.getViewVector(0).normalize();
        }
    }

    /**
     * shoot pea with offsets.
     */
    public @Nullable Projectile performShoot(double forwardOffset, double rightOffset, double heightOffset, boolean needSound, double randomAngle) {
        LivingEntity target = this.getTarget();
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
            EntityUtil.playSound(this, SoundEvents.SNOW_GOLEM_SHOOT);
        }
        bullet.setOwner(this);
        BaseBullet bullet1 = (BaseBullet) bullet;
        bullet1.setAttackDamage((float) this.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
        this.level.addFreshEntity(bullet);
        return bullet;
    }

    @Override
    public void shootBullet() {
        this.performShoot(SHOOT_OFFSET, 0, -0.2F, true, 0);
    }


    protected PeaBullet createBullet() {
        PeaBullet bullet = new PeaBullet(this.level, this, PeaBullet.PeaType.Common);
        return bullet;
    }

    public void setSniperMode(boolean sniperMode) {
        if (this.isInSniperMode != sniperMode) {
            this.isInSniperMode = sniperMode;
            // 更新属性
            if (this.isInSniperMode) {
                this.getAttribute(Attributes.FOLLOW_RANGE).addTransientModifier(
                    new AttributeModifier(ATTRIBUTE_MODIFIER_UUID, "sniper bonus", 36, AttributeModifier.Operation.ADDITION)
                );
            } else {
                this.getAttribute(Attributes.FOLLOW_RANGE).removeModifier(ATTRIBUTE_MODIFIER_UUID);
            }
        }
    }

    @Override
    public ResourceLocation getPlantTextureLocation() {
        return Util.prefix("textures/entity/plants/pea_shooter/pea_shooter.png");
    }

    @Override
    public String getPlantModelClassName() {
        return "com.hungteen.pvz.client.model.plants.PeaShooterModel";
    }

    @Override
    public float getPlantHeadScale() {
        return 1.0F;
    }

    @Override
    public float getPlantHeadOffsetY() {
        return 1.1F;
    }

    public AnimationState getAnimationState(String name) {
        return switch (name) {
            case "idle" -> this.idleAnimationState;
            case "shoot" -> this.shootAnimationState;
            default -> null;
        };
    }

    public Set<Integer> shootTimes() {
        return Set.of(10);
    }

    public int shootAnimLength() {
        return 20;
    }

    public boolean canShoot() {
        return this.isAlive();
    }

    public int getShootCD() {
        return this.isInSniperMode ? 160 : 40;
    }

    public float getBulletSpeed() {
        return (this.isInSniperMode ? 4F : 1F);
    }

    @Override
    public boolean isHeightAvailable(Entity target) {
        return false;
    }


    public int getAttackTime() {
        return entityData.get(ATTACK_TIME);
    }

    public void setAttackTime(int cd) {
        entityData.set(ATTACK_TIME, cd);
    }


    protected void shooterAttackGoalTick() {

        LivingEntity target = this.getTarget();
        if (this.canShoot() && EntityUtil.isEntityValid(target)) {
            int time = this.getAttackTime();
            if (time > 0) {
                this.setAttackTime(time - 1);
            } else {
                this.setAttackTime(this.getShootCD());
            }

            this.entityData.set(POSE, (this.getAttackTime() < this.shootAnimLength()));

            if (this.shootTimes().contains(this.getAttackTime())) {
                this.shootBullet();
                this.aimTime = 0;
                this.storedEnemyPos = target.position();
            }
        }
    }
} 