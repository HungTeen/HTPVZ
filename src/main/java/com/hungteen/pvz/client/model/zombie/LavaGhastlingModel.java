package com.hungteen.pvz.client.model.zombie;

import com.hungteen.pvz.common.entity.LavaGhastling;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class LavaGhastlingModel<T extends LavaGhastling> extends EntityModel<T> {
    private final ModelPart total;

    public LavaGhastlingModel(ModelPart root) {
        this.total = root.getChild("total");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create().texOffs(0, 17).addBox(-12.0F, -3.0F, 4.0F, 8.0F, 3.0F, 8.0F, new CubeDeformation(0.01F)), PartPose.offset(8.0F, 24.0F, -8.0F));

        PartDefinition up = total.addOrReplaceChild("up", CubeListBuilder.create().texOffs(0, 0).addBox(-6.0F, -7.5F, -6.0F, 12.0F, 5.0F, 12.0F, new CubeDeformation(0.02F))
                .texOffs(24, 17).addBox(-3.0F, -7.5F, -3.0F, 6.0F, 1.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(-8.0F, 0.0F, 8.0F));

        PartDefinition leave1 = up.addOrReplaceChild("leave1", CubeListBuilder.create(), PartPose.offset(4.0F, -8.5F, -3.5F));

        PartDefinition leaves_r1 = leave1.addOrReplaceChild("leaves_r1", CubeListBuilder.create().texOffs(3, 0).addBox(0.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, -0.3491F));

        PartDefinition leave2 = up.addOrReplaceChild("leave2", CubeListBuilder.create(), PartPose.offset(4.0F, -8.5F, -3.5F));

        PartDefinition leaves_r2 = leave2.addOrReplaceChild("leaves_r2", CubeListBuilder.create().texOffs(-1, 0).addBox(-2.0F, 0.0F, -0.5F, 2.0F, 0.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.0F, -0.7854F, 0.3491F));

        return LayerDefinition.create(meshdefinition, 64, 64);
    }
    @Override
    public void setupAnim(T flowerPot, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.total.getAllParts().forEach(ModelPart::resetPose);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        total.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

}
