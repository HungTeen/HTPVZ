package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

/**Can only be LivingEntity.*/
public interface IGardenPlant {
    InteractionResult onWatered(Player player, ItemStack stack);
    InteractionResult onFertilized(Player player, ItemStack stack);

    /**@return  The max level the garden plant can reach.*/
    int getMaxLevel();
    int getGrowLevel();
    void setGrowLevel(int level);

    /**@return The plant is requiring water or fertilizer.*/
    boolean isRequiringWater();
    boolean isRequiringFertilizer();
    void setRequiringWater(boolean bool);
    void setRequiringFertilizer(boolean bool);

    int getRemainingGrowTick();
    void setRemainingGrowTick(int tick);
}
