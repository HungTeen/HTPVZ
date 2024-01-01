package com.hungteen.pvz.common.entity.plants.base;

import com.hungteen.pvz.api.interfaces.IShooter;
import com.hungteen.pvz.common.entity.PVZPlant;
import com.hungteen.pvz.common.entity.ai.goal.PVZNearestTargetGoal;
import com.hungteen.pvz.common.entity.bullet.AbstractBulletEntity;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.Optional;

public abstract class PlantShooterEntity extends PVZPlant implements IShooter {

	//use for normal shoot attack animation and shoot goal.
	public static final float FORWARD_SHOOT_ANGLE = 0;
	public static final float BACK_SHOOT_ANGLE = 180;
	public static final float FORWARD_LEFT_SHOOT_ANGLE = -7.5F;
	public static final float FORWARD_RIGHT_SHOOT_ANGLE = 7.5F;
	public static final int SHOOT_ANIM_CD = 10;
	public static final int SHOOT_POINT = SHOOT_ANIM_CD * 3 / 4;
	public static final int SHOOT_POINT_OFFSET = SHOOT_ANIM_CD - SHOOT_POINT;
	
	public PlantShooterEntity(EntityType<? extends Mob> type, Level worldIn) {
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
		this.targetSelector.addGoal(0, new ShooterNearestTargetGoal(this, true, false, getShootRange(), getShootHeight()));
	}


	/**
	 * shoot pea with offsets.
	 */
	public void performShoot(double forwardOffset, double rightOffset, double heightOffset, boolean needSound, double angleOffset) {
		Optional.ofNullable(this.getTarget()).ifPresent(target -> {
		 final Vec3 vec = EntityUtil.getNormalisedVector2d(this, target);
           final double deltaY = this.getDimensions(getPose()).height * 0.7F + heightOffset;
           final double deltaX = forwardOffset * vec.x - rightOffset * vec.z;
           final double deltaZ = forwardOffset * vec.z + rightOffset * vec.x;
            AbstractBulletEntity bullet = this.createBullet();
           bullet.setPos(this.getX() + deltaX, this.getY() + deltaY, this.getZ() + deltaZ);
           bullet.shootPea(target.getX() - bullet.getX(), target.getY() + target.getBbHeight() - bullet.getY(), target.getZ() - bullet.getZ(), this.getBulletSpeed(), angleOffset);
            if(needSound) {
            	EntityUtil.playSound(this, this.getShootSound());
            }
            //bullet.setOwner(this);

           // bullet.setAttackDamage(this.getAttackDamage());
            this.level.addFreshEntity(bullet);
		});
	}
	
	/**
	 * shoot pea by angle.
	 */
//public void shootByAngle(float angle, float height) {
//	angle *= 3.14159F / 180F;
//	final double vx = - Mth.sin(angle);
//	final double vz = Mth.cos(angle);
//	final Arrow bullet = this.createBullet();
//	bullet.setPos(getX(), getY() + height, getZ());
//	bullet.setDeltaMovement(vx * this.getBulletSpeed(), 0, vz * this.getBulletSpeed());
//	bullet.setOwner(this);
   //  //  bullet.setAttackDamage(this.getAttackDamage());
//	level.addFreshEntity(bullet);
//}

	protected abstract AbstractBulletEntity createBullet();
	
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
	 * {@link AbstractBulletEntity#getShootPeaAngle()}
	 */
	public double getMaxShootAngle() {
		return 12;
	}
	
	/**
	 * max target horizontal distance.
	 */
	public float getShootRange() {
		return 45;
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
		return 1.5F;
	}
	

	static class ShooterAttackGoal extends Goal {

		protected final PlantShooterEntity shooter;
		protected LivingEntity target;
		
		public ShooterAttackGoal(PlantShooterEntity shooter) {
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

		private final PlantShooterEntity shooter;

		public ShooterNearestTargetGoal(PlantShooterEntity mobIn, boolean checkSight, boolean memory, float w, float h) {
			super(mobIn, checkSight, memory, w, h);
			this.shooter = mobIn;
		}

		@Override
		protected boolean checkOther(LivingEntity entity) {
			return super.checkOther(entity) && this.shooter.checkY(entity);
		}

	}
}