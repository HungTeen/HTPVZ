package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

import javax.annotation.Nullable;

/**Can only be LivingEntity.*/
public interface IGardenPlant {

    /**This determines GardenFlowerPotBlock or other possible garden flower pots is water pot.*/
    BooleanProperty WATER = BooleanProperty.create("water");

    InteractionResult onWatered(@Nullable Player player, ItemStack stack);
    InteractionResult onFertilized(@Nullable Player player, ItemStack stack);

    /**@return  The max level the garden plant can reach.*/
    int getMaxLevel();
    int getGrowLevel();
    void setGrowLevel(int level);

    /**@return The plant is requiring water or fertilizer.*/
    boolean isRequiringWater();
    boolean isRequiringFertilizer();
    void setRequiringWater(boolean bool);
    void setRequiringFertilizer(boolean bool);
    /**Only available on server.*/
    int getRemainingGrowTick();
    /**Only available on server.*/
    void setRemainingGrowTick(int tick);
}
