package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.ICanBePlantedOn;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.tags.PVZEntityTags;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;
import net.minecraftforge.fluids.FluidType;

import java.util.List;
import java.util.function.Predicate;

public class FlowerPot extends SimplePlant implements ICanBePlantedOn {
    public AnimationState idleAnimationState = new AnimationState();
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.flower_pot.refractory_ceramics", PVZItems.IGNIS_ESSENCE, 8, 6, 50, 0),
            new Skill("skill.pvz.flower_pot.free_seat", PVZItems.LUX_ESSENCE, 8, 4, -25, 140).avoidSkills(0, 3),
            new Skill("skill.pvz.flower_pot.portable_pot", PVZItems.TERRA_ESSENCE, 4, 4, 75, 440),
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
                .add(Attributes.MAX_HEALTH, 8D);
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
        this.shouldAlign = false;
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
    public void setupPresentationAnim() {
        this.idleAnimationState.start(this.tickCount);
    }

    public boolean fireImmune() {
        return super.fireImmune() || this.hasSkill(this, "skill.pvz.flower_pot.refractory_ceramics");
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AttractEnemyGoal(this, () -> this.getFirstPassenger() == null, 2));
        this.goalSelector.addGoal(3, new AxisLookAroundGoal(this));
    }

    @Override
    public Predicate<Entity> canPush(){
        return entity -> true;
    }

    @Override
    public double getMyRidingOffset() {
        return (getVehicle() instanceof Minecart ? 0.27 : getVehicle() instanceof Boat ? 0.2 : 0) + super.getMyRidingOffset();
    }
    @Override
    public boolean rideableUnderWater() {
        return true;
    }

    @Override
    public MutableComponent plantVehicleSafe(PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        if (target == null) {
            return Component.translatable("hint.pvz.plant.entity_not_present");
        } else if (hasSkill("skill.pvz.flower_pot.portable_pot") && (target instanceof Minecart || target instanceof Boat)) {
            if (isPlanting) {
                if (!target.isVehicle()) {
                    if (event != null) {
                        if (event.cost > PVZPlayerCapability.getValue(event.getEntity(), event.resource)) {
                            return Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource));
                        }
                    }
                    this.startRiding(target);
                    return null;
                } else {
                    return Component.translatable("hint.pvz.plant.no_enough_place");
                }
            } else {
                return null;
            }
        }
        return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), target.getName());
    }
}
