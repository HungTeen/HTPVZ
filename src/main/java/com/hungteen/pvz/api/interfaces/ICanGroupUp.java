package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;

import java.util.stream.Stream;

/**Must Be {@link Mob} .*/
public interface ICanGroupUp {
    ICanGroupUp getLeader();
    void setLeader(ICanGroupUp entity);
    int getMaxSchoolSize();
    int getSchoolSize();
    void setSchoolSize(int size);
    int getGroupRangeSqr();

    default Mob self() {
        return (Mob) this;
    }

    default boolean isFollower() {
        return this.getLeader() != null && this.getLeader().self().isAlive();
    }

    default void startFollowing(ICanGroupUp target) {
        this.setLeader(target);
        target.addFollower();
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

    static <T extends Entity & ICanGroupUp> boolean canBeFollowed(T target) {
        return target.hasFollowers() && target.getSchoolSize() < target.getMaxSchoolSize();
    }

    default boolean inRangeOfLeader() {
        return self().distanceToSqr((Entity) this.getLeader()) <= getGroupRangeSqr();
    }

    default void pathToLeader() {
        if (this.isFollower() && ((Entity)this).position().distanceToSqr(((Entity) getLeader()).position()) > getGroupRangeSqr() / 4) {
            self().getNavigation().moveTo((Entity) this.getLeader(), 1.0D);
        }
    }
}
