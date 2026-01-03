package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.interfaces.ICanGroupUp;
import com.hungteen.pvz.api.interfaces.IHangable;
import com.hungteen.pvz.common.entity.ai.goal.BlockWithShieldGoal;
import com.hungteen.pvz.common.entity.ai.goal.FollowGroupLeaderGoal;
import com.hungteen.pvz.common.entity.ai.goal.GroupShareEnemyGoal;
import com.hungteen.pvz.common.register.PVZBannerPatterns;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.EntityUtil;
import com.mojang.serialization.Dynamic;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MoveThroughVillageGoal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.IronGolem;
import net.minecraft.world.entity.animal.Turtle;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.monster.ZombifiedPiglin;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BannerPattern;
import net.minecraft.world.level.block.entity.BannerPatterns;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Consumer;

/**Basic class for pvz zombies. Pose.LONG_JUMPING is regarded tied and hanged under something here.*/
public class PVZZombie extends Zombie implements ICanGroupUp, IHangable {
    public static final EntityDataAccessor<String> SKIN = SynchedEntityData.defineId(PVZZombie.class, EntityDataSerializers.STRING);
    /**
     * The entity the zombie ties itself on.
     */
    private static final EntityDataAccessor<Optional<UUID>> TIED_ENTITY = SynchedEntityData.defineId(PVZZombie.class, EntityDataSerializers.OPTIONAL_UUID);
    /**
     * The position the zombie ties itself on.
     */
    private static final EntityDataAccessor<Optional<BlockPos>> TIED_POSITION = SynchedEntityData.defineId(PVZZombie.class, EntityDataSerializers.OPTIONAL_BLOCK_POS);

    public double ropeLengthSqr = 25;
    public boolean renderHand = true; // controlled by renderer.
    public boolean renderHead = true; // controlled by renderer.
    protected ZombieAttackGoal attackGoal;
    protected RandomStrollGoal randomStrollGoal;
    public static Consumer<Entity> CONEHEAD_ZOMBIE_CONSUMER = (entity) -> entity.setItemSlot(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance());
    public static Consumer<Entity> BUCKET_ZOMBIE_CONSUMER = (entity) -> entity.setItemSlot(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance());
    public static Consumer<Entity> DUCK_LIFEBUOY_ZOMBIE_CONSUMER = (entity) -> entity.setItemSlot(EquipmentSlot.LEGS, PVZItems.DUCK_LIFEBUOY.get().getDefaultInstance());
    public static Consumer<Entity> SCREEN_DOOR_CONSUMER = (entity) -> entity.setItemSlot(EquipmentSlot.MAINHAND, PVZItems.SCREEN_DOOR_SHIELD.get().getDefaultInstance());
    public static Consumer<Entity> OVERWORLD_FLAG_ZOMBIE_CONSUMER = (entity) -> {
        entity.setItemSlot(EquipmentSlot.HEAD, getOverworldBanner());
        entity.getEntityData().set(SKIN, "minecraft_overworld");
    };
    public static Consumer<Entity> NETHER_FLAG_ZOMBIE_CONSUMER = (entity) -> {
        entity.setItemSlot(EquipmentSlot.HEAD, getNetherBanner());
        entity.getEntityData().set(SKIN, "minecraft_the_nether");
    };
    public static Consumer<Entity> END_FLAG_ZOMBIE_CONSUMER = (entity) -> {
        entity.setItemSlot(EquipmentSlot.HEAD, getEndBanner());
        entity.getEntityData().set(SKIN, "minecraft_the_end");
    };
    public static UUID GROUP_UP_MODIFIER = UUID.fromString("772807aa-672f-bfda-7d21-0f66823f6d53");
    public PVZZombie(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
    }

    //methods
    public boolean shouldDropHand() {
        return this.getHealth() < this.getAttributeBaseValue(Attributes.MAX_HEALTH) / 2;
    }

    public boolean shouldDropHead() {
        return this.getHealth() <= 0;
    }

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType spawnType, @Nullable SpawnGroupData data, @Nullable CompoundTag tag) {
        if (data instanceof Zombie.ZombieGroupData zombie$zombiegroupdata) {
            zombie$zombiegroupdata.canSpawnJockey = false;
            zombie$zombiegroupdata.isBaby = false;
        }
        SpawnGroupData spawnGroupData = super.finalizeSpawn(level, difficulty, spawnType, data, tag);
        return spawnGroupData;
    }

    public static boolean checkSpawnRules(EntityType<PVZZombie> p_219014_, ServerLevelAccessor p_219015_, MobSpawnType p_219016_, BlockPos p_219017_, RandomSource p_219018_) {
        return p_219015_.getDifficulty() != Difficulty.PEACEFUL && ! p_219015_.getBlockState(p_219017_.below()).getFluidState().is(FluidTags.LAVA);
    }
    //configs
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        ResourceLocation res = this.level.dimension().location();
        entityData.define(SKIN, res.getNamespace() + "_" + res.getPath());
        this.entityData.define(TIED_POSITION, Optional.empty());
        this.entityData.define(TIED_ENTITY, Optional.empty());
    }

    @Override
    protected void addBehaviourGoals() {
        attackGoal = new ZombieAttackGoal(this, 1.0D, false);
        randomStrollGoal = new RandomStrollGoal(this, 1.0D);
        this.goalSelector.addGoal(1, new BlockWithShieldGoal(this));
        this.goalSelector.addGoal(2, attackGoal);
        this.goalSelector.addGoal(3, new FollowGroupLeaderGoal(this));
        this.goalSelector.addGoal(6, new MoveThroughVillageGoal(this, 1.0D, true, 4, this::canBreakDoors));
        this.goalSelector.addGoal(7, randomStrollGoal);
        this.targetSelector.addGoal(1, new GroupShareEnemyGoal(this));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this)).setAlertOthers(ZombifiedPiglin.class));
        this.targetSelector.addGoal(2, new NearestAttackableTargetGoal<>(this, Player.class, true));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, AbstractVillager.class, false));
        this.targetSelector.addGoal(3, new NearestAttackableTargetGoal<>(this, IronGolem.class, true));
        this.targetSelector.addGoal(5, new NearestAttackableTargetGoal<>(this, Turtle.class, 10, true, false, Turtle.BABY_ON_LAND_SELECTOR));
    }

    @Override
    protected boolean isSunSensitive() {
        return false;
    }
    @Override
    protected boolean convertsInWater() {
        return false;
    }
    @Override
    protected ItemStack getSkull() {
        return ItemStack.EMPTY;
    }
    @Override
    public int getExperienceReward() {
        return 0;
    }

    @Override
    public boolean isHanging() {
        return this.getHangingPosition() != null || this.getHangingEntity() != null;
    }
    @Override
    public @Nullable Entity getHangingEntity() {
        Optional<UUID> opt = this.entityData.get(TIED_ENTITY);
        return opt.map(value -> this.level.isClientSide ? ((ClientLevel) this.level).getEntities().get(value) :
                ((ServerLevel) this.level).getEntity(value)).orElse(null);
    }

    @Override
    public boolean hangableToEntity(Entity entity) {
        return EntityUtil.isEntityValid(entity);
    }
    @Override
    public boolean hangableToBlockPos(BlockPos pos) {
        return ! level.getBlockState(pos).isAir();
    }
    @Override
    public BlockPos getHangingPosition() {
        Optional<BlockPos> optional = this.entityData.get(TIED_POSITION);
        return optional.orElse(null);
    }
    @Override
    public void setHangingPosition(BlockPos pos) {
        this.entityData.set(TIED_POSITION, Optional.ofNullable(pos));
    }
    @Override
    public void setHangingEntity(@Nullable Entity entity) {
        this.entityData.set(TIED_ENTITY, entity == null ? Optional.empty() : Optional.of(entity.getUUID()));
    }

    public boolean needHangingPose() {
        return this.isHanging() && ! this.isPassenger() && EntityUtil.isLeavingGround(this);
    }
    @Override
    public void setRopeLengthSqr(double lengthSqr) {
        this.ropeLengthSqr = lengthSqr;
    }
    @Override
    public double getRopeLengthSqr() {
        return this.ropeLengthSqr;
    }


    @Override
    public EntityDimensions getDimensions(Pose pose) {
        if (pose == Pose.LONG_JUMPING) {
            EntityDimensions dimensions = super.getDimensions(pose);
            return new EntityDimensions(dimensions.width, 1.5F, dimensions.fixed);
        } else if (pose == Pose.SWIMMING) {
            EntityDimensions dimensions = super.getDimensions(pose);
            return new EntityDimensions(dimensions.width, 0.6F, dimensions.fixed);
        }
        return super.getDimensions(pose);
    }

    protected float getStandingEyeHeight(Pose pose, EntityDimensions dimensions) {
        return switch (pose) {
            case SWIMMING, FALL_FLYING, SPIN_ATTACK -> 0.5F;
            case CROUCHING -> this.isBaby() ? 0.75F : 1.37F;
            case LONG_JUMPING -> this.isBaby() ? 0.65F : 1.24F;
            default -> this.isBaby() ? 0.93F : 1.74F;
        };
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("style_path", this.entityData.get(SKIN));
        tag.putDouble("hanging_rope_length", this.ropeLengthSqr);
        Optional<BlockPos> optBlockPos = this.entityData.get(TIED_POSITION);
        optBlockPos.flatMap(blockPos -> BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, blockPos).resultOrPartial(PVZMod.LOGGER::error))
                .ifPresent((pos) -> tag.put("hanging_pos", pos));
        Optional<UUID> optUUID = this.entityData.get(TIED_ENTITY);
        optUUID.ifPresent(uuid -> tag.putUUID("hanging_entity_id", uuid));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("style_path")) {
            this.entityData.set(SKIN, tag.getString("style_path"));
        }
        if (tag.contains("hanging_pos")) {
            BlockPos.CODEC.parse(new Dynamic<>(NbtOps.INSTANCE, tag.getCompound("hanging_pos"))).resultOrPartial(PVZMod.LOGGER::error)
                    .ifPresent((pos) -> this.entityData.set(TIED_POSITION, Optional.of(pos)));
        }
        if (tag.contains("hanging_entity_id")) {
            this.entityData.set(TIED_ENTITY, Optional.of(tag.getUUID("hanging_entity_id")));
        }
        if (tag.contains("hanging_rope_length")) {
            this.ropeLengthSqr = tag.getDouble("hanging_rope_length");
        }
    }
    public String getStyle() {
        return this.entityData.get(SKIN);
    }

    @Override
    public void tick() {
        super.tick();
        if (! level.isClientSide) {
            if (this.isHanging()) {
                this.fallDistance = 0;
            }
            if (this.needHangingPose()) {
                this.setPose(Pose.LONG_JUMPING);
            } else if (this.getPose() == Pose.LONG_JUMPING) {
                this.setPose(Pose.STANDING);
            }
            if (this.isHanging()) {
                double actualLengthSqr = 0;
                Vec3 direction = null;
                BlockPos hangingPos = this.getHangingPosition();
                if (this.getHangingPosition() != null) {
                    if (hangingPos != null && ! hangableToBlockPos(hangingPos)) {
                        this.setHangingPosition(null);
                    } else {
                        actualLengthSqr = this.blockPosition().offset(0, Math.ceil(this.getBbHeight()),0).distSqr(this.getHangingPosition());
                        direction = Vec3.atBottomCenterOf(this.getHangingPosition()).subtract(this.position());
                    }
                } else {
                    Entity hangingEntity = this.getHangingEntity();
                    if (hangingEntity != null) {
                        if (! hangableToEntity(hangingEntity)) {
                            this.setHangingEntity(null);
                        } else {
                            actualLengthSqr = this.blockPosition().offset(0, Math.ceil(this.getBbHeight()),0).distSqr(this.getHangingEntity().blockPosition());
                            direction = this.getHangingEntity().position().subtract(this.position());
                        }
                    }
                }
                if (direction != null && actualLengthSqr > this.ropeLengthSqr) {
                    double stretched = Math.min(0.5, (actualLengthSqr - this.ropeLengthSqr) / 10);
                    direction = direction.normalize().multiply(stretched, stretched, stretched);
                    this.setDeltaMovement(this.getDeltaMovement().add(direction));
                    if (actualLengthSqr > Math.max(16, 4 * ropeLengthSqr)) {
                        this.setHangingPosition(null);
                    }
                }
            }
        }
        if (! EntityUtil.isEntityValid(this.getVehicle())) {
            this.stopRiding();
        }
        AttributeInstance instance = this.getAttribute(Attributes.FOLLOW_RANGE);
        if (this.schoolSize > 1) {
            if (instance.getModifier(GROUP_UP_MODIFIER) == null) {
                instance.addTransientModifier(new AttributeModifier(GROUP_UP_MODIFIER, "group_up_modifier", -16, AttributeModifier.Operation.ADDITION));
            }
        } else {
            instance.removeModifier(GROUP_UP_MODIFIER);
        }
    }


    //utils
    public static ItemStack getOverworldBanner() {
        final ItemStack itemstack = new ItemStack(Items.RED_BANNER);
        final CompoundTag tag = new CompoundTag();
        ListTag listTag = (new BannerPattern.Builder())
                .addPattern(BannerPatterns.BORDER, DyeColor.BLUE)
                .addPattern(BannerPatterns.TRIANGLES_TOP, DyeColor.WHITE)
                .addPattern(BannerPatterns.TRIANGLES_BOTTOM, DyeColor.WHITE)
                .addPattern(PVZBannerPatterns.BRAIN.getKey(), DyeColor.WHITE)
                .toListTag();
        tag.put("Patterns", listTag);
        BlockItem.setBlockEntityData(itemstack, BlockEntityType.BANNER, tag);
        itemstack.hideTooltipPart(ItemStack.TooltipPart.ADDITIONAL);
        itemstack.setHoverName((Component.translatable("block.pvz.brain_banner")));
        return itemstack;
    }
    public static ItemStack getNetherBanner() {
        final ItemStack itemstack = new ItemStack(Items.RED_BANNER);
        final CompoundTag tag = new CompoundTag();
        ListTag listTag = (new BannerPattern.Builder())
                .addPattern(BannerPatterns.GRADIENT, DyeColor.ORANGE)
                .addPattern(BannerPatterns.BRICKS, DyeColor.BROWN)
                .addPattern(BannerPatterns.CIRCLE_MIDDLE, DyeColor.ORANGE)
                .addPattern(BannerPatterns.BORDER, DyeColor.ORANGE)
                .addPattern(PVZBannerPatterns.BRAIN.getKey(), DyeColor.WHITE)
                .toListTag();
        tag.put("Patterns", listTag);
        BlockItem.setBlockEntityData(itemstack, BlockEntityType.BANNER, tag);
        itemstack.hideTooltipPart(ItemStack.TooltipPart.ADDITIONAL);
        itemstack.setHoverName((Component.translatable("block.pvz.brain_banner")));
        return itemstack;
    }
    public static ItemStack getEndBanner() {
        final ItemStack itemstack = new ItemStack(Items.PURPLE_BANNER);
        final CompoundTag tag = new CompoundTag();
        ListTag listTag = (new BannerPattern.Builder())
                .addPattern(BannerPatterns.TRIANGLE_TOP, DyeColor.BLACK)
                .addPattern(BannerPatterns.TRIANGLE_BOTTOM, DyeColor.BLACK)
                .addPattern(PVZBannerPatterns.BRAIN.getKey(), DyeColor.WHITE)
                .toListTag();
        tag.put("Patterns", listTag);
        BlockItem.setBlockEntityData(itemstack, BlockEntityType.BANNER, tag);
        itemstack.hideTooltipPart(ItemStack.TooltipPart.ADDITIONAL);
        itemstack.setHoverName((Component.translatable("block.pvz.brain_banner")));
        return itemstack;
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
        return this.getItemBySlot(EquipmentSlot.HEAD).getItem() instanceof BannerItem ? 20 : 5;
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
    public int getGroupRangeSqr() {
        return 8;
    }


}
