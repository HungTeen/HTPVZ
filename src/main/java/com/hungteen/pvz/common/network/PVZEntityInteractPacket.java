package com.hungteen.pvz.common.network;

import com.hungteen.pvz.common.entity.IEntityPacketHandler;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PVZEntityInteractPacket {
    private final UUID entityID;
    private final int type;

    public PVZEntityInteractPacket(Entity entity, int type) {
        this.entityID = entity.getUUID();
        this.type = type;
    }

    public PVZEntityInteractPacket(FriendlyByteBuf buf) {
        this.entityID = buf.readUUID();
        this.type = buf.readInt();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(entityID);
        buf.writeInt(type);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        NetworkEvent.Context context = ctx.get();
        if (((ServerLevel) context.getSender().level).getEntity(this.entityID) instanceof IEntityPacketHandler entity) {
            entity.handlePVZPacket(type);
        }
    }
}
