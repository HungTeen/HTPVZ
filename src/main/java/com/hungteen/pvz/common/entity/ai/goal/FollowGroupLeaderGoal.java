package com.hungteen.pvz.common.entity.ai.goal;

import com.hungteen.pvz.api.interfaces.ICanGroupUp;
import com.hungteen.pvz.util.EntityUtil;
import com.mojang.datafixers.DataFixUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;

import java.util.EnumSet;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

/**From {@link net.minecraft.world.entity.ai.goal.FollowFlockLeaderGoal}. */
public class FollowGroupLeaderGoal extends Goal {
    private static final int INTERVAL_TICKS = 200;
    private final ICanGroupUp mob;
    private int timeToRecalcPath;
    private int nextStartTick;

    public FollowGroupLeaderGoal(ICanGroupUp mob) {
        this.mob = mob;
        this.nextStartTick = this.nextStartTick(mob);
        this.setFlags(EnumSet.of(Flag.MOVE));
    }

    protected int nextStartTick(ICanGroupUp mob) {
        return reducedTickDelay(INTERVAL_TICKS + ((LivingEntity) mob).getRandom().nextInt(INTERVAL_TICKS) % 20);
    }

    public boolean canUse() {
        if (mob instanceof LivingEntity mob) {
            if (this.mob.hasFollowers()) {
                return false;
            } else if (this.mob.isFollower()) {
                return true;
            } else if (this.nextStartTick > 0) {
                --this.nextStartTick;
                return false;
            } else {
                this.nextStartTick = this.nextStartTick(this.mob);
                double range = ((ICanGroupUp) mob).getGroupRangeSqr();
                Predicate<Entity> Leaderpredicate = (target) ->
                        EntityUtil.isTeammate(mob, target) && target instanceof ICanGroupUp &&
                                ICanGroupUp.canBeFollowed((Entity & ICanGroupUp) target) || ! ((ICanGroupUp) target).isFollower();
                List<? extends LivingEntity> list = mob.level.getEntitiesOfClass(mob.getClass(),
                        mob.getBoundingBox().inflate(range, range, range), Leaderpredicate);
                ICanGroupUp entity = (Entity & ICanGroupUp) DataFixUtils
                        .orElse(list.stream().filter(entity1 -> ICanGroupUp.canBeFollowed((Entity & ICanGroupUp) entity1)).findAny(), this.mob);
                entity.addFollowers(
                        (Stream<? extends ICanGroupUp>) list.stream().filter((target) -> target instanceof ICanGroupUp && ! ((ICanGroupUp) target).isFollower()));
                return this.mob.isFollower();
            }
        }
        return false;
    }

    public boolean canContinueToUse() {
        return this.mob.isFollower() && this.mob.inRangeOfLeader();
    }

    public void start() {
        this.timeToRecalcPath = 0;
    }

    public void stop() {
        this.mob.stopFollowing();
    }

    public void tick() {
        if (--this.timeToRecalcPath <= 0) {
            this.timeToRecalcPath = this.adjustedTickDelay(10);
            this.mob.pathToLeader();
        }
    }
}