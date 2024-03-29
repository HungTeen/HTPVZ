package com.hungteen.pvz.client.model.attached;// Made with Blockbench 4.9.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.hungteen.pvz.common.network.ClientProxy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Phantom;
import net.minecraft.world.entity.monster.Vex;

public class ButterHeadModel<T extends Entity> extends HierarchicalModel<T> {
	private final ModelPart main;
	private EntityModel model;
	private Entity entity;

	public ButterHeadModel(ModelPart root) {
		this.main = root.getChild("main");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition main = partdefinition.addOrReplaceChild("main", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -1.0F, -4.5F, 9.0F, 8.0F, 9.0F, new CubeDeformation(0.01F))
				.texOffs(0, 17).addBox(-4.5F, -0.75F, -4.5F, 9.0F, 8.0F, 9.0F, new CubeDeformation(-0.2F)), PartPose.offset(0.0F, 0F, 0.0F));

		PartDefinition cube_r1 = main.addOrReplaceChild("drop", CubeListBuilder.create().texOffs(0, 34).addBox(-4.0F, -4.0F, -4.25F, 8.0F, 4.0F, 8.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.2182F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		EntityRenderer<?> renderer = ClientProxy.MC.getEntityRenderDispatcher().renderers.get(entity.getType());
		model = null;
		this.entity = entity;
		if (renderer instanceof LivingEntityRenderer renderer1) {
			model = renderer1.getModel();
		}
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}

	@Override
	public ModelPart root() {
		return main;
	}
}