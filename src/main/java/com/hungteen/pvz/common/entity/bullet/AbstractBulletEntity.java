package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.entity.PVZPlant;
import it.unimi.dsi.fastutil.ints.IntOpenHashSet;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public abstract class AbstractBulletEntity extends Projectile{

	protected IntOpenHashSet hitEntities;
	protected float airSlowDown = 0.99F;
	protected float attackDamage = 0F;
	private PVZOwnedCapability cap;
	protected boolean canExist = true;

	public AbstractBulletEntity(EntityType<? extends Projectile> type, Level worldIn, PVZOwnedCapability cap) {
		super(type, worldIn);
		this.setNoGravity(true);
	}

	public AbstractBulletEntity(EntityType<? extends Projectile> type, Level worldIn, LivingEntity shooter) {
		super(type, worldIn);
	//	this.cap = this.getCapability(PVZOwnedCapability.CAP).orElse(null);
	//	cap.setOwner(shooter);
		this.setNoGravity(true);
	}

	public AbstractBulletEntity(EntityType<? extends AbstractBulletEntity> bulletEntityType, Level level) {
		super(bulletEntityType,level);
	}


	protected boolean canHitEntity(Entity entity) {
		return super.canHitEntity(entity)&&PVZOwnedCapability.isTeammate(cap.getOwner(), entity);
	}


	/**
	 * shoot bullet such as pea or spore
	 */
	public void shootPea(double dx, double dy, double dz, double speed, double angleOffset) {
		final double down = this.getShootPeaAngle();
		final double dxz = Math.sqrt(dx * dx + dz * dz);
		if(down != 0){
			dy = Mth.clamp(dy, - dxz / down, dxz / down);//fix dy by angle
		}
//		System.out.println(dy + "," + dxz);
		final double degree = Mth.atan2(dz, dx) + Math.toRadians(angleOffset);
		dx = Math.cos(degree) * dxz;
		dz = Math.sin(degree) * dxz;
		final double totSpeed = Math.sqrt(dxz * dxz + dy * dy);
		this.setDeltaMovement(new Vec3(dx / totSpeed, dy / totSpeed, dz / totSpeed).scale(speed));
	}

	public void shootToTarget(LivingEntity target, double speed) {
		this.setDeltaMovement(target.position().add(0, target.getEyeHeight(), 0).subtract(this.position()).normalize().scale(speed));
	}

	/**
	 * Called when this EntityThrowable hits a block or entity.<br>
	 * only in server side.
	 */
	protected void onHit(HitResult result) {
		HitResult.Type type = result.getType();
		if (type == HitResult.Type.ENTITY) {
			this.onHitEntity((EntityHitResult)result);
		} else if (type == HitResult.Type.BLOCK) {
			this.onHitBlock((BlockHitResult)result);
		}

		if (type != HitResult.Type.MISS) {
			this.gameEvent(GameEvent.PROJECTILE_LAND, this.getOwner());
		}
		//handle hit and remove.
		if(! this.level.isClientSide && ! this.canExist){
			this.level.broadcastEntityEvent(this, (byte)3);
			this.discard();
		}

	}
	protected void onHitEntity(EntityHitResult result) {
		if(this.canHitEntity(result.getEntity())){
			this.dealDamageTo(result.getEntity());
		}
	}
	protected abstract void dealDamageTo(Entity target);
	protected abstract int getMaxLiveTick();

	@Override
	protected void defineSynchedData() {

	}

	@Nullable
	public LivingEntity getThrower() {
		return (LivingEntity) this.getOwner();
	}

	public float getAttackDamage() {
		return this.attackDamage;
	}

	public void setAttackDamage(float damage) {
		this.attackDamage = damage;
	}

	/**
	 * Gets the amount of gravity to apply to the thrown entity with each tick.
	 */
	protected float getGravityVelocity() {
		return 0.03F;
	}

	/**
	 * get how much angle can shoot by thrower
	 */
	public double getShootPeaAngle() {
		if (this.getThrower() instanceof PVZPlant plant) {
			//return plant.getMaxShootAngle();
		}
		return 0;
	}

	/**
	 * Checks if the entity is in range to render.
	 */
	@OnlyIn(Dist.CLIENT)
	public boolean shouldRenderAtSqrDistance(double distance) {
		double d0 = this.getBoundingBox().getSize() * 4.0D;
		if (Double.isNaN(d0)) {
			d0 = 4.0D;
		}

		d0 = d0 * 64.0D;
		return distance < d0 * d0;
	}

	/**
	 * Updates the entity motion clientside, called by packets from the server
	 */
//	@OnlyIn(Dist.CLIENT)
//	public void lerpMotion(double x, double y, double z) {
//		this.setDeltaMovement(x, y, z);
//		if (this.xRotO == 0.0F && this.yRotO == 0.0F) {
//			float f = Mth.sqrt((float) (x * x + z * z));
//			this.yRot = (float) (Mth.atan2(x, z) * (double) (180F / (float) Math.PI));
//			this.xRot = (float) (Mth.atan2(y, (double) f) * (double) (180F / (float) Math.PI));
//			this.yRotO = this.yRot;
//			this.xRotO = this.xRot;
//			this.moveTo(this.getX(), this.getY(), this.getZ(), this.yRot,
//					this.xRot);
//		}
//	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if(compound.contains("bullet_attack_damage")) {
			this.attackDamage = compound.getFloat("bullet_attack_damage");
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putFloat("bullet_attack_damage", this.attackDamage);
	}


}