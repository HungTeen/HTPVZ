package com.hungteen.pvz.common.entity.ai.goal;

import com.hungteen.pvz.common.block.CopperLadderBlock;
import com.hungteen.pvz.common.entity.zombies.CopperLadderZombie;
import com.hungteen.pvz.common.register.PVZBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.Path;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.apache.commons.lang3.ObjectUtils;

import javax.annotation.Nullable;
import java.util.EnumSet;

public class CopperLadderBuildGoal extends Goal {

    private static final int BUILD_INTERVAL = 20;
    private static final int SEARCH_RADIUS = 4;
    private static final int FAILURE_RETRY_INTERVAL = 40;

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
        if (this.zombie.level.isClientSide || this.zombie.level.getGameTime() < this.retryAt || !this.zombie.canBuildLadder()) {
            return false;
        }
        LivingEntity target = this.zombie.getTarget();
        Direction direction = getBuildDirection(target);
        if (direction == null) {
            return false;
        }
        this.buildingDirection = direction;
        if (!this.pathFinished()) {
            // 必须是走投无路了才行
            return false;
        }
        if (!this.findPlan(target, direction)) {
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
        int dy = target.blockPosition().getY() - this.zombie.blockPosition().getY();
        if (dy >= 2) {
            return Direction.UP;
        }
        //        if (dy <= -4) {
        //            return Direction.DOWN;
        //        }
        return null;
    }

    private boolean findPlan(LivingEntity target, Direction direction) {
        int startY = this.zombie.blockPosition().getY();
        int targetY = target.blockPosition().getY();
        int firstY = direction == Direction.UP ? startY : startY - 1;
        int endY = direction == Direction.UP ? Math.min(targetY, startY + CopperLadderBlock.MAX_SEARCH_DISTANCE)
            : Math.max(targetY, startY - CopperLadderBlock.MAX_SEARCH_DISTANCE);

        for (int i = 0; i < 6; ++ i) {
            int dx = this.zombie.getRandom().nextIntBetweenInclusive(-SEARCH_RADIUS, SEARCH_RADIUS);
            int dz = this.zombie.getRandom().nextIntBetweenInclusive(-SEARCH_RADIUS, SEARCH_RADIUS);
            BlockPos base = new BlockPos(this.zombie.getBlockX() + dx, firstY, this.zombie.getBlockZ() + dz);
            BlockPos bottom = null;
            for (int dh = -1; dh <= 3; ++dh) {
                BlockPos cur = base.below(dh);
                // 地面最好是结实的
                if (hasStableGround(cur.below()) && hasClearance(cur)) {
                    bottom = cur;
                }
            }
            if (bottom == null) {
                // 找不到放置梯子的地面
                continue;
            }
            BlockPos planEndPos = this.calculateBuildingEndPos(bottom, direction, endY);
            if (planEndPos != null) {
                this.buildingStart = bottom;
                this.buildingEnd = planEndPos;
                this.standingPos = findStandingPos(this.buildingStart, this.buildingFacing);
                return true;
            }
        }
        return false;
    }

    private BlockPos findStandingPos(BlockPos buildingStart, Direction facing) {
        // 找到梯子前面几格
        BlockPos relative = buildingStart.relative(facing, 3);
        // 随便往周围偏移，找找符合的位置
        for (int i = 0; i < 5; ++ i) {
            int dx = this.zombie.getRandom().nextIntBetweenInclusive(-2, 2);
            int dz = this.zombie.getRandom().nextIntBetweenInclusive(-2, 2);
            for (int j = -1; j <= 2; ++ j) {
                // 上下偏移找地面
                BlockPos curPos = relative.offset(dx, j, dz);
                if (hasStableGround(curPos.below()) && hasClearance(curPos)) {
                    return curPos;
                }
            }
        }
        // 兜底位置
        return buildingStart;
    }

    private BlockPos calculateBuildingEndPos(BlockPos base, Direction direction, int endY) {
        Level level = this.zombie.level;
        BlockPos.MutableBlockPos pos = base.mutable();
        Direction buildFacing = null;
        while (pos.getY() != endY) {
            pos.move(direction);
            if (!level.isInWorldBounds(pos) || !level.isLoaded(pos)) {
                return null;
            }
            BlockState state = level.getBlockState(pos);
            // 当前位置要么有梯子了，要么可被替换
            if (!CopperLadderBlock.isCopperLadder(state) && !state.getMaterial().isReplaceable()) {
                return null;
            }
            Direction ladderFacing = CopperLadderBlock.getCopperLadderFacing(state);
            if (ladderFacing != null) {
                if (buildFacing != null && !buildFacing.equals(ladderFacing)) {
                    // 存在方向不同的梯子
                    return null;
                }
                buildFacing = ladderFacing;
            }
            // 找到了安全平台，直接返回即可
            Direction stablePlatformFacing = getStablePlatformFacing(pos.immutable(), buildFacing);
            if (stablePlatformFacing != null) {
                this.buildingFacing = stablePlatformFacing;
                return pos.immutable();
            }
        }
        return null;
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
        return hasStableGround(stepOn) && hasClearance(stepOn.above());
    }

    private boolean hasStableGround(BlockPos pos) {
        return this.zombie.level.getBlockState(pos).isFaceSturdy(this.zombie.level, pos, Direction.UP);
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

}
