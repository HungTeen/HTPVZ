package com.hungteen.pvz.common.capability.level;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.network.PVZFogPacket;
import com.hungteen.pvz.common.network.PVZPacketHandler;
import com.hungteen.pvz.common.world.PVZFog;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class PVZFogCapability implements ICapabilitySerializable<CompoundTag> {
    public Map<UUID, PVZFog> fogs = new HashMap<>();
    public final Level level;

    public PVZFogCapability(Level level) {
        this.level = level;
    }
    public static final Capability<PVZFogCapability> CAP = CapabilityManager.get(new CapabilityToken<>(){});

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CAP ? LazyOptional.of(() -> (T) this) : LazyOptional.empty();
    }

    public static void tick(TickEvent.ServerTickEvent ev) {
        ev.getServer().getAllLevels().forEach(level -> level.getCapability(PVZFogCapability.CAP).ifPresent(cap -> {
            for (PVZFog fog : Set.copyOf(cap.fogs.values())) {
                if (fog.lifeLeft % 60 == 0) {
                    PVZPacketHandler.sendToLevel(level, new PVZFogPacket(fog));
                }
                PVZMod.LOGGER.info(fog.lifeLeft + " ");
                fog.lifeLeft --;
                if (fog.targetPos != null && ! fog.position.equals(fog.targetPos)) {
                    fog.position = new BlockPos(
                            fog.position.getX() + Math.signum(fog.targetPos.getX() - fog.position.getX()),
                            fog.position.getY() + Math.signum(fog.targetPos.getY() - fog.position.getY()),
                            fog.position.getZ() + Math.signum(fog.targetPos.getZ() - fog.position.getZ())
                    );
                }
                if (fog.lifeLeft < 0) {
                    cap.fogs.remove(fog.uuid);
                }
            }
        }));
    }

    public static @Nullable PVZFog getFog(Level level, UUID uuid) {
        final PVZFog[] result = new PVZFog[1];
        result[0] = null;
        level.getCapability(CAP).ifPresent(cap ->  {
            result[0] = cap.fogs.get(uuid);
        });
        return result[0];
    }

    public static boolean addOrResetFog(Level level, BlockPos position, int lifeTime, double strength, double range, UUID uuid) {
        if (getFog(level, uuid) == null) {
            addOrResetFogSided(level, position, lifeTime, strength, range, uuid);
            PVZPacketHandler.sendToLevel(level, new PVZFogPacket(position, lifeTime, strength, range, uuid));
            return true;
        } else {
            return false;
        }
    }

    public static PVZFog addOrResetFogSided(Level level, BlockPos position, int lifeTime, double strength, double range, UUID uuid) {
        PVZFog fog = getFog(level, uuid);
        if (fog != null) {
            fog.position = position;
            fog.lifeLeft = lifeTime;
            fog.strength = strength;
            fog.range = range;
        } else {
            fog = new PVZFog(position, lifeTime, strength, range, uuid);
            PVZFog finalFog = fog;
            level.getCapability(CAP).ifPresent(cap -> cap.fogs.put(uuid, finalFog));
        }
        return fog;
    }

    public static boolean modifyFogFeatures(Level level, UUID uuid, PVZFogPacket.ModifyType type, double value) {
        PVZFog fog = getFog(level, uuid);
        if (fog != null) {
            switch (type) {
                case LIFE_TIME -> fog.lifeLeft = (int) value;
                case STRENGTH -> fog.strength = value;
                case RANGE -> fog.range = value;
                case REMOVE -> fog.lifeLeft = -1;
            }
            PVZPacketHandler.sendToLevel(level, new PVZFogPacket(type, value, uuid));
            return true;
        } else {
            return false;
        }
    }

    public static boolean modifyFogPosition(Level level, UUID uuid, BlockPos position) {
        PVZFog fog = getFog(level, uuid);
        if (fog != null) {
            fog.position = position;
            PVZPacketHandler.sendToLevel(level, new PVZFogPacket(position, uuid));
            return true;
        } else {
            return false;
        }
    }

    public static void requireFog(UUID uuid) {
        PVZPacketHandler.sendToServer(new PVZFogPacket(uuid));
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag list = new ListTag();
        for (PVZFog fog : fogs.values()) {
            CompoundTag fogTag = new CompoundTag();
            fogTag.putInt("x", fog.position.getX());
            fogTag.putInt("y", fog.position.getY());
            fogTag.putInt("z", fog.position.getZ());
            fogTag.putInt("timeLeft", fog.lifeLeft);
            fogTag.putDouble("strength", fog.strength);
            fogTag.putDouble("range", fog.range);
            fogTag.putUUID("uuid", fog.uuid);
            list.add(fogTag);
        }
        tag.put("fogs", list);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("fogs")) {
            ListTag list = nbt.getList("fogs", Tag.TAG_COMPOUND);
            for (Tag tag : list) {
                if (tag instanceof CompoundTag fogTag) {
                    UUID uuid = fogTag.getUUID("uuid");
                    fogs.put(uuid, new PVZFog(new BlockPos(fogTag.getInt("x"), fogTag.getInt("y"), fogTag.getInt("z")),
                            fogTag.getInt("timeLeft"), fogTag.getDouble("strength"), fogTag.getDouble("range"), uuid
                            ));
                }
            }
        }
    }
}
