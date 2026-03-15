package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.api.events.GardenPlantGrowUpEvent;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.api.interfaces.IGardenPlant;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.plants.base.SimplePlant;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZStats;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;

import java.util.Set;

public class MariGold extends SimplePlant implements IGardenPlant {
    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState produceAnimationState = new AnimationState();
    public static final EntityDataAccessor<Boolean> REQUIRES_WATER = SynchedEntityData.defineId(MariGold.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> GROW_LEVEL = SynchedEntityData.defineId(MariGold.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> GROW_ENDED = SynchedEntityData.defineId(MariGold.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> IS_PRODUCING = SynchedEntityData.defineId(MariGold.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Integer> COLOR = SynchedEntityData.defineId(MariGold.class, EntityDataSerializers.INT);
    public long growEndTime = -1;

    public MariGold(EntityType<? extends Mob> type, Level worldIn) {
        super(type, worldIn);
        this.entityData.set(DATA_POSE, Pose.DIGGING);
        this.setRemainingGrowTick(PVZConfig.PVZGameRules.getInt(this.level, PVZConfig.Common.marigoldGrowTime));
    }

    //interaction
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand handIn) {
        if (this.isAlive()) {
            if (PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.dyeMarigold) && player.getItemInHand(handIn).getItem() instanceof DyeItem dye) {
                if (this.isAlive() && this.getColor() != dye.getDyeColor().getTextColor()) {
                    this.level.playSound(player, this, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    if (! this.level.isClientSide) {
                        this.setColor(dye.getDyeColor().getTextColor());
                        player.getItemInHand(handIn).shrink(1);
                    }
                }
            }
        }
        return super.mobInteract(player, handIn);
    }
    public void setupPresentationAnim() {
        this.idleAnimationState.start(this.tickCount);
        this.setGrowLevel(2);
    }
    /**Check {@link IGardenPlant} for the two methods below.*/
    @Override
    public InteractionResult onWatered(Player player, ItemStack stack) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (this.isRequiringWater()) {
            this.setRemainingGrowTick(100 + random.nextInt(100));
            this.setRequiringWater(random.nextInt(5) < 2);
            this.setRequiring(false);
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.FAIL;
        }
    }

    public InteractionResult onFertilized(Player player, ItemStack stack) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (this.isRequiringFertilizer()) {
            GardenPlantGrowUpEvent event = new GardenPlantGrowUpEvent(this, true);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.shouldApplyEffects) {
                this.entityData.set(IS_PRODUCING, true);
            }
            if (! event.isCanceled()) {
                setGrowLevel(this.getGrowLevel() + 1);
            }
            this.setRequiringWater(true);
            this.setRemainingGrowTick(PVZConfig.PVZGameRules.getInt(this.level, PVZConfig.Common.marigoldGrowTime));
            this.setRequiring(false);
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.FAIL;
        }
    }
    @Override
    public void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(GROW_LEVEL, 0);
        this.entityData.define(IS_PRODUCING, false);
        this.entityData.define(REQUIRES_WATER, true);
        this.entityData.define(GROW_ENDED, false);
        this.entityData.define(COLOR, DyeColor.values()[this.random.nextInt(16)].getTextColor());
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return pose == Pose.DIGGING || pose == Pose.DYING ? EntityDimensions.scalable(0.4F, 0.5F) :
                pose == Pose.SWIMMING ? EntityDimensions.scalable(0.6F, 0.8F) :
                        this.getType().getDimensions();
    }

    public void produce() {
        ItemEntity itementity = new ItemEntity(this.level, this.getX(), this.getEyeY(), this.getZ(), this.getRandomIngot().getDefaultInstance());
        BlockPos pos = blockPosition();
        itementity.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        level.addFreshEntity(itementity);
    }

    private Item getRandomIngot() {
        if (this.getGrowLevel() < 3) {
            if (random.nextFloat() < getIronChance()) {
                return Items.IRON_INGOT;
            }
            return Items.GOLD_INGOT;
        }
        return PVZItems.JEWEL.get();
    }


    //methods

    /**Check {@link IGardenPlant} for the overriding methods below.*/
    @Override
    public int getGrowLevel() {
        return this.entityData.get(GROW_LEVEL);
    }
    @Override
    public int getMaxLevel() {
        return 3;
    }
    @Override
    public void setGrowLevel(int level) {
        this.entityData.set(GROW_LEVEL, level);
        this.entityData.set(DATA_POSE, level == 0 ? Pose.DIGGING : (level == 1 ? Pose.SWIMMING : Pose.STANDING));
    }
    @Override
    public void setRequiringWater(boolean bool) {
        this.entityData.set(REQUIRES_WATER, bool);
    }
    @Override
    public boolean isRequiringWater() {
        return this.entityData.get(REQUIRES_WATER) && isRequiring() && this.getGrowLevel() < 3;
    }
    @Override
    public void setRequiringFertilizer(boolean bool) {
        this.entityData.set(REQUIRES_WATER, !bool);
    }
    @Override
    public boolean isRequiringFertilizer() {
        return (! this.entityData.get(REQUIRES_WATER)) && isRequiring() && this.getGrowLevel() < 3;
    }
    protected boolean isRequiring() {
        return this.entityData.get(GROW_ENDED);
    }
    protected void setRequiring(boolean bool) {
        this.entityData.set(GROW_ENDED, bool);
    }
    @Override
    public int getRemainingGrowTick() {
        return (this.getLevel().isClientSide || this.growEndTime == -1) ? 0 : (int) (this.growEndTime - this.getLevel().getGameTime());
    }
    @Override
    public void setRemainingGrowTick(int time) {
        this.growEndTime = this.getLevel().isClientSide ? -1L : this.getLevel().getGameTime() + time;
    }
    public int getColor() {
        return this.entityData.get(COLOR);
    }
    public void setColor(int level) {
        this.entityData.set(COLOR, level);
    }


    //definitions

    @Override
    public MutableComponent customVehicleSafe(PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        if (target == null) {
            return net.minecraft.network.chat.Component.translatable("hint.pvz.plant.entity_not_present");
        }
        return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), target.getName());
    }
    public Set<TagKey<Block>> getAcceptableTags() {
        return Set.of(PVZBlockTags.PLANTABLE_DIRT, PVZBlockTags.GARDEN_FLOWER_POT);
    }

    public float getIronChance() {
        return 0.75F;
    }

    @Override
    public ItemStack getPickResult() {
        return PVZItems.MARIGOLD_SPROUT.get().getDefaultInstance();
    }
    @Override
    public boolean removeWhenFarAway(double p_27598_) {
        return false;
    }
    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 8D)
                .add(Attributes.FOLLOW_RANGE, 2D);
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new MarigoldProduceGoal(this));
        this.goalSelector.addGoal(3, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(4, new RandomLookAroundGoal(this));
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("growLevel", this.getGrowLevel());
        tag.putLong("growEndTime", this.growEndTime);
        tag.putBoolean("requiresWater", this.isRequiringWater());
        tag.putInt("color", this.entityData.get(COLOR));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        this.setGrowLevel(tag.getInt("growLevel"));
        this.setRequiringWater(tag.getBoolean("requiresWater"));
        this.growEndTime = tag.getLong("growEndTime");
        this.entityData.set(COLOR, tag.getInt("color"));
    }
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_219422_) {
        if (IS_PRODUCING.equals(p_219422_)) {
            if (entityData.get(IS_PRODUCING)) {
                this.idleAnimationState.stop();
                this.produceAnimationState.start(this.tickCount);
            } else {
                this.produceAnimationState.stop();
                this.idleAnimationState.start(this.tickCount);
            }
        }

        super.onSyncedDataUpdated(p_219422_);
    }
    protected class MarigoldProduceGoal extends Goal {
        protected MariGold mariGold;
        public MarigoldProduceGoal(MariGold marigold) {
            this.mariGold = marigold;
        }
        @Override
        public boolean canUse() {
            if (mariGold.getRemainingGrowTick() <= 0) {
                setRequiring(true);
            }
            return mariGold.entityData.get(IS_PRODUCING);
        }
        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
        @Override
        public void tick() {
            int time = PVZConfig.PVZGameRules.getInt(mariGold.level, PVZConfig.Common.marigoldGrowTime) - mariGold.getRemainingGrowTick();
            if (time > 40) {
                mariGold.getEntityData().set(IS_PRODUCING, false);
                if (mariGold.getGrowLevel() >= 3) {
                    mariGold.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> {
                        Entity owner = cap.getOwner();
                        if (owner instanceof Player player) player.awardStat(PVZStats.HARVEST_MARIGOLDS);
                    });
                    mariGold.discard();
                }
            } else if (time == 8 || time == 10 || time == 12
                    || ((time == 9 || time == 11 || time == 13 || time == 15) && random.nextBoolean() && mariGold.getGrowLevel() < 3)) {
                mariGold.produce();
            }
        }
    }
}
