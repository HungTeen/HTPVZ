package com.hungteen.pvz.common.world;

import com.hungteen.pvz.common.register.PVZZombieEvents;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * The parent class of zombie events such as invasions and challenges.
 * Stored in {@link com.hungteen.pvz.common.capability.level.PVZZombieEventCapability PVZZombieEventCapability}.
 * Register Zombie events by putting class in {@link com.hungteen.pvz.common.register.PVZZombieEvents#REGISTRY REGISTRY}.
 */
public abstract class ZombieEvent implements INBTSerializable<CompoundTag> {

    public final Level level;
    public BlockPos position;
    public LivingEntity target;
    public int range;
    public final UUID uuid;
    public boolean removed;
    public Set<Entity> members = new HashSet<>();
    protected static int tickCount = 1;// not 0 to avoid ticking before the entities finished loading.

    public ZombieEvent(Level level, UUID uuid) {
        this.level = level;
        this.uuid = uuid;
        this.removed = false;
        this.range = 50;
    }
    public ZombieEvent(Level level, UUID uuid, CompoundTag tag) {
        this(level, uuid);
        this.deserializeNBT(tag);
    }

    public static void init() {}
    public ResourceLocation getType() {
        for (ResourceLocation type : PVZZombieEvents.REGISTRY.get().getKeys()) {
            if (PVZZombieEvents.REGISTRY.get().getValue(type) == this.getClass()) {
                return type;
            }
        }
        return null;
    }

    public void remove() {
        this.removed = true;
    }

    public void tick(TickEvent.ServerTickEvent ev) {
        tickCount ++;
        if (tickCount >= 10000000) {
            tickCount = 0;
        }
        if (tickCount % 5 == 0) {
            Set<Entity> removingEntities = new HashSet<>();
            this.members.forEach(entity -> {
                if (! EntityUtil.isEntityValid(entity)) {
                    removingEntities.add(entity);
                }
            });
            removingEntities.forEach(entity -> members.remove(entity));
        }
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putString("event_type", getType().toString());
        if (target != null) {
            tag.putUUID("target", target.getUUID());
        }
        if (position != null) {
            tag.putInt("x", position.getX());
            tag.putInt("y", position.getY());
            tag.putInt("z", position.getZ());
        }
        tag.putInt("range", range);
        tag.putBoolean("removed", removed);
        return tag;
    }

    public static ZombieEvent fromTag(Level level, UUID uuid, CompoundTag tag) {
        try {
            Class<? extends ZombieEvent> evClass = PVZZombieEvents.REGISTRY.get().getValue(new ResourceLocation(tag.getString("event_type")));
            return evClass.getConstructor(Level.class, UUID.class, CompoundTag.class)
                    .newInstance(level, uuid, tag);
        } catch (Exception ignored) {
            return null;
        }
    }

    @Override
    public void deserializeNBT(CompoundTag tag) {
        if (tag.contains("target")) {
            this.target = level.getPlayerByUUID(tag.getUUID("target"));
        }
        if (tag.contains("x")) {
            this.position = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        }
        this.range = tag.getInt("range");
        this.removed = tag.getBoolean("removed");
    }
}
