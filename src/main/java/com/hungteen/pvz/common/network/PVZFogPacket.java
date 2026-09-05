package com.hungteen.pvz.common.network;

import com.hungteen.pvz.common.capability.level.PVZFogCapability;
import com.hungteen.pvz.common.world.PVZFog;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PVZFogPacket {
    private BlockPos position;
    private int lifeTime;
    private double strength;
    private double range;
    private final UUID uuid;
    private ModifyType modifyType;

    //to client
    public PVZFogPacket(BlockPos position, int lifeTime, double strength, double range, UUID uuid) {
        this.position = position;
        this.lifeTime = lifeTime;
        this.strength = strength;
        this.range = range;
        this.uuid = uuid;
        this.modifyType = ModifyType.NEW_OR_RESET;
    }
    public PVZFogPacket(PVZFog fog) {
        this.position = fog.position;
        this.lifeTime = fog.lifeLeft;
        this.strength = fog.strength;
        this.range = fog.range;
        this.uuid = fog.uuid;
        this.modifyType = ModifyType.NEW_OR_RESET;
    }
    public PVZFogPacket(ModifyType type, double value, UUID uuid){
        this.uuid = uuid;
        this.modifyType = type;
        if (type == ModifyType.LIFE_TIME) {
            this.lifeTime = (int) value;
        } else if (type == ModifyType.STRENGTH) {
            this.strength = value;
        } else if (type == ModifyType.RANGE) {
            this.range = value;
        } else if (type != ModifyType.REMOVE) {
            this.modifyType = ModifyType.ERROR;
        }
    }

    public PVZFogPacket(BlockPos pos, UUID uuid) {
        this.uuid = uuid;
        this.position = pos;
        this.modifyType = ModifyType.POSITION;
    }

    //to server
    public PVZFogPacket(UUID uuid) {
        this.uuid = uuid;
        this.modifyType = ModifyType.REQUIRE_FOG;
    }

    public PVZFogPacket(FriendlyByteBuf buf) {
        this.modifyType = ModifyType.fromValue(buf.readChar());
        this.uuid = buf.readUUID();
        if (modifyType == ModifyType.NEW_OR_RESET) {
            this.position = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
            this.lifeTime = (int) buf.readDouble();
            this.strength = buf.readDouble();
            this.range = buf.readDouble();
        } else if (modifyType == ModifyType.RANGE){
            this.range = buf.readDouble();
        } else if (modifyType == ModifyType.LIFE_TIME) {
            this.lifeTime = buf.readInt();
        } else if (modifyType == ModifyType.STRENGTH) {
            this.strength = buf.readDouble();
        } else if (modifyType == ModifyType.POSITION) {
            this.position = new BlockPos(buf.readInt(), buf.readInt(), buf.readInt());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeChar(modifyType.getValue());
        buf.writeUUID(uuid);
        if (modifyType == ModifyType.NEW_OR_RESET) {
            buf.writeInt(position.getX());
            buf.writeInt(position.getY());
            buf.writeInt(position.getZ());
            buf.writeDouble(lifeTime);
            buf.writeDouble(strength);
            buf.writeDouble(range);
        } else if (modifyType == ModifyType.RANGE){
            buf.writeDouble(range);
        } else if (modifyType == ModifyType.LIFE_TIME) {
            buf.writeInt(lifeTime);
        } else if (modifyType == ModifyType.STRENGTH) {
            buf.writeDouble(strength);
        } else if (modifyType == ModifyType.POSITION) {
            buf.writeInt(position.getX());
            buf.writeInt(position.getY());
            buf.writeInt(position.getZ());
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (modifyType == ModifyType.NEW_OR_RESET) {
                //in client
                PVZFogCapability.addOrResetFogSided(ClientProxy.getLevel(), position, lifeTime, strength, range, uuid);
            } else if (modifyType == ModifyType.REQUIRE_FOG) {
                //in server
                ServerPlayer player = ctx.get().getSender();
                if (player == null) return;
                player.level.getCapability(PVZFogCapability.CAP).ifPresent(cap -> {
                    PVZFog fog = PVZFogCapability.getFog(player.level, uuid);
                    PVZPacketHandler.sendToLevel(player.level, new PVZFogPacket(fog.position, fog.lifeLeft, fog.strength, fog.range, uuid));
                });
            } else if (modifyType != ModifyType.ERROR) {
                //in client
                PVZFog fog = PVZFogCapability.getFog(ClientProxy.getLevel(), uuid);
                if (fog != null) {
                    switch (modifyType) {
                        case RANGE -> fog.range = this.range;
                        case POSITION -> fog.position = this.position;
                        case LIFE_TIME -> fog.lifeLeft = this.lifeTime;
                        case REMOVE -> fog.lifeLeft = -1;
                    }
                } else {
                    PVZFogCapability.requireFog(uuid);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }

    public enum ModifyType {
        NEW_OR_RESET(0), RANGE(1), LIFE_TIME(2), STRENGTH(3), POSITION(4), ERROR(5), REQUIRE_FOG(6), REMOVE(7);
        private final char value;

        ModifyType(int value) {
            this.value = (char) value;
        }

        public static ModifyType fromValue(int value) {
            for (ModifyType type: ModifyType.values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return null;
        }

        public int getValue() {
            return value;
        }
    }
}
