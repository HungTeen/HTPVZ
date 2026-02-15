package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.Shulker;
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
	protected float attackDamage = 0F;
	protected float gravity = 0.1F;
	protected float size = 1F;// need sync?
	protected float knockBackStrengh = 0F;
	protected String shootDamageName = "pvz.shot";
	protected String hitDamageName = "pvz.shot";

	public BaseBullet(EntityType<? extends Projectile> type, Level worldIn, LivingEntity shooter) {
		super(type, worldIn);
		this.setNoGravity(true);
	}

	public BaseBullet(EntityType<? extends BaseBullet> bulletEntityType, Level level) {
		super(bulletEntityType,level);
	}

	protected void splashParticle() {
	}

	protected float getWaterSlowDown() {
		return 0.92F;
	}

	@Override
	public void onClientRemoval() {
		super.onClientRemoval();
		this.splashParticle();
	}

	@Override
	protected boolean canHitEntity(Entity entity) {
		return super.canHitEntity(entity) && ! this.level.isClientSide && EntityUtil.checkCanEntityBeAttack(this, entity);
	}

	@Override
	public void setOwner(@Nullable Entity entity) {
		if (! level.isClientSide()) {
			this.getCapability(PVZEntityCapability.CAP).orElse(null).setOwner(entity);
		}
		super.setOwner(entity);
	}

	@Override
	public void shoot(double deltaX, double deltaY, double deltaZ, float speed, float randomAngle) {
		if (! this.isNoGravity()) {
			double distance = new Vec3(deltaX, deltaY, deltaZ).distanceTo(Vec3.ZERO);
			super.shoot(deltaX, deltaY, deltaZ, speed, randomAngle);
			double time = Math.min(distance / speed, 100);
			this.setDeltaMovement(this.getDeltaMovement().add(0.0D, gravity / 2 * time, 0.0D));
			return;
		}
		super.shoot(deltaX, deltaY, deltaZ, speed, randomAngle);
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
		if (this.isInFluidType() || level.getBlockState(this.blockPosition()).is(Blocks.POWDER_SNOW)) {
			dx -= (1 - this.getWaterSlowDown()) * dx;
			dy -= (1 - this.getWaterSlowDown()) * dy;
			dz -= (1 - this.getWaterSlowDown()) * dz;
			if (! this.isNoGravity()) {
				dy += gravity * 0.6;
			}
		}
		this.setDeltaMovement(dx, dy, dz);
		this.setPos(this.getX() + dx, this.getY() + dy, this.getZ() + dz);

		if (this.tickCount > getMaxLiveTick()) {
			this.discard();
		}
		if (! this.isNoGravity()) { //when is pult ammo.
			this.setDeltaMovement(this.getDeltaMovement().add(0.0D, - gravity, 0.0D));
			if (this.getOwner() instanceof Mob owner && EntityUtil.isEntityValid(owner) && EntityUtil.isEntityValid(owner.getTarget())
					&& this.getDeltaMovement().y < 0 && this.getY() > owner.getTarget().getY()) {
				Entity target = owner.getTarget();
				float fixLimit = Math.min((float) Math.max(0.03, 0.5F * target.getDeltaMovement().distanceToSqr(Vec3.ZERO)), (float) (this.getDeltaMovement().distanceToSqr(Vec3.ZERO) / 10));
				double timeLand = 5;
				double heightRelate = target.getY() + target.getBbHeight() / 2 - this.getY();
				for (int i = 0; i < 5; i ++) {
					timeLand = (timeLand + 2 * heightRelate / (2 * this.getDeltaMovement().y - gravity * timeLand)) / 2;
				}
				vec3 = target.position().subtract(this.position()).subtract(this.getDeltaMovement().x * timeLand, 0, this.getDeltaMovement(). z * timeLand);
				this.setDeltaMovement(this.getDeltaMovement()
						.add(Math.min(fixLimit, Math.max(-fixLimit, vec3.x / timeLand)), 0, Math.min(fixLimit, Math.max(-fixLimit, vec3.z / timeLand))));
			}
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

		this.wasInPowderSnow = this.isInPowderSnow;
		this.isInPowderSnow = false;
		this.updateInWaterStateAndDoFluidPushing();

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
		if (! this.level.isClientSide()) {
			this.dealDamageTo(result.getEntity());
		}
	}
	@Override
	protected void onHitBlock(BlockHitResult result) {
		super.onHitBlock(result);
		this.discard();
	}
	protected boolean dealDamageTo(Entity target) {
		final float damage = this.getAttackDamage();
		final DamageSource damageSource = getDamageSource(target);
		if (target instanceof Shulker shulker && shulker.isClosed()
				&& ! (damageSource.isBypassArmor() || damageSource.isExplosion() || PVZDamageSource.isBypassShield(damageSource))) {
			return false;
		}
		//default normal damage.
		boolean hurt = target.hurt(damageSource, damage);
		this.discard();
		return hurt;
	}

	protected DamageSource getDamageSource(Entity target) {
		DamageSource source = PVZDamageSource.transferKiller(
				PVZDamageSource.ignoreInvTime(
						PVZDamageSource.hitBossWithProportion(
								PVZDamageSource.knockBack(
										PVZDamageSource.projectileDamageSource(getDamageName(), this, getOwner())
				, getKnockBackStrength()), target)), PVZEntityCapability.getOwner(this));
		if (! this.isNoGravity()) {
			source.damageHelmet();
		}
		return source;
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
		return this.gravity == 0 || this.isNoGravity() ? shootDamageName : hitDamageName;
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
		if (compound.contains("gravity")) {
			this.gravity = compound.getFloat("gravity");
		}
	}

	@Override
	public void addAdditionalSaveData(CompoundTag compound) {
		super.addAdditionalSaveData(compound);
		compound.putFloat("attack_damage", this.attackDamage);
		compound.putFloat("size", this.size);
		compound.putFloat("knock_back_strength", this.knockBackStrengh);
		compound.putFloat("gravity", this.gravity);
	}


}