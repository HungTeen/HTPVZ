package com.hungteen.pvz.common.network;

import net.minecraft.core.particles.ParticleType;
import net.minecraft.core.particles.SimpleParticleType;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class SpawnParticlePacket {
    private String type;
    private double x;
    private double y;
    private double z;

    public SpawnParticlePacket(String type, double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.type = type;
    }

    public SpawnParticlePacket(FriendlyByteBuf buf) {
        this.type = buf.readUtf();
        this.x = buf.readDouble();
        this.y = buf.readDouble();
        this.z = buf.readDouble();
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(this.type);
        buf.writeDouble(x);
        buf.writeDouble(y);
        buf.writeDouble(z);
    }

    public void handle(Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            ParticleType<?> particle = ForgeRegistries.PARTICLE_TYPES.getValue(new ResourceLocation(type));
            if(particle instanceof SimpleParticleType){
                ClientProxy.getPlayer().level.addParticle(((SimpleParticleType) particle).getType(), x, y, z, 0, 0, 0);
            }
        });
        ctx.get().setPacketHandled(true);
    }


    //methods
    public static void particle(Level level, ParticleType<?> particleType, Vec3 vec){
        //TODO change to sendToClients.
        PVZPacketHandler.sendToNearByClient(level, vec, 50, new SpawnParticlePacket(ForgeRegistries.PARTICLE_TYPES.getKey(particleType).toString(), vec.x, vec.y, vec.z));
    }
}
