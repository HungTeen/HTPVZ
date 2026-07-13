package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.api.events.GardenPlantGrowUpEvent;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.api.interfaces.IGardenPlant;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.plants.base.SimplePlant;
import com.hungteen.pvz.common.register.PVZCriteriaTriggers;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZSoundEvents;
import com.hungteen.pvz.common.register.PVZStats;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
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
import net.minecraft.world.entity.animal.Bee;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraftforge.common.MinecraftForge;

import javax.annotation.Nullable;
import java.util.List;
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
                    this.level.playSound(null, this, SoundEvents.DYE_USE, SoundSource.PLAYERS, 1.0F, 1.0F);
                    if (! this.level.isClientSide) {
                        level.playSound(null, this, PVZSoundEvents.SPROUT_HARVEST.get(), SoundSource.PLAYERS, 1.0F, 1.0F);
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
    public InteractionResult onWatered(@Nullable Player player, @Nullable ItemStack stack) {
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

    public InteractionResult onFertilized(@Nullable Player player, @Nullable ItemStack stack) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (this.isRequiringFertilizer()) {
            GardenPlantGrowUpEvent event = new GardenPlantGrowUpEvent(this, true);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.shouldProduce) {
                ExperienceOrb.award((ServerLevel)this.level, this.position(), 3);
                this.entityData.set(IS_PRODUCING, true);
            }
            if (! event.isCanceled()) {
                if (! level.isClientSide)
                    level.playSound(null, this, PVZSoundEvents.SPROUT_WATER.get(), player == null ? SoundSource.NEUTRAL : SoundSource.PLAYERS, 1.0F, 1.0F);
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
    public void tick() {
        super.tick();
        if (! this.level.isClientSide) {
            if (this.getRemainingGrowTick() > 40) {
                List<Bee> bees = this.level.getEntitiesOfClass(Bee.class
                        , this.getBoundingBox().inflate(0.5, 0.5, 0.5)
                        , bee -> bee.savedFlowerPos != null && bee.savedFlowerPos.equals(this.blockPosition()));
                if (! bees.isEmpty()) {
                    this.growEndTime -= 3;
                    if (this.random.nextInt(3) == 0) {
                        ((ServerLevel) this.level).sendParticles(ParticleTypes.COMPOSTER,
                                position().x, position().y + 0.3, position().z,
                                2, 0.3, 0.3, 0.3, 0);
                    }
                }
            }
        }
    }

    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return pose == Pose.DIGGING || pose == Pose.DYING ? EntityDimensions.scalable(0.4F, 0.5F) :
                pose == Pose.SWIMMING ? EntityDimensions.scalable(0.6F, 0.8F) :
                        this.getType().getDimensions();
    }

    public void produce() {
        ItemEntity itementity = new ItemEntity(this.level, this.getX(), this.getEyeY(), this.getZ(), this.getRandomLoot());
        BlockPos pos = blockPosition();
        itementity.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
        level.addFreshEntity(itementity);
    }

    private @Nullable ItemStack getRandomLoot() {
        LootTable lootTable = this.getProduceLootTable();
        if (lootTable == null) return ItemStack.EMPTY;
        List<ItemStack> items = lootTable.getRandomItems(
                new LootContext.Builder((ServerLevel) this.level)
                        .create(LootContextParamSet.builder().build()));
        if (items.isEmpty()) return ItemStack.EMPTY;
        return items.get(this.random.nextInt(items.size()));
    }

    public LootTable getProduceLootTable() {
        return this.level.getServer().getLootTables().get(Util.prefix("entities/marigold_produce_" + this.getGrowLevel()));
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
        return this.entityData.get(REQUIRES_WATER) && isRequiring() && this.getGrowLevel() < this.getMaxLevel();
    }
    @Override
    public void setRequiringFertilizer(boolean bool) {
        this.entityData.set(REQUIRES_WATER, !bool);
    }
    @Override
    public boolean isRequiringFertilizer() {
        return (! this.entityData.get(REQUIRES_WATER)) && isRequiring() && this.getGrowLevel() < this.getMaxLevel();
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
    public void setColor(int color) {
        this.entityData.set(COLOR, color);
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
                if (PVZConfig.PVZGameRules.getBoolean(mariGold.level, PVZConfig.Common.marigoldsRequires))
                    setRequiring(true);
                else {
                    mariGold.onFertilized(null, null);
                }
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
                if (mariGold.getGrowLevel() >= mariGold.getMaxLevel()) {
                    mariGold.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> {
                        Entity owner = cap.getOwner();
                        if (owner instanceof ServerPlayer serverPlayer) {
                            serverPlayer.awardStat(PVZStats.HARVEST_MARIGOLDS);
                            int color = mariGold.getColor();
                            for (DyeColor dye: PVZCriteriaTriggers.marigoldTriggers.keySet()) {
                                if (dye.getTextColor() == color) {
                                    PVZCriteriaTriggers.marigoldTriggers.get(dye).trigger(serverPlayer);
                                    break;
                                }
                            }
                        }
                    });
                    mariGold.discard();
                }
            } else if (time == 8 || time == 10 || time == 12
                    || ((time == 9 || time == 11 || time == 13 || time == 15) && random.nextBoolean())) {
                mariGold.produce();
            } else if (time == 2) {
                mariGold.level.playSound(null, mariGold
                        , mariGold.getGrowLevel() < 3 ? PVZSoundEvents.MARIGOLD_PRODUCE.get() : PVZSoundEvents.MARIGOLD_PRODUCE_GEMS.get()
                        , SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
        }
    }
}
