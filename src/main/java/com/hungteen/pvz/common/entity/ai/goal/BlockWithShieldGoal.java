package com.hungteen.pvz.common.entity.ai.goal;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ShieldItem;

public class BlockWithShieldGoal extends Goal {
    Mob mob;
    public BlockWithShieldGoal(Mob entity) {
        this.mob = entity;
    }
    @Override
    public boolean canUse() {
        return (mob.isUsingItem() || mob.tickCount % 100 < 2) && mob.getMainHandItem().getItem() instanceof ShieldItem;
    }

    public void start() {
        super.start();
        this.mob.startUsingItem(InteractionHand.MAIN_HAND);
    }

    public void stop() {
        super.stop();
        this.mob.stopUsingItem();
    }
}
