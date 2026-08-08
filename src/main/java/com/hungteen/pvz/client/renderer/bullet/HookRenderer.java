package com.hungteen.pvz.client.renderer.bullet;

import com.hungteen.pvz.common.entity.Hook;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class HookRenderer<T extends Hook> extends EntityRenderer<T> {
    public static final ResourceLocation HOOK_LOCATION = Util.prefix("textures/entity/projectiles/hook.png");

    public HookRenderer(EntityRendererProvider.Context p_173917_) {
        super(p_173917_);
    }

    @Override
    public boolean shouldRender(T p_114491_, Frustum p_114492_, double p_114493_, double p_114494_, double p_114495_) {
        return true;
    }

    @Override
    public void render(T hook, float p_113840_, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int p_113844_) {
        poseStack.pushPose();
        poseStack.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(partialTicks, hook.yRotO, hook.getYRot()) - 90.0F));
        poseStack.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(partialTicks, hook.xRotO, hook.getXRot())));
        poseStack.mulPose(Vector3f.XP.rotationDegrees(45.0F));
        poseStack.scale(0.05625F, 0.05625F, 0.05625F);
        poseStack.translate(-4.0D, 0.0D, 0.0D);
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityCutout(this.getTextureLocation(hook)));
        PoseStack.Pose posestack$pose = poseStack.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();
        this.vertex(matrix4f, matrix3f, vertexconsumer, -2, -2, -2, 0.0F, 0.3125F, -1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, -2, -2, 2, 0.125F, 0.3125F, -1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, -2, 2, 2, 0.125F, 0.4375F, -1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, -2, 2, -2, 0.0F, 0.4375F, -1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, -2, 2, -2, 0.0F, 0.3125F, 1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, -2, 2, 2, 0.125F, 0.3125F, 1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, -2, -2, 2, 0.125F, 0.4375F, 1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, -2, -2, -2, 0.0F, 0.4375F, 1, 0, 0, p_113844_);
//

        for(int j = 0; j < 4; ++j) {
            poseStack.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            this.vertex(matrix4f, matrix3f, vertexconsumer, -6, -5, 0, 0.0F, 0.0F, 0, 1, 0, p_113844_);
            this.vertex(matrix4f, matrix3f, vertexconsumer, 6, -5, 0, 0.375F, 0.0F, 0, 1, 0, p_113844_);
            this.vertex(matrix4f, matrix3f, vertexconsumer, 6, 5, 0, 0.375F, 0.3125F, 0, 1, 0, p_113844_);
            this.vertex(matrix4f, matrix3f, vertexconsumer, -6, 5, 0, 0.0F, 0.3125F, 0, 1, 0, p_113844_);
        }
        poseStack.popPose();
        super.render(hook, p_113840_, partialTicks, poseStack, bufferSource, p_113844_);
    }

    @Override
    public ResourceLocation getTextureLocation(T p_114482_) {
        return HOOK_LOCATION;
    }

    public void vertex(Matrix4f p_113826_, Matrix3f p_113827_, VertexConsumer p_113828_, int p_113829_, int p_113830_, int p_113831_, float p_113832_, float p_113833_, int p_113834_, int p_113835_, int p_113836_, int p_113837_) {
        p_113828_.vertex(p_113826_, (float)p_113829_, (float)p_113830_, (float)p_113831_).color(255, 255, 255, 255).uv(p_113832_, p_113833_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_113837_).normal(p_113827_, (float)p_113834_, (float)p_113836_, (float)p_113835_).endVertex();
    }

}
