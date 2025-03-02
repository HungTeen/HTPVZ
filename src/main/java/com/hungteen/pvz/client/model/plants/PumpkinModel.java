package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.9.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.common.entity.plants.Pumpkin;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class PumpkinModel<T extends Pumpkin> extends HierarchicalModel<T> {
	private final ModelPart total;

	public static CubeListBuilder cubes = CubeListBuilder.create().texOffs(0, 0).addBox(-8.5F, -10.0F, -8.5F, 17.0F, 10.0F, 17.0F, new CubeDeformation(0.0F))
			.texOffs(50, 0).addBox(-8.5F, -5.999F, -9.0F, 17.0F, 6.0F, 1.0F, new CubeDeformation(0.0F))
			.texOffs(0, 34).addBox(-8.0F, -9.5F, -8.0F, 16.0F, 9.0F, 16.0F, new CubeDeformation(0.0F));

	public static PartPose pose = PartPose.offset(0.0F, 24.0F, 0.0F);

	public PumpkinModel(ModelPart root) {
		this.total = root.getChild("total");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", cubes, pose);

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {

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