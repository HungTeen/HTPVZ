package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.register.PVZDamageSource;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.ProjectileUtil;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

public class BaseBullet extends Projectile {
	protected float airSlowDown = 0.99F;
	protected float attackDamage = 0F;
	protected float size = 1F;// need sync?
	protected float knockBackStrengh = 0F;
	protected String damageName = "pvz_bullet";

	public BaseBullet(EntityType<? extends Projectile> type, Level worldIn, LivingEntity shooter) {
		super(type, worldIn);
		this.setNoGravity(true);
	}

	public BaseBullet(EntityType<? extends BaseBullet> bulletEntityType, Level level) {
		super(bulletEntityType,level);
	}

	public void shootToTarget(LivingEntity target, float speed) {
		this.setDeltaMovement(target.position().add(0, target.getEyeHeight(), 0).subtract(this.position()).normalize().scale(speed));
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return super.canHitEntity(entity) && ! PVZOwnedCapability.isTeammate(this, entity);
	}
	@Override
	public void setOwner(@Nullable Entity entity) {
		if (! level.isClientSide()) {
			this.getCapability(PVZOwnedCapability.CAP).orElse(null).setOwner(entity);
		}
		super.setOwner(entity);
	}
	@Override
	public void tick() {
		super.tick();
		HitResult hitresult = ProjectileUtil.getHitResult(this, this::canHitEntity);
		if (hitresult.getType() != HitResult.Type.MISS && !net.minecraftforge.event.ForgeEventFactory.onProjectileImpact(this, hitresult)) {
			this.onHit(hitresult);
		}
		Vec3 vec3 = this.getDeltaMovement();
		double dx = vec3.x;
		double dy = vec3.y;
		double dz = vec3.z;
		this.updateRotation();
		if (! this.isNoGravity()) {
			dy -= 0.06F;
		}
		if (level.getBlockState(this.blockPosition()).is(Blocks.WATER) || level.getBlockState(this.blockPosition()).is(Blocks.POWDER_SNOW)) {
 			dx -= 0.08 * dx;
			dy -= 0.08 * dy;
			dz -= 0.08 * dz;
		}
		this.setDeltaMovement(dx, dy, dz);
		this.setPos(this.getX() + dx, this.getY() + dy, this.getZ() + dz);

		if (this.tickCount > getMaxLiveTick()) {
			this.discard();
		}
	}
	@Override
	public void baseTick() {
		//deleted unnecessary calculations.
		this.level.getProfiler().push("entityBaseTick");
		if (this.boardingCooldown > 0) {
			--this.boardingCooldown;
		}
		this.walkDistO = this.walkDist;
		this.xRotO = this.getXRot();
		this.yRotO = this.getYRot();
		this.handleNetherPortal();
		if (this.isInLava()) {
			this.lavaHurt();
			this.fallDistance *= this.getFluidFallDistanceModifier(net.minecraftforge.common.ForgeMod.LAVA_TYPE.get());
		}
		this.checkOutOfWorld();
		this.firstTick = false;
		this.level.getProfiler().pop();
	}

	@Override
	protected void onHit(HitResult result) {
		super.onHit(result);
		HitResult.Type type = result.getType();
		if (type != HitResult.Type.MISS) {
			this.gameEvent(GameEvent.PROJECTILE_LAND, this.getOwner());
		}
	}
	@Override
	protected void onHitEntity(EntityHitResult result) {
		if (!this.level.isClientSide() && result.getEntity() instanceof LivingEntity) {
			this.dealDamageTo(result.getEntity());
		}
	}
	@Override
	protected void onHitBlock(BlockHitResult result) {
		super.onHitBlock(result);
		this.discard();
	}
	protected void dealDamageTo(Entity target) {
		final float damage = this.getAttackDamage();
		//default normal damage.
		target.hurt(PVZDamageSource.knockBack(PVZDamageSource.ignoreInvTime(
				PVZDamageSource.projectileDamageSource(getDamageName(), this, getOwner()))
						, getKnockBackStrength()), damage);
		this.discard();
	}

	protected int getMaxLiveTick() {
		return 80;
	}
	public float getKnockBackStrength() {
		return knockBackStrengh;
	}
	public void setKnockBackStrength(float strength) {
		knockBackStrengh = strength;
	}
	public String getDamageName() {
		return damageName;
	}
	public BaseBullet setDamageName(String name) {
		this.damageName = name;
		return this;
	}

	public float getAttackDamage() {
		return this.attackDamage;
	}

	public void setAttackDamage(float damage) {
		this.attackDamage = damage;
	}
	public float getSize() {
		return this.size;
	}

	public void setSize(float size) {
		this.size = size;
	}

//	/**
//	 * Gets the amount of gravity to apply to the thrown entity with each tick.
//	 */
//	protected float getGravity() {
//		return 0.03F;
//	}

	/**
	 * Checks if the entity is in range to render.
	 */
	@OnlyIn(Dist.CLIENT)
	@Override
	public boolean shouldRenderAtSqrDistance(double distance) {
		double d0 = this.getBoundingBox().getSize() * 4.0D;
		if (Double.isNaN(d0)) {
			d0 = 4.0D;
		}

		d0 = d0 * 64.0D;
		return distance < d0 * d0;
	}

	@Override
	protected void defineSynchedData() {

	}

	@Override
	public void readAdditionalSaveData(CompoundTag compound) {
		super.readAdditionalSaveData(compound);
		if (compound.contains("attack_damage")) {
			this.attackDamage = compound.getFloat("attack_damage");
		}
		if (compound.contains("size")) {
			this.size = compound.getFloat("size");
		}
		if (compound.contains("knock_back_strength")) {
			this.size = compound.getFloat("knock_back_strength");
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putFloat("attack_damage", this.attackDamage);
		compound.putFloat("size", this.size);
		compound.putFloat("knock_back_strength", this.knockBackStrengh);
	}


}