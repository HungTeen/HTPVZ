package com.hungteen.pvz.common.network;

import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

public class PVZEntityCapPacket {
    public static final Map<UUID, CompoundTag> clientPacketsToHandle = new HashMap<>();
    private final PVZEntityCapability cap;
    private final UUID uuid;
    private final int stuckArrowWithATarget;

    public PVZEntityCapPacket(UUID uuid, PVZEntityCapability cap) {
        this.cap = cap;
        this.uuid = uuid;
        this.stuckArrowWithATarget = cap.getStuckArrowWithATarget();
    }

    public PVZEntityCapPacket(FriendlyByteBuf buf) {
        cap = null;
        this.uuid = buf.readUUID();
        this.stuckArrowWithATarget = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(uuid);
        buf.writeInt(cap.getStuckArrowWithATarget());
    }

    private CompoundTag toCompoundTag() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("stuckArrowWithATarget", stuckArrowWithATarget);
        return tag;
    }
    public static void read(UUID uuid, PVZEntityCapability cap) {
        if (PVZEntityCapPacket.clientPacketsToHandle.containsKey(uuid)) {
            CompoundTag tag = PVZEntityCapPacket.clientPacketsToHandle.get(uuid);
            cap.setStuckArrowWithATarget(tag.getInt("stuckArrowWithATarget"));
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            clientPacketsToHandle.put(uuid, toCompoundTag());
        });
        ctx.get().setPacketHandled(true);
    }

    //methods
    public static void sync(UUID uuid, PVZEntityCapability cap) {
        if (cap.isDirty) {
            PVZPacketHandler.sendToPlayers(new PVZEntityCapPacket(uuid, cap));
            cap.isDirty = false;
        }
    }
}
