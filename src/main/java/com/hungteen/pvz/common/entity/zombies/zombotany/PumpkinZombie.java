package com.hungteen.pvz.common.entity.zombies.zombotany;

import com.hungteen.pvz.api.interfaces.IArmorEntity;
import com.hungteen.pvz.api.interfaces.ICanBePlantedOn;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.Util;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomStrollGoal;
import net.minecraft.world.entity.ai.goal.ZombieAttackGoal;
import net.minecraft.world.entity.monster.Slime;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.UUID;

public class PumpkinZombie extends PVZZombie implements IZombotany, IArmorEntity, ICanBePlantedOn, ItemSteerable {
    private static final ResourceLocation STATE0 = Util.prefix("textures/entity/plants/pumpkin/pumpkin_0.png");
    private static final ResourceLocation STATE1 = Util.prefix("textures/entity/plants/pumpkin/pumpkin_1.png");
    private static final ResourceLocation STATE2 = Util.prefix("textures/entity/plants/pumpkin/pumpkin_2.png");

    private static final EntityDataAccessor<Integer> DATA_BOOST_TIME = SynchedEntityData.defineId(PumpkinZombie.class, EntityDataSerializers.INT);
    private static final EntityDataAccessor<Boolean> DATA_SADDLE_ID = SynchedEntityData.defineId(PumpkinZombie.class, EntityDataSerializers.BOOLEAN);
    public static final UUID TARGET_ATTRIBUTE_UUID = UUID.fromString("506f5a1b-375a-b955-1ac1-4424cabdabca");
    private final ItemBasedSteering steering = new ItemBasedSteering(this.entityData, DATA_BOOST_TIME, DATA_SADDLE_ID);

    public PumpkinZombie(EntityType<? extends Zombie> type, Level level) {
        super(type, level);
    }
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(DATA_SADDLE_ID, true);
        this.entityData.define(DATA_BOOST_TIME, 0);
    }
    public void addAdditionalSaveData(CompoundTag p_29495_) {
        super.addAdditionalSaveData(p_29495_);
        this.steering.addAdditionalSaveData(p_29495_);
    }
    public void readAdditionalSaveData(CompoundTag p_29478_) {
        super.readAdditionalSaveData(p_29478_);
        this.steering.readAdditionalSaveData(p_29478_);
    }
    public void tick() {
        super.tick();
        if (! this.getPassengers().isEmpty() && this.getFirstPassenger() instanceof Player player && (
                player.getMainHandItem().is(PVZItems.POP_SMARTS_ON_A_STICK.get()) || player.getOffhandItem().is(PVZItems.POP_SMARTS_ON_A_STICK.get())
                )) {
            EntityUtil.addModifierToAttribute(this, Attributes.FOLLOW_RANGE, new AttributeModifier(TARGET_ATTRIBUTE_UUID, "riden", -0.95, AttributeModifier.Operation.MULTIPLY_BASE));
        } else {
            EntityUtil.removeModifierFromAttribute(this, Attributes.FOLLOW_RANGE, TARGET_ATTRIBUTE_UUID);
        }
    }
    @Override
    protected void addBehaviourGoals() {
        super.addBehaviourGoals();
        attackGoal = new ZombieAttackGoal(this, 1.0D, false);
        randomStrollGoal = new RandomStrollGoal(this, 1.0D);
        this.goalSelector.addGoal(1, new PickUpPassengerGoal(this));
        this.targetSelector.addGoal(1, new PickUpPassengerGoal(this));
    }
    @Override
    public Vec3 getPlantHeadOffset() {
        return new Vec3(0, -0.05, 0);
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
        return (pos.y < 0 || pos.y * pos.y / (pos.x * pos.x + pos.z * pos.z) < 3);
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
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_29480_) {
        if (DATA_BOOST_TIME.equals(p_29480_) && this.level.isClientSide) {
            this.steering.onSynced();
        }

        super.onSyncedDataUpdated(p_29480_);
    }
    public boolean boost() {
        return this.steering.boost(this.getRandom());
    }
    public void travel(Vec3 p_29506_) {
        this.travel(this, this.steering, p_29506_);
    }

    public float getSteeringSpeed() {
        return (float)this.getAttributeValue(Attributes.MOVEMENT_SPEED) * 0.225F;
    }
    @Nullable
    public Entity getControllingPassenger() {
        Entity entity = this.getFirstPassenger();
        return entity != null && this.canBeControlledBy(entity) ? entity : null;
    }

    private boolean canBeControlledBy(Entity p_218248_) {
        if (p_218248_ instanceof Player player) {
            return player.getMainHandItem().is(PVZItems.POP_SMARTS_ON_A_STICK.get()) || player.getOffhandItem().is(PVZItems.POP_SMARTS_ON_A_STICK.get());
        } else {
            return false;
        }
    }

    public void travelWithInput(Vec3 p_29482_) {
        super.travel(p_29482_);
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
                    alt -> alt.getBbWidth() < 1 && ! alt.isPassenger() && ! (alt instanceof Slime) && EntityUtil.isTeammate(alt, entity) && alt != entity);
            if (! passengerAlts.isEmpty()) {
                passengerAlts.get(0).startRiding(entity);
            }
        }
    }
}