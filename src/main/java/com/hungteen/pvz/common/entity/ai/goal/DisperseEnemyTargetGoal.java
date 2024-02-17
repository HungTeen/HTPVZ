package com.hungteen.pvz.common.entity.ai.goal;

import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Predicate;


public class DisperseEnemyTargetGoal extends NearestAttackableTargetGoal<LivingEntity> {

    protected final Predicate<Entity> predicate;
    protected List<Entity> targetCandidates = new ArrayList<>();

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
    protected void findTarget() {
        //from candidates
        if (! targetCandidates.isEmpty()) {
            Set<Entity> removeList = new HashSet<>();
            for (Entity entity : targetCandidates) {
                if (entity instanceof LivingEntity entity1 && predicate.test(entity1) &&
                        entity1.position().distanceTo(this.mob.position()) <= this.getFollowDistance() && EntityUtil.checkCanEntityBeAttack(mob, entity1)) {
                    this.target = entity1;
                    return;
                } else {
                    removeList.add(entity);
                }
            }
            for (Entity entity : removeList) {
                targetCandidates.remove(entity);
            }
        }
        //search for target
        this.target = this.mob.level.getNearestEntity(this.mob.level.getEntitiesOfClass(LivingEntity.class, this.getTargetSearchArea(this.getFollowDistance()),
                this.predicate), this.targetConditions, this.mob, this.mob.getX(), this.mob.getEyeY(), this.mob.getZ());
        if (target != null) {
            targetCandidates = this.mob.level.getEntities(target, target.getBoundingBox().inflate(4), (entity) -> entity instanceof LivingEntity && this.predicate.test(entity));
            targetCandidates.add(target);
            this.target = (LivingEntity) targetCandidates.get(mob.getRandom().nextInt(targetCandidates.size()));
        }
    }
}
