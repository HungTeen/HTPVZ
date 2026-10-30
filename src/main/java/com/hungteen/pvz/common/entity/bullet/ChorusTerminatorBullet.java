package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.entity.zombies.ChorusTerminatorBoss;
import com.hungteen.pvz.common.register.OtherRegisters;
import com.hungteen.pvz.common.register.PVZEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;

import java.util.List;
import java.util.stream.Stream;

public class ChorusTerminatorBullet extends BaseBullet {

    private static final EntityDataAccessor<Boolean> IS_CHORUS = SynchedEntityData.defineId(ChorusTerminatorBullet.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Vec3> TARGET = SynchedEntityData.defineId(ChorusTerminatorBullet.class, OtherRegisters.VEC3.get());
    private static final EntityDataAccessor<Vec3> HEADING = SynchedEntityData.defineId(ChorusTerminatorBullet.class, OtherRegisters.VEC3.get());
    private static final double SPEED = 0.25;
    private static final double TURN_STEP = 9.5;
    private static final double TARGET_RANGE = 2.5;
    public double health;
    public Direction.Axis directionIdentity = null;
    private double turnRemaining = 0;

    public ChorusTerminatorBullet(EntityType<? extends BaseBullet> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
        this.health = 20;
        this.setTarget(this.position());
    }

    public ChorusTerminatorBullet(Level worldIn, LivingEntity owner, Vec3 target, boolean isChorus) {
        super(PVZEntities.CHORUS_TERMINATOR_BULLET.get(), worldIn, owner);
        this.health = 20;
        setOwner(owner);
        this.setTarget(target);
        setChorus(isChorus);
        this.knockBackStrengh = (float) owner.getAttribute(Attributes.ATTACK_KNOCKBACK).getValue();
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    protected int getMaxLiveTick() {
        return 1000;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(IS_CHORUS, false);
        this.entityData.define(TARGET, Vec3.ZERO);
        this.entityData.define(HEADING, new Vec3(0, 0, 1));
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 dMov = this.getDeltaMovement();
        if (level.isClientSide) {
            if (this.tickCount % 3 == 0) this.level.addParticle(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.45, this.getZ(), 0.0D, 0.0D, 0.0D);
            if (this.tickCount % 30 == 0) {
                Vec3 target = this.getTarget();
                for (int i = 0; i < 10; i ++) {
                    this.level.addParticle(ParticleTypes.END_ROD
                            , target.x * i / 10 + position().x * (10 - i) / 10
                            , target.y * i / 10 + position().y * (10 - i) / 10 + 0.3
                            , target.z * i / 10 + position().z * (10 - i) / 10,
                            0, 0, 0);
                }
            }
            dMov = this.getHeading();
        } else {
            BlockPos[] poses = new BlockPos[] {
                    new BlockPos(getX() - 0.5, getY(), getZ() - 0.5),
                    new BlockPos(getX() + 0.5, getY(), getZ() - 0.5),
                    new BlockPos(getX() + 0.5, getY(), getZ() + 0.5),
                    new BlockPos(getX() - 0.5, getY(), getZ() + 0.5),
            };
            for (BlockPos pos : poses) {
                if (pos.getY() > this.level.getMaxBuildHeight() || pos.getY() <= this.level.getMinBuildHeight()) continue;
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() == Blocks.CHORUS_PLANT || state.getBlock() == Blocks.CHORUS_FLOWER) this.level.removeBlock(pos, false);
            }
            if (this.directionIdentity == null) {
                this.directionIdentity = Math.abs(dMov.x) >= Math.abs(dMov.z) ? Direction.Axis.X : Direction.Axis.Z;
            }
            Vec3 target = this.getTarget();
            double dx = target.x - this.getX();
            double dz = target.z - this.getZ();
            if (Math.abs(dx) < TARGET_RANGE && Math.abs(dz) < TARGET_RANGE) {
                this.directionIdentity = Direction.Axis.Y;
                this.turnRemaining = 0;
            }
            if (this.directionIdentity == Direction.Axis.Y) {
                dMov = dMov.multiply(0.9, 0.9, 0.9).add(this.axisVelocity(dx, dz).multiply(0.1, 0.1, 0.1));
            } else {
                double horizontalSpeed = Math.sqrt(dMov.x * dMov.x + dMov.z * dMov.z);
                Vec3 horizontal = horizontalSpeed > 1.0E-4
                        ? new Vec3(dMov.x / horizontalSpeed * SPEED, dMov.y, dMov.z / horizontalSpeed * SPEED)
                        : this.axisVelocity(dx, dz);
                if (this.turnRemaining != 0) {
                    dMov = this.turn(horizontal);
                } else {
                    double along = this.directionIdentity == Direction.Axis.X ? horizontal.x : horizontal.z;
                    double offset = this.directionIdentity == Direction.Axis.X ? this.getX() - target.x : this.getZ() - target.z;
                    if (along * offset > 0) {
                        Direction.Axis next = this.directionIdentity == Direction.Axis.X ? Direction.Axis.Z : Direction.Axis.X;
                        double currentYaw = Math.toDegrees(-Math.atan2(-horizontal.x, -horizontal.z));
                        double targetYaw = next == Direction.Axis.X ? (dx >= 0 ? 90 : -90) : (dz >= 0 ? 180 : 0);
                        double delta = Mth.wrapDegrees(targetYaw - currentYaw);
                        this.directionIdentity = next;
                        this.turnRemaining = Math.abs(delta) < 1.0E-4 ? 0 : (delta > 0 ? delta - 360 : delta + 360);
                        dMov = this.turn(horizontal);
                    } else {
                        dMov = horizontal.multiply(0.9, 0.9, 0.9).add(this.axisVelocity(dx, dz).multiply(0.1, 0.1, 0.1));
                    }
                }
            }
            if (tickCount < 20 && this.getUUID().getMostSignificantBits() % 2 == 0) dMov = dMov.add(0, 0.05, 0);
            this.setDeltaMovement(dMov);
            this.setHeading(dMov);
        }
        this.setXRot((float) Math.toDegrees(Math.atan2(dMov.y, Math.sqrt(dMov.x * dMov.x + dMov.z * dMov.z))));
        this.setYRot((float) Math.toDegrees(- Math.atan2(- dMov.x, - dMov.z)));
    }

    private Vec3 turn(Vec3 movement) {
        double angle = Math.min(Math.abs(this.turnRemaining), TURN_STEP);
        double radians = Math.toRadians(Math.copySign(angle, this.turnRemaining));
        double cos = Math.cos(radians);
        double sin = Math.sin(radians);
        Vec3 rotated = new Vec3(movement.x * cos - movement.z * sin, movement.y, movement.z * cos + movement.x * sin);
        this.turnRemaining -= Math.copySign(angle, this.turnRemaining);
        if (Math.abs(this.turnRemaining) <= 1e-6) {
            this.turnRemaining = 0;
            Vec3 target = this.getTarget();
            return this.axisVelocity(target.x - this.getX(), target.z - this.getZ());
        }
        return rotated;
    }

    private Vec3 axisVelocity(double dx, double dz) {
        if (this.directionIdentity == Direction.Axis.Y) {
            return new Vec3(0, - SPEED, 0);
        }
        if (this.directionIdentity == Direction.Axis.X) {
            return new Vec3(dx >= 0 ? SPEED : - SPEED, 0, 0);
        }
        return new Vec3(0, 0, dz >= 0 ? SPEED : - SPEED);
    }

    private Vec3 getHeading() {
        return this.entityData.get(HEADING);
    }

    private void setHeading(Vec3 heading) {
        this.entityData.set(HEADING, heading);
    }

    public boolean isChorus() {
        return this.entityData.get(IS_CHORUS);
    }
    public void setChorus(boolean value) {
        this.entityData.set(IS_CHORUS, value);
    }

    public Vec3 getTarget() {
        return this.entityData.get(TARGET);
    }

    public void setTarget(Vec3 target) {
        this.entityData.set(TARGET, target);
    }

    @Override
    public boolean hurt(DamageSource damageSource, float amount) {
        if (this.health <= 0) return false;
        this.health -= amount;
        Entity entity = damageSource.getDirectEntity();
        if (entity != null) {
            Vec3 vec = entity.position().add(0, entity.getEyeHeight(), 0);
            float knockBackAmount = amount / 20 + 0.6F;
            this.setDeltaMovement(this.getDeltaMovement()
                    .add(this.position().add(0, this.getBbHeight() / 2, 0).subtract(vec).normalize().multiply(knockBackAmount, knockBackAmount, knockBackAmount)));
        }
        if (this.health <= 0) {
            explode();
        }
        return true;
    }

    private void explode() {
        if (! this.level.isClientSide) {
            ChorusTerminatorBoss boss = this.getOwner() instanceof ChorusTerminatorBoss tmp ? tmp : null;
            List<Entity> entities = this.level.getEntities(this, this.getBoundingBox().inflate(3, 1, 3)
                    , e -> ! (e instanceof PartEntity));
            for (Entity entity : entities) {
                entity.setDeltaMovement(new Vec3(0, 0.3, 0).add(entity.getDeltaMovement()));
            }
            ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, this.position().x, this.position().y + 1, this.position().z, 15, 2, 1, 2, 0);
            ((ServerLevel) level).sendParticles(ParticleTypes.END_ROD, this.position().x, this.position().y + 1, this.position().z, 15, 2, 1, 2, 0.2);
            if (this.isChorus()) {
                if (boss != null && boss.homePos != null) {
                    boolean teleportType = this.getUUID().getMostSignificantBits() % 2 == 0;
                    Vec3 center = Vec3.atCenterOf(boss.homePos);
                    entities = entities.stream().filter(e -> e instanceof Projectile || e.isPickable()).toList();
                    Vec3 pos = this.position().subtract(center);
                    pos = teleportType ? new Vec3(center.x + pos.x, center.y + pos.y, center.z - pos.z)
                            : new Vec3(center.x - pos.x, center.y + pos.y, center.z + pos.z);
                    entities = Stream.concat(entities.stream()
                            , this.level.getEntities(this
                                    , this.getBoundingBox().move(pos.subtract(this.position())).inflate(3, 1, 3)
                                    , e -> (e instanceof Projectile || e.isPickable()) && ! (e instanceof PartEntity)).stream()
                    ).toList();
                    for (Entity entity : entities) {
                        pos = entity.position().subtract(center);
                        pos = teleportType ? new Vec3(center.x + pos.x, center.y + pos.y, center.z - pos.z)
                                : new Vec3(center.x - pos.x, center.y + pos.y, center.z + pos.z);
                        entity.teleportTo(pos.x, pos.y + 1, pos.z);
                    }
                }
            } else {
                AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.level, this.getX(), this.getY(), this.getZ());
                areaeffectcloud.setRadius(2f);
                areaeffectcloud.setDuration(80);
                areaeffectcloud.setWaitTime(0);
                if (this.getOwner() instanceof LivingEntity living) areaeffectcloud.setOwner(living);
                areaeffectcloud.addEffect(new MobEffectInstance(MobEffects.LEVITATION, 120));
                this.level.addFreshEntity(areaeffectcloud);
            }
            BlockState state = level.getBlockState(this.getOnPos().below());
            BlockState aboveState = level.getBlockState(this.getOnPos());
            if (state.isSuffocating(level, this.getOnPos().below()) && state.getBlock().defaultDestroyTime() < 4f
                    && (aboveState.isAir() || aboveState.is(BlockTags.REPLACEABLE_PLANTS))) {
                level.setBlock(this.getOnPos().below(), Blocks.END_STONE.defaultBlockState(), 3);
                level.setBlock(this.getOnPos(), Blocks.CHORUS_FLOWER.defaultBlockState(), 3);
            }
            this.discard();
        }
    }

    @Override
    public void onHitBlock(BlockHitResult result) {
        super.onHitBlock(result);
        explode();
    }

    @Override
    public boolean canHitEntity(Entity entity) {
        return false;
    }
}
