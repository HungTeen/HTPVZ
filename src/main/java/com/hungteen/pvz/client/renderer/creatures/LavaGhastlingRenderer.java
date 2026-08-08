package com.hungteen.pvz.client.renderer.creatures;

import com.hungteen.pvz.client.layer.VanillaLightLayer;
import com.hungteen.pvz.client.model.LavaGhastlingLightModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.LavaGhastling;
import com.hungteen.pvz.common.entity.zombies.GhastRiderBoss;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.GhastModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

import java.util.List;


public class LavaGhastlingRenderer<T extends LavaGhastling> extends MobRenderer<T, EntityModel<T>> {

    private static final ResourceLocation SHOOTING = Util.prefix("textures/entity/lava_ghastling/lava_ghastling_shooting.png");
    private static final ResourceLocation COMMON = Util.prefix("textures/entity/lava_ghastling/lava_ghastling.png");
    private static final ResourceLocation RIDEN_SHOOTING = Util.prefix("textures/entity/lava_ghastling/lava_ghastling_shooting_riden.png");
    private static final ResourceLocation RIDEN_COMMON = Util.prefix("textures/entity/lava_ghastling/lava_ghastling_riden.png");
    private static final ResourceLocation LIGHT_PHASE1 = Util.prefix("textures/entity/lava_ghastling/lava_ghastling_light_0.png");
    private static final ResourceLocation LIGHT_PHASE2 = Util.prefix("textures/entity/lava_ghastling/lava_ghastling_light_1.png");

    public LavaGhastlingRenderer(EntityRendererProvider.Context context) {
        super(context, new GhastModel<>(context.bakeLayer(ModelLayers.GHAST)), 0.4F);
        this.addLayer(new VanillaLightLayer<>(this
                ,new LavaGhastlingLightModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("lava_ghastling_light:main")))
                , LIGHT_PHASE1, ghast -> ghast.getFirstPassenger() instanceof GhastRiderBoss g && ! g.isPhase2()));
        this.addLayer(new VanillaLightLayer<>(this
                ,new LavaGhastlingLightModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("lava_ghastling_light:main")))
                , LIGHT_PHASE2, ghast -> ghast.getFirstPassenger() instanceof GhastRiderBoss g && g.isPhase2()));
    }

    public void render(T ghastling, float p_115456_, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int p_115460_) {
        super.render(ghastling, p_115456_, partialTicks, poseStack, buffer, p_115460_);
        if (ghastling.getFirstPassenger() instanceof GhastRiderBoss) {
            renderLeash(ghastling, partialTicks, poseStack, buffer);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T ghastling) {
        boolean shooting = ghastling.isCharging();
        return ghastling.isVehicle() ? (shooting ? RIDEN_SHOOTING : RIDEN_COMMON) : (shooting ? SHOOTING : COMMON);
    }

    @Override
    public boolean shouldRender(T ghastling, Frustum p_115469_, double p_115470_, double p_115471_, double p_115472_) {
        if (super.shouldRender(ghastling, p_115469_, p_115470_, p_115471_, p_115472_)) {
            return true;
        } else {
            return ghastling.getFirstPassenger() instanceof GhastRiderBoss;
        }
    }
    protected void renderLeash(T ghastling, float partialTicks, PoseStack poseStack, MultiBufferSource buffer) {
        if ((ghastling.getFirstPassenger() instanceof GhastRiderBoss b)) {
            List<LavaGhastling> entities =
                    ghastling.level.getEntitiesOfClass(LavaGhastling.class, ghastling.getBoundingBox().inflate(40, 20, 40));
            for (LavaGhastling g : entities) {
                poseStack.pushPose();
                Vec3 vec3 = g.position();
                double entityRot = (double)(Mth.lerp(partialTicks, ghastling.yRotO, ghastling.yRot) * ((float)Math.PI / 180F)) + (Math.PI / 2D);
                Vec3 leashOffset = ghastling.getLeashOffset();
                double leashOffsetX = Math.cos(entityRot) * leashOffset.z + Math.sin(entityRot) * leashOffset.x;
                double leashOffsetZ = Math.sin(entityRot) * leashOffset.z - Math.cos(entityRot) * leashOffset.x;
                double offsetX = Mth.lerp(partialTicks, ghastling.xo, ghastling.getX()) + leashOffsetX;
                double offsetY = Mth.lerp(partialTicks, ghastling.yo, ghastling.getY()) + leashOffset.y;
                double offsetZ = Mth.lerp(partialTicks, ghastling.zo, ghastling.getZ()) + leashOffsetZ;
                poseStack.translate(leashOffsetX, leashOffset.y + 0.8, leashOffsetZ);
                float leashX = (float)(vec3.x - offsetX);
                float leashY = (float)(vec3.y - offsetY);
                float leashZ = (float)(vec3.z - offsetZ);
                float width = 0.1F;
                VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.leash());
                Matrix4f matrix4f = poseStack.last().pose();
                float f4 = Mth.fastInvSqrt(leashX * leashX + leashZ * leashZ) * width / 2.0F;
                float fZ = leashZ * f4;
                float fX = leashX * f4;
                int anchorBlockLight = ghastling.level.getBrightness(LightLayer.BLOCK, g.blockPosition());
                int anchorSkyLight = ghastling.level.getBrightness(LightLayer.SKY, g.blockPosition());
                int jointNum = (int) Math.sqrt(ghastling.distanceToSqr(g)) * 8;
                for(int i = jointNum; i >= 0; --i) {
                    float afx = i % 2 == 1 ? 0 : fX;
                    float afz = i % 2 == 1 ? 0 : fZ;
                    addVertexPair(vertexconsumer, matrix4f, leashX, leashY, leashZ, 15, anchorBlockLight, 15, anchorSkyLight,
                            width * 2, 0, afz, afx, jointNum, jointNum, b.isPhase2());
                }
                for(int i = jointNum; i >= 0; --i) {
                    float afx = i % 2 == 1 ? 0 : fX;
                    float afz = i % 2 == 1 ? 0 : fZ;
                    addVertexPair(vertexconsumer, matrix4f, leashX, leashY, leashZ, 15, anchorBlockLight, 15, anchorSkyLight,
                            width, 0, afz, afx, i, jointNum, b.isPhase2());
                }
                poseStack.popPose();
            }
        }
    }
    private static void addVertexPair(VertexConsumer vertexConsumer, Matrix4f matrix4f, float leashX, float leashY, float leashZ
            , int entityBlockLight, int anchorBlockLight, int entitySkyLight, int anchorSkyLight
            , float widthZ, float widthX, float fZ, float fX, int progress, int jointNum, boolean phase2) {
        float progressPercent = (float) progress / jointNum;
        int i = (int)Mth.lerp(progressPercent, (float)entityBlockLight, (float)anchorBlockLight);
        int j = (int)Mth.lerp(progressPercent, (float)entitySkyLight, (float)anchorSkyLight);
        int k = LightTexture.pack(i, j);
        float r = phase2 ? 0.6F + 0.4f * progressPercent : 1f;
        float g = 0.8F + (float) Math.max(0, 0.6 * progressPercent - 0.4);
        float b = phase2 ? 1f : 0.2F + 0.7f * progressPercent;
        float progressX = leashX * progressPercent;
        float progressY = leashY > 0.0F ? leashY * progressPercent * progressPercent : leashY - leashY * (1.0F - progressPercent) * (1.0F - progressPercent);
        float progressZ = leashZ * progressPercent;
        vertexConsumer.vertex(matrix4f, progressX - fZ, progressY + widthX, progressZ + fX)
                .color(r, g, b, 1).uv2(k).endVertex();
        vertexConsumer.vertex(matrix4f, progressX + fZ, progressY + widthZ - widthX, progressZ - fX)
                .color(r, g, b, 1).uv2(k).endVertex();
    }
}
