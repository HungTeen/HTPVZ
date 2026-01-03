package com.hungteen.pvz.api.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;

import javax.annotation.Nullable;

public interface IHangable {
    default boolean hangableToEntity(Entity entity) {
        return false;
    }

    default boolean hangableToBlockPos(BlockPos pos) {
        return false;
    }

    boolean isHanging();
    default void setHangingPosition(@Nullable BlockPos pos) {
    }
    default void setHangingEntity(@Nullable Entity entity) {
    }
    default @Nullable BlockPos getHangingPosition() {
        return null;
    }
    default @Nullable Entity getHangingEntity() {
        return null;
    }
    double getRopeLengthSqr();
    void setRopeLengthSqr(double lengthSqr);
}
