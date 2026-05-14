package com.hungteen.pvz.common.entity.ai.goal;

import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.Util;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Predicate;


public class DisperseEnemyTargetGoal extends NearestAttackableTargetGoal<LivingEntity> {

    protected Predicate<Entity> predicate;
    protected double range;

    /**@param range set to -1 to fit follow range attribute. See {@link DisperseEnemyTargetGoal#getFollowDistance()}.*/
    public DisperseEnemyTargetGoal(Mob mobIn, Predicate<Entity> predicate, boolean mustSee, double range) {
        super(mobIn, LivingEntity.class, mustSee);
        this.predicate = predicate;
        this.range = range;
    }
    public DisperseEnemyTargetGoal(Mob mobIn, Predicate<Entity> predicate, double range) {
        this(mobIn, predicate, true, range);
    }
    public DisperseEnemyTargetGoal(Mob mobIn) {
        this(mobIn, getDefaultPredicate(mobIn), -1);
    }

    public static Predicate<Entity> getDefaultPredicate(Mob mobIn) {
        return (entity) -> EntityUtil.checkCanEntityBeAttack(mobIn, entity) && entity != mobIn
                && (! Util.hasBlockBetween(mobIn.level, mobIn.position().add(0, mobIn.getEyeHeight(), 0), entity.position().add(0, entity.getEyeHeight(), 0)));
    }
    protected double getFollowDistance() {
        return range < 0 ? this.mob.getAttributeValue(Attributes.FOLLOW_RANGE) : range;
    }

    @Override
    public boolean canUse() {
        boolean flag = super.canUse();
        if (flag) {
            if (! EntityUtil.checkCanEntityBeAttack(this.mob, target)) {
                return false;
            }
        }
        return flag;
    }
    @Override
    public boolean canContinueToUse() {
        if (EntityUtil.isEntityValid(target)) {
            if (! this.predicate.test(target)) {
                target = null;
                this.mob.setTarget(null);
                return false;
            }
        }
        return super.canContinueToUse();
    }

    protected AABB getTargetSearchArea(double p_26069_) {
        return this.mob.getBoundingBox().inflate(p_26069_, 12.0D, p_26069_);
    }
    @Override
    protected void findTarget() {
        //search for target
        this.target = this.mob.level.getNearestEntity(this.mob.level.getEntitiesOfClass(LivingEntity.class, this.getTargetSearchArea(this.getFollowDistance()),
                this.predicate), this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        if (target != null) {
            double dist = this.mob.distanceTo(target);
            List<Entity> list = this.mob.level.getEntities(target, target.getBoundingBox().inflate(dist / 4),
                    (entity) -> entity instanceof LivingEntity && this.predicate.test(entity));
            list.add(target);
            this.target = (LivingEntity) list.get(mob.getRandom().nextInt(list.size()));
        }
    }
}
