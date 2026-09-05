package com.hungteen.pvz.api;

import com.hungteen.pvz.api.events.ZombieEventEvent;
import com.hungteen.pvz.common.register.PVZZombieEvents;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.INBTSerializable;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nullable;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * The parent class of zombie events such as invasions and challenges.
 * Stored in {@link com.hungteen.pvz.common.capability.level.PVZZombieEventCapability PVZZombieEventCapability}.
 * Register Zombie events by putting class in {@link com.hungteen.pvz.common.register.PVZZombieEvents#REGISTRY REGISTRY}.
 * <br> To get the registry name of a ZombieEvent, call {@link com.hungteen.pvz.common.register.PVZZombieEvents#getType(ZombieEvent) PVZZombieEvents#getType()}.
 * <br> To get a ZombieEvent from a CompoundTag, call {@link com.hungteen.pvz.common.register.PVZZombieEvents#fromTag(Level, UUID, CompoundTag) PVZZombieEvents#fromTag()}.
 */
public abstract class ZombieEvent implements INBTSerializable<CompoundTag> {

    public final Level level;
    public BlockPos position;
    @Nullable
    public LivingEntity target;
    public UUID targetUUID;
    public int range;
    public final UUID uuid;
    public boolean removed;
    protected Set<Entity> members = new HashSet<>();
    protected int tickCount = 1;// not 0 to avoid ticking before the entities finished loading.

    public ZombieEvent(Level level, UUID uuid) {
        this.level = level;
        this.uuid = uuid;
        this.removed = false;
        this.range = 50;
        MinecraftForge.EVENT_BUS.post(new ZombieEventEvent(this, ZombieEventEvent.Phase.New));
    }

    /**Used by {@link com.hungteen.pvz.common.register.PVZZombieEvents#fromTag(Level, UUID, CompoundTag) PVZZombieEvents#fromTag()} when syncing or reloading.
     * <br><b>⚠ATTENTION⚠</b> Any children of ZombieEvent should contain a constructor of this type.*/
    public ZombieEvent(Level level, UUID uuid, CompoundTag tag) {
        this(level, uuid);
        ZombieEventEvent event = new ZombieEventEvent(this, ZombieEventEvent.Phase.Load);
        event.tag = tag;
        MinecraftForge.EVENT_BUS.post(event);
    }

    public boolean isMainEvent() {
        return true;
    }

    public boolean needsSync() {
        return true;
    }

    public void remove() {
        MinecraftForge.EVENT_BUS.post(new ZombieEventEvent(this, ZombieEventEvent.Phase.Remove));
        this.removed = true;
        if (this.needsSync()) PVZAPI.get().removeClientZombieEvent(this);
    }

    public void tick(TickEvent.ServerTickEvent ev) {
        //target.
        if (this.target != null) {
            this.targetUUID = target.getUUID();
        } else if (this.targetUUID != null) {
            Entity entity = ((ServerLevel) level).getEntity(this.targetUUID);
            if (entity instanceof LivingEntity living) {
                this.target = living;
            }
        }
        //ticking.
        tickCount ++;
        if (tickCount >= 10000000) {
            tickCount = 0;
        }
        if (tickCount % 5 == 0) {
            Set<Entity> removingEntities = new HashSet<>();
            this.members.forEach(entity -> {
                if (entity == null || ! entity.isAlive()) {
                    removingEntities.add(entity);
                }
            });
            removingEntities.forEach(this::removeMember);
        }
        MinecraftForge.EVENT_BUS.post(new ZombieEventEvent(this, ZombieEventEvent.Phase.Tick));
    }

    public Set<Entity> getMembers() {
        return Set.copyOf(this.members);
    }

    public void addMember(Entity member) {
        this.members.add(member);
    }

    public void removeMember(Entity member) {
        this.members.remove(member);
    }

    public ResourceLocation getType() {
        return PVZAPI.get().getZombieEventType(this);
    }

    public Component getDisplayName() {
        return Component.translatable(Util.makeDescriptionId("zombie_event", PVZZombieEvents.REGISTRY.get().getKey(this.getClass())));
    }

    @Override
    public final CompoundTag serializeNBT() {
        CompoundTag tag = new CompoundTag();
        tag.putUUID("uuid", this.uuid);
        tag.putString("event_type", getType().toString());
        if (target != null) {
            tag.putUUID("target", this.target.getUUID());
        } else if (targetUUID != null) {
            tag.putUUID("target", this.targetUUID);
        }
        if (position != null) {
            tag.putInt("x", position.getX());
            tag.putInt("y", position.getY());
            tag.putInt("z", position.getZ());
        }
        tag.putInt("range", range);
        tag.putBoolean("removed", removed);
        tag.put("data", this.addAdditionalSaveData(new CompoundTag()));
        return tag;
    }

    public CompoundTag addAdditionalSaveData(CompoundTag tag) {
        return tag;
    }

    @Override
    public final void deserializeNBT(CompoundTag tag) {
        if (tag.contains("target")) {
            this.targetUUID = tag.getUUID("target");
        }
        if (tag.contains("x")) {
            this.position = new BlockPos(tag.getInt("x"), tag.getInt("y"), tag.getInt("z"));
        }
        this.range = tag.getInt("range");
        this.removed = tag.getBoolean("removed");
        this.readAdditionalSaveData(tag.getCompound("data"));
    }

    public void readAdditionalSaveData(CompoundTag tag) {
    }
}
