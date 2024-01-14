package com.hungteen.pvz.common.entity.ai.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.target.TargetGoal;

import java.util.EnumSet;

public class ServerStressReleaseGoals {
    public static int averageTickTime = 0;
    public static class ServerStressReleaseTargetGoal extends TargetGoal {
        public ServerStressReleaseTargetGoal(Mob p_26140_, boolean p_26141_) {
            super(p_26140_, p_26141_);
            setFlags(EnumSet.of(Flag.TARGET, Flag.MOVE, Flag.JUMP, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return averageTickTime == 50 && this.mob.level.getNearestPlayer(this.mob, Math.min(5, (125000D / Math.pow(averageTickTime, 3)) * 100)) == null;
        }
    }
    public static class ServerStressReleaseGoal extends Goal {
        Mob mob;
        public ServerStressReleaseGoal(Mob mob) {
            super();
            this.mob = mob;
            setFlags(EnumSet.of(Flag.TARGET, Flag.MOVE, Flag.JUMP, Flag.LOOK));
        }

        @Override
        public boolean canUse() {
            return averageTickTime == 50 && this.mob.level.getNearestPlayer(this.mob, Math.min(5, (125000D / Math.pow(averageTickTime, 3)) * 100)) == null;
        }
    }
}
