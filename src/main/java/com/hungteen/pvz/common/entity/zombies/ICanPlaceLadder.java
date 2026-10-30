package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.common.block.CopperLadderBlock;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.GameRules;

public interface ICanPlaceLadder {

    default PathfinderMob get() {
        return (PathfinderMob) this;
    }

    default boolean canBuildLadder() {
        return get().getLevel().getGameRules().getBoolean(GameRules.RULE_MOBGRIEFING)
                && get().getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BlockItem b && b.getBlock() instanceof CopperLadderBlock;
    }


    default boolean consumeLadder() {
        if (! this.canBuildLadder()) {
            return false;
        }
        get().getItemInHand(InteractionHand.MAIN_HAND).shrink(1);
        return true;
    }
}
