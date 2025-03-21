package com.hungteen.pvz.common.entity.plants.base;

import com.hungteen.pvz.api.interfaces.IShooter;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.ShooterTargetGoal;
import com.hungteen.pvz.common.entity.bullet.BaseBullet;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.MathUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.Set;

public abstract class ShooterPlant extends SimplePlant implements IShooter {
	public Vec3 storedEnemyPos = null;
	public int aimTime = 0;
	public AnimationState idleAnimationState = new AnimationState();
	public AnimationState shootAnimationState = new AnimationState();
	protected static final EntityDataAccessor<Boolean> POSE = SynchedEntityData.defineId(ShooterPlant.class, EntityDataSerializers.BOOLEAN);
	protected ShooterAttackGoal shooterAttackGoal;
	protected TargetGoal targetGoal;

	public ShooterPlant(EntityType<? extends Mob> type, Level worldIn) {
		super(type, worldIn);
		this.setAttackTime(40);
	}

	@Override
	protected void registerGoals() {
		super.registerGoals();
		this.shooterAttackGoal = new ShooterAttackGoal(this);
		this.goalSelector.addGoal(1, shooterAttackGoal);
		this.goalSelector.addGoal(1, new AttractEnemyGoal(this, () -> this.getFirstPassenger() == null, 1));
		this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 6.0F));
		this.goalSelector.addGoal(2, new RandomLookAroundGoal(this));

		this.targetGoal = new ShooterTargetGoal(this);
		this.targetSelector.addGoal(1, targetGoal);
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
			EntityUtil.playSound(this, this.getShootSound());
		}
		bullet.setOwner(this);
		if (bullet instanceof BaseBullet bullet1) {
			bullet1.setAttackDamage(this.getAttackDamage());
		}
		this.level.addFreshEntity(bullet);
		return bullet;
	}

	@Override
	public void tick() {
		super.tick();
		if (EntityUtil.isEntityValid(getTarget())) {
			if (storedEnemyPos == null || aimTime % 50 == 0) {
				storedEnemyPos = getTarget().position();
				aimTime = 0;
			}
			aimTime ++;
		} else {
			aimTime = 0;
		}
	}

	//animate related.
	@Override
	protected void defineSynchedData() {
		super.defineSynchedData();
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

	public void setupPresentationAnim() {
		this.idleAnimationState.start(this.tickCount);
	}

	protected abstract Projectile createBullet();

	protected SoundEvent getShootSound() {
		return SoundEvents.SNOW_GOLEM_SHOOT;
	}

	/**
	 * get shooter bullet attack damage.
	 */
	public abstract float getAttackDamage();


	@Override
	public boolean isHeightAvailable(Entity target) {
		final double dx = target.getX() - this.getX();
		final double dz = target.getZ() - this.getZ();
		final double minY = target.getY() - this.getY() - this.getEyeHeight();
		final double maxY = minY + target.getBbHeight();
		final double dis = Math.sqrt(dx * dx + dz * dz);
		final double y = dis * getMaxShootAngleTangent();
		return minY < y && maxY > - y;
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
				targetSpeed = target.getDeltaMovement().add(0, 0.08/*minus gravity*/, 0);
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
			return this.getLookAngle().normalize();
		}
	}

	public boolean canShoot() {
		return this.isAlive();
	}

	@Override
	public abstract int getShootCD();

	public Set<Integer> shootTimes() {
		return Set.of(10);
	}

	public int shootAnimLength() {
		return 20;
	}

	@Override
	public float getBulletSpeed() {
		return 1F;
	}


	public static class ShooterAttackGoal extends Goal {

		protected final ShooterPlant shooter;

		public ShooterAttackGoal(ShooterPlant shooter) {
			this.shooter = shooter;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}

		@Override
		public boolean canUse() {
			//looking control.
			LivingEntity target = this.shooter.getTarget();
			if (EntityUtil.isEntityValid(target)) {
				this.shooter.getLookControl().setLookAt(target.getX(), target.getY(), target.getZ());
			}
			//countdown.
			final int time = this.shooter.getAttackTime();
			if (time != this.shooter.shootAnimLength() || (this.shooter.canShoot() && EntityUtil.isEntityValid(target))) {
				this.shooter.setAttackTime(time > 0 ? time - 1 : this.shooter.getShootCD());
			}
			shooter.entityData.set(POSE, (this.shooter.getAttackTime() < this.shooter.shootAnimLength()));
			//can shoot.
			return this.shooter.canShoot();
		}

		@Override
		public boolean canContinueToUse() {
			return this.canUse();
		}

		@Override
		public void tick() {
			int time = this.shooter.getAttackTime();
			if (this.shooter.shootTimes().contains(time)) {
				this.shooter.shootBullet();
				if (EntityUtil.isEntityValid(this.shooter.getTarget())) {
					shooter.aimTime = 0;
					shooter.storedEnemyPos = this.shooter.getTarget().position();
				}
			}
		}
	}
}