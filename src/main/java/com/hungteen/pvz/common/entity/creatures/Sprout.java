package com.hungteen.pvz.common.entity.creatures;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.api.events.GardenPlantGrowUpEvent;
import com.hungteen.pvz.api.interfaces.IGardenPlant;
import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.plants.MariGold;
import com.hungteen.pvz.common.event.SproutTransformEvent;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Sprout extends Mob implements IGardenPlant {
    @OnlyIn(Dist.CLIENT)
    public LivingEntity plant;
    public Map<String, Integer> transformChance = new HashMap<>();
    private static final EntityDataAccessor<Boolean> IS_MARIGOLD = SynchedEntityData.defineId(Sprout.class, EntityDataSerializers.BOOLEAN);
    public static final EntityDataAccessor<Boolean> REQUIRES_WATER = SynchedEntityData.defineId(Sprout.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> GROW_ENDED = SynchedEntityData.defineId(Sprout.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Integer> GROW_LEVEL = SynchedEntityData.defineId(Sprout.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<String> PLANT_NAME = SynchedEntityData.defineId(Sprout.class, EntityDataSerializers.STRING);
    public long growEndTime = -1;
    public Sprout(EntityType<? extends Mob> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
        this.entityData.set(DATA_POSE, Pose.DIGGING);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 8.0D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1)
                .add(Attributes.MOVEMENT_SPEED, 0);
    }

    @Override
    public void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IS_MARIGOLD, false);
        this.entityData.define(REQUIRES_WATER, true);
        this.entityData.define(GROW_ENDED, false);
        this.entityData.define(GROW_LEVEL, 0);
        this.entityData.define(PLANT_NAME, "");
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
    }
    @Override
    public EntityDimensions getDimensions(Pose pose) {
        return pose == Pose.DIGGING || pose == Pose.DYING ? this.getType().getDimensions() :
                pose == Pose.SWIMMING ? EntityDimensions.scalable(0.5F, 0.7F) :
                        EntityDimensions.scalable(0.6F, 1F);
    }

    @Override
    public boolean isPushable(){
        return false;
    }
    public void setMarigold(boolean isMarigold) {
        this.entityData.set(IS_MARIGOLD, isMarigold);
    }
    public boolean isMarigold() {
        return this.entityData.get(IS_MARIGOLD);
    }

    /**Check {@link IGardenPlant} for the overriding methods below.*/
    @Override
    public void setRequiringWater(boolean bool) {
        this.entityData.set(REQUIRES_WATER, bool);
    }
    @Override
    public boolean isRequiringWater() {
        return this.entityData.get(REQUIRES_WATER) && isRequiring() && this.getGrowLevel() < 2;
    }
    @Override
    public void setRequiringFertilizer(boolean bool) {
        this.entityData.set(REQUIRES_WATER, !bool);
    }
    @Override
    public boolean isRequiringFertilizer() {
        return (! this.entityData.get(REQUIRES_WATER)) && isRequiring() && this.getGrowLevel() < 2;
    }
    protected boolean isRequiring() {
        return this.entityData.get(GROW_ENDED);
    }
    protected void setRequiring(boolean bool) {
        this.entityData.set(GROW_ENDED, bool);
    }
    @Override
    public int getGrowLevel() {
        return this.entityData.get(GROW_LEVEL);
    }
    @Override
    public int getMaxLevel() {
        return 2;
    }
    @Override
    public void setGrowLevel(int level) {
        this.entityData.set(GROW_LEVEL, level);
        this.entityData.set(DATA_POSE, level == 0 ? Pose.DIGGING : (level == 1 ? Pose.SWIMMING : Pose.STANDING));
    }
    @Override
    public int getRemainingGrowTick() {
        return (this.getLevel().isClientSide || this.growEndTime == -1) ? 0 : (int) (this.growEndTime - this.getLevel().getGameTime());
    }
    @Override
    public void setRemainingGrowTick(int time) {
        this.growEndTime = this.getLevel().isClientSide ? -1L : this.getLevel().getGameTime() + time;
    }

    @Override
    public void tick() {
        super.tick();
        if (! level.isClientSide) {
            if (this.getRemainingGrowTick() <= 0) {
                setRequiring(true);
            }
            if (this.tickCount % 10 == 0 && ! level.getBlockState(this.getOnPos()).is(PVZBlockTags.GARDEN_FLOWER_POT)) {
                this.hurt(PVZDamageSource.PLANT_WILT, (float) (0.2 * this.getAttribute(Attributes.MAX_HEALTH).getValue()));
            }
        } else {
            if (! entityData.get(PLANT_NAME).equals("") && this.plant == null) {
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityData.get(PLANT_NAME)));
                if (type != null && type.canSummon()) {
                    Entity entity = type.create(level);
                    if (entity instanceof LivingEntity entity1) {
                        this.plant = entity1;
                        entity.moveTo(this.position());
                        if (entity instanceof IPlant plant) {
                            plant.setupPresentationAnim();
                        }
                    }
                }
            } else if (getGrowLevel() >= 2 && this.getRemainingGrowTick() <= 0 ) {
                this.level.addParticle(ParticleTypes.ELECTRIC_SPARK,
                        getX() + random.nextFloat() * 0.8 - 0.4, getY() + random.nextFloat() * 0.5, getZ() + random.nextFloat() * 0.8 - 0.4,
                        0, random.nextFloat(), 0);
            }
            if (plant != null) {
                plant.tick();
                plant.moveTo(this.position());
                plant.tickCount ++;
                plant.hurtTime = this.hurtTime;
                plant.deathTime = this.deathTime;
            }
        }
    }

    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (getGrowLevel() >= 2 && this.getRemainingGrowTick() <= 0) {
            produce();
            this.discard();
            return InteractionResult.CONSUME;
        }
        return super.mobInteract(player, hand);
    }

    public void renewWaterPot() {
        boolean water = false;
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityData.get(PLANT_NAME)));
        if (type != null) {
            Entity entity = type.create(this.level);
            if (entity instanceof IPlant iplant) {
                entity.discard();
                water = iplant.needWaterPotInGarden();
            } else {
                entity.discard();
            }
        }
        BlockState blockState = level.getBlockState(this.getOnPos());
        if (blockState.hasProperty(IGardenPlant.WATER)) {
            level.setBlock(this.getOnPos(), blockState.setValue(IGardenPlant.WATER, water), 3);
        }
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
    @Override
    public InteractionResult onFertilized(Player player, ItemStack stack) {
        if (level.isClientSide) return InteractionResult.SUCCESS;
        if (this.isRequiringFertilizer()) {
            GardenPlantGrowUpEvent event = new GardenPlantGrowUpEvent(this, this.getGrowLevel() == 0);
            MinecraftForge.EVENT_BUS.post(event);
            if (event.shouldApplyEffects) {
                this.transformPlant();
            }
            if (! event.isCanceled()) {
                setGrowLevel(this.getGrowLevel() + 1);
            }
            this.setRemainingGrowTick(PVZConfig.PVZGameRules.getInt(this.level, PVZConfig.Common.sproutGrowTime));
            this.setRequiringWater(true);
            this.setRequiring(false);
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.FAIL;
        }
    }

    public void produce() {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(entityData.get(PLANT_NAME)));
        if (type != null) {
            Entity entity = type.create(level);
            if (entity != null) {
                ItemStack stack = entity.getPickResult();
                int num = this.random.nextInt(2) + 2;
                for (int i = 0; i < num; i ++) {
                    ItemEntity itementity = new ItemEntity(this.level, this.getX(), this.getEyeY(), this.getZ(), stack);
                    BlockPos pos = blockPosition();
                    itementity.setPos(pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5);
                    level.addFreshEntity(itementity);
                }
            }
        }
    }
    private void transformPlant() {
        String result = this.entityData.get(PLANT_NAME);
        if (result.isEmpty()) {
            if (this.isMarigold()) {
                result = "pvz:marigold";
            } else {
                int allChance = 1;
                for (int i: transformChance.values()) {
                    allChance += i;
                }
                int chosen = random.nextInt(allChance);
                for (String name: transformChance.keySet()) {
                    chosen -= transformChance.get(name) > 0 ? transformChance.get(name) : 0;
                    if (chosen <= 0) {
                        result = name;
                        break;
                    }
                }
                if (result.equals("")) {
                    result = "pvz:marigold";
                }
            }
        }
        SproutTransformEvent event = new SproutTransformEvent(this, result);
        MinecraftForge.EVENT_BUS.post(event);
        result = event.name;
        this.entityData.set(PLANT_NAME, result);
        if (this.entityData.get(PLANT_NAME).equals("pvz:marigold")) {
            MariGold marigold = this.convertTo(PVZEntities.MARIGOLD.get(), true);
            if (marigold != null) {
                final UUID[] owner = new UUID[1];
                this.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> owner[0] = cap.getOwnerUuid());
                marigold.getCapability(PVZEntityCapability.CAP).ifPresent((cap) -> cap.setOwner(owner[0]));
            }
        }
        renewWaterPot();
    }

    //settings

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putBoolean("isMarigold", this.isMarigold());
        tag.putBoolean("requiresWater", this.isRequiringWater());
        tag.putInt("growLevel", this.getGrowLevel());
        tag.putLong("growEndTime", this.growEndTime);
        if (! this.entityData.get(PLANT_NAME).equals("")) {
            tag.putString("plantName", this.entityData.get(PLANT_NAME));
        } else {
            CompoundTag mapTag = new CompoundTag();
            for (String name : this.transformChance.keySet()) {
                mapTag.putInt(name, this.transformChance.get(name));
            }
            tag.put("transformChance", mapTag);
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        this.setMarigold(tag.getBoolean("isMarigold"));
        this.setGrowLevel(tag.getInt("growLevel"));
        this.setRequiringWater(tag.getBoolean("requiresWater"));
        this.growEndTime = tag.getLong("growEndTime");
        if (tag.contains("transformChance")) {
            CompoundTag map = (CompoundTag) tag.get("transformChance");
            for (String name : map.getAllKeys()) {
                this.transformChance.put(name, map.getInt(name));
            }
        } else {
            this.entityData.set(PLANT_NAME, tag.getString("plantName"));
        }
    }
    @Override
    public boolean removeWhenFarAway(double p_27598_) {
        return false;
    }
    @Override
    public boolean canBeLeashed(Player player) {
        return false;
    }
    @Override
    protected void pushEntities() {
    }
}
