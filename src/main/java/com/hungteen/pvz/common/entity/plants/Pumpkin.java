package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.*;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

import static net.minecraftforge.event.ForgeEventFactory.canMountEntity;

public class Pumpkin extends SimplePlant implements IDefenderPlant, IArmorEntity, ICanBePlantedOn {
    float storedHealth;
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.pumpkin.wall_nut_first_aid", PVZItems.LUX_ESSENCE, 4, 4, 0, 0)
    );

    public Pumpkin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        storedHealth = 0;
    }

    public void setupPresentationAnim() {
    }

    //entity settings
    public void setSecondsOnFire(int seconds) {
        super.setSecondsOnFire(seconds * 3);//balance test.
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 30D)
                .add(Attributes.ARMOR, 25D)
                .add(Attributes.ARMOR_TOUGHNESS, 20D)
                .add(Attributes.FOLLOW_RANGE, 2D);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AttractEnemyGoal(this));
        this.goalSelector.addGoal(3, new AxisLookAroundGoal(this));
    }

    @Override
    public boolean canHold(LivingEntity plant, boolean isPlanting) {
        return ! (plant instanceof ICanBePlantedOn) &&
                (! isPlanting || getPassengers().isEmpty()) &&
                (! isPassenger() || ! (this.getVehicle() instanceof ICanBePlantedOn vehicle) || (vehicle.canHold(plant, isPlanting))) &&
                plant.getBbWidth() < 1;
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.05;
    }
    @Override
    public MutableComponent plantVehicleSafe(PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        //resource check.
        if (isPlanting && event != null) {
            if (event.cost > PVZPlayerCapability.getValue(event.getEntity(), event.resource)) {
                return Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource));
            }
        }
        //target unavailable.
        if (target == null) {
            if (! isPlanting) {
                //find rideable entity.
                if (this.getVehicle() == null) {
                    this.boardingCooldown = 0;
                    List<Entity> list = level.getEntities(this, this.getBoundingBox().inflate(0, 1, 0),
                            (entity) -> entity instanceof IPlant && ((IPlant)entity).takesCoincideDmg() && this.getVehicle() != entity && entity.getVehicle() != this);
                    list.forEach((entity) -> {
                        if (this.getVehicle() == null && entity instanceof ICanBePlantedOn vehicle && vehicle.canHold(this, false) && EntityUtil.isTeammate(this, entity)) {
                            this.startRiding(entity);
                        }
                    });
                    if (this.getVehicle() != null) {
                        return null;
                    }
                }
            }
            return Component.translatable("hint.pvz.plant.entity_not_present");
        }
        //first aid.
        if (hasSkill(this, "skill.pvz.pumpkin.wall_nut_first_aid") && target.getClass() == this.getClass()) {
            if (EntityUtil.isTeammate(this, target)) {
                if (((Pumpkin) target).getHealth() > ((Pumpkin) target).getMaxHealth() * 0.67) {
                    return Component.translatable("hint.pvz.plant.pumpkin.not_broken");
                }
                if (isPlanting) {
                    moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
                    ((ServerLevel)this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()), this.getX(), this.getY(), this.getZ(), 5, 0.0D, 0.0D, 0.0D, 0.15F);
                    target = ((Pumpkin) target).convertTo(((EntityType<Mob>) this.getType()), true);
                    if (target != null) {
                        ((Pumpkin) target).setSkillVal(this.getSkillVal());
                        if (event != null) {
                            target.getCapability(PVZOwnedCapability.CAP).ifPresent((cap) -> cap.setOwner(event.getEntity()));
                        }
                    }
                    this.discard();
                }
                return null;
            }
            return Component.translatable("hint.pvz.plant.need_own_team");
        }
        //target is ICanBePlantedOn.
        if (target instanceof ICanBePlantedOn && ((ICanBePlantedOn) target).canHold(this, isPlanting)) {
            if (EntityUtil.isTeammate(this, target)) {
                if (isPlanting) {
                    if (canMountEntity(this, target, true)) {
                        this.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), 0.0F);
                        this.startRiding(target);
                        return null;
                    } else {
                        return target.getFirstPassenger() != null ?
                                plantVehicleSafe(event, target.getFirstPassenger(), true) :
                                Component.translatable("hint.pvz.plant.no_enough_place");
                    }
                }
                return null;
            }
            return Component.translatable("hint.pvz.plant.need_own_team");
        // hold target entity.
        } else if (isPlanting && target instanceof LivingEntity livingEntity && this.canHold(livingEntity, true)) {
            //team check
            if (! EntityUtil.isTeammate(this, target)) {
                return Component.translatable("hint.pvz.plant.need_own_team");
            }
            //when target is planted on another vehicle.
            if (target.getVehicle() instanceof ICanBePlantedOn entityRiding && ((ICanBePlantedOn) target.getVehicle()).canHold(this, false)) {
                if (EntityUtil.isTeammate(this, target.getVehicle())) {
                    target.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                    target.startRiding(this);
                    this.moveTo(((Entity) entityRiding).getX(), ((Entity) entityRiding).getY(), ((Entity) entityRiding).getZ(),
                            ((Entity) entityRiding).getYRot(), 0.0F);
                    this.startRiding((Entity) entityRiding);
                    return null;
                } else {
                    return Component.translatable("hint.pvz.plant.need_own_team");
                }
            //target is not riding.
            } else if (target.getVehicle() == null) {
                target.startRiding(this);
                if (plantPositionSafe(event, target.level, target.getOnPos(), Direction.UP, true) == null &&
                        (((target instanceof IPlant) && ((IPlant) target).plantVehicleSafe(event, this, false) == null) ||
                                ! (target instanceof INeedSafeSituation) || ((INeedSafeSituation) target).isVehicleSafe(event, this, true) == null)) {
                    return null;
                } else {
                    return Component.translatable("hint.pvz.plant.no_enough_place");
                }
            } else {
                return Component.translatable("hint.pvz.plant.no_enough_place");
            }
        } else {
            return Component.translatable("hint.pvz.plant.cant_plant_on", this.getName(), target.getName());
        }
    }

    //overrides
    @Override
    public void baseTick() {
        super.baseTick();
        if (getHealth() < storedHealth && level.isClientSide()) {
            for (int i = 0; i < 3; i ++) {
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.PUMPKIN.defaultBlockState()).setPos(this.getOnPos()),
                    getX()+random.nextFloat() - 0.5, getY() + 1.1, getZ()+random.nextFloat() - 0.5, 0, 0, 0);
            }
        }
        storedHealth = getHealth();
    }

}
