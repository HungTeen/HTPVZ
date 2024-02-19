package com.hungteen.pvz.api.interfaces;


import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import net.minecraft.world.entity.LivingEntity;

public interface ICanBePlantedOn {
    default boolean canHold(LivingEntity plant, boolean isPlanting) {
        return (!isPlanting || ((LivingEntity) this).getPassengers().isEmpty()) && PVZOwnedCapability.isTeammate((LivingEntity) this, plant);
    }
}
