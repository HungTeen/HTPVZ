package com.hungteen.pvz.common.network;

import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.LogicalSide;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

public class EnderSeedBundleContainerPacket {
    final int slot;
    final List<ItemStack> items = new ArrayList<>();
    public EnderSeedBundleContainerPacket(Player player, int slot) {
        this.slot = slot;
        if (slot < 0) {
            for (int i = 0; i < 9; i ++) {
                items.add(PVZPlayerCapability.getEnderSeedBundleSlot(player, i));
            }
        } else {
            items.add(PVZPlayerCapability.getEnderSeedBundleSlot(player, slot));
        }
    }

    public EnderSeedBundleContainerPacket(FriendlyByteBuf buf) {
        slot = buf.readInt();
        if (this.slot < 0) {
            for (int i = 0; i < 9; i ++) {
                items.add(buf.readItem());
            }
        } else {
            items.add(buf.readItem());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(slot);
        for (ItemStack item : items) {
            buf.writeItem(item);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Player player = ctx.get().getDirection().getOriginationSide() == LogicalSide.SERVER ? ClientProxy.getPlayer() : ctx.get().getSender();
            if (player == null) return;
            if (this.slot < 0) {
                for (int i = 0; i < 9; i ++) {
                    PVZPlayerCapability.setEnderSeedBundleSlot(player, i, items.get(i));
                }
            } else {
                PVZPlayerCapability.setEnderSeedBundleSlot(player, slot, items.get(0));
            }
            player.getCapability(PVZPlayerCapability.CAP).ifPresent(cap -> cap.clientBundleContainerDirty = -2);
        });
        ctx.get().setPacketHandled(true);
    }

    public static void syncToClient(ServerPlayer player, int slot) {
        PVZPacketHandler.sendToClient(player, new EnderSeedBundleContainerPacket(player, slot));
    }

    @OnlyIn(Dist.CLIENT)
    public static void syncToServer(int slot) {
        PVZPacketHandler.sendToServer(new EnderSeedBundleContainerPacket(ClientProxy.getPlayer(), slot));
    }

}
