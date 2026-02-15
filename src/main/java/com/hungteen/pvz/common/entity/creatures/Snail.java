package com.hungteen.pvz.common.entity.creatures;

import com.hungteen.pvz.api.interfaces.IGardenPlant;
import com.hungteen.pvz.common.item.FertilizerItem;
import com.hungteen.pvz.common.item.WateringPotItem;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.tags.PVZItemTags;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.behavior.BehaviorUtils;
import net.minecraft.world.entity.ai.control.JumpControl;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.TemptGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.ai.navigation.PathNavigation;
import net.minecraft.world.entity.ai.navigation.WallClimberNavigation;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.npc.InventoryCarrier;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class Snail extends TamableAnimal implements InventoryCarrier {

    float animationCount = -1;
    public AnimationState commonAnimationState = new AnimationState();
    public AnimationState inAnimationState = new AnimationState();
    public AnimationState outAnimationState = new AnimationState();

    public long awakenTick = -1;
    private final SimpleContainer inventory = new SimpleContainer(4);

    static final UUID speedModifierUUID = UUID.fromString("88af647f-66f7-3017-95af-5f1a2fb2b730");
    static final UUID armorModifierUUID = UUID.fromString("f9c668cd-b7bf-ce27-3caa-c188738feb43");
    static final UUID knockBackModifierUUID = UUID.fromString("58084a23-aaad-c12f-7a7c-67fe101b1f46");

    private static final EntityDataAccessor<Boolean> CLIMBING = SynchedEntityData.defineId(Snail.class, EntityDataSerializers.BOOLEAN);

    public Snail(EntityType<? extends TamableAnimal> p_27557_, Level level) {
        super(p_27557_, level);
        if (! level.isClientSide) {
            this.jumpControl = new SnailJumpControl(this);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 12)
                .add(Attributes.FOLLOW_RANGE, 12)
                .add(Attributes.MOVEMENT_SPEED, 0.1);
    }
    @Override
    public void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(CLIMBING, false);
    }
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new SnailRetreatIntoShellGoal(this));
        this.goalSelector.addGoal(2, new TemptGoal(this, 1.25D, Ingredient.of(PVZItems.CHOCOLATE.get()), false));
        this.goalSelector.addGoal(3, new SnailCollectItemsGoal(this));
        this.goalSelector.addGoal(4, new SnailFeedPlantsGoal(this));
        this.goalSelector.addGoal(5, new WaterAvoidingRandomStrollGoal(this, 1.0D));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
        super.registerGoals();
    }

    protected PathNavigation createNavigation(Level p_33802_) {
        return new WallClimberNavigation(this, p_33802_);
    }

    public boolean isClimbing() {
        return this.entityData.get(CLIMBING);
    }

    public boolean onClimbable() {
        return this.isClimbing();
    }

    public void setClimbing(boolean climbing) {
        this.entityData.set(CLIMBING, climbing);
    }
    public int getAwakenTime() {
        return (this.getLevel().isClientSide || this.awakenTick == -1) ? 2100000000 : (int) (this.getLevel().getGameTime() - this.awakenTick);
    }

    public void recordAwakenTick() {
        this.awakenTick = this.getLevel().isClientSide ? -1L : this.getLevel().getGameTime();
    }

    public void recordAwakenTick(long tick) {
        this.awakenTick = this.getLevel().isClientSide ? -1L : tick;
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            this.setClimbing(this.horizontalCollision && this.getPose() == Pose.STANDING);
        }
        if (this.getPose() == Pose.STANDING) {
            if (commonAnimationState.isStarted()) {
                animationCount += this.getDeltaMovement().lengthSqr() > 0.05 ? 1 : 0.5;
            }
            if (animationCount <= 0) {
                if (animationCount == 0) {
                    commonAnimationState.start(this.tickCount);
                    outAnimationState.stop();
                    EntityUtil.removeModifierFromAttribute(this, Attributes.MOVEMENT_SPEED, speedModifierUUID);
                    EntityUtil.removeModifierFromAttribute(this, Attributes.ARMOR, armorModifierUUID);
                    EntityUtil.removeModifierFromAttribute(this, Attributes.KNOCKBACK_RESISTANCE, knockBackModifierUUID);
                }
                animationCount += 1;
            }
        } else {
            EntityUtil.addModifierToAttribute(this, Attributes.MOVEMENT_SPEED, new AttributeModifier(speedModifierUUID, "snail_shell", -1, AttributeModifier.Operation.MULTIPLY_BASE));
            EntityUtil.addModifierToAttribute(this, Attributes.ARMOR, new AttributeModifier(armorModifierUUID, "snail_shell", 20, AttributeModifier.Operation.ADDITION));
            EntityUtil.addModifierToAttribute(this, Attributes.KNOCKBACK_RESISTANCE, new AttributeModifier(knockBackModifierUUID, "snail_shell", 1, AttributeModifier.Operation.ADDITION));
        }
        if (this.hasEffect(MobEffects.MOVEMENT_SPEED)) {
            this.recordAwakenTick(this.level.getGameTime() - 100);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putLong("AwakenTick", awakenTick);
        tag.putBoolean("Climbing", this.isClimbing());
        tag.putBoolean("Sleeping", this.getPose() != Pose.STANDING);
        tag.put("Inventory", this.inventory.createTag());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("AwakenTick")) {
            this.awakenTick = tag.getLong("AwakenTick");
        }
        if (tag.contains("Climbing")) {
            this.setClimbing(tag.getBoolean("Climbing"));
        }
        if (tag.contains("Sleeping")) {
            this.setPose(tag.getBoolean("Sleeping") ? Pose.DIGGING : Pose.STANDING);
        }
        if (tag.contains("Inventory")) {
            this.inventory.fromTag(tag.getList("Inventory", 10));
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> data) {
        super.onSyncedDataUpdated(data);
        if (data == DATA_POSE) {
            if (this.getPose() == Pose.STANDING) {
                outAnimationState.start(this.tickCount);
                inAnimationState.stop();
                commonAnimationState.stop();
                animationCount = -18;
            } else {
                inAnimationState.start(this.tickCount);
                outAnimationState.stop();
                commonAnimationState.stop();
            }
        }
    }

    public InteractionResult mobInteract(Player player, InteractionHand p_30413_) {
        ItemStack itemstack = player.getItemInHand(p_30413_);
        if (this.level.isClientSide) {
            boolean flag = this.isOwnedBy(player) || this.isTame() || (isFood(itemstack) && ! this.isTame());
            return flag ? InteractionResult.CONSUME : InteractionResult.PASS;
        } else {
            if (this.isTame()) {
                if (this.getPose() != Pose.STANDING) {
                    this.emergeFromShell();
                }
                if (this.isFood(itemstack) && ! this.hasEffect(MobEffects.MOVEMENT_SPEED)) {
                    this.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 3000, 5));
                    this.addEffect(new MobEffectInstance(MobEffects.REGENERATION, 100));
                    if (!player.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }

                    this.gameEvent(GameEvent.EAT, this);
                    return InteractionResult.SUCCESS;
                } else if (itemstack.isEmpty()) {
                    ItemStack item = this.getItemInHand(InteractionHand.MAIN_HAND);
                    BehaviorUtils.throwItem(this, item, player.position());
                    for (int i = 0; i < this.getInventory().getContainerSize(); i ++) {
                        if (this.getInventory().getItem(i) == item) {
                            this.getInventory().setItem(i, ItemStack.EMPTY);
                            this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            } else if (isFood(itemstack)) {
                if (!player.getAbilities().instabuild) {
                    itemstack.shrink(1);
                }

                if (this.random.nextInt(3) == 0 && !net.minecraftforge.event.ForgeEventFactory.onAnimalTame(this, player)) {
                    this.tame(player);
                    this.recordAwakenTick();
                    this.navigation.stop();
                    this.setTarget(null);
                    this.level.broadcastEntityEvent(this, (byte)7);
                } else {
                    this.level.broadcastEntityEvent(this, (byte)6);
                }
                return InteractionResult.SUCCESS;
            }

            return super.mobInteract(player, p_30413_);
        }
    }

    public void retreatIntoShell() {
        this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
        this.setPose(Pose.DIGGING);
    }
    public void emergeFromShell() {
        this.setPose(Pose.STANDING);
        this.recordAwakenTick();
    }

    @Override
    public boolean isFood(ItemStack p_27600_) {
        return p_27600_.is(PVZItems.CHOCOLATE.get());
    }

    public SimpleContainer getInventory() {
        return this.inventory;
    }

    protected void pickUpItem(ItemEntity p_35467_) {
        InventoryCarrier.pickUpItem(this, this, p_35467_);
    }

    public boolean wantsToPickUp(ItemStack itemStack) {
        boolean hadPot = this.getInventory().hasAnyMatching(itemStack1 -> itemStack1.is(PVZItemTags.WATERING_POTS));
        return itemStack.is(PVZItemTags.SNAILS_CAN_PICK_UP)
                && this.getInventory().canAddItem(itemStack)
                && ! (hadPot && itemStack.is(PVZItemTags.WATERING_POTS));
    }

    @Override
    public @Nullable AgeableMob getBreedOffspring(ServerLevel p_146743_, AgeableMob p_146744_) {
        return null;
    }

    @Override
    protected void playStepSound(BlockPos p_28301_, BlockState p_28302_) {
    }

    public static boolean checkSnailSpawnRules(EntityType<? extends Animal> p_218105_, LevelAccessor p_218106_, MobSpawnType p_218107_, BlockPos p_218108_, RandomSource p_218109_) {
        return p_218106_.getBlockState(p_218108_.below()).is(BlockTags.ANIMALS_SPAWNABLE_ON);
    }

    public static class SnailCollectItemsGoal extends Goal {
        final Snail snail;
        ItemEntity target;

        public SnailCollectItemsGoal(Snail snail) {
            this.snail = snail;
            target = null;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (snail.getAwakenTime() < 100) return false;
            if (! this.snail.isTame()) return false;
            if (EntityUtil.isEntityValid(target)) return true;
            target = null;
            if (! this.snail.navigation.isDone() || this.snail.random.nextInt(10) == 0) return false;
            List<ItemEntity> items = this.snail.level.getEntitiesOfClass(ItemEntity.class
                    , this.snail.getBoundingBox().inflate(this.snail.getAttributeValue(Attributes.FOLLOW_RANGE))
                    , item -> this.snail.wantsToPickUp(item.getItem()) && ! item.hasPickUpDelay());
            if (items.isEmpty()) {
                return false;
            } else {
                target = items.get(0);
                return true;
            }
        }

        @Override
        public void tick() {
            if (EntityUtil.isEntityValid(target) && this.snail.navigation.isDone()) {
                this.snail.navigation.moveTo(target, 1);
            }
            if (this.snail.distanceToSqr(target) < 1) {
                snail.pickUpItem(target);
                target = null;
            } else if (target.level.getNearestPlayer(target, 3) != null) {
                target = null;
            }
        }
    }

    public static class SnailFeedPlantsGoal extends Goal {
        final Snail snail;
        LivingEntity target;

        public SnailFeedPlantsGoal(Snail snail) {
            this.snail = snail;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (snail.getAwakenTime() < 100) return false;
            if (! this.snail.isTame()) return false;
            List<LivingEntity> plants = this.snail.level.getEntitiesOfClass(LivingEntity.class
                    , this.snail.getBoundingBox().inflate(this.snail.getAttributeValue(Attributes.FOLLOW_RANGE))
                    , livingEntity -> livingEntity instanceof IGardenPlant);
            for (int i = 0; i < 4; i ++) {
                ItemStack item = this.snail.getInventory().getItem(i);
                if (item.isEmpty()) continue;
                if (item.getItem() instanceof FertilizerItem) {
                    Optional<LivingEntity> opt = plants.stream().filter(entity ->
                        entity instanceof IGardenPlant iGardenPlant && iGardenPlant.isRequiringFertilizer()).findFirst();
                    if (opt.isPresent()) {
                        this.snail.setItemInHand(InteractionHand.MAIN_HAND, item);
                        target = opt.get();
                        return true;
                    }
                } else if (item.getItem() instanceof WateringPotItem) {
                    Optional<LivingEntity> opt = plants.stream().filter(entity ->
                            entity instanceof IGardenPlant iGardenPlant && iGardenPlant.isRequiringWater()).findFirst();
                    if (opt.isPresent()) {
                        this.snail.setItemInHand(InteractionHand.MAIN_HAND, item);
                        target = opt.get();
                        return true;
                    }
                }
            }
            for (int i = 0; i < 4; i ++) {
                ItemStack item = this.snail.getInventory().getItem(i);
                if (item.isEmpty()) continue;
                if (! (item.getItem() instanceof FertilizerItem || item.getItem() instanceof WateringPotItem)) {
                    Player player = this.snail.level.getNearestPlayer(snail, snail.getAttributeValue(Attributes.FOLLOW_RANGE));
                    if (EntityUtil.isEntityValid(player)) {
                        this.snail.setItemInHand(InteractionHand.MAIN_HAND, item);
                        target = player;
                        return true;
                    }
                }
            }
            return false;
        }

        @Override
        public void tick() {
            ItemStack itemStack = this.snail.getItemInHand(InteractionHand.MAIN_HAND);
            if (this.snail.navigation.isDone()) {
                this.snail.navigation.moveTo(target, 1);
            }
            if (this.snail.tickCount % 50 < 2 && this.snail.distanceToSqr(target) < 3) {
                if (itemStack.getItem() instanceof FertilizerItem item) {
                    item.fertilise(this.target, null, itemStack);
                    this.target = null;
                    this.snail.navigation.stop();
                    this.snail.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                } else if (itemStack.getItem() instanceof WateringPotItem item) {
                    item.water(this.target, null, itemStack);
                    this.target = null;
                    this.snail.navigation.stop();
                    this.snail.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                } else if (! itemStack.isEmpty()) {
                    for (int i = 0; i < this.snail.getInventory().getContainerSize(); i ++) {
                        if (this.snail.getInventory().getItem(i) == itemStack) {
                            BehaviorUtils.throwItem(this.snail, itemStack, target.position());
                            this.snail.getInventory().setItem(i, ItemStack.EMPTY);
                            this.snail.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                            break;
                        }
                    }
                }
            }
        }
    }

    public static class SnailRetreatIntoShellGoal extends Goal {
        final Snail snail;

        public SnailRetreatIntoShellGoal(Snail snail) {
            this.snail = snail;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (snail.getAwakenTime() < 20) return false; //prevent situations animation is not over.
            if (snail.isTame()) {
                if (this.snail.hasEffect(MobEffects.MOVEMENT_SPEED)) return false;
                return snail.random.nextInt(Math.max(1, snail.getAwakenTime() / 100)) > 100 && snail.random.nextInt(100) == 0;
            } else {
                return snail.random.nextInt(3000) == 0
                        || ! this.snail.level.getEntitiesOfClass(Player.class, this.snail.getBoundingBox().inflate(10, 10, 5)
                        , player -> (! player.isShiftKeyDown() || EntityUtil.isLeavingGround(player)) && ! player.isSpectator()).isEmpty();
            }
        }

        @Override
        public boolean canContinueToUse() {
            return snail.getPose() != Pose.STANDING && (snail.isTame() || snail.tickCount % 500 > 1) || canUse();
        }

        @Override
        public void tick() {
            snail.navigation.stop();
        }

        @Override
        public void start() {
            snail.retreatIntoShell();
        }

        @Override
        public void stop() {
            snail.emergeFromShell();
        }

    }


    public class SnailJumpControl extends JumpControl {

        public SnailJumpControl(Mob mob) {
            super(mob);
        }

        public void jump() {
        }
    }
}
