package com.hungteen.pvz.client.model;// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.common.entity.creatures.Sprout;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class SproutModel<T extends Sprout> extends HierarchicalModel<T> {
	private final ModelPart bone;
	private final ModelPart eleaf;
	private final ModelPart wleaf;

	public SproutModel(ModelPart root) {
		this.bone = root.getChild("bone");
		this.eleaf = bone.getChild("eleaf");
		this.wleaf = bone.getChild("wleaf");
	}


	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 0).addBox(-0.5F, -2.0F, -0.5F, 1.0F, 2.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition eleaf = bone.addOrReplaceChild("eleaf", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition eleaf_r1 = eleaf.addOrReplaceChild("eleaf_r1", CubeListBuilder.create().texOffs(-4, 4).addBox(-5.0F, -2.0F, -2.0F, 5.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.0F, 0.0F, 0.1745F));

		PartDefinition wleaf = bone.addOrReplaceChild("wleaf", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition wleaf_r1 = wleaf.addOrReplaceChild("wleaf_r1", CubeListBuilder.create().texOffs(0, 0).addBox(0.0F, -2.0F, -2.0F, 5.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, 0.0F, 0.0F, -0.1745F));

		PartDefinition nleaf = bone.addOrReplaceChild("nleaf", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition nleaf_r1 = nleaf.addOrReplaceChild("nleaf_r1", CubeListBuilder.create().texOffs(-5, 8).addBox(-2.0F, -2.0F, -5.0F, 4.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, 0.0F, -0.1745F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 16, 16);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		wleaf.yRot = entity.isMarigold() ? - 0.5233334f : 0;
		eleaf.yRot = entity.isMarigold() ? 0.5233334f : 0;
		bone.visible = (entity.plant == null);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		bone.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return bone;
	}
}