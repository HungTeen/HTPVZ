package com.hungteen.pvz.api.interfaces;


import net.minecraft.world.entity.LivingEntity;

public interface ICanBePlantedOn {
    default boolean canHold(LivingEntity plant) {
        return true;
    }
}
