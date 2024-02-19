package com.hungteen.pvz.common.entity.ai.goal;

import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.ai.util.DefaultRandomPos;
import net.minecraft.world.phys.Vec3;

import java.util.function.Predicate;

public class AvoidTargetGoal extends AvoidEntityGoal<LivingEntity> {

    Predicate<LivingEntity> predicate;
    protected final TargetingConditions avoidEntityTargeting;
    public AvoidTargetGoal(PathfinderMob p_25027_, Predicate<LivingEntity> predicate, float p_25029_, double p_25030_, double p_25031_) {
        super(p_25027_, LivingEntity.class, p_25029_, p_25030_, p_25031_);
        this.predicate = predicate;
        this.avoidEntityTargeting = TargetingConditions.forCombat().range(p_25029_).selector(predicate);
    }
    public boolean canUse() {
        //from AvoidEntityGoal
        this.toAvoid = this.mob.level.getNearestEntity(this.mob.level.getEntitiesOfClass(this.avoidClass,
                this.mob.getBoundingBox().inflate(this.maxDist, 3.0D, this.maxDist), this.predicate),
                this.avoidEntityTargeting, this.mob, this.mob.getX(), this.mob.getY(), this.mob.getZ());
        if (this.toAvoid == null) {
            return false;
        } else {
            Vec3 vec3 = DefaultRandomPos.getPosAway(this.mob, 6, 6, this.toAvoid.position());
            if (vec3 == null) {
                return false;
            } else if (this.toAvoid.distanceToSqr(vec3.x, vec3.y, vec3.z) < this.toAvoid.distanceToSqr(this.mob)) {
                return false;
            } else {
                this.path = this.pathNav.createPath(vec3.x, vec3.y, vec3.z, 0);
                return this.path != null;
            }
        }
    }

}
