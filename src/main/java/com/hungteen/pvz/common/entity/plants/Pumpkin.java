package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.*;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.common.ForgeHooks;

import java.util.List;
import java.util.function.Predicate;

import static com.hungteen.pvz.common.world.PVZDamageSource.teamFilter;
import static net.minecraftforge.event.ForgeEventFactory.canMountEntity;

public class Pumpkin extends SimplePlant implements IDefenderPlant, IIronEntity, IArmorEntity, ICanBePlantedOn {
    float storedHealth;
    float storedArmor;
    public static final EntityDataAccessor<Float> IRON_ARMOR = SynchedEntityData.defineId(Pumpkin.class, EntityDataSerializers.FLOAT);
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.wall_nut.wall_nut_first_aid", PVZItems.ORIGIN_ESSENCE, 4, 4, 0, 0),
            new Skill("skill.pvz.wall_nut.iron_armor", PVZItems.TERRA_ESSENCE, 4, 8, 50, 0).avoidSkills(1)
    );

    public Pumpkin(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        storedHealth = 0;
        storedArmor = 0;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(IRON_ARMOR, 0F);
    }

    //about iron armor
    public boolean isIronMaterial() {
        return hasIronArmor();
    }
    public boolean hasIronArmor() {
        return entityData.get(IRON_ARMOR) > 0;
    }
    public float getIronArmor() {
        return entityData.get(IRON_ARMOR);
    }
    public void setIronArmor(float value) {
        entityData.set(IRON_ARMOR, value);
    }
    public float getMaxIronArmor() {
        return 200;
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
    public Predicate<Entity> canPush(){
        return entity -> true;
    }

    @Override
    public boolean canHold(LivingEntity plant) {
        return ! (plant instanceof IArmorEntity);
    }

    @Override
    public double getPassengersRidingOffset() {
        return 0.05;
    }
    @Override
    public MutableComponent isVehicleSafe(Entity target, boolean isPlanting) {
        if (target == null) {
            return Component.translatable("hint.pvz.plant.entity_not_present");
        }
        if (hasSkill(this, "skill.pvz.wall_nut.wall_nut_first_aid") && target != null && target.getClass() == this.getClass()) {
            if (PVZOwnedCapability.isTeammate(this, target)) {
                if (((Pumpkin) target).getHealth() > ((Pumpkin) target).getMaxHealth() * 0.67) {
                    return Component.translatable("hint.pvz.plant.wall_nut.not_broken");
                }
                if (isPlanting) {
                    moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
                    yBodyRot = ((Pumpkin) target).yBodyRot;
                    if (target.hasCustomName()) {
                        setCustomName(target.getCustomName());
                        setCustomNameVisible(target.isCustomNameVisible());
                    }
                    setInvulnerable(target.isInvulnerable());
                    target.discard();
                }
                return null;
            }
            return Component.translatable("hint.pvz.plant.need_own_team");
        }
        if (target instanceof ICanBePlantedOn && ((ICanBePlantedOn) target).canHold(this)) {
            if (PVZOwnedCapability.isTeammate(this, target)) {
                if (! canMountEntity(this, target, this.getVehicle() == target)) {
                    return isPlanting && target.getFirstPassenger() != null ?
                            isVehicleSafe(target.getFirstPassenger(), true) :
                            Component.translatable("hint.pvz.plant.no_enough_place", this.getName());
                }
                if (isPlanting) {
                    this.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), 0.0F);
                    this.startRiding(target);
                }
                return null;
            }
            return Component.translatable("hint.pvz.plant.need_own_team");
        } else if (isPlanting && target instanceof LivingEntity livingEntity && this.canHold(livingEntity)){
            if (target.getVehicle() instanceof ICanBePlantedOn entityRiding && ((ICanBePlantedOn) target.getVehicle()).canHold(this)) {
                target.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                target.startRiding(this);
                this.moveTo(((Entity) entityRiding).getX(), ((Entity) entityRiding).getY(), ((Entity) entityRiding).getZ(),
                        ((Entity) entityRiding).getYRot(), 0.0F);
                this.startRiding((Entity) entityRiding);
                return null;
            } else if (target.getVehicle() == null) {
                target.moveTo(this.getX(), this.getY(), this.getZ(), this.getYRot(), 0.0F);
                target.startRiding(this);
                if (isPositionSafe(target.level, target.blockPosition().below(), true) == null) {
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
            level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.BIRCH_PLANKS.defaultBlockState()).setPos(this.getOnPos()),
                    getX()+random.nextFloat() - 0.5, getY() + 1.1, getZ()+random.nextFloat() - 0.5, 0, 0, 0);
            }
        }
        storedHealth = getHealth();
        if (getIronArmor() < storedArmor && level.isClientSide()) {
            for (int i = 0; i < 3; i ++) {
                level.addParticle(new BlockParticleOption(ParticleTypes.BLOCK, Blocks.ANVIL.defaultBlockState()).setPos(this.getOnPos()),
                        getX()+random.nextFloat() - 0.5, getY() + 1.1, getZ()+random.nextFloat() - 0.5, 0, 0, 0);
            }
        }
        storedArmor = getIronArmor();
        if (this.hasSkill(this, "skill.pvz.wall_nut.iron_armor") && getIronArmor() == 0) {
            setIronArmor(getMaxIronArmor());
        }
    }


    @Override
    public boolean hurt(DamageSource dmgSource, float dmgNum) {
        if (!ForgeHooks.onLivingAttack(this, dmgSource, dmgNum)) return false;
        if (this.isInvulnerableTo(dmgSource)) {
            return false;
        } else if (this.level.isClientSide) {
            return false;
        } else if (this.isDeadOrDying()) {
            return false;
        } else if (dmgSource.isFire() && this.hasEffect(MobEffects.FIRE_RESISTANCE)) {
            return false;
        }
        if (dmgNum > 0 && !dmgSource.isBypassArmor()) {
            if (this.hasIronArmor()) {
                double blocked = Math.min(dmgNum, this.getIronArmor());
                setIronArmor((float) (getIronArmor() - blocked));
                dmgNum -= blocked;
                if (getIronArmor() <= 0) {
                    setIronArmor(-1);
                }
            }
        }
        return super.hurt(dmgSource, dmgNum);
    }
}
