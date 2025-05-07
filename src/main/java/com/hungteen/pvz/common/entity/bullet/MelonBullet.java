package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.OtherRegisters;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.List;

public class MelonBullet extends BaseBullet {
    protected static final EntityDataAccessor<MelonType> TYPE = SynchedEntityData.defineId(MelonBullet.class, OtherRegisters.melonTypeDataSerializer);
    protected static final EntityDataAccessor<MelonSkill> SKILL = SynchedEntityData.defineId(MelonBullet.class, OtherRegisters.melonSkillDataSerializer);

    public MelonBullet(EntityType<? extends BaseBullet> entityIn, Level level) {
        super(entityIn,level);
        this.setNoGravity(false);
        this.knockBackStrengh = 0.5F;
        this.size = 2F;
    }

    public MelonBullet(Level worldIn, LivingEntity melonPult, MelonType type) {
        super(PVZEntities.MELON.get(), worldIn, melonPult);
        setOwner(melonPult);
        this.setMelonType(type);
        this.setNoGravity(false);
        this.knockBackStrengh = 0.5F;
        this.size = 2F;
    }

    public void shoot(double deltaX, double deltaY, double deltaZ, float speed, float randomAngle) {
        double distance = new Vec3(deltaX, deltaY, deltaZ).distanceTo(Vec3.ZERO);
        super.shoot(deltaX, deltaY, deltaZ, speed, randomAngle);
        double time = Math.min(distance / speed, 100);
        this.setDeltaMovement(this.getDeltaMovement().add(0.0D, 0.05D * time, 0.0D));
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        if (! this.level.isClientSide()) {
            if (this.getMelonSkill() == MelonSkill.POTION) {
                applySplash(getMobEffects(), result.getEntity());
            } else {
                List<Entity> entities = level.getEntities(this, this.getBoundingBox().inflate(1.5, 1, 1.5).move(0, -0.5, 0),
                        (entity) -> entity instanceof LivingEntity && EntityUtil.checkCanEntityBeAttack(this, entity));
                entities.forEach((target -> {
                    target.hurt(PVZDamageSource.hitBossWithProportion(PVZDamageSource.knockBack(PVZDamageSource.ignoreInvTime(PVZDamageSource.setInterrupting(
                                    PVZDamageSource.owned(getDamageName(), getOwner() instanceof LivingEntity ? (LivingEntity) getOwner() : null)))
                            , getKnockBackStrength()), target, 0.05F), this.getAttackDamage() / 3);//splash damage regarded as non-projectile.
                    if (this.getMelonType() == MelonType.Ice && target.canFreeze()) {
                        target.setTicksFrozen(400);
                    }
                }));
            }
        }
        super.onHitEntity(result);
    }
    @Override
    protected void onHitBlock(BlockHitResult result) {
        if (!this.level.isClientSide()) {
            if (this.getMelonSkill() == MelonSkill.POTION) {
                applySplash(getMobEffects(), null);
            } else {
                List<Entity> entities = level.getEntities(this, this.getBoundingBox().inflate(3, 2, 3),
                        (entity) -> entity instanceof LivingEntity && EntityUtil.checkCanEntityBeAttack(this, entity));
                entities.forEach((entity -> {
                    entity.hurt(PVZDamageSource.knockBack(PVZDamageSource.ignoreInvTime(
                                    PVZDamageSource.projectileDamageSource(getDamageName(), this, getOwner()))
                            , getKnockBackStrength()), this.getAttackDamage() / 3);
                    if (this.getMelonType() == MelonType.Ice && entity.canFreeze()) {
                        entity.setTicksFrozen(400);
                    }
                }));
            }
        }
        super.onHitBlock(result);
    }
    @Override
    public float getAttackDamage() {
        return (float) (this.attackDamage * (this.getMelonSkill() == MelonSkill.GRAVITY ? this.getDeltaMovement().distanceToSqr(Vec3.ZERO) : 1));
    }
    public List<MobEffectInstance> getMobEffects() {
        List<MobEffectInstance> list = new java.util.ArrayList<>(List.of(new MobEffectInstance(MobEffects.HEAL, 2)));
        if (this.getMelonType() == MelonType.Ice) {
            list.add(new MobEffectInstance(PVZMobEffects.FREEZE.get(), 80));
        }
        return list;
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("melon_type", getMelonType().ordinal());
        tag.putInt("melon_skill", getMelonSkill().ordinal());
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("melon_type")) {
            setMelonType(MelonType.values()[tag.getInt("melon_type")]);
        }
        if (tag.contains("melon_skill")) {
            setMelonSkill(MelonSkill.values()[tag.getInt("melon_skill")]);
        }
    }
    protected void splashParticle() {
        Vec3 movement = getDeltaMovement();
        for (int i = 0; i < 50; i ++) {
            level.addParticle(new ItemParticleOption(ParticleTypes.ITEM,
                            new ItemStack(this.getMelonSkill() == MelonSkill.POTION ? Items.GLISTERING_MELON_SLICE : Items.MELON_SLICE)),
                    getX() + random.nextFloat() * 0.5 - 0.25,
                    getY() + random.nextFloat() * 0.5 - 0.5,
                    getZ() + random.nextFloat() * 0.5 - 0.25,
                    - movement.x * 0.25 + random.nextFloat() * 0.5 - 0.25,
                    - movement.y * 0.25 + random.nextFloat() * 0.25,
                    - movement.z * 0.25 + random.nextFloat() * 0.5 - 0.25);
        }
        if (this.getMelonSkill() == MelonSkill.POTION) {
            for (int i = 0; i < 25; i ++) {
                Vec3 pos = this.position();
                Particle particle = ClientProxy.MC.levelRenderer.addParticleInternal(ParticleTypes.ENTITY_EFFECT.getType(), false,
                        pos.x + random.nextFloat() * 4 - 2, pos.y + random.nextFloat() * 2.0 - 1.4, pos.z + random.nextFloat() * 4 - 2, 0, 0, 0);

                if (particle != null) {
                    int color = PotionUtils.getColor(getMobEffects());
                    float r = (float)(color >> 16 & 255) / 255.0F;
                    float g = (float)(color >> 8 & 255) / 255.0F;
                    float b = (float)(color & 255) / 255.0F;
                    particle.setColor(r, g, b);
                }
            }
        }
    }

    public void applySplash(List<MobEffectInstance> p_37548_, @Nullable Entity p_37549_) {
        AABB aabb = this.getBoundingBox().inflate(4.0D, 2.0D, 4.0D);
        List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, aabb);
        if (!list.isEmpty()) {
            Entity entity = this.getEffectSource();

            for(LivingEntity livingentity : list) {
                if (livingentity.isAffectedByPotions()) {
                    double d0 = this.distanceToSqr(livingentity);
                    if (d0 < 16.0D) {
                        double d1 = 1.0D - Math.sqrt(d0) / 4.0D;
                        if (livingentity == p_37549_) {
                            d1 = 1.0D;
                        }

                        for(MobEffectInstance mobeffectinstance : p_37548_) {
                            MobEffect mobeffect = mobeffectinstance.getEffect();
                            if (mobeffect.isInstantenous()) {
                                mobeffect.applyInstantenousEffect(this, this.getOwner(), livingentity, mobeffectinstance.getAmplifier(), d1);
                            } else {
                                int i = (int)(d1 * (double)mobeffectinstance.getDuration() + 0.5D);
                                if (i > 20) {
                                    livingentity.addEffect(new MobEffectInstance(mobeffect, i, mobeffectinstance.getAmplifier(), mobeffectinstance.isAmbient(), mobeffectinstance.isVisible()), entity);
                                }
                            }
                        }
                    }
                }
            }
        }

    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide && this.getMelonSkill() == MelonSkill.POTION) {
            Vec3 pos = this.position();
            Particle particle = ClientProxy.MC.levelRenderer.addParticleInternal(ParticleTypes.ENTITY_EFFECT.getType(), false,
                    pos.x + random.nextFloat() * 0.6 - 0.3, pos.y + random.nextFloat() * 1.0 - 0.3, pos.z + random.nextFloat() * 0.6 - 0.3, 0, 0, 0);

            if (particle != null) {
                int color = PotionUtils.getColor(getMobEffects());
                float r = (float)(color >> 16 & 255) / 255.0F;
                float g = (float)(color >> 8 & 255) / 255.0F;
                float b = (float)(color & 255) / 255.0F;
                particle.setColor(r, g, b);
            }
        }
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TYPE, MelonType.Common);
        this.entityData.define(SKILL, MelonSkill.NULL);
    }

    public MelonType getMelonType() {
        return entityData.get(TYPE);
    }
    public void setMelonType(MelonType type) {
        entityData.set(TYPE, type);
    }
    public MelonSkill getMelonSkill() {
        return entityData.get(SKILL);
    }
    public void setMelonSkill(MelonSkill type) {
        entityData.set(SKILL, type);
    }

    public enum MelonType {
        Common, Ice
    }
    public enum MelonSkill {
        NULL, POTION, GRAVITY
    }
}
