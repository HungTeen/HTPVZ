package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.ICanBePlantedOn;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

import java.util.List;
import java.util.function.Predicate;

public class FlowerPot extends SimplePlant implements ICanBePlantedOn {
    public AnimationState idleAnimationState = new AnimationState();
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.flower_pot.refractory_ceramics", PVZItems.IGNIS_ESSENCE, 8, 6, 0, 0),
            new Skill("skill.pvz.flower_pot.free_seat", PVZItems.LUX_ESSENCE, 8, 4, -25, 140).avoidSkills(0),
            new Skill("skill.pvz.flower_pot.chinaware", PVZItems.ORIGIN_ESSENCE, 8, 16, 0, 0)
    );

    public FlowerPot(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.set(ROOT, false);
    }

    //entity settings
    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 8D)
                .add(Attributes.FOLLOW_RANGE, 2D);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    //overrides
    @Override
    public boolean canBeCollidedWith() {
        return true;
    }
    public double getPassengersRidingOffset() {
        return 0.45;
    }
    @Override
    public void tick() {
        super.tick();
        if (this.isVehicle() && this.idleAnimationState.isStarted()) {
            this.idleAnimationState.stop();
        } else if (! this.isVehicle() && ! this.idleAnimationState.isStarted()){
            this.idleAnimationState.start(this.tickCount);
        }
        if (this.isVehicle() && this.hasSkill(this, "skill.pvz.flower_pot.refractory_ceramics")) {
            this.fireImmune();
            this.getPassengers().forEach((Entity::clearFire));
            this.getPassengers().forEach((entity -> {
                if (entity instanceof LivingEntity e) {
                    e.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, 40), this);
                }
            }));
        }
    }

    public boolean fireImmune() {
        return super.fireImmune() || this.hasSkill(this, "skill.pvz.flower_pot.refractory_ceramics");
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AttractEnemyGoal(this));
        this.goalSelector.addGoal(3, new AxisLookAroundGoal(this));
    }

    @Override
    public Predicate<Entity> canPush(){
        return entity -> true;
    }

    @Override
    public MutableComponent isVehicleSafe(Entity target, boolean isPlanting) {
        if (target == null) {
            return Component.translatable("hint.pvz.plant.entity_not_present");
        }
        return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), target.getName());
    }
}
