package com.hungteen.pvz.common.network;

import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.world.PVZSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Available sending from server to client.
 */
public class TeamEvilnessPacket {
    private final ServerLevel level;
    private final Set<String> evilSet;
    public TeamEvilnessPacket(ServerLevel level) {
        this.level = level;
        this.evilSet = new HashSet<>();
    }
    public TeamEvilnessPacket(FriendlyByteBuf buf) {
        this.level = null;
        this.evilSet = new HashSet<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i ++) {
            evilSet.add(buf.readUtf());
        }
    }
    public void toBytes(FriendlyByteBuf buf) {
        this.evilSet.clear();
        Collection<PlayerTeam> teams = level.getScoreboard().getPlayerTeams();
        teams.forEach(team -> {
            if (PVZSavedData.isEvil(level.getScoreboard(), team.getName())) {
                this.evilSet.add(team.getName());
            }
        });
        buf.writeInt(evilSet.size());
        evilSet.forEach(buf::writeUtf);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> PVZPlayerCapability.getPlayerData(ClientProxy.getPlayer()).ifPresent(nbt -> {
            PVZSavedData.clientEvilList = this.evilSet;
        }));
        ctx.get().setPacketHandled(true);
    }


    //method
    public static void sync(ServerPlayer player, ServerLevel level){
        PVZPacketHandler.sendToClient(player, new TeamEvilnessPacket(level));
    }
}
