package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.DisperseEnemyTargetGoal;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ItemParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.FluidTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Bucketable;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ItemUtils;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.Shapes;

import java.util.List;
import java.util.Optional;
import java.util.Set;
public class TangleKelp extends SimplePlant implements Bucketable {
    private static final EntityDataAccessor<Boolean> FROM_BUCKET = SynchedEntityData.defineId(TangleKelp.class, EntityDataSerializers.BOOLEAN);
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.tangle_kelp.torpedo_kelp", PVZItems.AQUA_ESSENCE, 8, 4, 50, 0),
            new Skill("skill.pvz.tangle_kelp.oxygen_algae", PVZItems.VENTUS_ESSENCE, 8, 8, 175, 700).avoidSkills(0)
    );

    public void setupPresentationAnim() {
    }
    public TangleKelp(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.entityData.set(root(), false);
        this.entityData.set(SimplePlant.TAKES_COINCIDE_DMG, false);
    }

    //entity settings
    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 8D)
                .add(Attributes.FOLLOW_RANGE, 4D)
                .add(Attributes.MOVEMENT_SPEED, 0.1D)
                .add(Attributes.ATTACK_DAMAGE, 3.0D);
    }
    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(FROM_BUCKET, false);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }
    public boolean canBreatheUnderwater() {
        return true;
    }
    @Override
    public Set<TagKey<Block>> getAcceptableTags() {
        return Set.of(PVZBlockTags.PLANTABLE_DIRT, BlockTags.SNOW);
    }
    @Override
    public void tick() {
        if (! this.noPhysics) {
            //TODO sync from Lily Pad.
            if (this.getFirstPassenger() != null && ! level.getFluidState(new BlockPos(position())).isEmpty()) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, - 0.08, 0));
            }
            if (! level.getFluidState(new BlockPos(position().add(0, 0.8, 0))).isEmpty() && this.getFirstPassenger() == null) {
                this.setDeltaMovement(this.getDeltaMovement().add(0, 0.08, 0).multiply(0.5, 0.5, 0.5));
            } else if (! level.getFluidState(new BlockPos(position().add(0, 0.5, 0))).isEmpty()) {
                this.setDeltaMovement(this.getDeltaMovement().multiply(0.5, 0.5, 0.5));
                if (Math.abs(this.getDeltaMovement().y) < 0.001 && this.getDeltaMovement().y != 0) {
                    this.setDeltaMovement(this.getDeltaMovement().multiply(1, 0, 1));
                }
            }
        }
        if (level.isClientSide) {
            if (this.getFirstPassenger() != null) {
                int i = 0;
                    while (i < 3) {
                        i ++;
                        level.addParticle(new ItemParticleOption(ParticleTypes.ITEM, new ItemStack(Items.KELP)),
                            getX() + random.nextFloat() * 0.25 - 0.12, getY() + 0.5 + random.nextFloat() * 0.25 - 0.12, getZ() + random.nextFloat() * 0.25 - 0.12,
                            random.nextFloat() * 0.3 - 0.15,
                            random.nextFloat() * 0.3,
                            random.nextFloat() * 0.3 - 0.15);
                }
            }
            if (this.hasSkill("skill.pvz.tangle_kelp.oxygen_algae")) {
                level.addParticle(ParticleTypes.BUBBLE,//TODO change that.
                        getX() - random.nextFloat() * 4 + 2, getY() + 0.5 - random.nextFloat() * 2, getZ() - random.nextFloat() * 4 + 2,
                        random.nextFloat() * 0.25 - 0.12,
                        random.nextFloat() * 0.25,
                        random.nextFloat() * 0.25 - 0.12);
            }
        }
        this.shouldAlign = false;
        super.tick();
    }
    protected void handleAirSupply(int p_30344_) {
        if (this.isAlive() && !this.isInWaterOrBubble()) {
            this.setAirSupply(p_30344_ - 1);
            if (this.getAirSupply() == -20) {
                this.setAirSupply(0);
                this.hurt(DamageSource.DROWN, 2.0F);
            }
        } else {
            this.setAirSupply(this.getMaxAirSupply());
        }
    }
    public void baseTick() {
        int i = this.getAirSupply();
        super.baseTick();
        this.handleAirSupply(i);
    }

    public boolean isPushedByFluid() {
        return true;
    }
    @Override
    public boolean isPushable(){
        return this.getFirstPassenger() != null;
    }
    @Override
    public Direction getGrowDirection() {
        return null;
    }
    @Override
    public BlockPos getRootBlockPos() {
        return new BlockPos(this.position());
    }
    @Override
    public MutableComponent plantPositionSafe(PVZResourceEvent.CheckPlantConditionEvent event, Level level, BlockPos pos, Direction direction, boolean isPlanting) {
        //resource check.
        if (isPlanting && event != null) {
            if (event.cost > PVZPlayerCapability.getValue(event.getEntity(), event.resource)) {
                return Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource));
            }
        }
        //position adjustment.
        Vec3i offset = direction == null ? Vec3i.ZERO : direction.getNormal();
        pos = pos.offset(offset).offset(getGrowDirection() == null ? Vec3i.ZERO : getGrowDirection().getOpposite().getNormal());
        direction = getGrowDirection();
        offset = direction == null ? Vec3i.ZERO : direction.getNormal();
        //collision check.
        AABB aabb = AABB.ofSize(new Vec3(pos.getX() + 0.5 + offset.getX(),
                        pos.getY() + offset.getY() + getBbHeight() / 2,
                        pos.getZ() + 0.5 + offset.getZ()),
                getBbWidth() - 0.0001, getBbHeight() - 0.0001, getBbWidth() - 0.0001);
            //1. blocks.
        if (BlockPos.betweenClosedStream(aabb).anyMatch((p_201942_) -> {
            BlockState blockstate = this.level.getBlockState(p_201942_);
            return !blockstate.isAir() && blockstate.isSuffocating(this.level, p_201942_) &&
                    Shapes.joinIsNotEmpty(blockstate.getCollisionShape(this.level, p_201942_).move(p_201942_.getX(), p_201942_.getY(), p_201942_.getZ()), Shapes.create(aabb), BooleanOp.AND);
        })) {
            return Component.translatable("hint.pvz.plant.no_enough_place");
        }
            //2. entities.
        if (shouldHaveCoincideDmg(level, Vec3.atBottomCenterOf(pos.offset(offset)))) {
            return Component.translatable("hint.pvz.plant.no_enough_place");
        }
        //root block available check.
        if (! this.getEntityData().get(root()) || (! level.getBlockState(pos).isAir())) {
            if (level.getBlockState(pos).getFluidState().is(FluidTags.WATER)) {
                //final plant.
                if (isPlanting) {
                    this.moveTo(
                            pos.getX() + 0.5 + offset.getX(),
                            pos.getY() + (level.getBlockState(pos).getCollisionShape(level, pos).isEmpty() ?
                                    level.getFluidState(pos).isEmpty() ? 0: level.getFluidState(pos).getHeight(level, pos) :
                                    level.getBlockState(pos).getCollisionShape(level, pos).bounds().maxY),
                            pos.getZ() + 0.5 + offset.getZ());
                    ((ServerLevel)this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()), this.getX(), this.getY(), this.getZ(), 5, 0.0D, 0.0D, 0.0D, 0.15F);
                }
                return null;
            }
        }
        if (isPlanting) {
            return Component.translatable("hint.pvz.plant.can_only_plant_in_water", this.getName());
        }
        return null;
    }
    @Override
    public MutableComponent plantVehicleSafe(PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        if (target == null) {
            return Component.translatable("hint.pvz.plant.entity_not_present");
        }
        return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), target.getName());
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new TangleKelpAttackGoal(this));
        this.targetSelector.addGoal(1, new DisperseEnemyTargetGoal(this,
                (entity)-> EntityUtil.checkCanEntityBeAttack(this, entity) && ! entity.isPassenger(), -1));
    }

    public boolean rideableUnderWater() {
        return true;
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
        return TangleKelp.bucketMobPickup(player, hand, this).orElse(super.mobInteract(player, hand));
    }

    private static <T extends TangleKelp & Bucketable> Optional<InteractionResult> bucketMobPickup(Player player, InteractionHand hand, T tangleKelp) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (PVZConfig.PVZGameRules.getBoolean(tangleKelp.level, "canCanCanKelp") && itemstack.getItem() == Items.WATER_BUCKET && tangleKelp.isAlive() && EntityUtil.isTeammate(player, tangleKelp)) {
            tangleKelp.playSound(tangleKelp.getPickupSound(), 1.0F, 1.0F);
            ItemStack itemstack1 = tangleKelp.getBucketItemStack();
            tangleKelp.saveToBucketTag(itemstack1);
            ItemStack itemstack2 = ItemUtils.createFilledResult(itemstack, player, itemstack1, false);
            player.setItemInHand(hand, itemstack2);
            Level level = tangleKelp.level;
            if (!level.isClientSide) {
                CriteriaTriggers.FILLED_BUCKET.trigger((ServerPlayer) player, itemstack1);
            }
            tangleKelp.discard();
            return Optional.of(InteractionResult.sidedSuccess(level.isClientSide));
        } else {
            return Optional.empty();
        }
    }

    @Override
    public ItemStack getBucketItemStack() {
        return PVZItems.TANGLE_KELP_BUCKET.get().getDefaultInstance();
    }

    public void saveToBucketTag(ItemStack itemStack) {
        Bucketable.saveDefaultDataToBucketTag(this, itemStack);
        CompoundTag compoundtag = itemStack.getOrCreateTag();
        PVZOwnedCapability cap = getCapability(PVZOwnedCapability.CAP).orElse(null);
        if (cap != null && cap.ownerUuid != null) {
            compoundtag.putUUID("Owner", cap.ownerUuid);
        }
    }

    public void loadFromBucketTag(CompoundTag tag) {
        Bucketable.loadDefaultDataFromBucketTag(this, tag);
        if (tag.contains("Owner")) {
            PVZOwnedCapability cap = getCapability(PVZOwnedCapability.CAP).orElse(null);
            if (cap != null) {
                cap.ownerUuid = tag.getUUID("Owner");
            }
        }
    }

    @Override
    public SoundEvent getPickupSound() {
        //TODO sounds has not changed.
        return SoundEvents.BUCKET_FILL_AXOLOTL;
    }

    public static class TangleKelpAttackGoal extends Goal{
        TangleKelp tangleKelp;
        public TangleKelpAttackGoal(TangleKelp tangleKelp) {
            this.tangleKelp = tangleKelp;
        }
        @Override
        public boolean canUse() {
            return tangleKelp.tickCount > 20;
        }

        @Override
        public void tick() {
            if (tangleKelp.tickCount % 50 < 2 && tangleKelp.hasSkill("skill.pvz.tangle_kelp.oxygen_algae")) {
                List<LivingEntity> list = tangleKelp.level.getEntities(EntityTypeTest.forClass(LivingEntity.class),
                        new AABB(tangleKelp.getX() - 6, tangleKelp.getY() - 6, tangleKelp.getZ() - 6,
                                tangleKelp.getX() + 6, tangleKelp.getY(), tangleKelp.getZ() + 6),
                        (player) -> EntityUtil.isTeammate(player, tangleKelp));
                list.forEach((player -> player.addEffect(new MobEffectInstance(MobEffects.WATER_BREATHING, 100), this.tangleKelp)));
            }
            if (tangleKelp.hasSkill("skill.pvz.tangle_kelp.torpedo_kelp") && EntityUtil.isEntityValid(tangleKelp.getTarget())) {
                tangleKelp.lookAt(tangleKelp.getTarget(), 10, 10);
                tangleKelp.setDeltaMovement(tangleKelp.getDeltaMovement().multiply(0, 1, 0)
                        .add(tangleKelp.getTarget().position().subtract(tangleKelp.position()).multiply(1, 0, 1).normalize()
                                .scale(tangleKelp.getAttributeValue(Attributes.MOVEMENT_SPEED)))
                        );
            }
            if (tangleKelp.isInWaterOrBubble()) {
                if (! EntityUtil.isEntityValid(tangleKelp.getFirstPassenger())) {
                    List<Entity> entities = tangleKelp.level.getEntities(tangleKelp, tangleKelp.getBoundingBox().inflate(0, 0.5, 0),
                        (entity) -> (entity instanceof LivingEntity && entity.isAlive() && ! entity.isPassenger() &&
                                EntityUtil.checkCanEntityBeAttack(entity, this.tangleKelp)));
                    if (! entities.isEmpty()) {
                        entities.get(0).startRiding(this.tangleKelp);
                    }
                } else if (tangleKelp.tickCount % 20 < 2) {
                    Entity target = tangleKelp.getFirstPassenger();
                    target.hurt(PVZDamageSource.TANGLE_KELP, (float) this.tangleKelp.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    tangleKelp.hurt(PVZDamageSource.TANGLE_KELP, 1);
                    if (! target.isAlive()) {
                        this.tangleKelp.discard();
                    }
                }
            } else {

            }
        }
    }
}
