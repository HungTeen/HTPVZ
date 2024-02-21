package com.hungteen.pvz.client.model.armor;// Made with Blockbench 4.9.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.network.ClientProxy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
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

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class ButterHeadModel<T extends Entity> extends EntityModel<T> {
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
		if (model instanceof HierarchicalModel<?> && hasHead(((HierarchicalModel<?>) model).root())) {
			if (entity instanceof Phantom) poseStack.translate(0, 1.5, 0);//TODO find why and fix this.
			renderHead(((HierarchicalModel<?>) model).root(), main, poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		} else if (model instanceof HeadedModel model1) {
			if (entity instanceof Vex) poseStack.translate(0, 1.2, 0);//TODO find why and fix this.
			if (entity instanceof LivingEntity entity1 && entity1.isBaby()) poseStack.translate(0, 0.5, 0);//TODO find why and fix this.
			ModelPart head = model1.getHead();
			head.translateAndRotate(poseStack);
			poseStack.translate(0, - getBoneHeight(head) / 16, 0);
			main.compile(poseStack.last(), vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
			main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		} else if (model instanceof QuadrupedModel model1) {
			model1.head.translateAndRotate(poseStack);
			poseStack.translate(0, - getBoneHeight(model1.head) / 16, 0);
			main.compile(poseStack.last(), vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
			main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		} else {
			poseStack.translate(0, 1.5 - entity.getBbHeight(), 0);
			main.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		}
	}

	public boolean hasHead(ModelPart root) {
		for (String name: root.children.keySet()) {
			if (name.contains("head")) {
				return true;
			}
		}
		for (ModelPart part: root.children.values()) {
			if (hasHead(part)) {
				return true;
			}
		}
		return false;
	}

	public void renderHead(ModelPart root, ModelPart main, PoseStack stack,
						   VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		stack.pushPose();
		root.translateAndRotate(stack);
		for (String name: root.children.keySet()) {
			if (name.contains("head")) {
				stack.pushPose();
				root.getChild(name).translateAndRotate(stack);
				stack.translate(0, - getBoneHeight(root.getChild(name)) / 16 - 0.125, 0);
				main.compile(stack.last(), vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
				main.render(stack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
				stack.popPose();
			}
		}
		for (ModelPart part: root.children.values()) {
			renderHead(part, main, stack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
		}
		stack.popPose();
	}

	private float getBoneHeight(ModelPart part) {
		float result = 0;
		for (ModelPart.Cube cube : part.cubes) {
			result = Math.max(Math.max(cube.maxY, cube.maxY - cube.minY), result);
		}
		return result;
	}
}