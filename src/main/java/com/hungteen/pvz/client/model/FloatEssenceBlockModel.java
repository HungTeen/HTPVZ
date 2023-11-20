package com.hungteen.pvz.client.model;
// Made with Blockbench 4.8.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.Model;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.util.Mth;

public class FloatEssenceBlockModel extends Model {
	private final ModelPart total;
	private final ModelPart inner;

	public FloatEssenceBlockModel(ModelPart root) {
		super(RenderType::entityCutout);
		this.total = root.getChild("total");
		this.inner = total.getChild("inner");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create()
				.texOffs(0, 0).addBox(-3.0F, -3.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(30, 18).addBox(3.0F, 3.0F, 3.0F, -6.0F, -6.0F, -6.0F, new CubeDeformation(0.0F)),
				PartPose.offset(0.0F, 21.0F, 0.0F));

		PartDefinition inner = total.addOrReplaceChild("inner", CubeListBuilder.create().texOffs(0, 12).addBox(-1.5F, -1.5F, -1.5F, 3.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 32, 32);
	}

	public void setupAnim(float time) {
		this.total.xRot = time * Mth.PI / 100F;
		this.total.yRot = time * Mth.PI / 120F;
		this.total.zRot = time * Mth.PI / 150F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		total.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	public void render(PoseStack p_102317_, VertexConsumer p_102318_, int p_102319_, int p_102320_, float p_102321_, float p_102322_, float p_102323_, float p_102324_) {
		this.total.render(p_102317_, p_102318_, p_102319_, p_102320_, p_102321_, p_102322_, p_102323_, p_102324_);
	}
}