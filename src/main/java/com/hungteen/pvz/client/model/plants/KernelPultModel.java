package com.hungteen.pvz.client.model.plants;

import com.hungteen.pvz.client.model.plants.animation.CabbagePultAnimation;
import com.hungteen.pvz.common.entity.plants.KernelPult;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class KernelPultModel<T extends KernelPult> extends HierarchicalModel<T> {
    private final ModelPart total;
    private final ModelPart pult;
    private final ModelPart basket;
    private final ModelPart butter;
    private final ModelPart kernel;

    public KernelPultModel(ModelPart root) {
        this.total = root.getChild("total");
        this.pult = total.getChild("pult");
        this.basket = pult.getChild("basket");
        this.butter = basket.getChild("butter");
        this.kernel = basket.getChild("kernel");

    }


    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 23.8F, 0.0F));

        PartDefinition head = total.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -8.0F, -4.5F, 9.0F, 9.0F, 9.0F, new CubeDeformation(0.0F))
                .texOffs(27, 24).addBox(-4.5F, -8.0071F, -4.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.2F))
                .texOffs(27, 24).mirror().addBox(1.5F, -8.0071F, -4.5F, 3.0F, 2.0F, 1.0F, new CubeDeformation(0.2F)).mirror(false)
                .texOffs(0, 28).addBox(-3.5F, -11.0F, -3.5F, 7.0F, 3.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition leaves = total.addOrReplaceChild("leaves", CubeListBuilder.create().texOffs(0, 18).addBox(-4.5F, -0.875F, -4.5F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition bone = leaves.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition leave1 = bone.addOrReplaceChild("leave1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0436F, 0.0F, 0.0F));

        PartDefinition cube_r5_r1 = leave1.addOrReplaceChild("cube_r5_r1", CubeListBuilder.create().texOffs(33, 25).addBox(0.0F, 0.001F, -3.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.8933F, -11.5646F, -0.3491F, 0.0F, 0.0F));

        PartDefinition cube_r4_r1 = leave1.addOrReplaceChild("cube_r4_r1", CubeListBuilder.create().texOffs(34, 32).addBox(0.0F, 0.001F, 0.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.8933F, -11.5646F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r3_r1 = leave1.addOrReplaceChild("cube_r3_r1", CubeListBuilder.create().texOffs(27, 18).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5043F, -5.5708F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bone2 = leaves.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition leave2 = bone2.addOrReplaceChild("leave2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0436F, 0.0F, 0.0F));

        PartDefinition cube_r6_r1 = leave2.addOrReplaceChild("cube_r6_r1", CubeListBuilder.create().texOffs(33, 25).addBox(0.0F, 0.001F, -3.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.8933F, -11.5646F, -0.3491F, 0.0F, 0.0F));

        PartDefinition cube_r5_r2 = leave2.addOrReplaceChild("cube_r5_r2", CubeListBuilder.create().texOffs(34, 32).addBox(0.0F, 0.001F, 0.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.8933F, -11.5646F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r4_r2 = leave2.addOrReplaceChild("cube_r4_r2", CubeListBuilder.create().texOffs(27, 18).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5043F, -5.5708F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bone3 = leaves.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.3562F, 0.0F));

        PartDefinition leave3 = bone3.addOrReplaceChild("leave3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0436F, 0.0F, 0.0F));

        PartDefinition cube_r7_r1 = leave3.addOrReplaceChild("cube_r7_r1", CubeListBuilder.create().texOffs(33, 25).addBox(0.0F, 0.001F, -3.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.8933F, -11.5646F, -0.3491F, 0.0F, 0.0F));

        PartDefinition cube_r6_r2 = leave3.addOrReplaceChild("cube_r6_r2", CubeListBuilder.create().texOffs(34, 32).addBox(0.0F, 0.001F, 0.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.8933F, -11.5646F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r5_r3 = leave3.addOrReplaceChild("cube_r5_r3", CubeListBuilder.create().texOffs(27, 18).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5043F, -5.5708F, -0.5236F, 0.0F, 0.0F));

        PartDefinition bone4 = leaves.addOrReplaceChild("bone4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.3562F, 0.0F));

        PartDefinition leave4 = bone4.addOrReplaceChild("leave4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0436F, 0.0F, 0.0F));

        PartDefinition cube_r8_r1 = leave4.addOrReplaceChild("cube_r8_r1", CubeListBuilder.create().texOffs(33, 25).addBox(0.0F, 0.001F, -3.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.8933F, -11.5646F, -0.3491F, 0.0F, 0.0F));

        PartDefinition cube_r7_r2 = leave4.addOrReplaceChild("cube_r7_r2", CubeListBuilder.create().texOffs(34, 32).addBox(0.0F, 0.001F, 0.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.8933F, -11.5646F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r6_r3 = leave4.addOrReplaceChild("cube_r6_r3", CubeListBuilder.create().texOffs(27, 18).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5043F, -5.5708F, -0.5236F, 0.0F, 0.0F));

        PartDefinition pult = total.addOrReplaceChild("pult", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, 0.0F));

        PartDefinition cube_r27 = pult.addOrReplaceChild("cube_r27", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -1.6192F, 9.0169F, -1.6144F, 0.0F, 0.0F));

        PartDefinition cube_r27_r1 = cube_r27.addOrReplaceChild("cube_r27_r1", CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, -0.5F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(-0.4F)), PartPose.offsetAndRotation(0.0F, -0.3846F, 0.6958F, 0.1309F, 0.0F, 0.0F));

        PartDefinition cube_r28 = pult.addOrReplaceChild("cube_r28", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -3.8077F, 1.2387F, -1.8326F, 0.0F, 0.0F));

        PartDefinition cube_r28_r1 = cube_r28.addOrReplaceChild("cube_r28_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.5F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.4F)), PartPose.offsetAndRotation(0.0F, -4.0142F, 1.8391F, -0.2182F, 0.0F, 0.0F));

        PartDefinition cube_r29 = pult.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(0, 38).addBox(-1.0F, -4.0F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(-0.4F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition basket = pult.addOrReplaceChild("basket", CubeListBuilder.create().texOffs(27, 0).addBox(-2.5F, -2.0F, 0.0F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(34, 36).addBox(-1.5F, -2.0F, 1.0F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 9.0F, 0.1745F, 0.0F, 0.0F));

        PartDefinition butter = basket.addOrReplaceChild("butter", CubeListBuilder.create().texOffs(21, 28).addBox(-3.0F, -4.0F, 0.5F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.2F))
                .texOffs(24, 35).addBox(-4.0F, -2.25F, 0.5F, 1.0F, 5.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 0.0F, 0.0F));

        PartDefinition kernel = basket.addOrReplaceChild("kernel", CubeListBuilder.create().texOffs(36, 8).addBox(-2.5F, -4.5F, 1.0F, 3.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    @Override
    public void setupAnim(T kernelPult, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.total.getAllParts().forEach(ModelPart::resetPose);
        float f = ageInTicks - (float) kernelPult.tickCount;
        this.animate(kernelPult.idleAnimationState, CabbagePultAnimation.idle, ageInTicks);
        this.animate(kernelPult.shootAnimationState, CabbagePultAnimation.shoot, ageInTicks);
        this.kernel.visible = kernelPult.getCurrentBullet() == KernelPult.CornTypes.KERNEL;
        this.butter.visible = kernelPult.getCurrentBullet() == KernelPult.CornTypes.BUTTER;

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