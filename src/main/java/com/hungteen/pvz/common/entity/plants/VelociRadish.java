package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.api.interfaces.*;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.ai.goal.AvoidTargetGoal;
import com.hungteen.pvz.common.entity.ai.goal.DisperseEnemyTargetGoal;
import com.hungteen.pvz.common.entity.ai.goal.FollowGroupLeaderGoal;
import com.hungteen.pvz.common.entity.ai.goal.GroupShareEnemyGoal;
import com.hungteen.pvz.common.entity.plants.base.SimplePlant;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZSeedPackets;
import com.hungteen.pvz.common.register.PVZSoundEvents;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

import static com.hungteen.pvz.common.entity.plants.base.SimplePlant.tryShovel;
import static net.minecraftforge.event.ForgeEventFactory.canMountEntity;

public class VelociRadish extends PathfinderMob implements ICanGroupUp, IPlant, INeedSafeSituation, IHaveSkills {
    private static final UUID ATTACK_MODIFIER_UUID = UUID.fromString("191e7725-e0a8-45cf-93ac-5a1749b36d03");
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("45e5f868-f733-423f-81b0-b3df87d3f266");
    private int animationTick = 0;
    private boolean animationChangeable = false;

    public static final String STRONG_SKILL_NAME = "skill.pvz.veloci_radish.veloci_nip";
    public static final String GROUP_SKILL_NAME = "skill.pvz.veloci_radish.clever_girls";

    public static List<Skill> staticSkillList = List.of(
            new Skill(STRONG_SKILL_NAME, PVZItems.ORIGIN_ESSENCE, 12, 8, 25, PVZSeedPackets.SLOW - PVZSeedPackets.FAST),
            new Skill(GROUP_SKILL_NAME, PVZItems.LUX_ESSENCE, 12, 8, 100, PVZSeedPackets.SLOW - PVZSeedPackets.FAST).avoidSkills(STRONG_SKILL_NAME)
    );
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState moveAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public final AnimationState disAppearAnimationState = new AnimationState();
    protected boolean firstUnsafeSituationMercy = true;
    protected boolean isDisappearing = false;
    protected static final EntityDataAccessor<Integer> POSE = SynchedEntityData.defineId(VelociRadish.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> ROOT = SynchedEntityData.defineId(VelociRadish.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> SKILL = SynchedEntityData.defineId(VelociRadish.class, EntityDataSerializers.INT);
    public VelociRadish(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
        this.setPathfindingMalus(BlockPathTypes.WATER, 8.0F);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 5)
                .add(Attributes.KNOCKBACK_RESISTANCE, 0)
                .add(Attributes.MOVEMENT_SPEED, 0.35)
                .add(Attributes.ATTACK_DAMAGE, 4)
                .add(Attributes.ATTACK_KNOCKBACK, 0.5)
                .add(Attributes.FOLLOW_RANGE, 16D);
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ROOT, false);
        this.entityData.define(POSE, 0);
        this.entityData.define(SKILL, 0);
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AvoidTargetGoal(this,
                (entity -> entity instanceof Mob mob && mob.getTarget() == this &&
                        (this.getAttribute(Attributes.ATTACK_DAMAGE).getModifier(ATTACK_MODIFIER_UUID) == null)),
                6.0F, 1.0D, 1.0D));
        this.goalSelector.addGoal(1, new GroupShareEnemyGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(2, new TurnipAttackGoal(this, 1, true));
        this.goalSelector.addGoal(3, new FollowGroupLeaderGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new DisperseEnemyTargetGoal(this));
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Root", getEntityData().get(ROOT));
        tag.putBoolean("IsDisappearing", isDisappearing);
        saveSkills(tag);
        tag.putInt("TickCount", tickCount);

    }
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand handIn) {
        if (tryShovel(player, handIn, this)) {
            return level.isClientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
        } else {
            return super.mobInteract(player, handIn);
        }
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        readSkills(tag);
        if (tag.contains("Root")) {
            this.getEntityData().set(ROOT, tag.getBoolean("Root"));
        }
        if (tag.contains("IsDisappearing")) {
            this.isDisappearing = tag.getBoolean("IsDisappearing");
        }
        if (tag.contains("TickCount")) {
            this.tickCount = tag.getInt("TickCount");
        }
    }

    //IPlant
    @Override
    public EntityDataAccessor<Boolean> root() {
        return ROOT;
    }
    @Override
    public boolean takesCoincideDmg() {
        return false;
    }

    //skills
    @Override
    public List<Skill> getBasicStaticSkillList(){
        return staticSkillList;
    }
    @Override
    public int getSkillVal(Object obj) {
        return entityData.get(SKILL);
    }
    @Override
    public void setSkillVal(Object obj, int value) {
        entityData.set(SKILL, value);
    }

    //animation
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_219422_) {
        if (POSE.equals(p_219422_)) {
            if (entityData.get(POSE) == 1) {
                this.moveAnimationState.start(this.tickCount);
                this.attackAnimationState.stop();
                this.idleAnimationState.stop();
            } else if (entityData.get(POSE) == -1) {
                this.disAppearAnimationState.start(this.tickCount);
                this.moveAnimationState.stop();
                this.attackAnimationState.stop();
                this.idleAnimationState.stop();
            } else if (entityData.get(POSE) == 2) {
                this.attackAnimationState.start(this.tickCount);
                this.moveAnimationState.stop();
                this.idleAnimationState.stop();
            } else {
                this.idleAnimationState.start(this.tickCount);
                this.moveAnimationState.stop();
                this.attackAnimationState.stop();
            }
        }
        super.onSyncedDataUpdated(p_219422_);
    }

    public void setupPresentationAnim() {
        this.idleAnimationState.start(this.tickCount);
    }
    //overrides
    @Override
    public MutableComponent customPositionSafe(PVZResourceEvent.CheckPlantConditionEvent event, Level level, BlockPos pos, Direction direction, boolean isPlanting) {
        //resource check.
        if (isPlanting && event != null) {
            if (event.cost > PVZPlayerCapability.getValue(event.getEntity(), event.resource)) {
                return Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource));
            }
        }
        //position adjustment.
            //1. for replaceable plants and multi-face block like vine and glow lichen.
        if (level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() && direction != null) {
            pos = pos.offset(direction.getOpposite().getNormal());
        }
            //2. when clicked on sides of blocks, plant on relative place.
        Vec3i offset = direction == null ? Vec3i.ZERO : direction.getNormal();
        boolean isSide = direction != null && direction.getAxis() != Direction.Axis.Y;
        direction = getGrowDirection();
        pos = pos.offset(offset).offset(direction == null ? Vec3i.ZERO : getGrowDirection().getOpposite().getNormal());
        offset = direction == null ? Vec3i.ZERO : direction.getNormal();
        //collision check.
        AABB aabb = AABB.ofSize(new Vec3(pos.getX() + 0.5, pos.getY() + 1 + getBbHeight() / 2, pos.getZ() + 0.5), getBbWidth(), getBbHeight() - 0.0001, getBbWidth());
        if (BlockPos.betweenClosedStream(aabb).anyMatch((p_201942_) -> {
            BlockState blockstate = this.level.getBlockState(p_201942_);
            return !blockstate.isAir() && blockstate.isSuffocating(this.level, p_201942_) && Shapes.joinIsNotEmpty(blockstate.getCollisionShape(this.level, p_201942_).move((double)p_201942_.getX(), (double)p_201942_.getY(), (double)p_201942_.getZ()), Shapes.create(aabb), BooleanOp.AND);
        })) {
            return Component.translatable("hint.pvz.plant.no_enough_place");
        }
        //root block available check.
        if (! this.getEntityData().get(root()) || ! level.getBlockState(pos).isAir()) {
            BlockState state = level.getBlockState(pos);
            if (isPlanting) {
                //check
                if (state.getCollisionShape(level, pos).isEmpty()) {
                    return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), level.getBlockState(pos).getBlock().getName());
                }
                //final plant.
                this.moveTo(
                        pos.getX() + 0.5 + offset.getX(),
                        pos.getY() + (isSide ? 1 : direction == Direction.UP ? (state.getCollisionShape(level, pos).isEmpty() ?
                                (level.getFluidState(pos).isEmpty() ? 0: level.getFluidState(pos).getHeight(level, pos)) :
                                state.getCollisionShape(level, pos).bounds().maxY) : offset.getY()),
                        pos.getZ() + 0.5 + offset.getZ());
                ((ServerLevel)this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()), this.getX(), this.getY(), this.getZ(), 5, 0.0D, 0.0D, 0.0D, 0.15F);
            }
            return null;
        } else {
            return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), level.getBlockState(pos).getBlock().getName());
        }
    }
    public static boolean checkSpawnRules(EntityType<? extends LivingEntity> entityType, ServerLevelAccessor level, MobSpawnType mobSpawnType, BlockPos pos, RandomSource random) {
        return true;
    }

    @Override
    public MutableComponent customVehicleSafe(PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        //resource check.
        if (isPlanting && event != null) {
            if (event.cost > PVZPlayerCapability.getValue(event.getEntity(), event.resource)) {
                return Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource));
            }
        }
        //target unavailable.
        if (target == null) {
            return Component.translatable("hint.pvz.plant.entity_not_present");
        }
        //target is ICanBePlantedOn.
        if (target instanceof ICanBePlantedOn && ((ICanBePlantedOn) target).canHold(this, isPlanting)) {
            if (EntityUtil.isTeammate(this, target)) {
                if (isPlanting) {
                    if (canMountEntity(this, target, true)) {
                        this.moveTo(target.getX(), target.getY() + target.getPassengersRidingOffset(), target.getZ(), target.getYRot(), 0.0F);
                        return null;
                    } else {
                        return target.getFirstPassenger() != null ?
                                customVehicleSafe(event, target.getFirstPassenger(), true) :
                                Component.translatable("hint.pvz.plant.no_enough_place");
                    }
                }
                return null;
            }
            return Component.translatable("hint.pvz.plant.need_own_team");
        } else {
            return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), target.getName());
        }
    }
    @Nullable
    public ItemStack getPickResult() {
        return SimplePlant.getPickResult(this);
    }

    @Override
    public void tick() {
        if (!this.level.isClientSide && this.tickCount > 2400 && this.random.nextInt(15) == 0) {
            this.disappear();
        }
        // skill
        if (! level.isClientSide) {
            if (hasSkill(this, STRONG_SKILL_NAME)) {
                if (! EntityUtil.attributeHasModifierOfUUID(this, Attributes.ATTACK_DAMAGE, ATTACK_MODIFIER_UUID)) {
                    this.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(new AttributeModifier(ATTACK_MODIFIER_UUID, "skill bonus", 26, AttributeModifier.Operation.ADDITION));
                    this.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(new AttributeModifier(HEALTH_MODIFIER_UUID, "skill bonus", 15, AttributeModifier.Operation.ADDITION));
                    this.getAttribute(Attributes.MOVEMENT_SPEED).addTransientModifier(new AttributeModifier(HEALTH_MODIFIER_UUID, "skill bonus", 0.15, AttributeModifier.Operation.ADDITION));
                    this.heal(20);
                } else if (tickCount > 200) {
                    this.removeSkill(this, getSkillFromName(STRONG_SKILL_NAME));
                    ((ServerLevel) this.level).sendParticles(ParticleTypes.CAMPFIRE_COSY_SMOKE
                            , this.getX(), this.getY() + 0.5, this.getZ(), 10
                            , 0.3F, 0.3F, 0.3F, 0.01f);
                    this.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_MODIFIER_UUID);
                    this.getAttribute(Attributes.MAX_HEALTH).removeModifier(HEALTH_MODIFIER_UUID);
                    this.getAttribute(Attributes.MOVEMENT_SPEED).removeModifier(HEALTH_MODIFIER_UUID);
                }
            } else if (hasSkill(this, GROUP_SKILL_NAME)) {
                for (int i = 0; i < 3; i ++) {
                    VelociRadish turnip = PVZEntities.VELOCI_RADISH.get().create(level);
                    turnip.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                    ((ServerLevel) level).addFreshEntityWithPassengers(turnip);
                    if (hasCustomName()) {
                        turnip.setCustomName(this.getCustomName());
                    }
                    PVZEntityCapability cap = turnip.getCapability(PVZEntityCapability.CAP).orElse(null);
                    cap.setOwner(PVZEntityCapability.getOwner(this));
                    turnip.startFollowing(this);
                }
                this.removeSkill(this, getSkillFromName(GROUP_SKILL_NAME));
                this.setDeltaMovement(this.getDeltaMovement().add(0.1, 0 ,0));
            }
        }
        //check plant situation damage.
        firstUnsafeSituationMercy = SimplePlant.testPlantSafe(this, firstUnsafeSituationMercy);
        SimplePlant.testDisappear(this);
        //animation
        animationTick ++;
        if (entityData.get(POSE) == 0) {
            animationChangeable = animationTick == 24;
            animationTick = animationTick == 24 ? 0 : animationTick;
        } else if (entityData.get(POSE) == -1) {
            if (animationTick == 16) {
                this.discard();
            }
        } else {
            animationChangeable = animationTick == 20;
            animationTick = animationTick == 20 ? 0 : animationTick;
        }
        if (animationChangeable) {
            entityData.set(POSE, isDisappearing ? -1 : isPathFinding() ? 1 : 0);
        }
        super.tick();
    }

    public void disappear() {
        this.isDisappearing = true;
    }
    public boolean onBeingShoveled(Player player, InteractionHand handIn) {
        return SimplePlant.onBeingShoveled(player, handIn, this);
    }
    @Override
    public boolean isPushable(){
        return true;
    }
    @Override
    public boolean canBeLeashed(Player p_21418_) {
        return true;
    }
    //ICanGroupUp
    ICanGroupUp leader = null;
    int schoolSize = 1;
    @Override
    public ICanGroupUp getLeader() {
        return leader;
    }
    @Override
    public void setLeader(ICanGroupUp entity) {
        leader = entity;
    }
    @Override
    public int getMaxSchoolSize() {
        return 5;
    }
    @Override
    public int getSchoolSize() {
        return schoolSize;
    }
    @Override
    public void setSchoolSize(int size) {
        schoolSize = size;
    }
    @Override
    public int getGroupRangeSqr(){
        return 64;
    }

    @Override
    public Mob self() {
        return this;
    }

    private static class TurnipAttackGoal extends MeleeAttackGoal {
        public TurnipAttackGoal(VelociRadish p_25552_, double p_25553_, boolean p_25554_) {
            super(p_25552_, p_25553_, p_25554_);
        }
        @Override
        protected void checkAndPerformAttack(LivingEntity entity, double p_25558_) {
            double d0 = this.getAttackReachSqr(entity);
            if (p_25558_ <= d0 && ((VelociRadish) this.mob).animationChangeable) {
                ((VelociRadish) this.mob).animationChangeable = false;
                this.mob.getEntityData().set(POSE, 2);
            }
            if (((VelociRadish) this.mob).animationTick == 5 && this.mob.getEntityData().get(POSE) == 2) {
                //TODO remake the relationship of attack and animation
                this.mob.swing(InteractionHand.MAIN_HAND);
                this.mob.doHurtTarget(entity);
                this.mob.level.playSound(null, mob, ((VelociRadish) mob).hasSkill(STRONG_SKILL_NAME)
                        ? PVZSoundEvents.VELOCI_RADISH_STRONG_ATTACK.get()
                        : PVZSoundEvents.VELOCI_RADISH_ATTACK.get()
                        , SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
        }
        @Override
        protected double getAttackReachSqr(LivingEntity p_25556_) {
            return this.mob.getBbWidth() * this.mob.getBbWidth() * 8.0F + p_25556_.getBbWidth() * p_25556_.getBbWidth();
        }
    }
}