package com.hungteen.pvz.client.model.zombie;

import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class SnorkelZombieModel<T extends PVZZombie> extends PVZZombieModel<T> {
    public final ModelPart leftFlipper;
    public final ModelPart rightFlipper;
    public SnorkelZombieModel(ModelPart root) {
        super(root, true);
        leftFlipper = root.getChild("left_pants").getChild("left_flipper");
        rightFlipper = root.getChild("right_pants").getChild("right_flipper");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition definition = PlayerModel.createMesh(CubeDeformation.NONE, true);
        //pole
        PartDefinition root = definition.getRoot();

        PartDefinition rightFlipper = root.getChild("right_pants").addOrReplaceChild("right_flipper",
                CubeListBuilder.create().texOffs(44, 20).addBox(-2.5F, 0.0F, -10.0F, 5.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, -2.0F));
        PartDefinition leftFlipper = root.getChild("left_pants").addOrReplaceChild("left_flipper",
                CubeListBuilder.create().texOffs(44, 20).addBox(-2.5F, 0.0F, -10.0F, 5.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, -2.0F));
        PartDefinition bone2 = root.getChild("hat").addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(24, 0).addBox(3.0F, -1.0F, -1.0F, 4.0F, 3.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(52, 17).addBox(-0.5F, -0.5F, -0.75F, 5.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(60, 42).addBox(-1.0F, -8.5F, -0.75F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.0F))
                .texOffs(60, 53).addBox(-1.0F, -8.5F, -0.75F, 1.0F, 10.0F, 1.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(-5.0F, -1.0F, -5.0F, -0.0873F, 0.3491F, 0.1745F));
        PartDefinition bone = bone2.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, -8.0F, 1.25F));
        PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.5F, -4.0F, -0.5F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -2.0F, -1.5F, -0.3927F, 0.0F, 0.0F));
        //left sleeve texture position adjusted to make room for snorkel.
        PartDefinition left_sleeve = root.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(46, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.5F, 0.0F));
        return LayerDefinition.create(definition, 64, 64);
    }

    @Override
    public void setupAnim(T zombie, float p_102002_, float p_102003_, float p_102004_, float p_102005_, float p_102006_) {
        super.setupAnim(zombie, p_102002_, p_102003_, p_102004_, p_102005_, p_102006_);
        if (this.swimAmount > 0.0F) {
            this.leftFlipper.xRot = 0.5F + this.swimAmount * 0.55F * Mth.cos(0.1F * p_102004_);
            this.rightFlipper.xRot = 0.5F - this.swimAmount * 0.55F * Mth.cos(0.1F * p_102004_);
        } else {
            this.leftFlipper.xRot = Math.min(0, - this.leftLeg.xRot);
            this.rightFlipper.xRot = Math.min(0, - this.rightLeg.xRot);
        }
    }

    @Override
    public void setArmPose(T zombie) {
        super.setArmPose(zombie);

    }
}
