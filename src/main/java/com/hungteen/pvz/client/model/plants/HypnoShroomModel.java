package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.11.1
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.client.model.plants.animation.HypnoShroomAnimation;
import com.hungteen.pvz.common.entity.plants.HypnoShroom;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class HypnoShroomModel<T extends HypnoShroom> extends HierarchicalModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "hypnoshroommodel"), "main");
	private final ModelPart total;
	private final ModelPart bone;
	private final ModelPart reye;
	private final ModelPart leye;
	private final ModelPart hat;
	private final ModelPart lclosed;
	private final ModelPart rclosed;

	public HypnoShroomModel(ModelPart root) {
		this.total = root.getChild("total");
		this.bone = this.total.getChild("bone");
		this.reye = this.bone.getChild("reye");
		this.leye = this.bone.getChild("leye");
		this.hat = this.bone.getChild("hat");
		this.lclosed = this.bone.getChild("lclosed");
		this.rclosed = this.bone.getChild("rclosed");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition bone = total.addOrReplaceChild("bone", CubeListBuilder.create().texOffs(0, 32).addBox(-4.0F, -7.0F, -4.0F, 8.0F, 7.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition reye = bone.addOrReplaceChild("reye", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -4.0F, -4.25F));

		PartDefinition leye = bone.addOrReplaceChild("leye", CubeListBuilder.create().texOffs(0, 4).addBox(-2.0F, -2.0F, 0.0F, 4.0F, 4.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -4.0F, -4.25F));

		PartDefinition hat = bone.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -3.0F, -6.0F, 12.0F, 5.0F, 12.0F, new CubeDeformation(0.0F))
		.texOffs(0, 17).addBox(-5.0F, -8.0F, -5.0F, 10.0F, 5.0F, 10.0F, new CubeDeformation(0.0F))
		.texOffs(30, 17).addBox(-3.0F, -11.0F, -3.0F, 6.0F, 3.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));

		PartDefinition lclosed = bone.addOrReplaceChild("lclosed", CubeListBuilder.create().texOffs(0, 9).addBox(-2.0F, 0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(3.0F, -4.0F, -4.25F));

		PartDefinition rclosed = bone.addOrReplaceChild("rclosed", CubeListBuilder.create().texOffs(0, 8).addBox(-2.0F, 0.5F, 0.0F, 4.0F, 1.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-3.0F, -4.0F, -4.25F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(HypnoShroom hypnoShroom, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.total.getAllParts().forEach(ModelPart::resetPose);
		this.animate(hypnoShroom.idleAnimationState, HypnoShroomAnimation.idle, ageInTicks);
		this.animate(hypnoShroom.sleepAnimationState, HypnoShroomAnimation.sleep, ageInTicks);
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