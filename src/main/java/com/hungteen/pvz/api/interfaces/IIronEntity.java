package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface IIronEntity {
    default boolean isIronMaterial() {
        return true;
    }
    default void onAttracted(Vec3 from) {
        if (this instanceof Entity entity) {
            entity.setDeltaMovement(entity.getDeltaMovement()
                    .add(from.x - entity.getX(), from.y - entity.getY(), from.z - entity.getZ())
                    .normalize().scale(4 / from.distanceToSqr(entity.position())));
        }
    }
}
