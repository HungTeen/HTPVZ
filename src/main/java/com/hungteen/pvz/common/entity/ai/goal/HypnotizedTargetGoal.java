package com.hungteen.pvz.common.entity.ai.goal;

import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;

import java.util.Optional;

public class HypnotizedTargetGoal extends DisperseEnemyTargetGoal {

    public HypnotizedTargetGoal(Mob mob) {
     super(mob);
    }
    @Override
    public boolean canUse() {
        if (mob.hasEffect(PVZMobEffects.HYPNOTISED.get())) {
            if (mob.getTarget() == null || EntityUtil.isTeammate(mob, mob.getTarget())) {
                this.target = null;
                this.findTarget();
                this.mob.setTarget(this.target);
            }
            try {
                Optional<LivingEntity> opt = mob.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
                if (opt.isEmpty() || EntityUtil.isTeammate(mob, opt.get())) {
                    this.findTarget();
                    mob.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, this.target);
                    mob.getBrain().setMemory(MemoryModuleType.ANGRY_AT, this.target.getUUID());
                    mob.getBrain().setMemory(MemoryModuleType.HURT_BY_ENTITY, this.target);
                }
            } catch (Exception ignored) {
            }
            return true;
        }
        return false;
    }

    @Override
    public boolean canContinueToUse() {
        return this.canUse();
    }
}
