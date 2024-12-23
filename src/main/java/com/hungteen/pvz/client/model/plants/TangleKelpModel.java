package com.hungteen.pvz.client.model.plants;// Made with Blockbench 4.9.4
// Exported for Minecraft version 1.17 or later with Mojang mappings
// Paste this class into your mod and generate all required imports


import com.hungteen.pvz.common.entity.plants.TangleKelp;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class TangleKelpModel<T extends TangleKelp> extends HierarchicalModel<T> {
	private final ModelPart total;
	private final ModelPart c1;
	private final ModelPart c2;
	private final ModelPart f1;
	private final ModelPart f2;
	private final ModelPart f3;
	private final ModelPart f4;

	public TangleKelpModel(ModelPart root) {
		this.total = root.getChild("total");
		this.c1 = total.getChild("c1");
		this.c2 = total.getChild("c2");
		this.f1 = total.getChild("f1");
		this.f2 = total.getChild("f2");
		this.f3 = total.getChild("f3");
		this.f4 = total.getChild("f4");
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = new MeshDefinition();
		PartDefinition partdefinition = meshdefinition.getRoot();

		PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create().texOffs(94, 0).addBox(-4.5F, -8.0F, -4.0F, 9.0F, 8.0F, 8.0F, new CubeDeformation(0.0F))
		.texOffs(104, 28).addBox(-3.0F, -14.0F, -3.0F, 6.0F, 14.0F, 6.0F, new CubeDeformation(0.0F))
		.texOffs(104, 16).addBox(-3.0F, -2.0F, -3.0F, 6.0F, 6.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

		PartDefinition c1 = total.addOrReplaceChild("c1", CubeListBuilder.create().texOffs(0, 0).addBox(-5.5F, -18.0F, -5.0F, 11.0F, 19.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition c2 = total.addOrReplaceChild("c2", CubeListBuilder.create().texOffs(0, 29).addBox(-5.5F, -18.0F, -5.0F, 11.0F, 19.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition f1 = total.addOrReplaceChild("f1", CubeListBuilder.create().texOffs(54, -10).addBox(0.0F, -22.0F, -5.0F, 0.0F, 16.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition n_r1 = f1.addOrReplaceChild("n_r1", CubeListBuilder.create().texOffs(48, 0).addBox(-8.0F, -11.0F, 0.0F, 16.0F, 22.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition s_r1 = f1.addOrReplaceChild("s_r1", CubeListBuilder.create().texOffs(48, 44).addBox(-8.0F, -11.0F, 0.0F, 16.0F, 22.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition f2 = total.addOrReplaceChild("f2", CubeListBuilder.create().texOffs(54, 12).addBox(0.0F, -22.0F, -5.0F, 0.0F, 16.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition n_r2 = f2.addOrReplaceChild("n_r2", CubeListBuilder.create().texOffs(48, 22).addBox(-8.0F, -11.0F, 0.0F, 16.0F, 22.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition s_r2 = f2.addOrReplaceChild("s_r2", CubeListBuilder.create().texOffs(48, 66).addBox(-8.0F, -11.0F, 0.0F, 16.0F, 22.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition f3 = total.addOrReplaceChild("f3", CubeListBuilder.create().texOffs(54, 34).addBox(0.0F, -22.0F, -5.0F, 0.0F, 16.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition n_r3 = f3.addOrReplaceChild("n_r3", CubeListBuilder.create().texOffs(48, 44).addBox(-8.0F, -11.0F, 0.0F, 16.0F, 22.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition s_r3 = f3.addOrReplaceChild("s_r3", CubeListBuilder.create().texOffs(48, 0).addBox(-8.0F, -11.0F, 0.0F, 16.0F, 22.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		PartDefinition f4 = total.addOrReplaceChild("f4", CubeListBuilder.create().texOffs(54, 56).addBox(0.0F, -22.0F, -5.0F, 0.0F, 16.0F, 10.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));

		PartDefinition n_r4 = f4.addOrReplaceChild("n_r4", CubeListBuilder.create().texOffs(48, 66).addBox(-8.0F, -11.0F, 0.0F, 16.0F, 22.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, -0.4363F, 0.0F, 0.0F));

		PartDefinition s_r4 = f4.addOrReplaceChild("s_r4", CubeListBuilder.create().texOffs(48, 22).addBox(-8.0F, -11.0F, 0.0F, 16.0F, 22.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.0F, 0.0F, 0.4363F, 0.0F, 0.0F));

		return LayerDefinition.create(meshdefinition, 128, 128);
	}

	@Override
	public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
		this.c1.visible = entity.tickCount % 12 > 6;
		this.c2.visible = ! c1.visible;
		this.f1.visible = false;
		this.f2.visible = false;
		this.f3.visible = false;
		this.f4.visible = false;
		if (entity.tickCount % 12 < 3) {
			this.f1.visible = true;
		}
		else if (entity.tickCount % 12 < 6) {
			this.f2.visible = true;
		}
		else if (entity.tickCount % 12 < 9) {
			this.f3.visible = true;
		}
		else {
			this.f4.visible = true;
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