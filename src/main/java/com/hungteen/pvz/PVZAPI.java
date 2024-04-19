package com.hungteen.pvz;

import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.world.entity.Entity;

public class PVZAPI implements com.hungteen.pvz.api.PVZAPI.IPVZAPI {
    @Override
    public String getSunString() {
        return PVZPlayerCapNBT.SUN;
    }

    @Override
    public boolean isTeammate(Entity A, Entity B) {
        return EntityUtil.isTeammate(A, B);
    }
}
