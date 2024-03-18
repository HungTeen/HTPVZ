package com.hungteen.pvz.common.item;

import com.hungteen.pvz.api.interfaces.IPlant;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SeedDispensaryItem extends Item {
    public SeedDispensaryItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof IPlant && ! player.level.isClientSide) {
            ItemStack itemStack1 = target.getPickResult();
            if (itemStack1 != null && !itemStack1.isEmpty()) {
                if (((IPlant) target).onBeingShoveled(player, hand)) {
                    player.getInventory().add(itemStack1);
                    itemStack.shrink(1);
                return InteractionResult.CONSUME;
                }
            }
        }
        return super.interactLivingEntity(itemStack, player, target, hand);
    }
}
