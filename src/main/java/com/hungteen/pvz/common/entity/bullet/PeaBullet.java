package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.entity.plants.PeaShooter;
import com.hungteen.pvz.common.register.OtherRegisters;
import com.hungteen.pvz.common.register.PVZEntities;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

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
        }
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
