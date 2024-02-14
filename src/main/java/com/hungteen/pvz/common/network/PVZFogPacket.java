package com.hungteen.pvz.common.network;

import com.hungteen.pvz.common.world.PVZFog;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class PVZFogPacket {
    private ResourceLocation dimension;
    private Vec3 position;
    private double lifeTime;
    private double strength;
    private double range;
    private final UUID uuid;
    private ModifyType modifyType;

    //to client
    private PVZFogPacket(ResourceLocation level, Vec3 position, double lifeTime, double strength, double range, UUID uuid) {
        this.dimension = level;
        this.position = position;
        this.lifeTime = lifeTime;
        this.strength = strength;
        this.range = range;
        this.uuid = uuid;
        this.modifyType = ModifyType.NEW;
    }
    private PVZFogPacket(ModifyType type, double value, UUID uuid){
        this.uuid = uuid;
        this.modifyType = type;
        if (type == ModifyType.LIFE_TIME) {
            this.lifeTime = value;
        } else if (type == ModifyType.STRENGTH) {
            this.strength = value;
        } else if (type == ModifyType.RANGE) {
            this.range = value;
        } else {
            this.modifyType = ModifyType.ERROR;
        }
    }

    private PVZFogPacket(Vec3 vec3, UUID uuid) {
        this.uuid = uuid;
        this.position = vec3;
        this.modifyType = ModifyType.POSITION;
    }

    //to server
    private PVZFogPacket(UUID uuid) {
        this.uuid = uuid;
        this.modifyType = ModifyType.REQUIRE_FOG;
    }

    public PVZFogPacket(FriendlyByteBuf buf) {
        this.modifyType = ModifyType.fromValue(buf.readInt());
        this.uuid = buf.readUUID();
        if (modifyType == ModifyType.NEW) {
            this.dimension = new ResourceLocation(buf.readUtf());
            this.position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
            this.lifeTime = buf.readDouble();
            this.strength = buf.readDouble();
            this.range = buf.readDouble();
        } else if (modifyType == ModifyType.RANGE){
            this.range = buf.readDouble();
        } else if (modifyType == ModifyType.LIFE_TIME) {
            this.lifeTime = buf.readDouble();
        } else if (modifyType == ModifyType.STRENGTH) {
            this.strength = buf.readDouble();
        } else if (modifyType == ModifyType.POSITION) {
            this.position = new Vec3(buf.readDouble(), buf.readDouble(), buf.readDouble());
        }
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(modifyType.value);
        buf.writeUUID(uuid);
        if (modifyType == ModifyType.NEW) {
            buf.writeUtf(dimension.toString());
            buf.writeDouble(position.x);
            buf.writeDouble(position.y);
            buf.writeDouble(position.z);
            buf.writeDouble(lifeTime);
            buf.writeDouble(strength);
            buf.writeDouble(range);
        } else if (modifyType == ModifyType.RANGE){
            buf.writeDouble(range);
        } else if (modifyType == ModifyType.LIFE_TIME) {
            buf.writeDouble(lifeTime);
        } else if (modifyType == ModifyType.STRENGTH) {
            buf.writeDouble(strength);
        } else if (modifyType == ModifyType.POSITION) {
            buf.writeDouble(position.x);
            buf.writeDouble(position.y);
            buf.writeDouble(position.z);
        }
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            if (modifyType == ModifyType.NEW) {
                //in client
                PVZFog.addFog(dimension, position, lifeTime, strength, range, uuid);
            } else if (modifyType == ModifyType.REQUIRE_FOG) {
                //in server
                PVZFog fog = PVZFog.getFog(uuid);
                PVZPacketHandler.sendToPlayers(new PVZFogPacket(fog.dimension, fog.position, fog.lifeLeft, fog.strength, fog.range, uuid));
            } else if (modifyType != ModifyType.ERROR) {
                //in client
                PVZFog fog = PVZFog.getFog(uuid);
                if (fog != null) {
                    switch (modifyType) {
                        case RANGE -> fog.range = this.range;
                        case POSITION -> fog.position = this.position;
                        case LIFE_TIME -> fog.lifeLeft = this.lifeTime;
                    }
                } else {
                    requireFog(uuid);
                }
            }
        });
        ctx.get().setPacketHandled(true);
    }
    public enum ModifyType {
        NEW(0), RANGE(1), LIFE_TIME(2), STRENGTH(3), POSITION(4), ERROR(5), REQUIRE_FOG(6);
        final int value;

        ModifyType(int value) {
            this.value = value;
        }
        static ModifyType fromValue(int value) {
            for (ModifyType type: ModifyType.values()) {
                if (type.value == value) {
                    return type;
                }
            }
            return null;
        }
    }

    //methods
        //server to client
    public static void fog(Level level, Vec3 position, double lifeTime, double strength, double range, UUID uuid) {
        fog(level.dimension().location(), position, lifeTime, strength, range, uuid);
    }
    public static void fog(ResourceLocation dimension, Vec3 position, double lifeTime, double strength, double range, UUID uuid) {
        PVZFog.addFog(dimension, position, lifeTime, strength, range, uuid);
        PVZPacketHandler.sendToPlayers(new PVZFogPacket(dimension, position, lifeTime, strength, range, uuid));
    }
    public static void modifyFog(UUID uuid, ModifyType type, double value) {
        PVZPacketHandler.sendToPlayers(new PVZFogPacket(type, value, uuid));
    }
    public static void modifyFog(UUID uuid, Vec3 position) {
        PVZPacketHandler.sendToPlayers(new PVZFogPacket(position, uuid));
    }
        //client to server
    public static void requireFog(UUID uuid) {
        PVZPacketHandler.sendToServer(new PVZFogPacket(uuid));
    }
}
