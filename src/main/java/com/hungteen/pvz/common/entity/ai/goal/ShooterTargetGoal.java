package com.hungteen.pvz.common.entity.ai.goal;

import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.entity.plants.base.ShooterPlant;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;

public class ShooterTargetGoal extends DisperseEnemyTargetGoal {

    public ShooterTargetGoal(ShooterPlant mobIn) {
        super(mobIn);
        this.setFlags(EnumSet.of(Goal.Flag.TARGET));
    }

    @Override
    protected void findTarget() {
        if (targetCandidates.isEmpty() && mob.getRandom().nextInt(5) == 0) {
            List<Entity> list = this.mob.level.getEntities(mob, mob.getBoundingBox().inflate(4),
                    (entity) -> entity instanceof LivingEntity && PVZOwnedCapability.isTeammate(mob, entity) && entity instanceof ShooterPlant);
            if (! list.isEmpty()) {
                targetCandidates = ((ShooterPlant) list.get(mob.getRandom().nextInt(list.size()))).getTargetCandidates();
            }
        }
        ((ShooterPlant) mob).setTargetCandidates(targetCandidates);
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
        if (! super.canContinueToUse()) {
            target = null;
            this.mob.setTarget(null);
            return false;
        }
        if (EntityUtil.isEntityValid(target)) {
            if (! ((ShooterPlant) this.mob).isHeightAvailable(target)) {
                target = null;
                this.mob.setTarget(null);
                return false;
            }
        }
        return true;
    }

}
