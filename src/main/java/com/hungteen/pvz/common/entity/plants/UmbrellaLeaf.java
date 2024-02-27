package com.hungteen.pvz.common.entity.plants;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.function.Predicate;

import static com.hungteen.pvz.common.world.PVZDamageSource.teamFilter;

public class UmbrellaLeaf extends SimplePlant {

    public AnimationState idleAnimationState = new AnimationState();
    public AnimationState openAnimationState = new AnimationState();
    protected static final EntityDataAccessor<Boolean> POSE = SynchedEntityData.defineId(UmbrellaLeaf.class, EntityDataSerializers.BOOLEAN);
    public static List<Skill> staticSkillList = List.of(
            new Skill("skill.pvz.umbrella_leaf.a_skill_name_for_cheap_but_breakable_umbrella_leaf", PVZItems.LUX_ESSENCE, 8, 8, -100, -50)
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
    @Override
    public void tick() {
        super.tick();
        if (level.isClientSide) {
            List<Entity> entities = this.level.getEntities(this, this.getBoundingBox().inflate(1.5, 1, 1.5),
                    (entity) -> entity instanceof Player player && ClientProxy.getPlayer() == player && player.getDeltaMovement().length() > 0.6);
            if (! entities.isEmpty()) {
                entities.forEach((entity1 -> {
                    Vec3 vec = entity1.getDeltaMovement();
                    entity1.setDeltaMovement(Math.min(0.5 * (entity1.getX() - this.getX()), 1),
                            Math.min(Math.abs(vec.y), 1),
                            0.5 * Math.min((entity1.getZ() - this.getZ()), 1));
                    entity1.fallDistance = 0;
                }));
                this.setAttackTime(30);
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

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 8D);
    }
    @Override
    public List<Skill> getStaticSkillList(){
        return staticSkillList;
    }

    @Override
    public Predicate<Entity> canPush(){
        return entity -> true;
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
            List<Entity> entities = entity.level.getEntities(entity, entity.getBoundingBox().inflate(2, 1, 2).move(0, 0.5, 0),
                    (entity) -> (entity instanceof LivingEntity || EntityUtil.checkCanEntityBeAttack(entity, this.entity)
                            || (entity instanceof Projectile && EntityUtil.checkCanEntityBeAttack(((Projectile) entity).getOwner(), this.entity)))
                            && entity.getDeltaMovement().length() > 0.6);
            if (! entities.isEmpty()) {
                entities.forEach((entity1 -> {
                    Vec3 vec = entity1.getDeltaMovement();
                    entity.setDeltaMovement(0, 0.25, 0);
                    entity1.setDeltaMovement(Math.min(0.5 * (entity1.getX() - entity.getX()), 1),
                            Math.min(Math.abs(vec.y), 1),
                            Math.min(0.5 * (entity1.getZ() - entity.getZ()), 1));
                    entity1.fallDistance = 0;
                    if (entity1 instanceof Projectile) {
                        ((Projectile) entity1).setOwner(entity);
                    }
                }));
                entity.setAttackTime(30);
            }
        }
    }
}
