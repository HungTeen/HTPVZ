package com.hungteen.pvz.common.network;

import com.hungteen.pvz.common.entity.zombies.ChorusTerminatorBoss;
import com.hungteen.pvz.common.entity.zombies.ChorusTerminatorPart;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

public class ChorusTerminatorSyncPacket {
    private ChorusTerminatorBoss boss;
    Map<String, Vec3> posMap = new HashMap<>();
    public ChorusTerminatorSyncPacket(ChorusTerminatorBoss boss) {
        this.boss = boss;
    }

    public ChorusTerminatorSyncPacket(FriendlyByteBuf buf) {
        Entity boss = ClientProxy.getLevel().getEntity(buf.readInt());
        if (boss instanceof ChorusTerminatorBoss b) {
            this.boss = b;
        } else {
            return;
        }
        boolean stopped = false;
        while (! stopped) {
            try {
                posMap.put(buf.readUtf(), new Vec3(buf.readFloat(), buf.readFloat(), buf.readFloat()));
            } catch (Exception e) {
                stopped = true;
            }
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(this.boss.getId());
        for (ChorusTerminatorPart part : boss.subEntities) {
            if (! part.needSync) continue;
            buf.writeUtf(part.name);
            buf.writeFloat((float) part.getX());
            buf.writeFloat((float) part.getY());
            buf.writeFloat((float) part.getZ());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (boss == null) return;
            for (ChorusTerminatorPart part : boss.subEntities) {
                if (! part.needSync) continue;
                if (posMap.containsKey(part.name)) {
                    part.setPos(posMap.get(part.name));
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public static void sync(ChorusTerminatorBoss boss) {
        PVZPacketHandler.sendToNearByClient(boss.level, boss.position(), 100, new ChorusTerminatorSyncPacket(boss));
    }
}
