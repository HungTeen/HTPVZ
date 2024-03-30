package com.hungteen.pvz.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Available sending from server to client. Used only in Excitement mob effect in pvz.
 */
public class PlayerCoolDownPacket {
    public int coolDown;
    public PlayerCoolDownPacket(){
        this(10);
    }
    public PlayerCoolDownPacket(int coolDown){
        this.coolDown = coolDown;
    }
    public PlayerCoolDownPacket(FriendlyByteBuf buf) {
        buf.writeInt(coolDown);
    }

    public void toBytes(FriendlyByteBuf buf) {
        coolDown = buf.readInt();
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            for (int i = 0; i < coolDown; i ++) {
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
