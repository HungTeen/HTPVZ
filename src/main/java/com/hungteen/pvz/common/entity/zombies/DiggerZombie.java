package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.client.sound.DiggerZombieSoundInstance;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.PickaxeItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;

import javax.annotation.Nullable;
import java.util.UUID;
import java.util.function.BiConsumer;

public class DiggerZombie extends PVZZombie implements VibrationListener.VibrationListenerConfig {
    public boolean renderHat = true;
    private final DynamicGameEventListener<VibrationListener> dynamicGameEventListener;
    private final UUID TRACKING_MODIFIER = UUID.fromString("c5321b12-712e-7474-5b37-f162e6b49f56");
    public DiggerZombie(EntityType<? extends Zombie> p_34271_, Level level) {
        super(p_34271_, level);
        if (! level.isClientSide) {
            this.jumpControl = new DiggerZombieJumpControl(this);
        }
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationListener(new EntityPositionSource(this, this.getEyeHeight()), 24, this, (VibrationListener.ReceivingEvent)null, 0.0F, 0));
    }
    public static AttributeSupplier.Builder createAttributes() {
        return PVZZombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 25)
                .add(Attributes.FOLLOW_RANGE, 8);
    }
    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        this.goalSelector.addGoal(1, new DiggerZombieDigGoal(this));
    }
    public boolean hasHelmet() {
        return this.getHealth() >= this.getMaxHealth() * 0.8F;
    }
    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        SpawnGroupData spawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, data, tag);
        this.setItemInHand(InteractionHand.MAIN_HAND, Items.IRON_PICKAXE.getDefaultInstance());
        return spawnGroupData;
    }

    @Override
    public void tick() {
        super.tick();
        if (level instanceof ServerLevel serverlevel) {
            this.dynamicGameEventListener.getListener().tick(serverlevel);
            AttributeInstance attribute = this.getAttribute(Attributes.FOLLOW_RANGE);
            if (EntityUtil.isEntityValid(this.getTarget())) {
                if (attribute.getModifier(TRACKING_MODIFIER) == null) {
                    attribute.addTransientModifier(new AttributeModifier(TRACKING_MODIFIER, "tracking", 8, AttributeModifier.Operation.ADDITION));
                }
            } else {
                attribute.removeModifier(TRACKING_MODIFIER);
            }
        } else if (this.getPose() == Pose.SWIMMING) {
            for (int i = 0; i < 5; i ++) {
                this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()),
                        this.getX() + (this.random.nextDouble() - 0.5D) - this.getDeltaMovement().x / 2,
                        this.getY() + this.getBbHeight() + 0.6D,
                        this.getZ() + (this.random.nextDouble() - 0.5D) - this.getDeltaMovement().z / 2,
                        (this.random.nextDouble() - 0.5) * 6.0D, 2D, (this.random.nextDouble() - 0.5) * 4.0D);
            }
        }
    }

    //vibration listener.
    @Override
    public boolean shouldListen(ServerLevel p_223872_, GameEventListener p_223873_, BlockPos p_223874_, GameEvent event, GameEvent.Context context) {
        return EntityUtil.checkCanEntityBeAttack(this, context.sourceEntity()) && (this.getPose() != Pose.STANDING || this.getTarget() == null);
    }
    @Override
    public void onSignalReceive(ServerLevel p_223865_, GameEventListener p_223866_, BlockPos pos, GameEvent p_223868_, @javax.annotation.Nullable Entity target, @Nullable Entity ownerOfTarget, float p_223871_) {
        if (! this.isDeadOrDying() && this.getTarget() == null) {
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
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        if (DATA_POSE.equals(data)) {
            if (getPose() == Pose.SWIMMING && level.isClientSide) {
                DiggerZombieSoundInstance.add(this);
            }
        }
    }

    public static class DiggerZombieDigGoal extends Goal {
        final DiggerZombie zombie;
        public DiggerZombieDigGoal(DiggerZombie zombie) {
            super();
            this.zombie = zombie;
        }
        @Override
        public boolean canUse() {
            return true;
        }
        @Override
        public void tick() {
            if (this.zombie.getTarget() != null && zombie.getMainHandItem().getItem() instanceof PickaxeItem && zombie.hasHelmet() &&
                    (zombie.distanceToSqr(zombie.getTarget()) > 9 || ! zombie.level.getBlockState(zombie.blockPosition().above()).isAir())) {
                switch (zombie.getPose()) {
                    case STANDING -> zombie.setPose(Pose.DIGGING);
                    case DIGGING -> {
                        if (! EntityUtil.isLeavingGround(zombie)) {
                            Vec3 delta = zombie.getDeltaMovement();
                            zombie.setDeltaMovement(delta.x, Math.min(-0.2, delta.y), delta.z);
                            BlockState blockState = zombie.level.getBlockState(new BlockPos(zombie.position().add(0, zombie.getBbHeight(), 0)));
                            if (! blockState.getCollisionShape(zombie.level, new BlockPos(zombie.position().add(0, zombie.getBbHeight(), 0))).isEmpty()) {
                                zombie.setDeltaMovement(zombie.getDeltaMovement().multiply(1, 0, 1));
                                zombie.setPose(Pose.SWIMMING);
                            }
                            zombie.noPhysics = true;
                            zombie.setNoGravity(true);
                        } else {
                            zombie.noPhysics = false;
                            zombie.setNoGravity(false);
                        }
                    }
                    case SWIMMING -> {
                        zombie.lookAt(zombie.getTarget(), 10, 10);
                        double maxY = zombie.getTarget().getY();
                        double y = (int) (zombie.getY() + zombie.getBbHeight());
                        BlockPos pos = zombie.blockPosition();
                        while (y < maxY) {
                            BlockPos pos1 = new BlockPos(pos.getX(), y, pos.getZ());
                            VoxelShape shape = zombie.level.getBlockState(pos1).getCollisionShape(zombie.level, pos1);
                            if (zombie.level.getBlockState(pos1).isAir() || shape.max(Direction.Axis.Y) == 0) {
                                break;
                            }
                            y += shape.max(Direction.Axis.Y);
                            if (zombie.getBbHeight() + y < Math.ceil(y) + shape.min(Direction.Axis.Y)) {
                                break;
                            }
                        }
                        y = Math.min(maxY, y) - zombie.getBbHeight() - (zombie.isBaby() ? 0 : 0.6) - zombie.getY();
                        Vec3 delta = zombie.getDeltaMovement();
                        delta = delta.multiply(1, 0, 1).add(0, Math.signum(y) * 0.03, 0);
                        zombie.setDeltaMovement(delta);
                        if (EntityUtil.isLeavingGround(zombie)) {
                            zombie.setNoGravity(false);
                            zombie.noPhysics = false;
                            zombie.setPose(Pose.STANDING);
                        }
                        zombie.noPhysics = true;
                        zombie.setNoGravity(true);
                        if (! zombie.level.getBlockState(zombie.blockPosition().above()).isAir()) {
                            zombie.getNavigation().stop();
                            double speed = zombie.getAttribute(Attributes.MOVEMENT_SPEED).getValue();
                            Vec3 delta1 = zombie.getTarget().position().subtract(zombie.position()).normalize().multiply(speed, speed, speed);
                            zombie.moveTo(zombie.position().add(delta1));
                        }
                    }
                    case EMERGING -> {
                        zombie.getNavigation().stop();
                        if (! EntityUtil.isLeavingGround(zombie)) {
                            Vec3 delta = zombie.getDeltaMovement();
                            if (delta.y < 0.24) {
                                delta = delta.multiply(1, 0, 1).add(0, 0.24, 0);
                            }
                            zombie.setDeltaMovement(delta);
                            zombie.noPhysics = true;
                            zombie.setNoGravity(true);
                        } else {
                            zombie.setNoGravity(false);
                            zombie.noPhysics = false;
                            zombie.setPose(Pose.STANDING);
                        }
                    }
                    default -> {
                        if (EntityUtil.isLeavingGround(zombie)) {
                            zombie.setNoGravity(false);
                            zombie.noPhysics = false;
                        }
                    }
                }
            } else {
                switch (zombie.getPose()) {
                    case EMERGING -> {
                        zombie.getNavigation().stop();
                        if (! EntityUtil.isLeavingGround(zombie)) {
                            Vec3 delta = zombie.getDeltaMovement();
                            if (delta.y < 0.24) {
                                delta = delta.multiply(1, 0, 1).add(0, 0.24, 0);
                            }
                            zombie.setDeltaMovement(delta);
                            zombie.noPhysics = true;
                            zombie.setNoGravity(true);
                        } else {
                            zombie.setNoGravity(false);
                            zombie.noPhysics = false;
                            zombie.setPose(Pose.STANDING);
                        }
                    }
                    case SWIMMING -> zombie.setPose(Pose.EMERGING);
                    default -> {
                        zombie.setNoGravity(false);
                        zombie.noPhysics = false;
                        if (zombie.getPose() != Pose.STANDING) {
                            zombie.setPose(Pose.EMERGING);
                        }
                    }
                }
            }
        }
    }

    public static class DiggerZombieJumpControl extends JumpControl {
        final DiggerZombie zombie;
        public DiggerZombieJumpControl(DiggerZombie zombie) {
            super(zombie);
            this.zombie = zombie;
        }

        @Override
        public void jump() {
            if (zombie.getPose() == Pose.STANDING) {
                super.jump();
            }
        }
    }
}
