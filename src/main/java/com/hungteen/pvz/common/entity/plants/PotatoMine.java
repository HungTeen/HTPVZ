package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.Set;

import static com.hungteen.pvz.common.register.PVZDamageSource.teamFilter;

public class PotatoMine extends SimplePlant {
    public static final EntityDataAccessor<Integer> EXPLODE_COUNT = SynchedEntityData.defineId(WallNut.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> PREPARE_COUNT = SynchedEntityData.defineId(WallNut.class, EntityDataSerializers.INT);

    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState sleepAnimationState = new AnimationState();
    public AnimationState outAnimationState = new AnimationState();

    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.potato_mine.potato_miner", PVZItems.TERRA_ESSENCE, 4, 6, 0, 0),
            new Skill("skill.pvz.potato_mine.lethal_dose", PVZItems.IGNIS_ESSENCE, 8, 8, 75, 0).avoidSkills(1),
            new Skill("skill.pvz.potato_mine.quick_load", PVZItems.LUX_ESSENCE, 12, 8, 25, 0).avoidSkills(1, 2)
    );
    public PotatoMine(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    private void explode() {
        if (!this.level.isClientSide) {
            this.dead = true;
            level.explode(this, teamFilter(DamageSource.explosion(this).bypassArmor()), null, this.getX(), this.getY(), this.getZ(),
                    this.hasSkill("skill.pvz.potato_mine.lethal_dose") ? 3F : 2F, false, Explosion.BlockInteraction.NONE);
            this.discard();//TODO modify damage calculator.
        }
    }

    //overrides
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new PotatoExplodeGoal(this));
        this.goalSelector.addGoal(1, new AttractEnemyGoal(this));
        this.goalSelector.addGoal(1, new PotatoPrepareGoal(this));
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }
    @Override
    public void baseTick() {
        super.baseTick();
        if (getEntityData().get(PREPARE_COUNT) - 1 <= 7) {
            getEntityData().set(DATA_POSE, Pose.STANDING);
        }
        if (hasSkill("skill.pvz.potato_mine.quick_load") && this.getEntityData().get(PREPARE_COUNT) > 10) {
            this.getEntityData().set(PREPARE_COUNT, 10);
        }
        if (this.getEntityData().get(EXPLODE_COUNT) > -1) {
            this.getEntityData().set(EXPLODE_COUNT, this.getEntityData().get(EXPLODE_COUNT) + 1);
            if (this.getEntityData().get(EXPLODE_COUNT) > 10) {
                this.explode();
            }
        }
        if (level.isClientSide()) {
            if (this.getEntityData().get(PREPARE_COUNT) < 15 && this.getEntityData().get(PREPARE_COUNT) > 5) {
                for (int i = 0; i < 5; i ++) {
                    this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()), this.getX() + (this.random.nextDouble() - 0.5D), this.getY() + 0.1D, this.getZ() + (this.random.nextDouble() - 0.5D), (this.random.nextDouble() - 0.5) * 6.0D, 2D, (this.random.nextDouble() - 0.5) * 4.0D);
                }
            }
        }
    }
    @Override
    protected AABB makeBoundingBox() {
        double width = this.getEntityData().get(PREPARE_COUNT) - 10 > 0 ? 0.2 : 0.35;
        return new AABB(this.position().add(-width, 0, -width), this.position().add(width, 0.4F, width));
    }
    @Override
    public EntityDimensions getDimensions(Pose p_19975_) {
        return this.entityData.get(DATA_POSE) == Pose.DIGGING ? this.getType().getDimensions() : EntityDimensions.scalable(0.7F, 0.4F);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (this.entityData.get(PREPARE_COUNT) <= 0 && ! damageSource.isMagic() && PVZOwnedCapability.isTeammate(this, damageSource.getEntity())) {
            this.explode();
        }
        super.die(damageSource);
    }
    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_219422_) {
        if (PREPARE_COUNT.equals(p_219422_)) {
            if (entityData.get(PREPARE_COUNT) > 10) {
                this.idleAnimationState.stop();
                this.outAnimationState.stop();
                this.sleepAnimationState.start(this.tickCount);
            } else if (entityData.get(PREPARE_COUNT) <= 0) {
                this.sleepAnimationState.stop();
                this.outAnimationState.stop();
                this.idleAnimationState.start(this.tickCount);
            } else if (entityData.get(PREPARE_COUNT) == 10){
                this.sleepAnimationState.stop();
                this.idleAnimationState.stop();
                this.outAnimationState.start(this.tickCount);
            }
        }
        super.onSyncedDataUpdated(p_219422_);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(EXPLODE_COUNT, -1);
        this.entityData.define(PREPARE_COUNT, 100);
        this.entityData.set(DATA_POSE, Pose.DIGGING);
    }
    @Override
    public Set<TagKey<Block>> getAcceptableTags() {
        return this.hasSkill("skill.pvz.potato_mine.potato_miner") ?
                Set.of(PVZBlockTags.PLANTABLE_DIRT, PVZBlockTags.UNPLANTABLE_DIRT, PVZBlockTags.PLANTABLE_STONE):
                Set.of(PVZBlockTags.PLANTABLE_DIRT, PVZBlockTags.UNPLANTABLE_DIRT);
    }
    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 2D);
    }
    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("PrepareTime", this.getEntityData().get(PREPARE_COUNT));

    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("PrepareTime")) {
            this.getEntityData().set(PREPARE_COUNT, tag.getInt("PrepareTime"));
        }
    }

    public static class PotatoPrepareGoal extends Goal {
        private final PotatoMine potatoMine;
        public PotatoPrepareGoal(PotatoMine potatoMine) {
            this.potatoMine = potatoMine;
        }
        @Override
        public boolean canUse() {
            return potatoMine.getEntityData().get(PREPARE_COUNT) > 0;
        }
        public void tick() {
            int currentTick = potatoMine.getEntityData().get(PREPARE_COUNT) - 1;
            potatoMine.getEntityData().set(PREPARE_COUNT, currentTick);
        }
    }

    public static class PotatoExplodeGoal extends Goal {
        private final PotatoMine potatoMine;
        public PotatoExplodeGoal(PotatoMine potatoMine) {
            this.potatoMine = potatoMine;
        }

        @Override
        public boolean canUse() {
            if (potatoMine.getEntityData().get(EXPLODE_COUNT) == -1 && potatoMine.getEntityData().get(PREPARE_COUNT) == 0) {
                List<Entity> targets = this.potatoMine.level.getEntities(potatoMine, potatoMine.getBoundingBox(),
                        (entity) -> entity instanceof LivingEntity && EntityUtil.checkCanEntityBeAttack(potatoMine, entity));
                targets.addAll(this.potatoMine.level.getEntities(potatoMine, new AABB(potatoMine.getRootBlockPos()),
                        (entity) -> entity instanceof LivingEntity && EntityUtil.checkCanEntityBeAttack(potatoMine, entity)));
                if (! targets.isEmpty()) {
                    potatoMine.getEntityData().set(EXPLODE_COUNT, 0);
                }
            }
            return false;
        }
    }
}
