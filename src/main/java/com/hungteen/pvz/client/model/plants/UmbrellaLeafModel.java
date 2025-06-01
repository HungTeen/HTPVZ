package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.client.model.plants.animation.UmbrellaLeafAnimation;
import com.hungteen.pvz.common.entity.plants.UmbrellaLeaf;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class UmbrellaLeafModel<T extends UmbrellaLeaf> extends HierarchicalModel<T> {
	private final ModelPart total;
	private final ModelPart flower;

	public UmbrellaLeafModel(ModelPart root) {
		this.total = root.getChild("total");
		this.flower = total.getChild("head").getChild("leaves").getChild("flower");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition head = total.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -12.5F, -1.0F, 2.0F, 2.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 49).addBox(-3.0F, -11.0F, -3.0F, 6.0F, 11.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition eyes_closed = head.addOrReplaceChild("eyes_closed", CubeListBuilder.create().texOffs(18, 0).addBox(-3.0F, -11.0F, -3.0F, 6.0F, 11.0F, 6.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition leaves = head.addOrReplaceChild("leaves", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition flower = leaves.addOrReplaceChild("flower", CubeListBuilder.create().texOffs(-7, 4).addBox(-3.5F, -0.15F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F))
				.texOffs(-7, 11).addBox(-3.5F, -0.01F, -3.5F, 7.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.9F, 0.0F));

		PartDefinition open = leaves.addOrReplaceChild("open", CubeListBuilder.create(), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition open2 = open.addOrReplaceChild("open2", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition open2_r1 = open2.addOrReplaceChild("open2_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-21.0F, -12.0F, -21.0F, 42.0F, 0.0F, 42.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 12.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition open1 = open.addOrReplaceChild("open1", CubeListBuilder.create().texOffs(0, 42).addBox(-21.0F, 0.0F, -21.0F, 42.0F, 0.0F, 42.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.75F, 0.0F));

		PartDefinition closed = leaves.addOrReplaceChild("closed", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition swe = closed.addOrReplaceChild("swe", CubeListBuilder.create().texOffs(0, 71).addBox(-5.0F, 0.5F, -1.0F, 6.0F, 10.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition swd = closed.addOrReplaceChild("swd", CubeListBuilder.create().texOffs(18, 60).addBox(-1.0F, -0.15F, -1.01F, 6.0F, 10.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition nw = closed.addOrReplaceChild("nw", CubeListBuilder.create().texOffs(0, 18).addBox(-2.0F, -0.5F, -7.0F, 8.0F, 10.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition e = closed.addOrReplaceChild("e", CubeListBuilder.create().texOffs(0, 86).addBox(-7.0F, 0.0F, -5.0F, 8.0F, 10.0F, 11.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		PartDefinition sw = closed.addOrReplaceChild("sw", CubeListBuilder.create().texOffs(0, 35).addBox(-1.0F, -0.25F, -1.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -12.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 256, 256);

	}

	@Override
	public void setupAnim(T umbrellaLeaf, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		flower.visible = ! umbrellaLeaf.hasSkill("skill.pvz.umbrella_leaf.a_skill_name_for_cheap_but_breakable_umbrella_leaf");
		this.total.getAllParts().forEach(ModelPart::resetPose);
		this.animate(umbrellaLeaf.idleAnimationState, UmbrellaLeafAnimation.idle, ageInTicks);
		this.animate(umbrellaLeaf.openAnimationState, umbrellaLeaf.hasSkill(UmbrellaLeaf.BOUNCE_SKILL_NAME) ?
				UmbrellaLeafAnimation.open_expanded : UmbrellaLeafAnimation.open, ageInTicks);
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