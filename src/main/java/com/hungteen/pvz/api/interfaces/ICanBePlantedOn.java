package com.hungteen.pvz.api.interfaces;


import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import net.minecraft.world.entity.LivingEntity;

public interface ICanBePlantedOn {
    default boolean canHold(LivingEntity plant) {
        return PVZOwnedCapability.isTeammate((LivingEntity) this, plant);
    }
}
