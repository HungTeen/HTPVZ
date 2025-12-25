package com.hungteen.pvz.client.model.plants;

import com.hungteen.pvz.common.entity.plants.WallNut;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class WallNutArmorModel<T extends WallNut> extends EntityModel<T> {
    private final ModelPart total;
    private final ModelPart body;

    public WallNutArmorModel(ModelPart root) {
        this.total = root.getChild("total");
        this.body = total.getChild("body");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create(), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition body = total.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0).addBox(-8.0F, -7.0F, -8.0F, 16.0F, 13.0F, 16.0F, new CubeDeformation(0.0F))
                .texOffs(0, 29).addBox(-7.0F, -10.0F, -7.0F, 14.0F, 3.0F, 14.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, -8.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }


    @Override
    public void setupAnim(T wallnut, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (wallnut.isBowling()) {
            this.body.xRot = (float) ((wallnut.getDeltaMovement().z * Math.cos(wallnut.yRot / 57.3) + wallnut.getDeltaMovement().x * Math.sin(wallnut.yRot / 57.3)) * wallnut.tickCount);
            this.body.zRot = (float) ((wallnut.getDeltaMovement().x * Math.cos(wallnut.yRot / 57.3) + wallnut.getDeltaMovement().z * Math.sin(wallnut.yRot / 57.3)) * wallnut.tickCount);
        } else {
            this.body.xRot = 0;
            this.body.zRot = 0;
        }
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        total.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
