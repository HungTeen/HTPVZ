package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.10.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.client.model.plants.animation.StarfruitAnimation;
import com.hungteen.pvz.common.entity.plants.Starfruit;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class StarfruitModel<T extends Starfruit> extends HierarchicalModel<T> {
	private final ModelPart total;
	private final ModelPart body;
	private final ModelPart r1;
	private final ModelPart r2;
	private final ModelPart r3;
	private final ModelPart r4;
	private final ModelPart r0;
	private final ModelPart sprout;
	private final ModelPart eyes_closed;
	private final ModelPart leaves;
	private final ModelPart leaves1;
	private final ModelPart leaves2;

	public StarfruitModel(ModelPart root) {
		this.total = root.getChild("total");
		this.body = total.getChild("head");
		this.r1 = body.getChild("1");
		this.r2 = body.getChild("2");
		this.r3 = body.getChild("3");
		this.r4 = body.getChild("4");
		this.r0 = body.getChild("0");
		this.sprout = r0.getChild("sprout");
		this.eyes_closed = body.getChild("eyes_closed");
		this.leaves = total.getChild("leaves");
		this.leaves1 = leaves.getChild("leaves1");
		this.leaves2 = leaves.getChild("leaves2");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		//named head to let butter layer identify.
		PartDefinition body = total.addOrReplaceChild("head", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition main_r1 = body.addOrReplaceChild("main_r1", CubeListBuilder.create().texOffs(0, 32).addBox(-4.3F, -2.5F, -5.0F, 9.0F, 5.0F, 9.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.2F, -3.0F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition r1 = body.addOrReplaceChild("1", CubeListBuilder.create(), PartPose.offset(0.0F, -1.5F, 0.0F));

		PartDefinition cube_r1 = r1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 46).addBox(4.0F, -3.0F, -3.0F, 3.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, -3.1416F, -0.9425F, 3.1416F));

		PartDefinition cube_r2 = r1.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(0, 21).addBox(7.0F, -3.0F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, -0.9425F, 3.1416F));

		PartDefinition r2 = body.addOrReplaceChild("2", CubeListBuilder.create(), PartPose.offset(0.0F, -1.5F, 0.0F));

		PartDefinition cube_r3 = r2.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(36, 32).addBox(3.0F, -3.0F, -3.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, -3.1416F, 0.3142F, 3.1416F));

		PartDefinition cube_r4 = r2.addOrReplaceChild("cube_r4", CubeListBuilder.create().texOffs(48, 0).addBox(7.0F, -3.0F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -3.1416F, 0.3142F, 3.1416F));

		PartDefinition r3 = body.addOrReplaceChild("3", CubeListBuilder.create(), PartPose.offset(0.0F, -1.5F, 0.0F));

		PartDefinition cube_r5 = r3.addOrReplaceChild("cube_r5", CubeListBuilder.create().texOffs(18, 46).addBox(4.0F, -3.0F, -3.0F, 3.0F, 4.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.0F, -0.9425F, 0.0F));

		PartDefinition cube_r6 = r3.addOrReplaceChild("cube_r6", CubeListBuilder.create().texOffs(0, 14).addBox(7.0F, -3.0F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.9425F, 0.0F));

		PartDefinition r4 = body.addOrReplaceChild("4", CubeListBuilder.create(), PartPose.offset(0.0F, -1.5F, 0.0F));

		PartDefinition cube_r7 = r4.addOrReplaceChild("cube_r7", CubeListBuilder.create().texOffs(30, 42).addBox(3.0F, -3.0F, -3.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 7).addBox(7.0F, -2.5F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.0F, 0.3142F, 0.0F));

		PartDefinition r0 = body.addOrReplaceChild("0", CubeListBuilder.create(), PartPose.offset(0.0F, -1.5F, 0.0F));

		PartDefinition cube_r8 = r0.addOrReplaceChild("cube_r8", CubeListBuilder.create().texOffs(44, 46).addBox(3.0F, -3.0F, -3.0F, 4.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 0).addBox(7.0F, -2.5F, -2.0F, 3.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 0.0F, 0.0F, 1.5708F, 0.0F));

		PartDefinition sprout = r0.addOrReplaceChild("sprout", CubeListBuilder.create(), PartPose.offset(-0.5F, -3.0F, -8.5F));

		PartDefinition cube_r9 = sprout.addOrReplaceChild("cube_r9", CubeListBuilder.create().texOffs(0, 28).addBox(-1.5F, -3.5F, 0.0F, 5.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.1745F, 0.0F, 0.0F));

		PartDefinition eyes_closed = body.addOrReplaceChild("eyes_closed", CubeListBuilder.create(), PartPose.offset(0.2F, -3.0F, 0.0F));

		PartDefinition cube_r10 = eyes_closed.addOrReplaceChild("cube_r10", CubeListBuilder.create().texOffs(-9, 32).addBox(-4.3F, 0.999F, -5.0F, 9.0F, 0.0F, 9.0F, new CubeDeformation(0.005F)), PartPose.offsetAndRotation(0.0F, -2.5F, 0.0F, 0.0F, 3.1416F, 0.0F));

		PartDefinition leaves = total.addOrReplaceChild("leaves", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition leaves1 = leaves.addOrReplaceChild("leaves1", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition cube_r11 = leaves1.addOrReplaceChild("cube_r11", CubeListBuilder.create().texOffs(0, 16).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, 0.2618F, 0.0F));

		PartDefinition leaves2 = leaves.addOrReplaceChild("leaves2", CubeListBuilder.create(), PartPose.offset(0.0F, -0.25F, 0.0F));

		PartDefinition cube_r12 = leaves2.addOrReplaceChild("cube_r12", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, 0.0F, -8.0F, 16.0F, 0.0F, 16.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.3491F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(Starfruit starfruit, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.total.getAllParts().forEach(ModelPart::resetPose);
		this.animate(starfruit.idleAnimationState, StarfruitAnimation.idle, ageInTicks);
		this.animate(starfruit.shootAnimationState, StarfruitAnimation.shoot, ageInTicks);
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