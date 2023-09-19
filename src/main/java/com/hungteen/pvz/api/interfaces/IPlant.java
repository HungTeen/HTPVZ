package com.hungteen.pvz.api.interfaces;

import net.minecraft.network.syncher.EntityDataAccessor;

public interface IPlant extends INeedSafeSituation{
    EntityDataAccessor<Boolean> root();
}
