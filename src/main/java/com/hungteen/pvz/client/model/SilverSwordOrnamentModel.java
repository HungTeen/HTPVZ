package com.hungteen.pvz.client.model;// Made with Blockbench 4.12.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.common.block.entity.SilverSwordOrnamentBlockEntity;
import com.hungteen.pvz.common.network.ClientProxy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;

public class SilverSwordOrnamentModel<T extends Entity> extends HierarchicalModel<T> {
	private final ModelPart total;

	public SilverSwordOrnamentModel(ModelPart root) {
		this.total = root.getChild("total");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();
		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.ZERO);

		PartDefinition root = total.addOrReplaceChild("root", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition body = root.addOrReplaceChild("body", CubeListBuilder.create().texOffs(18, 34).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 14).addBox(-4.0F, -0.5F, -3.0F, 8.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition needle = body.addOrReplaceChild("needle", CubeListBuilder.create().texOffs(12, 19).mirror().addBox(0.0F, -2.0F, -3.0F, 0.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.0F, -0.5F, 0.0F, 0.0F, 0.0F, -0.7854F));

		PartDefinition needle2 = body.addOrReplaceChild("needle2", CubeListBuilder.create().texOffs(12, 19).addBox(0.0F, -2.0F, -3.0F, 0.0F, 2.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -0.5F, 0.0F, 0.0F, 0.0F, 0.7854F));

		PartDefinition needle3 = body.addOrReplaceChild("needle3", CubeListBuilder.create().texOffs(28, 22).addBox(-4.0F, -2.0F, 0.0F, 8.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 4.0F, -0.7854F, 0.0F, 0.0F));

		PartDefinition body2 = body.addOrReplaceChild("body2", CubeListBuilder.create().texOffs(33, 12).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 7).addBox(-4.0F, -0.5F, -2.75F, 8.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));

		PartDefinition needle5 = body2.addOrReplaceChild("needle5", CubeListBuilder.create().texOffs(14, 14).addBox(0.0F, -5.0F, -2.75F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -0.5F, 0.0F, 0.0F, 0.0F, 0.4363F));

		PartDefinition needle4 = body2.addOrReplaceChild("needle4", CubeListBuilder.create().texOffs(14, 14).mirror().addBox(0.0F, -5.0F, -2.75F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.0F, -0.5F, 0.0F, 0.0F, 0.0F, -0.4363F));

		PartDefinition needle6 = body2.addOrReplaceChild("needle6", CubeListBuilder.create().texOffs(28, 17).addBox(-4.0F, -5.0F, 0.0F, 8.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 4.25F, -0.4363F, 0.0F, 0.0F));

		PartDefinition needle7 = body2.addOrReplaceChild("needle7", CubeListBuilder.create().texOffs(28, 17).addBox(-4.0F, -5.0F, 0.0F, 8.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, -2.75F, 0.0436F, 0.0F, 0.0F));

		PartDefinition body3 = body2.addOrReplaceChild("body3", CubeListBuilder.create().texOffs(12, 33).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(0, 0).addBox(-4.0F, -0.5F, -2.5F, 8.0F, 0.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));

		PartDefinition needle9 = body3.addOrReplaceChild("needle9", CubeListBuilder.create().texOffs(0, 14).addBox(0.0F, -5.0F, -2.5F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(4.0F, -0.5F, 0.0F, 0.0F, 0.0F, 0.2618F));

		PartDefinition needle8 = body3.addOrReplaceChild("needle8", CubeListBuilder.create().texOffs(0, 14).mirror().addBox(0.0F, -5.0F, -2.5F, 0.0F, 5.0F, 7.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-4.0F, -0.5F, 0.0F, 0.0F, 0.0F, -0.2618F));

		PartDefinition needle10 = body3.addOrReplaceChild("needle10", CubeListBuilder.create().texOffs(26, 26).addBox(-4.0F, -5.0F, 0.0F, 8.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 4.5F, -0.2618F, 0.0F, 0.0F));

		PartDefinition body4 = body3.addOrReplaceChild("body4", CubeListBuilder.create().texOffs(33, 9).addBox(-1.0F, -1.0F, -1.5F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(17, 0).addBox(-3.5F, -0.5F, -2.25F, 7.0F, 0.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.25F));

		PartDefinition needle12 = body4.addOrReplaceChild("needle12", CubeListBuilder.create().texOffs(0, 20).addBox(0.0F, -5.0F, -2.0F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(3.5F, -0.5F, -0.25F));

		PartDefinition needle11 = body4.addOrReplaceChild("needle11", CubeListBuilder.create().texOffs(0, 20).mirror().addBox(0.0F, -5.0F, -2.25F, 0.0F, 5.0F, 6.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offset(-3.5F, -0.5F, 0.0F));

		PartDefinition needle13 = body4.addOrReplaceChild("needle13", CubeListBuilder.create().texOffs(12, 28).addBox(-3.0F, -5.0F, 0.0F, 7.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, -0.5F, 3.75F));

		PartDefinition needle14 = body4.addOrReplaceChild("needle14", CubeListBuilder.create().texOffs(12, 28).addBox(-3.0F, -5.0F, -0.25F, 7.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-0.5F, -0.5F, -2.0F));

		PartDefinition body5 = body4.addOrReplaceChild("body5", CubeListBuilder.create().texOffs(33, 6).addBox(-1.0F, -1.0F, -1.0F, 2.0F, 1.0F, 2.0F, new CubeDeformation(0.0F))
				.texOffs(18, 6).addBox(-2.5F, -0.5F, -1.25F, 5.0F, 0.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, -0.5F));

		PartDefinition needle16 = body5.addOrReplaceChild("needle16", CubeListBuilder.create().texOffs(23, 6).addBox(0.0F, -6.0F, -2.75F, 0.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.5F, -0.5F, 1.5F, 0.0F, 0.0F, -0.1309F));

		PartDefinition needle15 = body5.addOrReplaceChild("needle15", CubeListBuilder.create().texOffs(23, 6).mirror().addBox(0.0F, -6.0F, -2.25F, 0.0F, 6.0F, 5.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(-2.5F, -0.5F, 1.0F, 0.0F, 0.0F, 0.1309F));

		PartDefinition needle17 = body5.addOrReplaceChild("needle17", CubeListBuilder.create().texOffs(26, 31).addBox(-2.5F, -6.0F, 0.0F, 5.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -0.5F, 3.75F, 0.1309F, 0.0F, 0.0F));

		PartDefinition needle18 = body5.addOrReplaceChild("needle18", CubeListBuilder.create().texOffs(26, 31).mirror().addBox(-2.5F, -6.0F, 0.0F, 5.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)).mirror(false), PartPose.offsetAndRotation(0.0F, -0.5F, -1.25F, -0.1309F, 0.0F, 0.0F));

		PartDefinition face = root.addOrReplaceChild("face", CubeListBuilder.create(), PartPose.offset(0.0F, -2.0F, -3.25F));

		PartDefinition eyes = face.addOrReplaceChild("eyes", CubeListBuilder.create(), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition eye = eyes.addOrReplaceChild("eye", CubeListBuilder.create().texOffs(0, 4).addBox(-1.0F, -1.5F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.6269F, -1.4071F, 0.5F, 0.0F, 0.0F, -0.0873F));

		PartDefinition eyeball = eye.addOrReplaceChild("eyeball", CubeListBuilder.create().texOffs(3, 9).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.5F, 0.0F, -0.125F, 0.0F, 0.0F, 0.0873F));

		PartDefinition eye2 = eyes.addOrReplaceChild("eye2", CubeListBuilder.create().texOffs(0, 0).addBox(-1.0F, -1.5F, -0.5F, 2.0F, 3.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.6269F, -1.4071F, 0.5F, 0.0F, 0.0F, 0.0873F));

		PartDefinition eyeball2 = eye2.addOrReplaceChild("eyeball2", CubeListBuilder.create().texOffs(0, 8).addBox(-0.5F, -0.5F, -0.5F, 1.0F, 1.0F, 1.0F, new CubeDeformation(-0.1F)), PartPose.offsetAndRotation(0.5F, -0.5F, -0.125F, 0.0F, 0.0F, -0.0873F));

		PartDefinition eyebrow = eyes.addOrReplaceChild("eyebrow", CubeListBuilder.create().texOffs(36, 31).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.7073F, -2.9554F, -0.25F, -0.0076F, -0.043F, 0.2183F));

		PartDefinition eyebrow2 = eyes.addOrReplaceChild("eyebrow2", CubeListBuilder.create().texOffs(36, 24).addBox(-2.0F, -1.0F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(1.3573F, -3.7456F, -0.25F, 0.0F, 0.0873F, -0.6109F));

		PartDefinition musk = face.addOrReplaceChild("musk", CubeListBuilder.create().texOffs(33, 15).addBox(-1.7652F, -0.1736F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-2.0F, 0.0F, -0.075F, 0.0F, 0.0873F, 0.1745F));

		PartDefinition musk2 = face.addOrReplaceChild("musk2", CubeListBuilder.create().texOffs(28, 24).addBox(-2.2348F, -0.1736F, 0.0F, 4.0F, 2.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(2.0F, 0.0F, -0.075F, 0.0F, -0.0873F, -0.1745F));

		PartDefinition neddles = face.addOrReplaceChild("neddles", CubeListBuilder.create().texOffs(0, 31).addBox(-3.0F, -3.0F, 0.0F, 6.0F, 6.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 2.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);

	}

	@Override
	public void setupAnim(Entity nuoo, float x, float y, float ageInTicks, float z, float headPitch) {
		Level level = ClientProxy.getLevel();
		if (level.getBlockEntity(new BlockPos(x, y, z)) instanceof SilverSwordOrnamentBlockEntity entity) {
			this.total.getAllParts().forEach(ModelPart::resetPose);
			this.animate(entity.idleAnimationState, SilverSwordOrnamentAnimation.idle, ageInTicks);
			this.animate(entity.attackAnimationState, SilverSwordOrnamentAnimation.attack, ageInTicks);
		}
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