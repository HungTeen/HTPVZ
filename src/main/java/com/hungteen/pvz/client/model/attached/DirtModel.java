package com.hungteen.pvz.client.model.attached;// Made with Blockbench 4.9.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.Entity;

public class DirtModel<T extends Entity> extends EntityModel<T> {
	private final ModelPart dirt;
	private final ModelPart ne;
	private final ModelPart nw;
	private final ModelPart se;
	private final ModelPart sw;

	public DirtModel(ModelPart root) {
		this.dirt = root.getChild("dirt");
		this.ne = dirt.getChild("ne");
		this.nw = dirt.getChild("nw");
		this.se = dirt.getChild("se");
		this.sw = dirt.getChild("sw");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition dirt = partdefinition.addOrReplaceChild("dirt", CubeListBuilder.create(), PartPose.offset(0.0F, 25.0F, 0.0F));

		PartDefinition ne = dirt.addOrReplaceChild("ne", CubeListBuilder.create().texOffs(8, 4).mirror().addBox(-6.6F, -4.0F, -6.6F, 2.0F, 3.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(4, 6).mirror().addBox(-4.6F, -2.0F, -6.6F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(-2.6F, -2.0F, -6.6F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 3).mirror().addBox(-6.6F, -3.0F, -4.6F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(5.0F, 0.0F, 5.0F));

		PartDefinition sw = dirt.addOrReplaceChild("sw", CubeListBuilder.create().texOffs(0, 0).mirror().addBox(0.6F, -2.0F, 4.6F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(2, 5).mirror().addBox(3.6F, -3.0F, 3.6F, 3.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(2, 3).mirror().addBox(4.6F, -2.0F, 0.6F, 2.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-6.0F, 0.1F, -6.0F));

		PartDefinition se = dirt.addOrReplaceChild("se", CubeListBuilder.create().texOffs(2, 6).mirror().addBox(-6.5F, -4.0F, 3.5F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(-6.5F, -2.0F, -0.5F, 3.0F, 1.0F, 4.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(-2.5F, -2.0F, 4.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(4.0F, -0.1F, -6.0F));

		PartDefinition nw = dirt.addOrReplaceChild("nw", CubeListBuilder.create().texOffs(0, 9).mirror().addBox(4.5F, -3.0F, -6.5F, 2.0F, 2.0F, 3.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(1.5F, -2.0F, -6.5F, 3.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false)
		.texOffs(0, 0).mirror().addBox(5.5F, -2.0F, -3.5F, 1.0F, 1.0F, 2.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-6.0F, 0.2F, 5.0F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		float width = entity.getBbWidth() * 8;
		dirt.visible = ! entity.level.getBlockState(entity.getOnPos()).isAir();
		if (dirt.visible) {
			ne.x = -width + 5;
			ne.z = -width + 5;
			se.x = -width + 5;
			se.z = width - 5;
			nw.x = width - 5;
			nw.z = -width + 5;
			sw.x = width - 5;
			sw.z = width - 5;
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		dirt.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}