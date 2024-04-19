package com.hungteen.pvz.client.model.plants;

// Made with Blockbench 4.7.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.entity.plants.WallNut;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class WallNutModel<T extends WallNut> extends EntityModel<T> {
	private final ModelPart total;
	private final ModelPart body;
	private final ModelPart in0;
	private final ModelPart in1;
	private final ModelPart in2;

	public WallNutModel(ModelPart root) {
		this.total = root.getChild("total");
		this.body = total.getChild("body");
		this.in0 = body.getChild("in0");
		this.in1 = body.getChild("in1");
		this.in2 = body.getChild("in2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body = total.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -6.0F, -7.0F, 14.0F, 13.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(0, 27).addBox(-6.0F, -9.0F, -6.0F, 12.0F, 3.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(0, 42).addBox(-5.0F, 7.0F, -5.0F, 10.0F, 1.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(56, 0).addBox(-3.0F, -4.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));

		PartDefinition in2 = body.addOrReplaceChild("in2", CubeListBuilder.create().texOffs(0, 86).addBox(-7.0F, -14.0F, -6.6F, 14.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition in1 = body.addOrReplaceChild("in1", CubeListBuilder.create().texOffs(0, 71).addBox(-7.0F, -14.0F, -6.6F, 14.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, 0.0F));

		PartDefinition in0 = body.addOrReplaceChild("in0", CubeListBuilder.create().texOffs(0, 57).addBox(-7.0F, -14.0F, -6.6F, 14.0F, 13.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 8.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}


	@Override
	public void setupAnim(T wallnut, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		int tmp = wallnut.tickCount % 120;
		this.in0.visible = !(tmp == 1 || tmp == 2);
		this.in1.visible = tmp == 2;
		this.in2.visible = tmp == 1;
		if (wallnut.isBowling()) {
			this.body.xRot = (float) ((wallnut.getDeltaMovement().z * Math.cos(wallnut.yRot / 57.3) + wallnut.getDeltaMovement().x * Math.sin(wallnut.yRot / 57.3)) * wallnut.tickCount);
			this.body.zRot = (float) ((wallnut.getDeltaMovement().x * Math.cos(wallnut.yRot / 57.3) + wallnut.getDeltaMovement().z * Math.sin(wallnut.yRot / 57.3)) * wallnut.tickCount);
		} else {
			this.body.xRot = 0;
			this.body.zRot = 0;
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		total.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}