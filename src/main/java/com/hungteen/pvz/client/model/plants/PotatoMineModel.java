package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.9.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.client.model.plants.animation.PotatoMineAnimation;
import com.hungteen.pvz.common.entity.plants.PotatoMine;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class PotatoMineModel<T extends PotatoMine> extends HierarchicalModel<T> {
	private final ModelPart total;
	private final ModelPart light;

	public PotatoMineModel(ModelPart root) {
		this.total = root.getChild("total");
		this.light = total.getChild("antenna").getChild("top").getChild("light");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 25.0F, 0.0F));

		PartDefinition body = total.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -7.0F, -6.0F, 12.0F, 8.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition eye_closed = body.addOrReplaceChild("eye_closed", CubeListBuilder.create().texOffs(0, 30).addBox(-6.0F, -7.0F, -6.0F, 12.0F, 8.0F, 12.0F, new CubeDeformation(-0.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition antenna = total.addOrReplaceChild("antenna", CubeListBuilder.create().texOffs(40, 25).addBox(-3.0F, -7.5F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(0, 23).addBox(0.0F, -12.0F, -1.0F, 0.0F, 5.0F, 2.0F, new CubeDeformation(0.0F))
		.texOffs(14, 25).addBox(-1.0F, -12.0F, 0.0F, 2.0F, 5.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition top = antenna.addOrReplaceChild("top", CubeListBuilder.create().texOffs(48, 0).addBox(-2.0F, -14.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition light = top.addOrReplaceChild("light", CubeListBuilder.create().texOffs(48, 8).addBox(-2.0F, -14.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.001F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T potatoMine, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.total.getAllParts().forEach(ModelPart::resetPose);
		this.animate(potatoMine.idleAnimationState, PotatoMineAnimation.idle, ageInTicks);
		this.animate(potatoMine.outAnimationState, PotatoMineAnimation.out, ageInTicks);
		this.animate(potatoMine.sleepAnimationState, PotatoMineAnimation.sleep, 0);
		this.light.visible = potatoMine.getEntityData().get(PotatoMine.PREPARE_COUNT) <= 0 && ageInTicks % 50 < 4;
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