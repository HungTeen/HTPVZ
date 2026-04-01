package com.hungteen.pvz.common.world.invasion;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.world.PathSeeker;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.NaturalSpawner;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.common.util.TriPredicate;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Function;

public class InvasionTeam {
    private final PathSeeker seeker;
    private final ServerPlayer target;
    private static final Map<ServerPlayer, InvasionTeam> teamsToSummon = new HashMap<>();
    private static final Random random = new Random();
    private final List<InvasionType> invasionTypes;

    public InvasionTeam(PathSeeker seeker, ServerPlayer target) {
        this.seeker = seeker;
        this.target = target;
        this.invasionTypes = InvasionType.generateTypes(target);
    }


    public static void spawnFor(ServerPlayer player) {
        PathSeeker seeker = new PathSeeker((ServerLevel) player.level);
        seeker.minDistanceSqr = 576;
        seeker.maxDistanceSqr = 1024;
        InvasionTeam team = new InvasionTeam(seeker, player);
        team.seeker.center = player.blockPosition();
        team.seeker.targetPos = player.blockPosition();
        teamsToSummon.put(player, team);
    }

    /**@return Whether this team has ended spawning.*/
    public boolean tick() {
        this.seeker.tick();
        if (this.invasionTypes.isEmpty() || this.invasionTypes.stream().allMatch(type -> type.isAvailable(target, invasionTypes))) {
            return true;
        }
        if (! seeker.availablePositions.isEmpty()) {
            for (BlockPos pos : seeker.availablePositions) {
                if (target.level.getBrightness(LightLayer.BLOCK, pos) > 0) {
                    return false;
                }
                double dist = pos.distSqr(target.blockPosition());
                if (dist > 576 && dist < 1024) {
                    double horizontalDistSqr = (pos.getX() - target.getX()) * (pos.getX() - target.getX()) + (pos.getZ() - target.getZ()) * (pos.getZ() - target.getZ());
                    double verticalDist = pos.getY() - target.getY();
                    if (verticalDist > 0 || verticalDist * verticalDist < 1.5 * horizontalDistSqr) {
                        if (! invasionTypes.isEmpty()) {
                            Vec3 vec3 = Vec3.atBottomCenterOf(pos);
                            AtomicBoolean result = new AtomicBoolean();
                            result.set(false);
                            final int size = random.nextInt(4);
                            List<CompoundTag> tags = new ArrayList<>();
                            Optional<InvasionType.EnemyType> leader = invasionTypes.get(0).flagEnemy();
                            List<InvasionType.EnemyType> types = invasionTypes.get(0).enemies();
                            for (int i = 0; i < size; i ++) {
                                tags.add(types.get(random.nextInt(types.size())).entityData());
                            }
                            Entity entity;
                            if (leader.isPresent()) {
                                entity = summonEntity(leader.get().entityData().copy(), target.level, vec3);
                            } else {
                                entity = summonEntity(tags.get(0).copy(), target.level, vec3);
                                tags.remove(tags.get(0));
                            }
                            if (entity != null) {
                                entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> cap.containsInvasion = true);
                            }
                            tags.forEach(tag -> summonEntity(tags.get(0), target.level, vec3));
                            return true;
                        }
                    }
                }
            }
            seeker.availablePositions.clear();
        }
        return false;
    }

    public static void serverTick() {
        for (InvasionTeam team : teamsToSummon.values().stream().toList()) {
            if (team.tick()) {
                teamsToSummon.remove(team.target);
            }
        }
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
        return entity;
    }
}
