package com.hungteen.pvz.client.model.bullet;// Made with Blockbench 5.1.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.hungteen.pvz.common.entity.bullet.ChorusTerminatorBullet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;

public class ChorusTerminatorBulletModel<T extends ChorusTerminatorBullet> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	private final ModelPart total;
	private final ModelPart bone;
	private final ModelPart bone3;
	private final ModelPart bone2;

	public ChorusTerminatorBulletModel(ModelPart root) {
		this.total = root.getChild("total");
		this.bone = this.total.getChild("bone");
		this.bone3 = this.bone.getChild("bone3");
		this.bone2 = this.bone3.getChild("bone2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -9.0F, -5.0F, 10.0F, 12.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(28, 22).addBox(-4.0F, -12.0F, -4.0F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.0F))
				.texOffs(40, 0).addBox(-3.0F, 3.0F, -3.0F, 6.0F, 2.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(0, 32).addBox(-7.0F, -3.0F, 0.0F, 14.0F, 10.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(0, 8).addBox(0.0F, -3.0F, -7.0F, 0.0F, 10.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 23.0F, 0.0F));

		PartDefinition bone = total.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 42).addBox(-4.0F, -4.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, 0.0F));

		PartDefinition bone3 = bone.addOrReplaceChild("bone3", CubeListBuilder.create().texOffs(40, 8).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition bone2 = bone3.addOrReplaceChild("bone2", CubeListBuilder.create().texOffs(28, 34).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.bone2.yRot = Mth.rotLerp(ageInTicks, entity.tickCount, entity.tickCount + 1) / 20 % 6.28f;
		this.bone2.xRot = Mth.rotLerp(ageInTicks, entity.tickCount, entity.tickCount + 1) / 17 % 6.28f;
		this.bone3.zRot = Mth.rotLerp(ageInTicks, entity.tickCount, entity.tickCount + 1) / 13 % 6.28f;
		this.bone3.xRot = Mth.rotLerp(ageInTicks, entity.tickCount, entity.tickCount + 1) / 11 % 6.28f;
		this.total.xRot = Mth.rotLerp(ageInTicks % 1, - (entity.xRotO - 90) / 57.3f, - (entity.xRot - 90) / 57.3f);
		this.total.yRot = Mth.rotLerp(ageInTicks % 1, entity.yRotO / 57.3f, entity.yRot / 57.3f);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		total.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}