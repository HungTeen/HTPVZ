package com.hungteen.pvz.common.entity.ai.goal;

import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;

public class ShooterTargetGoal extends DisperseEnemyTargetGoal {

    public ShooterTargetGoal(ShooterPlant mobIn) {
        super(mobIn, DisperseEnemyTargetGoal.getDefaultPredicate(mobIn).and(mobIn::isHeightAvailable), -1);
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    protected void findTarget() {
        super.findTarget();
        if (EntityUtil.isEntityValid(target)) {
            if (! ((ShooterPlant) this.mob).isHeightAvailable(target)) {
                target = null;
                this.mob.setTarget(null);
            }
        }
    }

    @Override
    public boolean canContinueToUse() {
        if (! super.canContinueToUse() || this.mob.tickCount % 120 < 2 /*retarget enemy to prevent situations shooters ignoring closer enemies.*/) {
            target = null;
            this.mob.setTarget(null);
            return false;
        }
        return true;
    }

}
