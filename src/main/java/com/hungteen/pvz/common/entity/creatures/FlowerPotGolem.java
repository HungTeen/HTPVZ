package com.hungteen.pvz.common.entity.creatures;

import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.api.interfaces.ICanBePlantedOn;
import com.hungteen.pvz.api.interfaces.ICanChargeSun;
import com.hungteen.pvz.api.interfaces.ICanUseSeedPack;
import com.hungteen.pvz.common.block.entity.ResinBeaconBlockEntity;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapStats;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.register.PVZAttributes;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

/**
 * @author PangTeen
 * @program HTPVZ
 * @create 2026/10/26 23:00
 **/
public class FlowerPotGolem extends PathfinderMob implements ICanUseSeedPack, ICanChargeSun, ICanBePlantedOn {

    private static final int PLANT_ATTEMPT_INTERVAL = 20;

    public final AnimationState idleAnimationState = new AnimationState();
    private final ItemCooldowns cooldowns = new ItemCooldowns();
    private int sunAmount;
    private int plantAttemptCooldown;

    public FlowerPotGolem(EntityType<? extends PathfinderMob> entityType, Level level) {
        super(entityType, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 20.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.22D)
                .add(Attributes.FOLLOW_RANGE, 16.0D)
                .add(PVZAttributes.MAX_SUN.get(), 200.0D);
    }

    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.7D));
        this.goalSelector.addGoal(2, new LookAtPlayerGoal(this, Player.class, 8.0F));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level.isClientSide) {
            if (this.isVehicle() && this.idleAnimationState.isStarted()) {
                this.idleAnimationState.stop();
            } else if (!this.isVehicle() && !this.idleAnimationState.isStarted()) {
                this.idleAnimationState.start(this.tickCount);
            }
            return;
        }

        this.cooldowns.tick();
        boolean inBeaconRange = ResinBeaconBlockEntity.isWithinActiveBeacon(this.level, this.position());
        if (this.isNoAi() == inBeaconRange) {
            this.setNoAi(!inBeaconRange);
        }
        if (!inBeaconRange) {
            this.stopAllActivities();
            return;
        }

        if (!this.isVehicle() && ++this.plantAttemptCooldown >= PLANT_ATTEMPT_INTERVAL) {
            this.plantAttemptCooldown = 0;
            this.tryPlantHeldSeedPacket();
        }
    }

    private void stopAllActivities() {
        this.getNavigation().stop();
        this.setTarget(null);
        this.setDeltaMovement(0.0D, Math.min(0.0D, this.getDeltaMovement().y), 0.0D);
    }

    private void tryPlantHeldSeedPacket() {
        ItemStack packetStack = this.getMainHandItem();
        if (!(packetStack.getItem() instanceof SeedPacketItem<?> packet)
                || this.cooldowns.isOnCooldown(packet)) {
            return;
        }
        packet.plantOnEntity(this, packetStack.copy(), this.level, this);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (player.isShiftKeyDown()) {
            if (!this.level.isClientSide) {
                ItemStack golemStack = this.createPickupStack();
                if (!player.getInventory().add(golemStack)) {
                    player.drop(golemStack, false);
                }
                this.discard();
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }

        ItemStack playerStack = player.getItemInHand(hand);
        ItemStack heldPacket = this.getMainHandItem();
        if (playerStack.isEmpty() && !heldPacket.isEmpty()) {
            if (!this.level.isClientSide) {
                player.setItemInHand(hand, heldPacket.copy());
                this.setItemInHand(InteractionHand.MAIN_HAND, ItemStack.EMPTY);
                this.level.playSound(null, this, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.0F);
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }

        if (playerStack.getItem() instanceof SeedPacketItem<?> && heldPacket.isEmpty()) {
            if (!this.level.isClientSide) {
                ItemStack givenPacket = playerStack.copy();
                givenPacket.setCount(1);
                this.setItemInHand(InteractionHand.MAIN_HAND, givenPacket);
                if (!player.getAbilities().instabuild) {
                    playerStack.shrink(1);
                }
                this.ensureOwner(player);
                this.level.playSound(null, this, SoundEvents.ITEM_PICKUP, SoundSource.PLAYERS, 0.8F, 1.2F);
            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        }

        return super.mobInteract(player, hand);
    }

    private void ensureOwner(Player player) {
        if (!PVZEntityCapability.hasOwner(this)) {
            this.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.setOwner(player));
        }
    }

    private ItemStack createPickupStack() {
        ItemStack stack = new ItemStack(PVZItems.FLOWER_POT_GOLEM_SPAWN_EGG.get());
        CompoundTag entityTag = new CompoundTag();
        this.saveWithoutId(entityTag);
        entityTag.putString("id", EntityType.getKey(this.getType()).toString());
        stack.getOrCreateTag().put("EntityTag", entityTag);
        return stack;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty,
                                        MobSpawnType spawnType, @Nullable SpawnGroupData spawnData,
                                        @Nullable CompoundTag dataTag) {
        SpawnGroupData result = super.finalizeSpawn(level, difficulty, spawnType, spawnData, dataTag);
        this.setPersistenceRequired();
        return result;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (!this.level.isClientSide
                && !ResinBeaconBlockEntity.isWithinActiveBeacon(this.level,
                this.position().add(travelVector.x, 0.0D, travelVector.z))) {
            travelVector = new Vec3(0.0D, travelVector.y, 0.0D);
            this.getNavigation().stop();
        }
        super.travel(travelVector);
    }

    @Override
    public double getPassengersRidingOffset() {
        return this.getBbHeight();
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("SunAmount", this.sunAmount);
        tag.putInt("PlantAttemptCooldown", this.plantAttemptCooldown);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        this.setAmount(tag.getInt("SunAmount"));
        this.plantAttemptCooldown = tag.getInt("PlantAttemptCooldown");
    }

    @Override
    public void setAmount(int amount) {
        this.sunAmount = Math.max(0, Math.min(this.getCapacity(), amount));
    }

    @Override
    public int getAmount() {
        return this.sunAmount;
    }

    @Override
    public int getCapacity() {
        return this.getAttribute(PVZAttributes.MAX_SUN.get()) == null
                ? 200
                : (int) this.getAttributeValue(PVZAttributes.MAX_SUN.get());
    }

    @Override
    public ItemCooldowns getCooldowns() {
        return this.cooldowns;
    }

    @Override
    public FlowerPotGolem getEntity() {
        return this;
    }

    @Override
    public MutableComponent prePlantOnBlock(PVZResourceEvent.CheckPlantConditionEvent event,
                                            net.minecraft.world.entity.Entity entity, ItemStack plantCard,
                                            Level level, net.minecraft.core.BlockPos pos,
                                            net.minecraft.core.Direction side) {
        return this.checkPlantResource(event);
    }

    @Override
    public MutableComponent prePlantOnEntity(PVZResourceEvent.CheckPlantConditionEvent event,
                                             net.minecraft.world.entity.Entity entity, ItemStack plantCard,
                                             net.minecraft.world.entity.Entity target) {
        return this.checkPlantResource(event);
    }

    @Nullable
    private MutableComponent checkPlantResource(PVZResourceEvent.CheckPlantConditionEvent event) {
        if (!event.resource.equals(PVZPlayerCapStats.SUN) || event.cost > this.getAmount()) {
            return Component.translatable("hint.pvz.plant.no_enough_resource",
                    Component.translatable(event.resource));
        }
        return null;
    }

    @Override
    public void postPlantOnBlock(PVZResourceEvent.CheckPlantConditionEvent event,
                                 net.minecraft.world.entity.Entity entity, ItemStack plantCard,
                                 @Nullable MutableComponent result, Level level,
                                 net.minecraft.core.BlockPos pos, net.minecraft.core.Direction side) {
        this.finishPlanting(event, plantCard, result);
    }

    @Override
    public void postPlantOnEntity(PVZResourceEvent.CheckPlantConditionEvent event,
                                  net.minecraft.world.entity.Entity entity, ItemStack plantCard,
                                  @Nullable MutableComponent result,
                                  net.minecraft.world.entity.Entity target) {
        this.finishPlanting(event, plantCard, result);
    }

    private void finishPlanting(PVZResourceEvent.CheckPlantConditionEvent event, ItemStack plantCard,
                                @Nullable MutableComponent result) {
        if (result != null) {
            return;
        }
        this.setAmount(this.getAmount() - event.cost);
        if (event.coolDown > 0 && plantCard.getItem() instanceof SeedPacketItem<?> packet) {
            this.cooldowns.addCooldown(packet, event.coolDown);
        }
    }
}
