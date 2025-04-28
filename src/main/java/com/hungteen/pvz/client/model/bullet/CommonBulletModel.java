package com.hungteen.pvz.client.model.bullet;

import com.hungteen.pvz.common.entity.bullet.BaseBullet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class CommonBulletModel<T extends BaseBullet> extends EntityModel<T> {
    private final ModelPart total;

    public CommonBulletModel(ModelPart root) {
        this.total = root.getChild("total");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition total = partdefinition.addOrReplaceChild("total", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.0F))
                .texOffs(0, 8).addBox(-2.0F, -4.0F, -2.0F, 4.0F, 4.0F, 4.0F, new CubeDeformation(0.5F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 16, 16);
    }
    @Override
    public void setupAnim(T bullet, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.total.xRot = (float) (bullet.getXRot() / 57.3);
        this.total.yRot = (float) (bullet.getYRot() / 57.3);
        this.total.y = 24 - (float) (Math.sin(bullet.getXRot() / 57.3) * 0.5 * bullet.getSize());
        this.total.x = - (float) (Math.sin(bullet.getYRot() / 57.3) * 0.5 * bullet.getSize());
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer buffer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        total.render(poseStack, buffer, packedLight, packedOverlay);
    }
}