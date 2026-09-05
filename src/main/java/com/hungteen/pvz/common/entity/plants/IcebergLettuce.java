package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.common.register.PVZSeedPackets;
import com.hungteen.pvz.common.register.PVZSoundEvents;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.entity.projectile.Snowball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

public class IcebergLettuce extends ShooterPlant {
    public static final String SHOOTER_SKILL_NAME = "skill.pvz.iceberg_lettuce.lettuce_shooter";
    public static final String RANGE_SKILL_NAME = "skill.pvz.iceberg_lettuce.ice_storm";
    public static List<Skill> staticSkillList = List.of(
            new Skill(SHOOTER_SKILL_NAME, PVZItems.VENTUS_ESSENCE, 12, 6, 0, PVZSeedPackets.MEDIUM - PVZSeedPackets.FAST),
            new Skill(RANGE_SKILL_NAME, PVZItems.GELUM_ESSENCE, 14, 6, 50, PVZSeedPackets.MEDIUM - PVZSeedPackets.FAST)
    );

    public IcebergLettuce(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }


    //entity settings
    public static AttributeSupplier.Builder createAttributes() {
        return ShooterPlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 2D)
                .add(Attributes.FOLLOW_RANGE, 8D);
    }
    @Override
    public List<Skill> getBasicStaticSkillList(){
        return staticSkillList;
    }

    @Override
    public Set<TagKey<Block>> getAcceptableTags() {
        return Set.of(PVZBlockTags.PLANTABLE_DIRT, PVZBlockTags.UNPLANTABLE_DIRT, BlockTags.SNOW, BlockTags.ICE, PVZBlockTags.PLANTABLE_STONE);
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new IcebergLettuceFreezeGoal(this));
    }
    @Override
    public boolean isInvulnerableTo(DamageSource source) {
        return source == DamageSource.FREEZE || super.isInvulnerableTo(source);
    }
    @Override
    public boolean canFreeze() {
        return false;
    }
    @Override
    protected Projectile createBullet() {
        return new Snowball(this.level, this);
    }

    @Override
    public float getAttackDamage() {
        return 0;
    }
    @Override
    public void die(DamageSource damageSource) {
        if (! damageSource.isMagic() && damageSource.getEntity() != null && ! EntityUtil.isTeammate(this, damageSource.getEntity())) {
            if (damageSource.getEntity() instanceof LivingEntity target && damageSource.getDirectEntity() == damageSource.getEntity()) {
                MobEffectInstance instance = new MobEffectInstance(PVZMobEffects.FREEZE.get(), 40);
                if (this.hasSkill(RANGE_SKILL_NAME)) {
                    List<Entity> entities = this.level.getEntities(this, this.getBoundingBox().inflate(2, 0.25, 2),
                            (entity) -> (entity instanceof LivingEntity && EntityUtil.checkCanEntityBeAttack(this, entity)));
                    entities.forEach((entity) -> ((LivingEntity) entity).addEffect(instance));
                } else {
                    target.addEffect(instance);
                }
                ((ServerLevel) this.level).sendParticles(ParticleTypes.CLOUD, this.getX(), this.getY() + 0.2, this.getZ(), this.hasSkill(RANGE_SKILL_NAME) ? 60 : 20, 0.0D, 0.0D, 0.0D, this.hasSkill(RANGE_SKILL_NAME) ? 0.2F : 0.1F);
                this.discard();
            }
        }
        super.die(damageSource);
    }

    @Override
    public int getShootCD() {
        return 40;
    }
    @Override
    public Set<Integer> shootTimes() {
        return Set.of(1, 3, 5);
    }

    @Override
    public int shootAnimLength() {
        return 10;
    }
    @Override
    public boolean canShoot() {
        return super.canShoot() && this.hasSkill(SHOOTER_SKILL_NAME);
    }

    @Override
    public void shootBullet() {
        this.performShoot(0, 0, this.getBbHeight() * 0.5F, false, 5);
    }
    @Override
    public double getMaxShootAngleTangent() {
        return 1;
    }

    private static class IcebergLettuceFreezeGoal extends Goal {
        IcebergLettuce entity;
        public IcebergLettuceFreezeGoal(IcebergLettuce entity) {
            super();
            this.entity = entity;
        }
        @Override
        public boolean canUse() {
            return true;
        }
        @Override
        public void tick() {
            List<LivingEntity> entities = entity.level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(0.6, 0.2, 0.6),
                    (entity) -> (EntityUtil.checkCanEntityBeAttack(this.entity, entity) && ! entity.hasEffect(PVZMobEffects.FREEZE.get())));
            if (! entities.isEmpty() || this.entity.tickCount >= 100) {
                MobEffectInstance instance = new MobEffectInstance(PVZMobEffects.FREEZE.get(), 80);
                if (entity.hasSkill(RANGE_SKILL_NAME)) {
                    entities = entity.level.getEntitiesOfClass(LivingEntity.class, entity.getBoundingBox().inflate(2, 0.25, 2),
                            (entity) -> (entity instanceof LivingEntity && EntityUtil.checkCanEntityBeAttack(this.entity, entity)));
                    entities.forEach((entity) -> entity.addEffect(instance));
                } else if (! entities.isEmpty()) {
                    LivingEntity target = entities.get(0);
                    double targetDistance = target.position().distanceToSqr(entity.position());
                    for (LivingEntity i : entities) {
                        double tmp = i.position().distanceToSqr(entity.position());
                        if (i.position().distanceToSqr(entity.position()) < targetDistance) {
                            target = i;
                            targetDistance = tmp;
                        }
                    }
                    target.addEffect(instance);
                }
                ((ServerLevel) entity.level).sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY() + 0.2, entity.getZ(), entity.hasSkill(RANGE_SKILL_NAME) ? 60 : 20, 0.0D, 0.0D, 0.0D, entity.hasSkill(RANGE_SKILL_NAME) ? 0.2F : 0.1F);
                entity.discard();
                entity.playSound(PVZSoundEvents.ICEBERG_LETTUCE_EXPLODE.get(), 1.0F, 1.0F);
                return;
            }
            List<Player> players = entity.level.getNearbyPlayers(TargetingConditions.forNonCombat(), this.entity, entity.getBoundingBox().inflate(0.6, 0.2, 0.6));
            players = players.stream().filter(p -> EntityUtil.isTeammate(p, this.entity) && p.isOnFire()).toList();
            if (! players.isEmpty()) {
                if (entity.hasSkill(RANGE_SKILL_NAME)) {
                    players = entity.level.getNearbyPlayers(TargetingConditions.DEFAULT, this.entity
                            , entity.getBoundingBox().inflate(2, 0.25, 2));
                    players = players.stream().filter(p -> EntityUtil.isTeammate(p, this.entity) && p.isOnFire()).toList();
                    players.forEach(player -> {
                        player.clearFire();
                        player.setTicksFrozen(50);
                    });
                } else if (! players.isEmpty()) {
                    Player target = players.get(0);
                    double targetDistance = target.position().distanceToSqr(entity.position());
                    for (Player i : players) {
                        double tmp = i.position().distanceToSqr(entity.position());
                        if (i.position().distanceToSqr(entity.position()) < targetDistance) {
                            target = i;
                            targetDistance = tmp;
                        }
                    }
                    target.clearFire();
                    target.setTicksFrozen(50);
                }
                ((ServerLevel) entity.level).sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY() + 0.2, entity.getZ(), entity.hasSkill(RANGE_SKILL_NAME) ? 60 : 20, 0.0D, 0.0D, 0.0D, entity.hasSkill(RANGE_SKILL_NAME) ? 0.2F : 0.1F);
                entity.discard();
                entity.playSound(PVZSoundEvents.ICEBERG_LETTUCE_EXPLODE.get(), 1.0F, 1.0F);
            }
        }
    }
}
