package com.hungteen.pvz.api.interfaces;

import com.hungteen.pvz.api.events.PVZResourceEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;

public interface INeedSafeSituation {

    /**
     * the two functions checks if this place fits this entity.<br>
     * @param event CheckConditionEvent when planting. <br>
     *              If this parameter can is null while planting, whether resource is enough is ignored.
     * @param direction direction this plant attach. null for inside fluid blocks.
     * @return null for the situations are safe. If return with a Component, the reason of not safe is described with it.
     * */
    default MutableComponent isPositionSafe(@Nullable PVZResourceEvent.CheckPlantConditionEvent event, Level level, BlockPos pos, @Nullable Direction direction, boolean isPlanting) {
        return null;
    }
    default MutableComponent isVehicleSafe(@Nullable PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        return null;
    }
}
