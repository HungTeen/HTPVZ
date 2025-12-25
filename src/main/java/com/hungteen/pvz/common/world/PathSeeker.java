package com.hungteen.pvz.common.world;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Vec3i;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.Mth;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;

import java.util.HashSet;
import java.util.Random;
import java.util.Set;

public class PathSeeker {
    public Set<BlockPos> availablePositions = new HashSet<>();
    public final ServerLevel level;
    public int tickCount;
    public BlockPos center;
    public BlockPos targetPos;
    private static int count = 0;
    private static final Random random = new Random();
    public int minDistance = 144;
    public int maxDistance = 576;

    public PathSeeker(ServerLevel level) {
        this.tickCount = 0;
        this.level = level;
    }

    //raytrace seeker
    private final Set<Branch> branches = new HashSet<>();

    public void tick() {
        this.tickCount += 1;
        if (tickCount % 180 == 0) { //6 seconds before retest.
            tickCount = 0;
        }
        if (availablePositions.size() < 10) {
            this.branches.forEach(branch -> {
                if (PathSeeker.count(4)) {
                    branch.tick();
                }
            });
        }
        endCount();
        //refresh branches.
        if (tickCount % 5 == 0) {
            boolean positiveHalf = tickCount < 80;//decide which half of ball it is on.
            this.branches.clear();
            int tmp = this.tickCount / 5;
            if (tmp % 2 == 1) {
                tmp = tmp / 2;
                Set<Integer> angles = switch (tmp) {
                    case 1 -> Set.of(30, 90);
                    case 2 -> Set.of(60, -60);
                    case 3 -> Set.of(-30, -90);
                    case 4 -> Set.of(10, 80);
                    case 5 -> Set.of(-10, -80);
                    case 6 -> Set.of(20, 70);
                    case 7 -> Set.of(-20, -70);
                    default -> Set.of(0);
                };
                for (int x : angles) {
                    int y = 0;
                    while (y < 180) {
                        y += 15 / Math.cos(y / 57.3);
                        this.branches.add(new Branch(this, this.targetPos,
                                new Vec2(x + this.random.nextInt(10) - 5 + (positiveHalf ? 180 : 0), y + this.random.nextInt(10) - 5)));
                    }
                }
            }
        }
    }
    public static class Branch {
        private final int generation;
        private final PathSeeker seeker;
        private boolean grown = false;
        public final BlockPos position;
        public final Vec2 direction;

        public final Set<Branch> branches = new HashSet<>();
        public Branch(PathSeeker seeker, BlockPos position, Vec2 direction) {
            this(0, seeker, position, direction);
        }
        public Branch(int generation, PathSeeker seeker, BlockPos position, Vec2 direction) {
            this.generation = generation;
            this.seeker = seeker;
            this.position = position;
            this.direction = direction;
        }
        public void tick() {
            if (! grown) {
                BlockHitResult result = this.detect();
                Vec3i offset = result.getDirection().getNormal();
                BlockPos growFrom = result.getBlockPos().offset(offset);
                double dist = result.getBlockPos().distSqr(seeker.targetPos);
                //add to available positions.
                if (result.getType() != HitResult.Type.MISS && result.getBlockPos().distSqr(position) > (0.5625 * (24 - 4 * generation) * (24 - 4 * generation)) &&
                        dist > seeker.minDistance && dist < seeker.maxDistance && result.getDirection() == Direction.UP) {
                    this.seeker.availablePositions.add(growFrom);
                }
                if (this.generation <= 3 && Math.abs(this.direction.x) < 80 && dist > 64 && result.getDirection() != Direction.UP) {
                    this.branches.add(new Branch(5 /*to make branch not keep growing and has enough short acceptable distance.*/,
                            seeker, growFrom, new Vec2(90, 0)));
                    this.branches.forEach(Branch::tick);
                }
                //grow.
                if (this.generation <= 3 && result.getBlockPos().distSqr(position) > 16) {
                    Vec2 prefix = new Vec2(-90 * offset.getY(), 90 * offset.getX() - 180 * offset.getZ());
                    int y = -90;
                    while (y < 90) {
                        int x = -90;
                        while (x < 90) {
                            this.branches.add(new Branch(generation + 1, seeker, growFrom,
                                    new Vec2(prefix.x + x + PathSeeker.random.nextInt(10) - 5, prefix.y + y + PathSeeker.random.nextInt(10) - 5)));
                            x += this.generation * 20 + 20;
                        }
                        y += this.generation * 20 + 20;
                    }
                }
                this.grown = true;
            } else {
                this.branches.forEach(branch -> {
                    if (PathSeeker.count(4 - generation)) {
                        branch.tick();
                    }
                });
                endCount();
            }
        }

        private BlockHitResult detect() {
            float xRot = direction.x;
            float yRot = direction.y;
            float f2 = Mth.cos(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
            float f3 = Mth.sin(-yRot * ((float)Math.PI / 180F) - (float)Math.PI);
            float f4 = -Mth.cos(-xRot * ((float)Math.PI / 180F));
            float y = Mth.sin(-xRot * ((float)Math.PI / 180F));
            float x = f3 * f4;
            float z = f2 * f4;
            double distance = 24 - 4 * generation;
            Vec3 destination = Vec3.atCenterOf(position).add((double)x * distance, (double)y * distance, (double)z * distance);
            return seeker.level.clip(new ClipContext(Vec3.atCenterOf(position), destination, ClipContext.Block.OUTLINE, ClipContext.Fluid.ANY, null));
        }
    }
    private static boolean count(int max) {
        count ++;
        return count <= max;
    }
    private static void endCount() {
        count = 0;
    }
}
