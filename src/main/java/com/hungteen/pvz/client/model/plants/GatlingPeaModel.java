package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.9.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.client.model.plants.animation.GatlingPeaModelAnimation;
import com.hungteen.pvz.common.entity.plants.GatlingPea;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class GatlingPeaModel<T extends GatlingPea> extends HierarchicalModel<T> {
	private final ModelPart total;

	public GatlingPeaModel(ModelPart root) {
		this.total = root.getChild("total");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body = total.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 43).addBox(-1.0F, -12.0F, -1.0F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -10.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(82, 4).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

		PartDefinition mouth = head.addOrReplaceChild("mouth", CubeListBuilder.create().texOffs(70, 0).addBox(-3.0F, -3.0F, -3.25F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(30, 0).addBox(-2.0F, -2.0F, -2.25F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, -4.75F));

		PartDefinition barrel = mouth.addOrReplaceChild("barrel", CubeListBuilder.create().texOffs(80, 11).addBox(-2.0F, -2.0F, -1.5F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -4.75F));

		PartDefinition eyes_closed = head.addOrReplaceChild("eyes_closed", CubeListBuilder.create().texOffs(40, 0).addBox(-5.0F, -22.0F, -4.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, 12.0F, -1.0F));

		PartDefinition eyebrow = head.addOrReplaceChild("eyebrow", CubeListBuilder.create().texOffs(30, 9).addBox(-5.0F, -21.25F, -4.6F, 10.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 12.0F, -1.0F));

		PartDefinition helmet = head.addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(0, 46).addBox(-5.5F, -6.0F, -4.5F, 11.0F, 7.0F, 11.0F, new CubeDeformation(0.0F))
		.texOffs(0, 26).addBox(-5.5F, -9.0F, -5.5F, 11.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition absorb_pea1 = body.addOrReplaceChild("absorb_pea1", CubeListBuilder.create().texOffs(94, 0).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-1.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition absorb_pea2 = body.addOrReplaceChild("absorb_pea2", CubeListBuilder.create().texOffs(94, 0).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-1.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition down = total.addOrReplaceChild("down", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition n_r1 = down.addOrReplaceChild("n_r1", CubeListBuilder.create().texOffs(-6, 28).addBox(-2.0F, -1.0F, -7.0F, 4.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.8F, 0.0F, -0.1745F, -0.7854F, 0.0F));

		PartDefinition w_r1 = down.addOrReplaceChild("w_r1", CubeListBuilder.create().texOffs(-4, 20).addBox(1.0F, -1.0F, -2.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.8F, 0.0F, 0.1745F, -0.7854F, -0.1745F));

		PartDefinition e_r1 = down.addOrReplaceChild("e_r1", CubeListBuilder.create().texOffs(-4, 24).addBox(-7.0F, -1.0F, -2.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.8F, 0.0F, -0.1745F, -0.7854F, 0.1745F));

		PartDefinition s_r1 = down.addOrReplaceChild("s_r1", CubeListBuilder.create().texOffs(-6, 4).addBox(-2.0F, -1.0F, 1.0F, 4.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.8F, 0.0F, 0.1745F, -0.7854F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T peaShooter, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.total.getAllParts().forEach(ModelPart::resetPose);
		this.animate(peaShooter.idleAnimationState, GatlingPeaModelAnimation.idle, ageInTicks);
		this.animate(peaShooter.shootAnimationState, GatlingPeaModelAnimation.shoot, ageInTicks);
		this.animate(peaShooter.controlledAnimationState, GatlingPeaModelAnimation.controlled_shoot, ageInTicks);
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