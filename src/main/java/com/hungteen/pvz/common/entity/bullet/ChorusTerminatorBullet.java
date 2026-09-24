package com.hungteen.pvz.common.entity.bullet;

import com.hungteen.pvz.common.entity.zombies.ChorusTerminatorBoss;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.common.register.PVZEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.entity.PartEntity;

import java.util.List;
import java.util.Random;

public class ChorusTerminatorBullet extends BaseBullet {

    private static final EntityDataAccessor<Boolean> IS_CHORUS = SynchedEntityData.defineId(ChorusTerminatorBullet.class, EntityDataSerializers.BOOLEAN);
    public double health;
    public Vec3 target;
    public Direction.Axis directionIdentity = null;
    public double angleIdentity = 361;

    public ChorusTerminatorBullet(EntityType<? extends BaseBullet> p_37248_, Level p_37249_) {
        super(p_37248_, p_37249_);
        this.health = 20;
        this.target = this.position();
    }

    public ChorusTerminatorBullet(Level worldIn, LivingEntity owner, Vec3 target, boolean isChorus) {
        super(PVZEntities.CHORUS_TERMINATOR_BULLET.get(), worldIn, owner);
        this.health = 20;
        setOwner(owner);
        this.target = target;
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
    }

    @Override
    public void tick() {
        super.tick();
        Vec3 dMov = this.getDeltaMovement();
        if (level.isClientSide) {
            if (this.tickCount % 3 == 0) this.level.addParticle(ParticleTypes.END_ROD, this.getX(), this.getY() + 0.45, this.getZ(), 0.0D, 0.0D, 0.0D);
        } else {
            BlockPos[] poses = new BlockPos[] {
                    new BlockPos(getX() - 0.5, getY(), getZ() - 0.5),
                    new BlockPos(getX() + 0.5, getY(), getZ() - 0.5),
                    new BlockPos(getX() + 0.5, getY(), getZ() + 0.5),
                    new BlockPos(getX() - 0.5, getY(), getZ() + 0.5),
            };
            for (BlockPos pos : poses) {
                if (this.level instanceof ServerLevel level)
                    level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5f, pos.getX() + 0.5f, pos.getX() + 0.5f, 1
                            , 0, 0, 0, 0);
                if (pos.getY() > this.level.getMaxBuildHeight() || pos.getY() <= this.level.getMinBuildHeight()) continue;
                BlockState state = level.getBlockState(pos);
                if (state.getBlock() == Blocks.CHORUS_PLANT || state.getBlock() == Blocks.CHORUS_FLOWER) this.level.removeBlock(pos, false);
            }
            for (BlockPos pos : poses) {
                if (this.level instanceof ServerLevel level)
                    level.sendParticles(ParticleTypes.FLAME, pos.getX() + 0.5f, pos.getX() + 0.5f, pos.getX() + 0.5f, 1
                            , 0, 0, 0, 0);
                if (pos.getY() > this.level.getMaxBuildHeight() || pos.getY() <= this.level.getMinBuildHeight()) continue;
                BlockState state = level.getBlockState(pos);
                if (! state.getCollisionShape(level, pos).isEmpty()) continue;
                if (state.getDestroySpeed(level, pos) < 4f) this.level.removeBlock(pos, false);
            }
            if (this.tickCount % 20 == 0) {
                for (int i = 0; i < 10; i ++) {
                    ((ServerLevel) this.level).sendParticles(ParticleTypes.END_ROD
                            , target.x * i / 10 + position().x * (10 - i) / 10
                            , target.y * i / 10 + position().y * (10 - i) / 10 + 0.3
                            , target.z * i / 10 + position().z * (10 - i) / 10,
                            1, 0, 0, 0, 0);
                }
            }
            boolean xzTest = Math.abs(dMov.x) > Math.abs(dMov.z);
            Direction.Axis xTest = Math.abs(dMov.y) > Math.abs(dMov.x) ? Direction.Axis.Y : Direction.Axis.X;
            Direction.Axis zTest = Math.abs(dMov.y) > Math.abs(dMov.z) ? Direction.Axis.Y : Direction.Axis.Z;
            Direction.Axis currentMovingDirection = xzTest ? xTest : zTest;
            if (directionIdentity == null) {
                directionIdentity = currentMovingDirection;
            }
            boolean shouldTurnZ = dMov.x / (this.getX() - this.target.x) > 0;
            boolean shouldTurnX = dMov.z / (this.getZ() - this.target.z) > 0;
            boolean rotating = angleIdentity <= 360 && Math.abs(((angleIdentity - getYRot()) % 360 + 540) % 360 - 180) > 45;
            if (! rotating) {
                if (directionIdentity != Direction.Axis.Y && Math.abs(this.getX() - this.target.x) < 2 && Math.abs(this.getZ() - this.target.z) < 2) {
                    directionIdentity = Direction.Axis.Y;
                } else if (directionIdentity == Direction.Axis.X && shouldTurnZ && ! shouldTurnX) {
                    directionIdentity = Direction.Axis.Z;
                    angleIdentity = (this.getZ() - this.target.z) > 0 ? (dMov.x > 0 ? 360 : -360) : (dMov.x > 0 ? -180 : 180);
                } else if (directionIdentity == Direction.Axis.Z && shouldTurnX && ! shouldTurnZ) {
                    directionIdentity = Direction.Axis.X;
                    angleIdentity = (this.getX() - this.target.x) > 0 ? (dMov.z > 0 ? -90 : 270) : (dMov.z > 0 ? 90 : -270);
                }
            }
            final double speed = 0.1;
            Vec3 toDMov;
            if (directionIdentity != Direction.Axis.Y && angleIdentity <= 360 && Math.abs(((angleIdentity - getYRot()) % 360 + 540) % 360 - 180) > 45) {
                double addSpeed = speed * speed / 1.5;
                toDMov = angleIdentity > 0 ? new Vec3(- dMov.z, 0, dMov.x) : new Vec3(dMov.z, 0, - dMov.x);
                toDMov = toDMov.normalize().multiply(addSpeed, addSpeed, addSpeed);
                double tmp1 = dMov.length() * 0.9 + speed * 0.1;
                toDMov = dMov.add(toDMov).normalize().multiply(tmp1, tmp1, tmp1);
            } else {
                toDMov = directionIdentity == Direction.Axis.Y ? new Vec3(0, - speed, 0)
                        : (directionIdentity == Direction.Axis.X ? new Vec3(speed * Math.signum(this.target.x - this.getX()), 0, 0)
                        : new Vec3(0, 0, speed * Math.signum(this.target.z - this.getZ())));
                toDMov = dMov.multiply(0.9, 0.9, 0.9).add(toDMov.multiply(0.1, 0.1, 0.1));
            }
            if (tickCount < 20 && this.getUUID().getMostSignificantBits() % 2 == 0) toDMov = toDMov.add(0, 0.05, 0);
            toDMov = new Vec3(
                    Math.abs(toDMov.x) < 1e-4 ? 0 : toDMov.x
                    , Math.abs(toDMov.y) < 1e-4 ? 0 : toDMov.y
                    , Math.abs(toDMov.z) < 1e-4 ? 0 : toDMov.z);
            this.setDeltaMovement(toDMov);
            dMov = getDeltaMovement();
        }
        this.setXRot((float) Math.atan2(dMov.y, Math.sqrt(dMov.x * dMov.x + dMov.z * dMov.z)) * 57.3F);
        this.setYRot((float) - Math.atan2(- dMov.x, - dMov.z) * 57.3F);
    }

    public boolean isChorus() {
        return this.entityData.get(IS_CHORUS);
    }
    public void setChorus(boolean value) {
        this.entityData.set(IS_CHORUS, value);
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
            List<Entity> entities = this.level.getEntities(this, this.getBoundingBox().inflate(2, 1, 2)
                    , e -> ! (e instanceof PartEntity));
            for (Entity entity : entities) {
                entity.hurt(PVZDamageSource.knockBack(PVZDamageSource.crash(boss), 0), 4);
                entity.setDeltaMovement(new Vec3(0, 0.3, 0).add(entity.getDeltaMovement()));
            }
            ((ServerLevel) level).sendParticles(ParticleTypes.EXPLOSION, this.position().x, this.position().y + 1, this.position().z, 15, 2, 1, 2, 0);
            ((ServerLevel) level).sendParticles(ParticleTypes.END_ROD, this.position().x, this.position().y + 1, this.position().z, 15, 2, 1, 2, 0.2);
            if (this.isChorus()) {
                if (boss != null && boss.homePos != null) {
                    entities = this.level.getEntities(this, this.getBoundingBox().inflate(3, 1, 3)
                            , e -> ! (e instanceof PartEntity) && e != boss);
                    if (! entities.isEmpty()) {
                        int teleportType = new Random().nextInt(4);
                        Vec3 center = Vec3.atCenterOf(boss.homePos);
                        for (Entity entity : entities) {
                            Vec3 pos = entity.position().subtract(center);
                            pos = switch (teleportType) {
                                case 0 -> new Vec3(center.x - pos.x, center.y + pos.y, center.z + pos.z);
                                case 1 -> new Vec3(center.x + pos.x, center.y + pos.y, center.z - pos.z);
                                case 2 -> new Vec3(center.x + pos.z, center.y + pos.y, center.z + pos.x);
                                default -> new Vec3(center.x - pos.z, center.y + pos.y, center.z - pos.x);
                            };
                            entity.teleportTo(pos.x, pos.y + 1, pos.z);
                        }
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
