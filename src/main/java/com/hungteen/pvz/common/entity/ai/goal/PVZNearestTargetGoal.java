package com.hungteen.pvz.common.entity.ai.goal;

import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class PVZNearestTargetGoal  extends PVZTargetGoal {

    protected final EntityUtil.EntitySorter sorter;

    public PVZNearestTargetGoal(Mob mobIn, boolean mustSee, boolean mustReach, float w, float h) {
        this(mobIn, mustSee, mustReach, w, h, h);
    }

    public PVZNearestTargetGoal(Mob mobIn, boolean mustSee, boolean mustReach, float w, float h1, float h2) {
        super(mobIn, mustSee, mustReach, w, h1, h2);
        this.sorter = new EntityUtil.EntitySorter(mob);
    }

    @Override
    public boolean canUse() {
        if (this.targetChance > 0 && this.mob.getRandom().nextInt(this.targetChance) != 0) {
            return false;
        }
        List<LivingEntity> list1 = this.mob.level.getEntitiesOfClass(LivingEntity.class, getAABB()).stream().filter(target -> {
            return (! this.mustSee || this.checkSenses(target)) && this.checkOther(target);
        }).collect(Collectors.toList());
        if (list1.isEmpty()) {
            return false;
        }
        Collections.sort(list1, this.sorter);
        this.targetMob = list1.get(0);
        return true;
    }

}
