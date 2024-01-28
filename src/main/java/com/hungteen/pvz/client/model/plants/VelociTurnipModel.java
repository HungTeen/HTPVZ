package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.9.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.client.model.plants.animation.VelociTurnipAnimation;
import com.hungteen.pvz.common.entity.plants.VelociTurnip;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class VelociTurnipModel<T extends VelociTurnip> extends HierarchicalModel<T> {
	private final ModelPart total;
	private final ModelPart root;

	public VelociTurnipModel(ModelPart root) {
		this.total = root.getChild("total");
		this.root = root;
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create().texOffs(0, 0).addBox(-4.0F, -6.0F, -4.0F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(30, 24).addBox(-2.5F, -6.0F, -5.5F, 5.0F, 2.0F, 2.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition tail = total.addOrReplaceChild("tail", CubeListBuilder.create().texOffs(24, 0).addBox(-1.0F, -1.0F, 0.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 4.0F));

		PartDefinition tailtop = tail.addOrReplaceChild("tailtop", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, 0.0F, 0.0F, 2.0F, 0.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -0.5F, 4.0F));

		PartDefinition head = total.addOrReplaceChild("head", CubeListBuilder.create().texOffs(24, 6).addBox(-4.0F, -2.0F, -8.0F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.01F))
		.texOffs(0, 25).addBox(-4.0F, -4.0F, -6.0F, 8.0F, 2.0F, 5.0F, new CubeDeformation(0.01F))
		.texOffs(4, 18).addBox(-4.0F, -3.0F, -10.0F, 8.0F, 3.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -5.0F, 4.0F));

		PartDefinition leaves = head.addOrReplaceChild("leaves", CubeListBuilder.create(), PartPose.offset(0.0F, -4.0F, 0.0F));

		PartDefinition leaf1 = leaves.addOrReplaceChild("leaf1", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition cube_r1 = leaf1.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 32).addBox(-3.0F, -10.0F, 0.0F, 6.0F, 9.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 1.2654F, 0.0F, 0.0F));

		PartDefinition leaf2 = leaves.addOrReplaceChild("leaf2", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 0.0F));

		PartDefinition cube_r2 = leaf2.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(16, 38).addBox(-3.0F, -9.0F, -1.0F, 6.0F, 10.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, -1.0036F, 0.0F, 0.0F));

		PartDefinition leaf3 = leaves.addOrReplaceChild("leaf3", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, -1.0F));

		PartDefinition cube_r3 = leaf3.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(26, 28).addBox(-3.0F, 0.0F, 0.0F, 6.0F, 8.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.6981F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.total.getAllParts().forEach(ModelPart::resetPose);
		this.animate(entity.idleAnimationState, VelociTurnipAnimation.idle, ageInTicks);
		this.animate(entity.moveAnimationState, VelociTurnipAnimation.move, ageInTicks);
		this.animate(entity.attackAnimationState, VelociTurnipAnimation.attack, ageInTicks);
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		total.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return root;
	}
}