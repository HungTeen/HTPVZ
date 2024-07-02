package com.hungteen.pvz.common.capability.entity;

import com.hungteen.pvz.common.world.ZombieEvent;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Scoreboard;
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

public class PVZEntityCapability implements ICapabilitySerializable<CompoundTag> {
    private final Entity entity;
    //summoned
    public String resource = "";
    public int cost = 0;
    //owner
    private Entity owner = null;
    public UUID ownerUuid = null;
    //invasion
    public Set<UUID> zombieEventUUIDs = new HashSet<>();
    private final ServerScoreboard scoreboard;
    public short tickCount = 0;

    public static final Capability<PVZEntityCapability> CAP = CapabilityManager.get(new CapabilityToken<>(){});
    public PVZEntityCapability(Entity entity) {
        this.entity = entity;
        scoreboard = entity.getServer().getScoreboard();
    }

    public static void tick(TickEvent.ServerTickEvent ev) {
        ev.getServer().getAllLevels().forEach((level -> level.getAllEntities().forEach((entity1 -> {
            entity1.getCapability(CAP).ifPresent((cap) -> {
                if (++cap.tickCount > 10) {
                    cap.tickCount = 0;

                    //owner---------------------------------------------------------------------------------------------
                    if (!EntityUtil.isEntityValid(cap.owner)) {//TODO will this lag?
                        Entity entity = ((ServerLevel) cap.entity.level).getEntity(cap.ownerUuid);
                        if (entity != null) {
                            cap.setOwner(entity);
                        }
                    }
                    String name = cap.entity.getScoreboardName();
                    if (cap.owner != null) {
                        if (!cap.owner.isAlive()) {
                            cap.setOwner(null);
                        } else {
                            PlayerTeam team = cap.scoreboard.getPlayersTeam(cap.owner.getScoreboardName());
                            if (team != null && team != cap.scoreboard.getPlayersTeam(name)) {
                                cap.scoreboard.addPlayerToTeam(name, team);
                            }
                            //invasion syncing to owned entities.
                            cap.owner.getCapability(CAP).ifPresent((ownerCap) -> cap.zombieEventUUIDs = Set.copyOf(ownerCap.zombieEventUUIDs));
                        }
                    }
                }

                //invasion------------------------------------------------------------------------------------------
                if (cap.zombieEventUUIDs != null) {
                    level.getCapability(PVZZombieEventCapability.CAP, null).ifPresent((capability) -> {
                        Set<UUID> removingUUIDs = new HashSet<>();
                        cap.zombieEventUUIDs.forEach(uuid -> {
                            ZombieEvent event = capability.getEvent(uuid);
                            if (event != null) {
                                if (EntityUtil.isEntityValid(cap.entity)) {
                                    if (cap.entity instanceof Mob mob && !EntityUtil.isEntityValid(mob.getTarget())) {
                                        mob.setTarget(event.target);
                                    }
                                    event.members.add(cap.entity);
                                }
                            } else {
                                removingUUIDs.add(uuid);
                            }
                        });
                        removingUUIDs.forEach(uuid -> cap.zombieEventUUIDs.remove(uuid));
                    });
                }
            });
        }))));
    }

    //owner
    public void setOwner(Entity entity) {
        this.owner = entity;
        if (entity == null) {
            this.ownerUuid = null;
            scoreboard.removePlayerFromTeam(this.entity.getScoreboardName());
        } else {
            this.ownerUuid = entity.getUUID();
            Scoreboard scoreboard = this.entity.getServer().getScoreboard();
            PlayerTeam team = scoreboard.getPlayersTeam(entity.getScoreboardName());
            if (team != null) {
                scoreboard.addPlayerToTeam(this.entity.getScoreboardName(), team);
            }
        }
    }
    /**don't use this method to adjust if the entity has owner! use {@link PVZEntityCapability#hasOwner() hasOwner()} instead.*/
    public Entity getOwner() {
        this.owner = owner == null ? ((ServerLevel) (entity.level)).getEntity(ownerUuid) : owner;
        return owner;
    }

    public boolean hasOwner() {
        return ownerUuid != null;
    }

    //invasion
    public boolean isInInvasion() {
        return zombieEventUUIDs != null;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CAP ? LazyOptional.of(() -> (T) this) : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag basicTag = new CompoundTag();
        basicTag.putString("resource", resource);
        basicTag.putInt("cost", cost);
        if (owner != null) {
            basicTag.putUUID("owner", owner.getUUID());
        }
        if (! zombieEventUUIDs.isEmpty()) {
            CompoundTag zombieEventTag = new CompoundTag();
            int i = 0;
            for (UUID uuid : zombieEventUUIDs) {
                zombieEventTag.putUUID(String.valueOf(i), uuid);
                i ++;
            }
            basicTag.put("zombie_events", zombieEventTag);
        }
        return basicTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        if (nbt.contains("resource")) {
            this.resource = nbt.getString("resource");
        }
        if (nbt.contains("cost")) {
            this.cost = nbt.getInt("cost");
        }
        if (nbt.contains("owner")) {
            this.ownerUuid = nbt.getUUID("owner");
            //TODO handle situation when player is not available when loading.
            this.owner = ((ServerLevel) (entity.level)).getEntity(ownerUuid);
        }
        if (nbt.contains("zombie_events")) {
            CompoundTag zombieEventTag = nbt.getCompound("zombie_events");
            int i = 0;
            while (zombieEventTag.contains(String.valueOf(i))) {
                zombieEventUUIDs.add(zombieEventTag.getUUID(String.valueOf(i)));
                i ++;
            }
        }
    }
}
