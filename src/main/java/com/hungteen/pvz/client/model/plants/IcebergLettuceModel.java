package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.common.entity.plants.IcebergLettuce;
import com.hungteen.pvz.util.EntityUtil;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.block.Blocks;

public class IcebergLettuceModel<T extends IcebergLettuce> extends EntityModel<T> {
	// This layer location should be baked with EntityRendererProvider.Context in the entity renderer and passed into this model's constructor
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation("modid", "iceberglettucemodel"), "main");
	private final ModelPart total;
	private final ModelPart snow;

	public IcebergLettuceModel(ModelPart root) {
		this.total = root.getChild("total");
		this.snow = total.getChild("snow");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create().texOffs(24, 29).addBox(-3.0F, -0.5F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.001F))
				.texOffs(0, 24).addBox(-3.0F, -5.999F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(24, 0).addBox(-2.5F, -5.75F, -2.5F, 5.0F, 5.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition snow = total.addOrReplaceChild("snow", CubeListBuilder.create().texOffs(-12, 0).addBox(-6.0F, 0.5F, -6.0F, 12.0F, 0.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(-12, 12).addBox(-6.0F, 0.999F, -6.0F, 12.0F, 0.0F, 12.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -1.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}

	@Override
	public void setupAnim(T p_102618_, float p_102619_, float p_102620_, float p_102621_, float p_102622_, float p_102623_) {

	}

	@Override
	public void prepareMobModel(T entity, float p_102615_, float p_102616_, float partialTicks) {
		this.snow.yRot = - Mth.rotLerp(partialTicks, entity.yBodyRotO, entity.yBodyRot) / 57.3F;
		this.total.y = (EntityUtil.isLeavingGround(entity) || entity.level.getBlockState(entity.blockPosition()).getBlock() != Blocks.SNOW) ? 24 : 22F;
	}

	@Override
	public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
		total.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
	}
}