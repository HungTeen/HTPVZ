package com.hungteen.pvz.client.model.plants;

// Made with Blockbench 4.9.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.client.model.plants.animation.PlanternAnimation;
import com.hungteen.pvz.common.entity.plants.Plantern;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.List;

public class PlanternModel<T extends Plantern> extends HierarchicalModel<T> {
	private final ModelPart total;
	private final ModelPart stick1;
	private final ModelPart stick2;
	private final ModelPart stick3;
	private final ModelPart stick4;
	private final ModelPart stick5;
	private final ModelPart stick6;
	private final ModelPart eyesOpen;
	private final ModelPart eyesClosed;

	public PlanternModel(ModelPart root) {
		this.total = root.getChild("total");
		stick1 = total.getChild("stick1");
		stick2 = stick1.getChild("stick2");
		stick3 = stick2.getChild("stick3");
		stick4 = stick3.getChild("stick4");
		stick5 = stick4.getChild("stick5");
		stick6 = stick5.getChild("stick6");
		eyesOpen = stick6.getChild("head").getChild("eyes_open");
		eyesClosed = stick6.getChild("head").getChild("eyes_closed");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition bottom = total.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(42, 50).addBox(-5.0F, -3.0F, 0.0F, 10.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(0.0F, -3.0F, -5.0F, 0.0F, 3.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(89, 12).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition stick1 = total.addOrReplaceChild("stick1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = stick1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(105, 13).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition stick2 = stick1.addOrReplaceChild("stick2", CubeListBuilder.create().texOffs(105, 13).addBox(-2.0F, -6.0F, -2.0F, 4.0F, 9.0F, 4.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition stick3 = stick2.addOrReplaceChild("stick3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r2 = stick3.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(106, 1).addBox(-1.5F, -9.0F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition stick4 = stick3.addOrReplaceChild("stick4", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r3 = stick4.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(106, 1).addBox(-1.5F, -9.0F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition stick5 = stick4.addOrReplaceChild("stick5", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r4 = stick5.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(106, 1).addBox(-1.5F, -9.0F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -1.5708F, 0.0F));

		PartDefinition stick6 = stick5.addOrReplaceChild("stick6", CubeListBuilder.create().texOffs(106, 1).addBox(-1.5F, -9.0F, -1.5F, 3.0F, 9.0F, 3.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition head = stick6.addOrReplaceChild("head", CubeListBuilder.create().texOffs(45, 53).addBox(-5.5F, -25.0F, -5.5F, 11.0F, 11.0F, 11.0F, new CubeDeformation(0.0F))
				.texOffs(0, 64).addBox(-5.0F, -25.0F, -5.0F, 10.0F, 11.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(0, 44).addBox(-7.0F, -14.0F, -7.0F, 14.0F, 6.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(90, 0).addBox(-1.5F, -14.0F, -1.5F, 3.0F, 8.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cover = head.addOrReplaceChild("cover", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -33.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(0, 23).addBox(-9.0F, -26.0F, -9.0F, 18.0F, 3.0F, 18.0F, new CubeDeformation(0.0F))
				.texOffs(42, 47).addBox(-6.0F, -29.0F, 0.0F, 12.0F, 3.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(42, 32).addBox(0.0F, -29.0F, -6.0F, 0.0F, 3.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition leave_nw = cover.addOrReplaceChild("leave_nw", CubeListBuilder.create().texOffs(30, 17).addBox(-5.0F, 0.0F, -1.0F, 6.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(8.5F, -23.0F, -8.5F));

		PartDefinition leave_ne = cover.addOrReplaceChild("leave_ne", CubeListBuilder.create().texOffs(12, 12).addBox(-1.5F, 0.0F, -1.5F, 3.0F, 4.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, -23.0F, -8.0F));

		PartDefinition leave_se = cover.addOrReplaceChild("leave_se", CubeListBuilder.create().texOffs(71, 14).addBox(-1.5F, 0.0F, -3.5F, 3.0F, 4.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, -23.0F, 8.0F));

		PartDefinition leave_sw = cover.addOrReplaceChild("leave_sw", CubeListBuilder.create().texOffs(50, 17).addBox(-1.5F, 0.0F, -1.0F, 3.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, -23.0F, 8.5F));

		PartDefinition eyes_closed = head.addOrReplaceChild("eyes_closed", CubeListBuilder.create().texOffs(30, 95).addBox(-5.0F, -25.0F, -4.9F, 10.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition eyes_open = head.addOrReplaceChild("eyes_open", CubeListBuilder.create().texOffs(10, 95).addBox(-5.0F, -25.0F, -4.9F, 10.0F, 11.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition inner_light = head.addOrReplaceChild("inner_light", CubeListBuilder.create().texOffs(73, 0).addBox(-2.0F, -22.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T plantern, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.total.getAllParts().forEach(ModelPart::resetPose);
		float f = ageInTicks - (float) plantern.tickCount;
		eyesOpen.visible = plantern.tickCount % 100 >= 3;
		eyesClosed.visible = ! eyesOpen.visible;
		double height = plantern.getBbHeight() - 2;
		if (height > 0) {
			for (ModelPart i : List.of(stick1, stick2, stick3, stick4, stick5, stick6)) {
					i.y -= height * 16 / 6;
			}
			stick3.y = Math.max(stick3.y, - 6);
		}
		this.animate(plantern.idleAnimationState, PlanternAnimation.idle, ageInTicks);
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