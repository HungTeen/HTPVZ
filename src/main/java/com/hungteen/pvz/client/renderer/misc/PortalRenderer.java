package com.hungteen.pvz.client.renderer.misc;

import com.hungteen.pvz.common.entity.Portal;
import com.hungteen.pvz.util.Util;
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
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class PortalRenderer extends EntityRenderer<Portal> {

    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/portal/portal.png");
    private static final int ANIM_LENGTH = 5;

    public PortalRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0;
    }

    @Override
    public void render(Portal portal, float entityYaw, float partialTicks, PoseStack stack, MultiBufferSource buffer, int packedLight) {
        stack.pushPose();
        stack.translate(0, Portal.HEIGHT / 2, 0);
        Vec3 n = portal.getPlaneNormal();
        stack.mulPose(Vector3f.YP.rotationDegrees((float) (180 - (Mth.atan2(- n.x, n.z) * Mth.RAD_TO_DEG))));
        float scale = 1;
        if (portal.getState() == 0) scale = Math.min(1.0F, (float) portal.animTick / ANIM_LENGTH);
        if (portal.getState() == 2) scale = Math.max(0.0F, (float) portal.animTick / ANIM_LENGTH);
        if (scale <= 0) {
            stack.popPose();
            return;
        }
        stack.scale(1, scale, 1);

        VertexConsumer consumer = buffer.getBuffer(RenderType.endPortal());
        PoseStack.Pose pose = stack.last();
        Matrix4f matrix = pose.pose();
        Matrix3f normal = pose.normal();
        float w = Portal.WIDTH / 2F;
        float h = Portal.HEIGHT / 2F;
        vertex(consumer, matrix, normal, -w, -h, 0, 0, 1, packedLight);
        vertex(consumer, matrix, normal,  w, -h, 0, 1, 1, packedLight);
        vertex(consumer, matrix, normal,  w,  h, 0, 1, 0, packedLight);
        vertex(consumer, matrix, normal, -w,  h, 0, 0, 0, packedLight);
        vertex(consumer, matrix, normal,  w, -h, 0, 0, 1, packedLight);
        vertex(consumer, matrix, normal, -w, -h, 0, 1, 1, packedLight);
        vertex(consumer, matrix, normal, -w,  h, 0, 1, 0, packedLight);
        vertex(consumer, matrix, normal,  w,  h, 0, 0, 0, packedLight);
        VertexConsumer consumerOutline = buffer.getBuffer(RenderType.entityTranslucent(TEXTURE));
        vertex(consumerOutline, matrix, normal, -w, -h, 0.005f, 0, 1, packedLight);
        vertex(consumerOutline, matrix, normal,  w, -h, 0.005f, 1, 1, packedLight);
        vertex(consumerOutline, matrix, normal,  w,  h, 0.005f, 1, 0, packedLight);
        vertex(consumerOutline, matrix, normal, -w,  h, 0.005f, 0, 0, packedLight);
        vertex(consumerOutline, matrix, normal,  w, -h, -0.005f, 0, 1, packedLight);
        vertex(consumerOutline, matrix, normal, -w, -h, -0.005f, 1, 1, packedLight);
        vertex(consumerOutline, matrix, normal, -w,  h, -0.005f, 1, 0, packedLight);
        vertex(consumerOutline, matrix, normal,  w,  h, -0.005f, 0, 0, packedLight);

        stack.popPose();
        super.render(portal, entityYaw, partialTicks, stack, buffer, packedLight);
    }

    private static void vertex(VertexConsumer consumer, Matrix4f matrix, Matrix3f normal, float x, float y, float z, float u, float v, int light) {
        consumer.vertex(matrix, x, y, z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(light).normal(normal, 0, 1, 0).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(Portal entity) {
        return TEXTURE;
    }
}