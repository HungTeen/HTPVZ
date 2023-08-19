package com.hungteen.pvz.common.network;

import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Available sending from server to client.
 */
public class PlayerCapPacket {
    private boolean type;//True for value, false for limit.
    private int value;
    private int value2;
    private String key;

    public PlayerCapPacket(String key, int value){
        this.type = true;
        this.key = key;
        this.value = value;
    }
    public PlayerCapPacket(String key, int limitMin, int limitMax){
        this.type = false;
        this.key = key;
        this.value = limitMin;
        this.value2 = limitMax;
    }
    public PlayerCapPacket(FriendlyByteBuf buf) {
        this.type = buf.readBoolean();
        this.key = buf.readUtf();
        this.value = buf.readInt();
        if (! type) {
            this.value2 = buf.readInt();
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBoolean(type);
        buf.writeUtf(key);
        buf.writeInt(value);
        if (! type){
            buf.writeInt(value2);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            PVZPlayerCapability.getPlayerData(ClientProxy.getPlayer()).ifPresent(nbt -> {
                if (type) {
                    nbt.setValue(key, value);
                } else {
                    nbt.setValueLimit(key, value, value2);
                }
            });
        });
        ctx.get().setPacketHandled(true);
    }


    //method
    public static void sync(ServerPlayer player, String key, Boolean valueOrLimit){
        if (valueOrLimit) {
            PVZPacketHandler.sendToClient(player, new PlayerCapPacket(key, PVZPlayerCapability.getValue(player, key)));
        } else {
            Pair<Integer, Integer> limit = PVZPlayerCapability.getValueLimit(player, key);
            PVZPacketHandler.sendToClient(player, new PlayerCapPacket(key, limit.getFirst(), limit.getSecond()));
        }
    }
}
