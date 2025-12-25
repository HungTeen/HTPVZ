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
        return (mob.isUsingItem() || mob.tickCount % 200 < 2) &&
                (mob.getMainHandItem().getItem() instanceof ShieldItem || mob.getOffhandItem().getItem() instanceof ShieldItem);
    }

    public void start() {
        super.start();
        if (mob.getMainHandItem().getItem() instanceof ShieldItem) {
            this.mob.startUsingItem(InteractionHand.MAIN_HAND);
        } else {
            this.mob.startUsingItem(InteractionHand.OFF_HAND);
        }
    }

    public void stop() {
        super.stop();
        this.mob.stopUsingItem();
    }
}
