package com.hungteen.pvz.common.entity;

public interface ICanAbsorbSun {
    default boolean canAbsorb(Sun sun) {
        return true;
    }
    void onAbsorb(Sun sun);
}
