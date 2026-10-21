package com.hungteen.pvz.common.entity.ai.goal;

import com.hungteen.pvz.common.block.CopperLadderBlock;
import com.hungteen.pvz.common.entity.ai.PVZZombieWalkNodeEvaluator;
import com.hungteen.pvz.common.entity.zombies.CopperLadderZombie;
import com.hungteen.pvz.common.register.PVZBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.PathNavigationRegion;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.FenceBlock;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.WallBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.level.pathfinder.PathFinder;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nullable;
import java.util.*;

public class CopperLadderBuildGoal extends Goal {

    private static final int BUILD_INTERVAL = 20;
    private static final int SEARCH_RADIUS = 4;
    private static final int FAILURE_RETRY_INTERVAL = 40;
    private static final int MAX_PLATFORM_CHECKS = 8;
    private static final int PATH_SEARCH_RANGE = 40;
    private static final int PATH_ACCURACY = 1;
    private static final float MAX_PATH_LENGTH = 32.0F;
    private static final int PATH_MAX_VISITED_NODES = 512;
    private static final int TARGET_TOLERANCE = 2;

    private final CopperLadderZombie zombie;
    private Direction buildingDirection;
    private Direction buildingFacing;
    private BlockPos buildingStart;
    private BlockPos buildingEnd;
    private BlockPos standingPos;
    private boolean planFinished = false;
    private int nextBuildTick;
    private long retryAt;

    public CopperLadderBuildGoal(CopperLadderZombie zombie) {
        this.zombie = zombie;
        this.setFlags(EnumSet.of(Flag.MOVE, Flag.JUMP));
    }

    @Override
    public boolean canUse() {
        if (this.zombie.level.isClientSide || this.zombie.level.getGameTime() < this.retryAt || ! this.zombie.canBuildLadder()) {
            return false;
        }
        LivingEntity target = this.zombie.getTarget();
        Direction direction = getBuildDirection(target);
        if (direction == null) {
            return false;
        }
        this.buildingDirection = direction;
        if (! this.pathFinished()) {
            // 必须是走投无路了才行
            return false;
        }
        if (! this.findPlan(target, direction)) {
            this.retryAt = this.zombie.level.getGameTime() + FAILURE_RETRY_INTERVAL;
            return false;
        }
        return true;
    }

    @Override
    public boolean canContinueToUse() {
        if (!this.zombie.canBuildLadder() || this.planFinished) {
            return false;
        }
        return ObjectUtils.allNotNull(this.buildingDirection, this.buildingFacing, this.buildingStart, this.buildingEnd, this.standingPos)
            && this.getStablePlatformFacing(this.buildingEnd, this.buildingFacing) != null;
    }

    @Override
    public void start() {
        this.nextBuildTick = BUILD_INTERVAL;
        this.planFinished = false;
        this.zombie.getNavigation().stop();
    }

    @Override
    public void stop() {
        this.planFinished = false;
        this.buildingStart = null;
        this.buildingEnd = null;
        this.buildingFacing = null;
        this.buildingDirection = null;
        this.standingPos = null;
    }

    @Override
    public void tick() {
        if (ObjectUtils.anyNull(this.buildingDirection, this.buildingFacing, this.buildingStart, this.buildingEnd, this.standingPos)) {
            // 没有计划，直接跳过
            return;
        }
        this.zombie.getLookControl().setLookAt(Vec3.atCenterOf(this.buildingStart));
        if (this.zombie.getNavigation().moveTo(this.standingPos.getX(), this.standingPos.getY(), this.standingPos.getZ(), 1F)) {
            // 必须站在正确的位置才能开始搭梯子，因此优先移动
            return;
        }
        if (--this.nextBuildTick > 0) {
            return;
        }

        BlockPos.MutableBlockPos pos = this.buildingStart.mutable().move(this.buildingDirection.getOpposite());
        BlockPos nextPos = null;
        while (!pos.equals(this.buildingEnd)) {
            pos.move(this.buildingDirection);
            if (!CopperLadderBlock.isCopperLadder(this.zombie.level.getBlockState(pos))) {
                nextPos = pos.immutable();
                break;
            }
        }

        // 没找到说明放置完成了
        if (nextPos == null) {
            this.planFinished = true;
            this.retryAt = this.zombie.level.getGameTime() + FAILURE_RETRY_INTERVAL;
            if (this.zombie.getTarget() != null) {
                this.zombie.getNavigation().createPath(this.zombie.getTarget(), 0);
            }
            return;
        }

        BlockState ladderState = this.getLadderState(nextPos, this.buildingFacing);
        if (ladderState == null || !this.zombie.canBuildLadder()) {
            this.planFinished = true;
            this.retryAt = this.zombie.level.getGameTime() + FAILURE_RETRY_INTERVAL;
            return;
        }

        this.zombie.level.setBlock(nextPos, ladderState, 3);

        this.zombie.consumeLadder();
        this.nextBuildTick = BUILD_INTERVAL;
        this.zombie.swing(InteractionHand.MAIN_HAND);
    }

    private @Nullable CopperLadderBlock getBlock() {
        return this.zombie.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof BlockItem i
                && i.getBlock() instanceof CopperLadderBlock b ? b : null;
    }

    private boolean pathFinished() {
        Path path = this.zombie.getNavigation().getPath();
        if (path == null || (path.isDone() && this.zombie.getRandom().nextFloat() < 0.2)) {
            return true;
        }
        if (this.zombie.getTarget() != null) {
            double dx = this.zombie.getTarget().getX() - this.zombie.getX();
            double dz = this.zombie.getTarget().getZ() - this.zombie.getZ();
            return dx * dx + dz * dz < 1;
        }
        return true;
    }

    private Direction getBuildDirection(LivingEntity target) {
        if (target == null) {
            return null;
        }
        return Direction.UP;
    }

    private boolean findPlan(LivingEntity target, Direction direction) {
        if (direction != Direction.UP) {
            return false;
        }
        CopperLadderBlock block = this.getBlock();
        if (block == null) {
            return false;
        }
        BlockPos origin = this.zombie.blockPosition();
        BlockPos targetPos = target.blockPosition();
        int ladderCount = this.zombie.getItemInHand(InteractionHand.MAIN_HAND).getCount();
        int maxLadders = Math.min(ladderCount, CopperLadderBlock.MAX_SEARCH_DISTANCE + 1);
        if (maxLadders <= 0) {
            return false;
        }

        int checks = 0;
        List<BlockPos> columns = new ArrayList<>();
        int dirX = Integer.signum(targetPos.getX() - origin.getX());
        int dirZ = Integer.signum(targetPos.getZ() - origin.getZ());
        for (int dx = -SEARCH_RADIUS; dx <= SEARCH_RADIUS; ++ dx) {
            for (int dz = -SEARCH_RADIUS; dz <= SEARCH_RADIUS; ++ dz) {
                if (dx * dx + dz * dz > SEARCH_RADIUS * SEARCH_RADIUS) {
                    continue;
                }
                columns.add(origin.offset(dx, 0, dz));
            }
        }
        columns.sort(Comparator.comparingDouble(pos -> {
            double distance = pos.distSqr(origin);
            int toward = (pos.getX() - origin.getX()) * dirX + (pos.getZ() - origin.getZ()) * dirZ;
            return distance - toward * 0.75D;
        }));
        for (BlockPos column : columns) {
            BlockPos base = null;
            Level level = this.zombie.level;
            for (int dy = 2; dy >= -3; -- dy) {
                BlockPos pos = column.offset(0, dy, 0);
                if (! level.isInWorldBounds(pos) || ! level.isLoaded(pos) || ! this.hasClearance(pos)) {
                    continue;
                }
                if (block.isGroundSupport(level, pos.below())) {
                    base = pos;
                    break;
                }
            }
            if (base == null) {
                continue;
            }
            for (PlatformCandidate candidate : this.findPlatformsAbove(block, base, maxLadders)) {
                if (++ checks > MAX_PLATFORM_CHECKS) {
                    return false;
                }
                Path path = this.createPathFrom(candidate.platformPos, targetPos);
                if (path == null || path.getNodeCount() <= 0) {
                    continue;
                }
                BlockPos end = path.getNode(path.getNodeCount() - 1).asBlockPos();
                int distance = Math.abs(end.getX() - targetPos.getX())
                        + Math.abs(end.getY() - targetPos.getY())
                        + Math.abs(end.getZ() - targetPos.getZ());
                if (distance > TARGET_TOLERANCE) {
                    continue;
                }
                BlockPos standing = this.findStandingPos(base, candidate.facing, base.getY(), null);
                if (! this.canWalkTo(standing)) {
                    continue;
                }
                this.buildingStart = base;
                this.buildingEnd = candidate.ladderTop;
                this.buildingFacing = candidate.facing;
                this.standingPos = standing;
                return true;
            }
        }
        return false;
    }

    private List<PlatformCandidate> findPlatformsAbove(CopperLadderBlock block, BlockPos base, int maxLadders) {
        List<PlatformCandidate> candidates = new ArrayList<>();
        Level level = this.zombie.level;
        int maxY = Math.min(base.getY() + maxLadders - 1, level.getMaxBuildHeight() - 1);
        int lastStableY = Integer.MIN_VALUE;
        for (int y = base.getY(); y <= maxY; ++ y) {
            BlockPos pos = base.atY(y);
            if (! level.isInWorldBounds(pos) || ! level.isLoaded(pos)) {
                break;
            }
            BlockState state = level.getBlockState(pos);
            if (! CopperLadderBlock.isCopperLadder(state) && ! state.getMaterial().isReplaceable()) {
                break;
            }
            if (! this.hasClearance(pos)) {
                break;
            }
            Direction facingWall = CopperLadderBlock.getCopperLadderFacing(state);
            if (facingWall != null && ! this.isFaceSturdy(pos.relative(facingWall.getOpposite()), facingWall)) {
                facingWall = null;
            }
            for (Direction direction : Direction.Plane.HORIZONTAL) {
                if (this.isFaceSturdy(pos.relative(direction.getOpposite()), direction)) {
                    facingWall = direction;
                }
            }
            boolean stable = block.isGroundSupport(level, pos.below()) || facingWall != null;
            if (stable) {
                lastStableY = y;
            } else if (lastStableY == Integer.MIN_VALUE || y - lastStableY > CopperLadderBlock.MAX_SEARCH_DISTANCE) {
                break;
            }
            if (y > base.getY()) {
                Direction facing = null;
                for (Direction direction : Direction.Plane.HORIZONTAL) {
                    BlockPos stepOn = pos.relative(direction.getOpposite());
                    if (this.isPlatformSupport(stepOn) && this.hasClearance(stepOn.above())) {
                        facing = direction;
                        break;
                    }
                }
                if (facing != null) {
                    candidates.add(new PlatformCandidate(pos, facing, pos.relative(facing.getOpposite()).above()));
                }
            }
        }
        return candidates;
    }

    private boolean isFaceSturdy(BlockPos wallPos, Direction face) {
        return this.zombie.level.getBlockState(wallPos).isFaceSturdy(this.zombie.level, wallPos, face);
    }

    @Nullable
    private Path createPathFrom(BlockPos from, BlockPos targetPos) {
        Level level = this.zombie.level;
        if (from.distSqr(targetPos) > (double) (PATH_SEARCH_RANGE * PATH_SEARCH_RANGE)) {
            return null;
        }
        PVZZombieWalkNodeEvaluator evaluator = new PVZZombieWalkNodeEvaluator();
        evaluator.setStartOverride(from);
        evaluator.setCanPassDoors(true);
        evaluator.setCanOpenDoors(false);
        evaluator.setCanFloat(true);
        PathFinder pathFinder = new PathFinder(evaluator, PATH_MAX_VISITED_NODES);
        int margin = PATH_SEARCH_RANGE + 4;
        BlockPos min = from.offset(-margin, -margin, -margin);
        BlockPos max = from.offset(margin, margin, margin);
        PathNavigationRegion region = new PathNavigationRegion(level, min, max);
        return pathFinder.findPath(region, this.zombie, Set.of(targetPos), PATH_SEARCH_RANGE, PATH_ACCURACY, MAX_PATH_LENGTH);
    }

    private boolean canWalkTo(BlockPos pos) {
        if (this.zombie.blockPosition().distSqr(pos) <= 4.0D) {
            return this.hasClearance(pos);
        }
        return this.zombie.getNavigation().createPath(pos, 0) != null;
    }

    private BlockPos findStandingPos(BlockPos buildingStart, Direction facing, int minStartY, @Nullable BlockPos preferred) {
        CopperLadderBlock block = this.getBlock();
        if (block == null) return preferred != null ? preferred : buildingStart;
        // 找到梯子前面几格
        BlockPos relative = buildingStart.relative(facing, 3);
        // 随便往周围偏移，找找符合的位置
        for (int i = 0; i < 5; ++ i) {
            int dx = this.zombie.getRandom().nextIntBetweenInclusive(-2, 2);
            int dz = this.zombie.getRandom().nextIntBetweenInclusive(-2, 2);
            for (int j = -1; j <= 2; ++ j) {
                // 上下偏移找地面
                BlockPos curPos = relative.offset(dx, j, dz);
                if (curPos.getY() >= minStartY && block.isGroundSupport(this.zombie.level, curPos.below()) && hasClearance(curPos)) {
                    return curPos;
                }
            }
        }
        return preferred != null && hasClearance(preferred) ? preferred : buildingStart;
    }

    private Direction getStablePlatformFacing(BlockPos layer, @Nullable Direction direction) {
        if (direction != null) {
            if (hasStableDirectionPlatform(layer, direction)) {
                return direction;
            }
            return null;
        }
        for (Direction dir : Direction.Plane.HORIZONTAL) {
            if (hasStableDirectionPlatform(layer, dir)) {
                return dir;
            }
        }
        return null;
    }

    private boolean hasStableDirectionPlatform(BlockPos layer, Direction facing) {
        BlockPos stepOn = layer.relative(facing.getOpposite());
        CopperLadderBlock block = this.getBlock();
        if (block == null) {
            return false;
        }
        return this.isPlatformSupport(stepOn) && hasClearance(stepOn.above());
    }

    private boolean isPlatformSupport(BlockPos stepOn) {
        Level level = this.zombie.level;
        BlockState state = level.getBlockState(stepOn);
        VoxelShape shape = state.getCollisionShape(level, stepOn, CollisionContext.empty());
        if (! shape.isEmpty()) {
            return true;
        }
        Block block = level.getBlockState(stepOn.below()).getBlock();
        return block instanceof WallBlock || block instanceof FenceBlock;
    }

    private boolean hasClearance(BlockPos pos) {
        double halfWidth = this.zombie.getBbWidth() / 2.0D;
        double height = this.zombie.getBbHeight();
        AABB box = new AABB(pos.getX() + 0.5D - halfWidth, pos.getY(), pos.getZ() + 0.5D - halfWidth,
            pos.getX() + 0.5D + halfWidth, pos.getY() + height, pos.getZ() + 0.5D + halfWidth);
        return this.zombie.level.noCollision(box);
    }

    private BlockState getLadderState(BlockPos pos, Direction facing) {
        Level level = this.zombie.level;
        BlockState oldState = level.getBlockState(pos);
        if (!oldState.getMaterial().isReplaceable()) {
            return null;
        }

        FluidState fluidState = level.getFluidState(pos);
        boolean waterlogged = fluidState.getType() == Fluids.WATER;
        BlockState state = PVZBlocks.COPPER_LADDER.get().defaultBlockState().setValue(LadderBlock.FACING, facing)
            .setValue(LadderBlock.WATERLOGGED, waterlogged);
        if (((CopperLadderBlock)PVZBlocks.COPPER_LADDER.get()).canSurvive(state, level, pos)) {
            return state;
        }

        for (Direction direction : Direction.Plane.HORIZONTAL) {
            state = PVZBlocks.COPPER_LADDER.get().defaultBlockState().setValue(LadderBlock.FACING, direction)
                .setValue(LadderBlock.WATERLOGGED, waterlogged);
            if (((CopperLadderBlock)PVZBlocks.COPPER_LADDER.get()).canSurvive(state, level, pos)) {
                return state;
            }
        }
        return null;
    }

    private record PlatformCandidate(BlockPos ladderTop, Direction facing, BlockPos platformPos) {
    }

}
