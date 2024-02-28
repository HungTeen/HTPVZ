package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.OtherRegisters;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZMobEffects;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ButterBullet extends BaseBullet {
    protected static final EntityDataAccessor<ButterSkill> SKILL = SynchedEntityData.defineId(ButterBullet.class, OtherRegisters.butterSkillDataSerializer);
    public ButterBullet(EntityType<? extends BaseBullet> entityIn, Level level) {
        super(entityIn,level);
        this.noPhysics = true;
        this.damageName = "butter";
        this.size = 1.5F;
    }

    public ButterBullet(Level worldIn, LivingEntity kernelPult) {
        super(PVZEntities.BUTTER.get(), worldIn, kernelPult);
        setOwner(kernelPult);
        this.setNoGravity(false);
        this.damageName = "butter";
        this.size = 1.5F;
    }

    public void shoot(double deltaX, double deltaY, double deltaZ, float speed, float randomAngle) {
        double distance = new Vec3(deltaX, deltaY, deltaZ).distanceTo(Vec3.ZERO);
        super.shoot(deltaX, deltaY, deltaZ, speed, randomAngle);
        double time = distance / speed;
        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.05D * time, 0.0D));
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(SKILL, ButterSkill.NULL);
    }
    @Override
    protected void onHit(HitResult result) {
        if (level.isClientSide && result.getType() != HitResult.Type.MISS) {
            splashParticle();
        }
        super.onHit(result);
    }
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (!this.level.isClientSide()) {
            if (result.getEntity() instanceof LivingEntity living) living.addEffect(new MobEffectInstance(PVZMobEffects.BUTTER.get(),100,1));
        }
    }
    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level.isClientSide() && getButterSkill() == ButterSkill.POTION) {
            AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.level, this.getX(), this.getY(), this.getZ());
            areaeffectcloud.setRadius(1F);
            areaeffectcloud.setRadiusOnUse(-0.5F);
            areaeffectcloud.setWaitTime(10);
            areaeffectcloud.setDuration(80);
            areaeffectcloud.setRadiusPerTick(-areaeffectcloud.getRadius() / (float)areaeffectcloud.getDuration());
            areaeffectcloud.addEffect(new MobEffectInstance(PVZMobEffects.BUTTER.get(),80,1));
            this.level.addFreshEntity(areaeffectcloud);
        }
        this.discard();
    }
    protected void splashParticle() {
        Vec3 movement = getDeltaMovement();
        for (int i = 0; i < 5; i ++) {
            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(PVZItems.LUX_ESSENCE.get())),//TODO change that.
                    getX(), getY(), getZ(),
                    - movement.x * 0.25 + random.nextFloat() * 0.25 - 0.12,
                    - movement.y * 0.25 + random.nextFloat() * 0.25,
                    - movement.z * 0.25 + random.nextFloat() * 0.25 - 0.12);
        }
    }
    @Override
    public void tick() {
        super.tick();
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, - 0.1D, 0.0D));
        }
        if (level.isClientSide && this.getButterSkill() == ButterSkill.POTION) {
            Vec3 pos = this.position();
            Particle particle = ClientProxy.MC.levelRenderer.addParticleInternal(ParticleTypes.ENTITY_EFFECT.getType(), false,
                    pos.x + random.nextFloat() * 0.6 - 0.3, pos.y + random.nextFloat() * 1.0 - 0.3, pos.z + random.nextFloat() * 0.6 - 0.3, 0, 0, 0);
            int color = PVZMobEffects.BUTTER.get().getColor();
            float r = (float)(color >> 16 & 255) / 255.0F;
            float g = (float)(color >> 8 & 255) / 255.0F;
            float b = (float)(color >> 0 & 255) / 255.0F;
            particle.setColor(r, g, b);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("butter_skill", getButterSkill().ordinal());
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("butter_skill")) {
            setButterSkill(ButterSkill.values()[tag.getInt("butter_skill")]);
        }
    }

    public ButterSkill getButterSkill() {
        return entityData.get(SKILL);
    }
    public void setButterSkill(ButterSkill type) {
        entityData.set(SKILL, type);
    }
    public enum ButterSkill {
        NULL, POTION
    }
}
