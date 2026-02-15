package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.client.model.plants.animation.MelonPultModelAnimation;
import com.hungteen.pvz.common.entity.plants.MelonPult;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class MelonPultModel<T extends MelonPult> extends HierarchicalModel<T> {
	private final ModelPart total;

	public MelonPultModel(ModelPart root) {
		this.total = root.getChild("total");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition head = total.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-7.0F, -10.5F, -5.0F, 14.0F, 10.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition eye_brow = head.addOrReplaceChild("eye_brow", CubeListBuilder.create().texOffs(32, 44).addBox(-7.0F, -8.0F, -5.2F, 14.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition eyes_closed = head.addOrReplaceChild("eyes_closed", CubeListBuilder.create().texOffs(0, 56).addBox(-6.0F, -7.5F, -4.991F, 12.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition plut = head.addOrReplaceChild("plut", CubeListBuilder.create(), PartPose.offsetAndRotation(0.0F, -9.0F, 5.0F, 0.3927F, 0.0F, 0.0F));

		PartDefinition bone = plut.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(52, 46).addBox(-1.5F, -2.5F, 5.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.9343F, -5.3848F));

		PartDefinition bone2 = bone.addOrReplaceChild("bone2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 7.0F));

		PartDefinition cube_r1 = bone2.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(52, 52).addBox(-2.5F, -3.0F, 0.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.1309F, -0.1745F, 0.0F));

		PartDefinition bone3 = bone2.addOrReplaceChild("bone3", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 2.0F));

		PartDefinition cube_r2 = bone3.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(52, 58).addBox(-1.0F, -2.0F, -1.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.75F, -0.2618F, 0.0F, 0.0F));

		PartDefinition busket = bone3.addOrReplaceChild("busket", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -0.5671F, 0.0474F, 2.0F, 2.0F, 1.0F, new CubeDeformation(0.0F))
				.texOffs(0, 20).addBox(-5.0F, -1.5671F, 1.0474F, 10.0F, 5.0F, 10.0F, new CubeDeformation(0.0F))
				.texOffs(0, 35).addBox(-4.0F, -1.5671F, 2.0474F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -1.0F, 2.0F, -0.2182F, 0.0F, 0.0F));

		PartDefinition melon = busket.addOrReplaceChild("melon", CubeListBuilder.create().texOffs(32, 27).addBox(-4.0F, -3.5F, -3.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -5.0671F, 6.0474F, -0.6545F, 0.0F, 0.0F));

		PartDefinition n_leaves = total.addOrReplaceChild("n_leaves", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition ne_r1 = n_leaves.addOrReplaceChild("ne_r1", CubeListBuilder.create().texOffs(39, 18).addBox(-10.0F, 0.0F, -8.0F, 11.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, 0.0873F));

		PartDefinition nw_r1 = n_leaves.addOrReplaceChild("nw_r1", CubeListBuilder.create().texOffs(29, 0).addBox(-1.0F, 0.0F, -8.0F, 11.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.0873F, 0.0F, -0.0873F));

		PartDefinition s_leaves = total.addOrReplaceChild("s_leaves", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition sw_r1 = s_leaves.addOrReplaceChild("sw_r1", CubeListBuilder.create().texOffs(13, 47).addBox(-1.0F, 0.0F, -1.0F, 11.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, -0.0873F));

		PartDefinition se_r1 = s_leaves.addOrReplaceChild("se_r1", CubeListBuilder.create().texOffs(-9, 47).addBox(-10.0F, 0.0F, -1.0F, 11.0F, 0.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0873F, 0.0F, 0.0873F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T melonPult, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.total.getAllParts().forEach(ModelPart::resetPose);
		this.animate(melonPult.idleAnimationState, MelonPultModelAnimation.idle, ageInTicks);
		this.animate(melonPult.shootAnimationState, MelonPultModelAnimation.shoot, ageInTicks);
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