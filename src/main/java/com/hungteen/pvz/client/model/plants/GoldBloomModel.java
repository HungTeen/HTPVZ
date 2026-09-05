package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.client.model.plants.animation.GoldBloomModelAnimation;
import com.hungteen.pvz.common.entity.plants.GoldBloom;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class GoldBloomModel<T extends GoldBloom> extends HierarchicalModel<T> {
	private final ModelPart total;

	public GoldBloomModel(ModelPart root) {
		this.total = root.getChild("total");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition head = total.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 14).addBox(-3.5F, -8.0F, -3.5F, 7.0F, 6.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition eyes = head.addOrReplaceChild("eyes", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition eyes_open = eyes.addOrReplaceChild("eyes_open", CubeListBuilder.create().texOffs(29, 36).addBox(-3.0F, -6.0F, -3.505F, 6.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition eyes_closed = eyes.addOrReplaceChild("eyes_closed", CubeListBuilder.create().texOffs(28, 5).addBox(-3.0F, -6.0F, -3.505F, 6.0F, 3.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ne = head.addOrReplaceChild("ne", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition cube_r1 = ne.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 32).addBox(-6.0F, 0.0F, -5.0F, 6.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, -0.0873F));

		PartDefinition se = head.addOrReplaceChild("se", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition cube_r2 = se.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 27).addBox(-6.0F, 0.0F, 0.0F, 6.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, -0.0873F));

		PartDefinition sw = head.addOrReplaceChild("sw", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition cube_r3 = sw.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(16, 0).addBox(0.0F, 0.0F, 0.0F, 6.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0873F));

		PartDefinition nw = head.addOrReplaceChild("nw", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition cube_r4 = nw.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(28, 0).addBox(0.0F, 0.0F, -5.0F, 6.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, 0.0873F));

		PartDefinition flowers = head.addOrReplaceChild("flowers", CubeListBuilder.create(), PartPose.offset(0.0F, -9.0F, 0.0F));

		PartDefinition flower1 = flowers.addOrReplaceChild("flower1", CubeListBuilder.create().texOffs(46, -5).addBox(0.0F, -4.0F, 0.0F, 0.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition close1 = flower1.addOrReplaceChild("close1", CubeListBuilder.create(), PartPose.offset(-0.5F, -3.5F, 3.5F));

		PartDefinition cube_r5 = close1.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -5.0F, -4.0F, 7.0F, 7.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 0.0F, -1.1999F, -0.4014F, 0.7069F));

		PartDefinition open1 = flower1.addOrReplaceChild("open1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r6 = open1.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(-10, 53).addBox(-5.0F, -1.0F, -5.0F, 10.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -4.0F, 4.0F, -0.829F, -0.5672F, 0.5672F));

		PartDefinition cube_r7 = open1.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(10, 41).addBox(-7.0F, 0.0F, -7.0F, 14.0F, 0.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -4.0F, 4.0F, -0.6545F, 0.0F, 0.0F));

		PartDefinition flower2 = flowers.addOrReplaceChild("flower2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r8 = flower2.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(17, 30).addBox(0.0F, -3.0F, -6.0F, 0.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.7854F, 0.0F));

		PartDefinition close2 = flower2.addOrReplaceChild("close2", CubeListBuilder.create(), PartPose.offset(-3.5F, -2.0F, -3.5F));

		PartDefinition cube_r9 = close2.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(23, 22).addBox(-3.5F, -2.0F, -3.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -3.0F, 0.0F, -0.6109F, -0.1745F, 0.2618F));

		PartDefinition open2 = flower2.addOrReplaceChild("open2", CubeListBuilder.create(), PartPose.offset(-3.0F, -2.0F, -4.0F));

		PartDefinition open2_r1 = open2.addOrReplaceChild("open2_r1", CubeListBuilder.create().texOffs(31, 57).addBox(-3.0F, 0.0F, -3.0F, 6.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -1.0F, -1.0F, 0.6981F, -0.1309F, -0.4363F));

		PartDefinition open2_r2 = open2.addOrReplaceChild("open2_r2", CubeListBuilder.create().texOffs(-10, 53).addBox(-5.0F, 1.0F, -5.0F, 10.0F, 0.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, -1.0F, -1.0F, 0.6981F, -0.1309F, -0.4363F));

		PartDefinition flower3 = flowers.addOrReplaceChild("flower3", CubeListBuilder.create(), PartPose.offset(3.0F, 0.0F, -5.0F));

		PartDefinition cube_r10 = flower3.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(29, 26).addBox(0.0F, -3.0F, -6.0F, 0.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, 0.0F, 5.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition close3 = flower3.addOrReplaceChild("close3", CubeListBuilder.create(), PartPose.offset(0.0F, -2.5F, 2.0F));

		PartDefinition cube_r11 = close3.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(22, 8).addBox(-3.0F, -2.5F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, -4.0F, -1.0F, -0.9599F, -0.6109F, 0.8727F));

		PartDefinition open3 = flower3.addOrReplaceChild("open3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r12 = open3.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(13, 55).addBox(-4.0F, -1.0F, -4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(-12, 41).addBox(-6.0F, 0.0F, -6.0F, 12.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.0F, 1.0F, 0.2618F, -0.6981F, 0.5236F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T goldBloom, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.total.getAllParts().forEach(ModelPart::resetPose);
		this.animate(goldBloom.explodeAnimationState, GoldBloomModelAnimation.explode, ageInTicks);
		this.animate(goldBloom.idleAnimationState, GoldBloomModelAnimation.idle, ageInTicks);
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