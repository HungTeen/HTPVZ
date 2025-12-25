package com.hungteen.pvz.common.entity;

import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientboundAddEntityPacket;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;

public class EntityLifter extends Entity {
    public EntityLifter(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
    }

    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide) {
            for (int i = 0; i < 5; i ++) {
                this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()),
                        this.getX() + (this.random.nextDouble() - 0.5D), this.getY() + 0.1D, this.getZ() + (this.random.nextDouble() - 0.5D),
                        (this.random.nextDouble() - 0.5) * 6.0D, 2D, (this.random.nextDouble() - 0.5) * 4.0D);
            }
        } else {
            Entity entity = this.getFirstPassenger();
            if (entity instanceof Mob mob) {
                mob.setNoAi(this.tickCount < 20);
                mob.noPhysics = this.tickCount < 20;
                if (this.tickCount >= 20) {
                    mob.stopRiding();
                }
            }
            if (this.tickCount > 20 || entity == null) {
                this.discard();
            }
        }
    }
    @Override
    public void remove(RemovalReason reason) {
        super.remove(reason);
        if (this.getFirstPassenger() instanceof Mob mob) {
            mob.setNoAi(false);
            mob.noPhysics = false;
        }
    }

    @Override
    public void positionRider(Entity entity) {
        entity.setPos(this.position().add(0, ((double) this.tickCount / 20 - 1) * entity.getBbHeight(), 0));
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }
    @Override
    protected void defineSynchedData() {
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        tag.putInt("lift_tick", this.tickCount);
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        this.tickCount = tag.getInt("lift_tick");
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return new ClientboundAddEntityPacket(this);
    }
}
