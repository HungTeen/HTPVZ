package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.12.6
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.client.model.plants.animation.DandelionModelAnimation;
import com.hungteen.pvz.common.entity.plants.Dandelion;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class DandelionModel<T extends Dandelion> extends HierarchicalModel<T> {
	private final ModelPart total;
	private final ModelPart body;
	private final ModelPart body_2;
	private final ModelPart head;
	private final ModelPart decoration;
	private final ModelPart eyebrows;
	private final ModelPart eyes;
	private final ModelPart close;
	private final ModelPart open;
	private final ModelPart flower;
	private final ModelPart inner;
	private final ModelPart outer;
	private final ModelPart feather;
	private final ModelPart ring;
	private final ModelPart bone;
	private final ModelPart leaves;
	private final ModelPart front;
	private final ModelPart f_front;
	private final ModelPart left;
	private final ModelPart l_front;
	private final ModelPart right;
	private final ModelPart r_front;

	public DandelionModel(ModelPart root) {
		this.total = root.getChild("total");
		this.body = this.total.getChild("body");
		this.body_2 = this.body.getChild("body_2");
		this.head = this.body_2.getChild("head");
		this.decoration = this.head.getChild("decoration");
		this.eyebrows = this.head.getChild("eyebrows");
		this.eyes = this.head.getChild("eyes");
		this.close = this.eyes.getChild("close");
		this.open = this.eyes.getChild("open");
		this.flower = this.head.getChild("flower");
		this.inner = this.flower.getChild("inner");
		this.outer = this.inner.getChild("outer");
		this.feather = this.outer.getChild("feather");
		this.ring = this.head.getChild("ring");
		this.bone = this.head.getChild("bone");
		this.leaves = this.total.getChild("leaves");
		this.front = this.leaves.getChild("front");
		this.f_front = this.front.getChild("f_front");
		this.left = this.leaves.getChild("left");
		this.l_front = this.left.getChild("l_front");
		this.right = this.leaves.getChild("right");
		this.r_front = this.right.getChild("r_front");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body = total.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 79).addBox(-1.5F, -6.0F, -1.5F, 3.0F, 6.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition body_2 = body.addOrReplaceChild("body_2", CubeListBuilder.create().texOffs(12, 79).addBox(-1.0F, -6.0F, -1.0F, 2.0F, 7.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -6.0F, 0.0F));

		PartDefinition head = body_2.addOrReplaceChild("head", CubeListBuilder.create().texOffs(72, 41).addBox(-3.0F, -6.0F, -3.0F, 6.0F, 5.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(72, 52).addBox(-6.0F, -9.0F, 0.0F, 12.0F, 9.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition decoration = head.addOrReplaceChild("decoration", CubeListBuilder.create().texOffs(88, 61).addBox(2.995F, -4.0F, -2.0F, 3.0F, 5.0F, 5.0F, new CubeDeformation(0.0F))
				.texOffs(72, 61).addBox(-5.995F, -4.0F, -2.0F, 3.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));

		PartDefinition cube_r1 = decoration.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(82, 79).addBox(-0.91F, -4.0F, 0.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F))
				.texOffs(70, 79).addBox(-9.89F, -4.0F, 0.0F, 3.0F, 5.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(3.9F, 1.65F, -2.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r2 = decoration.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(48, 21).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -4.0F, -0.25F, -0.7854F, 0.0F, 0.0F));

		PartDefinition cube_r3 = decoration.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(56, 79).addBox(-2.0F, -3.0F, -2.0F, 4.0F, 3.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -3.75F, 0.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition eyebrows = head.addOrReplaceChild("eyebrows", CubeListBuilder.create().texOffs(56, 21).addBox(-4.0F, -1.5F, -3.5F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(56, 22).addBox(1.0F, -1.5F, -3.5F, 3.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.0F, 0.0F));

		PartDefinition eyes = head.addOrReplaceChild("eyes", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition close = eyes.addOrReplaceChild("close", CubeListBuilder.create().texOffs(32, 80).addBox(-3.0F, -3.5F, -3.25F, 6.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition open = eyes.addOrReplaceChild("open", CubeListBuilder.create().texOffs(20, 80).addBox(-3.0F, -3.5F, -3.25F, 6.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition flower = head.addOrReplaceChild("flower", CubeListBuilder.create(), PartPose.offset(0.0F, -3.0F, 0.0F));

		PartDefinition inner = flower.addOrReplaceChild("inner", CubeListBuilder.create().texOffs(84, 17).addBox(5.0F, 0.0F, 5.0F, -9.0F, -8.0F, -9.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, 3.0F, -0.5F));

		PartDefinition outer = inner.addOrReplaceChild("outer", CubeListBuilder.create().texOffs(48, 24).addBox(6.0F, 0.0F, 6.0F, -12.0F, -11.0F, -12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.5F, 1.0F, -0.5F));

		PartDefinition feather = outer.addOrReplaceChild("feather", CubeListBuilder.create().texOffs(0, 58).addBox(-9.0F, -13.0F, 0.0F, 18.0F, 17.0F, 0.0F, new CubeDeformation(0.0F))
				.texOffs(0, 23).addBox(0.0F, -13.0F, -9.0F, 0.0F, 17.0F, 18.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -2.0F, 1.0F));

		PartDefinition cube_r4 = feather.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(36, 23).addBox(0.0F, -13.0F, -9.0F, 0.0F, 17.0F, 18.0F, new CubeDeformation(0.0F))
				.texOffs(36, 58).addBox(-9.0F, -13.0F, 0.0F, 18.0F, 17.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.0F));

		PartDefinition ring = head.addOrReplaceChild("ring", CubeListBuilder.create().texOffs(88, 41).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -3.0F, 0.0F));

		PartDefinition bone = head.addOrReplaceChild("bone", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, 0.0F));

		PartDefinition cube_r5 = bone.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(38, 75).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, 1.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition cube_r6 = bone.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(20, 75).addBox(-2.0F, 0.0F, -5.0F, 4.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.5F, -1.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition cube_r7 = bone.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(74, 75).addBox(0.0F, 0.0F, -2.0F, 5.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.0F, 0.5F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition cube_r8 = bone.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(56, 75).addBox(-5.0F, 0.0F, -2.0F, 5.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.0F, 0.5F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition leaves = total.addOrReplaceChild("leaves", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition front = leaves.addOrReplaceChild("front", CubeListBuilder.create().texOffs(72, 17).addBox(-4.0F, 0.0F, -9.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition f_front = front.addOrReplaceChild("f_front", CubeListBuilder.create().texOffs(48, 17).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -9.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition left = leaves.addOrReplaceChild("left", CubeListBuilder.create().texOffs(72, 25).addBox(-4.0F, 0.0F, -9.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, -2.0944F, 0.0F));

		PartDefinition l_front = left.addOrReplaceChild("l_front", CubeListBuilder.create().texOffs(72, 71).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -9.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition right = leaves.addOrReplaceChild("right", CubeListBuilder.create().texOffs(72, 33).addBox(-4.0F, 0.0F, -9.0F, 8.0F, 0.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -0.4363F, 2.0944F, 0.0F));

		PartDefinition r_front = right.addOrReplaceChild("r_front", CubeListBuilder.create().texOffs(0, 75).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 0.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, -9.0F, 0.7854F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}


	@Override
	public void setupAnim(Dandelion dandelion, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.total.getAllParts().forEach(ModelPart::resetPose);
		boolean special = dandelion.getCustomName() != null && dandelion.getCustomName().getString().equals("涟清");
		this.ring.visible = special;
		this.decoration.visible = special;
		this.animate(dandelion.idleAnimationState, DandelionModelAnimation.idle, ageInTicks);
		this.animate(dandelion.shootAnimationState, DandelionModelAnimation.shoot, ageInTicks);
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