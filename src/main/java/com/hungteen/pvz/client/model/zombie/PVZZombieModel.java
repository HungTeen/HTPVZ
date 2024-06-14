package com.hungteen.pvz.client.model.zombie;

import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import net.minecraft.client.model.AnimationUtils;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.UseAnim;

public class PVZZombieModel<T extends PVZZombie> extends PlayerModel<T> {
    public PVZZombieModel(ModelPart p_170821_) {
        super(p_170821_, false);
    }
    public PVZZombieModel(ModelPart p_170821_, boolean slim) {
        super(p_170821_, slim);
    }

    public void setupAnim(T zombie, float p_102002_, float p_102003_, float p_102004_, float p_102005_, float p_102006_) {
        this.head.visible = zombie.renderHead;
        this.hat.visible = zombie.renderHead;
        this.leftSleeve.visible = zombie.getMainArm() == HumanoidArm.LEFT || zombie.renderHand;
        this.leftArm.visible = zombie.getMainArm() == HumanoidArm.LEFT || zombie.renderHand;
        this.rightSleeve.visible = zombie.getMainArm() == HumanoidArm.RIGHT || zombie.renderHand;
        this.rightArm.visible = zombie.getMainArm() == HumanoidArm.RIGHT || zombie.renderHand;

        super.setupAnim(zombie, p_102002_, p_102003_, p_102004_, p_102005_, p_102006_);
        AnimationUtils.animateZombieArms(this.leftArm, this.rightArm, this.isAggressive(zombie), this.attackTime, p_102004_);
        if (this.swimAmount > 0.0F) {
            this.rightArm.xRot = this.rotlerpRad(this.swimAmount, this.rightArm.xRot, -2.5132742F) + this.swimAmount * 0.35F * Mth.sin(0.1F * p_102004_);
            this.leftArm.xRot = this.rotlerpRad(this.swimAmount, this.leftArm.xRot, -2.5132742F) - this.swimAmount * 0.35F * Mth.sin(0.1F * p_102004_);
            this.rightArm.zRot = this.rotlerpRad(this.swimAmount, this.rightArm.zRot, -0.15F);
            this.leftArm.zRot = this.rotlerpRad(this.swimAmount, this.leftArm.zRot, 0.15F);
            this.leftLeg.xRot -= this.swimAmount * 0.55F * Mth.sin(0.1F * p_102004_);
            this.rightLeg.xRot += this.swimAmount * 0.55F * Mth.sin(0.1F * p_102004_);
            this.head.xRot = 0.0F;
        }

        this.setArmPose(zombie);

        if (this.leftArmPose == ArmPose.THROW_SPEAR) {
            this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float)Math.PI * 0.8F;
            this.leftArm.yRot = 0.0F;
        }
        if (this.rightArmPose == ArmPose.THROW_SPEAR) {
            this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float)Math.PI * 0.8F;
            this.rightArm.yRot = 0.0F;
        }
        if (this.rightArmPose == ArmPose.BLOCK) {
            this.rightArm.xRot = this.rightArm.xRot * 0.5F + 0.2F;
            this.rightArm.yRot = (-(float)Math.PI / 4F);
        }
        if (this.leftArmPose == ArmPose.BLOCK) {
            this.leftArm.xRot = this.leftArm.xRot * 0.5F + 0.2F;
            this.leftArm.yRot = ((float)Math.PI / 4F);
        }
        if (this.rightArmPose == ArmPose.BOW_AND_ARROW) {
            this.rightArm.yRot = -0.1F + this.head.yRot * 0.5F;
            this.leftArm.yRot = 0.1F + this.head.yRot * 0.5F + 0.4F;
            this.rightArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot + 0.2F;
            this.leftArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot + 0.5F - (this.head.yRot > 0 ? 0 : this.head.yRot / 2);
        }
        if (this.leftArmPose == ArmPose.BOW_AND_ARROW) {
            this.rightArm.yRot = -0.1F + this.head.yRot * 0.5F - 0.4F;
            this.leftArm.yRot = 0.1F + this.head.yRot * 0.5F;
            this.rightArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot + 0.5F - (this.head.yRot > 0 ? 0 : this.head.yRot / 2);
            this.leftArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot + 0.2F;
        }

        //sleeves and pants
        this.leftPants.copyFrom(this.leftLeg);
        this.rightPants.copyFrom(this.rightLeg);
        this.leftSleeve.copyFrom(this.leftArm);
        this.rightSleeve.copyFrom(this.rightArm);
        this.jacket.copyFrom(this.body);
    }

    public boolean isAggressive(T p_104155_) {
        return p_104155_.isAggressive();
    }

    public void setArmPose(T zombie) {
        ItemStack item = zombie.getMainHandItem();
        boolean mainArmRight = zombie.getMainArm() == HumanoidArm.RIGHT;
        boolean blocking = item.getItem() instanceof ShieldItem && zombie.isUsingItem();
        if (blocking) {
            this.rightArmPose = mainArmRight ? ArmPose.BLOCK : ArmPose.EMPTY;
            this.leftArmPose = ! mainArmRight ? ArmPose.BLOCK : ArmPose.EMPTY;
        } else if (item.getUseAnimation() == UseAnim.BOW) {
            this.rightArmPose = mainArmRight ? ArmPose.BOW_AND_ARROW : ArmPose.EMPTY;
            this.leftArmPose = ! mainArmRight ? ArmPose.BOW_AND_ARROW : ArmPose.EMPTY;
        } else {
            this.rightArmPose = ArmPose.EMPTY;
            this.leftArmPose = ArmPose.EMPTY;
        }
    }
}
