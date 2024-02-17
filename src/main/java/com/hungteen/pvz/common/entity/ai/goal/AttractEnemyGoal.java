package com.hungteen.pvz.common.entity.ai.goal;

import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.capability.pvzRules.PVZRulesCapability;
import com.hungteen.pvz.api.interfaces.IDefenderPlant;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

public class AttractEnemyGoal extends Goal {
    public Mob entity;
    public int countDown;
    public boolean mustNotHavePassenger;
    public AttractEnemyGoal(Mob entity, boolean mustNotHavePassenger) {
        this.entity = entity;
        this.mustNotHavePassenger = mustNotHavePassenger;
        countDown = 15;
    }
    public AttractEnemyGoal(Mob entity) {
        this(entity, true);
    }

    @Override
    public boolean canUse() {
        return -- countDown <= 0 && (! mustNotHavePassenger || entity.getFirstPassenger() == null);
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

    public void attractEnemies(Mob entity) {
        double range = entity.getAttributeValue(Attributes.FOLLOW_RANGE);//recommended value is 2.
        entity.level.getEntities(entity, entity.getBoundingBox().inflate(range)).forEach((targetEntity) -> {
            //attracting limits about tergetEntity.
            boolean outOfHeightRegion = (targetEntity.getY() <= entity.getY()) == (targetEntity.getY() <= entity.getBbHeight() + entity.getY()) &&
                    (targetEntity.getY() + targetEntity.getBbHeight() <= entity.getY()) == (targetEntity.getY() + targetEntity.getBbHeight() <= entity.getBbHeight() + entity.getY()) &&
                    (targetEntity.getY() <= entity.getY()) == (targetEntity.getY() + targetEntity.getBbHeight() <= entity.getY());
            if (targetEntity instanceof Mob && ! PVZOwnedCapability.isTeammate(entity, targetEntity) && ! outOfHeightRegion) {
                LivingEntity targetOfTarget = ((Mob) targetEntity).getTarget();
                ///attracting limits about targetEntity's target.
                if (! (targetOfTarget instanceof IDefenderPlant) && ((! PVZRulesCapability.getBoolean("teamBattle")) ||
                        (! EntityUtil.isEntityValid(targetOfTarget) || PVZOwnedCapability.isTeammate(entity, targetOfTarget)))) {
                    //test if can attract.
                    ((Mob) targetEntity).targetSelector.getAvailableGoals().forEach((goal) -> {
                        if (goal.getGoal() instanceof TargetGoal) {
                            LivingEntity tmpEntity = ((Mob) targetEntity).getTarget();
                            ((Mob) targetEntity).setTarget(entity);
                            if (!goal.getGoal().canContinueToUse()) {
                                //redraw setting target if not match.
                                ((Mob) targetEntity).setTarget(tmpEntity);
                            }
                        }
                    });
                }
            }
        });
    }

}
