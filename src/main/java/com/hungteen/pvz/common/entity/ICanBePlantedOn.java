package com.hungteen.pvz.common.entity;

public interface ICanBePlantedOn {
    default boolean canHold(Plant plant) {
        return true;
    }
}
