package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.common.entity.Sun;
import com.hungteen.pvz.common.entity.ai.ChorusTerminatorActivitiesGoal;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.entity.PartEntity;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class ChorusTerminatorPart extends PartEntity<ChorusTerminatorBoss> implements Enemy {
    public final String name;
    public final Type type;
    private final EntityDimensions size;
    public final boolean needSync;
    public Vec3 intention;
    private @Nullable Vec3 intentionOld;
    int cooldown = 0; //server only.

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
    protected void readAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return this.type == Type.LEG && ! EntityUtil.isLeavingGround(this);
    }
    @Override
    public boolean canCollideWith(Entity entity) {
        return (this.type != Type.LEG && entity instanceof ChorusTerminatorPart p && p.type != Type.LEG || EntityUtil.isLeavingGround(this))
                && super.canCollideWith(entity);
    }

    @Override
    public boolean hurt(@NotNull DamageSource damageSource, float amount) {
        if (this.type == Type.EYE && ! this.level.isClientSide) Sun.spawnSunWithEffects(this.level, 50, this.blockPosition(), 0.3f);
        return ! this.isInvulnerableTo(damageSource) && this.getParent().hurt(this, damageSource, amount);
    }

    public void setIntention(Vec3 intention) {
        if (this.type == Type.BODY) {
            this.move(MoverType.SELF, intention.subtract(this.getIntention()));
        }
        this.intention = intention;
    }

    public Vec3 getIntention() {
        if (intention == null) {
            return switch (this.type) {
                case BODY -> new Vec3(0, 3, 0);
                case MOUTH -> new Vec3(0, 0, 0);
                case EYE -> new Vec3(2, 1.1, 2);
                default -> this.position();
            };
        }
        return intention;
    }

    public Vec3 getIntentionOld() {
        return intentionOld == null ? getIntention() : intentionOld;
    }

    @Override
    public void baseTick() {
        if (! this.getParent().isNoAi()) {
            double gravity = this.isNoGravity() || this.getParent().isNoGravity() ? 0 : getParent().getAttributeValue(ForgeMod.ENTITY_GRAVITY.get());
            BlockState bState = this.level.getBlockState(this.getBlockPosBelowThatAffectsMyMovement());
            float friction = (bState.getBlock() instanceof LiquidBlock || bState.isAir())
                    ? (this.type == Type.BODY ? 0.6f : 0.99f) : bState.getFriction(level, this.getBlockPosBelowThatAffectsMyMovement(), this);
            Vec3 distToIntention = this.position().subtract(getIntention());
            if (this.type == Type.LEG) {
                if (level.isClientSide) {
                    Vec3 intention = this.getIntention();
                    this.level.addParticle(ParticleTypes.BUBBLE, intention.x, intention.y, intention.z, 0, 0, 0);
                    for (int i = 0; i < 5; i ++) {
                        this.level.addParticle(ParticleTypes.BUBBLE
                                , intention.x * (5 - i) / 5 + position().x * i / 5
                                , intention.y * (5 - i) / 5 + position().y * i / 5
                                , intention.z * (5 - i) / 5 + position().z * i / 5
                                , 0, 0, 0);
                    }
                }
                ChorusTerminatorActivitiesGoal.Action action = this.getParent().getAction();
                boolean dashing = action == ChorusTerminatorActivitiesGoal.Action.DASH || action == ChorusTerminatorActivitiesGoal.Action.PRE_DASH;
                boolean jumping = action == ChorusTerminatorActivitiesGoal.Action.JUMP || action == ChorusTerminatorActivitiesGoal.Action.PRE_JUMP;
                gravity *= dashing ? 2.5 : 1;
                double distSqr = this.position().distanceToSqr(this.getParent().position());
                if (distSqr >= 64) {
                    if (distSqr > 121) {
                        this.setPos(this.getParent().position().add(this.getParent().position().subtract(this.position()).normalize()
                                .multiply(6, 6, 6)));
                    } else {
                        double speed = Math.min(0.2, (Math.sqrt(distSqr) - 8) / 20);
                        this.setDeltaMovement(this.getParent().position().subtract(this.position()).normalize()
                                .multiply(speed, speed, speed));
                    }
                }
                boolean leavingGround = EntityUtil.isLeavingGround(this);
                if (cooldown == 0) {
                    double distLength = distToIntention.length();
                    if (distToIntention.lengthSqr() > 1) {
                        this.cooldown = dashing ? 10 : 24;
                        if (! leavingGround) {
                            int t = jumping ? 30 : dashing ? 5 : 8;
                            if (distToIntention.y < - 0.5) t -= (int) distToIntention.y * (distLength > 4 ? 3 : 2);
                            if (distLength > 4) t += (int) (distLength - 4);
                            if (level.getBlockState(new BlockPos(position().subtract(distToIntention.normalize()))).canOcclude()) t += 5;
                            this.setDeltaMovement(this.getDeltaMovement().add(
                                    - distToIntention.x / t, gravity * ((float) t / 2 + 0.5) - distToIntention.y / t, - distToIntention.z / t));
                            friction = 1;
                        }
                    }
                } else {
                    if (position().y - getParent().position().y > 5) {
                        this.setPos(position().multiply(1, 0, 1).add(0, getParent().position().y + 5, 0));
                        this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0, 1).add(0, - gravity, 0));
                    }
                    //destroying blocks & entities
                    if (leavingGround) {
                        int x = (int) (this.getX() + this.getDeltaMovement().x);
                        int z = (int) (this.getZ() + this.getDeltaMovement().z);
                        for (int y = 1; y <= 3; y ++) {
                            BlockPos[] poses = new BlockPos[] {
                                    new BlockPos(x - 0.5, getY() + y, z - 0.5),
                                    new BlockPos(x + 0.5, getY() + y, z - 0.5),
                                    new BlockPos(x + 0.5, getY() + y, z + 0.5),
                                    new BlockPos(x - 0.5, getY() + y, z + 0.5),
                            };
                            for (BlockPos pos : poses) {
                                if (this.level instanceof ServerLevel level)
                                    level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5f, pos.getX() + 0.5f, pos.getX() + 0.5f, 1
                                            , 0, 0, 0, 0);
                                if (pos.getY() > this.level.getMaxBuildHeight() || pos.getY() <= this.level.getMinBuildHeight()) continue;
                                BlockState state = level.getBlockState(pos);
                                if (! state.getCollisionShape(level, pos).isEmpty()) continue;
                                if (state.getDestroySpeed(level, pos) < 4f) this.level.removeBlock(pos, false);
                            }
                        }
                        List<LivingEntity> list = this.level.getEntitiesOfClass(LivingEntity.class, this.getBoundingBox().inflate(0.5)
                                , e -> e.getLastHurtByMob() != this.getParent() && e.hurtTime <= 0 && e != this.getParent() && e.isAlive());
                        list.forEach(e -> {
                            e.hurt(PVZDamageSource.knockBack(PVZDamageSource.crash(this.getParent()), 4f), 4f);
                            e.setDeltaMovement(e.getDeltaMovement().add(0, 0.3, 0));
                            if (dashing && e.isAlive() && e.getDeltaMovement().lengthSqr() < 0.1f) {
                                getParent().act(ChorusTerminatorActivitiesGoal.Action.STIFF);
                            }
                        });
                    }
                    //adjust speed
                    if (this.getDeltaMovement().y < 0) {
                        double t = Math.max(1, (this.getY() - this.getIntention().y) / (-this.getDeltaMovement().y) + gravity * 8);
                        for (int i = 0; i < 3; i++) {
                            t = Math.max(1, (this.getY() - this.getIntention().y) / (-this.getDeltaMovement().y + gravity * t / 2));
                        }
                        double xExtraSpeed = (this.getX() + this.getDeltaMovement().x * t - this.getIntention().x) / t;
                        double zExtraSpeed = (this.getZ() + this.getDeltaMovement().z * t - this.getIntention().z) / t;
                        double adjustSpeed = Math.min(0.1, Math.sqrt(xExtraSpeed * xExtraSpeed + zExtraSpeed + zExtraSpeed));
                        if (adjustSpeed > 0.01) this.setDeltaMovement(this.getDeltaMovement().subtract(
                                new Vec3(xExtraSpeed, 0, zExtraSpeed).normalize().multiply(adjustSpeed, adjustSpeed, adjustSpeed)));
                    }
                }
                if (cooldown > 0) cooldown -= 1;
            } else if (this.type == Type.BODY) {
                if (! this.level.isClientSide) {
                    double tmpGravity = this.getParent().getAttributeValue(ForgeMod.ENTITY_GRAVITY.get());
                    setDeltaMovement(this.getParent().getDeltaMovement().add(0, tmpGravity, 0));
                    Vec3 center = Vec3.ZERO;
                    for (ChorusTerminatorPart part : getParent().legs) {
                        center = center.add(part.position().add(this.getIntention()).subtract(this.position()));
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
                    if (this.tickCount % 3 == 0) {
                        for (int x = -3; x <= 3; x ++) {
                            for (int z = -3; z <= 3; z ++) {
                                for (int y = 0; y <= 5; y ++) {
                                    BlockPos pos = new BlockPos(this.position().add(x, y, z));
                                    if (this.level instanceof ServerLevel level)
                                        level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5f, pos.getX() + 0.5f, pos.getX() + 0.5f, 1
                                                , 0, 0, 0, 0);
                                    if (pos.getY() > this.level.getMaxBuildHeight() || pos.getY() <= this.level.getMinBuildHeight()) continue;
                                    BlockState state = level.getBlockState(pos);
                                    if (! state.getCollisionShape(level, pos).isEmpty()) continue;
                                    if (state.getDestroySpeed(level, pos) < 4) this.level.removeBlock(pos, false);
                                }
                            }
                        }
                    }
                }
            } else if (this.type == Type.EYE) {
                this.setDeltaMovement(Vec3.ZERO);
                float angle = getParent().getVisualRotationYInDegrees();
                Vec3 intention = this.getIntention();
                this.setPos(getParent().body.position().add(-Math.sin(angle / 57.3) * intention.x, intention.y, Math.cos(angle / 57.3) * intention.z));
            } else if (this.type == Type.MOUTH) {
                this.setDeltaMovement(Vec3.ZERO);
                this.setPos(getParent().body.position().add(this.getIntention()));
            }
            this.setDeltaMovement(getDeltaMovement().add(new Vec3(0, - gravity, 0)));
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().multiply(new Vec3(friction, friction, friction)));
        }
        this.xOld = this.position().x;
        this.yOld = this.position().y;
        this.zOld = this.position().z;
        this.intentionOld = this.getIntention();
        this.tickCount ++;
        if (this.tickCount > 60000) this.tickCount -= 60000;
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
