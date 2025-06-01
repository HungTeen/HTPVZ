package com.hungteen.pvz.common.entity.ai.goal;

import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;

public class AxisLookAroundGoal extends RandomLookAroundGoal {
    private final Mob mob;
    private double relX;
    private double relZ;
    private int lookTime;

    public AxisLookAroundGoal(Mob p_25720_) {
        super(p_25720_);
        this.mob = p_25720_;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public void start() {
        double d0 = (Math.PI * 0.5D) * this.mob.getRandom().nextInt(4);
        this.relX = Math.cos(d0);
        this.relZ = Math.sin(d0);
        this.lookTime = 60;
    }

    public boolean canUse() {
        return this.mob.getRandom().nextFloat() < (this.mob.getYRot() + 1 % 90 < 1 ? 0.005F : 0.05F);
    }
    public boolean canContinueToUse() {
        return this.lookTime >= 0;
    }

    @Override
    public void tick() {
        --this.lookTime;
        this.mob.getLookControl().setLookAt(this.mob.getX() + this.relX, this.mob.getEyeY(), this.mob.getZ() + this.relZ);
        Vec3 vec = this.mob.getViewVector(0);
        if (vec.x == relX && vec.z == relZ) {
            this.lookTime = -1;
        }
    }
}