package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.IEntityPacketHandler;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

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
        if (entity.getType().is(Tags.EntityTypes.BOSSES) || entity.getDeltaMovement().length() < 0.5 || ! this.isAlive()) {
            return false;
        }
        BlockHitResult blockedCheck = level.clip(new ClipContext(position(), entity.position(), ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, this));
        if (blockedCheck.getType() != HitResult.Type.MISS) {
            return false;
        }
        if (isClient) {
            return entity instanceof Player;
        }
        return (entity instanceof LivingEntity || EntityUtil.checkCanEntityBeAttack(entity, this)
                || (entity instanceof Projectile && EntityUtil.checkCanEntityBeAttack(((Projectile) entity).getOwner(), this)));
    }

    @Override
    public void tick() {
        super.tick();
        if (hasSkill(FREE_SKILL_NAME)) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1D);
        }
        if (level.isClientSide) {
            int width = hasSkill(BOUNCE_SKILL_NAME) ? 4 : 2;
            List<Entity> entities = this.level.getEntities(this, this.getBoundingBox().inflate(width, 1.5, width).move(0, 0.5, 0),
                    (entity) -> canBounce(entity, true));
            if (! entities.isEmpty()) {
                entities.forEach((entity1 -> {
                    Vec3 vec = entity1.getDeltaMovement();
                    entity1.setDeltaMovement(Math.max(-0.8, Math.min(0.8 / (entity1.getX() - this.getX()), 0.8)),
                            Math.max(Math.abs(vec.y) * 0.8, 0.35),
                            Math.max(-0.8, Math.min(0.8 / (entity1.getZ() - this.getZ()), 0.8)));
                    PVZMod.LOGGER.info(entity1.getX() - this.getX() + " " + Math.min(0.5 / (entity1.getX() - this.getX()), 0.5));
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
            int width = entity.hasSkill(BOUNCE_SKILL_NAME) ? 4 : 2;
            List<Entity> entities = entity.level.getEntities(entity, entity.getBoundingBox().inflate(width, 1.5, width).move(0, 0.5, 0),
                    (entity) -> this.entity.canBounce(entity, false));
            if (! entities.isEmpty()) {
                entities.forEach((entity1 -> {
                    Vec3 vec = entity1.getDeltaMovement();
                    entity.setDeltaMovement(0, 0.25, 0);
                    entity1.setDeltaMovement(Math.max(-0.8, Math.min(0.8 / (entity1.getX() - entity.getX()), 0.8)),
                            Math.max(Math.abs(vec.y) * 0.8, 0.35),
                            Math.max(-0.8, Math.min(0.8 / (entity1.getZ() - entity.getZ()), 0.8)));
                    entity1.fallDistance = 0;
                    if (entity1 instanceof LivingEntity) {
                        ((LivingEntity) entity1).addEffect(new MobEffectInstance(MobEffects.SLOW_FALLING, 5, 0, false, false));
                    } else if (entity1 instanceof Projectile) {
                        ((Projectile) entity1).setOwner(entity);
                    }
                }));
                entity.setAttackTime(30);
            }
        }
    }
}
