package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.PVZAPI;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.events.TeammateTestingEvent;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.entity.plants.base.SimplePlant;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.*;
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
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.fml.common.Mod;

import java.util.List;
import java.util.Set;

import static com.hungteen.pvz.common.register.PVZDamageSource.*;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class PotatoMine extends SimplePlant {
    public static final EntityDataAccessor<Integer> EXPLODE_COUNT = SynchedEntityData.defineId(PotatoMine.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Integer> PREPARE_COUNT = SynchedEntityData.defineId(PotatoMine.class, EntityDataSerializers.INT);

    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState sleepAnimationState = new AnimationState();
    public AnimationState outAnimationState = new AnimationState();
    private boolean isPresentation = false;

    public static String MINER_SKILL_NAME = "skill.pvz.potato_mine.potato_miner";
    public static String STRONG_SKILL_NAME = "skill.pvz.potato_mine.lethal_dose";
    public static String QUICK_LOAD_SKILL_NAME = "skill.pvz.potato_mine.quick_load";
    public static String POISONOUS_SKILL_NAME = "skill.pvz.potato_mine.poison_enrichment";
    public static List<Skill> staticSkillList = List.of(
            new Skill(MINER_SKILL_NAME, PVZItems.TERRA_ESSENCE, 3, 1, 0, 0),
            new Skill(STRONG_SKILL_NAME, PVZItems.IGNIS_ESSENCE, 6, 6, 25, PVZSeedPackets.SLOW - PVZSeedPackets.MEDIUM),
            new Skill(QUICK_LOAD_SKILL_NAME, PVZItems.LUX_ESSENCE, 10, 12, 50, PVZSeedPackets.SLOW - PVZSeedPackets.MEDIUM)
                    .avoidSkills(STRONG_SKILL_NAME),
            new Skill(POISONOUS_SKILL_NAME, PVZItems.ORIGIN_ESSENCE, 12, 6, 75, PVZSeedPackets.SLOW - PVZSeedPackets.MEDIUM)
                    .avoidSkills(STRONG_SKILL_NAME)
    );
    public PotatoMine(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.setPoisonous(Math.random() <= 0.02);
    }

    private void explode() {
        if (! this.level.isClientSide) {
            this.dead = true;
            float radius = this.hasSkill(STRONG_SKILL_NAME) ? 3F : 2F;
//            level.playSound(null, this, PVZSoundEvents.POTATO_MINE_EXPLODE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
            level.explode(this, isPlantDamage(knockBack(
                    ignoreInvTime(
                            teamFilter(
                                    multiply(
                                            DamageSource.explosion(this).bypassArmor()
                                            , (this.isPoisonous() ? 0.75F : 1.25F) * PVZAPI.get().getPlantDamageDatum(this.level))))
                    , 0.2F), null),
                    null, this.getX(), this.getY(), this.getZ(), radius, false, Explosion.BlockInteraction.NONE);
            if (this.isPoisonous()) {
                this.spawnPoisonCloud();
            }
            if (PVZEntityCapability.getOwner(this) instanceof ServerPlayer player) {
                PVZCriteriaTriggers.SPUDOW.trigger(player);
            }
            this.discard();
            ((ServerLevel) this.level).sendParticles(PVZParticles.MASHED_POTATO.get(),
                    getX() + random.nextFloat() * 0.5 - 0.25,
                    getY() + random.nextFloat() * 0.5 + 0.25,
                    getZ() + random.nextFloat() * 0.5 - 0.25,
                    20, 0.5, 0.5, 0.5, 0.1);
        }
    }
    @Override
    public boolean isInvisible() {
        if (this.level.isClientSide) {
            Player player = ClientProxy.getPlayer();
            if (player != null && this.entityData.get(PREPARE_COUNT) <= 0 && ! player.isCreative() && ! player.isSpectator() && player.distanceToSqr(this) > 4) {
                return EntityUtil.checkCanEntityBeAttack(this, player) || super.isInvisible();
            }
        }
        return super.isInvisible();
    }

    @Override
    public boolean isInvisibleTo(Player player) {
        if (this.level.isClientSide) {
            if (this.entityData.get(PREPARE_COUNT) <= 0 && ! player.isCreative() && ! player.isSpectator() && player.distanceToSqr(this) < 16) {
                return false;
            }
        }
        return super.isInvisibleTo(player);
    }
    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void onPlantCheckTeammate(TeammateTestingEvent event) {
        //won't be regarded as target by shooters/pults if far enough.
        if (! event.forCombat) return;
        if (event.A.distanceToSqr(event.B) < 16) return;
        if (event.A instanceof PotatoMine || event.B instanceof PotatoMine) {
            Entity potatoMine = event.A instanceof PotatoMine ? event.A : event.B;
            if (potatoMine.getEntityData().get(PREPARE_COUNT) > 0) return;
            Entity other = event.A == potatoMine ? event.B : event.A;
            event.currentResult = event.currentResult || other instanceof ShooterPlant;
        }
    }
    private void spawnPoisonCloud() {
        AreaEffectCloud areaeffectcloud = new AreaEffectCloud(this.level, this.getX(), this.getY(), this.getZ());
        areaeffectcloud.setRadius(2F);
        areaeffectcloud.setDuration(400);
        areaeffectcloud.setWaitTime(0);
        areaeffectcloud.setOwner(this);
        areaeffectcloud.addEffect(new MobEffectInstance(PVZMobEffects.PHYTOTOXIN.get(), 400));

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
    public List<Skill> getBasicStaticSkillList(){
        return staticSkillList;
    }
    @Override
    public void baseTick() {
        if (this.hasEffect(MobEffects.POISON) || this.hasEffect(PVZMobEffects.PHYTOTOXIN.get())) {
            this.setPoisonous(true);
            this.removeEffect(MobEffects.POISON);
            this.removeEffect(PVZMobEffects.PHYTOTOXIN.get());
        }
        super.baseTick();
        if (getEntityData().get(PREPARE_COUNT) - 1 <= 7) {
            getEntityData().set(DATA_POSE, Pose.STANDING);
        }
        if (EntityUtil.isLeavingGround(this) || (hasSkill(QUICK_LOAD_SKILL_NAME) && this.getEntityData().get(PREPARE_COUNT) > 10)) {
            this.getEntityData().set(PREPARE_COUNT, 10);
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
    public boolean hurt(DamageSource source, float amount) {
        if (source == DamageSource.FALL) return false;
        return super.hurt(source, amount);
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
                level.playSound(null, this, PVZSoundEvents.POTATO_MINE_EMERGE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
            }
        }
        super.onSyncedDataUpdated(p_219422_);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(EXPLODE_COUNT, -1);
        this.entityData.define(PREPARE_COUNT, 80);
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
    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("PrepareTime")) {
            this.getEntityData().set(PREPARE_COUNT, tag.getInt("PrepareTime"));
        }
        this.tickCount += random.nextInt(50);
    }
    public boolean isPoisonous() {
        return this.hasSkill(POISONOUS_SKILL_NAME);
    }
    public void setPoisonous(boolean isPoisonous) {
        if (this.hasSkill(POISONOUS_SKILL_NAME) && ! isPoisonous) {
            this.removeSkill(this, getSkillFromName(POISONOUS_SKILL_NAME));
        } else if (! this.hasSkill(POISONOUS_SKILL_NAME) && isPoisonous) {
            this.addSkill(this, getSkillFromName(POISONOUS_SKILL_NAME));
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
