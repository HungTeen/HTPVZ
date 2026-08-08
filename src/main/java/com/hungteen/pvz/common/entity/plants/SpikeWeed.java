package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.common.block.EntityLightBlock;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.common.entity.plants.base.SimplePlant;
import com.hungteen.pvz.common.register.*;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.List;
import java.util.Set;

public class SpikeWeed extends SimplePlant {
    protected static final EntityDataAccessor<Direction> ATTACH_FACE = SynchedEntityData.defineId(SpikeWeed.class, EntityDataSerializers.DIRECTION);
    public static final String ON_WALL_SKILL_NAME = "skill.pvz.spike_weed.viscous_pseudoroots";
    public static final String POISONOUS_SKILL_NAME = "skill.pvz.spike_weed.poison_attenna";
    public static List<Skill> staticSkillList = List.of(
            new Skill(ON_WALL_SKILL_NAME, PVZItems.TERRA_ESSENCE, 12, 8, 0, 0),
            new Skill(POISONOUS_SKILL_NAME, PVZItems.ORIGIN_ESSENCE, 12, 8, 100, 0)
    );

    public SpikeWeed(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.ATTACK_DAMAGE, 3D);
    }

    public void setupPresentationAnim() {
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new SpikeWeedAttackGoal(this));
        this.goalSelector.addGoal(2, new AxisLookAroundGoal(this));
    }
    @Override
    public List<Skill> getBasicStaticSkillList(){
        return staticSkillList;
    }
    @Override
    public void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(ATTACH_FACE, Direction.UP);
    }
    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
    @Override
    public Direction getGrowDirection() {
        return this.entityData.get(ATTACH_FACE);
    }
    private void setGrowDirection(Direction p_149789_) {
        this.entityData.set(ATTACH_FACE, p_149789_);
    }
    @Override
    public Set<TagKey<Block>> getAcceptableTags() {
        return Set.of(PVZBlockTags.PLANTABLE_DIRT, PVZBlockTags.UNPLANTABLE_DIRT, PVZBlockTags.PLANTABLE_STONE);
    }
    @Override
    public BlockPos getRootBlockPos() {
        boolean relative = switch (this.getGrowDirection()) {
            case DOWN -> this.position().y - this.blockPosition().getY() >= 0;
            case UP -> this.position().y - this.blockPosition().getY() <= 0;
            case NORTH -> this.position().z - this.blockPosition().getZ() <= 0.5;
            case SOUTH -> this.position().z - this.blockPosition().getZ() >= 0.5;
            case WEST -> this.position().x - this.blockPosition().getX() <= 0.5;
            case EAST -> this.position().x - this.blockPosition().getX() >= 0.5;
        };
        return relative ? blockPosition().relative(getGrowDirection().getOpposite()) : blockPosition();
    }
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_33434_) {
        if (ATTACH_FACE.equals(p_33434_)) {
            this.setBoundingBox(this.makeBoundingBox());
        }
        super.onSyncedDataUpdated(p_33434_);
    }
    @Override
    protected AABB makeBoundingBox() {
        if (this.getGrowDirection() == Direction.UP) {
            return super.makeBoundingBox();
        }
        Direction direction = this.getGrowDirection();
        AABB aabb = new AABB(blockPosition());
        Vec3 offset = this.getPosition(0).subtract(blockPosition().getX(), blockPosition().getY(), blockPosition().getZ()).subtract(0.5, 0, 0.5);
        aabb = aabb.setMaxX(aabb.maxX - 1e-4 + offset.x);
        aabb = aabb.setMaxY(aabb.maxY - 1e-4 + offset.y);
        aabb = aabb.setMaxZ(aabb.maxZ - 1e-4 + offset.z);
        aabb = aabb.setMinX(aabb.minX + 1e-4 + offset.x);
        aabb = aabb.setMinY(aabb.minY + 1e-4 + offset.y);
        aabb = aabb.setMinZ(aabb.minZ + 1e-4 + offset.z);
        switch (direction) {
            case UP -> aabb = aabb.setMaxY(aabb.minY + 0.125);
            case DOWN -> aabb = aabb.setMinY(aabb.maxY - 0.125);
            case EAST -> aabb = aabb.setMaxX(aabb.minX + 0.125);
            case WEST -> aabb = aabb.setMinX(aabb.maxX - 0.125);
            case SOUTH -> aabb = aabb.setMaxZ(aabb.minZ + 0.125);
            case NORTH -> aabb = aabb.setMinZ(aabb.maxZ - 0.125);
        }
        return aabb;
    }

    @Override
    public Vec3 getDeltaMovement() {
        boolean bool = getGrowDirection() == Direction.UP
                || ! this.hasSkill(ON_WALL_SKILL_NAME)
                || level.getBlockState(this.getRootBlockPos()).isAir()
                || (level.getBlockState(this.getRootBlockPos()).getBlock() instanceof IFluidBlock);
        return bool ? super.getDeltaMovement() : Vec3.ZERO;
    }
    @Override
    public void tick() {
        if ((getGrowDirection() != Direction.UP)
                && (level.getBlockState(this.getRootBlockPos()).isAir()
                        || ! this.hasSkill(ON_WALL_SKILL_NAME)
                        || (level.getBlockState(this.getRootBlockPos()).getBlock() instanceof IFluidBlock))) {
            this.setGrowDirection(Direction.UP);
        }
        super.tick();
        BlockPos pos = blockPosition();
        if (level.isClientSide()) {
            return ;
        } else if (level.getBlockState(pos).isAir()) {
            level.setBlock(pos, PVZBlocks.ENTITY_LIGHT.get().defaultBlockState()
                    .setValue(EntityLightBlock.LEVEL, 6), 2);
        } else if (level.getBlockState(pos).is(Blocks.WATER)) {
            level.setBlock(pos, PVZBlocks.ENTITY_LIGHT.get().defaultBlockState()
                    .setValue(EntityLightBlock.WATERLOGGED, true).setValue(EntityLightBlock.LEVEL, 6), 2);
        }
        if (level.getBlockState(pos).is(PVZBlocks.ENTITY_LIGHT.get())) {
            level.setBlock(pos, level.getBlockState(pos)
                    .setValue(EntityLightBlock.HAS_SOURCE, true), 2);
        }
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("attach_direction", getGrowDirection().getName());
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        Direction direction = Direction.byName(tag.getString("attach_direction"));
        this.setGrowDirection(direction == null ? Direction.UP : direction);
    }
    @Override
    public MutableComponent customPositionSafe(PVZResourceEvent.CheckPlantConditionEvent event, Level level, BlockPos pos, Direction direction, boolean isPlanting) {
        if (isPlanting && hasSkill(ON_WALL_SKILL_NAME)) {
            setGrowDirection(direction);
        }
        return super.customPositionSafe(event, level, pos, direction, isPlanting);
    }
    @Override
    public MutableComponent customVehicleSafe(PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        if (target == null) {
            return Component.translatable("hint.pvz.plant.entity_not_present");
        }
        return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), target.getName());
    }
    public static class SpikeWeedAttackGoal extends Goal {
        SimplePlant entity;
        int attackCoolDown = 5;
        public SpikeWeedAttackGoal(SimplePlant entity) {
            this.entity = entity;
        }

        @Override
        public boolean canUse() {
            return -- attackCoolDown == 0;
        }

        @Override
        public void tick() {
            attackCoolDown = 5;
            Vec3i direction = entity.getGrowDirection().getNormal();
            List<Entity> list = entity.level.getEntities(entity,
                    entity.getBoundingBox().inflate(0.1 * Math.abs(direction.getX()), 0.1 * Math.abs(direction.getY()), 0.1 * Math.abs(direction.getZ())),
                    (entity1) -> EntityUtil.checkCanEntityBeAttack(entity, entity1));
            if (! list.isEmpty()) entity.level.playSound(null, entity, PVZSoundEvents.SPIKE_WEED_ATTACK.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
            list.forEach((entity1 -> {
                entity1.hurt(PVZDamageSource.spikeWeedHurt(entity, entity1), (float) entity.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
                if (entity1 instanceof LivingEntity && entity.hasSkill(POISONOUS_SKILL_NAME)) {
                    ((LivingEntity) entity1).addEffect(new MobEffectInstance(PVZMobEffects.PHYTOTOXIN.get(), 60));
                }
            }));
        }
    }
}
