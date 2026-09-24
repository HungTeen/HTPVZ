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
import java.util.List;
import java.util.Map;

public class PVZPortalCache {

    private static final Map<ResourceKey<Level>, Long2ObjectMap<Pair>> CACHE = new HashMap<>();
    private static int tickCount = 0;

    public record Pair(Portal entry, Portal exit, BlockPos exitNode) {}

    public static void tick(TickEvent.ServerTickEvent ev) {
        if (++ tickCount % 10 != 0) return;
        CACHE.clear();
        for (ServerLevel level : ev.getServer().getAllLevels()) {
            refresh(level);
        }
    }

    @Nullable
    public static Pair getPair(Level level, BlockPos pos) {
        Long2ObjectMap<Pair> map = CACHE.get(level.dimension());
        return map == null ? null : map.get(pos.asLong());
    }

    public static boolean isEmpty(Level level) {
        Long2ObjectMap<Pair> map = CACHE.get(level.dimension());
        return map == null || map.isEmpty();
    }

    private static void refresh(ServerLevel level) {
        Iterable<Entity> entities = level.getEntities().getAll();
        List<Portal> portals = new ArrayList<>();
        entities.forEach(entity -> {
            if (entity instanceof Portal) portals.add((Portal) entity);
        });
        Long2ObjectMap<Pair> map = new Long2ObjectOpenHashMap<>();
        for (Portal entry : portals) {
            Portal exit = entry.getPairedPortal();
            if (exit == null || !exit.isAlive() || exit.getState() != 1) continue;
            if (entry.getState() != 1) continue;
            Vec3 normal = exit.getPlaneRight().cross(new Vec3(0, 1, 0)).normalize();
            BlockPos exitNode = exit.blockPosition().offset(
                    Math.round(normal.x), 0, Math.round(normal.z));
            BlockPathTypes type = WalkNodeEvaluator.getBlockPathTypeRaw(level, exitNode);
            if (type == BlockPathTypes.BLOCKED || type.getMalus() < 0) {
                exitNode = exit.blockPosition();
                type = WalkNodeEvaluator.getBlockPathTypeRaw(level, exitNode);
                if (type == BlockPathTypes.BLOCKED || type.getMalus() < 0) continue;
            }

            Pair pair = new Pair(entry, exit, exitNode);
            Vec3 right = entry.getPlaneRight();
            Vec3 center = entry.getPortalCenter();
            AABB aabb = entry.getBoundingBox().inflate(0.5);
            BlockPos.betweenClosedStream(aabb).forEach(pos -> {
                Vec3 rel = Vec3.atCenterOf(pos).subtract(center);
                double u = rel.dot(right);
                double v = pos.getY() + 0.5 - center.y;
                if (Math.abs(u) <= Portal.WIDTH / 2 && Math.abs(v) <= Portal.HEIGHT / 2) {
                    map.putIfAbsent(pos.asLong(), pair);
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
                            map.putIfAbsent(pos.asLong(), pair);
                        }
                    }
                }
            }
        }
        if (! map.isEmpty()) CACHE.put(level.dimension(), map);
    }
}