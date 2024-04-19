package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.entity.LivingEntity;

public interface ICanBePlantedOn {
    default boolean canHold(LivingEntity plant, boolean isPlanting) {
        return (!isPlanting || (((LivingEntity) this).getPassengers().isEmpty() ||
                (((LivingEntity) this).getPassengers().size() == 1 && ((LivingEntity) this).getPassengers().get(0) instanceof ICanBePlantedOn)));
    }
}
