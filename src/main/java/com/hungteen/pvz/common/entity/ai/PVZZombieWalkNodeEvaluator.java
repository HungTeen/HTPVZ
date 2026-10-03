package com.hungteen.pvz.common.entity.ai;

import com.hungteen.pvz.common.register.PVZBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.Node;
import net.minecraft.world.level.pathfinder.WalkNodeEvaluator;
import org.jetbrains.annotations.Nullable;

/**
 * @author PangTeen
 * @program HTPVZ
 * @create 2026/10/3 16:42
 **/
public class PVZZombieWalkNodeEvaluator extends WalkNodeEvaluator {

    @Override
    public int getNeighbors(Node[] neighborsArray, Node current) {
        int neighbors = super.getNeighbors(neighborsArray, current);
        return addLadderNeighbors(neighborsArray, current, neighbors);
    }

    @Override
    protected boolean isNeighborValid(@Nullable Node neighbor, Node current) {
        if (!inValidNeighbor(neighbor, current)) {
            return false;
        }
        return super.isNeighborValid(neighbor, current);
    }

    @Override
    protected boolean isDiagonalValid(Node current, @Nullable Node neighbor1, @Nullable Node neighbor2, @Nullable Node p_77633_) {
        if (!inValidNeighbor(neighbor1, current) || !inValidNeighbor(neighbor2, current)) {
            return false;
        }
        return super.isDiagonalValid(current, neighbor1, neighbor2, p_77633_);
    }

    private boolean inValidNeighbor(@Nullable Node neighbor, Node current) {
        if (neighbor != null) {
            BlockState neighborState = level.getBlockState(neighbor.asBlockPos());
            if (neighborState.is(PVZBlocks.COPPER_LADDER.get())) {
                // 往铜梯子上扩散的节点，一定不能是它的反面
                Direction direction = neighborState.getValue(LadderBlock.FACING);
                if (current.asBlockPos().relative(direction).equals(neighbor.asBlockPos())) {
                    return false;
                }
            }
        }
        return true;
    }

    private int addLadderNeighbors(Node[] neighbors, Node current, int count) {
        if (this.isCopperLadder(current.x, current.y, current.z)
            || this.isCopperLadder(current.x, current.y + 1, current.z)) {
            count = this.addLadderNeighbor(neighbors, current.x, current.y + 1, current.z, count);
        }
        // TODO PT: 僵尸往下的寻路还不太好实现，因为 travel 里面只能往上
//        if (this.isCopperLadder(current.x, current.y, current.z)
//            || this.isCopperLadder(current.x, current.y - 1, current.z)) {
//            count = this.addLadderNeighbor(neighbors, current.x, current.y - 1, current.z, count);
//        }
        return count;
    }

    private int addLadderNeighbor(Node[] neighbors, int x, int y, int z, int count) {
        if (count >= neighbors.length) {
            return count;
        }
        Node node = this.getNode(x, y, z);
        if (node == null || node.closed || node.costMalus < 0.0F) {
            return count;
        }
        neighbors[count++] = node;
        return count;
    }

    private boolean isCopperLadder(int x, int y, int z) {
        return this.mob.level.getBlockState(new BlockPos(x, y, z)).is(PVZBlocks.COPPER_LADDER.get());
    }
}
