package com.hungteen.pvz.common.network;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapStats;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.mojang.datafixers.util.Pair;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

/**
 * Available sending from server to client.
 */
public class PlayerCapStatsPacket {
    private final List<String> keys = new ArrayList<>();
    private final List<Short> values = new ArrayList<>();
    private final List<Short> limitMin = new ArrayList<>();
    private final List<Short> limitMax = new ArrayList<>();

    public PlayerCapStatsPacket(PVZPlayerCapStats stats, boolean syncAll) {
        for (String key : syncAll ? stats.getKeySet() : stats.getDirtyList()) {
            this.keys.add(key);
            this.values.add(stats.getValue(key).shortValue());
            Pair<Integer, Integer> pair = stats.getValueLimit(key);
            this.limitMin.add(pair.getFirst().shortValue());
            this.limitMax.add(pair.getSecond().shortValue());
        }
    }

    public PlayerCapStatsPacket(FriendlyByteBuf buf) {
        while (true) {
            try {
                keys.add(buf.readUtf());
                values.add(buf.readShort());
                limitMin.add(buf.readShort());
                limitMax.add(buf.readShort());
            } catch (Exception e) {
                break;
            }
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        for (int i = 0; i < keys.size(); i ++) {
            buf.writeUtf(keys.get(i));
            buf.writeShort(values.get(i));
            buf.writeShort(limitMin.get(i));
            buf.writeShort(limitMax.get(i));
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> PVZPlayerCapability.getPlayerData(ClientProxy.getPlayer()).ifPresent(nbt -> {
            for (int i = 0; i < keys.size(); i ++) {
                nbt.setValue(keys.get(i), values.get(i).intValue());
                nbt.setValueLimit(keys.get(i), limitMin.get(i).intValue(), limitMax.get(i).intValue());
            }
        }));
        PVZMod.LOGGER.info("packet received!");
        ctx.get().setPacketHandled(true);
    }


    //method
    public static void sync(ServerPlayer player, PVZPlayerCapStats stats, boolean syncAll) {
        PlayerCapStatsPacket packet = new PlayerCapStatsPacket(stats, syncAll);
        if (! packet.keys.isEmpty()) PVZPacketHandler.sendToClient(player, packet);
    }
}
