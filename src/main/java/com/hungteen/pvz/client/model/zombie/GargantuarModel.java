package com.hungteen.pvz.client.model.zombie;// Made with Blockbench 4.10.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.client.model.zombie.animation.GargantuarModelAnimation;
import com.hungteen.pvz.common.entity.zombies.Gargantuar;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraft.world.item.UseAnim;

public class GargantuarModel<T extends Gargantuar> extends HierarchicalModel<T> implements HeadedModel, ArmedModel {
	public final ModelPart gargantuar;
	public final ModelPart total;
	public final ModelPart body;
	private final ModelPart stomach;
	private final ModelPart outerStomach;
	private final ModelPart basket;
	private final ModelPart jacket;
	private final ModelPart rightArm;
	private final ModelPart rightSleeve;
	private final ModelPart rightFist;
	private final ModelPart rightGlove;
	private final ModelPart leftArm;
	private final ModelPart leftSleeve;
	private final ModelPart leftFist;
	private final ModelPart leftGlove;
	private final ModelPart rightLeg;
	private final ModelPart rightPant;
	private final ModelPart leftLeg;
	private final ModelPart leftPant;
	public final ModelPart head;
	public final ModelPart hat;
	public HumanoidModel.ArmPose leftArmPose = HumanoidModel.ArmPose.EMPTY;
	public HumanoidModel.ArmPose rightArmPose = HumanoidModel.ArmPose.EMPTY;


	public GargantuarModel(ModelPart root) {
		this.gargantuar = root.getChild("gargantuar");
		this.total = gargantuar.getChild("total");
		this.body = total.getChild("body");
		this.stomach = body.getChild("stomach");
		this.outerStomach = stomach.getChild("outer_stomach");
		this.basket = body.getChild("basket");
		this.jacket = body.getChild("jacket");
		this.rightArm = body.getChild("right_arm");
		this.rightSleeve = rightArm.getChild("right_sleeve");
		this.rightFist = rightArm.getChild("right_fist");
		this.rightGlove = rightFist.getChild("right_glove");
		this.leftArm = body.getChild("left_arm");
		this.leftSleeve = leftArm.getChild("left_sleeve");
		this.leftFist = leftArm.getChild("left_fist");
		this.leftGlove = leftFist.getChild("left_glove");
		this.rightLeg = body.getChild("right_leg");
		this.rightPant = rightLeg.getChild("right_pant");
		this.leftLeg = body.getChild("left_leg");
		this.leftPant = leftLeg.getChild("left_pant");
		this.head = body.getChild("head");
		this.hat = head.getChild("hat");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition gargantuar = partdefinition.addOrReplaceChild("gargantuar", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, -4.0F));
		PartDefinition total = gargantuar.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 28.0F, 4.0F));

		PartDefinition body = total.addOrReplaceChild("body", CubeListBuilder.create(), PartPose.offset(0.0F, -17.0F, 1.0F));
		PartDefinition chest_r1 = body.addOrReplaceChild("chest_r1", CubeListBuilder.create().texOffs(0, 0).addBox(-11.0F, -6.5F, -5.0F, 22.0F, 11.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -15.5F, 0.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition stomach = body.addOrReplaceChild("stomach", CubeListBuilder.create().texOffs(0, 42).addBox(-7.0F, -15.0F, -1.0F, 14.0F, 15.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
		PartDefinition outer_stomach = stomach.addOrReplaceChild("outer_stomach", CubeListBuilder.create().texOffs(42, 42).addBox(-7.0F, -15.0F, -1.0F, 14.0F, 15.0F, 7.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition basket = body.addOrReplaceChild("basket", CubeListBuilder.create(), PartPose.offset(0.0F, -21.5F, -1.0F));
		PartDefinition outer_basket_r1 = basket.addOrReplaceChild("outer_basket_r1", CubeListBuilder.create().texOffs(64, 18).addBox(-4.0F, -4.5F, -5.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.5F))
				.texOffs(64, 0).addBox(-4.0F, -4.5F, -5.0F, 8.0F, 10.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 12.0F, 0.3491F, 0.0F, 0.0F));

		PartDefinition jacket = body.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(0, 110).addBox(-11.0F, -23.0F, -1.4F, 22.0F, 8.0F, 8.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition chest_r2 = jacket.addOrReplaceChild("chest_r2", CubeListBuilder.create().texOffs(0, 21).addBox(-11.0F, -6.5F, -5.0F, 22.0F, 11.0F, 10.0F, new CubeDeformation(0.5F)), PartPose.offsetAndRotation(0.0F, -15.5F, 0.0F, 0.7854F, 0.0F, 0.0F));

		PartDefinition right_arm = body.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(24, 64).addBox(-2.5F, -3.0F, -3.5F, 5.0F, 17.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(-13.0F, -20.0F, -1.0F));
		PartDefinition right_sleeve = right_arm.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(72, 64).addBox(-2.5F, -9.0F, -3.5F, 5.0F, 17.0F, 7.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition right_fist = right_arm.addOrReplaceChild("right_fist", CubeListBuilder.create().texOffs(72, 104).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 16.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 14.0F, 0.0F));
		PartDefinition right_glove = right_fist.addOrReplaceChild("right_glove", CubeListBuilder.create().texOffs(100, 104).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 16.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 2.0F, 0.0F));

		PartDefinition left_arm = body.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 64).addBox(-2.5F, -3.0F, -3.5F, 5.0F, 17.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(13.0F, -20.0F, -1.0F));
		PartDefinition left_sleeve = left_arm.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(48, 64).addBox(-2.5F, -9.0F, -3.5F, 5.0F, 17.0F, 7.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		PartDefinition left_fist = left_arm.addOrReplaceChild("left_fist", CubeListBuilder.create().texOffs(100, 34).addBox(-3.0F, 0.0F, -4.0F, 6.0F, 16.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 14.0F, 0.0F));
		PartDefinition left_glove = left_fist.addOrReplaceChild("left_glove", CubeListBuilder.create().texOffs(100, 58).addBox(-3.0F, -2.0F, -4.0F, 6.0F, 16.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 2.0F, 0.0F));

		PartDefinition right_leg = body.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(20, 88).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 17.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.5F, -0.5F, 2.0F));
		PartDefinition right_pant = right_leg.addOrReplaceChild("right_pant", CubeListBuilder.create().texOffs(60, 88).addBox(-2.5F, -0.5F, -2.5F, 5.0F, 17.0F, 5.0F, new CubeDeformation(0.49F)), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition left_leg = body.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 88).addBox(-2.5F, 0.5F, -2.5F, 5.0F, 17.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, -0.5F, 2.0F));
		PartDefinition left_pant = left_leg.addOrReplaceChild("left_pant", CubeListBuilder.create().texOffs(40, 88).addBox(-2.5F, -0.5F, -2.5F, 5.0F, 17.0F, 5.0F, new CubeDeformation(0.49F)), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition head = body.addOrReplaceChild("head", CubeListBuilder.create().texOffs(96, 0).addBox(-4.0F, -8.0F, -7.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -17.0F, -5.0F));
		PartDefinition hat = head.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(96, 17).addBox(-4.0F, -14.0F, -7.0F, 8.0F, 9.0F, 8.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 6.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T gargantuar, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.total.getAllParts().forEach(ModelPart::resetPose);
		//humanoid animations.
		boolean flag = gargantuar.getFallFlyingTicks() > 4;
		this.head.yRot = netHeadYaw * ((float)Math.PI / 180F);
		if (flag) {
			this.head.xRot = (-(float)Math.PI / 4F);
		} else {
			this.head.xRot = headPitch / 2 * ((float)Math.PI / 180F);
		}
		float f = 1.0F;
		if (flag) {
			f = (float)gargantuar.getDeltaMovement().lengthSqr();
			f /= 0.2F;
			f *= f * f;
		}
		if (f < 1.0F) {
			f = 1.0F;
		}
		this.rightLeg.xRot = Mth.cos(limbSwing * 0.6662F) * 1.4F * limbSwingAmount / f;
		this.leftLeg.xRot = Mth.cos(limbSwing * 0.6662F + (float)Math.PI) * 1.4F * limbSwingAmount / f;
		this.rightLeg.yRot = 0.0F;
		this.leftLeg.yRot = 0.0F;
		this.rightLeg.zRot = 0.0F;
		this.leftLeg.zRot = 0.0F;

		//keyFrame animations.
		this.animate(gargantuar.attackAnimationState, GargantuarModelAnimation.attack, ageInTicks);
		this.animate(gargantuar.throwAnimationState, GargantuarModelAnimation.throwing, ageInTicks);
		this.animate(gargantuar.idleAnimationState, GargantuarModelAnimation.idle, ageInTicks);

		//baby
		if (young) {
			this.body.xScale *= 0.5F;
			this.body.yScale *= 0.5F;
			this.body.zScale *= 0.5F;
			this.head.xScale *= 1.5F;
			this.head.yScale *= 1.5F;
			this.head.zScale *= 1.5F;
			this.basket.xScale *= 1.5F;
			this.basket.yScale *= 1.5F;
			this.basket.zScale *= 1.5F;
			this.basket.xRot -= 0.3;
			this.total.y = 36;
		}

		//poses
		this.setArmPose(gargantuar);

		if (this.leftArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
			this.leftArm.xRot = this.leftArm.xRot * 0.5F - (float)Math.PI * 0.8F;
			this.leftArm.yRot = 0.0F;
		}
		if (this.rightArmPose == HumanoidModel.ArmPose.THROW_SPEAR) {
			this.rightArm.xRot = this.rightArm.xRot * 0.5F - (float)Math.PI * 0.8F;
			this.rightArm.yRot = 0.0F;
		}
		if (this.rightArmPose == HumanoidModel.ArmPose.BLOCK) {
			this.rightArm.xRot = this.rightArm.xRot * 0.5F - 0.4F;
			this.rightArm.yRot = (-(float)Math.PI / 5F);
		}
		if (this.leftArmPose == HumanoidModel.ArmPose.BLOCK) {
			this.leftArm.xRot = this.leftArm.xRot * 0.5F - 0.4F;
			this.leftArm.yRot = ((float)Math.PI / 5F);
		}
		if (this.rightArmPose == HumanoidModel.ArmPose.BOW_AND_ARROW) {
			this.rightArm.yRot = -0.1F + this.head.yRot * 0.5F;
			this.leftArm.yRot = 0.1F + this.head.yRot * 0.5F + 0.4F;
			this.rightArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot + 0.2F;
			this.leftArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot + 0.5F - (this.head.yRot > 0 ? 0 : this.head.yRot / 2);
		}
		if (this.leftArmPose == HumanoidModel.ArmPose.BOW_AND_ARROW) {
			this.rightArm.yRot = -0.1F + this.head.yRot * 0.5F - 0.4F;
			this.leftArm.yRot = 0.1F + this.head.yRot * 0.5F;
			this.rightArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot + 0.5F - (this.head.yRot > 0 ? 0 : this.head.yRot / 2);
			this.leftArm.xRot = (-(float)Math.PI / 2F) + this.head.xRot + 0.2F;
		}

	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		gargantuar.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return gargantuar;
	}

	@Override
	public ModelPart getHead() {
		return head;
	}

	@Override
	public void translateToHand(HumanoidArm arm, PoseStack poseStack) {
		this.gargantuar.translateAndRotate(poseStack);
		this.total.translateAndRotate(poseStack);
		this.body.translateAndRotate(poseStack);
		if (arm == HumanoidArm.LEFT) {
			this.leftArm.translateAndRotate(poseStack);
			this.leftFist.translateAndRotate(poseStack);
			poseStack.translate(young ? 1 : 0, young ? -0.8 : 0.2, -0.1);
		} else {
			this.rightArm.translateAndRotate(poseStack);
			this.rightFist.translateAndRotate(poseStack);
			poseStack.translate(young ? -1 : 0, young ? -0.8 : 0.2, -0.1);
		}
		if (this.young) {
			poseStack.scale(1.5F, 1.5F, 1.5F);
		}
	}

	public void setArmPose(T zombie) {
		ItemStack item = zombie.getMainHandItem();
		boolean mainArmRight = zombie.getMainArm() == HumanoidArm.RIGHT;
		if (zombie.getMainHandItem().getItem() instanceof ShieldItem && zombie.isUsingItem()) {
			this.rightArmPose = mainArmRight ? HumanoidModel.ArmPose.BLOCK : HumanoidModel.ArmPose.EMPTY;
			this.leftArmPose = ! mainArmRight ? HumanoidModel.ArmPose.BLOCK : HumanoidModel.ArmPose.EMPTY;
		} else if (zombie.getOffhandItem().getItem() instanceof ShieldItem && zombie.isUsingItem()) {
			this.leftArmPose = mainArmRight ? HumanoidModel.ArmPose.BLOCK : HumanoidModel.ArmPose.EMPTY;
			this.rightArmPose = ! mainArmRight ? HumanoidModel.ArmPose.BLOCK : HumanoidModel.ArmPose.EMPTY;
		} else if (item.getUseAnimation() == UseAnim.BOW) {
			this.rightArmPose = mainArmRight ? HumanoidModel.ArmPose.BOW_AND_ARROW : HumanoidModel.ArmPose.EMPTY;
			this.leftArmPose = ! mainArmRight ? HumanoidModel.ArmPose.BOW_AND_ARROW : HumanoidModel.ArmPose.EMPTY;
		} else {
			this.rightArmPose = HumanoidModel.ArmPose.EMPTY;
			this.leftArmPose = HumanoidModel.ArmPose.EMPTY;
		}
	}
}