package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.api.events.PlantShoveledEvent;
import com.hungteen.pvz.api.interfaces.*;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.enchantment.SunShovelEnchantment;
import com.hungteen.pvz.common.entity.ai.goal.ServerStressReleaseGoals;
import com.hungteen.pvz.common.item.SeedItem;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.common.register.PVZEnchantments;
import com.hungteen.pvz.common.register.PVZParticles;
import com.hungteen.pvz.common.tags.PVZBlockTags;
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
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.Predicate;

import static net.minecraftforge.event.ForgeEventFactory.canMountEntity;
/**
 * Not including all plants.<br>
 * To identify if a mob is plant or not, use {@link com.hungteen.pvz.api.interfaces.IPlant} which supports more.
 */

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class SimplePlant extends Mob implements IHaveSkills, IPlant, ICanAttack {


    /**
     * whether this plant need proper plant-able blocks.
     */
    public static final EntityDataAccessor<Boolean> ROOT = SynchedEntityData.defineId(SimplePlant.class, EntityDataSerializers.BOOLEAN);
    /**
     * whether this plant occupy an area so other plants can't plant on.
     */
    public static final EntityDataAccessor<Boolean> TAKES_COINCIDE_DMG = SynchedEntityData.defineId(SimplePlant.class, EntityDataSerializers.BOOLEAN);
    /**
     * how long can this plant still live. When player is too far, this countdown goes faster.
     */
    public static final EntityDataAccessor<Integer> WILT_COUNTDOWN = SynchedEntityData.defineId(SimplePlant.class, EntityDataSerializers.INT);
    /**skill id. see {@link Skill}.*/
    public static final EntityDataAccessor<Integer> SKILL = SynchedEntityData.defineId(SimplePlant.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Integer> ATTACK_TIME = SynchedEntityData.defineId(SimplePlant.class, EntityDataSerializers.INT);
    protected boolean shouldAlign = true;
    protected boolean firstUnsafeSituationMercy = true;

    protected SimplePlant(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.MOVEMENT_SPEED, 0);
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new ServerStressReleaseGoals.ServerStressReleaseGoal(this));
        this.targetSelector.addGoal(0, new ServerStressReleaseGoals.ServerStressReleaseTargetGoal(this, false));
    }

    //IPlant
    @Override
    public EntityDataAccessor<Boolean> root() {
        return ROOT;
    }
    @Override
    public boolean takesCoincideDmg() {
        return this.getEntityData().get(TAKES_COINCIDE_DMG);
    }

    /** control if this plant has coincide dmg.
     */
    public boolean shouldHaveCoincideDmg(Level level, Vec3 position) {
        return shouldHaveCoincideDmg(this, level, position);
    }

    //for easy maintenance.
    public static boolean shouldHaveCoincideDmg(IPlant plant, Level level, Vec3 position) {
        if (! plant.takesCoincideDmg()) {
            return false;
        } else {
            Vec3 subPos = ((LivingEntity) plant).position();
            AABB range = ((LivingEntity) plant).getBoundingBox().move(position.add(-subPos.x, -subPos.y, -subPos.z)).inflate(-1e-4);
            List<Entity> list = level.getEntities(((LivingEntity) plant), range,
                    (entity) -> entity instanceof IPlant && ((IPlant)entity).takesCoincideDmg() && ! EntityUtil.hasRidingRelationship(((LivingEntity) plant), entity));
            return !list.isEmpty();
        }
    }
    public Set<TagKey<Block>> getMushroomAcceptableTags() {
        return Set.of(PVZBlockTags.PLANTABLE_DIRT, PVZBlockTags.PLANTABLE_STONE, PVZBlockTags.SCULK);
    }

    /**
     * @see INeedSafeSituation
     */
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
        if (plantableOn(level.getBlockState(finalPos))) {
            //final plant.
            BlockState state = level.getBlockState(pos);
            if (isPlanting) {
                this.moveTo(
                        pos.getX() + 0.5 + offset.getX(),
                        pos.getY() + (direction == Direction.UP ? (state.getCollisionShape(level, pos).isEmpty() ?
                                (level.getFluidState(pos).isEmpty() ? 0: level.getFluidState(pos).getHeight(level, pos)) :
                                state.getCollisionShape(level, pos).bounds().maxY) : offset.getY()),
                        pos.getZ() + 0.5 + offset.getZ());
            }
            return null;
        } else {
            return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), level.getBlockState(pos).getBlock().getName());
        }
    }

    @Override
    public MutableComponent customVehicleSafe(PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        if (this.isPassenger() && ! EntityUtil.isEntityValid(this.getVehicle())) {
            this.stopRiding();
        }
        //resource check.
        if (isPlanting && event != null) {
            if (event.cost > PVZPlayerCapability.getValue(event.getEntity(), event.resource)) {
                return Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource));
            }
        }
        //target unavailable.
        if (target == null) {
            if (! isPlanting) {
                //find rideable entity.
                if (this.getVehicle() == null) {
                    this.boardingCooldown = 0;//renew riding cool down.
                    List<Entity> list = level.getEntities(this, this.getBoundingBox().inflate(0, 1, 0),
                            (entity) -> entity instanceof IPlant && ((IPlant)entity).takesCoincideDmg() && this.getVehicle() != entity && entity.getVehicle() != this);
                    list.forEach((entity) -> {
                        if (this.getVehicle() == null && entity instanceof ICanBePlantedOn vehicle && vehicle.canHold(this, false) && EntityUtil.isTeammate(this, entity)) {
                            this.startRiding(entity);
                        }
                    });
                    if (this.getVehicle() != null) {
                        return null;//skip other logic.
                    }
                }
            }
            return Component.translatable("hint.pvz.plant.entity_not_present");
        }
        //target is ICanBePlantedOn.
        if (target instanceof ICanBePlantedOn && ((ICanBePlantedOn) target).canHold(this, isPlanting)) {
            if (EntityUtil.isTeammate(this, target)) {
                if (isPlanting) {
                    if (canMountEntity(this, target, true)) {
                        this.moveTo(target.getX(), target.getY() + target.getPassengersRidingOffset(), target.getZ(), target.getYRot(), 0.0F);
                        this.startRiding(target);
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

    //skill
    @Override
    public int getSkillVal(Object obj) {
        return entityData.get(SKILL);
    }
    @Override
    public void setSkillVal(Object obj, int val) {
        entityData.set(SKILL, val);
    }
    @Override
    public List<Skill> getBasicStaticSkillList() {
        return List.of();
    }

    //overrides
    @Override
    public void baseTick() {
        super.baseTick();
        //check plant situation damage.
        firstUnsafeSituationMercy = testPlantSafe(this, firstUnsafeSituationMercy);
        //about aligning blocks.
        if (! this.isOnGround() || this.getDeltaMovement().distanceToSqr(new Vec3(0, 0, 0)) > 0.05) {
            shouldAlign = true;
        } else if (shouldAlign) {
            alignBlocks();
            setDeltaMovement(0, 0, 0);
            shouldAlign = false;
        }
        //mushroom relative
        if (this instanceof IMushroom mushroom) {
            if (level.isClientSide) {
                if (this.isSleeping() && this.tickCount % 100 < 30 && this.tickCount % 10 == 0) {
                    level.addParticle(PVZParticles.Z.get(),
                            this.position().x + random.nextFloat() * 0.6 - 0.3,
                            this.position().y + random.nextFloat() * 0.3,
                            this.position().z + random.nextFloat() * 0.6 - 0.3, 0, 0, 0);
                }
            } else {
                if (! this.isSleeping() && mushroom.shouldFallAsleep()) {
                    mushroom.fallAsleep();
                } else if (this.isSleeping() && mushroom.shouldWakeUp()) {
                    mushroom.wakeUp();
                }
            }
        }
        //TODO relative codes. add particle when plant is dying.
    }

    /**
     * control if this plant can push another entity.*/
    public Predicate<Entity> canPush() {
        return (entity) -> this.isPushable();
    }
    /**
     * control if this plant can be pushed by another entity.*/
    @Override
    public boolean isPushable(){
        return false;
    }

    @Override
    public int getMaxAirSupply() {
        return 20;
    }

    public boolean canBeLeashed(Player p_21418_) {
        return false;
    }
    public void alignBlocks() {
        BlockPos pos = this.getOnPos();
        moveTo(pos.getX() + 0.5, this.getY(), pos.getZ() + 0.5);
    }

    protected InteractionResult mobInteract(Player player, InteractionHand handIn) {
        if (tryShovel(player, handIn, this)) {
            return level.isClientSide ? InteractionResult.CONSUME : InteractionResult.SUCCESS;
        } else {
            return super.mobInteract(player, handIn);
        }
    }

    /**For plants not extending SimplePlant, use {@link SimplePlant#onBeingShoveled(Player, InteractionHand, LivingEntity)}*/
    public boolean onBeingShoveled(Player player, InteractionHand handIn) {
        return onBeingShoveled(player, handIn, this);
    }

    //for easy maintenance. These methods are put here because they imported classes that are not in api pack.
    public static boolean tryShovel(Player player, InteractionHand handIn, LivingEntity target) {
        ItemStack itemstack = player.getItemInHand(handIn);
        ItemStack itemstack1 = player.getItemInHand(InteractionHand.MAIN_HAND);
        boolean result = (itemstack.getItem() instanceof ShovelItem || itemstack.getItem() instanceof IPlantShovelable)
                && ! player.getCooldowns().isOnCooldown(itemstack.getItem())
                && ! (handIn == InteractionHand.OFF_HAND && itemstack1.getItem() instanceof SeedPacketItem<?>);
                    // TODO why does shovel in offHand still runs useOn() when seedPacket is already planted? If can solve, this part can be deleted.
        if (target.level.isClientSide) {
            return result;
        }
        if (result) {
            PlantShoveledEvent event = new PlantShoveledEvent(player, handIn, target, PlantShoveledEvent.Phase.PRE);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.isCanceled()) {
                return false;
            }
            result = ((IPlant) target).onBeingShoveled(player, handIn);
            //post-shovel.
            if (result && itemstack.getItem() instanceof IPlantShovelable shovelable) {
                shovelable.onPlantShoveled(itemstack, player, target, handIn);
                event = new PlantShoveledEvent(player, handIn, target, PlantShoveledEvent.Phase.POST);
                MinecraftForge.EVENT_BUS.post(event);
            }
        }
        return result;
    }
    public static boolean onBeingShoveled(Player player, InteractionHand handIn, LivingEntity target) {
        //check permission.
        final boolean[] permission = {false};
        Entity owner = PVZEntityCapability.getOwner(target);
        if (owner != null) {
            permission[0] = PVZConfig.PVZGameRules.getBoolean(player.level, PVZConfig.Common.shovelPermission) ?
                    (EntityUtil.isTeammate(owner, player) || ! PVZConfig.PVZGameRules.getBoolean(player.level, PVZConfig.Common.teamBattle)) : owner.is(player);
        } else {
            permission[0] = PVZConfig.PVZGameRules.getBoolean(player.level, PVZConfig.Common.shovelPermission) && EntityUtil.isTeammate(target, player);
        }
        //shovel plant.
        if (! player.level.isClientSide()) {
            if (! permission[0]) {
                player.displayClientMessage(Component.translatable("hint.pvz.plant.need_own_team"), true);
            } else {
                ItemStack itemstack = player.getItemInHand(handIn);
                itemstack.hurtAndBreak(2, player, (entity) -> entity.broadcastBreakEvent(handIn));
                int enchantmentLevel = EnchantmentHelper.getTagEnchantmentLevel(PVZEnchantments.SUN_SHOVEL.get(), itemstack);
                PVZEntityCapability cap = target.getCapability(PVZEntityCapability.CAP).orElse(null);
                if (cap != null && enchantmentLevel > 0 && Objects.equals(cap.resource, PVZPlayerCapNBT.SUN)) {
                    Sun.spawnSunsWithEffectsByAmount(target.level, target.getOnPos(), (int) (cap.cost * SunShovelEnchantment.returnSunPercent(enchantmentLevel)), 0, 0.25F);
                }
                ((ServerLevel)target.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, target.level.getBlockState(target.getOnPos())).setPos(target.getOnPos()), target.getX(), target.getY(), target.getZ(), 5, 0.0D, 0.0D, 0.0D, 0.15F);
                target.remove(RemovalReason.DISCARDED);
                if (target.isVehicle()) {
                    target.getPassengers().forEach((entity -> {
                        if (entity instanceof INeedSafeSituation entity1) {
                            entity1.isVehicleSafe(null, target.getVehicle(), true);
                        }
                    }));
                }
                return true;
            }
            return false;
        }
        return true;
    }

    /**a method that should be added to {@link Entity#baseTick() baseTick()} method of each {@link INeedSafeSituation} to test if the plant is safe. Put here for easy maintenance.
     @return boolean value of if this plant still has first time environment hurt mercy.*/
    public static <T extends LivingEntity & IPlant & INeedSafeSituation> boolean testPlantSafe(T plant, boolean firstUnsafeSituationMercy) {
        if (! plant.level.isClientSide) {
            if (plant.tickCount % 40 == 0) {
                if (plant.isPositionSafe(null, plant.level, plant.getRootBlockPos(), plant.getGrowDirection(), false) != null &&
                        plant.isVehicleSafe(null, plant.getVehicle(), false) != null &&
                        plant.getAttribute(Attributes.MAX_HEALTH) != null) {
                    if (! firstUnsafeSituationMercy) { //ensure plants won't instantly get hurt after leaving safe situation.
                        plant.hurt(PVZDamageSource.PLANT_WILT, (float) (0.2 * plant.getAttribute(Attributes.MAX_HEALTH).getValue()));
                    } else {
                        return false;
                    }
                } else {
                    return true;
                }
            }
        }
        return firstUnsafeSituationMercy;
    }


    //data
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ROOT, true);
        this.entityData.define(TAKES_COINCIDE_DMG, true);
        this.entityData.define(WILT_COUNTDOWN, -1);
        this.entityData.define(SKILL, 0);
        this.entityData.define(ATTACK_TIME, 0);
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("Root", getEntityData().get(ROOT));
        tag.putBoolean("HasCoincideDmg", getEntityData().get(TAKES_COINCIDE_DMG));
        tag.putInt("WiltCountDown", getEntityData().get(WILT_COUNTDOWN));
        saveSkills(tag);
        tag.putInt("PlantAttackTime", getAttackTime());

    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        readSkills(tag);
        if (tag.contains("PlantAttackTime")) {
            this.setAttackTime(tag.getInt("PlantAttackTime"));
        }
        if (tag.contains("WiltCountDown")) {
            this.getEntityData().set(WILT_COUNTDOWN, tag.getInt("WiltCountDown"));
        }
        if (tag.contains("Root")) {
            this.getEntityData().set(ROOT, tag.getBoolean("Root"));
        }
        if (tag.contains("HasCoincideDmg")) {
            this.getEntityData().set(TAKES_COINCIDE_DMG, tag.getBoolean("HasCoincideDmg"));
        }
    }


    //others
    @Override
    protected void pushEntities() {
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

    public int getAttackTime() {
        return entityData.get(ATTACK_TIME);
    }

    public void setAttackTime(int cd) {
        entityData.set(ATTACK_TIME, cd);
    }

    /**For Plants that not extending SimplePlant, use {@link SimplePlant#getPickResult(LivingEntity)}*/
    @Nullable
    public ItemStack getPickResult() {
        return getPickResult(this);
    }

    //for easy maintenance.
    public static ItemStack getPickResult(LivingEntity entity) {
        return (SeedItem.getSeed(entity.getType())).getDefaultInstance();
    }
    @Override
    public boolean removeWhenFarAway(double p_27598_) {
        PVZEntityCapability cap = this.getCapability(PVZEntityCapability.CAP).orElse(null);
        return cap == null || ! cap.hasOwner();
        //TODO handle situation when player is not available when loading.
    }
    public static boolean checkSpawnRules(EntityType<? extends LivingEntity> entityType, ServerLevelAccessor level, MobSpawnType mobSpawnType, BlockPos pos, RandomSource random) {
        return entityType instanceof IWaterPlant ? level.getBlockState(pos.below()).is(PVZBlockTags.PLANTABLE_DIRT) : level.getBlockState(pos).getFluidState().is(FluidTags.WATER);
    }

}
