package com.hungteen.pvz.client.model;

// Made with Blockbench 5.0.7
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.common.entity.creatures.Snail;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.HumanoidArm;

public class SnailModel<T extends Snail> extends HierarchicalModel<T> implements ArmedModel {
    private final ModelPart total;
    private final ModelPart shell;
    private final ModelPart nut;
    private final ModelPart mushroom;
    private final ModelPart common;
    private final ModelPart tail;
    private final ModelPart neck;
    private final ModelPart head;
    private final ModelPart right_eye;
    private final ModelPart left_eye;
    public final Type type;

    public SnailModel(ModelPart root, Type type) {
        this.total = root.getChild("total");
        this.shell = this.total.getChild("shell");
        this.nut = this.shell.getChild("nut");
        this.mushroom = this.nut.getChild("mushroom");
        this.common = this.shell.getChild("common");
        this.tail = this.total.getChild("tail");
        this.neck = this.total.getChild("neck");
        this.head = this.neck.getChild("head");
        this.right_eye = this.head.getChild("right_eye");
        this.left_eye = this.head.getChild("left_eye");
        this.type = type;
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition shell = total.addOrReplaceChild("shell", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));

        PartDefinition nut = shell.addOrReplaceChild("nut", CubeListBuilder.create().texOffs(0, 23).addBox(-5.0F, -14.0F, -5.0F, 10.0F, 14.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(0, 0).addBox(-6.0F, -12.0F, -6.0F, 12.0F, 11.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

        PartDefinition mushroom = nut.addOrReplaceChild("mushroom", CubeListBuilder.create().texOffs(36, -8).addBox(2.0F, -19.0F, 3.0F, 0.0F, 7.0F, 8.0F, new CubeDeformation(0.0F))
                .texOffs(30, 22).addBox(7.0F, -6.0F, -1.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(48, 10).addBox(-9.0F, -15.0F, -6.0F, 0.0F, 7.0F, 6.0F, new CubeDeformation(0.0F))
                .texOffs(48, 9).addBox(-12.0F, -15.0F, -3.0F, 6.0F, 7.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(30, 23).addBox(6.0F, -6.0F, 2.0F, 6.0F, 5.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(52, 0).addBox(-1.0F, -19.0F, 7.0F, 6.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition common = shell.addOrReplaceChild("common", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -12.0F, -6.0F, 8.0F, 13.0F, 13.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition tail = total.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(24, 46).addBox(-2.0F, -2.0F, -7.0F, 4.0F, 2.0F, 16.0F, new CubeDeformation(-0.001F)), PartPose.offset(0.0F, 0.0F, 1.0F));

        PartDefinition neck = total.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(0, 47).addBox(-2.0F, -8.0F, -3.0F, 4.0F, 9.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, -6.0F));

        PartDefinition head = neck.addOrReplaceChild("head", CubeListBuilder.create().texOffs(14, 47).addBox(-2.0F, -2.0F, -4.0F, 4.0F, 2.0F, 5.0F, new CubeDeformation(0.001F)), PartPose.offset(0.0F, -8.0F, -1.0F));

        PartDefinition right_eye = head.addOrReplaceChild("right_eye", CubeListBuilder.create().texOffs(15, 56).addBox(-0.5F, -3.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -2.0F, -1.0F));

        PartDefinition cube_r1 = right_eye.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(15, 55).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

        PartDefinition left_eye = head.addOrReplaceChild("left_eye", CubeListBuilder.create().texOffs(15, 56).addBox(1.5F, -3.0F, 0.0F, 1.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, -2.0F, -1.0F));

        PartDefinition cube_r2 = left_eye.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(15, 55).addBox(-0.5F, -1.0F, 0.0F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, -3.0F, 0.0F, 0.5236F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(Snail snail, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.total.getAllParts().forEach(ModelPart::resetPose);
        switch (this.type) {
            case snail -> this.nut.visible = false;
            default -> this.common.visible = false;
        }
        this.animate(snail.commonAnimationState, SnailModelAnimation.common, ageInTicks);
        this.animate(snail.inAnimationState, SnailModelAnimation.in, ageInTicks);
        this.animate(snail.outAnimationState, SnailModelAnimation.out, ageInTicks);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        total.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
        this.total.translateAndRotate(poseStack);
        this.neck.translateAndRotate(poseStack);
        this.head.translateAndRotate(poseStack);
        poseStack.translate(0, -0.6, -0.1);
    }

    @Override
    public ModelPart root() {
        return total;
    }

    public enum Type {
        snail, wall_nail, fungicocilidae
    }
}