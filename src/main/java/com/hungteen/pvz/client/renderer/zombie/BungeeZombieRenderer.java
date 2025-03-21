package com.hungteen.pvz.client.renderer.zombie;

import com.hungteen.pvz.client.model.zombie.PVZZombieModel;
import com.hungteen.pvz.common.entity.zombies.BungeeZombie;
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
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public class BungeeZombieRenderer<T extends BungeeZombie, M extends PVZZombieModel<T>> extends PVZZombieRenderer<T, M>{
    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/zombie/bungee_zombie/bungee_zombie.png");
    public BungeeZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(T zombie, float p_115456_, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int p_115460_) {
        super.render(zombie, p_115456_, partialTicks, poseStack, bufferSource, p_115460_);
        if (zombie.isHanging()) {
            renderLeash(zombie, partialTicks, poseStack, bufferSource);
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T zombie) {
        return TEXTURE;
    }
    private void renderLeash(T zombie, float p_115463_, PoseStack poseStack, MultiBufferSource buffer) {
        poseStack.pushPose();
        BlockPos tiedPosition = zombie.getHangingPosition();
        double d0 = (double)(Mth.lerp(p_115463_, zombie.yRotO, zombie.yRot) * ((float)Math.PI / 180F)) + (Math.PI / 2D);
        Vec3 vec31 = zombie.getLeashOffset();
        double d1 = Math.cos(d0) * vec31.z + Math.sin(d0) * vec31.x;
        double d2 = Math.sin(d0) * vec31.z - Math.cos(d0) * vec31.x;
        double d3 = Mth.lerp(p_115463_, zombie.xo, zombie.getX()) + d1;
        double d4 = Mth.lerp(p_115463_, zombie.yo, zombie.getY()) + vec31.y;
        double d5 = Mth.lerp(p_115463_, zombie.zo, zombie.getZ()) + d2;
        poseStack.translate(d1, vec31.y, d2);
        Vec3 vec3 = Vec3.atCenterOf(tiedPosition);
        float f = (float)(vec3.x - d3);
        float f1 = (float)(vec3.y - d4);
        float f2 = (float)(vec3.z - d5);
        float f3 = 0.025F;
        VertexConsumer vertexconsumer = buffer.getBuffer(RenderType.leash());
        Matrix4f matrix4f = poseStack.last().pose();
        float f4 = Mth.fastInvSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
        float f5 = f2 * f4;
        float f6 = f * f4;
        BlockPos blockpos = new BlockPos(zombie.getEyePosition(p_115463_));
        int i = this.getBlockLightLevel(zombie, blockpos);
        int j = zombie.level.getBrightness(LightLayer.BLOCK, tiedPosition);
        int k = zombie.level.getBrightness(LightLayer.SKY, blockpos);
        int l = zombie.level.getBrightness(LightLayer.SKY, tiedPosition);

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
