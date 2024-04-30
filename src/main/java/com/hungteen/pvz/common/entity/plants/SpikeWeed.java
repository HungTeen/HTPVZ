package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.common.register.PVZDamageSource;
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
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.fluids.IFluidBlock;

import java.util.List;
import java.util.Set;

public class SpikeWeed extends SimplePlant {
    protected static final EntityDataAccessor<Direction> ATTACH_FACE = SynchedEntityData.defineId(SpikeWeed.class, EntityDataSerializers.DIRECTION);
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.spike_weed.viscous_pseudoroots", PVZItems.TERRA_ESSENCE, 6, 4, 0, 0),
            new Skill("skill.pvz.spike_weed.poison_attenna", PVZItems.ORIGIN_ESSENCE, 6, 4, 100, 0)
    );

    public SpikeWeed(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.setNoGravity(true);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 8D)
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
    public List<Skill> getStaticSkillList(){
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
        return getAttachFace();
    }
    @Override
    public Set<TagKey<Block>> getAcceptableTags() {
        return Set.of(PVZBlockTags.PLANTABLE_DIRT, PVZBlockTags.UNPLANTABLE_DIRT, PVZBlockTags.PLANTABLE_STONE);
    }
    @Override
    public BlockPos getRootBlockPos() {
        return getOnPos().above().relative(getGrowDirection().getOpposite());
    }
    public Direction getAttachFace() {
        return this.entityData.get(ATTACH_FACE);
    }
    private void setAttachFace(Direction p_149789_) {
        this.entityData.set(ATTACH_FACE, p_149789_);
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
        Direction direction = this.getAttachFace();
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
    public void baseTick() {
        super.baseTick();
        setNoGravity(! level.getBlockState(this.getRootBlockPos()).isAir() && ! (level.getBlockState(this.getRootBlockPos()).getBlock() instanceof IFluidBlock));
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("attach_direction", this.entityData.get(ATTACH_FACE).getName());
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        Direction direction = Direction.byName(tag.getString("attach_direction"));
        this.entityData.set(ATTACH_FACE, direction == null ? Direction.UP : direction);
    }
    @Override
    public MutableComponent plantPositionSafe(PVZResourceEvent.CheckPlantConditionEvent event, Level level, BlockPos pos, Direction direction, boolean isPlanting) {
        if (isPlanting && hasSkill("skill.pvz.spike_weed.viscous_pseudoroots")) {
            setAttachFace(direction);
        }
        return super.plantPositionSafe(event, level, pos, direction, isPlanting);
    }
    @Override
    public MutableComponent plantVehicleSafe(PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        if (target == null) {
            return Component.translatable("hint.pvz.plant.entity_not_present");
        }
        return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), target.getName());
    }
    public static class SpikeWeedAttackGoal extends Goal {
        SimplePlant entity;
        int attackCoolDown = 3;
        public SpikeWeedAttackGoal(SimplePlant entity) {
            this.entity = entity;
        }

        @Override
        public boolean canUse() {
            return -- attackCoolDown == 0;
        }

        @Override
        public void tick() {
            attackCoolDown = 3;
            Vec3i direction = entity.getGrowDirection().getNormal();
            List<Entity> list = entity.level.getEntities(entity,
                    entity.getBoundingBox().inflate(0.1 * Math.abs(direction.getX()), 0.1 * Math.abs(direction.getY()), 0.1 * Math.abs(direction.getZ())),
                    (entity1) -> EntityUtil.checkCanEntityBeAttack(entity, entity1));
            list.forEach((entity1 -> {
                entity1.hurt(PVZDamageSource.SPIKE_WEED, (float) entity.getAttribute(Attributes.ATTACK_DAMAGE).getValue());
                if (entity1 instanceof LivingEntity && entity.hasSkill("skill.pvz.spike_weed.poison_attenna")) {
                    ((LivingEntity) entity1).addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 10));
                }
            }));
        }
    }
}
