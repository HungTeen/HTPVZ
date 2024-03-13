package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.entity.Anger;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import java.util.List;
import java.util.Set;

import static com.hungteen.pvz.common.register.PVZDamageSource.teamFilter;

public class Jalapeno extends SimplePlant {
    public AnimationState idleAnimationState = new AnimationState();
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.jalapeno.tracking_fire", PVZItems.IGNIS_ESSENCE, 4, 4, 50, 0)
    );

    public Jalapeno(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.idleAnimationState.start(this.tickCount);
        this.entityData.set(root(), false);
    }
    @Override
    public boolean fireImmune() {
        return true;
    }
    public void explode() {
        level.explode(this, teamFilter(DamageSource.explosion(this).bypassArmor()), null, this.getX(), this.getY() + 1, this.getZ(),
                1F, false, Explosion.BlockInteraction.NONE);
        if (! level.isClientSide) {
            for (Direction direction : List.of(Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH)) {
                Anger anger = new Anger(level);
                anger.setPos(this.position().add(0, 1, 0));
                anger.getCapability(PVZOwnedCapability.CAP).orElse(null).setOwner(this);
                anger.yRot = direction.toYRot();
                level.addFreshEntity(anger);
                if (this.hasSkill("skill.pvz.jalapeno.tracking_fire")) {
                    anger.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(this.getAttributeValue(Attributes.ATTACK_DAMAGE) / 3);
                    anger.getAttribute(Attributes.FLYING_SPEED).setBaseValue(0.6F);
                    anger.maxLife = 150;
                } else {
                    anger.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(this.getAttributeValue(Attributes.ATTACK_DAMAGE));
                    anger.targetSelector.disableControlFlag(Goal.Flag.TARGET);
                }
            }
        }
        this.discard();
    }

    //entity settings
    public static AttributeSupplier.Builder createAttributes() {
        return ShooterPlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 8D)
                .add(Attributes.FOLLOW_RANGE, 6D)
                .add(Attributes.ATTACK_DAMAGE, 25.0D);
    }
    @Override
    public void baseTick() {
        super.baseTick();
        level.addParticle(ParticleTypes.LAVA, getX(), getY(), getZ(),
                random.nextFloat() * 0.15 - 0.075, random.nextFloat() * 0.15, random.nextFloat() * 0.15 - 0.075);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    @Override
    public Set<TagKey<Block>> getAcceptableTags() {
        return Set.of(PVZBlockTags.PLANTABLE_DIRT, BlockTags.SNOW);
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new JalapenoExplodeGoal(this));
    }

    public static class JalapenoExplodeGoal extends Goal{
        Jalapeno jalapeno;
        public JalapenoExplodeGoal(Jalapeno jalapeno) {
            this.jalapeno = jalapeno;
        }
        @Override
        public boolean canUse() {
            return jalapeno.tickCount > 40;
        }

        @Override
        public void tick() {
            jalapeno.explode();
        }
    }
}
