package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.phys.Vec3;

/**
 * Sun absorber attracts and absorbs sun. Must be Entity or BlockEntity to automate absorb.
 * <br> <b>Attention! </b>  Player is also sun absorber but not implementing ISunAbsorber...
 */
public interface ISunAbsorber {

    int COMMON_ABSORB_PRIORITY = 100;
    int COLLECTOR_ABSORB_PRIORITY = 200;
    int STEAL_ABSORB_PRIORITY = 300;

    /**
     * 吸收阳光的优先级，值越大优先级越低，优先级高可以抢占吸收阳光 <br>
     * 玩家优先级中等，阳光收集器优先级较高，太阳神僵尸优先级最高
     * @return priority
     */
    default int getAbsorbPriority() {
        return COMMON_ABSORB_PRIORITY;
    }

    default boolean canAbsorb(ISun sun) {
        return true;
    }

    void onAbsorb(ISun sun);

    Vec3 getAbsorbPosition();

}