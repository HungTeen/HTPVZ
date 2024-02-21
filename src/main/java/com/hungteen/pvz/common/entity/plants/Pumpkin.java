package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.IArmorEntity;
import com.hungteen.pvz.api.interfaces.ICanBePlantedOn;
import com.hungteen.pvz.api.interfaces.IDefenderPlant;
import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.common.event.PVZResourceEvent;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.core.BlockPos;
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
            new Skill("skill.pvz.pumpkin.wall_nut_first_aid", PVZItems.ORIGIN_ESSENCE, 4, 4, 0, 0)
    );

    public Pumpkin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        storedHealth = 0;
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
        this.goalSelector.addGoal(1, new AttractEnemyGoal(this, false));
        this.goalSelector.addGoal(3, new AxisLookAroundGoal(this));
    }

    @Override
    public boolean canHold(LivingEntity plant, boolean isPlanting) {
        return ! (plant instanceof IArmorEntity) && (!isPlanting || getPassengers().isEmpty()) && PVZOwnedCapability.isTeammate(this, plant);
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.05;
    }
    @Override
    public MutableComponent isVehicleSafe(PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        if (isPlanting && event != null) {
            if (event.cost > PVZPlayerCapability.getValue(event.getEntity(), event.resource)) {
                return Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource));
            }
        }
        if (target == null) {
            if (! isPlanting) {
                this.boardingCooldown = 0;
                BlockPos onPos = this.getOnPos();
                List<Entity> list = level.getEntities(this, this.getBoundingBox().move(onPos.offset(-onPos.getX(), -onPos.getY() - 0.0001, -onPos.getZ())).inflate(0, 0.0001, 0),
                        (entity) -> entity instanceof IPlant && ((IPlant)entity).takesCoincideDmg() && this.getVehicle() != entity && entity.getVehicle() != this);
                if (this.getVehicle() == null) {
                    list.forEach((entity) -> {
                        if (this.getVehicle() == null && entity instanceof ICanBePlantedOn vehicle && vehicle.canHold(this, false)) {
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
        if (hasSkill(this, "skill.pvz.pumpkin.wall_nut_first_aid") && target.getClass() == this.getClass()) {
            if (PVZOwnedCapability.isTeammate(this, target)) {
                if (((Pumpkin) target).getHealth() > ((Pumpkin) target).getMaxHealth() * 0.67) {
                    return Component.translatable("hint.pvz.plant.pumpkin.not_broken");
                }
                if (isPlanting) {
                    moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
                    ((ServerLevel)this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()), this.getX(), this.getY(), this.getZ(), 5, 0.0D, 0.0D, 0.0D, 0.15F);
                    ((Pumpkin) target).convertTo(((EntityType<Mob>) this.getType()), true);
                    ((Pumpkin) target).setSkillVal(this.getSkillVal());
                    this.discard();
                }
                return null;
            }
            return Component.translatable("hint.pvz.plant.need_own_team");
        }
        if (target instanceof ICanBePlantedOn && ((ICanBePlantedOn) target).canHold(this, isPlanting)) {
            if (PVZOwnedCapability.isTeammate(this, target)) {
                if (! canMountEntity(this, target, this.getVehicle() == target)) {
                    return isPlanting && target.getFirstPassenger() != null ?
                            isVehicleSafe(event, target.getFirstPassenger(), true) :
                            Component.translatable("hint.pvz.plant.no_enough_place", this.getName());
                }
                if (isPlanting) {
                    this.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), 0.0F);
                    this.startRiding(target);
                }
                return null;
            }
            return Component.translatable("hint.pvz.plant.need_own_team");
        } else if (isPlanting && target instanceof LivingEntity livingEntity && this.canHold(livingEntity, true)){
            if (target.getVehicle() instanceof ICanBePlantedOn entityRiding && ((ICanBePlantedOn) target.getVehicle()).canHold(this, false)) {
                target.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                target.startRiding(this);
                this.moveTo(((Entity) entityRiding).getX(), ((Entity) entityRiding).getY(), ((Entity) entityRiding).getZ(),
                        ((Entity) entityRiding).getYRot(), 0.0F);
                this.startRiding((Entity) entityRiding);
                return null;
            } else if (target.getVehicle() == null) {
                target.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                target.startRiding(this);
                if (isPositionSafe(event, target.level, target.blockPosition().below(), Direction.UP, true) == null) {
                    return null;
                } else {
                    target.stopRiding();
                    return Component.translatable("hint.pvz.plant.no_enough_place", this.getName());
                }
            } else {
                return Component.translatable("hint.pvz.plant.no_enough_place", this.getName());
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
