package com.hungteen.pvz.common.entity.ai;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.tags.BlockTags;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.level.block.LadderBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.VoxelShape;

/**
 * @author PangTeen
 * @program HTPVZ
 * @create 2026/10/3 16:11
 **/
public class PVZZombieMoveControl extends MoveControl {

    public PVZZombieMoveControl(Mob p_24983_) {
        super(p_24983_);
    }

    @Override
    public void tick() {
        if (this.operation == MoveControl.Operation.MOVE_TO) {
            // 重写 MoveTo
            this.operation = MoveControl.Operation.WAIT;
            double d0 = this.wantedX - this.mob.getX();
            double d1 = this.wantedZ - this.mob.getZ();
            double d2 = this.wantedY - this.mob.getY();
            double d3 = d0 * d0 + d2 * d2 + d1 * d1;
            if (d3 < (double)2.5000003E-7F) {
                this.mob.setZza(0.0F);
                return;
            }
            BlockPos blockpos = this.mob.blockPosition();
            BlockState blockstate = this.mob.level.getBlockState(blockpos);
            boolean justClimb = blockstate.is(BlockTags.CLIMBABLE) && blockstate.hasProperty(LadderBlock.FACING);
            if (justClimb && (this.mob.getTarget() == null || this.mob.getTarget().getY() > this.mob.getY())) {
                // 往梯子上挤就完事了，啥也别干
                Direction direction = blockstate.getValue(LadderBlock.FACING).getOpposite();
                d0 = direction.getStepX();
                d1 = direction.getStepZ();
            }
            float f9 = (float)(Mth.atan2(d1, d0) * (double)(180F / (float)Math.PI)) - 90.0F;
            this.mob.setYRot(this.rotlerp(this.mob.getYRot(), f9, 90.0F));
            this.mob.setSpeed((float)(this.speedModifier * this.mob.getAttributeValue(Attributes.MOVEMENT_SPEED)));
            if (justClimb) {
                return;
            }
            VoxelShape voxelshape = blockstate.getCollisionShape(this.mob.level, blockpos);
            if (d2 > (double)this.mob.getStepHeight() && d0 * d0 + d1 * d1 < (double)Math.max(1.0F, this.mob.getBbWidth()) || !voxelshape.isEmpty() && this.mob.getY() < voxelshape.max(
                Direction.Axis.Y) + (double)blockpos.getY() && !blockstate.is(BlockTags.DOORS) && !blockstate.is(BlockTags.FENCES)) {
                this.mob.getJumpControl().jump();
                this.operation = MoveControl.Operation.JUMPING;
            }
            return;
        }
        super.tick();
    }
}
