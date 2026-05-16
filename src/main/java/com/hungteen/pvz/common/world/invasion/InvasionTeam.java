package com.hungteen.pvz.common.world.invasion;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.ZombieEvent;
import com.hungteen.pvz.api.interfaces.ICanGroupUp;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapStats;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.common.world.PathSeeker;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.event.TickEvent;

import java.util.*;
import java.util.function.Function;

public class InvasionTeam extends ZombieEvent {
    private final PathSeeker seeker;
    public Entity leader;
    public UUID leaderUUID;
    public int missingCount = 0;
    private static final Random random = new Random();
    private List<InvasionType> invasionTypes;

    public InvasionTeam(PathSeeker seeker, ServerPlayer target, UUID uuid) {
        super(target.level, uuid);
        this.seeker = seeker;
        this.target = target;
        this.invasionTypes = InvasionType.generateTypes(target);
    }

    /**Used by {@link com.hungteen.pvz.common.register.PVZZombieEvents#fromTag(Level, UUID, CompoundTag) PVZZombieEvents#fromTag()} when syncing or reloading.*/
    public InvasionTeam(Level level, UUID uuid, CompoundTag tag) {
        super(level, uuid, tag);
        this.seeker = new PathSeeker((ServerLevel) level);
        seeker.minDistanceSqr = 576;
        seeker.maxDistanceSqr = 1024;
        this.deserializeNBT(tag);
    }

    public static boolean spawnFor(ServerPlayer player) {
        if (! Invasion.canInvade(player)) return false;
        PathSeeker seeker = new PathSeeker((ServerLevel) player.level);
        seeker.minDistanceSqr = 576;
        seeker.maxDistanceSqr = 1024;
        InvasionTeam team = new InvasionTeam(seeker, player, UUID.randomUUID());
        team.seeker.center = player.blockPosition();
        team.seeker.targetPos = player.blockPosition();
        PVZZombieEventCapability.fromLevel(player.level).addEvent(team);
        return true;
    }

    @Override
    public void tick(TickEvent.ServerTickEvent ev) {
        super.tick(ev);
        if (target == null) {
            if (this.leader != null) {
                target = level.getNearestPlayer(leader, 16);
            }
            if (target == null) remove();
            return;
        }
        if (this.leaderUUID == null) {
            this.seeker.tick();
            this.position = this.target.blockPosition();
            if (this.invasionTypes.isEmpty() || ! this.invasionTypes.stream().allMatch(type -> type.isAvailable(target, invasionTypes))) {
                return;
            }
            if (! seeker.availablePositions.isEmpty()) {
                for (BlockPos pos : seeker.availablePositions) {
                    if (target.level.getBrightness(LightLayer.BLOCK, pos) > 0) {
                        return;
                    }
                    double dist = pos.distSqr(target.blockPosition());
                    if (dist > 576 && dist < 1024) {
                        double horizontalDistSqr = (pos.getX() - target.getX()) * (pos.getX() - target.getX()) + (pos.getZ() - target.getZ()) * (pos.getZ() - target.getZ());
                        double verticalDist = pos.getY() - target.getY();
                        if (verticalDist > 0 || verticalDist * verticalDist < 1.5 * horizontalDistSqr) {
                            if (! invasionTypes.isEmpty()) {
                                Vec3 vec3 = Vec3.atBottomCenterOf(pos);
                                final int size = random.nextInt(4);
                                List<CompoundTag> tags = new ArrayList<>();
                                Optional<InvasionType.EnemyType> leader = invasionTypes.get(0).flagEnemy();
                                List<InvasionType.EnemyType> types = invasionTypes.get(0).enemies();
                                types = types.stream().filter(t -> t.startFrom() <= 0.1 && ! t.isElite()).toList();
                                if (! types.isEmpty()) {
                                    for (int i = 0; i < size; i ++) {
                                        tags.add(types.get(random.nextInt(types.size())).entityData());
                                    }
                                }
                                if (leader.isEmpty() && types.isEmpty()) remove();
                                Entity entity;
                                if (leader.isPresent()) {
                                    this.leader = summonEntity(leader.get().entityData().copy(), target.level, vec3);
                                } else {
                                    this.leader = summonEntity(tags.get(0).copy(), target.level, vec3);
                                    tags.remove(tags.get(0));
                                }
                                if (this.leader != null) {
                                    this.leaderUUID = this.leader.getUUID();
                                    for (CompoundTag tag : tags) {
                                        entity = summonEntity(tag, target.level, vec3);
                                        if (entity != null) entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.setOwner(this.leader));
                                        if (entity instanceof ICanGroupUp zombie && this.leader instanceof ICanGroupUp leader1) zombie.setLeader(leader1);
                                    }
                                }
                                return;
                            }
                        }
                    }
                }
                seeker.availablePositions.clear();
            }
        } else {
            if (this.leader == null) {
                if (tickCount % 20 == 0) {
                    leader = ((ServerLevel) level).getEntity(leaderUUID);
                    missingCount++;
                }
                if (leader == null && missingCount > 5) {
                    remove();
                }
            } else {
                this.position = this.leader.blockPosition();
                missingCount = 0;
            }
            if (this.tickCount > 6000 && (this.leader == null || this.level.getNearestPlayer(leader, 16) == null)) {
                if (this.leader != null) leader.discard();
                this.remove();
            }
            if ((this.tickCount % 20 == 0 && ! EntityUtil.isEntityValid(leader))) {
                this.remove();
            }
        }
    }

    @Override
    public boolean isMainEvent() {
        return false;
    }
    @Override
    public boolean needsSync() {
        return false;
    }

    public void onLeaderDie(Entity leader, DamageSource dmg) {
        Entity source = dmg instanceof IndirectEntityDamageSource source1 ? source1.owner : dmg.getEntity();
        ServerPlayer player = source instanceof ServerPlayer player1 ? player1
                : (source != null && (PVZEntityCapability.getOwner(source) instanceof ServerPlayer player1) ? player1 : null);// no need to be this.target.
        if (player != null) {
            boolean shouldApplyEffect = true;
            if (Invasion.canInvade(player)) {
                if (! this.invasionTypes.stream().allMatch(type -> type.isAvailable(player, this.invasionTypes))) {
                    this.invasionTypes.clear();
                }
                if (this.invasionTypes.isEmpty()) {
                    this.invasionTypes = InvasionType.generateTypes(target);
                }
                if (! this.invasionTypes.isEmpty()) {
                    shouldApplyEffect = false;
                    PVZPlayerCapability.getPlayerData(player).ifPresent(nbt -> nbt.setValue(PVZPlayerCapStats.LAST_INVASION, 0));
                    PVZZombieEventCapability.fromLevel(player.level)
                            .addEvent(new Invasion(this.level, this.uuid, this.invasionTypes, player, this.position, Util.getInvasionLevel(player)));
                }
            }
            if (shouldApplyEffect) {
                player.addEffect(new MobEffectInstance(PVZMobEffects.INVASION_OMEN.get(),
                        player.getRandom().nextInt(100) * 200 + 50000, Util.getInvasionLevel(player) - 1));
            }
        }
        remove();
    }

    private Entity summonEntity(CompoundTag tag, Level level, Vec3 pos) {
        Entity entity = EntityType.loadEntityRecursive(tag, target.level, Function.identity());
        if (entity == null) {
            return null;
        }
        BlockPos pos1 = new BlockPos(pos);
        if (! NaturalSpawner.isValidEmptySpawnBlock(level, pos1, level.getBlockState(pos1), level.getFluidState(pos1), entity.getType())) {
            entity.discard();
            return null;
        }
        entity.setPos(pos);
        if (PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.showInvasionDetails) && (entity instanceof LivingEntity living)) {
            living.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10000));
            living.getIndirectPassengers().forEach(e -> {
                if (e instanceof LivingEntity living1) {
                    living1.addEffect(new MobEffectInstance(MobEffects.GLOWING, 10000));
                }
            });
        }
        if (! this.invasionTypes.stream().allMatch(type -> {
            for (TriPredicate<Invasion, Entity, Integer> predicate : type.getModifiers()) {
                if (! predicate.test(null, entity, 0)) return false;
            }
            return true;
        })) {
            entity.discard();
            return null;
        }
        ((ServerLevel) level).addFreshEntityWithPassengers(entity);
        PlayerTeam enemyTeam = entity.getServer().getScoreboard().getPlayerTeam(PVZMod.ENEMY_TEAM);
        String name = entity.getScoreboardName();
        if (enemyTeam != null) {
            entity.getServer().getScoreboard().addPlayerToTeam(name, enemyTeam);
        }
        if (entity instanceof Mob mob) {
            mob.setTarget(target);
        }
        return entity;
    }

    @Override
    public CompoundTag addAdditionalSaveData(CompoundTag tag) {
        if (this.leaderUUID != null) tag.putUUID("leader", leaderUUID);
        if (! this.invasionTypes.isEmpty()) {
            ListTag types = new ListTag();
            for (InvasionType type : this.invasionTypes) {
                if (type.getName() != null) {
                    ResourceLocation location = type.getName();
                    if (location == null) {
                        PVZMod.LOGGER.warn("Trying to save an invasion type which is not registered.");
                    } else {
                        types.add(StringTag.valueOf(location.toString()));
                    }
                }
            }
            tag.put("invasion_types", types);
        }
        return tag;
    }


    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        if (tag.contains("invasion_types")) {
            if (this.invasionTypes == null) invasionTypes = new ArrayList<>();
            this.invasionTypes.clear();
            ListTag types = ((ListTag) tag.get("invasion_types"));
            types.forEach(typeName ->  {
                InvasionType type = InvasionType.getInvasionType(new ResourceLocation(typeName.getAsString()));
                if (type == null) {
                    PVZMod.LOGGER.warn("Trying to load an invasion type with name" + new ResourceLocation(typeName.getAsString()) + " which is unavailable.");
                } else {
                    this.invasionTypes.add(type);
                }
            });
        }
        if (tag.contains("leader")) {
            this.leaderUUID = tag.getUUID("leader");
        }
    }
}
