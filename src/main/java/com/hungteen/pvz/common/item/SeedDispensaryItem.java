package com.hungteen.pvz.common.item;

import com.hungteen.pvz.api.interfaces.IPlantShovelable;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class SeedDispensaryItem extends Item implements IPlantShovelable {
    public SeedDispensaryItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public void onShovelPlant(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
        ItemStack itemStack1 = target.getPickResult();
        if (itemStack1 != null && !itemStack1.isEmpty()) {
            player.getInventory().add(itemStack1);
            itemStack.shrink(1);
        }
    }
}
