package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.entity.ai.goal.ShooterTargetGoal;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Set;

public class GatlingPea extends Repeater implements PlayerRideableJumping {

    //TODO   ------------------  NOT COMPLETED!     GrassCarp is still working on it.   -----------------

    public AnimationState controlledAnimationState = new AnimationState();
    private boolean playerPressedFire = false;
    protected static final EntityDataAccessor<Integer> OVERHEATING = SynchedEntityData.defineId(GatlingPea.class, EntityDataSerializers.INT);

    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.pea_shooter.punch", PVZItems.VENTUS_ESSENCE, 8, 4, 150, 0),
            new Skill("skill.pvz.gatling_pea.low_budget_configuration", PVZItems.ORIGIN_ESSENCE, 8, 4, -200, -1000),
            new Skill("skill.pvz.pea_shooter.fire_shooter", PVZItems.IGNIS_ESSENCE, 4, 4, 150, 0).avoidSkills(0, 1)
    );
    public GatlingPea(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Repeater.createAttributes()
                .add(Attributes.ATTACK_DAMAGE, 5D);
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.removeGoal(shooterAttackGoal);
        this.goalSelector.addGoal(1, new GatlingAttackGoal(this));
    }

    protected Set<Integer> shootTimes() {
        return this.getFirstPassenger() instanceof Player ? Set.of(1, 4, 6, 8, 10, 12 ,15, 17) :Set.of(8, 10, 12, 14);
    }

    public int getOverheat() {
        return entityData.get(OVERHEATING);
    }
    public void setOverheat(int value) {
        entityData.set(OVERHEATING, value);
    }

    @Override
    public void shootBullet() {
        this.performShoot(SHOOT_OFFSET, 0, 0, true,
                this.getOverheat() > 500 ? (double) (this.getOverheat() - 500) / 50 : 0);
        this.setOverheat(this.getOverheat() + 20);
    }

    @Override
    public void baseTick() {
        super.baseTick();
        if (level.isClientSide && random.nextInt(5) == 0 && this.getOverheat() > 500) {
            final Vec3 vec = this.getLookAngle().normalize().scale(0.5);
            level.addParticle(ParticleTypes.SMOKE, getX() + vec.x, getY() + getEyeHeight(), getZ() + vec.z, 0, 0, 0);
        }
        if (! (this.getFirstPassenger() instanceof Player)) {
            playerPressedFire = false;
        }
    }

    @Override
    public void travel(Vec3 vec3) {
        if (this.isAlive() && this.isVehicle() && this.getFirstPassenger() instanceof Player player) {
            this.setRot(this.getYRot() + ((player.getYRot() % 360F - this.getYRot() + 180F) % 360F - 180F) * 0.2F, 0);
            this.yBodyRot = this.getYRot();
            this.yHeadRot = this.yBodyRot;
        }
        super.travel(vec3);
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (this.getOverheat() > 0) {
            this.setOverheat(this.getOverheat() - (this.getOverheat() < 500 ? 2 : 1));
        }
    }

    @Override
    public int getShootCD() {
        return this.getOverheat() > 750 ? 500 : this.getFirstPassenger() instanceof Player ? 20 : 40;
    }

    @Override
    public Vec3 getShootAngle(Entity target) {
        return this.getFirstPassenger() instanceof Player ? this.getLookAngle().normalize() : EntityUtil.getNormalisedVector2d(this, target);
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
    }


    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (! hasSkill("skill.pvz.gatling_pea.low_budget_configuration")) {
            if (PVZOwnedCapability.isTeammate(this, player) && getPassengers().isEmpty() && player.getItemInHand(hand).isEmpty()) {
                player.moveTo(getX(), getY(), getZ(), getYRot(), 0.0F);
                player.startRiding(this);
                return InteractionResult.sidedSuccess(this.level.isClientSide);
            }
        }
        return super.mobInteract(player, hand);
    }
    @Override
    public MutableComponent isPositionSafe(Level level, BlockPos onPos, boolean isPlanting) {
        if (isPlanting) {
            return Component.translatable("hint.pvz.plant.can_only_plant_on", this.getName(), PVZEntities.REPEATER.get().getDescription());
        }
        return super.isPositionSafe(level, onPos, false);
    }
    @Override
    public MutableComponent isVehicleSafe(Entity target, boolean isPlanting) {
        if (isPlanting) {
            if (target.getType() == PVZEntities.REPEATER.get()) {
                GatlingPea gatlingPea = ((Mob) target).convertTo(PVZEntities.GATLING_PEA.get(), true);
                if (this.hasCustomName() && gatlingPea != null) {
                    gatlingPea.setCustomName(this.getCustomName());
                }
                this.discard();
                return null;
            }
            return Component.translatable("hint.pvz.plant.can_only_plant_on", this.getName(), PVZEntities.REPEATER.get().getDescription());
        }
        return super.isVehicleSafe(target, false);
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("ShootTime", getAttackTime(this));

    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("ProduceTime")) {
            setAttackTime(this, tag.getInt("ShootTime"));
        }
    }

    @Override
    public boolean shouldRiderSit()
    {
        return false;
    }

    //to hide gui.
    @Override
    public void onPlayerJump(int p_21696_) {
    }
    @Override
    public boolean canJump() {
        return true;
    }
    @Override
    public void handleStartJump(int p_21695_) {
        playerPressedFire = true;
    }
    @Override
    public void handleStopJump() {
        playerPressedFire = false;
    }

    public static class GatlingAttackGoal extends ShooterAttackGoal {

        public GatlingAttackGoal(GatlingPea shooter) {
            super(shooter);
        }
        @Override
        public boolean canUse() {
            if (! (this.shooter.getFirstPassenger() instanceof Player) || ((GatlingPea) shooter).playerPressedFire) {
                if (! this.shooter.canShoot()) {
                    return false;
                }
                final int time = this.shooter.getAttackTime(this);
                if (! (time == this.shooter.shootAnimLength()) ||
                        (EntityUtil.isEntityValid(shooter.getTarget()) && ! (this.shooter.getFirstPassenger() instanceof Player)) ||
                        ((GatlingPea) shooter).playerPressedFire) {
                    this.shooter.setAttackTime(this, time > 0 ? time - 1 : this.shooter.getShootCD());
                }
                shooter.getEntityData().set(POSE, (this.shooter.getAttackTime(this) < this.shooter.shootAnimLength()));
                return EntityUtil.isEntityValid(shooter.getTarget());
            }
            return false;
        }
    }
}
