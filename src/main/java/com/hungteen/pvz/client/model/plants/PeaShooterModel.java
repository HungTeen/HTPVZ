package com.hungteen.pvz.client.model.plants;

import com.hungteen.pvz.client.model.plants.animation.PeaShooterAnimation;
import com.hungteen.pvz.common.entity.plants.PeaShooter;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class PeaShooterModel<T extends PeaShooter> extends HierarchicalModel<T> {
    private final ModelPart total;

    public PeaShooterModel(ModelPart root) {
        this.total = root.getChild("total");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = total.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 31).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(40, 8).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

        PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(44, 0).addBox(-3.0F, -3.0F, -4.25F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
                .texOffs(30, 0).addBox(-2.0F, -2.0F, -3.25F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, -4.75F));

        PartDefinition mouth_closed = mouth.addOrReplaceChild("mouth_closed", CubeListBuilder.create().texOffs(0, 56).addBox(-3.0F, -3.0F, -1.0F, 6.0F, 6.0F, 2.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, 0.0F, -3.25F));

        PartDefinition eyes_closed = head.addOrReplaceChild("eyes_closed", CubeListBuilder.create().texOffs(0, 35).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, -5.0F, 0.0F));

        PartDefinition hair = head.addOrReplaceChild("hair", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -0.4429F, 0.0825F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F))
                .texOffs(42, 19).addBox(-1.0F, -0.0429F, 2.0825F, 4.0F, 6.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -5.5F, 5.0F, 0.1745F, 0.0F, 0.0F));

        PartDefinition absorb_pea = body.addOrReplaceChild("absorb_pea", CubeListBuilder.create().texOffs(31, 36).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-1.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition down = total.addOrReplaceChild("down", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition n_r1 = down.addOrReplaceChild("n_r1", CubeListBuilder.create().texOffs(4, 20).addBox(-2.0F, -1.0F, -7.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.8F, 0.0F, -0.1745F, -0.7854F, 0.0F));

        PartDefinition w_r1 = down.addOrReplaceChild("w_r1", CubeListBuilder.create().texOffs(6, 27).addBox(1.0F, -1.0F, -2.0F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.8F, 0.0F, 0.1745F, -0.7854F, -0.1745F));

        PartDefinition e_r1 = down.addOrReplaceChild("e_r1", CubeListBuilder.create().texOffs(26, 27).addBox(-7.0F, -1.0F, -2.0F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.8F, 0.0F, -0.1745F, -0.7854F, 0.1745F));

        PartDefinition s_r1 = down.addOrReplaceChild("s_r1", CubeListBuilder.create().texOffs(24, 20).addBox(-2.0F, -1.0F, 1.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.8F, 0.0F, 0.1745F, -0.7854F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    @Override
    public void setupAnim(T peaShooter, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.total.getAllParts().forEach(ModelPart::resetPose);
        float f = ageInTicks - (float) peaShooter.tickCount;
        this.animate(peaShooter.idleAnimationState, PeaShooterAnimation.idle, ageInTicks);
        this.animate(peaShooter.shootAnimationState, PeaShooterAnimation.shoot, ageInTicks);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        total.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return total;
    }
}
