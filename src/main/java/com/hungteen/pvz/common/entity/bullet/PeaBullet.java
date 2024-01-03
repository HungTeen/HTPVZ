package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.entity.plants.PeaShooter;
import com.hungteen.pvz.common.register.OtherRegisters;
import com.hungteen.pvz.common.register.PVZEntities;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.level.Level;

public class PeaBullet extends BaseBullet {
    protected static final EntityDataAccessor<PeaType> TYPE = SynchedEntityData.defineId(PeaBullet.class, OtherRegisters.peaTypeDataSerializer);

    public PeaBullet(EntityType<? extends BaseBullet> entityIn, Level level) {
        super(entityIn,level);
        this.noPhysics = true;
    }

    public PeaBullet(Level worldIn, PeaShooter peaShooter, PeaType type) {
        super(PVZEntities.PEA.get(), worldIn, peaShooter);
        setOwner(peaShooter);
        setPeaType(type);
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
