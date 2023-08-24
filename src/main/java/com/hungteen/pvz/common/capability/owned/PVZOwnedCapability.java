package com.hungteen.pvz.common.capability.owned;

import com.hungteen.pvz.PVZMod;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.ServerScoreboard;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraft.world.scores.Team;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.CapabilityManager;
import net.minecraftforge.common.capabilities.CapabilityToken;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;

public class PVZOwnedCapability implements ICapabilitySerializable<CompoundTag> {
    private final Entity entity;
    public String resource = "";
    public int cost = 0;
    private Entity owner = null;
    public boolean isTeamDirty = false;
    private final ServerScoreboard scoreboard;
    public static Set<PVZOwnedCapability> capSet = new HashSet<>();

    public static final Capability<PVZOwnedCapability> CAP = CapabilityManager.get(new CapabilityToken<>(){});
    public PVZOwnedCapability(Entity entity) {
        this.entity = entity;
        scoreboard = entity.getServer().getScoreboard();
        capSet.add(this);
    }

    public static void tick(){
        for (PVZOwnedCapability cap : capSet) {
            String name = cap.entity.getScoreboardName();
            if (cap.entity instanceof ServerPlayer && cap.scoreboard.getPlayersTeam(name) == null) {
                cap.scoreboard.addPlayerToTeam(name, cap.scoreboard.getPlayerTeam(PVZMod.GLOBAL_TEAM));
            }
            if (cap.owner != null) {
                if (!cap.owner.isAlive()) {
                    cap.setOwner(null);
                } else if (cap.scoreboard.getPlayersTeam(cap.owner.getScoreboardName()) != cap.scoreboard.getPlayersTeam(name)) {
                    cap.scoreboard.addPlayerToTeam(name, cap.scoreboard.getPlayersTeam(cap.owner.getScoreboardName()));
                }
            }
        }
    }

    public void setOwner(Entity entity) {
        this.owner = entity;
        if (entity == null) {
            scoreboard.removePlayerFromTeam(entity.getScoreboardName());
        }
    }

    public Entity getOwner(){
        return owner;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.of(() -> this).cast();
    }

    public static PVZOwnedCapability getCap(Entity entity){
        for (PVZOwnedCapability cap : capSet) {
            if (cap.entity == entity) {
                return cap;
            }
        }
        return null;
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
            this.owner = ((ServerLevel) (entity.level)).getEntity(nbt.getUUID("owner"));
        }
    }
}
