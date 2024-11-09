package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.client.model.plants.animation.SplitPeaAnimation;
import com.hungteen.pvz.common.entity.plants.SplitPea;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class SplitPeaModel<T extends SplitPea> extends HierarchicalModel<T> {
	private final ModelPart total;

	public SplitPeaModel(ModelPart root) {
		this.total = root.getChild("total");
	}

	public static LayerDefinition createBodyLayer() {MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body = total.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 31).addBox(-1.0F, -12.0F, -1.5F, 2.0F, 12.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(40, 8).addBox(-2.0F, 0.0F, -2.0F, 4.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -12.0F, 0.0F, 0.0873F, 0.0F, 0.0F));

		PartDefinition back = head.addOrReplaceChild("back", CubeListBuilder.create(), PartPose.offset(0.0F, -2.6F, 0.2F));

		PartDefinition head_r1 = back.addOrReplaceChild("head_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(-0.5F)), PartPose.offsetAndRotation(0.0F, -1.0F, 4.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition eyes_closed_b = back.addOrReplaceChild("eyes_closed_b", CubeListBuilder.create(), PartPose.offset(0.0F, -1.0F, 4.0F));

		PartDefinition head_r2 = eyes_closed_b.addOrReplaceChild("head_r2", CubeListBuilder.create().texOffs(0, 35).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(-0.51F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition mouth_b = back.addOrReplaceChild("mouth_b", CubeListBuilder.create().texOffs(30, 0).addBox(-2.0F, -2.0F, -1.7F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, 9.0F));

		PartDefinition mouth_r1 = mouth_b.addOrReplaceChild("mouth_r1", CubeListBuilder.create().texOffs(44, 0).addBox(-3.0F, -3.0F, -1.0F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 2.2F, 0.0F, 3.1416F, 0.0F));

		PartDefinition mouth_closed_b = mouth_b.addOrReplaceChild("mouth_closed_b", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 2.2F));

		PartDefinition mouth_r2 = mouth_closed_b.addOrReplaceChild("mouth_r2", CubeListBuilder.create().texOffs(0, 56).addBox(-3.0F, -3.0F, -1.0F, 6.0F, 6.0F, 2.0F, new CubeDeformation(-0.01F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition eye_brows = back.addOrReplaceChild("eye_brows", CubeListBuilder.create().texOffs(10, 32).addBox(-5.0F, -3.75F, 0.75F, 10.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 8.0F));

		PartDefinition front = head.addOrReplaceChild("front", CubeListBuilder.create().texOffs(0, 0).addBox(-5.0F, -6.0F, -9.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition mouth_f = front.addOrReplaceChild("mouth_f", CubeListBuilder.create().texOffs(30, 0).addBox(-2.0F, -2.0F, -3.0F, 4.0F, 4.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(44, 0).addBox(-3.0F, -3.0F, -4.0F, 6.0F, 6.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 1.0F, -9.0F));

		PartDefinition mouth_closed_f = mouth_f.addOrReplaceChild("mouth_closed_f", CubeListBuilder.create().texOffs(0, 56).addBox(-3.0F, -3.0F, -1.0F, 6.0F, 6.0F, 2.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, 0.0F, -3.0F));

		PartDefinition eyes_closed_f = front.addOrReplaceChild("eyes_closed_f", CubeListBuilder.create().texOffs(0, 35).addBox(-5.0F, -5.0F, -5.0F, 10.0F, 10.0F, 10.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, -1.0F, -4.0F));

		PartDefinition absorb_pea = body.addOrReplaceChild("absorb_pea", CubeListBuilder.create().texOffs(31, 36).addBox(-2.0F, -4.0F, -2.5F, 4.0F, 4.0F, 4.0F, new CubeDeformation(-1.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition down = total.addOrReplaceChild("down", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition n_r1 = down.addOrReplaceChild("n_r1", CubeListBuilder.create().texOffs(-6, 20).addBox(-2.0F, -1.0F, -7.0F, 4.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.8F, 0.0F, -0.1745F, -0.7854F, 0.0F));

		PartDefinition w_r1 = down.addOrReplaceChild("w_r1", CubeListBuilder.create().texOffs(-4, 27).addBox(1.0F, -1.0F, -2.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.8F, 0.0F, 0.1745F, -0.7854F, -0.1745F));

		PartDefinition e_r1 = down.addOrReplaceChild("e_r1", CubeListBuilder.create().texOffs(9, 27).addBox(-7.0F, -1.0F, -2.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.8F, 0.0F, -0.1745F, -0.7854F, 0.1745F));

		PartDefinition s_r1 = down.addOrReplaceChild("s_r1", CubeListBuilder.create().texOffs(2, 20).addBox(-2.0F, -1.0F, 1.0F, 4.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.8F, 0.0F, 0.1745F, -0.7854F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);

	}

	@Override
	public void setupAnim(T splitPea, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.total.getAllParts().forEach(ModelPart::resetPose);
		this.animate(splitPea.idleAnimationState, SplitPeaAnimation.idle, ageInTicks);
		this.animate(splitPea.forwardAnimationState, SplitPeaAnimation.front_shoot, ageInTicks);
		this.animate(splitPea.backwardAnimationState, SplitPeaAnimation.back_shoot, ageInTicks);
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