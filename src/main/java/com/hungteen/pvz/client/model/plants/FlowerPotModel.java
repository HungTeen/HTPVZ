package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.9.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.client.model.plants.animation.FlowerPotModelAnimation;
import com.hungteen.pvz.common.entity.plants.FlowerPot;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class FlowerPotModel<T extends FlowerPot> extends HierarchicalModel<T> {
	private final ModelPart total;

	public FlowerPotModel(ModelPart root) {
		this.total = root.getChild("total");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create().texOffs(0, 17).addBox(-12.0F, -3.0F, 4.0F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.01F)), PartPose.offset(8.0F, 24.0F, -8.0F));

		PartDefinition up = total.addOrReplaceChild("up", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -7.5F, -6.0F, 12.0F, 5.0F, 12.0F, new CubeDeformation(0.02F))
		.texOffs(24, 17).addBox(-3.0F, -7.5F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, 0.0F, 8.0F));

		PartDefinition leave1 = up.addOrReplaceChild("leave1", CubeListBuilder.create(), PartPose.offset(4.0F, -8.5F, -3.5F));

		PartDefinition leaves_r1 = leave1.addOrReplaceChild("leaves_r1", CubeListBuilder.create().texOffs(3, 0).addBox(0.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, -0.3491F));

		PartDefinition leave2 = up.addOrReplaceChild("leave2", CubeListBuilder.create(), PartPose.offset(4.0F, -8.5F, -3.5F));

		PartDefinition leaves_r2 = leave2.addOrReplaceChild("leaves_r2", CubeListBuilder.create().texOffs(-1, 0).addBox(-2.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.3491F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T flowerPot, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.total.getAllParts().forEach(ModelPart::resetPose);
		this.animate(flowerPot.idleAnimationState, FlowerPotModelAnimation.idle, ageInTicks);
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