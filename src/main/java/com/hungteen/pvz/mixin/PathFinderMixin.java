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
import java.util.Set;

@Mixin(PathFinder.class)
public abstract class PathFinderMixin {

    @Unique
    @Nullable
    private Mob pvz$mob;

    @Inject(method = "findPath(Lnet/minecraft/world/level/PathNavigationRegion;Lnet/minecraft/world/entity/Mob;Ljava/util/Set;FIF)Lnet/minecraft/world/level/pathfinder/Path;",
            at = @At("HEAD"))
    private void pvz$preFindPath(PathNavigationRegion region,
                              Mob mob, Set<BlockPos> targets, float maxRange, int accuracy, float searchDepth,
                              CallbackInfoReturnable<Path> cir) {
        this.pvz$mob = mob;
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

    @Redirect(
            method = "findPath(Lnet/minecraft/util/profiling/ProfilerFiller;Lnet/minecraft/world/level/pathfinder/Node;Ljava/util/Map;FIF)Lnet/minecraft/world/level/pathfinder/Path;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/pathfinder/PathFinder;distance(Lnet/minecraft/world/level/pathfinder/Node;Lnet/minecraft/world/level/pathfinder/Node;)F")
    )
    private float pvz$setPortalCost(PathFinder self, Node current, Node neighbor) {
        Mob mob = this.pvz$mob;
        if (mob != null && !mob.level.isClientSide) {
            PVZPortalCache.Pair pair = PVZPortalCache.getPair(mob.level, current.asBlockPos());
            if (pair != null && neighbor.asBlockPos().equals(pair.exitNode())) {
                return 4;
            }
        }
        return current.distanceTo(neighbor);
    }

    @Redirect(
            method = "findPath(Lnet/minecraft/util/profiling/ProfilerFiller;Lnet/minecraft/world/level/pathfinder/Node;Ljava/util/Map;FIF)Lnet/minecraft/world/level/pathfinder/Path;",
            at = @At(value = "INVOKE",
                    target = "Lnet/minecraft/world/level/pathfinder/Node;distanceManhattan(Lnet/minecraft/world/level/pathfinder/Node;)F")
    )
    private float pvz$avoidPortalEarlyStop(Node node, Node target) {
        float real = node.distanceManhattan(target);
        Mob mob = this.pvz$mob;
        if (mob != null && !mob.level.isClientSide) {
            PVZPortalCache.Pair pair = PVZPortalCache.getPair(mob.level, node.asBlockPos());
            if (pair != null) {
                BlockPos exit = pair.exitNode();
                float exitDist = Math.abs(exit.getX() - target.x)
                        + Math.abs(exit.getY() - target.y)
                        + Math.abs(exit.getZ() - target.z);
                if (exitDist < real) {
                    return 1E7F;
                }
            }
        }
        return real;
    }
}