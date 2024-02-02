package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.entity.plants.PeaShooter;
import com.hungteen.pvz.common.register.OtherRegisters;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class PeaBullet extends BaseBullet {
    protected static final EntityDataAccessor<PeaType> TYPE = SynchedEntityData.defineId(PeaBullet.class, OtherRegisters.peaTypeDataSerializer);

    public PeaBullet(EntityType<? extends BaseBullet> entityIn, Level level) {
        super(entityIn,level);
        this.noPhysics = true;
        this.damageName = "pea";
    }

    public PeaBullet(Level worldIn, PeaShooter peaShooter, PeaType type) {
        super(PVZEntities.PEA.get(), worldIn, peaShooter);
        setOwner(peaShooter);
        setPeaType(type);
        this.knockBackStrengh = (float) peaShooter.getAttribute(Attributes.ATTACK_KNOCKBACK).getValue();
        this.damageName = "pea";
    }
    @Override
    public void tick() {
        super.tick();
        if (getPeaType() == PeaType.Fire || level.getBlockState(this.blockPosition()).is(Blocks.FIRE) || level.getBlockState(this.blockPosition()).is(Blocks.SOUL_FIRE)) {
            setSecondsOnFire(1);
        }
        if (level.getBlockState(this.blockPosition()).is(Blocks.WATER) || level.getBlockState(this.blockPosition()).is(Blocks.POWDER_SNOW)) {
            if (getPeaType() == PeaType.Fire) {
                setPeaType(PeaType.Common);
            }
        } else if (isOnFire()) {
            if (getPeaType() == PeaType.Ice) {
                setPeaType(PeaType.Common);
            }
        } else if (level.getBlockState(this.blockPosition()).is(Blocks.LAVA)) {
            setPeaType(PeaType.Fire);
        }
        if (level.isClientSide) {
            Vec3 movement = getDeltaMovement();
            if (movement.distanceToSqr(Vec3.ZERO) > 2) {
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
                        - movement.x * 0.25 + random.nextFloat() * 0.25 - 0.12,
                        - movement.y * 0.25 + random.nextFloat() * 0.25,
                        - movement.z * 0.25 + random.nextFloat() * 0.25 - 0.12);
            }
        } else if (getPeaType() == PeaType.Ice) {
            for (int i = 0; i < 5; i ++) {
                level.addParticle(ParticleTypes.ITEM_SNOWBALL,
                        getX(), getY(), getZ(),
                        - movement.x * 0.25 + random.nextFloat() * 0.25 - 0.12,
                        - movement.y * 0.25 + random.nextFloat() * 0.25,
                        - movement.z * 0.25 + random.nextFloat() * 0.25 - 0.12);
            }
        } else if (getPeaType() == PeaType.Fire) {
            for (int i = 0; i < 5; i ++) {
                level.addParticle(ParticleTypes.FLAME,
                        getX() + random.nextFloat() - 0.5,
                        getY() + random.nextFloat() - 0.5,
                        getZ() + random.nextFloat() - 0.5,
                        - movement.x * 0.1 + random.nextFloat() * 0.1 - 0.05,
                        - movement.y * 0.1 + random.nextFloat() * 0.1,
                        - movement.z * 0.1 + random.nextFloat() * 0.1 - 0.05);
            }
        }
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        super.onHitEntity(result);
        if (getPeaType() == PeaType.Fire) {
            result.getEntity().setSecondsOnFire(5);
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
        Common, Fire, Ice, Poison
    }
}
