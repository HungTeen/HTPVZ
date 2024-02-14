package com.hungteen.pvz.client.model.armor;
// Made with Blockbench 4.9.3
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class BucketHelmetModel<T extends LivingEntity> extends HumanoidModel<T> {
	public BucketHelmetModel(ModelPart root) {
		super(root);
	}

	public static LayerDefinition createBodyLayer(CubeDeformation deformation) {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(deformation, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();

		final PartDefinition head = partdefinition.getChild("head");
		PartDefinition total = head.addOrReplaceChild("total", CubeListBuilder.create().texOffs(0, 39).addBox(-4.5F, -2.0F, -4.5F, 9.0F, 8.0F, 9.0F, new CubeDeformation(0.0F))
				.texOffs(32, 48).addBox(-4.0F, -10.0F, -4.0F, 8.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -7.0F, 0.0F));

		PartDefinition break_point_r1 = total.addOrReplaceChild("break_point_r1", CubeListBuilder.create().texOffs(29, 34).addBox(-4.0F, -9.5F, -3.562F, 8.0F, 6.0F, 8.0F, new CubeDeformation(0.01F)), PartPose.offsetAndRotation(0.005F, 0.045F, 0.0512F, 0.1309F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}
}