package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.IHangable;
import com.hungteen.pvz.common.entity.IEntityPacketHandler;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.entity.plants.base.SimplePlant;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.common.register.PVZSeedPackets;
import com.hungteen.pvz.common.register.PVZSoundEvents;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.Util;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.AbstractHurtingProjectile;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

import java.util.EnumSet;
import java.util.List;

public class UmbrellaLeaf extends SimplePlant implements IEntityPacketHandler {

    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState openAnimationState = new AnimationState();
    public static final String FREE_SKILL_NAME = "skill.pvz.umbrella_leaf.a_skill_name_for_cheap_but_breakable_umbrella_leaf";
    public static final String BOUNCE_SKILL_NAME = "skill.pvz.umbrella_leaf.bounce_bounds_bonus";
    public static List<Skill> staticSkillList = List.of(
            new Skill(FREE_SKILL_NAME, PVZItems.LUX_ESSENCE, 8, 1, -75, PVZSeedPackets.VERY_FAST - PVZSeedPackets.MEDIUM),
            new Skill(BOUNCE_SKILL_NAME, PVZItems.VENTUS_ESSENCE, 8, 9, 50, 0)
    );

    public UmbrellaLeaf(EntityType<? extends Mob> entityType, Level level) {
        super(entityType, level);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
    }

    /** for players, another method in client side is used. See {@link #tick()}. */
    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(1, new UmbrellaLeafBounceGoal(this));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(1, new AttractEnemyGoal(this));
    }

    public boolean canBounce(Entity target, boolean isClient) {
        if (target == this) return false;
        AttributeInstance instance = this.getAttribute(Attributes.MOVEMENT_SPEED);
        if (instance != null && instance.getModifier(PVZMobEffects.BUTTER_EFFECT_UUID) != null) {
            return false;
        }
        if (target.getType().is(Tags.EntityTypes.BOSSES) || ! this.isAlive()) {
            return false;
        }
        if (Util.hasBlockBetween(this.level, this.position(), target.position())) {
            return false;
        }
        if (isClient) {
            if (target.getDeltaMovement().length() < 0.45 || target.getDeltaMovement().subtract(this.getDeltaMovement()).length() < 0.45) {
                return false;
            }
            return (! EntityUtil.isTeammate(target, this)) || target.isCrouching();
        }
        if (target.getDeltaMovement().length() < 0.5 || target.getDeltaMovement().subtract(this.getDeltaMovement()).length() < 0.5) {
            return false;
        }
        Vec3 vec31 = target.getDeltaMovement();
        Vec3 vec32 = this.position().subtract(target.position());
        if (vec31.x * vec32.x + vec31.y * vec32.y + vec31.z * vec32.z < 0) {
            return false;
        }
        return ((target instanceof LivingEntity && ! (target instanceof Player)) || EntityUtil.checkCanEntityBeAttack(target, this)
                || (target instanceof Projectile proj && EntityUtil.checkCanEntityBeAttack(proj.getOwner(), this)));
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
                    (entity) -> entity == ClientProxy.getPlayer() && canBounce(entity, true));
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
        if (ATTACK_TIME.equals(p_219422_)) {
            if (entityData.get(ATTACK_TIME) == 30) {
                this.openAnimationState.stop();
                this.idleAnimationState.stop();
                playSound(PVZSoundEvents.UMBRELLA_LEAF_BOUNCE.get(), 1.0F, 1.0F);
                this.openAnimationState.start(this.tickCount);
            } else if (entityData.get(ATTACK_TIME) < 20 && ! this.idleAnimationState.isStarted()) {
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
    public List<Skill> getBasicStaticSkillList(){
        return staticSkillList;
    }

    @Override
    public void handlePVZPacket(ServerPlayer player, int val) {
        if (this.getAttackTime() <= 24) {
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
            if (entity.getAttackTime() == 25 && entity.hasSkill(FREE_SKILL_NAME)) {
                entity.discard();
            }
            return entity.getAttackTime() <= 24;
        }
        @Override
        public boolean requiresUpdateEveryTick() {
            return true;
        }
        @Override
        public void tick() {
            float width = entity.hasSkill(BOUNCE_SKILL_NAME) ? 6 : 2.5F;
            List<Entity> entities = entity.level.getEntities(entity, entity.getBoundingBox().inflate(width, 2.5, width),
                    (entity) -> this.entity.canBounce(entity, false));
            if (! entities.isEmpty()) {
                entities.forEach((entity1 -> {
                    if (entity1 instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0) {
                        arrow.setPierceLevel((byte) (arrow.getPierceLevel() - 1));
                    } else {
                        Vec3 vec = entity1.getDeltaMovement();
                        entity.setDeltaMovement(0, 0.25, 0);
                        double dist = Math.sqrt((entity1.getX() - entity.getX()) * (entity1.getX() - entity.getX()) + (entity1.getZ() - entity.getZ()) * (entity1.getZ() - entity.getZ()));
                        dist = dist == 0 ? 0.01 : dist;
                        entity1.setDeltaMovement(Math.max(-0.8, Math.min((entity1.getX() - entity.getX()) / dist, 0.8)),
                                Math.max(Math.abs(vec.y), 0.35),
                                Math.max(-0.8, Math.min((entity1.getZ() - entity.getZ()) / dist, 0.8)));
                        entity1.fallDistance = 0;
                        Vec3 vec3 = entity1.getDeltaMovement().normalize();
                        Vec2 vec2 = new Vec2((float) vec3.x, (float) vec3.z).normalized();
                        entity1.setYRot((float) (vec2.y == 0 ? (vec2.x > 0 ? - Math.PI / 2 : Math.PI) : Math.atan(- vec2.x / vec2.y) + (vec2.y < 0 ? Math.PI : 0)) * 57.3F);
                        entity1.hasImpulse = true; //to let server sync entity motions.
                        if (entity1 instanceof Projectile proj) {
                            proj.setOwner(entity);
                            if (proj instanceof AbstractHurtingProjectile hProj) {
                                hProj.xPower = vec3.x * 0.12D;
                                hProj.yPower = vec3.y * 0.12D;
                                hProj.zPower = vec3.z * 0.12D;
                            }
                        } else if (entity1 instanceof IHangable hangable) {
                            hangable.setHangingPosition(null);
                        }
                    }
                }));
                entity.setAttackTime(30);
            }
        }
    }
}
