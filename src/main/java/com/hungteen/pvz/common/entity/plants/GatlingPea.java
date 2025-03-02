package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.api.interfaces.IAdvancedPlant;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.IEntityPacketHandler;
import com.hungteen.pvz.common.entity.ai.goal.ShooterTargetGoal;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

public class GatlingPea extends Repeater implements PlayerRideableJumping, IEntityPacketHandler, IAdvancedPlant {

    public AnimationState controlledAnimationState = new AnimationState();
    private boolean playerFire = false;
    public static int MAX_OVERHEAT = 750;
    protected static final EntityDataAccessor<Integer> OVERHEATING = SynchedEntityData.defineId(GatlingPea.class, EntityDataSerializers.INT);
    protected static final EntityDataAccessor<Boolean> FUSING = SynchedEntityData.defineId(GatlingPea.class, EntityDataSerializers.BOOLEAN);

    public static final String LOW_BUDGET_SKILL_NAME = "skill.pvz.gatling_pea.low_budget_configuration";
    public static final String RAPID_DEPLOYMENT_SKILL_NAME = "skill.pvz.plant.rapid_deployment";
    public static List<Skill> staticSkillList = List.of(
            new Skill(PUNCH_SKILL_NAME, PVZItems.VENTUS_ESSENCE, 8, 8, 150, 0),
            new Skill(LOW_BUDGET_SKILL_NAME, PVZItems.LUX_ESSENCE, 4, 4, -250, -1000),
            new Skill(FIRE_SKILL_NAME, PVZItems.IGNIS_ESSENCE, 4, 3, 100, 0).avoidSkills(LOW_BUDGET_SKILL_NAME),
            new Skill(RAPID_DEPLOYMENT_SKILL_NAME, PVZItems.ORIGIN_ESSENCE, 16, 4, 150, 0)
    );
    public GatlingPea(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.removeGoal(shooterAttackGoal);
        this.goalSelector.addGoal(1, new GatlingAttackGoal(this));
        this.targetSelector.removeGoal(targetGoal);
        this.targetSelector.addGoal(1, new GatlingTargetGoal(this));
    }

    public void LookAtLookingAngleOf(Entity entity) {
        this.setRot(this.getYRot() + ((entity.getYRot() % 360F - this.getYRot() + 180F) % 360F - 180F) * 0.2F, entity.getXRot());
        this.yBodyRot = this.getYRot();
        this.yHeadRot = this.yBodyRot;
    }

    public int getOverheat() {
        return entityData.get(OVERHEATING);
    }
    public void setOverheat(int value) {
        entityData.set(OVERHEATING, value);
    }

    public boolean getFusing() {
        return entityData.get(FUSING);
    }
    public void setFusing(boolean value) {
        entityData.set(FUSING, value);
    }

    @Override
    public boolean canShoot() {
        return ! entityData.get(FUSING);
    }
    @Override
    public void shootBullet() {
        this.performShoot(SHOOT_OFFSET, 0, 0, true,
                this.getOverheat() > MAX_OVERHEAT * 0.67 ? (this.getOverheat() - MAX_OVERHEAT * 0.67) / 25 : 0);
        this.setOverheat(this.getOverheat() + 12 * (this.getFirstPassenger() instanceof Player player && player.isCreative() ? 0 : 1));
        if (getOverheat() > MAX_OVERHEAT && ! this.entityData.get(FUSING)) {
            this.entityData.set(FUSING, true);
        }
    }

    public Set<Integer> shootTimes() {
        return this.getFirstPassenger() instanceof Player ?
                Set.of(0, 1 ,2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19) :
                Set.of(8, 10, 12, 14);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PeaShooter.createAttributes()
                .add(Attributes.ATTACK_KNOCKBACK, 0.3D);
    }

    @Override
    public void baseTick() {
        if (! EntityUtil.attributeHasModifierOfUUID(this, Attributes.ATTACK_KNOCKBACK, ATTRIBUTE_MODIFIER_UUID)) {
            this.getAttribute(Attributes.ATTACK_KNOCKBACK).addTransientModifier(new AttributeModifier(ATTRIBUTE_MODIFIER_UUID, "skill bonus", 0.25, AttributeModifier.Operation.ADDITION));
        }
        super.baseTick();
        if (level.isClientSide && random.nextInt(3) == 0 && this.getOverheat() > MAX_OVERHEAT * 0.67 || this.entityData.get(FUSING)) {
            final Vec3 vec = new Vec3(- Math.sin(yBodyRot / 360 * 2 * Math.PI) * 0.6, 0, Math.cos(yBodyRot / 360 * 2 * Math.PI) * 0.6);
            level.addParticle(ParticleTypes.SMOKE, getX() + vec.x + (random.nextFloat() - 0.5) * 0.25,
                    getY() + getEyeHeight() + (random.nextFloat() - 0.5) * 0.25,
                    getZ() + vec.z + (random.nextFloat() - 0.5) * 0.25, 0, 0, 0);
            if (this.getOverheat() > MAX_OVERHEAT) {
                level.addParticle(ParticleTypes.EXPLOSION, getX() + vec.x + (random.nextFloat() - 0.5) * 0.5,
                        getY() + getEyeHeight() + (random.nextFloat() - 0.5) * 0.5,
                        getZ() + vec.z + (random.nextFloat() - 0.5) * 0.5, 0, 0, 0);
            }
        }
        if (! (this.getFirstPassenger() instanceof Player player)) {
            playerFire = false;
        } else if (level.isClientSide) {
            if (this.controlledAnimationState.isStarted()) {
                boolean usingSpyGlass = PlayerRenderer.getArmPose((AbstractClientPlayer) player, InteractionHand.MAIN_HAND) == HumanoidModel.ArmPose.SPYGLASS ||
                        PlayerRenderer.getArmPose((AbstractClientPlayer) player, InteractionHand.OFF_HAND) == HumanoidModel.ArmPose.SPYGLASS;
                if (this.getFirstPassenger().xRot < 21 && this.getFirstPassenger().xRot > -21) {
                    this.getFirstPassenger().xRot = (float) Math.max(-20 - random.nextFloat(), Math.min(20 + random.nextFloat(), this.getFirstPassenger().xRot - random.nextFloat() * 1.5 * (usingSpyGlass ? 0.2 : 1)));
                } else {
                    this.getFirstPassenger().xRot += random.nextFloat() - 0.5;
                }
                this.getFirstPassenger().yRot -= (random.nextFloat() * 1 - 0.5) * (usingSpyGlass ? 0.2 : 1);
            }
            if (this.getFirstPassenger() != null) {
                this.setYBodyRot(this.getYRot());
            }
        }
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getOverheat() > 0) {
            this.setOverheat(this.getOverheat() - (this.getOverheat() < MAX_OVERHEAT * 0.67 && ! entityData.get(FUSING) ? 2 : 1));
        } else {
            this.entityData.set(FUSING, false);
        }
    }

    @Override
    /** shoot cd is also affected by {@link FUSING}.*/
    public int getShootCD() {
        return this.getFirstPassenger() instanceof Player ? 21 : 40;
    }

    @Override
    public Vec3 getShootAngle(Entity target, double forwardOffset, double rightOffset, double heightOffset) {
        return this.getFirstPassenger() instanceof Player ? this.getLookAngle().normalize() :
                super.getShootAngle(target, forwardOffset, rightOffset, heightOffset);
    }

    @Override
    public float getBulletSpeed() {
        return (this.getFirstPassenger() instanceof Player ? 1.5F : 1F) * super.getBulletSpeed();
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_219422_) {
        super.onSyncedDataUpdated(p_219422_);
        if (POSE.equals(p_219422_)) {
            if (entityData.get(POSE)) {
                this.idleAnimationState.stop();
                if (this.getFirstPassenger() instanceof Player) {
                    this.controlledAnimationState.start(this.tickCount);
                    this.shootAnimationState.stop();
                } else {
                    this.shootAnimationState.start(this.tickCount);
                    this.controlledAnimationState.stop();
                }
            } else {
                this.shootAnimationState.stop();
                this.controlledAnimationState.stop();
                this.idleAnimationState.start(this.tickCount);
            }
        }
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(OVERHEATING, 0);
        this.entityData.define(FUSING, false);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand handIn) {
        if (getPassengers().isEmpty() && ! player.isShiftKeyDown()) {
            if (level.isClientSide) {
                sendPVZPacketToServer();
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else {
            return super.mobInteract(player, handIn);
        }
    }
    @Override
    public MutableComponent customPositionSafe(PVZResourceEvent.CheckPlantConditionEvent event, Level level, BlockPos pos, Direction direction, boolean isPlanting) {
        if (isPlanting) {
            if (hasSkill(RAPID_DEPLOYMENT_SKILL_NAME) || (event != null && event.getEntity() != null && event.getEntity().isCreative())) {
                return super.customPositionSafe(event, level, pos, direction, true);
            }
            return Component.translatable("hint.pvz.plant.can_only_plant_on", this.getName(), PVZEntities.REPEATER.get().getDescription());
        }
        return super.customPositionSafe(event, level, pos, direction, false);
    }
    @Override
    public MutableComponent customVehicleSafe(PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        if (isPlanting && event != null) {
            if (event.cost > PVZPlayerCapability.getValue(event.getEntity(), event.resource)) {
                return Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource));
            }
        }
        if (isPlanting) {
            if (hasSkill(RAPID_DEPLOYMENT_SKILL_NAME)) {
                return super.customVehicleSafe(event, target, true);
            }
            if (target.getType() == PVZEntities.REPEATER.get()) {
                GatlingPea gatlingPea = ((Mob) target).convertTo(PVZEntities.GATLING_PEA.get(), true);
                if (gatlingPea != null) {
                    gatlingPea.setSkillVal(this.getSkillVal());
                    if (event != null) {
                        gatlingPea.getCapability(PVZEntityCapability.CAP).ifPresent((cap) -> cap.setOwner(event.getEntity()));
                    }
                    if (this.hasCustomName()) {
                        gatlingPea.setCustomName(this.getCustomName());
                    }
                }
                this.discard();
                return null;
            }
            return Component.translatable("hint.pvz.plant.can_only_plant_on", this.getName(), PVZEntities.REPEATER.get().getDescription());
        }
        //when not is planting.
        return super.customVehicleSafe(event, target, false);
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Fusing", entityData.get(FUSING));
        tag.putInt("Overheating", entityData.get(OVERHEATING));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("Fusing")) {
            entityData.set(FUSING, tag.getBoolean("Fusing"));
        }
        if (tag.contains("Overheating")) {
            entityData.set(OVERHEATING, tag.getInt("Overheating"));
        }
    }


    //about riding.
    @Override
    protected void removePassenger(Entity entity) {
        super.removePassenger(entity);
        this.setAttackTime(getShootCD());
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0;
    }
    @Override
    public void positionRider(Entity entity) {
        entity.setPos(this.getPosition(0).add(this.getLookAngle().normalize().scale(-0.5)));
    }

    @Override
    public boolean shouldRiderSit() {
        return false;
    }

    @Override
    public void onPlayerJump(int p_21696_) {
    }
    @Override
    public boolean canJump() {
        return true;
    }
    @Override
    public void handleStartJump(int p_21695_) {
        playerFire = !playerFire;
        if (getAttackTime() < shootAnimLength()) {
            setAttackTime(0);
        }
    }
    @Override
    public void handleStopJump() {
    }

    @Override
    public void handlePVZPacket(ServerPlayer player, int val) {
        if (! hasSkill(LOW_BUDGET_SKILL_NAME)) {
            if (EntityUtil.isTeammate(this, player)) {
                player.moveTo(getX(), getY(), getZ(), getYRot(), 0.0F);
                player.startRiding(this);
                this.setTarget(null);
                this.setAttackTime(30);
            }
        }
    }

    //goals.

    private static class GatlingAttackGoal extends ShooterAttackGoal {

        public GatlingAttackGoal(GatlingPea shooter) {
            super(shooter);
        }
        @Override
        public boolean canUse() {
            //looking control.
            LivingEntity target = this.shooter.getTarget();
            if (shooter.isVehicle() && shooter.getFirstPassenger() instanceof Player player) {
                ((GatlingPea) shooter).LookAtLookingAngleOf(player);
            } else if (EntityUtil.isEntityValid(target)) {
                this.shooter.getLookControl().setLookAt(target.getX(), target.getY(), target.getZ());
            }
            //countdown.
            final int time = this.shooter.getAttackTime();
            if (time != this.shooter.shootAnimLength() || (shooter.canShoot() &&
                    (EntityUtil.isEntityValid(target) ||
                    (this.shooter.getFirstPassenger() instanceof Player && ((GatlingPea) shooter).playerFire)))) {
                this.shooter.setAttackTime(time > 0 ? time - 1 : this.shooter.getShootCD());
            }
            shooter.getEntityData().set(POSE, (this.shooter.getAttackTime() < this.shooter.shootAnimLength()));
            //can shoot.
            return this.shooter.canShoot();
        }
    }

    private static class GatlingTargetGoal extends ShooterTargetGoal{

        public GatlingTargetGoal(ShooterPlant mobIn) {
            super(mobIn);
        }

        @Override
        public boolean canUse() {
            if (mob.getFirstPassenger() instanceof Player) {
                mob.setTarget(null);
                this.targetMob = null;
                this.target = null;
                return false;
            } else {
                return super.canUse();
            }
        }
    }
}
