package com.hungteen.pvz.client.model.plants;

import com.hungteen.pvz.client.model.plants.animation.SunFlowerAnimation;
import com.hungteen.pvz.common.entity.plants.SunFlower;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class SunFlowerModel<T extends SunFlower> extends HierarchicalModel<T> {
    // This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
    private final ModelPart total;
    public SunFlowerModel(ModelPart root) {
        this.total = root.getChild("total");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(8.0F, 24.0F, -8.0F));

        PartDefinition leaves = total.addOrReplaceChild("leaves", CubeListBuilder.create(), PartPose.offset(-8.0F, 1.0F, 8.0F));

        PartDefinition w_r1 = leaves.addOrReplaceChild("w_r1", CubeListBuilder.create().texOffs(0, 24).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.1745F, 0.0F, 0.0F));

        PartDefinition w_r2 = leaves.addOrReplaceChild("w_r2", CubeListBuilder.create().texOffs(24, 7).addBox(-2.0F, -0.5F, -7.0F, 4.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, -0.1745F, 0.0F, 0.0F));

        PartDefinition e_r1 = leaves.addOrReplaceChild("e_r1", CubeListBuilder.create().texOffs(26, 14).addBox(1.0F, -0.5F, -2.0F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, -1.0F, 0.0F, 0.0F, -0.1745F));

        PartDefinition e_r2 = leaves.addOrReplaceChild("e_r2", CubeListBuilder.create().texOffs(30, 0).addBox(-7.0F, -0.5F, -2.0F, 6.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, -1.0F, 0.0F, 0.0F, 0.1745F));

        PartDefinition body = total.addOrReplaceChild("body", CubeListBuilder.create().texOffs(26, 30).addBox(-1.0F, -14.0F, 1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, 0.0F, 7.0F));

        PartDefinition stickd_r1 = body.addOrReplaceChild("stickd_r1", CubeListBuilder.create().texOffs(18, 30).addBox(-1.0F, -8.0F, -1.0F, 2.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(22, 20).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 13).addBox(-5.0F, -5.0F, -3.0F, 10.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -13.0F, 1.0F));

        PartDefinition face_produce = head.addOrReplaceChild("face_produce", CubeListBuilder.create().texOffs(0, 40).addBox(-5.0F, -4.0F, -1.5F, 10.0F, 8.0F, 3.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, -1.0F, -1.5F));

        PartDefinition petals = head.addOrReplaceChild("petals", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -6.0F, -0.5F, 14.0F, 12.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, -1.5F));

        PartDefinition right_hand = body.addOrReplaceChild("right_hand", CubeListBuilder.create(), PartPose.offset(-0.5F, -3.5F, 1.0F));

        PartDefinition leafw_r1 = right_hand.addOrReplaceChild("leafw_r1", CubeListBuilder.create().texOffs(8, 32).addBox(-3.5F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.0F, 0.1745F));

        PartDefinition left_hand = body.addOrReplaceChild("left_hand", CubeListBuilder.create(), PartPose.offset(0.5F, -4.5F, 1.0F));

        PartDefinition leafe_r1 = left_hand.addOrReplaceChild("leafe_r1", CubeListBuilder.create().texOffs(0, 31).addBox(0.5F, -0.5F, -1.0F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.0F, -0.1745F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T sunFlower, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.total.getAllParts().forEach(ModelPart::resetPose);
        float f = ageInTicks - (float)sunFlower.tickCount;
        this.animate(sunFlower.idleAnimationState, SunFlowerAnimation.idle, ageInTicks);
        this.animate(sunFlower.produceAnimationState, SunFlowerAnimation.produce, ageInTicks);
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