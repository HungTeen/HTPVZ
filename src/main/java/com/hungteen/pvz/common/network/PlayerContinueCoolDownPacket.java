package com.hungteen.pvz.common.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

public class PlayerContinueCoolDownPacket {
    public List<String> name = new ArrayList<>();
    public List<Integer> start = new ArrayList<>();
    public List<Integer> end = new ArrayList<>();

    public PlayerContinueCoolDownPacket(Player player) {
        Map<Item, ItemCooldowns.CooldownInstance> coolDowns = player.getCooldowns().cooldowns;
        int cur = player.getCooldowns().tickCount;
        for (Item item: coolDowns.keySet()) {
            name.add(ForgeRegistries.ITEMS.getKey(item).toString());
            ItemCooldowns.CooldownInstance instance = coolDowns.get(item);
            start.add(instance.startTime - cur);
            end.add(instance.endTime - cur);
        }
    }
    public PlayerContinueCoolDownPacket(FriendlyByteBuf buf) {
        while (true) {
            try {
                name.add(buf.readUtf());
                start.add(buf.readInt());
                end.add(buf.readInt());
            } catch (Exception e) {
                break;
            }
        }
    }
    public void toBytes(FriendlyByteBuf buf) {
        for (int i = 0; i < name.size(); i ++) {
            buf.writeUtf(name.get(i));
            buf.writeInt(start.get(i));
            buf.writeInt(end.get(i));
        }
    }
    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ItemCooldowns coolDowns = ClientProxy.getPlayer().getCooldowns();
            for (int i = 0; i < name.size(); i ++) {
                Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(name.get(i)));
                coolDowns.cooldowns.put(item, new ItemCooldowns.CooldownInstance(coolDowns.tickCount + start.get(i), coolDowns.tickCount + end.get(i)));
            }
        });
        ctx.get().setPacketHandled(true);
    }

    //methods
    public static void sync(ServerPlayer player) {
        var packet = new PlayerContinueCoolDownPacket(player);
        if (! packet.name.isEmpty()) {
            PVZPacketHandler.sendToClient(player, packet);
        }
    }
}
