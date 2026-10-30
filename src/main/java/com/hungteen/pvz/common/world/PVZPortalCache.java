package com.hungteen.pvz.common.world;

import com.hungteen.pvz.common.entity.Portal;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class PVZPortalCache {

    public static final float PORTAL_COST = 4.0F;

    private static final Map<ResourceKey<Level>, Long2ObjectMap<Pair>> CACHE = new HashMap<>();
    private static final Map<ResourceKey<Level>, List<Pair>> PAIRS = new HashMap<>();
    private static int tickCount = 0;

    public record Pair(Portal entry, Portal exit, BlockPos exitNode, List<BlockPos> entryNodes) {}

    public static void tick(TickEvent.ServerTickEvent ev) {
        if (++ tickCount % 10 != 0) return;
        CACHE.clear();
        PAIRS.clear();
        for (ServerLevel level : ev.getServer().getAllLevels()) {
            refresh(level);
        }
    }

    @Nullable
    public static Pair getPair(Level level, BlockPos pos) {
        Long2ObjectMap<Pair> map = CACHE.get(level.dimension());
        return map == null ? null : map.get(pos.asLong());
    }

    public static List<Pair> getPairs(Level level) {
        return PAIRS.getOrDefault(level.dimension(), List.of());
    }

    public static boolean isEmpty(Level level) {
        Long2ObjectMap<Pair> map = CACHE.get(level.dimension());
        return map == null || map.isEmpty();
    }

    public static List<Pair> selectPairs(Level level, Vec3 from, Set<BlockPos> targets) {
        List<Pair> all = getPairs(level);
        if (all.size() <= 1 || targets.isEmpty()) return all;
        Set<Pair> selected = new LinkedHashSet<>();
        for (BlockPos target : targets) {
            double direct = from.distanceTo(Vec3.atCenterOf(target));
            Pair best = null;
            double bestVia = Double.MAX_VALUE;
            for (Pair pair : all) {
                double via = distanceToEntry(from, pair) + PORTAL_COST + distance(pair.exitNode(), target);
                if (via < bestVia) {
                    bestVia = via;
                    best = pair;
                }
                if (via <= direct) {
                    selected.add(pair);
                }
            }
            if (best != null) {
                selected.add(best);
            }
        }
        return selected.isEmpty() ? all : new ArrayList<>(selected);
    }

    public static float[] computeExitHeuristics(List<Pair> pairs, BlockPos target) {
        int size = pairs.size();
        float[] result = new float[size];
        for (int i = 0; i < size; i++) {
            result[i] = distance(pairs.get(i).exitNode(), target);
        }
        for (int iteration = 0; iteration < size; iteration++) {
            boolean changed = false;
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    float candidate = distanceToEntry(pairs.get(i).exitNode(), pairs.get(j)) + PORTAL_COST + result[j];
                    if (candidate < result[i]) {
                        result[i] = candidate;
                        changed = true;
                    }
                }
            }
            if (!changed) break;
        }
        return result;
    }

    public static float lowerBound(List<Pair> pairs, float[] exitHeuristics, BlockPos node, BlockPos target, float direct) {
        float result = direct;
        for (int i = 0; i < pairs.size(); i++) {
            float candidate = distanceToEntry(node, pairs.get(i)) + PORTAL_COST + exitHeuristics[i];
            if (candidate < result) {
                result = candidate;
            }
        }
        return result;
    }

    private static float distanceToEntry(BlockPos pos, Pair pair) {
        float result = Float.MAX_VALUE;
        for (BlockPos entryNode : pair.entryNodes()) {
            result = Math.min(result, distance(pos, entryNode));
        }
        return result == Float.MAX_VALUE ? distance(pos, pair.entry().blockPosition()) : result;
    }

    private static double distanceToEntry(Vec3 pos, Pair pair) {
        double result = Double.MAX_VALUE;
        for (BlockPos entryNode : pair.entryNodes()) {
            result = Math.min(result, pos.distanceTo(Vec3.atCenterOf(entryNode)));
        }
        return result == Double.MAX_VALUE ? pos.distanceTo(Vec3.atCenterOf(pair.entry().blockPosition())) : result;
    }

    private static float distance(BlockPos a, BlockPos b) {
        float dx = a.getX() - b.getX();
        float dy = a.getY() - b.getY();
        float dz = a.getZ() - b.getZ();
        return (float) Math.sqrt(dx * dx + dy * dy + dz * dz);
    }

    private static void refresh(ServerLevel level) {
        Iterable<Entity> entities = level.getEntities().getAll();
        List<Portal> portals = new ArrayList<>();
        entities.forEach(entity -> {
            if (entity instanceof Portal) portals.add((Portal) entity);
        });
        Long2ObjectMap<Pair> map = new Long2ObjectOpenHashMap<>();
        List<Pair> pairs = new ArrayList<>();
        for (Portal entry : portals) {
            Portal exit = entry.getPairedPortal();
            if (exit == null || !exit.isAlive() || exit.getState() != 1) continue;
            if (entry.getState() != 1) continue;

            Vec3 normal = new Vec3(0, 1, 0).cross(exit.getPlaneRight()).normalize();
            int nx = (int) Math.round(normal.x);
            int nz = (int) Math.round(normal.z);
            BlockPos exitNode = exit.blockPosition().offset(nx, 0, nz);
            BlockPathTypes type = WalkNodeEvaluator.getBlockPathTypeRaw(level, exitNode);
            if (type == BlockPathTypes.BLOCKED || type.getMalus() < 0) {
                exitNode = exit.blockPosition().offset(- nx, 0, - nz);
                type = WalkNodeEvaluator.getBlockPathTypeRaw(level, exitNode);
                if (type == BlockPathTypes.BLOCKED || type.getMalus() < 0) {
                    exitNode = exit.blockPosition();
                    type = WalkNodeEvaluator.getBlockPathTypeRaw(level, exitNode);
                    if (type == BlockPathTypes.BLOCKED || type.getMalus() < 0) continue;
                }
            }

            Set<BlockPos> entryNodes = new LinkedHashSet<>();
            Vec3 right = entry.getPlaneRight();
            Vec3 center = entry.getPortalCenter();
            AABB aabb = entry.getBoundingBox().inflate(0.5);
            BlockPos.betweenClosedStream(aabb).forEach(pos -> {
                Vec3 rel = Vec3.atCenterOf(pos).subtract(center);
                double u = rel.dot(right);
                double v = pos.getY() + 0.5 - center.y;
                if (Math.abs(u) <= Portal.WIDTH / 2 && Math.abs(v) <= Portal.HEIGHT / 2) {
                    entryNodes.add(pos.immutable());
                }
            });

            Vec3 entryNormal = entry.getPlaneNormal();
            for (double s : new double[]{0.6D, 1.6D}) {
                for (double u = -1; u <= 1; u += 1) {
                    for (double sign : new double[]{1, -1}) {
                        Vec3 p = entry.position()
                                .add(entryNormal.scale(s * sign))
                                .add(right.scale(u));
                        BlockPos pos = new BlockPos(p);
                        BlockPathTypes t = WalkNodeEvaluator.getBlockPathTypeRaw(level, pos);
                        if (t != BlockPathTypes.BLOCKED && t.getMalus() >= 0) {
                            entryNodes.add(pos);
                        }
                    }
                }
            }

            if (entryNodes.isEmpty()) continue;
            Pair pair = new Pair(entry, exit, exitNode, List.copyOf(entryNodes));
            pairs.add(pair);
            for (BlockPos pos : entryNodes) {
                map.putIfAbsent(pos.asLong(), pair);
            }
        }
        if (!map.isEmpty()) {
            CACHE.put(level.dimension(), map);
            PAIRS.put(level.dimension(), pairs);
        }
    }
}
