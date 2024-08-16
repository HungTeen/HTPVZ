package com.hungteen.pvz.client.model;// Made with Blockbench 4.1.5
// Exported for Minecraft version 1.17 with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.common.entity.creatures.GrassCarp;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class GrassCarpModel<T extends GrassCarp> extends EntityModel<T> {

	private final ModelPart total;
	private final ModelPart hair;
//	private final ModelPart light;
	private final ModelPart hairb;
	private final ModelPart left;
	private final ModelPart right;
	private final ModelPart tail;

	public GrassCarpModel(ModelPart root) {
		this.total = root.getChild("total");
		this.hair = this.total.getChild("leaf");
//		this.light = this.total.getChild("head").getChild("light");
		this.hairb = this.hair.getChild("leafb");
		this.left = this.total.getChild("left");
		this.right = this.total.getChild("right");
		this.tail = this.total.getChild("tail");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition head = total.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 10).addBox(0.0F, 0.0F, 0.0F, 0.0F, 3.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(24, 4).addBox(-1.0F, 1.5F, 4.0F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

//		PartDefinition light = head.addOrReplaceChild("light", CubeListBuilder.create().texOffs(0, 0).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
//				.texOffs(8, 20).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(-6.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tails_r1 = head.addOrReplaceChild("tails_r1", CubeListBuilder.create().texOffs(-2, 0).addBox(0.0F, 0.0F, -1.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.3491F));

		PartDefinition tailn_r1 = head.addOrReplaceChild("tailn_r1", CubeListBuilder.create().texOffs(-2, 0).addBox(-2.0F, 0.0F, -1.0F, 2.0F, 0.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.3491F));

		PartDefinition leaf = total.addOrReplaceChild("leaf", CubeListBuilder.create().texOffs(20, 6).addBox(0.0F, -2.0F, -6.0F, 0.0F, 2.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 3.0F));

		PartDefinition leafb = leaf.addOrReplaceChild("leafb", CubeListBuilder.create().texOffs(0, 9).addBox(0.0F, -2.0F, 0.0F, 0.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition tail = total.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(24, 0).addBox(-1.5F, -1.5F, -0.7F, 3.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 2.5F, 5.0F));

		PartDefinition right = total.addOrReplaceChild("right", CubeListBuilder.create(), PartPose.offset(-3.0F, -3.0F, 0.0F));

		PartDefinition right_r1 = right.addOrReplaceChild("right_r1", CubeListBuilder.create().texOffs(18, 0).addBox(-2.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, -0.5236F));

		PartDefinition left = total.addOrReplaceChild("left", CubeListBuilder.create(), PartPose.offset(3.0F, -3.0F, 0.0F));

		PartDefinition left_r1 = left.addOrReplaceChild("left_r1", CubeListBuilder.create().texOffs(18, 3).addBox(0.0F, 0.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.5236F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		//hair.
		this.hair.visible = ! entity.isBald();
		//swing.
		final float animTick = ageInTicks / 3;
		this.left.zRot = (float) Math.sin(animTick);
		this.right.zRot = - (float) Math.sin(animTick);
		this.hairb.yRot = (float) Math.sin(animTick) / 3;
		this.tail.zRot = animTick % 360;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		total.render(poseStack, buffer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}