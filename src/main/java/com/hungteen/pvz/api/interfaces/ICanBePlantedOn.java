package com.hungteen.pvz.api.interfaces;


public interface ICanBePlantedOn {
    default boolean canHold(IPlant plant) {
        return true;
    }//TODO change to IPlant.
}
