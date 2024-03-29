package com.hungteen.pvz.client.renderer.bullet;

import com.hungteen.pvz.common.entity.bullet.ArrowWithATarget;
import com.hungteen.pvz.common.entity.bullet.SeedArrow;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix3f;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ArrowRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public class ArrowWithATargetRenderer extends ArrowRenderer<ArrowWithATarget> {
    public static final ResourceLocation ARROW_WITH_A_TARGET_LOCATION = Util.prefix("textures/entity/projectiles/arrow_with_a_target.png");

    public ArrowWithATargetRenderer(EntityRendererProvider.Context p_173917_) {
        super(p_173917_);
    }
    public void render(ArrowWithATarget p_113839_, float p_113840_, float p_113841_, PoseStack p_113842_, MultiBufferSource p_113843_, int p_113844_) {
        p_113842_.pushPose();
        p_113842_.mulPose(Vector3f.YP.rotationDegrees(Mth.lerp(p_113841_, p_113839_.yRotO, p_113839_.getYRot()) - 90.0F));
        p_113842_.mulPose(Vector3f.ZP.rotationDegrees(Mth.lerp(p_113841_, p_113839_.xRotO, p_113839_.getXRot())));
        int i = 0;
        float f = 0.0F;
        float f1 = 0.5F;
        float f2 = 0.0F;
        float f3 = 0.15625F;
        float f4 = 0.0F;
        float f5 = 0.15625F;
        float f6 = 0.15625F;
        float f7 = 0.3125F;
        float f8 = 0.05625F;
        float lt_y = 10 / 32F;
        float lt_x = 0;
        float rb_y = 24 / 32F;
        float rb_x = 14 / 32F;
        float f9 = (float)p_113839_.shakeTime - p_113841_;
        if (f9 > 0.0F) {
            float f10 = -Mth.sin(f9 * 3.0F) * f9;
            p_113842_.mulPose(Vector3f.ZP.rotationDegrees(f10));
        }

        p_113842_.mulPose(Vector3f.XP.rotationDegrees(45.0F));
        p_113842_.scale(0.05625F, 0.05625F, 0.05625F);
        p_113842_.translate(-4.0D, 0.0D, 0.0D);
        VertexConsumer vertexconsumer = p_113843_.getBuffer(RenderType.entityCutout(this.getTextureLocation(p_113839_)));
        PoseStack.Pose posestack$pose = p_113842_.last();
        Matrix4f matrix4f = posestack$pose.pose();
        Matrix3f matrix3f = posestack$pose.normal();
        this.vertex(matrix4f, matrix3f, vertexconsumer, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, p_113844_);

        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, -7, -7, lt_x, lt_y, -1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, -7, 7, rb_x, lt_y, -1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, 7, 7, rb_x, rb_y, -1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, 7, -7, lt_x, rb_y, -1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, 7, -7, lt_x, lt_y, 1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, 7, 7, rb_x, lt_y, 1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, -7, 7, rb_x, rb_y, 1, 0, 0, p_113844_);
        this.vertex(matrix4f, matrix3f, vertexconsumer, 4, -7, -7, lt_x, rb_y, 1, 0, 0, p_113844_);

        for(int j = 0; j < 4; ++j) {
            p_113842_.mulPose(Vector3f.XP.rotationDegrees(90.0F));
            this.vertex(matrix4f, matrix3f, vertexconsumer, -8, -2, 0, 0.0F, 0.0F, 0, 1, 0, p_113844_);
            this.vertex(matrix4f, matrix3f, vertexconsumer, 8, -2, 0, 0.5F, 0.0F, 0, 1, 0, p_113844_);
            this.vertex(matrix4f, matrix3f, vertexconsumer, 8, 2, 0, 0.5F, 0.15625F, 0, 1, 0, p_113844_);
            this.vertex(matrix4f, matrix3f, vertexconsumer, -8, 2, 0, 0.0F, 0.15625F, 0, 1, 0, p_113844_);
        }

        p_113842_.popPose();
        super.render(p_113839_, p_113840_, p_113841_, p_113842_, p_113843_, p_113844_);
    }
    @Override
    public ResourceLocation getTextureLocation(ArrowWithATarget p_114482_) {
        return ARROW_WITH_A_TARGET_LOCATION;
    }
}
