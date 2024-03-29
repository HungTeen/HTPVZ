package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.common.network.PVZEntityInteractPacket;
import com.hungteen.pvz.common.network.PVZPacketHandler;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

public interface IEntityPacketHandler {
    void handlePVZPacket(ServerPlayer player, int val);
    default void sendPVZPacketToServer() {
        sendPVZPacketToServer(0);
    }

    default void sendPVZPacketToServer(int val) {
        PVZPacketHandler.sendToServer(new PVZEntityInteractPacket((Entity) this, val));
    }
}
