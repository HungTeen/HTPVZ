package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.PVZAPI;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.creatures.Anger;
import com.hungteen.pvz.common.entity.plants.base.SimplePlant;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZSoundEvents;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.UUID;

/**For damaging related logic, see {@link Anger}.*/
public class Jalapeno extends SimplePlant {
    public AnimationState explodeAnimationState = new AnimationState();
    public static final String TRACK_SKILL_NAME = "skill.pvz.jalapeno.tracking_fire";
    public static final String NO_FRIENDLY_FIRE_SKILL_NAME = "skill.pvz.jalapeno.precise_strike";
    private static UUID SKILL_BOOST_UUID = UUID.fromString("42ec228b-586e-9369-8d0c-e336502daa20");
    public static List<Skill> staticSkillList = List.of(
            new Skill(TRACK_SKILL_NAME, PVZItems.IGNIS_ESSENCE, 4, 8, 200, 0),
            new Skill(NO_FRIENDLY_FIRE_SKILL_NAME, PVZItems.IGNIS_ESSENCE, 4, 8, 50, 0)
    );

    public Jalapeno(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        this.entityData.set(root(), false);
        this.explodeAnimationState.start(this.tickCount);
    }
    public void setupPresentationAnim() {
        this.explodeAnimationState.stop();
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    public void explode() {
        level.playSound(null, this.blockPosition(), PVZSoundEvents.JALAPENO_EXPLODE.get(), SoundSource.NEUTRAL, 1.0F, 1.0F);
        if (! level.isClientSide) {
            for (Direction direction : List.of(Direction.EAST, Direction.WEST, Direction.SOUTH, Direction.NORTH)) {
                Anger anger = new Anger(level);
                if (this.hasSkill(NO_FRIENDLY_FIRE_SKILL_NAME)) {
                    anger.friendlyFire = false;
                }
                anger.setPos(this.position().add(0, 1, 0));
                this.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> {
                    if (cap.getOwner() != null) {
                        anger.getCapability(PVZEntityCapability.CAP).ifPresent(angerCap -> angerCap.setOwner(cap.getOwner()));
                    }
                });
                anger.yRot = direction.toYRot();
                anger.getAttribute(Attributes.ATTACK_DAMAGE).setBaseValue(this.getAttributeValue(Attributes.ATTACK_DAMAGE) * PVZAPI.get().getPlantDamageDatum(this.level));
                if (this.hasSkill(TRACK_SKILL_NAME)) {
                    anger.maxLife = 150;
                    anger.getAttribute(Attributes.FLYING_SPEED).setBaseValue(0.6F);
                } else {
                    anger.targetSelector.disableControlFlag(Goal.Flag.TARGET);
                    anger.getAttribute(Attributes.FLYING_SPEED).setBaseValue(1F);
                }
                level.addFreshEntity(anger);
            }
        }
        this.discard();
    }

    //entity settings
    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 6D)
                .add(Attributes.ATTACK_DAMAGE, 25D);
    }
    @Override
    public void tick() {
        super.tick();
        if (EntityUtil.attributeHasModifierOfUUID(this, Attributes.ATTACK_DAMAGE, SKILL_BOOST_UUID)) {
            EntityUtil.addModifierToAttribute(this, Attributes.ATTACK_DAMAGE, new AttributeModifier(SKILL_BOOST_UUID, "skill_boost", -16, AttributeModifier.Operation.ADDITION));
        }
        if (level.isClientSide) {
            level.addParticle(ParticleTypes.LAVA, getX(), getY(), getZ(),
                    random.nextFloat() * 0.15 - 0.075, random.nextFloat() * 0.15, random.nextFloat() * 0.15 - 0.075);
        }
    }
    @Override
    public void die(DamageSource damageSource) {
        if (! damageSource.isMagic() && ! EntityUtil.isTeammate(this, damageSource.getEntity())) {
            this.explode();
        }
        super.die(damageSource);
    }
    @Override
    public List<Skill> getBasicStaticSkillList(){
        return staticSkillList;
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