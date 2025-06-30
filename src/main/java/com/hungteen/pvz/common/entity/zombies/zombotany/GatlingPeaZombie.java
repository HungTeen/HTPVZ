package com.hungteen.pvz.common.entity.zombies.zombotany;

import com.hungteen.pvz.common.entity.bullet.BaseBullet;
import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.MathUtil;
import com.hungteen.pvz.util.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.phys.Vec3;
import java.util.Set;
import java.util.UUID;

public class GatlingPeaZombie extends AbstractZombotanyZombie {
    protected static final EntityDataAccessor<Boolean> POSE = SynchedEntityData.defineId(GatlingPeaZombie.class, EntityDataSerializers.BOOLEAN);
    protected static final double SHOOT_OFFSET = -0.3D;
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState shootAnimationState = new AnimationState();
    public final AnimationState controlledAnimationState = new AnimationState();
    private boolean playerFire = false;
    public Vec3 storedEnemyPos = null;
    public int aimTime = 0;
    private static final EntityDataAccessor<Integer> ATTACK_TIME = SynchedEntityData.defineId(GatlingPeaZombie.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> OVERHEATING = SynchedEntityData.defineId(GatlingPeaZombie.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> FUSING = SynchedEntityData.defineId(GatlingPeaZombie.class, EntityDataSerializers.BOOLEAN);
    public static int MAX_OVERHEAT = 750;

    public GatlingPeaZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACK_TIME, 0);
        this.entityData.define(POSE, false);
        this.entityData.define(OVERHEATING, 0);
        this.entityData.define(FUSING, false);
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
        if (this.level.isClientSide()) {
            this.idleAnimationState.startIfStopped(this.tickCount);
        }
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
        if (this.getOverheat() > 0) {
            this.setOverheat(this.getOverheat() - (this.getOverheat() < MAX_OVERHEAT * 0.67 && !entityData.get(FUSING) ? 2 : 1));
        } else {
            this.entityData.set(FUSING, false);
        }
    }

    public int getOverheat() {
        return entityData.get(OVERHEATING);
    }
    public void setOverheat(int value) {
        entityData.set(OVERHEATING, value);
    }
    public boolean getFusing() {
        return entityData.get(FUSING);
    }
    public void setFusing(boolean value) {
        entityData.set(FUSING, value);
    }

    public Set<Integer> shootTimes() {
        return Set.of(8, 10, 12, 14);
    }

    public int shootAnimLength() {
        return 20;
    }

    public boolean canShoot() {
        return this.isAlive() && !entityData.get(FUSING);
    }

    public int getShootCD() {
        return 40;
    }

    public float getBulletSpeed() {
        return 1.5F;
    }

    public int getAttackTime() {
        return entityData.get(ATTACK_TIME);
    }
    public void setAttackTime(int cd) {
        entityData.set(ATTACK_TIME, cd);
    }

    public void shootBullet() {
        this.performShoot(SHOOT_OFFSET, 0, -0.2F, true, this.getOverheat() > MAX_OVERHEAT * 0.67 ? (this.getOverheat() - MAX_OVERHEAT * 0.67) / 25 : 0);
        this.setOverheat(this.getOverheat() + 12);
        if (getOverheat() > MAX_OVERHEAT && !this.entityData.get(FUSING)) {
            this.entityData.set(FUSING, true);
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
            if (storedEnemyPos != null && aimTime > 0) {
                targetSpeed = target.position().subtract(storedEnemyPos)
                        .multiply(1 / (float) aimTime, 1 / (float) aimTime, 1 / (float) aimTime);
            } else {
                targetSpeed = target.getDeltaMovement().add(0, 0.1, 0);
            }
            int time = (int) Math.round(distanceTo(target) / speed);
            deltaPos = new Vec3(target.getX() + targetSpeed.x * time - bulletPos.x,
                    target.getY() + targetSpeed.y * time + target.getBbHeight() / 2 - bulletPos.y,
                    target.getZ() + targetSpeed.z * time - bulletPos.z);
            for (int tmp = 0; tmp < 3; tmp++) {
                time = (int) Math.round(Math.sqrt(deltaPos.x * deltaPos.x + deltaPos.y * deltaPos.y + deltaPos.z * deltaPos.z) / speed);
                deltaPos = new Vec3(target.getX() + targetSpeed.x * time - bulletPos.x,
                        target.getY() + targetSpeed.y * time + target.getBbHeight() / 2 - bulletPos.y,
                        target.getZ() + targetSpeed.z * time - bulletPos.z);
            }
            return deltaPos;
        } else {
            return this.getViewVector(0).normalize();
        }
    }

    public Projectile performShoot(double forwardOffset, double rightOffset, double heightOffset, boolean needSound, double randomAngle) {
        LivingEntity target = this.getTarget();
        Vec3 deltaPos = getShootAngle(target, forwardOffset, rightOffset, heightOffset);
        Vec3 normalized = deltaPos.normalize();
        final double deltaY = this.getDimensions(getPose()).height * 0.7F + heightOffset;
        final double deltaX = forwardOffset * normalized.x - rightOffset * normalized.z;
        final double deltaZ = forwardOffset * normalized.z + rightOffset * normalized.x;
        Projectile bullet = this.createBullet();
        bullet.setPos(this.getX() + deltaX, this.getY() + deltaY, this.getZ() + deltaZ);
        double horizontal = Math.sqrt(deltaPos.x * deltaPos.x + deltaPos.z * deltaPos.z);
        double vertical = deltaPos.y;
        if (vertical > horizontal * 0.15) {
            deltaPos = new Vec3(deltaPos.x, horizontal * 0.15, deltaPos.z);
        } else if (vertical < -horizontal * 0.15) {
            deltaPos = new Vec3(deltaPos.x, -horizontal * 0.15, deltaPos.z);
        }
        if (MathUtil.horizontalDistSqrOf(deltaPos) == 0) {
            deltaPos = new Vec3(this.random.nextFloat(), 0, this.random.nextFloat());
        }
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

    protected PeaBullet createBullet() {
        PeaBullet bullet = new PeaBullet(this.level, this, PeaBullet.PeaType.Common);
        return bullet;
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

    @Override
    public ResourceLocation getPlantTextureLocation() {
        return Util.prefix("textures/entity/plants/gatling_pea/gatling_pea.png");
    }

    @Override
    public String getPlantModelClassName() {
        return "com.hungteen.pvz.client.model.plants.GatlingPeaModel";
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
            case "controlled" -> this.controlledAnimationState;
            default -> null;
        };
    }
} 