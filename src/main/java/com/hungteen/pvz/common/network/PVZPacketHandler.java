package com.hungteen.pvz.common.network;

import com.hungteen.pvz.Util;
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

        CHANNEL.registerMessage(id ++, SpawnParticlePacket.class, SpawnParticlePacket::toBytes, SpawnParticlePacket::new, SpawnParticlePacket::handle);
        CHANNEL.registerMessage(id ++, PlayerCapPacket.class, PlayerCapPacket::toBytes, PlayerCapPacket::new, PlayerCapPacket::handle);
    }

    public static <MSG> void sendToServer(MSG msg){
        CHANNEL.sendToServer(msg);
    }

    public static <MSG> void sendToClient(ServerPlayer serverPlayer, MSG msg){
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> serverPlayer), msg);
    }

    public static <MSG> void sendToNearByClient(Level world, Vec3 vec, double dis, MSG msg){
        PVZPacketHandler.CHANNEL.send(PacketDistributor.NEAR.with(() -> {
            return new PacketDistributor.TargetPoint(vec.x, vec.y, vec.z, dis, world.dimension());
        }), msg);
    }
//    TODO public static <MSG> void sendToClients(Level world, MSG msg){
//        CHANNEL.send(PacketDistributor.PLAYER.with(() -> p), msg);
//    }
}
