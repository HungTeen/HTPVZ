package com.hungteen.pvz.common.network;

import com.hungteen.pvz.common.entity.plants.Plantern;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.function.Supplier;

/**
 * Available sending from server to client. Used only for Planterns sync glowing data. Because team is not available in client this need to be synced from ths server.
 */
public class PlanternRefreshGlowPacket {
    public final Set<UUID> set;
    public PlanternRefreshGlowPacket(Set<UUID> set){
        this.set = set;
    }
    public PlanternRefreshGlowPacket(FriendlyByteBuf buf) {
        short length = buf.readShort();
        set = new HashSet<>();
        for (short i = 0; i < length; i ++) {
            set.add(buf.readUUID());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeShort(set.size());
        for (UUID id : set) {
            buf.writeUUID(id);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ClientProxy.getPlayer();
            if (player != null) {
                for (UUID uuid : set) {
                    Entity entity = ((ClientLevel) player.level).getEntities().get(uuid);
                    if (entity instanceof Plantern plantern) {
                        plantern.refreshSkillGlowTime();
                    }
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
