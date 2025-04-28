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
import net.minecraft.world.phys.Vec3;

public class ButterBullet extends BaseBullet {
    public ButterBullet(EntityType<? extends BaseBullet> entityIn, Level level) {
        super(entityIn,level);
        this.setNoGravity(false);
        this.size = 1.5F;
    }

    public ButterBullet(Level worldIn, LivingEntity kernelPult) {
        super(PVZEntities.BUTTER.get(), worldIn, kernelPult);
        setOwner(kernelPult);
        this.setNoGravity(false);
        this.size = 1.5F;
    }

    public void shoot(double deltaX, double deltaY, double deltaZ, float speed, float randomAngle) {
        double distance = new Vec3(deltaX, deltaY, deltaZ).distanceTo(Vec3.ZERO);
        super.shoot(deltaX, deltaY, deltaZ, speed, randomAngle);
        double time = Math.min(distance / speed, 100);
        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.05D * time, 0.0D));
    }

    public void tick() {
        super.tick();
        Vec3 movement = getDeltaMovement();
        level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(PVZItems.CORN_KERNELS.get())),
                getX(), getY(), getZ(),
                - movement.x * 0.25 + random.nextFloat() * 0.25 - 0.12,
                - movement.y * 0.25 + random.nextFloat() * 0.25,
                - movement.z * 0.25 + random.nextFloat() * 0.25 - 0.12);
    }
    @Override
    protected boolean dealDamageTo(Entity target) {
        boolean hurt = super.dealDamageTo(target);
        if (!this.level.isClientSide() && hurt) {
            if (target instanceof LivingEntity living) living.addEffect(new MobEffectInstance(PVZMobEffects.BUTTER.get(),200,1));
        }
        return hurt;
    }
    protected void splashParticle() {
        Vec3 movement = getDeltaMovement();
        for (int i = 0; i < 5; i ++) {
            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(PVZItems.CORN_KERNELS.get())),
                    getX(), getY(), getZ(),
                    - movement.x * 0.25 + random.nextFloat() * 0.25 - 0.12,
                    - movement.y * 0.25 + random.nextFloat() * 0.25,
                    - movement.z * 0.25 + random.nextFloat() * 0.25 - 0.12);
        }
    }
}
