package com.hungteen.pvz.common.network;

import com.hungteen.pvz.common.item.IDropWhenBroken;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class DropDamagedArmorPacket {
    Vec3 pos;
    String itemId;
    public DropDamagedArmorPacket(Item item, Vec3 pos) {
        itemId = ForgeRegistries.ITEMS.getKey(item).toString();
        this.pos = pos;
    }
    public DropDamagedArmorPacket(FriendlyByteBuf buf) {
        itemId = buf.readUtf();
        pos = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(itemId);
        buf.writeDouble(pos.x);
        buf.writeDouble(pos.y);
        buf.writeDouble(pos.z);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(itemId));
            if (item instanceof IDropWhenBroken droppingItem) {
                droppingItem.clientBroken(pos, ClientProxy.getPlayer().level);
            }
        });
        ctx.get().setPacketHandled(true);
    }


    //method
    public static void drop(IDropWhenBroken item, Level level, Vec3 pos) {
        PVZPacketHandler.sendToNearByClient(level, pos, 50, new DropDamagedArmorPacket((Item) item, pos));
    }
}
