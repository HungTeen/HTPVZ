package com.hungteen.pvz.common.entity.ai.goal;

import com.hungteen.pvz.common.capability.pvzRules.PVZRulesCapability;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;
import net.minecraft.world.scores.Team;

public class AttractEnemyGoal extends Goal {
    public Mob entity;
    public int countDown;

    public AttractEnemyGoal(Mob entity) {
        this.entity = entity;
        countDown = 15;
    }

    @Override
    public boolean canUse() {
        return -- countDown <= 0;
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
            if (targetEntity instanceof Mob && (entity.getTeam() == null || targetEntity.getTeam() != entity.getTeam())) {
                LivingEntity targetOfTarget = ((Mob) targetEntity).getTarget();
                ///attracting limits about targetEntity's target.
                if (targetOfTarget != entity && (! PVZRulesCapability.get("teamBattle") || (targetOfTarget != null && targetOfTarget.getTeam() == entity.getTeam()))) {
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
