package com.hungteen.pvz.common.entity.zombies;

import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.FluidTags;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.entity.ai.navigation.WaterBoundPathNavigation;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.Vec3;

public class SnorkelZombie extends PVZZombie {
    boolean searchingForLand;
    protected final WaterBoundPathNavigation waterNavigation;
    protected final GroundPathNavigation groundNavigation;
    public SnorkelZombie(EntityType<? extends Zombie> p_34271_, Level p_34272_) {
        super(p_34271_, p_34272_);
        this.moveControl = new SnorkelMoveControl(this);
        this.setPathfindingMalus(BlockPathTypes.WATER, 0.0F);
        this.waterNavigation = new WaterBoundPathNavigation(this, p_34272_);
        this.waterNavigation.setCanFloat(true);
        this.groundNavigation = new GroundPathNavigation(this, p_34272_);
        this.navigation = waterNavigation; //to avoid being equipped with life buoy.
    }
    public static boolean checkSpawnRules(EntityType<PVZZombie> p_218956_, ServerLevelAccessor p_218957_, MobSpawnType p_218958_, BlockPos p_218959_, RandomSource p_218960_) {
        if (!p_218957_.getFluidState(p_218959_.below()).is(FluidTags.WATER)) {
            return false;
        } else {
            return p_218957_.getDifficulty() != Difficulty.PEACEFUL;
        }
    }

    public void tick() {
        this.groundNavigation.setCanFloat(this.getPose() != Pose.SWIMMING && ! level.getBlockState(this.blockPosition().above().above()).getFluidState().isEmpty());
        super.tick();
    }


    public float getWaterSlowDown() {
        return 0.9F;
    }

    //swimming
    boolean wantsToSwim() {
        if (this.searchingForLand) {
            return true;
        } else {
            return EntityUtil.isEntityValid(this.getTarget())
                    && (! ((this.navigation.isDone() || this.getPose() != Pose.SWIMMING)
                    && level.getBlockState(this.blockPosition().above().above()).getFluidState().isEmpty()));
        }
    }

    @Override
    public void updateSwimming() {
        if (! this.level.isClientSide) {
            if (this.isEffectiveAi() && this.isInFluidType() && this.wantsToSwim()) {
                this.navigation = this.waterNavigation;
                this.setSwimming(true);
                this.setPose(Pose.SWIMMING);
            } else {
                this.navigation = this.groundNavigation;
                this.setSwimming(false);
            }
        }
    }

    public void travel(Vec3 p_32394_) {
        if (this.getPose() == Pose.SWIMMING && ! this.isSwimming()
                && this.level.noCollision(this, this.getBoundingBoxForPose(Pose.STANDING).deflate(1.0E-7D))) {
            this.setPose(Pose.STANDING);
        }
        if (this.isEffectiveAi() && this.isInFluidType() && this.wantsToSwim()) {
            this.moveRelative(0.02F, p_32394_);
            this.move(MoverType.SELF, this.getDeltaMovement());
            this.setDeltaMovement(this.getDeltaMovement().scale(0.9D));
        } else {
            super.travel(p_32394_);
        }
    }

    static class SnorkelMoveControl extends MoveControl {
        private final SnorkelZombie zombie;

        public SnorkelMoveControl(SnorkelZombie p_32433_) {
            super(p_32433_);
            this.zombie = p_32433_;
        }

        public void tick() {
            LivingEntity target = this.zombie.getTarget();
            if (this.zombie.wantsToSwim() && this.zombie.isInFluidType()) {
                if (target != null && target.getY() > this.zombie.getY() || this.zombie.searchingForLand) {
                    this.zombie.setDeltaMovement(this.zombie.getDeltaMovement().add(0.0D, 0.002D, 0.0D));
                }

                if (this.zombie.getNavigation().isDone()) {
                    this.zombie.setSpeed(0.0F);
                    return;
                }

                double d0 = this.wantedX - this.zombie.getX();
                double d1 = this.wantedY - this.zombie.getY();
                double d2 = this.wantedZ - this.zombie.getZ();
                double d3 = Math.sqrt(d0 * d0 + d1 * d1 + d2 * d2);
                d1 /= d3;
                float f = (float)(Mth.atan2(d2, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
                this.zombie.setYRot(this.rotlerp(this.zombie.getYRot(), f, 90.0F));
                this.zombie.yBodyRot = this.zombie.getYRot();
                float f1 = (float)(this.speedModifier * this.zombie.getAttributeValue(Attributes.MOVEMENT_SPEED));
                float f2 = Mth.lerp(0.125F, this.zombie.getSpeed(), f1);
                this.zombie.setSpeed(f2);
                this.zombie.setDeltaMovement(this.zombie.getDeltaMovement().add((double)f2 * d0 * 0.002D, (double)f2 * d1 * 0.1D, (double)f2 * d2 * 0.002D));
            } else {
                if (! this.zombie.onGround) {
                    this.zombie.setDeltaMovement(this.zombie.getDeltaMovement().add(0.0D, -0.008D, 0.0D));
                }

                super.tick();
            }
        }
    }
}
