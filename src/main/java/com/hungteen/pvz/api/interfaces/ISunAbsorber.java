package com.hungteen.pvz.api.interfaces;


public interface ISunAbsorber {
    default boolean canAbsorb(ISun sun) {
        return true;
    }
    void onAbsorb(ISun sun);
    int getContainingSun();
}
