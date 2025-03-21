package com.hungteen.pvz.client.renderer.bullet;

import com.hungteen.pvz.common.entity.Hook;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class HookRenderer extends EntityRenderer<Hook> {
    public static final ResourceLocation HOOK_LOCATION = Util.prefix("textures/entity/projectiles/hook.png");

    public HookRenderer(EntityRendererProvider.Context p_173917_) {
        super(p_173917_);
    }

    @Override
    public void render(Hook hook, float p_113840_, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int p_113844_) {
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
        if (hook.getOwner() != null) {
            renderLeash(hook, hook.getOwner(), partialTicks, poseStack, bufferSource);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(Hook p_114482_) {
        return HOOK_LOCATION;
    }

    public void vertex(Matrix4f p_113826_, Matrix3f p_113827_, VertexConsumer p_113828_, int p_113829_, int p_113830_, int p_113831_, float p_113832_, float p_113833_, int p_113834_, int p_113835_, int p_113836_, int p_113837_) {
        p_113828_.vertex(p_113826_, (float)p_113829_, (float)p_113830_, (float)p_113831_).color(255, 255, 255, 255).uv(p_113832_, p_113833_).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(p_113837_).normal(p_113827_, (float)p_113834_, (float)p_113836_, (float)p_113835_).endVertex();
    }

    private void renderLeash(Hook hook, Entity owner, float p_115463_, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();
        BlockPos tiedPosition = owner.blockPosition();
        double d0 = (double)(Mth.lerp(p_115463_, hook.yRotO, hook.yRot) * ((float)Math.PI / 180F)) + (Math.PI / 2D);
        Vec3 vec31 = hook.getLeashOffset();
        double d1 = Math.cos(d0) * vec31.z + Math.sin(d0) * vec31.x;
        double d2 = Math.sin(d0) * vec31.z - Math.cos(d0) * vec31.x;
        double d3 = Mth.lerp(p_115463_, hook.xo, hook.getX()) + d1;
        double d4 = Mth.lerp(p_115463_, hook.yo, hook.getY()) + vec31.y;
        double d5 = Mth.lerp(p_115463_, hook.zo, hook.getZ()) + d2;
        poseStack.translate(d1, vec31.y, d2);
        Vec3 vec3 = owner.position().add(0, owner.getEyeHeight(), 0);
        float f = (float)(vec3.x - d3);
        float f1 = (float)(vec3.y - d4);
        float f2 = (float)(vec3.z - d5);
        float f3 = 0.025F;
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.leash());
        Matrix4f matrix4f = poseStack.last().pose();
        float f4 = Mth.fastInvSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
        float f5 = f2 * f4;
        float f6 = f * f4;
        BlockPos blockpos = new BlockPos(hook.getEyePosition(p_115463_));
        int i = this.getBlockLightLevel(hook, blockpos);
        int j = hook.level.getBrightness(LightLayer.BLOCK, tiedPosition);
        int k = hook.level.getBrightness(LightLayer.SKY, blockpos);
        int l = hook.level.getBrightness(LightLayer.SKY, tiedPosition);

        for(int i1 = 0; i1 <= 24; ++i1) {
            addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.025F, f5, f6, i1, false);
        }

        for(int j1 = 24; j1 >= 0; --j1) {
            addVertexPair(vertexconsumer, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.0F, f5, f6, j1, true);
        }

        poseStack.popPose();
    }
    private static void addVertexPair(VertexConsumer vertexConsumer, Matrix4f matrix4f, float p_174310_, float p_174311_, float p_174312_, int p_174313_, int p_174314_, int p_174315_, int p_174316_, float p_174317_, float p_174318_, float p_174319_, float p_174320_, int p_174321_, boolean p_174322_) {
        float f = (float)p_174321_ / 24.0F;
        int i = (int)Mth.lerp(f, (float)p_174313_, (float)p_174314_);
        int j = (int)Mth.lerp(f, (float)p_174315_, (float)p_174316_);
        int k = LightTexture.pack(i, j);
        float f1 = p_174321_ % 2 == (p_174322_ ? 1 : 0) ? 0.7F : 1.0F;
        float f2 = 0.5F * f1;
        float f3 = 0.4F * f1;
        float f4 = 0.3F * f1;
        float f5 = p_174310_ * f;
        float f6 = p_174311_ > 0.0F ? p_174311_ * f * f : p_174311_ - p_174311_ * (1.0F - f) * (1.0F - f);
        float f7 = p_174312_ * f;
        vertexConsumer.vertex(matrix4f, f5 - p_174319_, f6 + p_174318_, f7 + p_174320_).color(f2, f3, f4, 1.0F).uv2(k).endVertex();
        vertexConsumer.vertex(matrix4f, f5 + p_174319_, f6 + p_174317_ - p_174318_, f7 - p_174320_).color(f2, f3, f4, 1.0F).uv2(k).endVertex();
    }
}
