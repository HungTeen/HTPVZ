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
	protected float size = 1F;// need sync?
	protected float knockBackStrengh = 0F;
	protected String damageName = "pvz.shot";

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
		if (this.isInWater() || level.getBlockState(this.blockPosition()).is(Blocks.POWDER_SNOW)) {
			dx -= (1 - this.getWaterSlowDown()) * dx;
			dy -= (1 - this.getWaterSlowDown()) * dy;
			dz -= (1 - this.getWaterSlowDown()) * dz;
			if (! this.isNoGravity()) {
				dy += 0.035;
			}
		}
		this.setDeltaMovement(dx, dy, dz);
		this.setPos(this.getX() + dx, this.getY() + dy, this.getZ() + dz);

		if (this.tickCount > getMaxLiveTick()) {
			this.discard();
		}
		if (! this.isNoGravity()) { //when is pult ammo.
			this.setDeltaMovement(this.getDeltaMovement().add(0.0D, - 0.1D, 0.0D));
			if (this.getOwner() instanceof Mob owner && EntityUtil.isEntityValid(owner) && EntityUtil.isEntityValid(owner.getTarget()) && this.getDeltaMovement().y < 0) {
				Entity target = owner.getTarget();
				double timeLand = 5;
				double heightRelate = target.getY() + target.getBbHeight() / 2 - this.getY();
				for (int i = 0; i < 5; i ++) {
					timeLand = (timeLand + 2 * heightRelate / (2 * this.getDeltaMovement().y - 0.1 * timeLand)) / 2;
				}
				vec3 = target.position().subtract(this.position()).subtract(this.getDeltaMovement().x * timeLand, 0, this.getDeltaMovement(). z * timeLand);
				this.setDeltaMovement(this.getDeltaMovement()
						.add(Math.min(0.03, Math.max(-0.03, vec3.x / timeLand)), 0, Math.min(0.03, Math.max(-0.03, vec3.z / timeLand))));
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
		if (!this.level.isClientSide()) {
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
		//default normal damage.
		boolean hurt = target.hurt(getDamageSource(target), damage);
		this.discard();
		return hurt;
	}

	protected DamageSource getDamageSource(Entity target) {
		DamageSource source = PVZDamageSource.transferKiller(
				PVZDamageSource.ignoreInvTime(
						PVZDamageSource.hitBossWithProportion(
								PVZDamageSource.knockBack(
										PVZDamageSource.projectileDamageSource(getDamageName(), this, getOwner())
				, getKnockBackStrength()), target, 0.2F)), PVZEntityCapability.getOwner(this));
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