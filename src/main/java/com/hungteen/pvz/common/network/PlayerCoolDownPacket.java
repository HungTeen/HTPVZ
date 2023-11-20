package com.hungteen.pvz.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Available sending from server to client. Used only in Excitement mob effect in pvz.
 */
public class PlayerCoolDownPacket {
    public PlayerCoolDownPacket(){
    }
    public PlayerCoolDownPacket(FriendlyByteBuf buf) {
    }

    public void toBytes(FriendlyByteBuf buf) {
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            for (int i = 0; i < 10; i ++) {
                ClientProxy.getPlayer().getCooldowns().tick();
            }
        });
        ctx.get().setPacketHandled(true);
    }


    //method
    public static void clientCoolDown(ServerPlayer player){
            PVZPacketHandler.sendToClient(player, new PlayerCoolDownPacket());
    }
}
