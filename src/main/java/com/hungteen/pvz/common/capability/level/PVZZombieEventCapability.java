package com.hungteen.pvz.common.capability.level;

import com.hungteen.pvz.common.network.ZombieEventPacket;
import com.hungteen.pvz.common.register.PVZZombieEvents;
import com.hungteen.pvz.api.ZombieEvent;
import com.hungteen.pvz.common.world.invasion.InvasionTeam;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.event.TickEvent;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

public class PVZZombieEventCapability implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<PVZZombieEventCapability> CAP = CapabilityManager.get(new CapabilityToken<>(){});
    private final Level level;
    private final Set<ZombieEvent> events = new HashSet<>();
    private static short tickCount = 1;
    private static Set<ZombieEvent> removeEvent = new HashSet<>();

    public PVZZombieEventCapability(Level level) {
        this.level = level;
    }
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CAP ? LazyOptional.of(() -> (T) this) : LazyOptional.empty();
    }

    public static PVZZombieEventCapability fromLevel(Level level) {
        AtomicReference<PVZZombieEventCapability> result = new AtomicReference<>();
        level.getCapability(CAP).ifPresent(result::set);
        return result.get();
    }

    public boolean hasEvent(UUID uuid) {
        return getEvent(uuid) != null;
    }

    public void addEvent(ZombieEvent event) {
        this.events.add(event);
        if (this.level instanceof ServerLevel) {
            ZombieEventPacket.toClient(event);
        }
    }

    @Nullable
    public ZombieEvent getEvent(UUID uuid) {
        for (ZombieEvent i : this.events) {
            if (i != null && i.uuid.equals(uuid)) {
                return i;
            }
        }
        return null;
    }

    public Set<ZombieEvent> getEvents() {
        return this.events;
    }

    public static void tick(TickEvent.ServerTickEvent ev) {
        if (++ tickCount >= 100) {
            tickCount = 0;
        }
        ev.getServer().getAllLevels().forEach(level -> level.getCapability(CAP).ifPresent(cap -> {
            if (tickCount == 0) {
                cap.events.forEach(ZombieEventPacket::toClient);
            }
            cap.events.forEach(event -> {
                if (! event.removed) {
                    event.tick(ev);
                } else {
                    removeEvent.add(event);
                }
            });
            removeEvent.forEach(cap.events::remove);
            removeEvent.clear();
        }));
        InvasionTeam.serverTick();
    }

    @Override
    public CompoundTag serializeNBT() {
        ListTag events = new ListTag();
        for (ZombieEvent i : this.events) {
            events.add(i.serializeNBT());
        }
        CompoundTag tag = new CompoundTag();
        tag.put("events", events);
        return tag;
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("events")) {
            ListTag events = tag.getList("events", Tag.TAG_COMPOUND);
            for (Tag ev : events) {
                if (ev instanceof CompoundTag eventTag) {
                    ZombieEvent event = PVZZombieEvents.fromTag(level, eventTag.getUUID("uuid"), eventTag);
                    if (event != null) {
                        this.addEvent(event);
                    }
                }
            }
        }
    }
}
