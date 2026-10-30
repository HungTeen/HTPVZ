package com.hungteen.pvz.mixin;

import com.hungteen.pvz.common.world.PVZPortalCache;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

@Mixin(PathFinder.class)
public abstract class PathFinderMixin {

    @Unique @Nullable private Mob pvz$mob;
    @Unique @Nullable private List<PVZPortalCache.Pair> pvz$pairs;
    @Unique @Nullable private Map<BlockPos, float[]> pvz$exitHeuristics;

    @Inject(method = "findPath(Lnet/minecraft/world/level/PathNavigationRegion;Lnet/minecraft/world/entity/Mob;Ljava/util/Set;FIF)Lnet/minecraft/world/level/pathfinder/Path;",
            at = @At("HEAD"))
    private void pvz$preFindPath(PathNavigationRegion region,
                              Mob mob, Set<BlockPos> targets, float maxRange, int accuracy, float searchDepth,
                              CallbackInfoReturnable<Path> cir) {
        this.pvz$mob = mob;
        this.pvz$exitHeuristics = new HashMap<>();
        this.pvz$pairs = mob.level.isClientSide ? List.of() : PVZPortalCache.selectPairs(mob.level, mob.position(), targets);
    }

    @Inject(method = "findPath(Lnet/minecraft/world/level/PathNavigationRegion;Lnet/minecraft/world/entity/Mob;Ljava/util/Set;FIF)Lnet/minecraft/world/level/pathfinder/Path;",
            at = @At("RETURN"), cancellable = true)
    private void pvz$findPath(PathNavigationRegion region,
                                 Mob mob, Set<BlockPos> targets, float maxRange, int accuracy, float searchDepth,
                                 CallbackInfoReturnable<Path> cir) {
        Path path = cir.getReturnValue();
        if (path == null || mob.level.isClientSide) return;
        if (PVZPortalCache.isEmpty(mob.level)) return;
        Path rewritten = EntityUtil.rewritePathThroughPortal(mob.level, mob, path);
        if (rewritten != path) {
            cir.setReturnValue(rewritten);
        }
    }

    @Redirect(method = "getBestH(Lnet/minecraft/world/level/pathfinder/Node;Ljava/util/Set;)F",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/pathfinder/Node;distanceTo(Lnet/minecraft/world/level/pathfinder/Node;)F")
    )
    private float pvz$setPortalH(Node node, Node target) {
        float direct = node.distanceTo(target);
        List<PVZPortalCache.Pair> pairs = this.pvz$pairs;
        if (pairs == null || pairs.isEmpty()) {
            return direct;
        }
        BlockPos targetPos = target.asBlockPos();
        if (this.pvz$exitHeuristics == null) {
            this.pvz$exitHeuristics = new HashMap<>();
        }
        float[] result = this.pvz$exitHeuristics.get(targetPos);
        if (result == null) {
            List<PVZPortalCache.Pair> pairs1 = this.pvz$pairs;
            result = PVZPortalCache.computeExitHeuristics(pairs1 == null ? List.of() : pairs1, targetPos);
            this.pvz$exitHeuristics.put(targetPos, result);
        }
        float[] exitHeuristics = result;
        return PVZPortalCache.lowerBound(pairs, exitHeuristics, node.asBlockPos(), targetPos, direct);
    }

    @Redirect(method = "findPath(Lnet/minecraft/util/profiling/ProfilerFiller;Lnet/minecraft/world/level/pathfinder/Node;Ljava/util/Map;FIF)Lnet/minecraft/world/level/pathfinder/Path;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/pathfinder/PathFinder;distance(Lnet/minecraft/world/level/pathfinder/Node;Lnet/minecraft/world/level/pathfinder/Node;)F")
    )
    private float pvz$setPortalCost(PathFinder self, Node current, Node neighbor) {
        Mob mob = this.pvz$mob;
        if (mob != null && !mob.level.isClientSide) {
            PVZPortalCache.Pair pair = PVZPortalCache.getPair(mob.level, current.asBlockPos());
            if (pair != null && neighbor.asBlockPos().equals(pair.exitNode())) {
                return PVZPortalCache.PORTAL_COST;
            }
        }
        return current.distanceTo(neighbor);
    }

    @Redirect(method = "findPath(Lnet/minecraft/util/profiling/ProfilerFiller;Lnet/minecraft/world/level/pathfinder/Node;Ljava/util/Map;FIF)Lnet/minecraft/world/level/pathfinder/Path;",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/level/pathfinder/Node;distanceManhattan(Lnet/minecraft/world/level/pathfinder/Node;)F")
    )
    private float pvz$avoidPortalEarlyStop(Node node, Node target) {
        float real = node.distanceManhattan(target);
        Mob mob = this.pvz$mob;
        if (mob != null && !mob.level.isClientSide) {
            PVZPortalCache.Pair pair = PVZPortalCache.getPair(mob.level, node.asBlockPos());
            if (pair != null) {
                List<PVZPortalCache.Pair> pairs = this.pvz$pairs;
                if (pairs != null && pairs.contains(pair)) {
                    BlockPos exit = pair.exitNode();
                    float exitDist = Math.abs(exit.getX() - target.x) + Math.abs(exit.getY() - target.y) + Math.abs(exit.getZ() - target.z);
                    if (exitDist < real) {
                        return 1e5f;
                    }
                }
            }
        }
        return real;
    }
}
