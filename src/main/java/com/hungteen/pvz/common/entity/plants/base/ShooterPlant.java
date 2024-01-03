package com.hungteen.pvz.common.entity.plants.base;

import com.hungteen.pvz.api.interfaces.IShooter;
import com.hungteen.pvz.common.entity.PVZPlant;
import com.hungteen.pvz.common.entity.ai.goal.PVZNearestTargetGoal;
import com.hungteen.pvz.common.entity.bullet.BaseBullet;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Optional;

public abstract class ShooterPlant extends PVZPlant implements IShooter {

	//use for normal shoot attack animation and shoot goal.
	public static final int SHOOT_ANIM_CD = 10;
	public static final int SHOOT_POINT = SHOOT_ANIM_CD * 3 / 4;
	public static final int SHOOT_POINT_OFFSET = SHOOT_ANIM_CD - SHOOT_POINT;
	
	public ShooterPlant(EntityType<? extends Mob> type, Level worldIn) {
		super(type, worldIn);
	}

	

	public static AttributeSupplier.Builder createAttributes() {
		return PVZPlant.createAttributes()
				.add(Attributes.FOLLOW_RANGE, 15D);
	}
	
	@Override
	protected void registerGoals() {
		super.registerGoals();
	    this.goalSelector.addGoal(0, new ShooterAttackGoal(this));
	    this.addTargetGoals();
	}
	
	protected void addTargetGoals() {
//		this.targetSelector.addGoal(0, new ShooterNearestTargetGoal(this, true, false, getShootRange(), getShootHeight()));
		//TODO make a ShooterTargetGoal extends TargetGoal. make sure use vanilla methods more.
		this.targetSelector.addGoal(0, new NearestAttackableTargetGoal<>(this, Zombie.class, true));
	}


	/**
	 * shoot pea with offsets.
	 */
	public void performShoot(double forwardOffset, double rightOffset, double heightOffset, boolean needSound, double randomAngle) {
		Optional.ofNullable(this.getTarget()).ifPresent(target -> {
		 	//offset
			final Vec3 vec = EntityUtil.getNormalisedVector2d(this, target);
		    final double deltaY = this.getDimensions(getPose()).height * 0.7F + heightOffset;
		    final double deltaX = forwardOffset * vec.x - rightOffset * vec.z;
		    final double deltaZ = forwardOffset * vec.z + rightOffset * vec.x;
		    Projectile bullet = this.createBullet();
		    bullet.setPos(this.getX() + deltaX, this.getY() + deltaY, this.getZ() + deltaZ);
		    //predict
			float speed = this.getBulletSpeed();
			Vec3 targetSpeed = target.getDeltaMovement();
			float time = distanceTo(target) / speed;
			//shoot
		    bullet.shoot(target.getX() + targetSpeed.x * time - bullet.getX(),
					target.getY() + targetSpeed.y * time + target.getEyeHeight() - bullet.getY(),//angle limit move to targeting goals.
					target.getZ() + targetSpeed.z * time - bullet.getZ(),
				    speed, (float) randomAngle);
			if(needSound) {
				EntityUtil.playSound(this, this.getShootSound());
			}
			bullet.setOwner(this);
			if (bullet instanceof BaseBullet bullet1) {
				bullet1.setAttackDamage(this.getAttackDamage());
			}
			this.level.addFreshEntity(bullet);
		});
	}

	protected abstract BaseBullet createBullet();
	
	protected SoundEvent getShootSound() {
		return SoundEvents.SNOW_GOLEM_SHOOT;
	}
	
	protected boolean canAttackNow() {
		return this.getAttackTime(this) <= 0;
	}
	
	/**
	 * get shooter bullet attack damage.
	 */
	public abstract float getAttackDamage();
	

	@Override
	public boolean checkY(Entity target) {
		final double dx = target.getX() - this.getX();
		final double dz = target.getZ() - this.getZ();
		final double minY = target.getY() - this.getY() - this.getEyeHeight();
		final double maxY = minY + target.getBbHeight();
		final double dis = Math.sqrt(dx * dx + dz * dz);
		final double y = dis / getMaxShootAngle();
		return minY < y && maxY > - y;
	}
	
	/**
	 * use to check horizontal shoot path.
	 * {@link #checkY(Entity)}
	 */
	public double getMaxShootAngle() {
		return 12;
	}
	
	/**
	 * max target horizontal distance.
	 */
	public float getShootRange() {
		return 16;
	}
	
	/**
	 * max target height.
	 */
	public float getShootHeight() {
		return 2;
	}
	

	public boolean canShoot() {
		return this.isAlive();
	}
	
	@Override
	public abstract int getShootCD();
	
	@Override
	public float getBulletSpeed() {
		return 1F;
	}
	

	static class ShooterAttackGoal extends Goal {

		protected final ShooterPlant shooter;
		protected LivingEntity target;
		
		public ShooterAttackGoal(ShooterPlant shooter) {
			this.shooter = shooter;
			this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
		}
		
		@Override
		public boolean canUse() {
		//if(! this.shooter.canShoot()) {//can not shoot because of the shooter itself.
		//	this.shooter.setShootTick(0);
		//	return false;
		//}
		//this.target = this.shooter.getTarget();
		//if(! this.checkTarget()) {//can not shoot because of its target.
		//	this.target = null;
		//	this.shooter.setTarget(null);
		//	return false;
		//}
			return true;
		}
		
		@Override
		public boolean canContinueToUse() {
			return this.canUse();
		}
		
		@Override
		public void stop() {

		}

		@Override
		public void tick() {
			if (!this.shooter.isEffectiveAi()) {
				return;
			}
			final int time = this.shooter.getAttackTime(this);
			if (time <= 1) {
				this.shooter.shootBullet();
				this.shooter.setAttackTime(this,this.shooter.getShootCD());
			} else {
				this.shooter.setAttackTime(this,Math.max(0, time - 1));
			}
		}
		
		private boolean checkTarget() {
			if(EntityUtil.checkCanEntityBeAttack(this.shooter, this.target)) {
				return this.shooter.getSensing().hasLineOfSight(this.target);
			}
			return false;
		}
		
	}

	protected static class ShooterNearestTargetGoal extends PVZNearestTargetGoal {

		private final ShooterPlant shooter;

		public ShooterNearestTargetGoal(ShooterPlant mobIn, boolean checkSight, boolean memory, float w, float h) {
			super(mobIn, checkSight, memory, w, h);
			this.shooter = mobIn;
		}

		@Override
		protected boolean checkOther(LivingEntity entity) {
			return super.checkOther(entity) && this.shooter.checkY(entity);
		}

	}
}