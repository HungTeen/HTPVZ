package com.hungteen.pvz.common.network;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.world.PVZSavedData;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.network.NetworkEvent;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Supplier;

/**
 * Available sending from server to client.
 */
public class ServerInfoPacket {
    private final ServerLevel level;
    private final boolean teamBattle;
    private final int advancedPlantExtraCostRange;
    private final Set<String> evilSet;
    public ServerInfoPacket(ServerLevel level) {
        this.level = level;
        this.teamBattle = PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.teamBattle);
        this.advancedPlantExtraCostRange = PVZConfig.PVZGameRules.getInt(level, PVZConfig.Common.advancedPlantExtraCostRange);
        this.evilSet = new HashSet<>();
    }
    public ServerInfoPacket(FriendlyByteBuf buf) {
        this.level = null;
        this.teamBattle = buf.readBoolean();
        this.advancedPlantExtraCostRange = buf.readInt();
        this.evilSet = new HashSet<>();
        int size = buf.readInt();
        for (int i = 0; i < size; i ++) {
            evilSet.add(buf.readUtf());
        }
    }
    public void toBytes(FriendlyByteBuf buf) {
        this.evilSet.clear();
        buf.writeBoolean(teamBattle);
        buf.writeInt(advancedPlantExtraCostRange);
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
        Player player = ClientProxy.getPlayer();
        if (player == null) return;
        ctx.get().enqueueWork(() -> player.getCapability(PVZPlayerCapability.CAP).ifPresent(cap -> {
            cap.isTeamBattleOn = teamBattle;
            cap.advancedPlantsExtraCostRange = advancedPlantExtraCostRange;
            PVZSavedData.clientEvilList = this.evilSet;
        }));
        ctx.get().setPacketHandled(true);
    }


    //method
    public static void sync(ServerPlayer player, ServerLevel level) {
        if (! PVZSavedData.isDirty(level.getScoreboard())) return;
        AtomicBoolean needSync = new AtomicBoolean();
        player.getCapability(PVZPlayerCapability.CAP).ifPresent(cap -> {
            boolean teamBattleUnsynced = cap.isTeamBattleOn != PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.teamBattle);
            boolean extraCostUnsynced = cap.advancedPlantsExtraCostRange != PVZConfig.PVZGameRules.getInt(level, PVZConfig.Common.advancedPlantExtraCostRange);
            needSync.set(teamBattleUnsynced || extraCostUnsynced);
        });
        if (! needSync.get()) return;
        PVZPacketHandler.sendToClient(player, new ServerInfoPacket(level));
    }
}
