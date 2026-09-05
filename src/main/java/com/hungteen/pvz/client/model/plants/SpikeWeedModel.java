package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.9.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.common.entity.plants.SpikeWeed;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class SpikeWeedModel<T extends SpikeWeed> extends HierarchicalModel<T> {
	private final ModelPart total;

	public SpikeWeedModel(ModelPart root) {
		this.total = root.getChild("total");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create().texOffs(0, 19).addBox(-8.0F, -3.0F, -8.0F, 16.0F, 3.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(-8.0F, -3.2F, -8.0F, 16.0F, 3.0F, 16.0F, new CubeDeformation(0.3F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition bone = total.addOrReplaceChild("head", CubeListBuilder.create().texOffs(32, 38).addBox(-8.0F, -6.0F, -4.0F, 16.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(32, 42).addBox(-8.0F, -6.0F, 4.0F, 16.0F, 4.0F, 0.0F, new CubeDeformation(0.0F))
		.texOffs(0, 22).addBox(-4.0F, -6.0F, -8.0F, 0.0F, 4.0F, 16.0F, new CubeDeformation(0.0F))
		.texOffs(0, 26).addBox(4.0F, -6.0F, -8.0F, 0.0F, 4.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
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