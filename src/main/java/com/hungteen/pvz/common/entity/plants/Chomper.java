package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.api.interfaces.ICanAttack;
import com.hungteen.pvz.api.interfaces.IHaveSkills;
import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.Sun;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.common.entity.ai.goal.DisperseEnemyTargetGoal;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.util.EntityUtil;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.Tags;

import javax.annotation.Nullable;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

import static com.hungteen.pvz.common.entity.SimplePlant.tryShovel;

public class Chomper extends PathfinderMob implements IPlant, IHaveSkills, ICanAttack, VibrationListener.VibrationListenerConfig {
    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState digAnimationState = new AnimationState();
    public AnimationState attackAnimationState = new AnimationState();
    public AnimationState outAnimationState = new AnimationState();
    public AnimationState digestAnimationState = new AnimationState();
    public AnimationState swallowAnimationState = new AnimationState();
    public AnimationState swimAnimationState = new AnimationState();
    public AnimationState meleeAnimationState = new AnimationState();
    private final DynamicGameEventListener<VibrationListener> dynamicGameEventListener;
    public static final EntityDataAccessor<Boolean> ROOT = SynchedEntityData.defineId(Chomper.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> WILT_COUNTDOWN = SynchedEntityData.defineId(Chomper.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> SKILL = SynchedEntityData.defineId(Chomper.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_TIME = SynchedEntityData.defineId(Chomper.class, EntityDataSerializers.INT);
    private static final UUID SPEED_MODIFIER_UUID = UUID.fromString("54570731-895f-e8d3-2a83-87c6604fb109");
    private final Map<Pose, AnimationState> poseMap = Map.of(Pose.STANDING, idleAnimationState, Pose.DIGGING, digAnimationState, Pose.USING_TONGUE, attackAnimationState,
            Pose.EMERGING, outAnimationState, Pose.CROUCHING, digestAnimationState, Pose.CROAKING, swallowAnimationState, Pose.SWIMMING, swimAnimationState, Pose.SPIN_ATTACK, meleeAnimationState);

    public static final String SUN_SKILL_NAME = "skill.pvz.chomper.energy_transduction";
    public static List<Skill> staticSkillList = List.of(
            new Skill(SUN_SKILL_NAME, PVZItems.LUX_ESSENCE, 8, 8, 50, 0)
    );
    Vec3 storedPosition;
    private BlockPos originalPos;
    protected boolean firstUnsafeSituationMercy = true;
    public int animTick = 0;
    public Chomper(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        originalPos = this.getOnPos();
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationListener(new EntityPositionSource(this, this.getEyeHeight()), 8, this, (VibrationListener.ReceivingEvent)null, 0.0F, 0));
    }

    public static boolean isSculk(LivingEntity chomper) {
        return EntityUtil.isSculk(chomper) &&
                ! ((IHaveSkills) chomper).hasSkill(SUN_SKILL_NAME);
    }

    @Override
    public void updateDynamicGameEventListener(BiConsumer<DynamicGameEventListener<?>, ServerLevel> p_219413_) {
        Level level = this.level;
        if (level instanceof ServerLevel serverlevel) {
            p_219413_.accept(this.dynamicGameEventListener, serverlevel);
        }

    }
    private int getAttackCD() {
        return 150;
    }

    protected BlockPos getOriginalPos() {
        return originalPos;
    }
    protected void setOriginalPos(BlockPos pos) {
        this.originalPos = pos;
    }
    public BlockPos getRootBlockPos() {
        return this.getOriginalPos();
    }

    //entity settings
    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 40D)
                .add(Attributes.MOVEMENT_SPEED, 0D)
                .add(Attributes.FOLLOW_RANGE, 24D);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    @Override
    public int getSkillVal(Object obj) {
        return this.entityData.get(SKILL);
    }

    @Override
    public void setSkillVal(Object obj, int value) {
        this.entityData.set(SKILL, value);
    }

    //overrides
    @Override
    public double getPassengersRidingOffset() {
        return 1.5;
    }//TODO modify this.
    @Override
    public boolean rideableUnderWater() {
        return true;
    }
    @Override
    public void baseTick() {
        super.baseTick();
        if (level.isClientSide && (this.getPose() == Pose.DIGGING || this.getPose() == Pose.SWIMMING)) {
            for (int i = 0; i < 5; i ++) {
                this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()), this.getX() + (this.random.nextDouble() - 0.5D) - this.getDeltaMovement().x / 2, this.getY() + 0.1D, this.getZ() + (this.random.nextDouble() - 0.5D) - this.getDeltaMovement().z / 2, (this.random.nextDouble() - 0.5) * 6.0D, 2D, (this.random.nextDouble() - 0.5) * 4.0D);
            }
        }
        animTick ++;
        //check plant situation damage.
        firstUnsafeSituationMercy = SimplePlant.testPlantSafe(this, firstUnsafeSituationMercy);
        //TODO relative codes. add particle when plant is dying.
    }

    @Override
    public void tick() {
        if (this.getPose() == Pose.SWIMMING) {
            EntityUtil.addModifierToAttribute(this, Attributes.MOVEMENT_SPEED, new AttributeModifier(SPEED_MODIFIER_UUID, "pose_addon", 0.4, AttributeModifier.Operation.ADDITION));
        } else {
            EntityUtil.removeModifierFromAttribute(this, Attributes.MOVEMENT_SPEED, SPEED_MODIFIER_UUID);
        }
        if (this.storedPosition == null) {
            this.storedPosition = this.position();
            this.setOriginalPos(new BlockPos((int) (storedPosition.x > 0 ? storedPosition.x : storedPosition.x - 1),
                    (int) storedPosition.y - 1,
                    (int) (storedPosition.z > 0 ? storedPosition.z : storedPosition.z - 1)));
        }
        if (this.getPose() == Pose.SWIMMING && ((this.position().distanceTo(this.storedPosition) < 0.1 && this.tickCount % 10 == 0) || super.isInWall())) {
            this.noPhysics = true;
            this.setNoGravity(true);
            if (level.getBlockState(this.blockPosition()).isSuffocating(level, this.blockPosition())) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.05, 0));
            }
            super.tick();
            this.setNoGravity(false);
            this.noPhysics = false;
            this.storedPosition = this.position();
        } else {
            super.tick();
        }
        if (level instanceof ServerLevel serverlevel) {
            this.dynamicGameEventListener.getListener().tick(serverlevel);
            if (getTarget() != null) {
                this.setYBodyRot(this.getYRot());
            }
        }
    }

    public void alignBlocks() {
        BlockPos pos = this.getRootBlockPos();
        moveTo(pos.getX() + 0.5, this.getY(), pos.getZ() + 0.5);
    }

    public float getEyeHeight(Pose p_20237_) {
        return ((this.getPose() == Pose.SWIMMING ? 0.1F : 1F) * this.getEyeHeight(p_20237_, this.getDimensions(p_20237_)));
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AttractEnemyGoal(this, () -> this.getPose() != Pose.SWIMMING, 2));
        this.goalSelector.addGoal(1, new ChomperAttackGoal(this));
        this.goalSelector.addGoal(3, new AxisLookAroundGoal(this));
        this.targetSelector.addGoal(1, new DisperseEnemyTargetGoal(this,
                (entity) -> this.getPose() != Pose.SWIMMING && ! entity.isPassenger() && EntityUtil.checkCanEntityBeAttack(this, entity) && entity != this, 3));
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ROOT, true);
        this.entityData.define(WILT_COUNTDOWN, -1);
        this.entityData.define(SKILL, 0);
        this.entityData.define(ATTACK_TIME, 0);
    }
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        if (DATA_POSE.equals(data)) {
            for (AnimationState animationState : this.poseMap.values()) {
                if (this.poseMap.get(entityData.get(DATA_POSE)) == animationState) {
                    animationState.start(this.tickCount);
                } else {
                    animationState.stop();
                }
            }
            this.animTick = 0;
        }
        super.onSyncedDataUpdated(data);
    }

    public void setupPresentationAnim() {
        this.idleAnimationState.start(this.tickCount);
    }

    @Override
    public EntityDataAccessor<Boolean> root() {
        return ROOT;
    }

    @Override
    public boolean takesCoincideDmg() {
        return this.getPose() != Pose.DIGGING && this.getPose() != Pose.USING_TONGUE && this.getPose() != Pose.SWIMMING;
    }
    @Override
    public boolean onBeingShoveled(Player player, InteractionHand handIn) {
        return SimplePlant.onBeingShoveled(player, handIn, this);
    }
    @Nullable
    public ItemStack getPickResult() {
        return SimplePlant.getPickResult(this);
    }

    public int getAttackTime() {
        return entityData.get(ATTACK_TIME);
    }

    public void setAttackTime(int cd) {
        entityData.set(ATTACK_TIME, cd);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Root", getEntityData().get(ROOT));
        tag.putInt("WiltCountDown", getEntityData().get(WILT_COUNTDOWN));
        ListTag skills = new ListTag();
        for (String name : getSkillNames()) {
            skills.add(StringTag.valueOf(name));
        }
        tag.put("Skill", skills);
        tag.putInt("PlantAttackTime", getAttackTime());
        tag.putInt("Pose", getEntityData().get(DATA_POSE).ordinal());
        tag.putLong("OriginalPos", this.getOriginalPos().asLong());
        VibrationListener.codec(this).encodeStart(NbtOps.INSTANCE, this.dynamicGameEventListener.getListener()).resultOrPartial(PVZMod.LOGGER::error).ifPresent((p_219418_) -> {
            tag.put("listener", p_219418_);
        });

    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("Skill")) {
            setSkillVal(this, getSkillValFromNames(tag.getList("Skill", Tag.TAG_STRING).stream().map(Tag::getAsString).toList()));
        }
        if (tag.contains("PlantAttackTime")) {
            this.setAttackTime(tag.getInt("PlantAttackTime"));
        }
        if (tag.contains("WiltCountDown")) {
            this.getEntityData().set(WILT_COUNTDOWN, tag.getInt("WiltCountDown"));
        }
        if (tag.contains("Root")) {
            this.getEntityData().set(ROOT, tag.getBoolean("Root"));
        }
        if (tag.contains("Pose")) {
            this.getEntityData().set(DATA_POSE, Pose.values()[tag.getInt("Pose")]);
        }
        if (tag.contains("OriginalPos")) {
            setOriginalPos(BlockPos.of(tag.getLong("OriginalPos")));
        }
        if (tag.contains("listener")) {
            VibrationListener.codec(this).parse(new Dynamic<>(NbtOps.INSTANCE, tag.getCompound("listener"))).resultOrPartial(PVZMod.LOGGER::error)
                    .ifPresent((p_219408_) -> this.dynamicGameEventListener.updateListener(p_219408_, this.level));
        }
    }

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
            //2. when clicked on sides of blocks, plant on relative plants.
        Vec3i offset = direction == null ? Vec3i.ZERO : direction.getNormal();
        pos = pos.offset(offset).offset(getGrowDirection() == null ? Vec3i.ZERO : getGrowDirection().getOpposite().getNormal());
        direction = getGrowDirection();
        offset = direction == null ? Vec3i.ZERO : direction.getNormal();
        //now pos is the rooted block position.
        //collision check.
        AABB aabb = AABB.ofSize(new Vec3(pos.getX() + 0.5 + offset.getX(),
                        pos.getY() + offset.getY() + getBbHeight() / 2,
                        pos.getZ() + 0.5 + offset.getZ()),
                getBbWidth() - 0.0001, getBbHeight() - 0.0001, getBbWidth() - 0.0001);
            //1. blocks.
        if (BlockPos.betweenClosedStream(aabb).anyMatch((pos1) -> {
            BlockState blockstate = this.level.getBlockState(pos1);
            return !blockstate.isAir() && blockstate.isSuffocating(this.level, pos1) &&
                    Shapes.joinIsNotEmpty(blockstate.getCollisionShape(this.level, pos1).move(pos1.getX(), pos1.getY(), pos1.getZ()), Shapes.create(aabb), BooleanOp.AND);
        })) {
            return Component.translatable("hint.pvz.plant.no_enough_place");
        }
            //2. entities.
        if (shouldHaveCoincideDmg(level, Vec3.atBottomCenterOf(pos.offset(offset)))) {
            return Component.translatable("hint.pvz.plant.no_enough_place");
        }
        //root block available check.
        BlockPos finalPos = pos;
        if ((! entityData.get(this.root())) || getAcceptableTags().stream().anyMatch((tag) -> level.getBlockState(finalPos).is(tag))) {
            //final plant.
            BlockState state = level.getBlockState(pos);
            if (isPlanting) {
                this.moveTo(
                        pos.getX() + 0.5 + offset.getX(),
                        pos.getY() + (direction == Direction.UP ? (state.getCollisionShape(level, pos).isEmpty() ?
                                (level.getFluidState(pos).isEmpty() ? 0: level.getFluidState(pos).getHeight(level, pos)) :
                                state.getCollisionShape(level, pos).bounds().maxY) : offset.getY()),
                        pos.getZ() + 0.5 + offset.getZ());
                ((ServerLevel)this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()), this.getX(), this.getY(), this.getZ(), 5, 0.0D, 0.0D, 0.0D, 0.25F);
            }
            return null;
        } else {
            return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), level.getBlockState(pos).getBlock().getName());
        }
    }
    @Override
    public MutableComponent customVehicleSafe(PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        if (target == null) {
            return Component.translatable("hint.pvz.plant.entity_not_present");
        }
        return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), target.getName());
    }
    @Override
    protected PathNavigation createNavigation(Level level) {
        return new GroundPathNavigation(this, level);
    }
    public Set<TagKey<Block>> getAcceptableTags() {
        return Set.of(PVZBlockTags.PLANTABLE_DIRT, PVZBlockTags.UNPLANTABLE_DIRT, PVZBlockTags.PLANTABLE_STONE);
    }

    public boolean shouldHaveCoincideDmg(Level level, Vec3 position) {
        return SimplePlant.shouldHaveCoincideDmg(this, level, position);
    }
    //bb and pushing
    @Override
    public AABB makeBoundingBox() {
        return this.getPose() == Pose.SWIMMING ? super.makeBoundingBox().inflate(-0.2,-0.8, -0.2).move(0, -0.8, 0) : super.makeBoundingBox();
    }
    @Override
    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return this.getPose() == Pose.SWIMMING ? 0.1F : super.getStandingEyeHeight(pose, dimensions);
    }
    @Override
    public boolean isInWall() {
        return super.isInWall() && this.getPose() != Pose.SWIMMING;
    }
    @Override
    protected void pushEntities(){
        List<Entity> list = this.level.getEntities(this, this.getBoundingBox(), EntitySelector.pushableBy(this).and(this.canPush()));
        if (!list.isEmpty()) {
            int i = this.level.getGameRules().getInt(GameRules.RULE_MAX_ENTITY_CRAMMING);
            if (i > 0 && list.size() > i - 1 && this.random.nextInt(4) == 0) {
                int j = 0;
                for (Entity entity : list) {
                    if (!entity.isPassenger()) {
                        ++j;
                    }
                }
                if (j > i - 1) {
                    this.hurt(DamageSource.CRAMMING, 6.0F);
                }
            }
            for (Entity entity : list) {
                this.doPush(entity);
            }
        }
    }
    @Override
    public boolean isPushable(){
        return false;
    }
    public Predicate<Entity> canPush(){
        return (entity) -> this.isPushable();
    }
    @Override
    public boolean removeWhenFarAway(double p_27598_) {
        PVZEntityCapability cap = this.getCapability(PVZEntityCapability.CAP).orElse(null);
        return cap == null || ! cap.hasOwner();
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
    public boolean shouldListen(ServerLevel p_223872_, GameEventListener p_223873_, BlockPos p_223874_, GameEvent p_223875_, GameEvent.Context context) {
        return isSculk(this) && EntityUtil.checkCanEntityBeAttack(this, context.sourceEntity()) && ! (context.sourceEntity() instanceof Slime);
    }

    @Override
    public void onSignalReceive(ServerLevel p_223865_, GameEventListener p_223866_, BlockPos pos, GameEvent p_223868_, @Nullable Entity target, @Nullable Entity ownerOfTarget, float p_223871_) {
        if (! this.isDeadOrDying() && (this.getTarget() == null || this.getTarget().blockPosition().distSqr(pos) > target.blockPosition().distSqr(pos))) {
            if (target instanceof LivingEntity entity) {
                this.setTarget(entity);
            } else if (ownerOfTarget instanceof LivingEntity entity) {
                this.setTarget(entity);
            }
        }
    }
    @Override
    public boolean dampensVibrations() {
        return true;
    }
    @Override
    public boolean canTriggerAvoidVibration() {
        return true;
    }

    //goals
    public static class ChomperAttackGoal extends Goal {
        Chomper chomper;
        public ChomperAttackGoal(Chomper chomper) {
            this.chomper = chomper;
            this.setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
        }

        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            if (EntityUtil.isEntityValid(chomper.getTarget())) {
                chomper.lookAt(chomper.getTarget(), 10, 10);
            }
            switch (chomper.getPose()) {
                case STANDING -> {
                    if (chomper.animTick > 59 && chomper.animTick % 60 <= 1 && EntityUtil.checkCanEntityBeAttack(chomper, this.chomper.getTarget())) {
                        if (chomper.getTarget().distanceToSqr(chomper) < 16) {
                            return true;
                        } else {
                            Path path = chomper.getNavigation().createPath(this.chomper.getTarget(), 0);
                            return path != null && path.getEndNode() != null;
                        }
                    }
                    return false;
                }
                case EMERGING -> {
                    chomper.alignBlocks();
                    chomper.getNavigation().stop();
                    return chomper.animTick > 29 && chomper.animTick % 30 <= 1;
                }
                case CROUCHING -> {
                    chomper.setAttackTime(Math.max(0, chomper.getAttackTime() - 1));
                    if (chomper.getAttackTime() <= 0) {
                        return chomper.animTick > 59 && chomper.animTick % 60 <= 1;
                    } else {
                        return false;
                    }
                }
                case DIGGING -> {
                    return chomper.animTick > 19 && chomper.animTick % 20 <= 1;
                }
                default -> {
                    return true;
                }
            }
        }
        @Override
        public void tick() {
            if (! this.canContinueToUse()) {
                return;
            }
            switch (chomper.getPose()) {
                case STANDING -> {
                    if (! EntityUtil.isEntityValid(this.chomper.getTarget())) {
                        return;
                    }
                    if (chomper.getTarget().distanceToSqr(chomper) < 16) {
                        chomper.setPose(Pose.SPIN_ATTACK);
                    } else {
                        chomper.setPose(Pose.DIGGING);
                    }
                }
                case DIGGING -> {
                    chomper.setPose(Pose.SWIMMING);
                }
                case USING_TONGUE, SPIN_ATTACK -> {
                    if (chomper.animTick == 11) {
                        LivingEntity target = chomper.getTarget();
                        if (EntityUtil.checkCanEntityBeAttack(chomper, target) && !(target.getVehicle() instanceof Chomper) &&
                                chomper.distanceToSqr(target.position()) <= (chomper.getPose() == Pose.SPIN_ATTACK ? 16 : 6) && ! target.getType().is(Tags.EntityTypes.BOSSES)) {
                            target.startRiding(chomper);
                            target.hurt(PVZDamageSource.transferKiller(PVZDamageSource.knockBack(PVZDamageSource.chomperHurt(chomper), 2F), PVZEntityCapability.getOwner(chomper)), 5F);
                            if (target.getBbWidth() > 1.25 || target instanceof Slime /*to prevent a vanilla bug*/) {
                                target.stopRiding();
                            }
                        }
                    } else if (chomper.animTick == 53) {
                        Entity rider = chomper.getFirstPassenger();
                        if (rider != null) {
                            chomper.setAttackTime(chomper.getAttackCD());
                            rider.hurt(PVZDamageSource.chomperHurt(chomper).bypassArmor(), 35);
                            if (rider.isAlive()) {
                                rider.stopRiding();
                            } else {
                                rider.discard();
                            }
                        }
                    } else if (chomper.animTick > 79) {
                        chomper.setPose(chomper.blockPosition().below().equals(chomper.getOriginalPos()) ?
                                (chomper.getAttackTime() <= 0 ? Pose.STANDING : Pose.CROUCHING) : Pose.DIGGING);
                        if (chomper.getPose() == Pose.CROUCHING) {
                            chomper.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 350, 3));
                        }
                    }
                }
                case EMERGING -> {
                    chomper.setPose(chomper.getAttackTime() <= 0 ? Pose.STANDING : Pose.CROUCHING);
                    if (chomper.getPose() == Pose.CROUCHING) {
                        chomper.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 350, 3));
                    }
                }
                case CROAKING -> {
                    if (chomper.hasSkill(SUN_SKILL_NAME)) {
                        if (chomper.animTick == 30) {
                            Sun.spawnSunWithEffects(this.chomper.level, 50, this.chomper.getOnPos().above(), 0.4F);
                        }
                        else if (chomper.animTick == 35) {
                            Sun.spawnSunWithEffects(this.chomper.level, 25, this.chomper.getOnPos().above(), 0.4F);
                        }
                        else if (chomper.animTick == 38) {
                            Sun.spawnSunWithEffects(this.chomper.level, 15, this.chomper.getOnPos().above(), 0.5F);
                        }
                        else if (chomper.animTick == 50) {
                            Sun.spawnSunWithEffects(this.chomper.level, 15, this.chomper.getOnPos().above(), 0.5F);
                        }
                        else if (chomper.animTick == 53) {
                            Sun.spawnSunWithEffects(this.chomper.level, 15, this.chomper.getOnPos().above(), 0.5F);
                        }
                        else if (chomper.animTick > 60 && chomper.animTick % 60 == 0) {
                            Sun.spawnSunWithEffects(this.chomper.level, 5, this.chomper.getOnPos().above(), 0.5F);
                        }
                    }
                    if (chomper.animTick > 10 && chomper.animTick % 60 == 0) {
                        chomper.setPose(Pose.STANDING);
                    }
                }
                case CROUCHING -> {
                    chomper.setPose(Pose.CROAKING);
                }
                case SWIMMING -> {
                    LivingEntity target = chomper.getTarget();
                    PathNavigation navigation = chomper.getNavigation();
                    if (! EntityUtil.isEntityValid(target) || target.getVehicle() instanceof Chomper || chomper.getAttackTime() != 0) {
                        BlockPos pos = chomper.getOriginalPos().above();
                        boolean homeSafe = chomper.isPositionSafe(null, chomper.level, pos.below(), Direction.UP, false) == null;
                        if (chomper.blockPosition().distSqr(pos) < 3 || ! homeSafe) {
                            if (! homeSafe) {
                                chomper.setOriginalPos(this.chomper.blockPosition().below());
                            }
                            chomper.setPose(Pose.EMERGING);
                            navigation.stop();
                            chomper.alignBlocks();
                            chomper.moveTo(chomper.position().x, chomper.getOriginalPos().getY() + 1, chomper.position().z);
                            chomper.setDeltaMovement(Vec3.ZERO);
                        } else if (navigation.isDone()) {
                            navigation.moveTo(navigation.createPath(pos, 0), 1);
                            if (navigation.getPath() == null ||
                                    (navigation.getPath().getEndNode() != null && navigation.getPath().getEndNode().asBlockPos().distSqr(chomper.getOriginalPos()) >= 2)) {
                                chomper.setOriginalPos(this.chomper.blockPosition().below());
                            }
                        }
                    } else if (navigation.isDone()) {
                        navigation.moveTo(navigation.createPath(target, 0), 1);
                        if (navigation.getPath() == null) {
                            chomper.setTarget(null);
                        }
                    }
                    if (chomper.getAttackTime() == 0 && EntityUtil.isEntityValid(target) && chomper.position().distanceToSqr(target.position()) <= 3) {
                        chomper.setPose(Pose.USING_TONGUE);
                        chomper.moveTo(target.position().multiply(1, 0, 1).add(0, chomper.getY(), 0));
                        navigation.stop();
                        chomper.setDeltaMovement(Vec3.ZERO);
                    }
                }
            }
        }
    }
}
