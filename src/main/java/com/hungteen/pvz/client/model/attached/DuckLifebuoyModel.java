package com.hungteen.pvz.client.model.attached;
// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class DuckLifebuoyModel<T extends LivingEntity> extends HumanoidModel<T> {

	public DuckLifebuoyModel(ModelPart root) {
		super(root);
	}

	public static LayerDefinition createBodyLayer(CubeDeformation deformation) {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(deformation, 0);
		PartDefinition partdefinition = meshdefinition.getRoot();

		partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create(), PartPose.ZERO);
		partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create(), PartPose.ZERO);
		partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create(), PartPose.ZERO);
		partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create(), PartPose.ZERO);
		PartDefinition total = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, 7.0F, -6.0F, 12.0F, 4.0F, 12.0F, new CubeDeformation(0.0F))
				.texOffs(0, 16).addBox(-3.0F, 7.0F, -3.0F, 6.0F, 4.0F, 6.0F, new CubeDeformation(0.0F))
				.texOffs(20, 22).addBox(-2.0F, 3.5F, -7.005F, 4.0F, 7.0F, 4.0F, new CubeDeformation(0.0F))
				.texOffs(18, 16).addBox(-1.5F, 5.5F, -9.0F, 3.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 26.0F, 0.0F));

		PartDefinition cube_r1 = total.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(0, 26).addBox(-2.0F, -1.0F, -1.0F, 4.0F, 1.0F, 3.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 8.0F, 6.0F, 0.5236F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 64, 64);
	}
}