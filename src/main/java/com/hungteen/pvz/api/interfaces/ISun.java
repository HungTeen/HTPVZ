package com.hungteen.pvz.api.interfaces;

/**
 * ISun can be absorbed by ISunAbsorber. It doesn't directly interact with ISunContainer but ISunContainers may call
 * {@link ISun#getAmount()}.
 */
public interface ISun extends ISunAbsorber, ISunContainer {

    @Override
    default int getCapacity() {
        return 150;
    }

    void onAbsorbedBy(ISunAbsorber entity);

    boolean canAttractThis(ISunAbsorber entity);

    /**
     * @return Can be ISunAbsorber or player.
     */
    ISunAbsorber getAttractor();

    /**
     * @return whether succeeded in setting attractor.
     */
    default boolean setAttractor(ISunAbsorber attractor) {
        return false;
    }

}
