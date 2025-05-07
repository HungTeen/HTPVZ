package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.IHangable;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.IEntityPacketHandler;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.Util;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;

public class UmbrellaLeaf extends SimplePlant implements IEntityPacketHandler {

    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState openAnimationState = new AnimationState();
    protected static final EntityDataAccessor<Boolean> POSE = SynchedEntityData.defineId(UmbrellaLeaf.class, EntityDataSerializers.BOOLEAN);
    public static final String FREE_SKILL_NAME = "skill.pvz.umbrella_leaf.a_skill_name_for_cheap_but_breakable_umbrella_leaf";
    public static final String BOUNCE_SKILL_NAME = "skill.pvz.umbrella_leaf.bounce_bounds_bonus";
    public static List<Skill> staticSkillList = List.of(
            new Skill(FREE_SKILL_NAME, PVZItems.LUX_ESSENCE, 8, 3, -75, -50),
            new Skill(BOUNCE_SKILL_NAME, PVZItems.VENTUS_ESSENCE, 8, 6, 50, 0)
    );

    public UmbrellaLeaf(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(POSE, false);
    }

    /** for players, another method in client side is used. See {@link #tick()}. */
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new UmbrellaLeafBounceGoal(this));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(1, new AttractEnemyGoal(this));
    }

    public boolean canBounce(Entity entity, boolean isClient) {
        AttributeInstance instance = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance != null && instance.getModifier(PVZMobEffects.BUTTER_EFFECT_UUID) != null) {
            return false;
        }
        if (entity.getType().is(Tags.EntityTypes.BOSSES) || ! this.isAlive()) {
            return false;
        }
        if (entity.getDeltaMovement().length() < 0.5 || entity.getDeltaMovement().subtract(this.getDeltaMovement()).length() < 0.5) {
            return false;
        }
        if (Util.hasBlockBetween(this.level, this.position(), entity.position())) {
            return false;
        }
        if (isClient) {
            return entity == ClientProxy.getPlayer() && ! entity.isShiftKeyDown();
        }
        Vec3 vec31 = entity.getDeltaMovement();
        Vec3 vec32 = this.position().subtract(entity.position());
        if (vec31.x * vec32.x + vec31.y * vec32.y + vec31.z * vec32.z < 0) {
            return false;
        }
        return ((entity instanceof LivingEntity && ! (entity instanceof Player)) || EntityUtil.checkCanEntityBeAttack(entity, this)
                || (entity instanceof Projectile && EntityUtil.checkCanEntityBeAttack(((Projectile) entity).getOwner(), this)));
    }

    @Override
    public void tick() {
        super.tick();
        if (hasSkill(FREE_SKILL_NAME)) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1D);
        }
        if (level.isClientSide) {
            float width = hasSkill(BOUNCE_SKILL_NAME) ? 3 : 1F;
            List<Entity> entities = this.level.getEntities(this, this.getBoundingBox().inflate(width, 1.5, width).move(0, 0.5, 0),
                    (entity) -> canBounce(entity, true));
            if (! entities.isEmpty()) {
                entities.forEach((entity1 -> {
                    Vec3 vec = entity1.getDeltaMovement();
                    double dist = Math.sqrt((entity1.getX() - getX()) * (entity1.getX() - getX()) + (entity1.getZ() - getZ()) * (entity1.getZ() - getZ()));
                    dist = dist == 0 ? 0.01 : dist;
                    entity1.setDeltaMovement(Math.max(-0.8, Math.min((entity1.getX() - this.getX()) / dist, 0.8)),
                            Math.max(Math.abs(vec.y), 0.35),
                            Math.max(-0.8, Math.min((entity1.getZ() - this.getZ()) / dist, 0.8)));
                    entity1.fallDistance = 0;
                }));
                sendPVZPacketToServer();
            }
        }
    }

    @Override
    public void onSyncedDataUpdated(EntityDataAccessor<?> p_219422_) {
        if (POSE.equals(p_219422_)) {
            if (entityData.get(POSE)) {
                this.idleAnimationState.stop();
                this.openAnimationState.start(this.tickCount);
            } else {
                this.openAnimationState.stop();
                this.idleAnimationState.start(this.tickCount);
            }
        }

        super.onSyncedDataUpdated(p_219422_);
    }
    public void setupPresentationAnim() {
        this.idleAnimationState.start(this.tickCount);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.FOLLOW_RANGE, 1D);// only for enemy attraction.
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    @Override
    public Predicate<Entity> canPush(){
        return entity -> true;
    }

    @Override
    public void handlePVZPacket(ServerPlayer player, int val) {
        if (this.getAttackTime() <= 20) {
            setAttackTime(30);
        }
        player.addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 5, 0, false, false));
    }

    private static class UmbrellaLeafBounceGoal extends Goal {
        UmbrellaLeaf entity;
        public UmbrellaLeafBounceGoal(UmbrellaLeaf entity) {
            super();
            this.entity = entity;
            this.setFlags(EnumSet.of(Flag.TARGET));
        }
        @Override
        public boolean canUse() {
            entity.setAttackTime(entity.getAttackTime() - 1);
            if (entity.getAttackTime() < 0) {
                entity.setAttackTime(19);
            }
            entity.getEntityData().set(POSE, entity.getAttackTime() > 20);
            if (entity.getAttackTime() == 22 && entity.hasSkill(FREE_SKILL_NAME)) {
                entity.discard();
            }
            return entity.getAttackTime() <= 20;
        }
        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
        @Override
        public void tick() {
            float width = entity.hasSkill(BOUNCE_SKILL_NAME) ? 5 : 3F;
            List<Entity> entities = entity.level.getEntities(entity, entity.getBoundingBox().inflate(width, 2.5, width),
                    (entity) -> this.entity.canBounce(entity, false));
            if (! entities.isEmpty()) {
                entities.forEach((entity1 -> {
                    Vec3 vec = entity1.getDeltaMovement();
                    entity.setDeltaMovement(0, 0.25, 0);
                    double dist = Math.sqrt((entity1.getX() - entity.getX()) * (entity1.getX() - entity.getX()) + (entity1.getZ() - entity.getZ()) * (entity1.getZ() - entity.getZ()));
                    dist = dist == 0 ? 0.01 : dist;
                    entity1.setDeltaMovement(Math.max(-0.8, Math.min((entity1.getX() - entity.getX()) / dist, 0.8)),
                            Math.max(Math.abs(vec.y), 0.35),
                            Math.max(-0.8, Math.min((entity1.getZ() - entity.getZ()) / dist, 0.8)));
                    entity1.fallDistance = 0;
                    Vec3 vec3 = entity1.getDeltaMovement().multiply(1, 0, 1).normalize();
                    entity1.setYRot((float) (vec3.z == 0 ? (vec3.x > 0 ? - Math.PI / 2 : Math.PI) : Math.atan(- vec3.x / vec3.z) + (vec3.z < 0 ? Math.PI : 0)) * 57.3F);
                    if (entity1 instanceof Projectile) {
                        ((Projectile) entity1).setOwner(entity);
                        if (entity1 instanceof AbstractHurtingProjectile projectile) {
                            vec3 = entity.getDeltaMovement().normalize();
                            projectile.setDeltaMovement(vec3);
                            projectile.xPower = vec3.x * 0.1D;
                            projectile.yPower = vec3.y * 0.1D;
                            projectile.zPower = vec3.z * 0.1D;
                            projectile.hurt(PVZEntityCapability.getOwner(entity) instanceof Player player ?
                                            DamageSource.playerAttack(player) : DamageSource.mobAttack(entity)
                                    , 0F);
                        }
                    } else if (entity1 instanceof IHangable hangable) {
                        hangable.setHangingPosition(null);
                    }
                }));
                entity.setAttackTime(30);
            }
        }
    }
}
