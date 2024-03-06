package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.common.network.PVZEntityInteractPacket;
import com.hungteen.pvz.common.network.PVZPacketHandler;
import net.minecraft.world.entity.Entity;

public interface IEntityPacketHandler {
    void handlePVZPacket(int val);

    default void sendPVZPacketToServer(int val) {
        PVZPacketHandler.sendToServer(new PVZEntityInteractPacket((Entity) this, val));
    }
}
