package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.ICanBePlantedOn;
import com.hungteen.pvz.api.interfaces.ICanGroupUp;
import com.hungteen.pvz.api.interfaces.IHaveSkills;
import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.pvzRules.PVZRulesCapability;
import com.hungteen.pvz.common.enchantment.SunShovelEnchantment;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.Sun;
import com.hungteen.pvz.common.entity.ai.goal.AvoidTargetGoal;
import com.hungteen.pvz.common.entity.ai.goal.DisperseEnemyTargetGoal;
import com.hungteen.pvz.common.entity.ai.goal.FollowGroupLeaderGoal;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.register.PVZEnchantments;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.world.PVZDamageSource;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
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
import java.util.Objects;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

import static net.minecraftforge.event.ForgeEventFactory.canMountEntity;

public class VelociTurnip extends PathfinderMob implements ICanGroupUp, IPlant, IHaveSkills {
    private static final UUID ATTACK_MODIFIER_UUID = UUID.fromString("191e7725-e0a8-45cf-93ac-5a1749b36d03");
    private static final UUID HEALTH_MODIFIER_UUID = UUID.fromString("45e5f868-f733-423f-81b0-b3df87d3f266");
    private int animationTick = 0;
    private boolean animationChangeable = false;
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.veloci_radish.veloci_nip", PVZItems.ORIGIN_ESSENCE, 8, 4, 75, 440).avoidSkills(1),
            new Skill("skill.pvz.veloci_radish.clever_girls", PVZItems.ORIGIN_ESSENCE, 8, 4, 150, 440).avoidSkills(0)
    );
    public final AnimationState idleAnimationState = new AnimationState();
    public final AnimationState moveAnimationState = new AnimationState();
    public final AnimationState attackAnimationState = new AnimationState();
    public boolean skillBoosted = false;
    private int situationHurtCount = 0;
    protected static final EntityDataAccessor<Integer> POSE = SynchedEntityData.defineId(VelociTurnip.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> ROOT = SynchedEntityData.defineId(VelociTurnip.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> WILT_COUNTDOWN = SynchedEntityData.defineId(VelociTurnip.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> SKILL = SynchedEntityData.defineId(VelociTurnip.class, EntityDataSerializers.INT);
    public VelociTurnip(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
        this.setPathfindingMalus(BlockPathTypes.DANGER_FIRE, 16.0F);
        this.setPathfindingMalus(BlockPathTypes.DAMAGE_FIRE, -1.0F);
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
        this.entityData.define(WILT_COUNTDOWN, -1);
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AvoidTargetGoal(this,
                (entity -> entity instanceof Mob mob && mob.getTarget() == this && getTarget() == mob),
                4.0F, 1.0D, 1.0D));
        this.goalSelector.addGoal(2, new TurnipAttackGoal(this, 1, true));
        this.goalSelector.addGoal(3, new FollowGroupLeaderGoal(this));
        this.goalSelector.addGoal(4, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(2, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(1, new TurnipShareTargetGoal(this));
        this.targetSelector.addGoal(1, new DisperseEnemyTargetGoal(this));
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Root", getEntityData().get(ROOT));
        tag.putInt("WiltCountDown", getEntityData().get(WILT_COUNTDOWN));
        tag.putInt("Skill", getSkillVal(this));
        tag.putInt("TickCount", tickCount);

    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("Skill")) {
            setSkillVal(this, tag.getInt("Skill"));
        }
        if (tag.contains("WiltCountDown")) {
            this.getEntityData().set(WILT_COUNTDOWN, tag.getInt("WiltCountDown"));
        }
        if (tag.contains("Root")) {
            this.getEntityData().set(ROOT, tag.getBoolean("Root"));
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
    public List<Skill> getStaticSkillList(){
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
            } else if (entityData.get(POSE) == 2) {
                this.attackAnimationState.start(this.tickCount);
                this.moveAnimationState.stop();
            } else {
                this.idleAnimationState.start(this.tickCount);
                this.moveAnimationState.stop();
                this.attackAnimationState.stop();
            }
        }
        super.onSyncedDataUpdated(p_219422_);
    }

    //overrides
    @Override
    public MutableComponent isPositionSafe(Level level, BlockPos onPos, boolean isPlanting) {
        AABB aabb = AABB.ofSize(new Vec3(onPos.getX() + 0.5, onPos.getY() + 1 + getBbHeight() / 2, onPos.getZ() + 0.5), getBbWidth(), getBbHeight() - 0.0001, getBbWidth());
        if (BlockPos.betweenClosedStream(aabb).anyMatch((p_201942_) -> {
            BlockState blockstate = this.level.getBlockState(p_201942_);
            return !blockstate.isAir() && blockstate.isSuffocating(this.level, p_201942_) && Shapes.joinIsNotEmpty(blockstate.getCollisionShape(this.level, p_201942_).move((double)p_201942_.getX(), (double)p_201942_.getY(), (double)p_201942_.getZ()), Shapes.create(aabb), BooleanOp.AND);
        })) {
            return Component.translatable("hint.pvz.plant.no_enough_place");
        }
        if (! this.getEntityData().get(root()) || ! level.getBlockState(onPos).isAir()) {
            if (isPlanting) {
                if (! level.getBlockState(onPos).getFluidState().isEmpty()) {
                    return Component.translatable("hint.pvz.plant.cant_plant_in_water", this.getName());
                }
                this.moveTo(
                        onPos.getX() + 0.5,
                        onPos.getY() + (level.getBlockState(onPos).getCollisionShape(level, onPos).isEmpty() ?
                                (level.getFluidState(onPos).isEmpty() ? 0: level.getFluidState(onPos).getHeight(level, onPos)) :
                                level.getBlockState(onPos).getCollisionShape(level, onPos).bounds().maxY),
                        onPos.getZ() + 0.5);
                ((ServerLevel)this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()), this.getX(), this.getY(), this.getZ(), 5, 0.0D, 0.0D, 0.0D, 0.15F);
            }
            return null;
        } else {
            return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), level.getBlockState(onPos).getBlock().getName());
        }
    }
    public static boolean checkSpawnRules(EntityType<? extends LivingEntity> entityType, ServerLevelAccessor level, MobSpawnType mobSpawnType, BlockPos pos, RandomSource random) {
        return true;
    }

    @Override
    public MutableComponent isVehicleSafe(Entity target, boolean isPlanting) {
        if (target == null) {
            return Component.translatable("hint.pvz.plant.entity_not_present");
        }
        if (target instanceof ICanBePlantedOn && ((ICanBePlantedOn) target).canHold(this, isPlanting)) {
            if (PVZOwnedCapability.isTeammate(this, target)) {
                if (!canMountEntity(this, target, this.getVehicle() == target)) {
                    return Component.translatable("hint.pvz.plant.no_enough_place", this.getName());
                }
                if (isPlanting) {
                    this.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), 0.0F);
                    ((ServerLevel)this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()), this.getX(), this.getY(), this.getZ(), 5, 0.0D, 0.0D, 0.0D, 0.15F);
                    this.startRiding(target);
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
        AtomicReference<Item> packetItem = new AtomicReference<>();
        SeedPacketItem.seedPacketItemList.forEach(item -> {
            if (item.getEntity().equals(this.getType())) {
                packetItem.set(item);
            }});
        return packetItem.get() == null ? super.getPickResult() : new ItemStack(packetItem.get());
    }

    @Override
    public void tick() {
        // skill
        if (hasSkill(this, "skill.pvz.veloci_radish.veloci_nip")) {
            if (!level.isClientSide) {
                setGlowingTag(tickCount < 200 && (tickCount <= 100 || tickCount % 10 < 5));
                if (!skillBoosted) {
                    skillBoosted = true;
                    this.getAttribute(Attributes.ATTACK_DAMAGE).addTransientModifier(new AttributeModifier(ATTACK_MODIFIER_UUID, "skill bonus", 26, AttributeModifier.Operation.ADDITION));
                    this.getAttribute(Attributes.MAX_HEALTH).addTransientModifier(new AttributeModifier(HEALTH_MODIFIER_UUID, "skill bonus", 8, AttributeModifier.Operation.ADDITION));
                } else if (tickCount > 200) {
                    this.removeSkill(this, getSkillFromName("skill.pvz.veloci_radish.veloci_nip"));
                    this.getAttribute(Attributes.ATTACK_DAMAGE).removeModifier(ATTACK_MODIFIER_UUID);
                    this.getAttribute(Attributes.MAX_HEALTH).removeModifier(HEALTH_MODIFIER_UUID);
                }
            }
        } else if (hasSkill(this, "skill.pvz.veloci_radish.clever_girls")) {
            if (!level.isClientSide) {
                for (int i = 0; i < 3; i ++) {
                    VelociTurnip turnip = PVZEntities.VELOCI_TURNIP.get().create(level);
                    turnip.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), this.getXRot());
                    ((ServerLevel) level).addFreshEntityWithPassengers(turnip);
                    if (hasCustomName()) {
                        turnip.setCustomName(this.getCustomName());
                    }
                    PVZOwnedCapability cap = turnip.getCapability(PVZOwnedCapability.CAP).orElse(null);
                    PVZOwnedCapability thisCap = getCapability(PVZOwnedCapability.CAP).orElse(null);
                    cap.setOwner(thisCap.getOwner());
                    turnip.startFollowing(this);
                }
                this.removeSkill(this, getSkillFromName("skill.pvz.veloci_radish.clever_girls"));
                this.setDeltaMovement(this.getDeltaMovement().add(0.1, 0 ,0));
            }
        }
        //check plant situation damage.
        if (! level.isClientSide) {
            if (isPositionSafe(this.level, this.getOnPos(), false) != null && isVehicleSafe(getVehicle(), false) != null &&
                    this.getAttribute(Attributes.MAX_HEALTH) != null) {
                if (++ situationHurtCount > 100) {
                    this.hurt(PVZDamageSource.PLANT_WILT, (float) (0.4 * this.getAttribute(Attributes.MAX_HEALTH).getValue()));
                    situationHurtCount = 0;
                }
            } else {
                situationHurtCount = 0;
            }
        }
        //animation
        animationTick ++;
        if (entityData.get(POSE) == 0) {
            animationChangeable = animationTick == 24;
            animationTick = animationTick == 24 ? 0 : animationTick;
        } else {
            animationChangeable = animationTick == 20;
            animationTick = animationTick == 20 ? 0 : animationTick;
        }
        if (animationChangeable) {
            entityData.set(POSE, isPathFinding() ? 1 : 0);
        }
        super.tick();
    }

    public boolean onBeingShoveled(Player player, InteractionHand handIn) {
        //check permission.
        final boolean[] permission = {false};
        this.getCapability(PVZOwnedCapability.CAP).ifPresent((cap) -> {
            Entity owner = cap.getOwner();
            if (owner != null) {
                permission[0] = PVZRulesCapability.getBoolean("shovelPermission") ?
                        (PVZOwnedCapability.isTeammate(owner, player) || ! PVZRulesCapability.getBoolean("teamBattle")) : owner.is(player);
            } else {
                permission[0] = PVZRulesCapability.getBoolean("shovelPermission");
            }
        });
        //shovel plant.
        if (!player.level.isClientSide() && permission[0]) {
            ItemStack itemstack = player.getItemInHand(handIn);
            itemstack.hurtAndBreak(2, player, (entity) -> {
                entity.broadcastBreakEvent(handIn);
            });
            int enchantmentLevel = EnchantmentHelper.getTagEnchantmentLevel(PVZEnchantments.SUN_SHOVEL.get(), itemstack);
            PVZOwnedCapability cap = this.getCapability(PVZOwnedCapability.CAP).orElse(null);
            if (cap != null && enchantmentLevel > 0 && Objects.equals(cap.resource, PVZPlayerCapNBT.SUN)) {
                Sun.spawnSunsRandomlyByAmount(level, getOnPos(), (int) (cap.cost * SunShovelEnchantment.returnSunPercent(enchantmentLevel)), 0, 0.25F);
            }
            ((ServerLevel)this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()), this.getX(), this.getY(), this.getZ(), 5, 0.0D, 0.0D, 0.0D, 0.15F);
            this.remove(RemovalReason.DISCARDED);
            return true;
        }
        return false;
    }
    @Override
    public boolean isPushable(){
        return true;
    }
    @Override
    public boolean canBeLeashed(Player p_21418_) {
        return true;
    }
    @Override
    public boolean removeWhenFarAway(double p_27598_) {
        PVZOwnedCapability cap = this.getCapability(PVZOwnedCapability.CAP).orElse(null);
        return cap.getOwner() == null;
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

    private static class TurnipAttackGoal extends MeleeAttackGoal {
        public TurnipAttackGoal(VelociTurnip p_25552_, double p_25553_, boolean p_25554_) {
            super(p_25552_, p_25553_, p_25554_);
        }
        @Override
        protected void checkAndPerformAttack(LivingEntity entity, double p_25558_) {
            double d0 = this.getAttackReachSqr(entity);
            if (p_25558_ <= d0 && ((VelociTurnip) this.mob).animationChangeable) {
                ((VelociTurnip) this.mob).animationChangeable = false;
                this.mob.getEntityData().set(POSE, 2);
            }
            if (((VelociTurnip) this.mob).animationTick == 5 && this.mob.getEntityData().get(POSE) == 2) {
                this.mob.swing(InteractionHand.MAIN_HAND);
                this.mob.doHurtTarget(entity);
            }
        }
        @Override
        protected double getAttackReachSqr(LivingEntity p_25556_) {
            return this.mob.getBbWidth() * this.mob.getBbWidth() * 8.0F + p_25556_.getBbWidth() * p_25556_.getBbWidth();
        }
    }
    private static class TurnipShareTargetGoal extends Goal{
        VelociTurnip turnip;
        int count = 5;
        public TurnipShareTargetGoal(VelociTurnip turnip) {
            this.turnip = turnip;
        }

        @Override
        public boolean canUse() {
            return (EntityUtil.isEntityValid((Entity) turnip.getLeader()) && EntityUtil.checkCanEntityBeAttack(turnip, turnip.getTarget()));
        }

        @Override
        public void tick(){
            if (-- count == 0) {
                count = 5;
                for (VelociTurnip entity : turnip.level.getEntitiesOfClass(VelociTurnip.class, this.turnip.getBoundingBox().inflate(2),
                        (target) -> PVZOwnedCapability.isTeammate(this.turnip, target))) {
                    if ((turnip.getLeader() == entity.getLeader() || turnip.getLeader() == entity) && ! EntityUtil.checkCanEntityBeAttack(entity, entity.getTarget())) {
                        entity.setTarget(turnip.getTarget());
                    }
                }
            }
        }
    }
}