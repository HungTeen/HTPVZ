package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.9.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.client.model.plants.animation.CabbagePultAnimation;
import com.hungteen.pvz.common.entity.plants.CabbagePult;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class CabbagePultModel<T extends CabbagePult> extends HierarchicalModel<T> {
	private final ModelPart total;

	public CabbagePultModel(ModelPart root) {
		this.total = root.getChild("total");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition bottom = total.addOrReplaceChild("bottom", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -0.5F, -7.0F, 14.0F, 0.0F, 14.0F, new CubeDeformation(0.0F))
		.texOffs(4, 47).addBox(-6.0F, -0.1F, -6.0F, 12.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cabbage = bottom.addOrReplaceChild("cabbage", CubeListBuilder.create().texOffs(0, 28).addBox(-4.5F, -8.0F, -4.5F, 9.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
		.texOffs(32, 20).addBox(-4.0F, -9.0F, -4.0F, 8.0F, 1.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition eyes_closed = cabbage.addOrReplaceChild("eyes_closed", CubeListBuilder.create().texOffs(36, 37).addBox(-4.5F, -4.0F, -5.0F, 9.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.6F));

		PartDefinition dicoration = cabbage.addOrReplaceChild("dicoration", CubeListBuilder.create().texOffs(0, 15).addBox(-5.0F, -2.0F, -5.0F, 10.0F, 3.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.0F, 0.0F));

		PartDefinition pult = cabbage.addOrReplaceChild("pult", CubeListBuilder.create().texOffs(8, 0).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 7.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -9.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition out = pult.addOrReplaceChild("out", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -6.0F, 0.0F, 2.0F, 6.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(42, 0).addBox(-3.0F, -12.0F, 0.0F, 6.0F, 6.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -6.0F, -1.0F, -1.309F, 0.0F, 0.0F));

		PartDefinition bullet = out.addOrReplaceChild("bullet", CubeListBuilder.create().texOffs(0, 45).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
		.texOffs(0, 53).addBox(-2.0F, -2.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, -9.0F, 1.0F, 1.5708F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T cabbagePult, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.total.getAllParts().forEach(ModelPart::resetPose);
		this.animate(cabbagePult.idleAnimationState, CabbagePultAnimation.idle, ageInTicks);
		this.animate(cabbagePult.shootAnimationState, CabbagePultAnimation.shoot, ageInTicks);
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