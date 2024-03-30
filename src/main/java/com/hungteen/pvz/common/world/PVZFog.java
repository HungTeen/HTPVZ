package com.hungteen.pvz.common.world;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.network.PVZFogPacket;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.common.register.PVZParticles;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.ViewportEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class PVZFog {
    public ResourceLocation dimension;
    public double lifeLeft;
    public Vec3 position;
    public double strength; //greater than 0.
    public double range;
    public double effect = 0;
    public final UUID uuid;
    private static List<PVZFog> pvzFogs = new ArrayList<>();
    private static Random random = new Random();
    private static float bufferStrength = 1;

    public PVZFog(Level level, Vec3 position, int lifeTime, double strength, double range) {
        this(level.dimension().location(), position, lifeTime, strength, range, UUID.randomUUID());
    }
    private PVZFog(ResourceLocation dimension, Vec3 position, double lifeTime, double strength, double range, UUID uuid) {
        this.dimension = dimension;
        this.position = position;
        this.lifeLeft = lifeTime;
        this.strength = strength;
        this.range = range;
        this.uuid = uuid;
        pvzFogs.add(this);
    }

    public static PVZFog addFog(ResourceLocation dimension, Vec3 position, double lifeTime, double strength, double range, UUID uuid) {
        PVZFog fog = getFog(uuid);
        if (fog != null) {
            fog.dimension = dimension;
            fog.position = position;
            fog.lifeLeft = lifeTime;
            fog.strength = strength;
            fog.range = range;
        } else {
            fog = new PVZFog(dimension, position, lifeTime, strength, range, uuid);
        }
        return fog;
    }

    public double getStrengthAt(Level level, Vec3 position) {
        if (! level.dimension().location().equals(this.dimension)) {
            return 0;
        }
        return Math.max(
                (range - this.position.multiply(1, range > 5 ? range / 5 : 1, 1)
                .distanceTo(position.multiply(1, range > 5 ? range / 5 : 1, 1))) / range * 5 * 0.5 + 0.5, 0) * strength;
    }

    public UUID getUuid() {
        return uuid;
    }

    //static methods
    public static double getFogStrengthAt(Level level, Vec3 position) {
        double strength = 0;
        for (PVZFog fog : pvzFogs) {
            strength = fog.effect * Math.max(fog.getStrengthAt(level, position), strength);
        }
        return strength;
    }

    public static void serverFogsTick() {
        for (PVZFog pvzFog : pvzFogs) {
            pvzFog.lifeLeft -= 0.025;
            PVZMod.LOGGER.info("FOG TICK");
        }
    }

    public static void clientFogsTick(double tickTime) {
        if (! Minecraft.getInstance().isPaused()) {
            for (int i = 0; i < pvzFogs.size(); i ++) {
                PVZFog fog = pvzFogs.get(i);
                fog.lifeLeft -= tickTime / 2;
                Player player = ClientProxy.getPlayer();
                if (player != null) {
                    boolean playerInFog = getFogStrengthAt(player.level, player.position()) >= 0.5;
                    float particleNum = (float) (fog.range * fog.range * fog.strength / (playerInFog ? 3000 : 500));
                    while (random.nextFloat() < particleNum) {
                        particleNum --;
                        double yOffset = (random.nextDouble(10) - 5) * (playerInFog ? random.nextFloat() : 1);
                        double yRot = random.nextDouble(Math.PI * 2);
                        double radius = random.nextDouble(fog.range);
                        if (player.level.dimension().location().equals(fog.dimension)) {
                            player.level.addParticle(PVZParticles.FOG.get(),
                                    fog.position.x + radius * Math.sin(yRot) + 5,//5 for particle motion.
                                    fog.position.y + yOffset,
                                    fog.position.z  + radius * Math.cos(yRot),
                                    0, 0, 0);
                        }
                    }
                    if (fog.getStrengthAt(ClientProxy.getPlayer().level, ClientProxy.getPlayer().position()) > 0) {
                        fog.effect += tickTime / 20;
                    } else {
                        fog.effect -= tickTime / 20;
                    }
                    fog.effect = Math.max(Math.min(Math.min(fog.lifeLeft, 1), fog.effect), 0);
                    if (fog.lifeLeft < 0) {
                        pvzFogs.remove(fog);
                    }
                }
            }
        }
    }

    public static PVZFog getFog(UUID uuid) {
        PVZFog result = null;
        for (PVZFog fog : pvzFogs) {
            if (fog.getUuid() == uuid) {
                result = fog;
            }
        };
        return result;
    }

    public static CompoundTag serialize() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("size", pvzFogs.size());
        int count = 0;
        for (PVZFog fog : pvzFogs) {
            CompoundTag fogTag = new CompoundTag();
            fogTag.putString("dimension", fog.dimension.toString());
            fogTag.putDouble("x", fog.position.x);
            fogTag.putDouble("y", fog.position.y);
            fogTag.putDouble("z", fog.position.z);
            fogTag.putDouble("timeLeft", fog.lifeLeft);
            fogTag.putDouble("strength", fog.strength);
            fogTag.putDouble("range", fog.range);
            fogTag.putUUID("uuid", fog.uuid);
            tag.put("fog_" + count, fogTag);
        }
        return tag;
    }
    public static void deserialize(CompoundTag nbt) {
        int size = nbt.getInt("size");
        for (int i = 0; i < size; i ++) {
            CompoundTag fogTag = (CompoundTag) nbt.get("fog_" + i);
            PVZFogPacket.fog(new ResourceLocation(fogTag.getString("dimension")),
                    new Vec3(fogTag.getDouble("x"), fogTag.getDouble("y"), fogTag.getDouble("z")),
                    fogTag.getDouble("timeLeft"), fogTag.getDouble("strength"), fogTag.getDouble("range"),
                    fogTag.getUUID("uuid"));
        }
    }
    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void handleFogs(ViewportEvent.RenderFog ev) {
        ev.setCanceled(true);
        if (ClientProxy.getPlayer() != null) {
            double strength = getFogStrengthAt(ClientProxy.getPlayer().level, ClientProxy.getPlayer().position());
            if (ClientProxy.getPlayer().hasEffect(PVZMobEffects.BRIGHTNESS.get())) {
                bufferStrength = (float) (bufferStrength * 0.95 + 0.0125);
            } else {
                bufferStrength = (float) (bufferStrength * 0.95 + 0.05);
            }
            strength = strength * bufferStrength;
            if (strength > 0) {
                ev.setNearPlaneDistance(0);
                ev.setFarPlaneDistance(
                        (float) (0.01 + 50 / (strength + 50 / ev.getFarPlaneDistance()))
                );
            }
        }
    }
}
