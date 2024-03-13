package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.client.model.plants.animation.JalapenoAnimation;
import com.hungteen.pvz.common.entity.plants.Jalapeno;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

import java.util.Random;

public class JalapenoModel<T extends Jalapeno> extends HierarchicalModel<T> {
	private Random random = new Random();
	private final ModelPart total;
	private final ModelPart inner;

	public JalapenoModel(ModelPart root) {
		this.total = root.getChild("total");
		this.inner = total.getChild("bone").getChild("inner");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition bone = total.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -22.0F, -5.0F, 10.0F, 13.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = bone.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(32, 15).addBox(-4.0F, -1.75F, -4.25F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(24, 30).addBox(-4.0F, -1.5F, -4.25F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -22.5F, 0.0F, 0.1309F, 0.0F, 0.0F));

		PartDefinition bottom = bone.addOrReplaceChild("bottom", CubeListBuilder.create(), PartPose.offset(0.0F, -7.5F, 0.25F));

		PartDefinition cube_r2 = bottom.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 23).addBox(-4.0F, -4.5F, -4.75F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, -0.25F, -0.2182F, 0.0F, 0.0F));

		PartDefinition cube_r3 = bottom.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(30, 0).addBox(-3.0F, 1.5F, -3.0F, 6.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 1.0F, -0.25F, -0.4363F, 0.0F, 0.0F));

		PartDefinition inner = bone.addOrReplaceChild("inner", CubeListBuilder.create().texOffs(0, 38).addBox(-5.0F, -22.0F, -5.0F, 10.0F, 13.0F, 10.0F, new CubeDeformation(-0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition stick = bone.addOrReplaceChild("stick", CubeListBuilder.create().texOffs(0, 30).addBox(0.0F, -9.0F, -7.0F, 0.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -22.0F, -0.5F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.total.getAllParts().forEach(ModelPart::resetPose);
		this.inner.x = (float) (random.nextFloat() * 0.5 - 0.25);
		this.inner.z = (float) (random.nextFloat() * 0.5 - 0.25);
		this.animate(entity.idleAnimationState, JalapenoAnimation.explode, ageInTicks);
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