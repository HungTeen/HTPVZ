package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZMobEffects;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class ButterBullet extends BaseBullet {
    protected float size = 1.5F;// need sync?
    public ButterBullet(EntityType<? extends BaseBullet> entityIn, Level level) {
        super(entityIn,level);
        this.noPhysics = true;
        this.damageName = "butter";
    }

    public ButterBullet(Level worldIn, LivingEntity kernelPult) {
        super(PVZEntities.BUTTER.get(), worldIn, kernelPult);
        setOwner(kernelPult);
        this.setNoGravity(false);
        this.damageName = "butter";
    }

    public void shoot(double deltaX, double deltaY, double deltaZ, float speed, float randomAngle) {
        double distance = new Vec3(deltaX, deltaY, deltaZ).distanceTo(Vec3.ZERO);
        super.shoot(deltaX, deltaY, deltaZ, speed, randomAngle);
        double time = distance / speed;
        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.05D * time, 0.0D));
    }

    @Override
    protected void onHit(HitResult result) {
        if (level.isClientSide && result.getType() != HitResult.Type.MISS) {
            splashParticle();
        }
        super.onHit(result);
    }
    protected void dealDamageTo(Entity target) {
        if(target instanceof LivingEntity living)living.addEffect(new MobEffectInstance(PVZMobEffects.BUTTER.get(),100,1));

        super.dealDamageTo(target);
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
    public float getSize() {
        return this.size;
    }
    @Override
    public void tick() {
        super.tick();
        if (!this.isNoGravity()) {
            this.setDeltaMovement(this.getDeltaMovement().add(0.0D, - 0.1D, 0.0D));
        }
    }

}
