package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.entity.LivingEntity;

public interface ICanBePlantedOn {
    default <T extends LivingEntity & ICanBePlantedOn> boolean canHold(LivingEntity plant, boolean isPlanting) {
        return this instanceof LivingEntity living && canHold((T) living, plant, isPlanting, false);
    }
    default <T extends LivingEntity & ICanBePlantedOn> boolean canHold(LivingEntity plant, boolean isPlanting, boolean passengerTested) {
        return this instanceof LivingEntity living && canHold((T) living, plant, isPlanting, passengerTested);
    }

    //for easy calling.
    static <T extends LivingEntity & ICanBePlantedOn> boolean canHold(T vehicle, LivingEntity plant, boolean isPlanting, boolean passengerTested) {
        return ! isPlanting ||
                (vehicle.getPassengers().isEmpty() ||
                        (vehicle.getPassengers().size() == 1 &&
                                vehicle.getPassengers().get(0) instanceof ICanBePlantedOn iCanBePlantedOn && (passengerTested || iCanBePlantedOn.canHold(plant, true))));
    }
    static <T extends LivingEntity & ICanBePlantedOn> boolean canHold(T vehicle, LivingEntity plant, boolean isPlanting) {
        return canHold(vehicle, plant, isPlanting, false);
    }
}
