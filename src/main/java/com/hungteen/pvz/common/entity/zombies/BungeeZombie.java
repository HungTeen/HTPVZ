package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.entity.Hook;
import com.hungteen.pvz.common.entity.bullet.ArrowWithATarget;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZSoundEvents;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.MathUtil;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.function.BiConsumer;

public class BungeeZombie extends PVZZombie implements VibrationListener.VibrationListenerConfig {
private final DynamicGameEventListener<VibrationListener> dynamicGameEventListener;

    public BungeeZombie(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationListener(new EntityPositionSource(this, this.getEyeHeight()), 8, this, (VibrationListener.ReceivingEvent)null, 0.0F, 0));
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

    public void tick() {
        super.tick();
        if (this.tickCount % 10 == 0 && this.getHangingPosition() != null && this.level.getBlockState(getHangingPosition()).getCollisionShape(level, getHangingPosition()).isEmpty()) {
            this.setHangingPosition(null);
        }
        if (level instanceof ServerLevel serverlevel) {
            if (this.getHangingPosition() == null && ! this.getPassengers().isEmpty()) {
                this.getPassengers().forEach(Entity::stopRiding);
            }
            this.dynamicGameEventListener.getListener().tick(serverlevel);
        }
    }

    @Override
    public boolean shouldListen(ServerLevel p_223872_, GameEventListener listener, BlockPos p_223874_, GameEvent gameEvent, GameEvent.Context context) {
        if (gameEvent == GameEvent.PROJECTILE_LAND && context.sourceEntity() instanceof LivingEntity entity) {
            return entity.getLastHurtByMob() == this && EntityUtil.checkCanEntityBeAttack(this, entity) && ! (entity instanceof Slime);
        }
        return false;
    }

    @Override
    public void onSignalReceive(ServerLevel p_223865_, GameEventListener p_223866_, BlockPos pos, GameEvent p_223868_, @javax.annotation.Nullable Entity target, @Nullable Entity ownerOfTarget, float p_223871_) {
        if (! this.isDeadOrDying() && (this.getTarget() == null || this.getTarget().blockPosition().distSqr(pos) < target.blockPosition().distSqr(pos))) {
            if (target instanceof LivingEntity entity) {
                this.setTarget(entity);
            } else if (ownerOfTarget instanceof LivingEntity entity) {
                this.setTarget(entity);
            }
        }
    }
    @Override
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> p_219413_) {
        Level level = this.level;
        if (level instanceof ServerLevel serverlevel) {
            p_219413_.accept(this.dynamicGameEventListener, serverlevel);
        }
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        VibrationListener.codec(this).encodeStart(NbtOps.INSTANCE, this.dynamicGameEventListener.getListener()).resultOrPartial(PVZMod.LOGGER::error).ifPresent((p_219418_) -> {
            tag.put("listener", p_219418_);
        });
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("listener")) {
            VibrationListener.codec(this).parse(new Dynamic<>(NbtOps.INSTANCE, tag.getCompound("listener"))).resultOrPartial(PVZMod.LOGGER::error)
                    .ifPresent((p_219408_) -> this.dynamicGameEventListener.updateListener(p_219408_, this.level));
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
                if (this.zombie.tickCount % 50 <= 1 && zombie.getHangingPosition() != null && zombie.getPassengers().isEmpty()
                        && Math.abs(this.zombie.getDeltaMovement().y) < 0.1 && zombie.blockPosition().distSqr(zombie.getHangingPosition()) < zombie.ropeLengthSqr
                        && EntityUtil.isEntityValid(zombie.getTarget()) && this.zombie.distanceToSqr(zombie.getTarget()) > 5) {
                    ArrowWithATarget arrow = PVZEntities.ARROW_WITH_A_TARGET.get().create(level);
                    if (arrow != null) {
                        arrow.setOwner(this.zombie);
                        arrow.setDeltaMovement(0, -0.3, 0);
                        arrow.setPos(zombie.position().add(0, -0.5, 0));
                        level.addFreshEntity(arrow);
                    }
                }
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
                            zombie.playSound(PVZSoundEvents.BUNGEE_ZOMBIE_STEAL.get());
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