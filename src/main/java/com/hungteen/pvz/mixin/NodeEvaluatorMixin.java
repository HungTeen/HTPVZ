package com.hungteen.pvz.mixin;

import com.hungteen.pvz.common.world.PVZPortalCache;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.NodeEvaluator;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import net.minecraftforge.common.Tags;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(WalkNodeEvaluator.class)
public abstract class NodeEvaluatorMixin extends NodeEvaluator {

    @Inject(method = "getNeighbors", at = @At("RETURN"), cancellable = true)
    private void pvz$getNeighbors(Node[] neighbors, Node current, CallbackInfoReturnable<Integer> cir) {
        int count = cir.getReturnValue();
        if (count >= neighbors.length || this.mob.level.isClientSide) return;
        if (this.mob.getType().is(Tags.EntityTypes.BOSSES)) return;
        if (PVZPortalCache.isEmpty(this.mob.level)) return;
        PVZPortalCache.Pair pair = PVZPortalCache.getPair(this.mob.level, current.asBlockPos());
        if (pair == null) return;
        Node exit = this.getNode(pair.exitNode().getX(), pair.exitNode().getY(), pair.exitNode().getZ());
        if (exit == null || exit.closed || exit == current) return;
        BlockPathTypes type = WalkNodeEvaluator.getBlockPathTypeRaw(this.mob.level, pair.exitNode());
        exit.type = type;
        exit.costMalus = Math.max(exit.costMalus, this.mob.getPathfindingMalus(type));

        neighbors[count] = exit;
        cir.setReturnValue(count + 1);
    }
}