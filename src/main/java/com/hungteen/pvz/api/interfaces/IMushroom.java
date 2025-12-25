package com.hungteen.pvz.api.interfaces;

import com.hungteen.pvz.api.PVZAPI;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.LightLayer;

public interface IMushroom {
    void fallAsleep();
    void wakeUp();
    default boolean shouldFallAsleep() {
        if (this instanceof Mob mob) {
            int light = mob.level.getBrightness(LightLayer.SKY, mob.blockPosition()) - mob.level.getSkyDarken();
            return (! PVZAPI.get().isSculk(mob)) && light >= 9;
        }
        return false;
    }

    default boolean shouldWakeUp() {
        return !shouldFallAsleep();
    }
}
