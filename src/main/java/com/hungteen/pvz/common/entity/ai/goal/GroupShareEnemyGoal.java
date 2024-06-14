package com.hungteen.pvz.common.entity.ai.goal;

import com.hungteen.pvz.api.interfaces.ICanGroupUp;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;

public class GroupShareEnemyGoal extends Goal {
    Mob self;
    int count = 5;
    public GroupShareEnemyGoal(Mob mob) {
        this.self = mob;
    }

    @Override
    public boolean canUse() {
        return (EntityUtil.isEntityValid((LivingEntity) ((ICanGroupUp) self).getLeader()) && EntityUtil.checkCanEntityBeAttack(self, self.getTarget()));
    }

    @Override
    public void tick() {
        count --;
        if (count == 0) {
            count = 5;
            for (Mob entity : self.level.getEntitiesOfClass(self.getClass(), this.self.getBoundingBox().inflate(2),
                    (target) -> EntityUtil.isTeammate(this.self, target))) {
                if ((((ICanGroupUp) self).getLeader() == ((ICanGroupUp) entity).getLeader() || ((ICanGroupUp) self).getLeader() == entity) && ! EntityUtil.checkCanEntityBeAttack(entity, entity.getTarget())) {
                    entity.setTarget(self.getTarget());
                }
            }
        }
    }
}
