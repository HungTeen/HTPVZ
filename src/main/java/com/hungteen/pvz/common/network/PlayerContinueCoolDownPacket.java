package com.hungteen.pvz.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class PlayerContinueCoolDownPacket {
    public String name;
    public int start;
    public int end;
    public PlayerContinueCoolDownPacket(String name, int start, int end) {
        this.name = name;
        this.start = start;
        this.end = end;
    }
    public PlayerContinueCoolDownPacket(FriendlyByteBuf buf) {
        name = buf.readUtf();
        start = buf.readInt();
        end = buf.readInt();
    }
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(name);
        buf.writeInt(start);
        buf.writeInt(end);
    }
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ItemCooldowns coolDowns = ClientProxy.getPlayer().getCooldowns();
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name));
            coolDowns.cooldowns.put(item, new ItemCooldowns.CooldownInstance(coolDowns.tickCount + start, coolDowns.tickCount + end));
        });
        ctx.get().setPacketHandled(true);
    }

    //methods
    public static void sync(ServerPlayer player, Item item, int start, int end) {
        PVZPacketHandler.sendToClient(player, new PlayerContinueCoolDownPacket(ForgeRegistries.ITEMS.getKey(item).toString(), start, end));
    }
}
