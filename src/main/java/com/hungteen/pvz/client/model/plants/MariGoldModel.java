package com.hungteen.pvz.client.model.plants;

import com.hungteen.pvz.client.model.plants.animation.MarigoldAnimation;
import com.hungteen.pvz.common.entity.plants.MariGold;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class MariGoldModel<T extends MariGold> extends HierarchicalModel<T> {
    private final ModelPart total;
    private final ModelPart nerd;
    private final ModelPart l_brow;
    private final ModelPart r_brow;

    public MariGoldModel(ModelPart root) {
        this.total = root.getChild("total");
        ModelPart head = total.getChild("body").getChild("head");
        this.nerd = head.getChild("nerd");
        this.l_brow = head.getChild("eye_brow").getChild("l_brow");
        this.r_brow = head.getChild("eye_brow").getChild("r_brow");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(8.0F, 24.0F, -8.0F));

        PartDefinition leaves = total.addOrReplaceChild("leaves", CubeListBuilder.create(), PartPose.offset(-8.0F, 1.0F, 8.0F));

        PartDefinition s_r1 = leaves.addOrReplaceChild("s_r1", CubeListBuilder.create().texOffs(17, 17).addBox(-2.0F, -3.0F, 0.25F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.48F, 0.7854F, 0.0F));

        PartDefinition e_r1 = leaves.addOrReplaceChild("e_r1", CubeListBuilder.create().texOffs(22, 0).addBox(-6.0F, -3.0F, -2.5F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, -0.4326F, 0.678F, -0.6346F));

        PartDefinition w_r1 = leaves.addOrReplaceChild("w_r1", CubeListBuilder.create().texOffs(22, 7).addBox(1.0F, -3.0F, -2.5F, 5.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.4326F, 0.678F, 0.6346F));

        PartDefinition n_r1 = leaves.addOrReplaceChild("n_r1", CubeListBuilder.create().texOffs(0, 22).addBox(-2.0F, -3.0F, -6.25F, 4.0F, 3.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 0.0F, 0.48F, 0.7854F, 0.0F));

        PartDefinition body = total.addOrReplaceChild("body", CubeListBuilder.create().texOffs(26, 25).addBox(-1.0F, -12.5F, 0.5F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, 0.0F, 7.0F));

        PartDefinition stickd_r1 = body.addOrReplaceChild("stickd_r1", CubeListBuilder.create().texOffs(8, 30).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.2618F, 0.0F, 0.0F));

        PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 12).addBox(-4.5F, -4.0F, -2.0F, 9.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 1.0F));

        PartDefinition cube_r1 = head.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 43).addBox(1.5F, 1.2F, 0.995F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
                .texOffs(0, 43).addBox(-2.2F, -2.5F, 0.995F, 1.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 3.25F, -3.0F, 0.0F, 0.0F, -0.7854F));

        PartDefinition face_produce = head.addOrReplaceChild("face_produce", CubeListBuilder.create().texOffs(0, 1).addBox(-4.5F, -3.0F, -0.5F, 9.0F, 8.0F, 2.0F, new CubeDeformation(-0.005F)), PartPose.offset(0.0F, -1.0F, -1.5F));

        PartDefinition petals = head.addOrReplaceChild("petals", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, -1.5F));

        PartDefinition cube_r2 = petals.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(30, 14).addBox(-2.0F, -7.5F, 0.5F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -2.7489F));

        PartDefinition cube_r3 = petals.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(30, 14).addBox(-2.0F, -7.5F, 0.5F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, 2.7489F));

        PartDefinition cube_r4 = petals.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(30, 14).addBox(-2.0F, -7.5F, 0.5F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, -1.9635F));

        PartDefinition cube_r5 = petals.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(30, 14).addBox(-2.0F, -7.5F, 0.5F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.9635F));

        PartDefinition cube_r6 = petals.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(30, 14).addBox(-2.0F, -7.5F, 0.5F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -1.1781F));

        PartDefinition cube_r7 = petals.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(30, 14).addBox(-2.0F, -7.5F, 0.5F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 1.1781F));

        PartDefinition cube_r8 = petals.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(30, 14).addBox(-2.0F, -7.5F, 0.5F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3927F));

        PartDefinition cube_r9 = petals.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(30, 14).addBox(-2.0F, -7.5F, 0.5F, 4.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, -0.3927F));

        PartDefinition back = head.addOrReplaceChild("back", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition u_r1 = back.addOrReplaceChild("u_r1", CubeListBuilder.create().texOffs(14, 25).addBox(-2.0F, -0.5F, 0.0F, 4.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.5F, 0.0F, 0.6981F, 0.0F, 0.0F));

        PartDefinition l_r1 = back.addOrReplaceChild("l_r1", CubeListBuilder.create().texOffs(18, 25).addBox(0.0F, -2.0F, 0.0F, 0.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.0F, 0.0F, 0.0F, -0.6981F, 0.0F));

        PartDefinition r_r1 = back.addOrReplaceChild("r_r1", CubeListBuilder.create().texOffs(0, 26).addBox(0.0F, -2.0F, 0.0F, 0.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.0F, 0.0F, 0.0F, 0.6981F, 0.0F));

        PartDefinition eye_brow = head.addOrReplaceChild("eye_brow", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition l_brow = eye_brow.addOrReplaceChild("l_brow", CubeListBuilder.create(), PartPose.offset(2.5F, -3.0F, -2.2F));

        PartDefinition l_brow_r1 = l_brow.addOrReplaceChild("l_brow_r1", CubeListBuilder.create().texOffs(0, 39).addBox(-1.5F, -0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0436F));

        PartDefinition r_brow = eye_brow.addOrReplaceChild("r_brow", CubeListBuilder.create(), PartPose.offset(-2.5F, -3.0F, -2.2F));

        PartDefinition r_brow_r1 = r_brow.addOrReplaceChild("r_brow_r1", CubeListBuilder.create().texOffs(0, 38).addBox(-1.5F, -0.5F, 0.0F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.0436F));

        PartDefinition nerd = head.addOrReplaceChild("nerd", CubeListBuilder.create().texOffs(0, 45).addBox(-5.0F, -13.0F, -1.5F, 10.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, -1.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }

    @Override
    public void setupAnim(T marigold, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.total.getAllParts().forEach(ModelPart::resetPose);
        boolean nerd = marigold.hasCustomName() && marigold.getCustomName().getString().equals("nerd");
        this.nerd.visible = nerd;
        this.l_brow.zRot = nerd ? (float) (1 / Math.PI) : 0;
        this.r_brow.zRot = nerd ? (float) (- 1 / Math.PI) : 0;
        this.animate(marigold.idleAnimationState, MarigoldAnimation.idle, ageInTicks);
        this.animate(marigold.produceAnimationState, MarigoldAnimation.produce, ageInTicks);

        //size
        int level = marigold.getGrowLevel();
        float scale = level >= 3 ? 1 : Math.min(1, (float) ((level + 1) * 0.3 + 0.2));
        total.xScale *= scale;
        total.x -= (1 - scale) * 8;
        total.yScale *= scale;
        total.zScale *= scale;
        total.z += (1 - scale) * 8;
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