package com.hungteen.pvz;

import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;

public class PVZAPI implements com.hungteen.pvz.api.PVZAPI.IPVZAPI {
    @Override
    public String getSunString() {
        return PVZPlayerCapNBT.SUN;
    }
}
