package com.hungteen.pvz.client.model.plants;
// Made with Blockbench 4.9.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.common.entity.plants.LilyPad;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class LilyPadModel<T extends LilyPad> extends EntityModel<T> {
	private final ModelPart total;
	private final ModelPart eyes;

	public LilyPadModel(ModelPart root) {
		this.total = root.getChild("bone");
		this.eyes = total.getChild("eyes");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-15.0F, -4.0F, 1.0F, 14.0F, 4.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-9.0F, -0.5F, 7.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 22).addBox(-15.0F, -0.05F, 1.0F, 14.0F, 2.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(8.0F, 22.5F, -8.0F));

		PartDefinition eyes = bone.addOrReplaceChild("eyes", CubeListBuilder.create().texOffs(0, 18).addBox(-7.0F, -4.0F, -7.1F, 14.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, 0.0F, 8.0F));


		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		total.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}