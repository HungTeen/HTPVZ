package com.hungteen.pvz.client.model.plants;

import com.hungteen.pvz.client.model.plants.animation.KernelPultModelAnimation;
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
    private final ModelPart butter;
    private final ModelPart kernel;

    public KernelPultModel(ModelPart root) {
        this.total = root.getChild("total");
        this.pult = total.getChild("head").getChild("pult");
        ModelPart basket = pult.getChild("cube_r29").getChild("cube_r28").getChild("cube_r27").getChild("basket");
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

        PartDefinition pult = head.addOrReplaceChild("pult", CubeListBuilder.create(), PartPose.offset(0.0F, -11.0F, 0.0F));

        PartDefinition cube_r29 = pult.addOrReplaceChild("cube_r29", CubeListBuilder.create().texOffs(0, 38).addBox(-1.0F, -4.05F, -1.0F, 2.0F, 5.0F, 2.0F, new CubeDeformation(-0.4F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.5236F, 0.0F, 0.0F));

        PartDefinition cube_r28 = cube_r29.addOrReplaceChild("cube_r28", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -5.5F, -0.5F, 2.0F, 6.0F, 2.0F, new CubeDeformation(-0.4F)), PartPose.offsetAndRotation(0.0F, -3.8F, -0.5F, -1.3963F, 0.0F, 0.0F));

        PartDefinition cube_r27 = cube_r28.addOrReplaceChild("cube_r27", CubeListBuilder.create().texOffs(0, 18).addBox(-1.0F, -4.5F, -1.5F, 2.0F, 5.0F, 2.0F, new CubeDeformation(-0.4F)), PartPose.offsetAndRotation(0.0F, -5.25F, 1.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition basket = cube_r27.addOrReplaceChild("basket", CubeListBuilder.create().texOffs(27, 0).addBox(-2.5F, -1.25F, 0.5F, 5.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
                .texOffs(34, 36).addBox(-1.5F, -1.25F, 1.5F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.7484F, -0.5825F, 1.5708F, 0.0F, 0.0F));

        PartDefinition butter = basket.addOrReplaceChild("butter", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 3.0F));

        PartDefinition before_land = butter.addOrReplaceChild("before_land", CubeListBuilder.create().texOffs(0, 52).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));

        PartDefinition land = butter.addOrReplaceChild("land", CubeListBuilder.create().texOffs(40, 50).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 8.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition kernel = basket.addOrReplaceChild("kernel", CubeListBuilder.create().texOffs(32, 60).addBox(-1.0F, -2.0F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.25F, 3.0F));

        PartDefinition leaves = total.addOrReplaceChild("leaves", CubeListBuilder.create().texOffs(0, 18).addBox(-4.5F, -0.875F, -4.5F, 9.0F, 1.0F, 9.0F, new CubeDeformation(0.2F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition ne = leaves.addOrReplaceChild("ne", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

        PartDefinition leave1 = ne.addOrReplaceChild("leave1", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0436F, 0.0F, 0.0F));

        PartDefinition cube_r5_r1 = leave1.addOrReplaceChild("cube_r5_r1", CubeListBuilder.create().texOffs(33, 25).addBox(0.0F, 0.001F, -3.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.8933F, -11.5646F, -0.3491F, 0.0F, 0.0F));

        PartDefinition cube_r4_r1 = leave1.addOrReplaceChild("cube_r4_r1", CubeListBuilder.create().texOffs(34, 32).addBox(0.0F, 0.001F, 0.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.8933F, -11.5646F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r3_r1 = leave1.addOrReplaceChild("cube_r3_r1", CubeListBuilder.create().texOffs(27, 18).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5043F, -5.5708F, -0.5236F, 0.0F, 0.0F));

        PartDefinition nw = leaves.addOrReplaceChild("nw", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

        PartDefinition leave2 = nw.addOrReplaceChild("leave2", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0436F, 0.0F, 0.0F));

        PartDefinition cube_r6_r1 = leave2.addOrReplaceChild("cube_r6_r1", CubeListBuilder.create().texOffs(33, 25).addBox(0.0F, 0.001F, -3.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.8933F, -11.5646F, -0.3491F, 0.0F, 0.0F));

        PartDefinition cube_r5_r2 = leave2.addOrReplaceChild("cube_r5_r2", CubeListBuilder.create().texOffs(34, 32).addBox(0.0F, 0.001F, 0.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.8933F, -11.5646F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r4_r2 = leave2.addOrReplaceChild("cube_r4_r2", CubeListBuilder.create().texOffs(27, 18).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5043F, -5.5708F, -0.5236F, 0.0F, 0.0F));

        PartDefinition sw = leaves.addOrReplaceChild("sw", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -2.3562F, 0.0F));

        PartDefinition leave3 = sw.addOrReplaceChild("leave3", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0436F, 0.0F, 0.0F));

        PartDefinition cube_r7_r1 = leave3.addOrReplaceChild("cube_r7_r1", CubeListBuilder.create().texOffs(33, 25).addBox(0.0F, 0.001F, -3.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.8933F, -11.5646F, -0.3491F, 0.0F, 0.0F));

        PartDefinition cube_r6_r2 = leave3.addOrReplaceChild("cube_r6_r2", CubeListBuilder.create().texOffs(34, 32).addBox(0.0F, 0.001F, 0.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.8933F, -11.5646F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r5_r3 = leave3.addOrReplaceChild("cube_r5_r3", CubeListBuilder.create().texOffs(27, 18).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5043F, -5.5708F, -0.5236F, 0.0F, 0.0F));

        PartDefinition se = leaves.addOrReplaceChild("se", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 2.3562F, 0.0F));

        PartDefinition leave4 = se.addOrReplaceChild("leave4", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, 0.0F, 1.0F, 0.0436F, 0.0F, 0.0F));

        PartDefinition cube_r8_r1 = leave4.addOrReplaceChild("cube_r8_r1", CubeListBuilder.create().texOffs(33, 25).addBox(0.0F, 0.001F, -3.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.8933F, -11.5646F, -0.3491F, 0.0F, 0.0F));

        PartDefinition cube_r7_r2 = leave4.addOrReplaceChild("cube_r7_r2", CubeListBuilder.create().texOffs(34, 32).addBox(0.0F, 0.001F, 0.0F, 6.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -0.8933F, -11.5646F, 0.5672F, 0.0F, 0.0F));

        PartDefinition cube_r6_r3 = leave4.addOrReplaceChild("cube_r6_r3", CubeListBuilder.create().texOffs(27, 18).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 1.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5043F, -5.5708F, -0.5236F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    @Override
    public void setupAnim(T kernelPult, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.total.getAllParts().forEach(ModelPart::resetPose);
        float f = ageInTicks - (float) kernelPult.tickCount;
        this.animate(kernelPult.idleAnimationState, KernelPultModelAnimation.idle, ageInTicks);
        this.animate(kernelPult.shootAnimationState, KernelPultModelAnimation.shoot, ageInTicks);
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