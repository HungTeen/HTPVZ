package com.hungteen.pvz.client.model.plants;

// Made with Blockbench 4.7.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class WallNutModel<T extends Entity> extends EntityModel<T> {
	private final ModelPart body;
	private final ModelPart in0;
	private final ModelPart in1;
	private final ModelPart in2;

	public WallNutModel(ModelPart root) {
		this.body = root.getChild("body");
		this.in0 = body.getChild("in0");
		this.in1 = body.getChild("in1");
		this.in2 = body.getChild("in2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -14.0F, -7.0F, 14.0F, 13.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(0, 27).addBox(-6.0F, -17.0F, -6.0F, 12.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(0, 42).addBox(-5.0F, -1.0F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(56, 0).addBox(-3.0F, -12.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition in2 = body.addOrReplaceChild("in2", CubeListBuilder.create().texOffs(0, 86).addBox(-7.0F, -14.0F, -6.6F, 14.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition in1 = body.addOrReplaceChild("in1", CubeListBuilder.create().texOffs(0, 71).addBox(-7.0F, -14.0F, -6.6F, 14.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition in0 = body.addOrReplaceChild("in0", CubeListBuilder.create().texOffs(0, 57).addBox(-7.0F, -14.0F, -6.6F, 14.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		int tmp = entity.tickCount % 120;
		this.in0.visible = !(tmp == 1 || tmp == 2);
		this.in1.visible = tmp == 2;
		this.in2.visible = tmp == 1;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}