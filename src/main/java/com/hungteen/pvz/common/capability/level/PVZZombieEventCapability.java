package com.hungteen.pvz.common.capability.level;

import com.hungteen.pvz.api.ZombieEvent;
import com.hungteen.pvz.common.network.ZombieEventPacket;
import com.hungteen.pvz.common.register.PVZZombieEvents;
import net.minecraft.core.BlockPos;
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
import java.util.function.Predicate;

public class PVZZombieEventCapability implements ICapabilitySerializable<CompoundTag> {
    public static final Capability<PVZZombieEventCapability> CAP = CapabilityManager.get(new CapabilityToken<>(){});
    public final Level level;
    private final Set<ZombieEvent> events = new HashSet<>();
    private static short tickCount = 1;
    private static final Set<ZombieEvent> removingEvents = new HashSet<>();

    public PVZZombieEventCapability(Level level) {
        this.level = level;
    }
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CAP ? LazyOptional.of(() -> (T) this) : LazyOptional.empty();
    }

    public static @Nullable PVZZombieEventCapability fromLevel(Level level) {
        AtomicReference<PVZZombieEventCapability> result = new AtomicReference<>();
        level.getCapability(CAP).ifPresent(result::set);
        return result.get();
    }

    public boolean hasEvent(UUID uuid) {
        return getEvent(uuid) != null;
    }

    public void addEvent(ZombieEvent event) {
        this.events.add(event);
        if (this.level instanceof ServerLevel && event.needsSync()) {
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
                cap.events.forEach(zEv -> {
                    if (zEv.needsSync()) ZombieEventPacket.toClient(zEv);
                });
            }
            cap.events.forEach(event -> {
                if (! event.removed) {
                    event.tick(ev);
                } else {
                    removingEvents.add(event);
                }
            });
            removingEvents.forEach(cap.events::remove);
            removingEvents.clear();

        }));
    }

    public <T extends ZombieEvent> T getNearestEvent(Class<T> clazz, BlockPos pos) {
        return getNearestEvent(clazz, pos, event -> true);
    }
    public <T extends ZombieEvent> T getNearestEvent(Class<T> clazz, BlockPos pos, Predicate<T> predicate) {
        double dist = -1;
        T result = null;
        for (ZombieEvent event : this.getEvents()) {
            if ((clazz == event.getClass() || clazz.isAssignableFrom(event.getClass())) && predicate.test((T) event)) {
                double newDist = event.position.distSqr(pos);
                if (dist < 0 || newDist < dist) {
                    dist = newDist;
                    result = (T) event;
                }
            }
        }
        return result;
    }

    public <T extends ZombieEvent> T getNearestEventRanged(Class<T> clazz, BlockPos pos) {
        return getNearestEventRanged(clazz, pos, e -> true);
    }

    public <T extends ZombieEvent> T getNearestEventRanged(Class<T> clazz, BlockPos pos, Predicate<T> predicate) {
        double dist = -1;
        T result = null;
        for (ZombieEvent event : this.getEvents()) {
            if ((clazz == event.getClass() || clazz.isAssignableFrom(event.getClass())) && predicate.test((T) event)) {
                double newDist = Math.sqrt(event.position.distSqr(pos)) - event.range;
                if (newDist <= 0) return (T) event;
                else if (dist < 0 || newDist < dist) {
                    dist = newDist;
                    result = (T) event;
                }
            }
        }
        return result;
    }

    public <T extends ZombieEvent> T getEventIn(Class<T> clazz, BlockPos pos) {
        return getEventIn(clazz, pos, e -> true, 0);
    }

    public <T extends ZombieEvent> T getEventIn(Class<T> clazz, BlockPos pos, Predicate<T> predicate, int additionalRange) {
        for (ZombieEvent event : this.getEvents()) {
            if ((clazz == event.getClass() || clazz.isAssignableFrom(event.getClass())) && predicate.test((T) event)) {
                if (Math.sqrt(event.position.distSqr(pos)) - (event.range + additionalRange) * (event.range + additionalRange) < 0) {
                    return (T) event;
                }
            }
        }
        return null;
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
