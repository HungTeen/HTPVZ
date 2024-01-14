package com.hungteen.pvz.api.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public interface INeedSafeSituation {
    /**
     * the two functions checks if this place fits this entity.
     * if function returns null, the situations are safe.
     * */
    default MutableComponent isPositionSafe(Level level, BlockPos onPos, boolean actuallyPlant) {
        return null;
    }
    default MutableComponent isVehicleSafe(Entity target,  boolean actuallyPlant) {
        return null;
    }
}
