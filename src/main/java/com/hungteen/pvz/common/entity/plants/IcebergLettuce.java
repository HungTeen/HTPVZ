package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
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
            new Skill(SHOOTER_SKILL_NAME, PVZItems.VENTUS_ESSENCE, 8, 4, 0, 120),
            new Skill(RANGE_SKILL_NAME, PVZItems.GELUM_ESSENCE, 8, 4, 50, 120)
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
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    @Override
    public Set<TagKey<Block>> getAcceptableTags() {
        return Set.of(PVZBlockTags.PLANTABLE_DIRT, BlockTags.SNOW, BlockTags.ICE);
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
        if (! damageSource.isMagic() && ! EntityUtil.isTeammate(this, damageSource.getEntity())) {
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
        this.performShoot(0, 0, 0.2, false, 5);
    }
    @Override
    public double getMaxShootAngleTangent() {
        return Double.POSITIVE_INFINITY;
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
            List<Entity> entities = entity.level.getEntities(entity, entity.getBoundingBox().inflate(0.6, 0.2, 0.6),
                    (entity) -> (entity instanceof LivingEntity && EntityUtil.checkCanEntityBeAttack(this.entity, entity) && ! ((LivingEntity) entity).hasEffect(PVZMobEffects.FREEZE.get())));
            if (entities.isEmpty() && this.entity.tickCount < 300) {
                return;
            }
            MobEffectInstance instance = new MobEffectInstance(PVZMobEffects.FREEZE.get(), 120);
            if (entity.hasSkill(RANGE_SKILL_NAME)) {
                entities = entity.level.getEntities(entity, entity.getBoundingBox().inflate(2, 0.25, 2),
                        (entity) -> (entity instanceof LivingEntity && EntityUtil.checkCanEntityBeAttack(this.entity, entity)));
                entities.forEach((entity) -> ((LivingEntity) entity).addEffect(instance));
            } else {
                if (! entities.isEmpty()) {
                    ((LivingEntity) entities.get(0)).addEffect(instance);
                }
            }
            ((ServerLevel) entity.level).sendParticles(ParticleTypes.CLOUD, entity.getX(), entity.getY() + 0.2, entity.getZ(), entity.hasSkill(RANGE_SKILL_NAME) ? 60 : 20, 0.0D, 0.0D, 0.0D, entity.hasSkill(RANGE_SKILL_NAME) ? 0.2F : 0.1F);
            entity.discard();
        }
    }
}
