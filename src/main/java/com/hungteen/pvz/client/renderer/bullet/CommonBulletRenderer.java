package com.hungteen.pvz.client.renderer.bullet;

import com.hungteen.pvz.client.model.bullet.CommonBulletModel;
import com.hungteen.pvz.common.entity.bullet.BaseBullet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;

public abstract class CommonBulletRenderer <T extends BaseBullet> extends EntityRenderer<T>{

    private final CommonBulletModel<BaseBullet> model;
    public CommonBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CommonBulletModel<>(context.bakeLayer(CommonBulletModel.LAYER_LOCATION));
    }

    public void render(T bullet, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLightIn) {
        poseStack.pushPose();
        poseStack.scale(-1, -1, 1);
        final float size = bullet.getSize();
        poseStack.scale(size, size, size);
        poseStack.translate(0.0, -1.5, 0.0);
        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(bullet)));
        this.model.setupAnim(bullet, 0, 0, bullet.tickCount + partialTicks, 0, 0);
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();

        super.render(bullet, entityYaw, partialTicks, poseStack, buffer, packedLightIn);
    }

}