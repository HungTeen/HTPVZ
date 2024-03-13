package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.ICanAttack;
import com.hungteen.pvz.api.interfaces.IHaveSkills;
import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.INeedSafeSituation;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.Sun;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.common.entity.ai.goal.DisperseEnemyTargetGoal;
import com.hungteen.pvz.common.event.PVZResourceEvent;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.util.EntityUtil;
import com.mojang.serialization.Dynamic;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.BushBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.DynamicGameEventListener;
import net.minecraft.world.level.gameevent.EntityPositionSource;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.gameevent.GameEventListener;
import net.minecraft.world.level.gameevent.vibrations.VibrationListener;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.fluids.IFluidBlock;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Predicate;

public class Chomper extends PathfinderMob implements IPlant, IHaveSkills, INeedSafeSituation, ICanAttack, VibrationListener.VibrationListenerConfig {
    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState digAnimationState = new AnimationState();
    public AnimationState attackAnimationState = new AnimationState();
    public AnimationState outAnimationState = new AnimationState();
    public AnimationState digestAnimationState = new AnimationState();
    public AnimationState swallowAnimationState = new AnimationState();
    public AnimationState swimAnimationState = new AnimationState();
    private final DynamicGameEventListener<VibrationListener> dynamicGameEventListener;

    private int situationHurtCount = 0;
    public static final EntityDataAccessor<Boolean> ROOT = SynchedEntityData.defineId(Chomper.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> WILT_COUNTDOWN = SynchedEntityData.defineId(Chomper.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> SKILL = SynchedEntityData.defineId(Chomper.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_TIME = SynchedEntityData.defineId(Chomper.class, EntityDataSerializers.INT);

    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.chomper.energy_transduction", PVZItems.LUX_ESSENCE, 8, 8, 50, 0)
    );
    Vec3 storedPosition;
    private BlockPos originalPos;

    public int animTick = 0;
    public Chomper(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        originalPos = this.getOnPos();
        this.dynamicGameEventListener = new DynamicGameEventListener<>(new VibrationListener(new EntityPositionSource(this, this.getEyeHeight()), 16, this, (VibrationListener.ReceivingEvent)null, 0.0F, 0));
    }

    public static boolean isSculk(LivingEntity chomper) {
        return chomper.level.getBlockState(chomper.getOnPos()).is(PVZBlockTags.SCULK) &&
                ! ((IHaveSkills) chomper).hasSkill("skill.pvz.chomper.energy_transduction");
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
                .add(Attributes.MAX_HEALTH, 30D)
                .add(Attributes.MOVEMENT_SPEED, 0.4D)
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
    public void baseTick() {
        super.baseTick();
        if (level.isClientSide && (this.getPose() == Pose.DIGGING || this.getPose() == Pose.SWIMMING)) {
            for (int i = 0; i < 5; i ++) {
                this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()), this.getX() + (this.random.nextDouble() - 0.5D) - this.getDeltaMovement().x / 2, this.getY() + 0.1D, this.getZ() + (this.random.nextDouble() - 0.5D) - this.getDeltaMovement().z / 2, (this.random.nextDouble() - 0.5) * 6.0D, 2D, (this.random.nextDouble() - 0.5) * 4.0D);
            }
        }
        animTick ++;
        //check plant situation damage.
        if (! level.isClientSide) {
            if (isPositionSafe(null, this.level, getRootBlockPos(), getGrowDirection(), false) != null && isVehicleSafe(null, getVehicle(), false) != null &&
                    this.getAttribute(Attributes.MAX_HEALTH) != null) {
                if (++ situationHurtCount > 10) {
                    this.hurt(PVZDamageSource.PLANT_WILT, (float) (0.2 * this.getAttribute(Attributes.MAX_HEALTH).getValue()));
                    situationHurtCount = 0;
                }
            } else {
                situationHurtCount = 0;
            }
        }
        //TODO relative codes. add particle when plant is dying.
    }

    @Override
    public void tick() {
        if (this.storedPosition == null) {
            this.storedPosition = Vec3.ZERO;
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
        this.goalSelector.addGoal(3, new AxisLookAroundGoal(this));
        this.goalSelector.addGoal(3, new ChomperAttackGoal(this));
        this.targetSelector.addGoal(1, new DisperseEnemyTargetGoal(this, (entity)-> EntityUtil.checkCanEntityBeAttack(this, entity), 8));
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
            switch (entityData.get(DATA_POSE)) {
                case STANDING -> {
                    this.idleAnimationState.start(this.tickCount);
                    this.digAnimationState.stop();
                    this.attackAnimationState.stop();
                    this.outAnimationState.stop();
                    this.digestAnimationState.stop();
                    this.swallowAnimationState.stop();
                    this.swimAnimationState.stop();
                }
                case DIGGING -> {
                    this.idleAnimationState.stop();
                    this.digAnimationState.start(this.tickCount);
                    this.attackAnimationState.stop();
                    this.outAnimationState.stop();
                    this.digestAnimationState.stop();
                    this.swallowAnimationState.stop();
                    this.swimAnimationState.stop();
                }
                case USING_TONGUE -> {
                    this.idleAnimationState.stop();
                    this.digAnimationState.stop();
                    this.attackAnimationState.start(this.tickCount);
                    this.outAnimationState.stop();
                    this.digestAnimationState.stop();
                    this.swallowAnimationState.stop();
                    this.swimAnimationState.stop();
                }
                case EMERGING -> {
                    this.idleAnimationState.stop();
                    this.digAnimationState.stop();
                    this.attackAnimationState.stop();
                    this.outAnimationState.start(this.tickCount);
                    this.digestAnimationState.stop();
                    this.swallowAnimationState.stop();
                    this.swimAnimationState.stop();
                }
                case CROUCHING -> {
                    this.idleAnimationState.stop();
                    this.digAnimationState.stop();
                    this.attackAnimationState.stop();
                    this.outAnimationState.stop();
                    this.digestAnimationState.start(this.tickCount);
                    this.swallowAnimationState.stop();
                    this.swimAnimationState.stop();
                }
                case CROAKING -> {
                    this.idleAnimationState.stop();
                    this.digAnimationState.stop();
                    this.attackAnimationState.stop();
                    this.outAnimationState.stop();
                    this.digestAnimationState.stop();
                    this.swallowAnimationState.start(this.tickCount);
                    this.swimAnimationState.stop();
                }
                case SWIMMING -> {
                    this.idleAnimationState.stop();
                    this.digAnimationState.stop();
                    this.attackAnimationState.stop();
                    this.outAnimationState.stop();
                    this.digestAnimationState.stop();
                    this.swallowAnimationState.stop();
                    this.swimAnimationState.start(this.tickCount);
                }
            }
        }
        super.onSyncedDataUpdated(data);
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
        tag.putInt("Skill", getSkillVal(this));
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
            setSkillVal(this, tag.getInt("Skill"));
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
    public MutableComponent isPositionSafe(PVZResourceEvent.CheckPlantConditionEvent event, Level level, BlockPos pos, Direction direction, boolean isPlanting) {
        if (isPlanting && event != null) {
            if (event.cost > PVZPlayerCapability.getValue(event.getEntity(), event.resource)) {
                return Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource));
            }
        }
        if (level.getBlockState(pos).getBlock() instanceof BushBlock) {
            pos = pos.offset(direction.getOpposite().getNormal());
        }
        if (! (level.getBlockState(pos).getBlock() instanceof IFluidBlock)) {
            pos = pos.offset(direction.getNormal()).offset(getGrowDirection().getOpposite().getNormal());
            direction = getGrowDirection();
        }
        AABB aabb = AABB.ofSize(new Vec3(pos.getX() + 0.5 + direction.getNormal().getX(),
                        pos.getY() + direction.getNormal().getY() + getBbHeight() / 2,
                        pos.getZ() + 0.5 + direction.getNormal().getZ()),
                getBbWidth() - 0.0001, getBbHeight() - 0.0001, getBbWidth() - 0.0001);
        if (BlockPos.betweenClosedStream(aabb).anyMatch((pos1) -> {
            BlockState blockstate = this.level.getBlockState(pos1);
            return !blockstate.isAir() && blockstate.isSuffocating(this.level, pos1) &&
                    Shapes.joinIsNotEmpty(blockstate.getCollisionShape(this.level, pos1).move(pos1.getX(), pos1.getY(), pos1.getZ()), Shapes.create(aabb), BooleanOp.AND);
        })) {
            return Component.translatable("hint.pvz.plant.no_enough_place");
        }
        if (shouldHaveCoincideDmg(level, Vec3.atBottomCenterOf(pos.offset(direction.getNormal())))) {
            return Component.translatable("hint.pvz.plant.no_enough_place");
        }
        boolean plantableOn = false;
        for (TagKey<Block> tag: getAcceptableTags()) {
            if (level.getBlockState(pos).is(tag)) {
                plantableOn = true;
                break;
            }
        }
        if (! this.getEntityData().get(root()) || (plantableOn && ! level.getBlockState(pos).isAir())) {
            if (level.getBlockState(pos).getFluidState().isEmpty()) {
                if (isPlanting) {
                    this.moveTo(
                            pos.getX() + 0.5 + direction.getNormal().getX(),
                            pos.getY() + (direction == Direction.UP ? (level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() ?
                                    (level.getFluidState(pos).isEmpty() ? 0: level.getFluidState(pos).getHeight(level, pos)) :
                                    level.getBlockState(pos).getCollisionShape(level, pos).bounds().maxY) : direction.getNormal().getY()),
                            pos.getZ() + 0.5 + direction.getNormal().getZ());
                    ((ServerLevel)this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()), this.getX(), this.getY(), this.getZ(), 5, 0.0D, 0.0D, 0.0D, 0.15F);
                    this.setOriginalPos(pos);
                }
                return null;
            }
            return Component.translatable("hint.pvz.plant.cant_plant_in_water", this.getName());
        } else {
            return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), level.getBlockState(pos).getBlock().getName());
        }
    }
    @Override
    public MutableComponent isVehicleSafe(PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        if (target == null) {
            return Component.translatable("hint.pvz.plant.entity_not_present");
        }
        return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), target.getName());
    }

    public Set<TagKey<Block>> getAcceptableTags() {
        return Set.of(PVZBlockTags.PLANTABLE_DIRT);
    }

    public boolean shouldHaveCoincideDmg(Level level, Vec3 position) {
        if (! takesCoincideDmg()) {
            return false;
        } else {
            Vec3 subPos = this.position();
            AABB range = this.getBoundingBox().move(position.add(-subPos.x, -subPos.y, -subPos.z)).inflate(-1e-4);
            List<Entity> list = level.getEntities(this, range,
                    (entity) -> entity instanceof IPlant && ((IPlant)entity).takesCoincideDmg() && this.getVehicle() != entity && entity.getVehicle() != this);
            return !list.isEmpty();
        }
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
        PVZOwnedCapability cap = this.getCapability(PVZOwnedCapability.CAP).orElse(null);
        return cap.getOwner() == null;
        //TODO handle situation when player is not available when loading.
    }

    @Override
    public boolean shouldListen(ServerLevel p_223872_, GameEventListener p_223873_, BlockPos p_223874_, GameEvent p_223875_, GameEvent.Context context) {
        return isSculk(this) && EntityUtil.checkCanEntityBeAttack(this, context.sourceEntity());
    }

    @Override
    public void onSignalReceive(ServerLevel p_223865_, GameEventListener p_223866_, BlockPos p_223867_, GameEvent p_223868_, @Nullable Entity target, @Nullable Entity ownerOfTarget, float p_223871_) {
        if (! this.isDeadOrDying()) {
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
        }
        @Override
        public boolean canUse() {
            switch (chomper.getPose()) {
                case STANDING -> {
                    chomper.alignBlocks();
                    chomper.navigation.stop();
                    chomper.setDeltaMovement(Vec3.ZERO);
                    if (EntityUtil.checkCanEntityBeAttack(chomper, this.chomper.getTarget())) {
                        return chomper.animTick > 10 && chomper.animTick % 60 <= 2;
                    }
                    return false;
                }
                case EMERGING -> {
                    chomper.alignBlocks();
                    chomper.getNavigation().stop();
                    chomper.setDeltaMovement(Vec3.ZERO);
                    return chomper.animTick > 10 && chomper.animTick % 30 <= 2;
                }
                case CROUCHING -> {
                    chomper.alignBlocks();
                    chomper.getNavigation().stop();
                    chomper.setDeltaMovement(Vec3.ZERO);
                    chomper.setAttackTime(Math.max(0, chomper.getAttackTime() - 1));
                    if (chomper.getAttackTime() <= 0) {
                        return chomper.animTick > 10 && chomper.animTick % 60 <= 2;
                    } else {
                        return false;
                    }
                }
                case CROAKING -> {
                    return chomper.animTick > 10 && chomper.animTick % 60 <= 2;
                }
                case DIGGING -> {
                    return chomper.animTick > 10 && chomper.animTick % 20 <= 2;
                }
                case USING_TONGUE -> {
                    chomper.getNavigation().stop();
                    chomper.setDeltaMovement(Vec3.ZERO);
                    return true;
                }
                default -> {
                    return true;
                }
            }
        }
        @Override
        public void tick() {
            switch (chomper.getPose()) {
                case STANDING -> {
                    chomper.setPose(Pose.DIGGING);
                    chomper.animTick = 0;
                }
                case DIGGING -> {
                    chomper.setPose(Pose.SWIMMING);
                    chomper.animTick = 0;
                }
                case USING_TONGUE -> {
                    LivingEntity target = chomper.getTarget();
                    if (chomper.animTick >= 11 && chomper.animTick < 14) {
                        if (EntityUtil.checkCanEntityBeAttack(chomper, target) && !(target.getVehicle() instanceof Chomper) && chomper.position().distanceTo(target.position()) <= 1.5) {
                            target.startRiding(chomper);
                        }
                    } else if (chomper.animTick >= 53 && chomper.animTick < 56) {
                        Entity rider = chomper.getFirstPassenger();
                        if (rider != null) {
                            chomper.setAttackTime(chomper.getAttackCD());
                            rider.hurt(PVZDamageSource.chomperHurt(chomper), 40);
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
                            chomper.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400));
                        }
                        chomper.animTick = 0;
                    }
                }
                case EMERGING -> {
                    chomper.setPose(chomper.getAttackTime() <= 0 ? Pose.STANDING : Pose.CROUCHING);
                    if (chomper.getPose() == Pose.CROUCHING) {
                        chomper.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 400));
                    }
                    chomper.animTick = 0;
                }
                case CROAKING -> {
                    if (chomper.hasSkill("skill.pvz.chomper.energy_transduction")) {
                        Sun.spawnSunWithEffects(this.chomper.level, 50, this.chomper.getOnPos().above(), 0.4F);
                        Sun.spawnSunWithEffects(this.chomper.level, 15, this.chomper.getOnPos().above(), 0.4F);
                        Sun.spawnSunWithEffects(this.chomper.level, 5, this.chomper.getOnPos().above(), 0.5F);
                        Sun.spawnSunWithEffects(this.chomper.level, 5, this.chomper.getOnPos().above(), 0.5F);
                    }
                    chomper.setPose(Pose.STANDING);
                    chomper.animTick = 0;
                }
                case CROUCHING -> {
                    chomper.setPose(Pose.CROAKING);
                    chomper.animTick = 0;
                }
                case SWIMMING -> {
                    LivingEntity target = chomper.getTarget();
                    PathNavigation navigation = chomper.getNavigation();
                    if (! EntityUtil.isEntityValid(target) || chomper.getAttackTime() != 0) {
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
                            chomper.animTick = 0;
                        } else if (navigation.isDone()) {
                            navigation.moveTo(navigation.createPath(pos, 0), 1);
                        }
                    } else if (navigation.isDone()) {
                        navigation.moveTo(navigation.createPath(target, 0), 1);
                    }
                    if (chomper.getAttackTime() == 0 && EntityUtil.isEntityValid(target) && chomper.position().distanceTo(target.position()) <= 1) {
                        chomper.setPose(Pose.USING_TONGUE);
                        navigation.stop();
                        chomper.setDeltaMovement(Vec3.ZERO);
                        chomper.animTick = 0;
                    }
                }
            }
        }
    }
}
