package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.stream.Stream;

/**Must Be {@link LivingEntity} .*/
public interface ICanGroupUp {
    ICanGroupUp getLeader();
    void setLeader(ICanGroupUp entity);
    int getMaxSchoolSize();
    int getSchoolSize();
    void setSchoolSize(int size);
    int getGroupRangeSqr();

    default boolean isFollower() {
        return this.getLeader() != null && ((LivingEntity) this.getLeader()).isAlive();
    }

    default ICanGroupUp startFollowing(ICanGroupUp target) {
        this.setLeader(target);
        target.addFollower();
        return target;
    }

    default void stopFollowing() {
        this.getLeader().removeFollower();
        this.setLeader(null);
    }

    default void addFollower() {
        this.setSchoolSize( this.getSchoolSize() + 1);
    }

    default void addFollowers(Stream<? extends ICanGroupUp> targets) {
        targets.limit(this.getMaxSchoolSize() - this.getSchoolSize()).filter((target) -> target != this).forEach((p_27536_) -> {
            p_27536_.startFollowing(this);
        });
    }

    default void removeFollower() {
        this.setSchoolSize( this.getSchoolSize() - 1);
    }

    default boolean hasFollowers() {
        return this.getSchoolSize() > 1;
    }

    static boolean canBeFollowed(Entity target) {
        if (! (target instanceof ICanGroupUp)) {
            return false;
        }
        return ((ICanGroupUp) target).hasFollowers() && ((ICanGroupUp) target).getSchoolSize() < ((ICanGroupUp) target).getMaxSchoolSize();
    }

    default boolean inRangeOfLeader() {
        return ((Entity) this).distanceToSqr((Entity) this.getLeader()) <= getGroupRangeSqr();
    }

    default void pathToLeader() {
        if (this.isFollower() && ((Entity)this).position().distanceToSqr(((Entity) getLeader()).position()) > getGroupRangeSqr() / 4) {
            ((Mob) this).getNavigation().moveTo((Entity) this.getLeader(), 1.0D);
        }
    }
}
