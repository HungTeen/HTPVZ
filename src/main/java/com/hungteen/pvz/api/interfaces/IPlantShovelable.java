package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

public interface IPlantShovelable {
    default boolean canShovel(LivingEntity entity, ItemStack shovelable) {
        return true;
    }
    default void onPlantShoveled(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {}
}
