package com.hungteen.pvz.client.renderer.zombies;

import com.hungteen.pvz.client.layer.VanillaLightLayer;
import com.hungteen.pvz.client.model.zombie.FireImpModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.zombies.GhastRiderBoss;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class GhastRiderRenderer<T extends GhastRiderBoss, M extends FireImpModel<T>> extends AbstractPVZZombieRenderer<T, M> {
    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/zombie/imp/ghast_rider.png");
    private static final ResourceLocation LIGHT_0 = Util.prefix("textures/entity/zombie/imp/ghast_rider_light_0.png");
    private static final ResourceLocation LIGHT_1 = Util.prefix("textures/entity/zombie/imp/ghast_rider_light_1.png");
    public GhastRiderRenderer(EntityRendererProvider.Context context) {
        super(context, (M) new FireImpModel<T>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("fire_imp:main"))));
        this.addLayer(new VanillaLightLayer<>(this, LIGHT_0, zombie -> ! zombie.isPhase2()));
        this.addLayer(new VanillaLightLayer<>(this, LIGHT_1, GhastRiderBoss::isPhase2));
    }

    @Override
    public ResourceLocation getTextureLocation(T zombie) {
        return TEXTURE;
    }
    protected void renderLeash(T zombie, float partialTicks, PoseStack poseStack, MultiBufferSource buffer) {
        if (! zombie.isHanging()) {
            return;
        }
        poseStack.pushPose();
        Vec3 vec3 = null;
        BlockPos tiedPosition = zombie.getHangingPosition();
        if (tiedPosition != null) {
            vec3 = Vec3.atCenterOf(tiedPosition);
        } else {
            Entity entity = zombie.getHangingEntity();
            if (entity != null) {
                tiedPosition = entity.blockPosition();
                vec3 = entity.position();
            }
        }
        if (vec3 == null) {
            return;
        }
        double entityRot = (double)(Mth.lerp(partialTicks, zombie.yRotO, zombie.yRot) * ((float)Math.PI / 180F)) + (Math.PI / 2D);
        Vec3 leashOffset = zombie.getLeashOffset();
        double leashOffsetX = Math.cos(entityRot) * leashOffset.z + Math.sin(entityRot) * leashOffset.x;
        double leashOffsetZ = Math.sin(entityRot) * leashOffset.z - Math.cos(entityRot) * leashOffset.x;
        double offsetX = Mth.lerp(partialTicks, zombie.xo, zombie.getX()) + leashOffsetX;
        double offsetY = Mth.lerp(partialTicks, zombie.yo, zombie.getY()) + leashOffset.y;
        double offsetZ = Mth.lerp(partialTicks, zombie.zo, zombie.getZ()) + leashOffsetZ;
        poseStack.translate(leashOffsetX, leashOffset.y, leashOffsetZ);
        float leashX = (float)(vec3.x - offsetX);
        float leashY = (float)(vec3.y - offsetY);
        float leashZ = (float)(vec3.z - offsetZ);
        float width = 0.1F;
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.leash());
        Matrix4f matrix4f = poseStack.last().pose();
        float f4 = Mth.fastInvSqrt(leashX * leashX + leashZ * leashZ) * width / 2.0F;
        float fZ = leashZ * f4;
        float fX = leashX * f4;
        BlockPos blockpos = new BlockPos(zombie.getEyePosition(partialTicks));
        int entityBlockLight = this.getBlockLightLevel(zombie, blockpos);
        int anchorBlockLight = zombie.level.getBrightness(LightLayer.BLOCK, tiedPosition);
        int entitySkyLight = zombie.level.getBrightness(LightLayer.SKY, blockpos);
        int anchorSkyLight = zombie.level.getBrightness(LightLayer.SKY, tiedPosition);
        int jointNum = (int) Math.sqrt(zombie.getRopeLengthSqr()) * 8;
        for(int i = jointNum; i >= 0; --i) {
            float afx = i % 2 == 1 ? 0 : fX;
            float afz = i % 2 == 1 ? 0 : fZ;
            addVertexPair(vertexconsumer, matrix4f, leashX, leashY, leashZ, entityBlockLight, 15, entitySkyLight, anchorSkyLight,
                    width * 2, 0, afz, afx, jointNum, jointNum, true);
        }
        for(int i = jointNum; i >= 0; --i) {
            float afx = i % 2 == 1 ? 0 : fX;
            float afz = i % 2 == 1 ? 0 : fZ;
            addVertexPair(vertexconsumer, matrix4f, leashX, leashY, leashZ, entityBlockLight, 15, entitySkyLight, anchorSkyLight,
                    width, 0, afz, afx, i, jointNum, true);
        }
        poseStack.popPose();
    }
    private static void addVertexPair(VertexConsumer vertexConsumer, Matrix4f matrix4f, float leashX, float leashY, float leashZ
            , int entityBlockLight, int anchorBlockLight, int entitySkyLight, int anchorSkyLight
            , float widthZ, float widthX, float fZ, float fX, int progress, int jointNum, boolean bool) {
        float progressPercent = (float) progress / jointNum;
        float heat = (float) (1 / (5.01 - 5 * progressPercent));
        int i = (int)Mth.lerp(progressPercent, (float)entityBlockLight, (float)anchorBlockLight);
        int j = (int)Mth.lerp(progressPercent, (float)entitySkyLight, (float)anchorSkyLight);
        int k = LightTexture.pack(i, j);
        float joint = progress % 2 == (bool ? 1 : 0) ? 0.7F : 1.0F;
        float r = Math.min(1, 0.1F * joint + 0.8F * heat);
        float g = Math.min(1, 0.1F * joint + 0.15F * heat);
        float b = Math.min(1, 0.2F * joint + 0.1F * heat);
        float progressX = leashX * progressPercent;
        float progressY = leashY > 0.0F ? leashY * progressPercent * progressPercent : leashY - leashY * (1.0F - progressPercent) * (1.0F - progressPercent);
        float progressZ = leashZ * progressPercent;
        vertexConsumer.vertex(matrix4f, progressX - fZ, progressY + widthX, progressZ + fX).color(r, g, b, 1.0F).uv2(k).endVertex();
        vertexConsumer.vertex(matrix4f, progressX + fZ, progressY + widthZ - widthX, progressZ - fX).color(r, g, b, 1.0F).uv2(k).endVertex();
    }
}
