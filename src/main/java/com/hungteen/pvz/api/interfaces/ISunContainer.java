package com.hungteen.pvz.api.interfaces;

import com.hungteen.pvz.common.world.PVZPlayerWrapper;

/**
 * ISunContainer includes all object that can store sun. When use player as a sun container, use the wrapper
 * {@link PVZPlayerWrapper PlayerSunContainerWrapper}.
 */
public interface ISunContainer {

    /**
     * set current amount
     * @param num sun amount
     */
    void setAmount(int num);

    /**
     * @return current sun amount
     */
    int getAmount();

    /**
     * If this is a LivingEntity, use attribute {@link com.hungteen.pvz.common.register.PVZAttributes#MAX_SUN} as
     * capacity of this entity.
     */
    int getCapacity();

}
