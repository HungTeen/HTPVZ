package com.hungteen.pvz;

import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import net.minecraft.world.entity.Entity;

public class PVZAPI implements com.hungteen.pvz.api.PVZAPI.IPVZAPI {
    @Override
    public String getSunString() {
        return PVZPlayerCapNBT.SUN;
    }

    @Override
    public boolean isTeammate(Entity A, Entity B) {
        return PVZOwnedCapability.isTeammate(A, B);
    }
}
