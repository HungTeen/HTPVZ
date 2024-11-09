package com.hungteen.pvz.common.network;

import com.hungteen.pvz.common.entity.plants.Plantern;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashSet;
import java.util.List;
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
                List<Plantern> list = player.level.getEntities(EntityTypeTest.forClass(Plantern.class),
                        new AABB(player.getX() - 200, player.getY() - 200, player.getZ() - 200,
                                player.getX() + 200, player.getY() + 200, player.getZ() + 200),
                        (plantern) -> set.contains(plantern.getUUID()));
                list.forEach(Plantern::refreshSkillGlowTime);
            }
        });
        ctx.get().setPacketHandled(true);
    }
}
