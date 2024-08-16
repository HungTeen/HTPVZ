package com.hungteen.pvz.common.entity.ai.goal;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.api.interfaces.IAttractsEnemy;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.entity.player.Player;

import java.util.function.Supplier;

public class AttractEnemyGoal extends Goal {
    public Mob entity;
    public int countDown;
    public Supplier<Boolean> condition;
    public double range;
    public AttractEnemyGoal(Mob entity, Supplier<Boolean> condition, double range) {
        this.entity = entity;
        this.condition = condition;
        this.range = range;
        countDown = 15;
    }
    public AttractEnemyGoal(Mob entity) {
        this(entity, () -> true, entity.getAttribute(Attributes.FOLLOW_RANGE).getValue());
    }

    @Override
    public boolean canUse() {
        return -- countDown <= 0 && condition.get();
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }

    @Override
    public void tick() {
        countDown = 15;
        attractEnemies(entity);
    }

    public float getBasicAttractingStrength(Entity attacker, Entity target) {
        return target instanceof IAttractsEnemy entity ? entity.getAttractStrength(attacker) : (target instanceof Player ? 5 : 10);
    }

    public float getAttractingStrength(Entity attacker, Entity target) {
        return getBasicAttractingStrength(attacker, target) / target.distanceTo(attacker);
    }

    public void attractEnemies(Mob entity) {
        entity.level.getEntities(entity, entity.getBoundingBox().inflate(range)).forEach((targetEntity) -> {
            //attracting limits about tergetEntity.
            boolean outOfHeightRegion = (targetEntity.getY() <= entity.getY()) == (targetEntity.getY() <= entity.getBbHeight() + entity.getY()) &&
                    (targetEntity.getY() + targetEntity.getBbHeight() <= entity.getY()) == (targetEntity.getY() + targetEntity.getBbHeight() <= entity.getBbHeight() + entity.getY()) &&
                    (targetEntity.getY() <= entity.getY()) == (targetEntity.getY() + targetEntity.getBbHeight() <= entity.getY());
            if (outOfHeightRegion) return;
            if (targetEntity instanceof Mob && ! EntityUtil.isTeammate(entity, targetEntity)) {
                LivingEntity targetOfTarget = ((Mob) targetEntity).getTarget();
                ///attracting limits about targetEntity's target.
                if (! EntityUtil.isEntityValid(targetOfTarget) ||
                        (getBasicAttractingStrength(targetEntity, targetOfTarget) < getBasicAttractingStrength(targetEntity, entity)) &&
                                ((! PVZConfig.PVZGameRules.getBoolean(entity.level, PVZConfig.Common.teamBattle)) || (EntityUtil.isTeammate(entity, targetOfTarget)))) {
                    if (((Mob) targetEntity).targetSelector.getAvailableGoals().stream().anyMatch((goal) -> goal.getGoal() instanceof TargetGoal)) {
                        ((Mob) targetEntity).setTarget(entity);
                    }
                }
            }
        });
    }

}
