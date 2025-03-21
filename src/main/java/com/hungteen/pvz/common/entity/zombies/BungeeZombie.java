package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.entity.Hook;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.MathUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class BungeeZombie extends PVZZombie {
    /**
     * The position the zombie ties itself on.
     */
    private static final EntityDataAccessor<BlockPos> TIED_POSITION = SynchedEntityData.defineId(BungeeZombie.class, EntityDataSerializers.BLOCK_POS);
    private static final EntityDataAccessor<Boolean> TIED = SynchedEntityData.defineId(BungeeZombie.class, EntityDataSerializers.BOOLEAN);
    public double ropeLengthSqr = 25;

    public BungeeZombie(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(TIED_POSITION, new BlockPos(0, -50, 0));
        this.entityData.define(TIED, false);
    }

    @Override
    public void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.addGoal(1, new BungeeZombieAttackGoal(this));
    }

    @Override
    public void positionRider(Entity entity) {
        if (this.needHangingPose()) {
            entity.setPos(this.position().add(0, entity.getBbHeight() > 1.5 ? -1 : -0.5, 0));
        } else {
            super.positionRider(entity);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PVZZombie.createAttributes().add(Attributes.MOVEMENT_SPEED, 0.2F).add(Attributes.ATTACK_DAMAGE, 0.5F);
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public boolean isHanging() {
        return this.getHangingPosition() != null || super.isHanging();
    }

    @Override
    public boolean needHangingPose() {
        return (this.getHangingPosition() != null && ! this.isPassenger() && EntityUtil.isLeavingGround(this))
                || super.needHangingPose();
    }

    public BlockPos getHangingPosition() {
        return this.entityData.get(TIED) ? this.entityData.get(TIED_POSITION) : null;
    }

    public void setHangingPosition(BlockPos pos) {
        if (pos == null) {
            this.entityData.set(TIED, false);
        } else {
            this.entityData.set(TIED, true);
            this.entityData.set(TIED_POSITION, pos);
        }
    }

    public void tick() {
        super.tick();
        if (this.tickCount % 10 == 0 && this.getHangingPosition() != null && this.level.getBlockState(getHangingPosition()).getCollisionShape(level, getHangingPosition()).isEmpty()) {
            this.setHangingPosition(null);
        }
        if (! level.isClientSide) {
            if (this.getHangingPosition() != null) {
            double actualLengthSq = this.blockPosition().offset(0, Math.ceil(this.getBbHeight()),0).distSqr(this.getHangingPosition());
                if (actualLengthSq > this.ropeLengthSqr) {
                    double stretched = Math.min(0.5, (actualLengthSq - this.ropeLengthSqr) / 10);
                    Vec3 vec3 = Vec3.atBottomCenterOf(this.getHangingPosition()).subtract(this.position()).normalize().multiply(stretched, stretched, stretched);
                    this.setDeltaMovement(this.getDeltaMovement().add(vec3));
                    if (actualLengthSq > Math.max(16, 4 * ropeLengthSqr)) {
                        this.setHangingPosition(null);
                    }
                }
            } else if (! this.getPassengers().isEmpty()) {
                this.getPassengers().forEach(Entity::stopRiding);
            }
        }
    }

    public class BungeeZombieAttackGoal extends Goal {
        public BungeeZombie zombie;
        private int stayTime = 0;


        public BungeeZombieAttackGoal(BungeeZombie zombie) {
            this.zombie = zombie;
        }

        @Override
        public boolean canUse() {
            return this.zombie.isHanging() || this.zombie.tickCount % 100 < 2;
        }

        @Override
        public void tick() {
            PVZMod.LOGGER.info("" + stayTime);
            Entity target = zombie.getTarget();
            boolean targetAvailable = EntityUtil.isEntityValid(target) && target.getBbWidth() <= 1;
            if (! this.zombie.isHanging() || this.zombie.tickCount % 200 <= 1) {
                if (targetAvailable) {
                    BlockPos pos = target.blockPosition().offset(0, Math.ceil(target.getBbHeight()),0);
                    for (; pos.getY() < this.zombie.level.getMaxBuildHeight(); pos = pos.above()) {
                        if (! level.getBlockState(pos).getCollisionShape(level, pos).isEmpty()) {
                            BlockPos nowPos = zombie.getHangingPosition();
                            if ((nowPos == null || ! nowPos.equals(pos)) && zombie.blockPosition().offset(0, Math.ceil(zombie.getBbHeight()), 0).getY() <= pos.getY()) {
                                Hook hook = new Hook(PVZEntities.HOOK.get(), this.zombie.level);
                                PVZMod.LOGGER.info("Hook launched!");
                                Vec3 zombieCenter = this.zombie.position().add(0, zombie.getBbHeight() / 2, 0);
                                hook.setPos(zombieCenter);
                                hook.setOwner(zombie);
                                Vec3 deltaPos = Vec3.atBottomCenterOf(pos).subtract(zombieCenter);
                                hook.shoot(deltaPos.x, deltaPos.y, deltaPos.z, 2, 0);
                                this.zombie.level.addFreshEntity(hook);
                            }
                        }
                    }
                }
            } else if (this.zombie.tickCount % 10 <= 1 && this.zombie.getHangingPosition() != null) {
                BlockPos pos = zombie.blockPosition().offset(0, Math.ceil(zombie.getBbHeight()),0);
                BlockPos pos1 = zombie.getHangingPosition();

                //about rope
                if ((zombie.getPassengers().isEmpty() || (targetAvailable && zombie.getPassengers().get(0) != target)) &&
                        MathUtil.horizontalDistSqrBetween(pos1, pos) < 9 && MathUtil.horizontalDistSqrOf(zombie.getDeltaMovement()) < 0.5 /*stable under hanging pos*/) {
                    zombie.ropeLengthSqr += Math.sqrt(ropeLengthSqr) * 5;
                    if (! EntityUtil.isLeavingGround(zombie)) {
                        if (! targetAvailable || MathUtil.horizontalDistSqrBetween(target.blockPosition(), pos1) >= 4) {
                            zombie.setHangingPosition(null);
                        }
                        for (Entity passenger : this.zombie.getPassengers()) {
                            passenger.stopRiding();
                        }
                    }
                } else if (zombie.getPassengers().isEmpty() || stayTime >= 2) {
                    zombie.ropeLengthSqr -= Math.sqrt(ropeLengthSqr) * 3;
                }
                if (zombie.ropeLengthSqr <= 0.5) {
                    zombie.ropeLengthSqr = 0.5;

                } else if (zombie.getHangingPosition() != null && targetAvailable && zombie.getPassengers().isEmpty()) {
                    double maxLen = Math.pow(this.zombie.getHangingPosition().getY() - target.getY() - this.zombie.getBbHeight() - 1, 2);
                    if (zombie.ropeLengthSqr > maxLen) {
                        zombie.ropeLengthSqr = maxLen;
                    }
                }
                if (zombie.blockPosition().offset(0, Math.ceil(zombie.getBbHeight()), 0).getY() > pos.getY()) {
                    zombie.setHangingPosition(null);
                    for (Entity passenger : this.zombie.getPassengers()) {
                        passenger.stopRiding();
                    }
                }
                //about attack
                if (targetAvailable) {
                    boolean nearTarget = target.distanceToSqr(zombie.position()) < (zombie.getBbWidth() + target.getBbWidth()) * 1.2;
                    if (target.getVehicle() != zombie && stayTime > 1) {
                        stayTime = -5;
                    }
                    if (nearTarget) {
                        if (! (target.getVehicle() instanceof BungeeZombie) && this.zombie.needHangingPose() && stayTime >= 1) {
                            target.stopRiding();
                            target.boardingCooldown = 0;
                            target.startRiding(zombie);
                            stayTime = 0;
                        } else if (stayTime > 10) {
                            for (Entity rider : zombie.getPassengers()) {
                                rider.stopRiding();
                                rider.setDeltaMovement(random.nextFloat() * 0.5 - 0.25, 0, random.nextFloat() * 0.5 - 0.25);
                                zombie.setTarget(null);
                                stayTime = -5;
                            }
                        } else {
                            stayTime ++;
                        }
                    }
                } else if (stayTime > 0) {
                    stayTime = 0;
                }
                if (stayTime < 0) {
                    stayTime ++;
                }
            }
        }
    }
}