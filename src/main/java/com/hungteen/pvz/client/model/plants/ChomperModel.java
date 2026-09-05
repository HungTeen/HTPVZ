package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.client.model.plants.animation.ChomperModelAnimation;
import com.hungteen.pvz.common.entity.plants.Chomper;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class ChomperModel<T extends Chomper> extends HierarchicalModel<T> {
	private final ModelPart bone;

	public ChomperModel(ModelPart root) {
		this.bone = root.getChild("bone");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition bone = partdefinition.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition total = bone.addOrReplaceChild("total", CubeListBuilder.create().texOffs(80, 0).addBox(-3.5F, -5.0F, -3.5F, 7.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition leaves = total.addOrReplaceChild("leaves", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition leafw = leaves.addOrReplaceChild("leafw", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r1 = leafw.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(39, 0).addBox(-3.0F, -2.75F, 6.0F, 13.0F, 0.0F, 15.0F, new CubeDeformation(0.0F))
				.texOffs(39, 15).addBox(-3.0F, -3.0F, 6.0F, 13.0F, 0.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, 0.0F, 2.0944F, 0.0F));

		PartDefinition leafe = leaves.addOrReplaceChild("leafe", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r2 = leafe.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(39, 45).addBox(-10.0F, -2.75F, 6.0F, 13.0F, 0.0F, 15.0F, new CubeDeformation(0.0F))
				.texOffs(26, 60).addBox(-10.0F, -3.0F, 6.0F, 13.0F, 0.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 4.0F, 0.0F, -2.0944F, 0.0F));

		PartDefinition leafs = leaves.addOrReplaceChild("leafs", CubeListBuilder.create().texOffs(0, 55).addBox(-6.5F, -3.0F, 3.0F, 13.0F, 0.0F, 15.0F, new CubeDeformation(0.0F))
				.texOffs(39, 30).addBox(-6.5F, -2.75F, 3.0F, 13.0F, 0.0F, 15.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition neck = total.addOrReplaceChild("neck", CubeListBuilder.create().texOffs(81, 20).addBox(-3.0F, -12.0F, -2.0F, 6.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition neck2 = neck.addOrReplaceChild("neck2", CubeListBuilder.create().texOffs(22, 90).addBox(-2.0F, -8.0F, -2.0F, 4.0F, 10.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition neck3 = neck2.addOrReplaceChild("neck3", CubeListBuilder.create().texOffs(0, 55).addBox(-1.5F, -9.0F, -1.5F, 3.0F, 10.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.5F, 0.0F));

		PartDefinition connect = neck3.addOrReplaceChild("connect", CubeListBuilder.create().texOffs(80, 37).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.5F, 0.0F));

		PartDefinition leafuw = connect.addOrReplaceChild("leafuw", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 2.0F));

		PartDefinition cube_r3 = leafuw.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(80, 49).addBox(-5.0F, -4.0F, 2.75F, 11.0F, 13.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(67, 84).addBox(-5.0F, -4.0F, 3.0F, 11.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 6.0F, -1.0F, 0.0F, 2.0944F, 0.0F));

		PartDefinition leafue = connect.addOrReplaceChild("leafue", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 2.0F));

		PartDefinition cube_r4 = leafue.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(89, 84).addBox(-5.0F, 0.0F, 2.75F, 11.0F, 13.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(0, 90).addBox(-5.0F, 0.0F, 3.0F, 11.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 2.0F, -1.0F, 0.0F, -2.0944F, 0.0F));

		PartDefinition leafu = connect.addOrReplaceChild("leafu", CubeListBuilder.create().texOffs(89, 97).addBox(-5.0F, 2.0F, 2.0F, 11.0F, 13.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(67, 97).addBox(-5.0F, 2.0F, 1.75F, 11.0F, 13.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 2.0F));

		PartDefinition up_head = connect.addOrReplaceChild("up_head", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -19.0F, -3.0F, 16.0F, 17.0F, 11.0F, new CubeDeformation(0.0F))
				.texOffs(38, 61).addBox(0.0F, -18.0F, 2.0F, 0.0F, 19.0F, 14.0F, new CubeDeformation(0.0F))
				.texOffs(67, 62).addBox(-8.5F, -20.0F, -0.25F, 17.0F, 19.0F, 3.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, -1.5F, 0.0F));

		PartDefinition down_head = connect.addOrReplaceChild("down", CubeListBuilder.create().texOffs(0, 28).addBox(-8.0F, -15.5F, -6.0F, 16.0F, 16.0F, 11.0F, new CubeDeformation(0.05F))
				.texOffs(0, 70).addBox(-8.5F, -17.0F, -1.25F, 17.0F, 18.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.5F, 2.0F));

		PartDefinition saliva = down_head.addOrReplaceChild("saliva", CubeListBuilder.create().texOffs(-5, 105).addBox(-8.0F, 0.0F, -5.0F, 16.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -17.0F, 1.0F));

		PartDefinition saliva2 = saliva.addOrReplaceChild("saliva2", CubeListBuilder.create().texOffs(-5, 110).addBox(-8.0F, 0.0F, -5.0F, 16.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, -5.0F));

		PartDefinition tonguer = down_head.addOrReplaceChild("tonguer", CubeListBuilder.create().texOffs(50, 94).addBox(-3.0F, -14.0F, 0.0F, 6.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 3.5F, -1.0F));

		PartDefinition tonguem = tonguer.addOrReplaceChild("tonguem", CubeListBuilder.create().texOffs(38, 94).addBox(-3.0F, -9.0F, 0.0F, 6.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -14.0F, 0.0F));

		PartDefinition tonguet = tonguem.addOrReplaceChild("tonguet", CubeListBuilder.create().texOffs(50, 102).addBox(-3.0F, -7.0F, 0.0F, 6.0F, 7.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -9.0F, 0.0F));

		PartDefinition swallow = total.addOrReplaceChild("swallow", CubeListBuilder.create().texOffs(96, 112).addBox(-4.0F, -8.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T chomper, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.bone.getAllParts().forEach(ModelPart::resetPose);
		this.animate(chomper.idleAnimationState, ChomperModelAnimation.idle, ageInTicks);
		this.animate(chomper.digAnimationState, ChomperModelAnimation.dig, ageInTicks);
		this.animate(chomper.attackAnimationState, ChomperModelAnimation.attack, ageInTicks);
		this.animate(chomper.outAnimationState, ChomperModelAnimation.out, ageInTicks);
		this.animate(chomper.digestAnimationState, ChomperModelAnimation.digest, ageInTicks);
		this.animate(chomper.swallowAnimationState, ChomperModelAnimation.swallow, ageInTicks);
		this.animate(chomper.swimAnimationState, ChomperModelAnimation.swim, ageInTicks);
		this.animate(chomper.meleeAnimationState, ChomperModelAnimation.melee, ageInTicks);
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