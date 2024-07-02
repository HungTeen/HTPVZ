package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.entity.IEntityPacketHandler;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.EntityUtil;
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
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.Tags;

import java.util.List;
import java.util.function.Predicate;

public class UmbrellaLeaf extends SimplePlant implements IEntityPacketHandler {

    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState openAnimationState = new AnimationState();
    protected static final EntityDataAccessor<Boolean> POSE = SynchedEntityData.defineId(UmbrellaLeaf.class, EntityDataSerializers.BOOLEAN);
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.umbrella_leaf.a_skill_name_for_cheap_but_breakable_umbrella_leaf", PVZItems.LUX_ESSENCE, 8, 3, -75, -50)
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
        Vec3 subPosition = entity.position().subtract(this.position()).multiply(1, 0, 1);
        if (subPosition.distanceToSqr(Vec3.ZERO) > 1) {
            Vec3i detectDirection = new Vec3i(
                    Math.abs(subPosition.x) < 0.5 ? 0 : subPosition.x > 0 ? 1 : -1, 0, Math.abs(subPosition.z) < 0.5 ? 0 : subPosition.z > 0 ? 1 : -1);
            if (level.getBlockState(this.blockPosition().offset(detectDirection)).isSuffocating(level, this.blockPosition().offset(detectDirection))) {
                return false;
            }
            if (subPosition.distanceToSqr(Vec3.ZERO) > 4 && detectDirection.getX() != 0 && detectDirection.getZ() != 0) {
                if (level.getBlockState(this.blockPosition().offset(detectDirection).offset(detectDirection.getX(), 0, 0))
                        .isSuffocating(level, this.blockPosition().offset(detectDirection)) &&
                        level.getBlockState(this.blockPosition().offset(detectDirection).offset(0, 0, detectDirection.getZ()))
                                .isSuffocating(level, this.blockPosition().offset(detectDirection))) {
                    return false;
                }
            }
        }
        return ! isClient ? (entity instanceof LivingEntity || EntityUtil.checkCanEntityBeAttack(entity, this)
                || (entity instanceof Projectile && EntityUtil.checkCanEntityBeAttack(((Projectile) entity).getOwner(), this))) :
                entity instanceof Player player && ClientProxy.getPlayer() == player;
    }

    @Override
    public void tick() {
        super.tick();
        if (hasSkill("skill.pvz.umbrella_leaf.a_skill_name_for_cheap_but_breakable_umbrella_leaf")) {
            this.getAttribute(Attributes.MAX_HEALTH).setBaseValue(1D);
        }
        if (level.isClientSide) {
            List<Entity> entities = this.level.getEntities(this, this.getBoundingBox().inflate(2, 1.5, 2).move(0, 0.5, 0),
                    (entity) -> canBounce(entity, true));
            if (! entities.isEmpty()) {
                entities.forEach((entity1 -> {
                    Vec3 vec = entity1.getDeltaMovement();
                    entity1.setDeltaMovement(Math.min(0.5 * (entity1.getX() - this.getX()), 1),
                            Math.min(Math.abs(vec.y), 1),
                            0.5 * Math.min((entity1.getZ() - this.getZ()), 1));
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
                .add(Attributes.MAX_HEALTH, 8D)
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
            if (entity.getAttackTime() == 22 && entity.hasSkill("skill.pvz.umbrella_leaf.a_skill_name_for_cheap_but_breakable_umbrella_leaf")) {
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
            List<Entity> entities = entity.level.getEntities(entity, entity.getBoundingBox().inflate(2, 1.5, 2).move(0, 0.5, 0),
                    (entity) -> this.entity.canBounce(entity, false));
            if (! entities.isEmpty()) {
                entities.forEach((entity1 -> {
                    Vec3 vec = entity1.getDeltaMovement();
                    entity.setDeltaMovement(0, 0.25, 0);
                    entity1.setDeltaMovement(Math.min(0.5 * (entity1.getX() - entity.getX()), 1),
                            Math.min(Math.abs(vec.y), 1),
                            Math.min(0.5 * (entity1.getZ() - entity.getZ()), 1));
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
