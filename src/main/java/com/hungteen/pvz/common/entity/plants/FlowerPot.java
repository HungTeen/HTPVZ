package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.api.interfaces.ICanBePlantedOn;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.common.entity.creatures.Snail;
import com.hungteen.pvz.common.entity.plants.base.SimplePlant;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZSeedPackets;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.vehicle.Boat;
import net.minecraft.world.entity.vehicle.Minecart;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.List;
import java.util.function.Predicate;

public class FlowerPot extends SimplePlant implements ICanBePlantedOn {
    public AnimationState idleAnimationState = new AnimationState();
    public static final String  FIRE_RESISTANCE_SKILL_NAME = "skill.pvz.flower_pot.refractory_ceramics";
    public static final String  FREE_SKILL_NAME = "skill.pvz.flower_pot.free_seat";
    public static final String  PORTABLE_SKILL_NAME = "skill.pvz.flower_pot.portable_pot";
    public static final String  CHINAWARE_SKILL_NAME = "skill.pvz.flower_pot.chinaware";
    public static List<Skill> staticSkillList = List.of(
            new Skill(FIRE_RESISTANCE_SKILL_NAME, PVZItems.IGNIS_ESSENCE, 6, 8, 75, PVZSeedPackets.MEDIUM - PVZSeedPackets.FAST),
            new Skill(FREE_SKILL_NAME, PVZItems.LUX_ESSENCE, 24, 12, -25, PVZSeedPackets.MEDIUM - PVZSeedPackets.FAST)
                    .avoidSkills(FIRE_RESISTANCE_SKILL_NAME, CHINAWARE_SKILL_NAME),
            new Skill(PORTABLE_SKILL_NAME, PVZItems.TERRA_ESSENCE, 14, 12, 25, PVZSeedPackets.SLOW - PVZSeedPackets.FAST),
            new Skill(CHINAWARE_SKILL_NAME, PVZItems.ORIGIN_ESSENCE, 24, 16, 0, 0)
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
    @Override
    public List<Skill> getBasicStaticSkillList(){
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
        } else if (! this.isVehicle() && ! this.idleAnimationState.isStarted()) {
            this.idleAnimationState.start(this.tickCount);
        }
        if (this.isVehicle() && this.hasSkill(this, FIRE_RESISTANCE_SKILL_NAME)) {
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
    public boolean plantableOn(BlockState blockState) {
        return true;
    }
    @Override
    public MutableComponent customVehicleSafe(PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        if (target == null) {
            return Component.translatable("hint.pvz.plant.entity_not_present");
        } else if (hasSkill(PORTABLE_SKILL_NAME) && (target instanceof Minecart || target instanceof Boat || (target instanceof Snail snail && snail.isTame()))) {
            if (isPlanting) {
                if (! target.isVehicle()) {
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
