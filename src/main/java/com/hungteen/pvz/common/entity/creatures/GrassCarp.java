package com.hungteen.pvz.common.entity.creatures;

import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.animal.AbstractFish;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.IForgeShearable;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.hungteen.pvz.common.network.SpawnParticlePacket.particle;
import static java.lang.Math.abs;
import static java.lang.Math.signum;
import static net.minecraft.world.level.biome.Biomes.LUSH_CAVES;

public class GrassCarp extends AbstractFish implements IForgeShearable {

    private static final EntityDataAccessor<Boolean> BALD = SynchedEntityData.defineId(GrassCarp.class, EntityDataSerializers.BOOLEAN);
    private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(GrassCarp.class, EntityDataSerializers.BOOLEAN);
    private int nextCheckLeft = this.getNextCheckTime();
    private int growHairTick = 0;
    public static Map<Item, EntityType<? extends Mob>> fishMap = Map.of(Items.COD, EntityType.COD, Items.TROPICAL_FISH, EntityType.TROPICAL_FISH,
            Items.SALMON, EntityType.SALMON, Items.PUFFERFISH, EntityType.PUFFERFISH);


    //basic
    public GrassCarp(EntityType<? extends AbstractFish> entityType, Level level) {
        super(entityType, level);
        this.refreshDimensions();
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(BALD, false);
        this.entityData.define(FROM_BUCKET, false);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 6.0D)
                .add(Attributes.MOVEMENT_SPEED, 0.8D);
    }

    public static boolean checkGrassCarpSpawnRules(EntityType<? extends LivingEntity> entityType, ServerLevelAccessor level, MobSpawnType mobSpawnType, BlockPos pos, RandomSource random) {
        return level.getBiome(pos).is(LUSH_CAVES) && level.getBlockState(pos.below()).is(Blocks.CLAY) && level.getBlockState(pos.above()).is(Blocks.AIR);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("GrowHairTick", this.growHairTick);
        tag.putInt("NextCheckLeft", this.nextCheckLeft);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("GrowHairTick")) {
            this.growHairTick = tag.getInt("GrowHairTick");
            this.setBald(growHairTick > 0);
        }
        if (tag.contains("NextCheckLeft")) {
            this.nextCheckLeft = tag.getInt("NextCheckLeft");
        }
    }


    //AI
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new CollectItemsGoal(this));
        this.goalSelector.addGoal(6, new LookAtPlayerGoal(this, Player.class, 6.0F));
        this.goalSelector.addGoal(7, new RandomLookAroundGoal(this));
    }

    @Override
    public void tick() {
        super.tick();
        if (!this.level.isClientSide) {
            //grow hair.
            if (this.isBald() && --this.growHairTick <= 0) {
                this.setBald(false);
            }
            //check item stack or blocks.
            check();
        } else {
            if (random.nextInt(10) == 0) {
                Vec3 pos = this.position();
                level.addParticle(ParticleTypes.COMPOSTER.getType(), pos.x + random.nextFloat() * 0.6 - 0.3, pos.y + random.nextFloat() * 1.0 - 0.3, pos.z + random.nextFloat() * 0.6 - 0.3, 0, 0, 0);
            }
        }
    }

    public void dropItem() {
        ItemEntity itementity = new ItemEntity(this.level, this.getX(), this.getEyeY(), this.getZ(), getItemBySlot(EquipmentSlot.MAINHAND));
        itementity.setPickUpDelay(40);
        itementity.setThrower(this.getUUID());
        final Vec3 speed = this.getDeltaMovement().add(0, 1.2, 0).normalize();
        itementity.setDeltaMovement(speed.scale(0.2F));
        this.level.addFreshEntity(itementity);
        setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
    }


    // check item stack or blocks.
    private void check() {
        //holding item particle.
        if (!getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && nextCheckLeft < 400) {
            if (random.nextInt(400) > nextCheckLeft) {
                particle(this.level, ParticleTypes.COMPOSTER, this.position().add(random.nextFloat() * 1.5 - 0.75, random.nextFloat() * 1.5, random.nextFloat() * 1.5 - 0.75));
                particle(this.level, ParticleTypes.COMPOSTER, this.position().add(random.nextFloat() * 1.5 - 0.75, random.nextFloat() * 1.5, random.nextFloat() * 1.5 - 0.75));
            }
        }
        if (--this.nextCheckLeft <= 0) {
            final ItemStack item = getItemBySlot(EquipmentSlot.MAINHAND);
            if (!item.isEmpty()) { //check item stack.
                if (item.getItem() == Items.KELP && this.isBald()) {
                    this.setBald(false);
                    item.shrink(1);
                    nextCheckLeft = 0;
                }
                if (item.getItem() == Items.BONE_MEAL) {
                    if (this.isBald()) {
                        this.setBald(false);
                    }
                    item.shrink(1);
                    nextCheckLeft = 0;
                } else {
                    boolean useItem = false;
                    for (Item i : fishMap.keySet()) {
                        if (item.getItem() == i) {
                            Mob fish = fishMap.get(i).create(this.level);
                            fish.moveTo(this.position());
                            final Vec3 speed = this.getDeltaMovement().add(0, 1.2, 0).normalize();
                            fish.setDeltaMovement(speed.scale(0.5F));
                            fish.finalizeSpawn((ServerLevelAccessor) this.level, this.level.getCurrentDifficultyAt(new BlockPos(this.position())), MobSpawnType.MOB_SUMMONED, null, null);
                            ((ServerLevelAccessor) level).addFreshEntityWithPassengers(fish);
                            useItem = true;
                            item.shrink(1);
                            nextCheckLeft = 0;
                            break;
                        }
                    }
                    if (!useItem) {
                        dropItem();
                    }
                }
            } else { //check blocks.
                int num = 3 + random.nextInt(5);
                for (int i = 0; i < num; i++) {
                    int x = -5 + random.nextInt(11);
                    int y = (random.nextBoolean() ? 1 : -1) * random.nextInt(8 - abs(x));
                    x -= x == 0 ? 0 : (signum((float) x) * (random.nextInt(abs(x)) == 0 ? 0 : 1));
                    y -= y == 0 ? 0 : (signum((float) y) * (random.nextInt(abs(y)) == 0 ? 0 : 1));
                    BlockPos pos = this.blockPosition();
                    for (int h = -1; h <= 2; ++h) {
                        if (checkBlock(this.blockPosition().offset(x, h, y))) {
                            for (int j = 0; j < 5; j++) {
                                particle(this.level, ParticleTypes.COMPOSTER, new Vec3(pos.getX() + x + random.nextFloat(), pos.getY() + h + 1.2, pos.getZ() + y + random.nextFloat()));
                            }
                        }
                    }
                }
            }
            this.nextCheckLeft = this.getNextCheckTime();
        }
    }

    private int getNextCheckTime() {
        return 100 + random.nextInt(50);
    }

    protected boolean checkBlock(BlockPos pos) {
        if (! net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(this.level, this)) return false;

        if (! this.level.getFluidState(pos.above()).isEmpty()) {
            return false;
        } else if (this.level.getBlockState(pos.above()).is(PVZBlocks.CARP_GRASS.get()) && this.level.getBlockState(pos.above()).getValue(BlockStateProperties.AGE_3) != 0) {
            this.level.setBlock(pos.above(), PVZBlocks.CARP_GRASS.get().defaultBlockState(), 18);
            return true;
        } else if (this.level.getBlockState(pos.above()).isAir()) {
            if (this.level.getBlockState(pos).is(PVZBlockTags.UNPLANTABLE_DIRT)) {
                this.level.setBlock(pos, Blocks.GRASS_BLOCK.defaultBlockState(), 3);
                return true;
            } else if (this.level.getBlockState(pos).isCollisionShapeFullBlock(level, pos)) {
                this.level.setBlock(pos.above(), PVZBlocks.CARP_GRASS.get().defaultBlockState(), 3);
                return true;
            }
        }

        return false;
    }


    //sharing
    @Override
    public boolean isShearable(@NotNull ItemStack item, Level level, BlockPos pos) {
        return !this.isBald() && this.isAlive();
    }

    @NotNull
    @Override
    public List<ItemStack> onSheared(@Nullable Player player, @NotNull ItemStack item, Level level, BlockPos pos, int fortune) {
        level.playSound(null, this, SoundEvents.SHEEP_SHEAR, player == null ? SoundSource.BLOCKS : SoundSource.PLAYERS, 1.0F, 1.0F);
        this.gameEvent(GameEvent.SHEAR, player);
        if (!level.isClientSide) {
            this.setBald(true);
            this.growHairTick = 400 + this.random.nextInt(600);
            return List.of(Items.KELP.getDefaultInstance());
        }
        return java.util.Collections.emptyList();
    }

    public boolean isBald() {
        return this.entityData.get(BALD);
    }

    public void setBald(boolean is) {
        this.entityData.set(BALD, is);
    }


    //bucket
    @Override
    public ItemStack getBucketItemStack() {
        return PVZItems.GRASSCARP_BUCKET.get().getDefaultInstance();
    }

    public void saveToBucketTag(ItemStack itemStack) {
        Bucketable.saveDefaultDataToBucketTag(this, itemStack);
        CompoundTag compoundtag = itemStack.getOrCreateTag();
        compoundtag.putInt("GrowHairTick", this.growHairTick);
        compoundtag.putInt("NextChangeTick", this.nextCheckLeft);
    }

    public void loadFromBucketTag(CompoundTag tag) {
        Bucketable.loadDefaultDataFromBucketTag(this, tag);
        if (tag.contains("GrowHairTick")) {
            this.growHairTick = tag.getInt("GrowHairTick");
            this.setBald(growHairTick > 0);
        }
        if (tag.contains("NextChangeTick")) {
            this.nextCheckLeft = tag.getInt("NextChangeTick");
        }
    }

    @Override
    public boolean fromBucket() {
        return this.entityData.get(FROM_BUCKET);
    }

    @Override
    public void setFromBucket(boolean is) {
        this.entityData.set(FROM_BUCKET, is);
    }

    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        return GrassCarp.bucketMobPickup(player, hand, this).orElse(super.mobInteract(player, hand));
    }

    private static <T extends GrassCarp & Bucketable> Optional<InteractionResult> bucketMobPickup(Player player, InteractionHand hand, T grassCarp) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (itemstack.getItem() == Items.WATER_BUCKET && grassCarp.isAlive()) {
            grassCarp.playSound(grassCarp.getPickupSound(), 1.0F, 1.0F);
            ItemStack itemstack1 = grassCarp.getBucketItemStack();
            grassCarp.saveToBucketTag(itemstack1);
            ItemStack itemstack2 = ItemUtils.createFilledResult(itemstack, player, itemstack1, false);
            player.setItemInHand(hand, itemstack2);
            Level level = grassCarp.level;
            if (!level.isClientSide) {
                CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, itemstack1);
            }
            grassCarp.dropItem();
            grassCarp.discard();
            return Optional.of(InteractionResult.sidedSuccess(level.isClientSide));
        } else {
            return Optional.empty();
        }
    }

    //pick-up
    @Override
    public boolean wantsToPickUp(ItemStack itemStack) {
        if (this.isBald() && itemStack.is(Items.KELP) || itemStack.is(Items.BONE_MEAL)) {
            return true;
        } else {
            for (Item item : GrassCarp.fishMap.keySet()) {
                if (itemStack.is(item)) {
                    return true;
                }
            }
        }
        return false;
    }


    //sounds
    @Override
    public SoundEvent getPickupSound() {
        //TODO sounds has not changed.
        return SoundEvents.BUCKET_FILL_AXOLOTL;
    }

    @Override
    protected SoundEvent getHurtSound(DamageSource p_149161_) {
        return SoundEvents.AXOLOTL_HURT;
    }

    @Override
    @javax.annotation.Nullable
    protected SoundEvent getDeathSound() {
        return SoundEvents.AXOLOTL_DEATH;
    }

    @Override
    @javax.annotation.Nullable
    protected SoundEvent getAmbientSound() {
        return this.isInWater() ? SoundEvents.AXOLOTL_IDLE_WATER : SoundEvents.AXOLOTL_IDLE_AIR;
    }

    @Override
    protected SoundEvent getSwimSplashSound() {
        return SoundEvents.AXOLOTL_SPLASH;
    }

    @Override
    protected SoundEvent getFlopSound() {
        return SoundEvents.COD_FLOP;
    }

    @Override
    protected SoundEvent getSwimSound() {
        return SoundEvents.AXOLOTL_SWIM;
    }

    class CollectItemsGoal extends Goal {
        private final GrassCarp carp;
        private ItemEntity target;
        private int coolDown;

        CollectItemsGoal(GrassCarp carp) {
            this.carp = carp;
            this.setFlags(EnumSet.of(Flag.MOVE));
        }

        @Override
        public boolean canUse() {
            if (target != null && target.isAlive()) {
                return true;
            } else if (this.carp.getItemBySlot(EquipmentSlot.MAINHAND).isEmpty() && target == null) {
                target = searchItem();
                return target != null;
            }
            return false;
        }

        @Override
        public boolean canContinueToUse() {
            return isWanted(target);
        }

        @Override
        public void start() {
            this.carp.getNavigation().moveTo(this.target, 1F);
            this.coolDown = this.carp.random.nextInt(100);
        }

        @Override
        public void stop() {
            this.target = null;
        }

        @Override
        public void tick() {
            if (this.carp.distanceToSqr(this.target) <= 2) {
                this.carp.setItemSlot(EquipmentSlot.MAINHAND, this.target.getItem());
                this.carp.take(this.target, 1);
                setGuaranteedDrop(EquipmentSlot.MAINHAND);
                if (target.getItem().getItem() == Items.KELP) {
                    this.carp.nextCheckLeft = this.carp.getNextCheckTime() / 2;
                } else if (target.getItem().getItem() == Items.BONE_MEAL) {
                    this.carp.nextCheckLeft = this.carp.getNextCheckTime() / 5;
                } else {
                    this.carp.nextCheckLeft = this.carp.getNextCheckTime() * 3;
                }
                this.target.discard();
            } else {
                this.carp.getNavigation().moveTo(this.target, 1F);
            }
        }

        private ItemEntity searchItem() {
            if (--this.coolDown <= 0) {
                final List<ItemEntity> list = this.carp.level.getEntitiesOfClass(ItemEntity.class, this.carp.getBoundingBox().inflate(16), this::isWanted);
                if (!list.isEmpty()) {
                    return list.get(0);
                }
            }
            return null;
        }

        public boolean isWanted(ItemEntity itemEntity) {
            return itemEntity.isInWater() && !itemEntity.hasPickUpDelay() && !itemEntity.isRemoved() &&
                    this.carp.getMainHandItem().isEmpty() && this.carp.wantsToPickUp(itemEntity.getItem());
        }
    }
}
