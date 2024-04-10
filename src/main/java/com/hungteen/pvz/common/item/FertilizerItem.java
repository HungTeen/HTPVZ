package com.hungteen.pvz.common.item;

import com.hungteen.pvz.api.interfaces.IGardenPlant;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class FertilizerItem extends Item {
    public FertilizerItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
        if (target instanceof IGardenPlant plant) {
            InteractionResult result = plant.onFertilized(player, itemStack);
            if (result.consumesAction() && ! player.level.isClientSide) {
                itemStack.shrink(1);
            }
            return result;
        } else {
            return InteractionResult.PASS;
        }
    }
}
