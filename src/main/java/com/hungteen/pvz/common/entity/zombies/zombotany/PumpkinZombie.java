package com.hungteen.pvz.common.entity.zombies.zombotany;

import com.hungteen.pvz.api.interfaces.IArmorEntity;
import com.hungteen.pvz.api.interfaces.ICanBePlantedOn;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class PumpkinZombie extends PVZZombie implements IZombotany, IArmorEntity, ICanBePlantedOn{
    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/pumpkin/pumpkin_0.png");
    private static final ResourceLocation STATE1 = Util.prefix("textures/entity/plants/pumpkin/pumpkin_1.png");
    private static final ResourceLocation STATE2 = Util.prefix("textures/entity/plants/pumpkin/pumpkin_2.png");

    public PumpkinZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }
    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        attackGoal = new ZombieAttackGoal(this, 1.0D, false);
        randomStrollGoal = new RandomStrollGoal(this, 1.0D);
        this.goalSelector.addGoal(1, new PickUpPassengerGoal(this));
    }
    @Override
    public boolean canHold(LivingEntity plant, boolean isPlanting) {
        if (isPlanting) return false;
        return ! (plant instanceof ICanBePlantedOn)
                && (!isPassenger() || ! (this.getVehicle() instanceof ICanBePlantedOn vehicle) || vehicle.canHold(plant, false, true))
                && plant.getBbWidth() <= 1;
    }
    @Override
    public boolean canRecieveDamage(DamageSource source, double amount, Entity target) {
        Entity entity = source.getDirectEntity();
        if (source.isBypassArmor() || entity == null) return false;
        Vec3 pos = entity.position().subtract(target.position());
        return (pos.y * pos.y / (pos.x * pos.x + pos.z * pos.z) < 3);
    }
    @Override
    public void setSecondsOnFire(int seconds) {
        super.setSecondsOnFire(seconds * 3);//balance test.
    }
    @Override
    public ResourceLocation getPlantTextureLocation() {
        float healthPercent = this.getHealth() / this.getMaxHealth();
        return healthPercent > 0.67 ? STATE0 : (healthPercent > 0.33 ? STATE1 : STATE2);
    }

    @Override
    public EntityType<?> getPlantType() {
        return PVZEntities.PUMPKIN.get();
    }

    public static AttributeSupplier.@NotNull Builder createAttributes() {
        return Zombie.createAttributes()
                .add(Attributes.MAX_HEALTH, 30D)
                .add(Attributes.ARMOR, 25D)
                .add(Attributes.ARMOR_TOUGHNESS, 5D);
    }

    public static class PickUpPassengerGoal extends Goal {
        Entity entity;
        public PickUpPassengerGoal(Entity entity) {
            this.entity = entity;
        }
        @Override
        public boolean canUse() {
            if (entity.getFirstPassenger() instanceof LivingEntity passenger && ! EntityUtil.isTeammate(entity, passenger)) {
                passenger.stopRiding();
            }
            return entity.tickCount % 40 < 2 && entity.getPassengers().isEmpty();
        }
        public void tick() {
            List<LivingEntity> passengerAlts = entity.level.getEntitiesOfClass(LivingEntity.class, this.entity.getBoundingBox().inflate(0.5F),
                    alt -> alt.getBbWidth() <= 1 && ! alt.isPassenger() && EntityUtil.isTeammate(alt, entity) && alt != entity);
            if (! passengerAlts.isEmpty()) {
                passengerAlts.get(0).startRiding(entity);
            }
        }
    }
}