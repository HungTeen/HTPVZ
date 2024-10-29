package com.hungteen.pvz.common.network;

import com.hungteen.pvz.util.Util;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

public class PVZPacketHandler {
    private static int id = 0;
    private static SimpleChannel CHANNEL;

    public static void init() {
        CHANNEL = NetworkRegistry.ChannelBuilder
                .named(Util.prefix("networking"))
                .networkProtocolVersion(() -> "1.0")
                .clientAcceptedVersions(s -> true)
                .serverAcceptedVersions(s -> true)
                .simpleChannel();
        //SERVER TO CLIENT.
        CHANNEL.registerMessage(id ++, PlayerCapPacket.class, PlayerCapPacket::toBytes, PlayerCapPacket::new, PlayerCapPacket::handle);
        CHANNEL.registerMessage(id ++, SpawnParticlePacket.class, SpawnParticlePacket::toBytes, SpawnParticlePacket::new, SpawnParticlePacket::handle);
        CHANNEL.registerMessage(id ++, PlayerCoolDownPacket.class, PlayerCoolDownPacket::toBytes, PlayerCoolDownPacket::new, PlayerCoolDownPacket::handle);
        CHANNEL.registerMessage(id ++, DropDamagedArmorPacket.class, DropDamagedArmorPacket::toBytes, DropDamagedArmorPacket::new, DropDamagedArmorPacket::handle);
        CHANNEL.registerMessage(id ++, PlanternRefreshGlowPacket.class, PlanternRefreshGlowPacket::toBytes, PlanternRefreshGlowPacket::new, PlanternRefreshGlowPacket::handle);
        CHANNEL.registerMessage(id ++, ZombieEventPacket.class, ZombieEventPacket::toBytes, ZombieEventPacket::new, ZombieEventPacket::handle);
        CHANNEL.registerMessage(id ++, PlayerKnockBackPacket.class, PlayerKnockBackPacket::toBytes, PlayerKnockBackPacket::new, PlayerKnockBackPacket::handle);
        //CLIENT TO SERVER.
        CHANNEL.registerMessage(id ++, PVZAddSkillPacket.class, PVZAddSkillPacket::toBytes, PVZAddSkillPacket::new, PVZAddSkillPacket::handle);
        CHANNEL.registerMessage(id ++, PVZEntityInteractPacket.class, PVZEntityInteractPacket::toBytes, PVZEntityInteractPacket::new, PVZEntityInteractPacket::handle);
        //BETWEEN SIDES
        CHANNEL.registerMessage(id ++, PVZFogPacket.class, PVZFogPacket::toBytes, PVZFogPacket::new, PVZFogPacket::handle);
    }

    public static <MSG> void sendToServer(MSG msg){
        CHANNEL.sendToServer(msg);
    }

    public static <MSG> void sendToClient(ServerPlayer serverPlayer, MSG msg){
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), msg);
    }

    public static <MSG> void sendToNearByClient(Level world, Vec3 vec, double dis, MSG msg){
        PVZPacketHandler.CHANNEL.send(PacketDistributor.NEAR.with(() ->
                new PacketDistributor.TargetPoint(vec.x, vec.y, vec.z, dis, world.dimension())), msg);
    }

    public static <MSG> void sendToPlayers(MSG msg) {
        CHANNEL.send(PacketDistributor.ALL.noArg(), msg);
    }
}
