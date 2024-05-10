package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.register.OtherRegisters;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class PeaBullet extends BaseBullet {
    public int changeCoolDown = 0;
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
    public void tick() {
        super.tick();
//        if (getPeaType() == PeaType.Fire || level.getBlockState(this.blockPosition()).is(Blocks.FIRE) || level.getBlockState(this.blockPosition()).is(Blocks.SOUL_FIRE)) {
//            setSecondsOnFire(1);
//        }
        //change type
        if (changeCoolDown <= 0) {
            if (level.getBlockState(this.blockPosition()).is(Blocks.WATER) || level.getBlockState(this.blockPosition()).is(Blocks.POWDER_SNOW)) {
                if (getPeaType() == PeaType.Fire) {
                    setPeaType(PeaType.Common);
                    this.changeCoolDown = 5;
                }
            } else if (isOnFire()) {
                if (getPeaType() == PeaType.Ice) {
                    setPeaType(PeaType.Common);
                    this.changeCoolDown = 5;
                }
            } else if (level.getBlockState(this.blockPosition()).is(Blocks.LAVA)) {
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
        if (getPeaType() == PeaType.Common) {
            for (int i = 0; i < 5; i ++) {
                level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(PVZItems.PEA.get())),
                        getX(), getY(), getZ(),
                        - movement.x * 0.25 + random.nextFloat() * 0.25 - 0.125,
                        - movement.y * 0.25 + random.nextFloat() * 0.25,
                        - movement.z * 0.25 + random.nextFloat() * 0.25 - 0.125);
            }
        } else if (getPeaType() == PeaType.Ice) {
            for (int i = 0; i < 5; i ++) {
                level.addParticle(ParticleTypes.ITEM_SNOWBALL,
                        getX(), getY(), getZ(),
                        - movement.x * 0.25 + random.nextFloat() * 0.25 - 0.125,
                        - movement.y * 0.25 + random.nextFloat() * 0.25,
                        - movement.z * 0.25 + random.nextFloat() * 0.25 - 0.125);
            }
        } else if (getPeaType() == PeaType.Fire) {
            for (int i = 0; i < 3; i ++) {
                level.addParticle(ParticleTypes.LAVA,
                        getX(), getY(), getZ(),
                        - movement.x * 0.25 + random.nextFloat() * 0.15 - 0.075,
                        - movement.y * 0.25 + random.nextFloat() * 0.15,
                        - movement.z * 0.25 + random.nextFloat() * 0.15 - 0.075);
            }
        }
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("pea_type", getPeaType().ordinal());
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("pea_type")) {
            setPeaType(PeaType.values()[tag.getInt("pea_type")]);
        }
    }
    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (getPeaType() == PeaType.Fire || getPeaType() == PeaType.SoulFire) {
            if (! result.getEntity().fireImmune()) {
                result.getEntity().setSecondsOnFire(2);
            }
        } else if (getPeaType() == PeaType.Ice) {
            result.getEntity().clearFire();
            if (result.getEntity().canFreeze()) {
                result.getEntity().setTicksFrozen(400);
            }
        }
    }
    @Override
    protected void onHit(HitResult result) {
        if (level.isClientSide && result.getType() != HitResult.Type.MISS) {
            splashParticle();
        }
        super.onHit(result);
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
