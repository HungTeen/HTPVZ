package com.hungteen.pvz.common.network;

import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class PlayerKnockBackPacket {
    final Vec3 knockBack;
    final boolean relative;

    public PlayerKnockBackPacket(Vec3 knockBack, boolean relative) {
        this.relative = relative;
        this.knockBack = knockBack;
    }

    public PlayerKnockBackPacket(FriendlyByteBuf buf) {
        this.knockBack = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        this.relative = buf.readBoolean();
    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeDouble(knockBack.x);
        buf.writeDouble(knockBack.y);
        buf.writeDouble(knockBack.z);
        buf.writeBoolean(relative);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (ClientProxy.getPlayer() instanceof LocalPlayer player) {
                player.setDeltaMovement((relative ? player.getDeltaMovement() : Vec3.ZERO).add(knockBack));
            }
        });
        ctx.get().setPacketHandled(true);
    }

    //methods
    public static void knockBack(ServerPlayer player, Vec3 knockBack, boolean relative) {
        PVZPacketHandler.sendToClient(player, new PlayerKnockBackPacket(knockBack, relative));
    }
}
