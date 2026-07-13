package com.hungteen.pvz.common.capability.entity;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.ZombieEvent;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.network.PVZEntityCapPacket;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.common.register.PVZParticles;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.tags.FluidTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class PVZEntityCapability implements ICapabilitySerializable<CompoundTag> {
    private final Entity entity;
    //summoned
    public String resource = "";
    public int cost = 0;
    //owner
    private Entity owner = null;
    private UUID ownerUuid = null;
    //invasion
    public Set<UUID> zombieEventUUIDs = new HashSet<>();
    public short tickCount = 0;
    public String hypnosisTempTeam;
    //client
    public boolean isDirty = false;
    private int stuckArrowWithATarget = 0;

    public static final Capability<PVZEntityCapability> CAP = CapabilityManager.get(new CapabilityToken<>(){});
    public PVZEntityCapability(Entity entity) {
        this.entity = entity;
    }

    public static void clientTick(TickEvent.ClientTickEvent ev) {
        ClientLevel level = (ClientLevel) ClientProxy.getLevel();
        if (level == null || ClientProxy.MC.isPaused()) {
            return;
        }
        level.getEntities().getAll().forEach(entity -> {
            entity.getCapability(CAP).ifPresent((cap) -> {
                if (entity instanceof LivingEntity living && living.getRandom().nextBoolean()
                        && living.getAttribute(Attributes.MAX_HEALTH).getModifier(PVZMobEffects.SUN_BLOOD_EFFECT_UUID) != null) {
                    level.addParticle(PVZParticles.SUN.get(), entity.getX(), entity.getY() + entity.getEyeHeight(), entity.getZ()
                            , living.getRandom().nextFloat() * 0.2 - 0.1, 0.1f, living.getRandom().nextFloat() * 0.2 - 0.1);
                }
                //sync
                PVZEntityCapPacket.read(entity.getUUID(), cap);
            });
        });
    }

    public static void tick(TickEvent.ServerTickEvent ev) {
        ev.getServer().getAllLevels().forEach((level -> level.getAllEntities().forEach((entity -> {
            entity.getCapability(CAP).ifPresent((cap) -> {
                //handle freeze and fire.
                if (entity.getTicksFrozen() > 0 && (entity.isInFluidType() && EntityUtil.getFluidEntityIn(entity).is(FluidTags.LAVA) || entity.wasOnFire || entity.isOnFire())) {
                    entity.setRemainingFireTicks(0);
                    entity.setTicksFrozen(0);
                    if (entity instanceof LivingEntity living && living.hasEffect(PVZMobEffects.FREEZE.get())) {
                        living.removeEffect(PVZMobEffects.FREEZE.get());
                    }
                }
                if (++cap.tickCount > 10) {
                    cap.tickCount = 0;
                    //target--------------------------------------------------------------------------------------------
                    //TODO find out why entities still target on entities that has been killed.
                    if (entity instanceof Mob mob && ! EntityUtil.isEntityValid(mob.getTarget())) {
                        mob.setTarget(null);
                    }
                    //owner---------------------------------------------------------------------------------------------
                    if (! EntityUtil.isEntityValid(cap.owner) && cap.ownerUuid != null) {
                        Entity entity1 = ((ServerLevel) cap.entity.level).getEntity(cap.ownerUuid);
                        if (entity1 != null) {
                            cap.setOwner(entity1);
                        }
                    }
                    String name = cap.entity.getScoreboardName();
                    if (! EntityUtil.isEntityValid(cap.owner)) {
                        cap.owner = null;
                    } else if (EntityUtil.canReteamToOwner(cap.entity, cap.owner)) {
                        PlayerTeam team = cap.entity.getServer().getScoreboard().getPlayersTeam(cap.owner.getScoreboardName());
                        if (team != null && team != cap.entity.getServer().getScoreboard().getPlayersTeam(name)) {
                            cap.entity.getServer().getScoreboard().addPlayerToTeam(name, team);
                        }
                    }
                }

                //zombie events-----------------------------------------------------------------------------------------
                if (cap.zombieEventUUIDs != null) {
                    level.getCapability(PVZZombieEventCapability.CAP, null).ifPresent((capability) -> {
                        Set<UUID> removingUUIDs = new HashSet<>();
                        cap.zombieEventUUIDs.forEach(uuid -> {
                            ZombieEvent event = capability.getEvent(uuid);
                            if (event != null) {
                                if (EntityUtil.isEntityValid(cap.entity)) {
                                    if (cap.entity.getTeam() == cap.entity.getServer().getScoreboard().getPlayerTeam(PVZMod.ENEMY_TEAM)) {
                                        if (cap.entity instanceof Mob mob && ! EntityUtil.isEntityValid(mob.getTarget())) {
                                            mob.setTarget(event.target);
                                        }
                                        if (! event.getMembers().contains(cap.entity)) {
                                            event.addMember(cap.entity);
                                        }
                                    } else if (event.getMembers().contains(cap.entity)) {
                                        event.removeMember(cap.entity);
                                    }
                                }
                            } else {
                                removingUUIDs.add(uuid);
                            }
                        });
                        removingUUIDs.forEach(uuid -> cap.zombieEventUUIDs.remove(uuid));
                    });
                }

                //stuck arrow_with_a_target render----------------------------------------------------------------------
                if (cap.stuckArrowWithATarget > 0 && entity.tickCount % 3200 == 0) {
                    cap.stuckArrowWithATarget --;
                    cap.isDirty = true;
                }

                //sync--------------------------------------------------------------------------------------------------
                PVZEntityCapPacket.sync(entity.getUUID(), cap);
            });
        }))));
    }

    //owner
    public void setOwner(UUID uuid) {
        if (owner instanceof Player) {
            this.ownerUuid = uuid;
        }
        this.owner = ((ServerLevel) (entity.level)).getEntity(uuid);
    }
    public void setOwner(Entity entity) {
        this.owner = entity;
        if (entity == null) {
            this.ownerUuid = null;
        } else {
            if (entity instanceof Player) {
                this.ownerUuid = entity.getUUID();
            }
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
    public UUID getOwnerUuid() {
        return this.owner == null ? ownerUuid : owner.getUUID();
    }

    public static Entity getOwner(Entity entity) {
        AtomicReference<Entity> entity1 = new AtomicReference<>();
        entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> {
            entity1.set(cap.getOwner());
        });
        return entity1.get();
    }

    public static UUID getOwnerUUID(Entity entity) {
        AtomicReference<UUID> entity1 = new AtomicReference<>();
        entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> {
            entity1.set(cap.getOwnerUuid());
        });
        return entity1.get();
    }

    public boolean hasOwner() {
        return ownerUuid != null;
    }

    public static boolean hasOwner(Entity entity) {
        AtomicBoolean result = new AtomicBoolean(true);
        entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> result.set(cap.hasOwner()));
        return result.get();
    }

    //invasion
    public boolean isInZombieEvent() {
        return zombieEventUUIDs != null;
    }

    //client
    public void setStuckArrowWithATarget(int num) {
        this.stuckArrowWithATarget = num;
        this.isDirty = true;
    }

    public int getStuckArrowWithATarget() {
        return this.stuckArrowWithATarget;
    }

    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return cap == CAP ? LazyOptional.of(() -> (T) this) : LazyOptional.empty();
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag basicTag = new CompoundTag();
        basicTag.putString("resource", resource);
        if (hypnosisTempTeam != null) {
            basicTag.putString("team_before_hypnosis", hypnosisTempTeam); //when this variable is saved it can only be the team before hypnosis.
        }
        basicTag.putInt("cost", cost);
        basicTag.putInt("stuck_arrow_with_a_target", stuckArrowWithATarget);
        if (owner != null) {
            basicTag.putUUID("owner", owner.getUUID());
        } else if (ownerUuid != null) {
            basicTag.putUUID("owner", ownerUuid);
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
        if (nbt.contains("stuck_arrow_with_a_target")) {
            this.stuckArrowWithATarget = nbt.getInt("stuck_arrow_with_a_target");
        }
        if (nbt.contains("team_before_hypnosis")) {
            this.hypnosisTempTeam = nbt.getString("team_before_hypnosis");
        }
        if (nbt.contains("owner")) {
            this.ownerUuid = nbt.getUUID("owner");
            //TODO handle situation when player is not available when loading.
            this.owner = ((ServerLevel) (entity.level)).getEntity(ownerUuid);
            if (owner != null && ! (owner instanceof Player)) {
                this.ownerUuid = null;
            }
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
