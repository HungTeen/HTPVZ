package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.register.OtherRegisters;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;

public class PeaBullet extends BaseBullet {
    public int changeCoolDown = 0;
    public boolean neverMelt = false;
    public boolean ignoreShield = false;
    protected static final EntityDataAccessor<PeaType> TYPE = SynchedEntityData.defineId(PeaBullet.class, OtherRegisters.peaTypeDataSerializer);

    public PeaBullet(EntityType<? extends BaseBullet> entityIn, Level level) {
        super(entityIn,level);
        this.noPhysics = true;
        this.damageName = "pea";
    }

    public PeaBullet(Level worldIn, LivingEntity peaShooter, PeaType type) {
        super(PVZEntities.PEA.get(), worldIn, peaShooter);
        setOwner(peaShooter);
        setPeaType(type);
        this.knockBackStrengh = (float) peaShooter.getAttribute(Attributes.ATTACK_KNOCKBACK).getValue();
        this.damageName = "pea";
    }

    @Override
    public boolean fireImmune() {
        return this.neverMelt || super.fireImmune();
    }
    @Override
    public void tick() {
        super.tick();
        //change type
        if (changeCoolDown <= 0) {
            if (this.isInWater() || level.getBlockState(this.blockPosition()).is(Blocks.POWDER_SNOW)) {
                if (getPeaType() == PeaType.Fire) {
                    setPeaType(PeaType.Common);
                    this.changeCoolDown = 5;
                }
            } else if (isOnFire()) {
                if (getPeaType() == PeaType.Ice) {
                    setPeaType(PeaType.Common);
                    this.changeCoolDown = 5;
                }
            } else if (this.isInLava()) {
                setPeaType(this.getPeaType() == PeaBullet.PeaType.Ice ? PeaBullet.PeaType.Common : PeaBullet.PeaType.Fire);
                this.changeCoolDown = 5;
            }
        } else {
            changeCoolDown -= 1;
        }
        //particles
        if (level.isClientSide) {
            Vec3 movement = getDeltaMovement();
            if (movement.distanceToSqr(Vec3.ZERO) >= 3) {
                for (int i = 5; i < 10; i ++) {
                    level.addParticle(ParticleTypes.CLOUD,
                            getX() - movement.x / 5 * i + this.random.nextFloat() / 5,
                            getY() - movement.y / 5 * i + this.random.nextFloat() / 5,
                            getZ() - movement.z / 5 * i + this.random.nextFloat() / 5,
                            0, 0, 0);
                }
            }
            if (getPeaType() == PeaType.Fire) {
                level.addParticle(ParticleTypes.SMOKE, getX(), getY(), getZ(), 0, 0, 0);
                level.addParticle(ParticleTypes.SMOKE, getX() - movement.x / 2, getY() - movement.y / 2, getZ() - movement.z / 2, 0, 0, 0);
            } else if (getPeaType() == PeaType.Ice) {
                level.addParticle(ParticleTypes.SNOWFLAKE, getX(), getY(), getZ(), 0, 0, 0);
            }
        }
    }

    protected void splashParticle() {
        Vec3 movement = getDeltaMovement();
        switch (getPeaType()) {
            case Common -> {
                for (int i = 0; i < 5; i ++) {
                    level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(PVZItems.PEA.get())),
                            getX(), getY(), getZ(),
                            - movement.x * 0.25 + random.nextFloat() * 0.25 - 0.125,
                            - movement.y * 0.25 + random.nextFloat() * 0.25,
                            - movement.z * 0.25 + random.nextFloat() * 0.25 - 0.125);
                }
            }
            case Ice -> {
                for (int i = 0; i < 5; i ++) {
                    level.addParticle(ParticleTypes.ITEM_SNOWBALL,
                            getX(), getY(), getZ(),
                            - movement.x * 0.25 + random.nextFloat() * 0.25 - 0.125,
                            - movement.y * 0.25 + random.nextFloat() * 0.25,
                            - movement.z * 0.25 + random.nextFloat() * 0.25 - 0.125);
                }
            }
            case Fire -> {
                for (int i = 0; i < 3; i ++) {
                    level.addParticle(ParticleTypes.LAVA,
                            getX(), getY(), getZ(),
                            - movement.x * 0.25 + random.nextFloat() * 0.15 - 0.075,
                            - movement.y * 0.25 + random.nextFloat() * 0.15,
                            - movement.z * 0.25 + random.nextFloat() * 0.15 - 0.075);
                }
            }
            case SoulFire -> {
                for (int i = 0; i < 3; i ++) {
                    level.addParticle(ParticleTypes.SCULK_SOUL,
                            getX() + random.nextFloat() * 0.5 - 0.25,
                            getY() + random.nextFloat() * 0.5 - 0.25,
                            getZ() + random.nextFloat() * 0.5 - 0.25,
                            - movement.x * 0.05 + random.nextFloat() * 0.1 - 0.05,
                            - movement.y * 0.15 + random.nextFloat() * 0.25,
                            - movement.z * 0.05 + random.nextFloat() * 0.1 - 0.05);
                }
            }
        }
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("pea_type", getPeaType().ordinal());
        tag.putBoolean("never_melt", this.neverMelt);
        tag.putBoolean("ignore_armor", this.ignoreShield);
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("pea_type")) {
            setPeaType(PeaType.values()[tag.getInt("pea_type")]);
        }
        if (tag.contains("never_melt")) {
            this.neverMelt = tag.getBoolean("never_melt");
        }
        if (tag.contains("ignore_armor")) {
            this.ignoreShield = tag.getBoolean("ignore_armor");
        }
    }
    @Override
    protected boolean dealDamageTo(Entity target) {
        boolean hurt;
        if (!ignoreShield) {
            hurt = super.dealDamageTo(target);
        } else {
            final float damage = this.getAttackDamage();
            //default normal damage.
            hurt = target.hurt(PVZDamageSource.hitBossWithMultiplier(PVZDamageSource.knockBack(PVZDamageSource.bypassShield(
                    PVZDamageSource.ignoreInvTime(PVZDamageSource.projectileDamageSource(getDamageName(), this, getOwner())))
                    , getKnockBackStrength()), target, 0.2F), damage);
            this.discard();
        }
        if (! hurt) {
            return false;
        }
        if (getPeaType() == PeaType.Fire || getPeaType() == PeaType.SoulFire) {
            if (! target.fireImmune()) {
                target.setSecondsOnFire(2);
            }
        } else if (getPeaType() == PeaType.Ice) {
            target.clearFire();
            if (target.canFreeze()) {
                target.setTicksFrozen(400);
            }
        }
        return true;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TYPE, PeaType.Common);
    }

    public PeaType getPeaType() {
        return entityData.get(TYPE);
    }
    public void setPeaType(PeaType type) {
        entityData.set(TYPE, type);
    }

    public enum PeaType {
        Common, SoulFire, Fire, Ice, Poison
    }
}
