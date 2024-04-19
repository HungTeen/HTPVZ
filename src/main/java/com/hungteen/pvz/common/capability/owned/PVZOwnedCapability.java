package com.hungteen.pvz.common.capability.owned;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
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

import java.util.UUID;

public class PVZOwnedCapability implements ICapabilitySerializable<CompoundTag> {
    private final Entity entity;
    public String resource = "";
    public int cost = 0;
    private Entity owner = null;
    public UUID ownerUuid = null;
    private final ServerScoreboard scoreboard;
    public static short tickCount = 0;

    public static final Capability<PVZOwnedCapability> CAP = CapabilityManager.get(new CapabilityToken<>(){});
    public PVZOwnedCapability(Entity entity) {
        this.entity = entity;
        scoreboard = entity.getServer().getScoreboard();
    }

    public static void tick(TickEvent.ServerTickEvent ev) {
        if (++ tickCount > 10) {
            tickCount = 0;
            ev.getServer().getAllLevels().forEach((level -> level.getAllEntities().forEach((entity1 -> {
                entity1.getCapability(CAP).ifPresent((cap) -> {
                    //owner
                    if (! EntityUtil.isEntityValid(cap.owner)) {//TODO will this lag?
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
                        }
                    }
                });
            }))));
        }
    }

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
    /**don't use this method to adjust if the entity has owner! use {@link PVZOwnedCapability#hasOwner() hasOwner()} instead.*/
    public Entity getOwner() {
        this.owner = owner == null ? ((ServerLevel) (entity.level)).getEntity(ownerUuid) : owner;
        return owner;
    }

    public boolean hasOwner() {
        return ownerUuid != null;
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
    }
}
