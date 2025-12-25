package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZParticles;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.PrimedTnt;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.hungteen.pvz.common.register.PVZDamageSource.*;

public class DandelionSeedBullet extends BaseBullet {
    public DandelionSeedBullet(EntityType<? extends BaseBullet> entityIn, Level level) {
        super(entityIn,level);
        gravity = 0;
    }

    public DandelionSeedBullet(Level worldIn, LivingEntity dandelion) {
        super(PVZEntities.DANDELION_SEED.get(), worldIn, dandelion);
        setOwner(dandelion);
        gravity = 0;
    }

    @Override
    public int getMaxLiveTick() {
        return 400;
    }

    @Override
    public void tick() {
        super.tick();
        if ((this.tickCount & 3) == 0 && this.isNoGravity() && ! level.getEntities(this,
                    this.getBoundingBox().inflate(1, 4, 1).move(0, -4, 0),
                    this::canHitEntity).isEmpty()) {
            if (! level.isClientSide()) {
                gravity = 0.1F;
                this.setNoGravity(false);
                Vec3 mov = this.getDeltaMovement();
                this.setDeltaMovement(mov.add(0, -0.3, 0));
                ((ServerLevel) level).sendParticles(PVZParticles.DANDELION.get(), getX(), getY(), getZ(), 3,
                        mov.x + 0.1 * (random.nextFloat() - 0.5),
                        - mov.y + 0.1 * (random.nextFloat() - 0.5),
                        mov.z + 0.1 * (random.nextFloat() - 0.5), 0);
            }
        }
    }
    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (!this.level.isClientSide()) {
            explode(result.getEntity().position());
        }
    }
    @Override
    protected void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        if (!this.level.isClientSide()) {
            explode(this.position());
        }
        explode(this.position());
    }
    protected DamageSource getDamageSource(Entity target) {
        return transferKiller(
                knockBack(ignoreInvTime(teamFilter(multiply(
                        PVZDamageSource.projectileDamageSource(getDamageName(), this, getOwner()).setExplosion().damageHelmet(), 0.5F))), 0F)
                , PVZEntityCapability.getOwner(this));
    }
    public void explode(Vec3 pos) {
        this.level.gameEvent(this, GameEvent.EXPLODE, pos);
        AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.level, pos.x, pos.y, pos.z);
        areaeffectcloud.setRadius(1.5F);
        areaeffectcloud.setDuration(60);
        areaeffectcloud.setWaitTime(0);
        areaeffectcloud.setOwner(EntityUtil.isEntityValid(this.getOwner()) && this.getOwner() instanceof LivingEntity living ? living : null);
        Explosion tmpExplosion = new Explosion(this.level, this, pos.x, pos.y, pos.z, 1.5F);
        float scale = 3;
        int k1 = Mth.floor(pos.x - (double)scale - 1);
        int l1 = Mth.floor(pos.x + (double)scale + 1);
        int i2 = Mth.floor(pos.y - (double)scale - 1);
        int i1 = Mth.floor(pos.y + (double)scale + 1);
        int j2 = Mth.floor(pos.z - (double)scale - 1);
        int j1 = Mth.floor(pos.z + (double)scale + 1);
        List<Entity> list = this.level.getEntities(this, new AABB(k1, i2, j2, l1, i1, j1)
                , entity -> EntityUtil.checkCanEntityBeAttack(this, entity));
        net.minecraftforge.event.ForgeEventFactory.onExplosionDetonate(this.level, tmpExplosion, list, scale);
        Map<MobEffect, MobEffectInstance> instances = new HashMap<>();
        for (Entity entity : list) {
            if (!entity.ignoreExplosion()) {
                double d12 = Math.sqrt(entity.distanceToSqr(pos)) / (double) scale;
                if (d12 <= 1.0D) {
                    double d5 = entity.getX() - this.getX();
                    double d7 = (entity instanceof PrimedTnt ? entity.getY() : entity.getEyeY()) - this.getY();
                    double d9 = entity.getZ() - this.getZ();
                    double d13 = Math.sqrt(d5 * d5 + d7 * d7 + d9 * d9);
                    if (d13 != 0) {
                        double d14 = Explosion.getSeenPercent(pos, entity);
                        double d10 = (1 - d12) * d14;
                        entity.hurt(this.getDamageSource(entity), (float) ((int) ((d10 * d10 + d10) / 2.0D * 7.0D * (double) scale + 1.0D)));
                        if (! entity.isAlive() && entity instanceof LivingEntity living) {
                            living.getActiveEffects().forEach(instance -> {
                                if (! instances.containsKey(instance.getEffect())) {
                                    instances.put(instance.getEffect(), instance);
                                }
                            });
                        }
                    }
                }
            }
        }
        this.discard();
        if (instances.isEmpty()) {
            areaeffectcloud.discard();
            return;
        }
        instances.values().forEach(areaeffectcloud::addEffect);
        this.level.addFreshEntity(areaeffectcloud);
    }
    protected void splashParticle() {
        level.addParticle(ParticleTypes.EXPLOSION,
                getX() + random.nextFloat() - 0.5, getY(), getZ() + random.nextFloat() - 0.5, 0, 0, 0);
    }
}
