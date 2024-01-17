package com.hungteen.pvz.common.entity.ai.goal;

import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.phys.AABB;

import java.util.List;
import java.util.function.Predicate;


public class DisperseEnemyTargetGoal extends NearestAttackableTargetGoal<LivingEntity> {

    protected final Predicate<Entity> predicate;

    public DisperseEnemyTargetGoal(Mob mobIn, Predicate<Entity> predicate) {
        super(mobIn, LivingEntity.class, true);
        this.predicate = predicate;
    }
    public DisperseEnemyTargetGoal(Mob mobIn) {
        this(mobIn, (entity)-> EntityUtil.checkCanEntityBeAttack(mobIn, entity));
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
    protected AABB getTargetSearchArea(double distance) {
        AABB box = this.mob.getBoundingBox()
                .move(this.mob.getLookAngle().normalize().multiply(distance / 2.5, distance / 2.5, distance / 2.5))
                .inflate(distance / 2, 4.0D, distance / 2);
        return box;
    }

    @Override
    protected void findTarget() {
        LivingEntity center;
        center = this.mob.level.getNearestEntity(this.mob.level.getEntitiesOfClass(LivingEntity.class, this.getTargetSearchArea(this.getFollowDistance()),
                this.predicate), this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        if (center != null) {
            List<Entity> entities = this.mob.level.getEntities(center, center.getBoundingBox().inflate(4), (entity) -> entity instanceof LivingEntity && this.predicate.test(entity));
            entities.add(center);
            int index = this.mob.getRandom().nextInt(entities.size());
            this.target = (LivingEntity) entities.get(index >= 0 ? index : - index);
        }
    }
}
