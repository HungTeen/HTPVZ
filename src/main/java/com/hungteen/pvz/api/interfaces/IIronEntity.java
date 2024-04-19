package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;

public interface IIronEntity {
    default boolean isIronMaterial() {
        return true;
    }
    default void onAttracted(Vec3 from) {
        Entity zhege = (Entity) this;
        zhege.setDeltaMovement(zhege.getDeltaMovement()
                .add(from.x - zhege.getX(), from.y - zhege.getY(), from.z - zhege.getZ())
                .normalize().scale(4 / from.distanceToSqr(zhege.position())));
    }
}
