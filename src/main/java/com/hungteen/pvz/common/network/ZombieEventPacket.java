package com.hungteen.pvz.common.network;

import com.hungteen.pvz.common.register.PVZZombieEvents;
import com.hungteen.pvz.api.ZombieEvent;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

/**PVZMod use this packet to sync ZombieEvents every second. If needed, you can manually sync them.*/
public class ZombieEventPacket {
    UUID uuid;
    CompoundTag eventTag;
    public ZombieEventPacket(UUID uuid, CompoundTag eventTag) {
        this.uuid = uuid;
        this.eventTag = eventTag;
    }

    public ZombieEventPacket(FriendlyByteBuf buf) {
        uuid = buf.readUUID();
        eventTag = buf.readNbt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeNbt(eventTag);
    }


    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> ClientProxy.getLevel().getCapability(PVZZombieEventCapability.CAP).ifPresent(cap -> {
            ZombieEvent event = cap.getEvent(uuid);
            if (event == null) {
                cap.addEvent(PVZZombieEvents.fromTag(ClientProxy.getLevel(), uuid, eventTag));
            } else {
                if (eventTag.contains("removal") && eventTag.getBoolean("removal")) {
                    cap.getEvents().remove(event);
                }
                event.deserializeNBT(eventTag);
            }
        }));
        ctx.get().setPacketHandled(true);
    }


    //methods
    public static void toClient(ZombieEvent event) {
        PVZPacketHandler.sendToNearByClient(event.level, Vec3.atCenterOf(event.position), event.range, new ZombieEventPacket(event.uuid, event.serializeNBT()));
    }
    public static void removalToClient(ZombieEvent event) {
        CompoundTag tag = new CompoundTag();
        tag.putBoolean("removal", true);
        PVZPacketHandler.sendToLevel(event.level, new ZombieEventPacket(event.uuid, tag));
    }
}
