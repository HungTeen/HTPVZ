package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.api.interfaces.IAttractsEnemy;
import com.hungteen.pvz.api.interfaces.IIronEntity;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.ai.goal.AxisLookAroundGoal;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;

import java.util.List;
import java.util.function.Predicate;

import static com.hungteen.pvz.common.register.PVZDamageSource.ignoreInvTime;
import static com.hungteen.pvz.common.register.PVZDamageSource.teamFilter;

public class WallNut extends SimplePlant implements IAttractsEnemy, IIronEntity {
    float storedHealth;
    float storedArmor;
    public static final EntityDataAccessor<Integer> EXPLODE_COUNT = SynchedEntityData.defineId(WallNut.class, EntityDataSerializers.INT);
    public static final EntityDataAccessor<Float> IRON_ARMOR = SynchedEntityData.defineId(WallNut.class, EntityDataSerializers.FLOAT);
    public static final EntityDataAccessor<Boolean> IS_BOWLING = SynchedEntityData.defineId(WallNut.class, EntityDataSerializers.BOOLEAN);
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.wall_nut.wall_nut_first_aid", PVZItems.LUX_ESSENCE, 4, 4, 0, 0),
            new Skill("skill.pvz.wall_nut.explode", PVZItems.IGNIS_ESSENCE, 4, 8, 150, 400),
            new Skill("skill.pvz.wall_nut.iron_armor", PVZItems.TERRA_ESSENCE, 4, 8, 50, 0).avoidSkills(1),
            new Skill("skill.pvz.wall_nut.elastic_collision", PVZItems.TERRA_ESSENCE, 4, 4, 50, 0).avoidSkills(2)
    );

    public WallNut(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
        storedHealth = 0;
        storedArmor = 0;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(EXPLODE_COUNT, -1);
        this.entityData.define(IRON_ARMOR, 0F);
        this.entityData.define(IS_BOWLING, false);
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
    public boolean isBowling() {
        return this.entityData.get(IS_BOWLING);
    }
    public boolean canBowling() {return true;}
    public void setBowling(boolean is_bowling) {
        this.entityData.set(IS_BOWLING, is_bowling);
    }

    //entity settings
    public void setSecondsOnFire(int seconds) {
        super.setSecondsOnFire(seconds * 3);//balance test.
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 40D)
                .add(Attributes.ARMOR, 25D)
                .add(Attributes.ARMOR_TOUGHNESS, 20D)
                .add(Attributes.ATTACK_DAMAGE, 50D)
                .add(Attributes.FOLLOW_RANGE, 2D);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    public void setupPresentationAnim() {
    }
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new AttractEnemyGoal(this));
        this.goalSelector.addGoal(1, new WallNutBowlingGoal(this));
        this.goalSelector.addGoal(3, new AxisLookAroundGoal(this));
    }

    @Override
    public Predicate<Entity> canPush(){
        return entity -> true;
    }

    @Override
    public MutableComponent plantVehicleSafe(PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        if (isPlanting && event != null) {
            if (event.cost > PVZPlayerCapability.getValue(event.getEntity(), event.resource)) {
                return Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource));
            }
        }
        if (hasSkill(this, "skill.pvz.wall_nut.wall_nut_first_aid") && target != null && target.getClass() == this.getClass()) {
            if (EntityUtil.isTeammate(this, target)) {
                if (((WallNut) target).getHealth() > ((WallNut) target).getMaxHealth() * 0.67) {
                    return Component.translatable("hint.pvz.plant.wall_nut.not_broken");
                }
                if (isPlanting) {
                    moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
                    ((ServerLevel)this.level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, this.level.getBlockState(this.getOnPos())).setPos(this.getOnPos()), this.getX(), this.getY(), this.getZ(), 5, 0.0D, 0.0D, 0.0D, 0.15F);
                    target = ((WallNut) target).convertTo(((EntityType<Mob>) this.getType()), true);
                    if (target != null) {
                        ((WallNut) target).setSkillVal(this.getSkillVal());
                        if (event != null) {
                            target.getCapability(PVZEntityCapability.CAP).ifPresent((cap) -> cap.setOwner(event.getEntity()));
                        }
                    }
                    this.discard();
                }
                return null;
            }
            return Component.translatable("hint.pvz.plant.need_own_team");
        }
        return super.plantVehicleSafe(event, target, isPlanting);
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

        if (this.hasSkill(this, "skill.pvz.wall_nut.explode") && this.getEntityData().get(EXPLODE_COUNT) > -1) {
            this.getEntityData().set(EXPLODE_COUNT, this.getEntityData().get(EXPLODE_COUNT) + 1);
            if (this.getEntityData().get(EXPLODE_COUNT) > 40) {
                this.explode();
            }
        }
        if (this.hasSkill(this, "skill.pvz.wall_nut.iron_armor") && getIronArmor() == 0) {
            setIronArmor(getMaxIronArmor());
        }
    }
    public void alignBlocks() {
        super.alignBlocks();
        this.setBowling(false);
    }

    private void explode() {
        if (!this.level.isClientSide) {
            this.dead = true;
            level.explode(this, ignoreInvTime(teamFilter(DamageSource.explosion(this).bypassArmor())), null, this.getX(), this.getY(), this.getZ(), 3F, false, Explosion.BlockInteraction.NONE);
            this.discard();
        }
    }

    @Override
    public void actuallyHurt(DamageSource dmgSource, float dmg) {
        super.actuallyHurt(dmgSource, dmg);
        if (this.hasSkill(this, "skill.pvz.wall_nut.explode") && this.getHealth() <= 0) {
            this.setHealth(0.1F);
            this.getEntityData().set(EXPLODE_COUNT, this.getEntityData().get(EXPLODE_COUNT) == -1 ? 0 : this.getEntityData().get(EXPLODE_COUNT));
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

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putDouble("IronArmor", getIronArmor());

    }
    @Override
    public void readAdditionalSaveData(CompoundTag tag){
        super.readAdditionalSaveData(tag);
        if (tag.contains("IronArmor")) {
            setIronArmor((float) tag.getDouble("IronArmor"));
        }
    }

    public static class WallNutBowlingGoal extends Goal {
        WallNut wallNut;
        Vec3 storedDeltaMovement;
        int wiltTick;
        int damageCooldown;
        public WallNutBowlingGoal(WallNut wallNut) {
            this.wallNut = wallNut;
            this.storedDeltaMovement = wallNut.getDeltaMovement();
            this.wiltTick = 0;
            this.damageCooldown = 0;
        }
        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }

        @Override
        public boolean canUse() {
            if (damageCooldown > 0) {
                damageCooldown -= 1;
            }
            if (wallNut.canBowling() && wallNut.getDeltaMovement().distanceToSqr(Vec3.ZERO) > 0.25) {
                wallNut.setBowling(true);
            }
            wallNut.entityData.set(TAKES_COINCIDE_DMG, ! wallNut.isBowling());
            wallNut.setDiscardFriction(wallNut.isBowling());
            if (! wallNut.isBowling()) {
                wiltTick = 0;
            }
            return wallNut.isBowling();
        }

        @Override
        public void tick() {
            List<Entity> entities = wallNut.level.getEntities(wallNut, wallNut.getBoundingBox().inflate(0.5, 0.5, 0.5),
                    (target) -> EntityUtil.checkCanEntityBeAttack(wallNut, target));
            if (! entities.isEmpty() && damageCooldown <= 0) {
                if (wallNut.hasSkill("skill.pvz.wall_nut.explode")) {
                    wallNut.explode();
                } else {
                    entities.forEach((entity -> {
                        entity.hurt(PVZDamageSource.wallNutCollide(this.wallNut), (float) this.wallNut.getAttributeValue(Attributes.ATTACK_DAMAGE));
                        damageCooldown = 5;
                        wallNut.hurt(PVZDamageSource.wallNutCollide(this.wallNut), 15);
                    }));
                }
                Vec3 deltaMovement = wallNut.getDeltaMovement();
                double angle = wallNut.random.nextFloat() * Math.PI;
                wallNut.setDeltaMovement(deltaMovement.x * Math.sin(angle) + deltaMovement.z * Math.cos(angle),
                        deltaMovement.y,
                        deltaMovement.z * Math.sin(angle) + deltaMovement.x * Math.cos(angle));
            } else if (wallNut.horizontalCollision) {
                wallNut.setDeltaMovement(wallNut.getDeltaMovement().add(
                        wallNut.getDeltaMovement().subtract(storedDeltaMovement).scale(wallNut.hasSkill("skill.pvz.wall_nut.elastic_collision") ? 1 : 0.5F)));
                wallNut.hurt(PVZDamageSource.wallNutCollide(this.wallNut), 10);
            }
            storedDeltaMovement = wallNut.getDeltaMovement();
            if (++ wiltTick % 20 == 0) {
                wallNut.hurt(PVZDamageSource.PLANT_WILT, 5);
            }
        }
    }
}
