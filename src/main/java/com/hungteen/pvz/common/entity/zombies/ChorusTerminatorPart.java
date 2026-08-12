package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.common.entity.Sun;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityDimensions;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.entity.PartEntity;

public class ChorusTerminatorPart extends PartEntity<ChorusTerminatorBoss> implements Enemy {
    public final String name;
    public final Type type;
    private final EntityDimensions size;
    public final boolean needSync;

    public ChorusTerminatorPart(ChorusTerminatorBoss parent, Type type, String name, float width, float height, boolean needSync) {
        super(parent);
        this.size = EntityDimensions.scalable(width, height);
        this.type = type;
        this.refreshDimensions();
        this.name = name;
        this.needSync = needSync;
        if (type != Type.LEG) this.setNoGravity(true);
    }

    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag p_20052_) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag p_20139_) {
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean hurt(DamageSource p_31020_, float p_31021_) {
        if (this.type == Type.EYE && ! this.level.isClientSide) Sun.spawnSunWithEffects(this.level, 50, this.blockPosition(), 0.2f);
        return ! this.isInvulnerableTo(p_31020_) && this.getParent().hurt(this, p_31020_, p_31021_);
    }

    @Override
    public void baseTick() {
        this.xOld = this.position().x;
        this.yOld = this.position().y;
        this.zOld = this.position().z;
        if (! this.getParent().isNoAi()) {
            if (this.type == Type.LEG) {
                double gravity = this.isNoGravity() ? 0 : getParent().getAttributeValue(ForgeMod.ENTITY_GRAVITY.get());
                Vec3 dDeltaMovement = Vec3.ZERO;
                dDeltaMovement = dDeltaMovement.add(0, - gravity, 0);
                this.setDeltaMovement(getDeltaMovement().add(dDeltaMovement));
            } else if (this.type == Type.BODY) {
                Vec3 center = Vec3.ZERO;
                for (ChorusTerminatorPart part : getParent().legs) {
                    center = center.add(part.position().subtract(this.position().add(0, -3, 0)));
                }
                float l = 1f / (getParent().legs.length);
                center = center.multiply(l, l, l);
                double distSqr = center.distanceToSqr(Vec3.ZERO);
                if (distSqr > 1e-3) {
                    if (distSqr > 16) {
                        double dist = Math.sqrt(distSqr) - 4;
                        this.setPos(this.position().add(center.normalize().multiply(dist, dist, dist)));
                    }
                    this.setDeltaMovement(this.getDeltaMovement().add(center.multiply(0.1, 0.1, 0.1)));
                }
            } else if (this.type == Type.EYE) {
                this.setDeltaMovement(Vec3.ZERO);
                float angle = getParent().getVisualRotationYInDegrees();
                this.setPos(getParent().body.position().add(-Math.sin(angle / 57.3) * 2, 1.1, Math.cos(angle / 57.3) * 2));
            } else if (this.type == Type.MOUTH) {
                this.setDeltaMovement(Vec3.ZERO);
                this.setPos(getParent().body.position().add(0, 0, 0));
            }
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        BlockState bState = this.level.getBlockState(this.getBlockPosBelowThatAffectsMyMovement());
        float friction = bState.isAir() ? (this.type == Type.BODY ? 0.6f : 0.99f) : bState.getFriction(level, this.getBlockPosBelowThatAffectsMyMovement(), this);
        this.setDeltaMovement(this.getDeltaMovement().multiply(new Vec3(friction, friction, friction)));
    }

    @Override
    public boolean is(Entity p_31031_) {
        return this == p_31031_ || this.getParent() == p_31031_;
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        throw new UnsupportedOperationException();
    }

    @Override
    public EntityDimensions getDimensions(Pose p_31023_) {
        return this.size;
    }

    @Override
    public boolean shouldBeSaved() {
        return false;
    }

    public enum Type {
        BODY, MOUTH, LEG, EYE, FRUIT
    }
}
