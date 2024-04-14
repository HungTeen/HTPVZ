package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

/**Sun absorber attracts and absorbs sun. Must be Entity or BlockEntity to automate absorb.
 * <br> <b>Attention! </b>  Player is also sun absorber but not implementing ISunAbsorber...*/
public interface ISunAbsorber {
    default boolean canAbsorb(ISun sun) {
        return true;
    }
    void onAbsorb(ISun sun);
    boolean isSunContainer();
    /**If object {@link ISunAbsorber#isSunContainer() is sun container}, this method below can be used.*/
    int getContainingSun();
    default Vec3 position() {
        return this instanceof Entity ? ((Entity) this).position().add(0, ((Entity) this).getBbHeight() / 2, 0) :
                Vec3.atCenterOf(((BlockEntity) this).getBlockPos());
    }
}
