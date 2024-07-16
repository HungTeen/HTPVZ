package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.entity.LivingEntity;

public interface ICanBePlantedOn {
    default boolean canHold(LivingEntity plant, boolean isPlanting) {
        return canHold(this, plant, isPlanting, false);
    }
    default boolean canHold(LivingEntity plant, boolean isPlanting, boolean passengerTested) {
        return canHold(this, plant, isPlanting, passengerTested);
    }

    //for easy calling.
    static boolean canHold(ICanBePlantedOn vehicle, LivingEntity plant, boolean isPlanting, boolean passengerTested) {
        return ! isPlanting ||
                (((LivingEntity) vehicle).getPassengers().isEmpty() ||
                        (((LivingEntity) vehicle).getPassengers().size() == 1 &&
                                ((LivingEntity) vehicle).getPassengers().get(0) instanceof ICanBePlantedOn iCanBePlantedOn && (passengerTested || iCanBePlantedOn.canHold(plant, true))));
    }
    static boolean canHold(ICanBePlantedOn vehicle, LivingEntity plant, boolean isPlanting) {
        return canHold(vehicle, plant, isPlanting, false);
    }
}
