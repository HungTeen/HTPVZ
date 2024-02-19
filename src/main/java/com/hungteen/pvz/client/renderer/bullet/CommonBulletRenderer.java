package com.hungteen.pvz.client.renderer.bullet;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.client.model.bullet.CommonBulletModel;
import com.hungteen.pvz.common.entity.bullet.BaseBullet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
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
        if (PVZConfig.renderBulletAsModel()) {
            poseStack.scale(-1, -1, 1);
            final float size = bullet.getSize();
            poseStack.scale(size, size, size);
            poseStack.translate(0.0, -1.5, 0.0);
            VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(bullet)));
            this.model.setupAnim(bullet, 0, 0, bullet.tickCount + partialTicks, 0, 0);
            this.model.renderToBuffer(poseStack, vertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        } else {
            poseStack.scale(0.5F, 0.5F, 0.5F);
            poseStack.mulPose(this.entityRenderDispatcher.cameraOrientation());
            poseStack.mulPose(Vector3f.YP.rotationDegrees(180.0F));
            PoseStack.Pose posestack$pose = poseStack.last();
            Matrix4f matrix4f = posestack$pose.pose();
            Matrix3f matrix3f = posestack$pose.normal();
            VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.entityCutoutNoCull(this.getTextureLocation(bullet)));
            vertex(vertexconsumer, matrix4f, matrix3f, packedLightIn, 0.0F, 0, 0, 1);
            vertex(vertexconsumer, matrix4f, matrix3f, packedLightIn, 1.0F, 0, 1, 1);
            vertex(vertexconsumer, matrix4f, matrix3f, packedLightIn, 1.0F, 1, 1, 0);
            vertex(vertexconsumer, matrix4f, matrix3f, packedLightIn, 0.0F, 1, 0, 0);
        }
        poseStack.popPose();
        super.render(bullet, entityYaw, partialTicks, poseStack, buffer, packedLightIn);
    }

    private static void vertex(VertexConsumer p_114090_, Matrix4f p_114091_, Matrix3f p_114092_, int p_114093_, float p_114094_, int p_114095_, int p_114096_, int p_114097_) {
        p_114090_.vertex(p_114091_, p_114094_ - 0.5F, (float)p_114095_ - 0.25F, 0.0F).color(255, 255, 255, 255).uv((float)p_114096_, (float)p_114097_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_114093_).normal(p_114092_, 0.0F, 1.0F, 0.0F).endVertex();
    }

}