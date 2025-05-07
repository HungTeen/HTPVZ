package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.common.register.PVZParticles;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.client.particle.Particle;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
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

import static com.hungteen.pvz.common.register.PVZDamageSource.*;

public class PotatoMine extends SimplePlant {
    public static final EntityDataAccessor<Integer> EXPLODE_COUNT = SynchedEntityData.defineId(PotatoMine.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> PREPARE_COUNT = SynchedEntityData.defineId(PotatoMine.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Boolean> IS_POISONOUS = SynchedEntityData.defineId(PotatoMine.class, EntityDataSerializers.BOOLEAN);

    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState sleepAnimationState = new AnimationState();
    public AnimationState outAnimationState = new AnimationState();
    private boolean isPresentation = false;

    public static String MINER_SKILL_NAME = "skill.pvz.potato_mine.potato_miner";
    public static String STRONG_SKILL_NAME = "skill.pvz.potato_mine.lethal_dose";
    public static String QUICK_LOAD_SKILL_NAME = "skill.pvz.potato_mine.quick_load";
    public static String POISONOUS_SKILL_NAME = "skill.pvz.potato_mine.poison_enrichment";
    public static List<Skill> staticSkillList = List.of(
            new Skill(MINER_SKILL_NAME, PVZItems.TERRA_ESSENCE, 4, 6, 0, 0),
            new Skill(STRONG_SKILL_NAME, PVZItems.IGNIS_ESSENCE, 8, 8, 25, 300),
            new Skill(QUICK_LOAD_SKILL_NAME, PVZItems.LUX_ESSENCE, 12, 8, 50, 300)
                    .avoidSkills(STRONG_SKILL_NAME),
            new Skill(POISONOUS_SKILL_NAME, PVZItems.ORIGIN_ESSENCE, 6, 8, 75, 300)
                    .avoidSkills(STRONG_SKILL_NAME, QUICK_LOAD_SKILL_NAME)
    );
    public PotatoMine(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.setPoisonous(Math.random() <= 0.02);
    }

    private void explode() {
        if (!this.level.isClientSide) {
            this.dead = true;
            float radius = this.hasSkill(STRONG_SKILL_NAME) ? 3F : 2F;
            level.explode(this, knockBack(transferKiller(knockBack(ignoreInvTime(teamFilter(multiply(DamageSource.explosion(this).bypassArmor(), 1.25F))), 0.1F), PVZEntityCapability.getOwner(this)), 0.2F),
                    null, this.getX(), this.getY(), this.getZ(),
                    radius, false, Explosion.BlockInteraction.NONE);
            if (this.isPoisonous()) {
                this.spawnPoisonCloud();
            }
            this.discard();
            ((ServerLevel) this.level).sendParticles(PVZParticles.MASHED_POTATO.get(),
                    getX() + random.nextFloat() * 0.5 - 0.25,
                    getY() + random.nextFloat() * 0.5 + 0.25,
                    getZ() + random.nextFloat() * 0.5 - 0.25,
                    20, 0.5, 0.5, 0.5, 0.1);
        }
    }

    public void addEffect(Entity entity) {
        if (entity instanceof LivingEntity livingEntity && entity.isAlive()) {
            MobEffect mobEffect = MobEffects.POISON;
            int time = 100;
            if(livingEntity instanceof Mob mob && mob.getMobType() == MobType.UNDEAD){
                mobEffect = MobEffects.WITHER;
                time += 200;
            }
            livingEntity.addEffect(new MobEffectInstance(mobEffect, time, 1));
        }
    }
    private void spawnPoisonCloud() {
        AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.level, this.getX(), this.getY(), this.getZ());
        areaeffectcloud.setRadius(2F);
        areaeffectcloud.setDuration(400);
        areaeffectcloud.setWaitTime(0);
        areaeffectcloud.setOwner(this);
        areaeffectcloud.addEffect(new MobEffectInstance(PVZMobEffects.PHYTOTOXIN.get(), 100));

        if(!this.level.isClientSide)this.level.addFreshEntity(areaeffectcloud);
    }
    public void setupPresentationAnim() {
        this.idleAnimationState.start(this.tickCount);
        this.entityData.set(DATA_POSE, Pose.STANDING);
        this.isPresentation = true;
    }
    //overrides
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new PotatoExplodeGoal(this));
        this.goalSelector.addGoal(1, new AttractEnemyGoal(this));
        this.goalSelector.addGoal(1, new PotatoPrepareGoal(this));
        this.goalSelector.addGoal(3, new AxisLookAroundGoal(this));
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }
    @Override
    public void baseTick() {
        if (this.hasEffect(MobEffects.POISON)) {
            this.setPoisonous(true);
            this.removeEffect(MobEffects.POISON);
        }
        super.baseTick();
        if (getEntityData().get(PREPARE_COUNT) - 1 <= 7) {
            getEntityData().set(DATA_POSE, Pose.STANDING);
        }
        if (EntityUtil.isLeavingGround(this) || (hasSkill(QUICK_LOAD_SKILL_NAME) && this.getEntityData().get(PREPARE_COUNT) > 10)) {
            this.getEntityData().set(PREPARE_COUNT, 10);
        }
        if (hasSkill(POISONOUS_SKILL_NAME) && ! this.getEntityData().get(IS_POISONOUS)) {
            this.getEntityData().set(IS_POISONOUS, true);
        }
        if (this.getEntityData().get(EXPLODE_COUNT) > -1) {
            this.getEntityData().set(EXPLODE_COUNT, this.getEntityData().get(EXPLODE_COUNT) + 1);
            if (this.getEntityData().get(EXPLODE_COUNT) > 10) {
                this.explode();
            }
        }
        if (level.isClientSide()) {
            if (this.isPresentation) {
                this.entityData.set(PREPARE_COUNT, 0);
            }
            if (this.getEntityData().get(PREPARE_COUNT) < 15 && this.getEntityData().get(PREPARE_COUNT) > 5) {
                for (int i = 0; i < 5; i ++) {
                    this.level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()), this.getX() + (this.random.nextDouble() - 0.5D), this.getY() + 0.1D, this.getZ() + (this.random.nextDouble() - 0.5D), (this.random.nextDouble() - 0.5) * 6.0D, 2D, (this.random.nextDouble() - 0.5) * 4.0D);
                }
            }
            if (this.isPoisonous()) {
                int color = MobEffects.POISON.getColor();
                float r = (float)(color >> 16 & 255) / 255.0F;
                float g = (float)(color >> 8 & 255) / 255.0F;
                float b = (float)(color & 255) / 255.0F;

                if (random.nextBoolean()) {
                    float xOffset = random.nextFloat() * 0.6F - 0.3F;
                    float yOffset = random.nextFloat() - 0.3F;
                    float zOffset = random.nextFloat() * 0.6F - 0.3F;
                    Particle particle = ClientProxy.MC.levelRenderer.addParticleInternal(ParticleTypes.ENTITY_EFFECT.getType(), false,
                            this.getX() + xOffset, this.getY() + yOffset, this.getZ() + zOffset, 0.1, 0.2, 0.1);
                    if (particle != null)particle.setColor(r, g, b);
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
    public EntityDimensions getDimensions(Pose pose) {
        return pose == Pose.DIGGING ? this.getType().getDimensions() : EntityDimensions.scalable(0.7F, 0.4F);
    }

    @Override
    public void die(DamageSource damageSource) {
        if (this.entityData.get(PREPARE_COUNT) <= 0 && ! damageSource.isMagic() && (damageSource.getEntity() == null || ! EntityUtil.isTeammate(this, damageSource.getEntity()))) {
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
        this.entityData.define(PREPARE_COUNT, 80);
        this.entityData.define(IS_POISONOUS, false);
        this.entityData.set(DATA_POSE, Pose.DIGGING);
    }
    @Override
    public Set<TagKey<Block>> getAcceptableTags() {
        return this.hasSkill(MINER_SKILL_NAME) ?
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
        tag.putBoolean("isPoisonous",this.getEntityData().get(IS_POISONOUS));
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("PrepareTime")) {
            this.getEntityData().set(PREPARE_COUNT, tag.getInt("PrepareTime"));
        }
        if(tag.contains("isPoisonous")){
            this.getEntityData().set(IS_POISONOUS, tag.getBoolean("isPoisonous"));
        }
        this.tickCount += random.nextInt(50);
    }
    public boolean isPoisonous() {
        return this.getEntityData().get(IS_POISONOUS);
    }
    public void setPoisonous(boolean isPoisonous) {
        this.getEntityData().set(IS_POISONOUS, isPoisonous);
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
                List<Entity> targets = this.potatoMine.level.getEntities(potatoMine, potatoMine.getBoundingBox().inflate(0.6, 0.3, 0.6),
                        (entity) -> entity instanceof LivingEntity && EntityUtil.checkCanEntityBeAttack(potatoMine, entity));
                targets.addAll(this.potatoMine.level.getEntities(potatoMine, new AABB(potatoMine.getRootBlockPos()),
                        (entity) -> entity instanceof LivingEntity && EntityUtil.checkCanEntityBeAttack(potatoMine, entity)));
                if (! targets.isEmpty()) {
                    targets.forEach(target -> {
                        if (target instanceof Mob mob) {
                            mob.setTarget(potatoMine);
                        }
                    });
                    potatoMine.getEntityData().set(EXPLODE_COUNT, 0);
                }
            }
            return false;
        }
    }
}
