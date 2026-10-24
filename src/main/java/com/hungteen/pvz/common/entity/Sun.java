package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.api.events.AbsorbSunEvent;
import com.hungteen.pvz.api.interfaces.ISun;
import com.hungteen.pvz.api.interfaces.ISunAbsorber;
import com.hungteen.pvz.api.interfaces.ISunContainer;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZParticles;
import com.hungteen.pvz.common.register.PVZSoundEvents;
import com.hungteen.pvz.common.world.PVZPlayerWrapper;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.network.NetworkHooks;

import javax.annotation.Nullable;
import java.util.Optional;

public class Sun extends Entity implements ISunAbsorber, ISunContainer, ISun {

    public static final float SUN_FALL_SPEED = 0.03F;
    public static final int DEFAULT_AMOUNT = 50;
    public static final int MAX_LIVE_TICK = 750;
    public LivingEntity controller = null;
    public Vec3 ColorBase = new Vec3(255, 235, 30);
    public Vec3 ColorChange = new Vec3(0, 10, 15);
    private ISunAbsorber attractedBy;
    private static final EntityDataAccessor<Integer> AMOUNT = SynchedEntityData.defineId(Sun.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> LIVE_TICK = SynchedEntityData.defineId(Sun.class, EntityDataSerializers.INT);

    public Sun(EntityType<?> p_19870_, Level p_19871_) {
        super(p_19870_, p_19871_);
        setAmount(DEFAULT_AMOUNT);
        this.setNoGravity(true);
    }

    @Nullable
    public static Sun spawnByAmount(Level level, int amount, BlockPos pos, Vec3 speed) {
        Sun sun = PVZEntities.SUN.get().create(level);
        if (sun != null) {
            sun.setAmount(amount);
            sun.moveTo(Vec3.atCenterOf(pos));
            sun.setDeltaMovement(speed);
            level.addFreshEntity(sun);
        }
        return sun;
    }

    /**
     * drop multiple suns with effects. if each > 0 drop in each, else drop with 50/25/15/5.
     */
    public static void spawnSunsWithEffectsByAmount(Level level, BlockPos pos, int amount, int each, float speed) {
        for (int i = amount; i > 0; ) {
            int singleSunAmount = i;
            singleSunAmount = each > 0 ? each : singleSunAmount > 50 ? 50
                : singleSunAmount > 25 ? 25 : singleSunAmount > 15 ? 15 : Math.min(singleSunAmount, 5);
            i -= singleSunAmount;
            spawnSunWithEffects(level, singleSunAmount, pos, speed);
        }
    }

    /**
     * spawn sun entity in range randomly with specific amount.
     */
    @Nullable
    public static Sun spawnSunWithEffects(Level level, int amount, BlockPos pos, float maxSpeed) {
        Sun sun = spawnByAmount(level, amount, pos, new Vec3((level.getRandom().nextFloat() - 0.5) * maxSpeed,
            level.getRandom().nextFloat() * 0.1 + 0.6 * maxSpeed, (level.getRandom().nextFloat() - 0.5) * maxSpeed));
        if (sun != null) {
            // 生成成功才有粒子
            ((ServerLevel)level).sendParticles(PVZParticles.SUN.get(), pos.getX() + 0.5, pos.getY() + 1,
                pos.getZ() + 0.5, amount / 10, 0, 0, 0, 0.1);
        }
        return sun;
    }

    @Override
    public int getAmount() {
        return this.entityData.get(AMOUNT);
    }

    @Override
    public void onSunConsume(int amount) {
        if (amount > 0) {
            this.playSound(PVZSoundEvents.COLLECT_SUN.get());
        }
        if (this.getAmount() <= amount) {
            // 阳光数量不足，直接移除
            this.discard();
        }
    }

    @Override
    public void setAmount(int num) {
        this.entityData.set(AMOUNT, num);
    }

    public int getLiveTick() {
        return this.entityData.get(LIVE_TICK);
    }

    public void setLiveTick(int value) {
        this.entityData.set(LIVE_TICK, value);
    }

    @Override
    public boolean canAttractThis(ISunAbsorber sunAbsorber) {
        if (this.tickCount < 20) {
            return false;
        }
        if (this.distanceToSqr(sunAbsorber.getAbsorbPosition()) > 64.0D) {
            // 距离太远了
            return false;
        }
        // TODO PT: 射线检测有没有方块挡住了
        return sunAbsorber.canAbsorb(this);
    }

    @Override
    public ISunAbsorber getAttractor() {
        return this.attractedBy;
    }

    @Override
    public boolean trySetAttractor(ISunAbsorber candidateAbsorber) {
        if (this.attractedBy == null || this.compareAbsorber(this.attractedBy, candidateAbsorber) < 0) {
            this.attractedBy = candidateAbsorber;
            return true;
        }
        return false;
    }

    @Override
    public void onAbsorbedBy(ISunAbsorber absorber) {
        AbsorbSunEvent event = new AbsorbSunEvent(this, absorber, AbsorbSunEvent.Phase.Start);
        MinecraftForge.EVENT_BUS.post(event);
        if (!event.isCanceled()) {
            absorber.onAbsorb(this);
            event = new AbsorbSunEvent(this, absorber, AbsorbSunEvent.Phase.End);
            MinecraftForge.EVENT_BUS.post(event);
        }
    }

    @Override
    public void onAbsorb(ISun sun) {
        if (sun instanceof Entity entity) {
            // 阳光合并同类项
            setAmount(getAmount() + sun.getAmount());
            float width = ((Entity)sun).getBbWidth() / 2;
            ((ServerLevel)level).sendParticles(PVZParticles.SUN.get(), entity.getX() + width,
                entity.getY() + entity.getBbHeight() / 2 + width, entity.getZ() + width, sun.getAmount() / 20, width,
                width, width, 0.1);
            ((Entity)sun).remove(Entity.RemovalReason.DISCARDED);
        }
    }

    @Override
    public Vec3 getAbsorbPosition() {
        return getEyePosition(0.5F);
    }

    @Override
    public boolean canAbsorb(ISun sun) {
        if (sun instanceof Sun sun1) {
            return getAmount() < 250 && sun.getAmount() < 250 && sun1.getId() < getId()
                && this.distanceToSqr(sun1.getAbsorbPosition()) < 4;
        } else {
            return false;
        }
    }

    @Override
    public void baseTick() {
        super.baseTick();

        // about sun disappear.
        if (!level.isClientSide) {
            if (PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.sunDisappear)) {
                this.setLiveTick(this.getLiveTick() + 1);
                if (this.getLiveTick() >= this.getMaxLiveTick()) {
                    this.remove(Entity.RemovalReason.DISCARDED);
                }
            } else {
                this.setLiveTick(0);
            }
        }
        // natural fall.
        if (!this.onGround && !this.isInWater()) {
            double speedY = this.getDeltaMovement().y;
            if (speedY > -SUN_FALL_SPEED) {
                speedY -= SUN_FALL_SPEED / 2;
            } else {
                speedY = -SUN_FALL_SPEED;
            }
            this.setDeltaMovement(this.getDeltaMovement().x * 0.94, speedY, this.getDeltaMovement().z * 0.94);
        } else {
            this.setDeltaMovement(new Vec3(0, 0, 0));
        }
        // update absorber
        // TODO PT : 此处为什么不是服务端专属逻辑？？？
        if (this.attractedBy != null && !this.attractedBy.canAbsorb(this)) {
            // 吸引不了了，则取消吸引
            this.attractedBy = null;
        }
        if (this.attractedBy == null || (this.tickCount + this.getId()) % 50 == 0) {
            // 重新选择吸引者，先比较优先级，再比较距离
            Optional<ISunAbsorber> candidateAbsorberOpt = level.getEntities(this, this.getBoundingBox().inflate(6)).stream().map(e -> {
                    if (e instanceof Player player) {
                        return new PVZPlayerWrapper(player);
                    }
                    return e;
                }).filter(ISunAbsorber.class::isInstance).map(ISunAbsorber.class::cast).filter(this::canAttractThis)
                .min(this::compareAbsorber);
            candidateAbsorberOpt.ifPresent(this::trySetAttractor);
        }
        // being attracted.
        if (this.attractedBy != null) {
            if (!level.isClientSide && distanceToSqr(attractedBy.getAbsorbPosition()) < 0.8F) {
                this.onAbsorbedBy(attractedBy);
            }
            Vec3 vec3 = this.attractedBy.getAbsorbPosition().subtract(this.position());
            double d0 = vec3.lengthSqr();
            if (d0 < 25.0D) {
                double d1 = 1.0D - Math.sqrt(d0) / 5.0D;
                this.setDeltaMovement(this.getDeltaMovement().add(vec3.normalize().scale(d1 * d1 * 0.3D)));
            }
        }
        this.move(MoverType.SELF, this.getDeltaMovement());
        //about controlled color.
        if (this.controller != null && (this.controller.isSpectator() || (!this.controller.isAlive()))) {
            this.controller = null;
        }
        if (this.controller == null) {
            ColorBase = new Vec3(255, 235, 30);
            ColorChange = new Vec3(0, 10, 15);
        }
    }

    private Integer compareAbsorber(ISunAbsorber e1, ISunAbsorber e2) {
        if (e1.getAbsorbPriority() == e2.getAbsorbPriority()) {
            return Double.compare(this.distanceToSqr(e1.getAbsorbPosition()), this.distanceToSqr(e2.getAbsorbPosition()));
        }
        return Integer.compare(e1.getAbsorbPriority(), e2.getAbsorbPriority());
    }

    @Override
    public EntityDimensions getDimensions(Pose poseIn) {
        float w = this.getAmount() * 1f / 200 + 0.2f;
        return EntityDimensions.scalable(w, w);
    }

    public int getMaxLiveTick() {
        return MAX_LIVE_TICK;
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(AMOUNT, 1);
        this.entityData.define(LIVE_TICK, 0);
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("Amount")) {
            setAmount(tag.getInt("Amount"));
        }
        if (tag.contains("SunLiveTick")) {
            this.setLiveTick(tag.getInt("SunLiveTick"));
        }
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putInt("Amount", this.getAmount());
        tag.putInt("SunLiveTick", this.getLiveTick());
    }

    @Override
    public Packet<?> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }
}
