package com.hungteen.pvz.client.renderer.zombie;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.client.model.zombie.PVZZombieModel;
import com.hungteen.pvz.common.entity.ModelPartEntity;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.common.network.ClientProxy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.phys.Vec3;

public abstract class AbstractPVZZombieRenderer<T extends PVZZombie, M extends PVZZombieModel<T>> extends HumanoidMobRenderer<T, M> {
    public AbstractPVZZombieRenderer(EntityRendererProvider.Context context, M p_174170_) {
        super(context, p_174170_, 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR))));
        if (PVZConfig.renderZombieStuckArrows()) {
            this.addLayer(new ArrowLayer<>(context, this));
        }
    }
    @Override
    public boolean shouldRender(T zombie, Frustum frustum, double p_114493_, double p_114494_, double p_114495_) {
        return zombie.isHanging() || super.shouldRender(zombie, frustum, p_114493_, p_114494_, p_114495_);
    }

    @Override
    public void render(T zombie, float p_115456_, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int p_115460_) {
        if (PVZConfig.zombieDropParts() && ! ClientProxy.MC.isPaused()) {
            this.model.setupAnim(zombie, 0, 0, partialTicks, zombie.getYRot(), zombie.getXRot());
            if (zombie.renderHand && zombie.shouldDropHand()) {
                zombie.renderHand = false;
                Vec3 speed = new Vec3(zombie.getRandom().nextFloat() * 0.25 - 0.125,
                        zombie.getRandom().nextFloat() * 0.15,
                        zombie.getRandom().nextFloat() * 0.25 - 0.125);
                new ModelPartEntity(zombie.level, model.leftArm, getTextureLocation(zombie)).pos(zombie.position().add(0, zombie.getBbHeight() * 0.75, 0))
                        .speed(speed).rotation(new Vec3(0.5, 0.5, 0.5)).scale(zombie.isBaby() ? 0.5F : 1F).join(zombie.level);
                new ModelPartEntity(zombie.level, model.leftSleeve, getTextureLocation(zombie)).pos(zombie.position().add(0,  zombie.getBbHeight() * 0.75, 0))
                        .speed(speed).rotation(new Vec3(0.5, 0.5, 0.5)).scale(zombie.isBaby() ? 0.5F : 1F).join(zombie.level);
            }
            if (zombie.renderHead && zombie.shouldDropHead()) {
                zombie.renderHead = false;
                Vec3 speed = new Vec3(zombie.getRandom().nextFloat() * 0.25 - 0.125,
                        zombie.getRandom().nextFloat() * 0.15,
                        zombie.getRandom().nextFloat() * 0.25 - 0.125);
                new ModelPartEntity(zombie.level, model.head, getTextureLocation(zombie)).pos(zombie.position().add(0, zombie.getBbHeight(), 0))
                        .speed(speed).rotation(new Vec3(0.5, 0.5, 0.5)).scale(zombie.isBaby() ? 0.67F : 1F).join(zombie.level);
                new ModelPartEntity(zombie.level, model.hat, getTextureLocation(zombie)).pos(zombie.position().add(0, zombie.getBbHeight(), 0))
                        .speed(speed).rotation(new Vec3(0.5, 0.5, 0.5)).scale(zombie.isBaby() ? 0.67F : 1F).join(zombie.level);
            }
        }
        super.render(zombie, p_115456_, partialTicks, poseStack, bufferSource, p_115460_);
        renderLeash(zombie, partialTicks, poseStack, bufferSource);
    }

    protected void setupRotations(T zombie, PoseStack poseStack, float p_114111_, float p_114112_, float p_114113_) {
        super.setupRotations(zombie, poseStack, p_114111_, p_114112_, p_114113_);
        float f = zombie.getSwimAmount(p_114113_);
        if (f > 0.0F) {
            float f3 = zombie.isInWater() || zombie.isInFluidType((fluidType, height) -> zombie.canSwimInFluidType(fluidType)) ? -90.0F - zombie.getXRot() : -90.0F;
            float f4 = Mth.lerp(f, 0.0F, f3);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(f4));
            poseStack.translate(0.0D, -f, 0.3F);
        }
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
        float width = 0.025F;
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

        for(int i = 0; i <= 24; ++i) {
            addVertexPair(vertexconsumer, matrix4f, leashX, leashY, leashZ, entityBlockLight, anchorBlockLight, entitySkyLight, anchorSkyLight, width, width, fZ, fX, i, false);
        }
        for(int i = 24; i >= 0; --i) {
            addVertexPair(vertexconsumer, matrix4f, leashX, leashY, leashZ, entityBlockLight, anchorBlockLight, entitySkyLight, anchorSkyLight, width, width, fZ, fX, i, true);
        }
        poseStack.popPose();
    }
    private static void addVertexPair(VertexConsumer vertexConsumer, Matrix4f matrix4f, float leashX, float leashY, float leashZ
            , int entityBlockLight, int anchorBlockLight, int entitySkyLight, int anchorSkyLight
            , float widthZ, float widthX, float fZ, float fX, int progress, boolean bool) {
        float progressPercent = (float)progress / 24.0F;
        int i = (int)Mth.lerp(progressPercent, (float)entityBlockLight, (float)anchorBlockLight);
        int j = (int)Mth.lerp(progressPercent, (float)entitySkyLight, (float)anchorSkyLight);
        int k = LightTexture.pack(i, j);
        float joint = progress % 2 == (bool ? 1 : 0) ? 0.7F : 1.0F;
        float r = 0.5F * joint;
        float g = 0.4F * joint;
        float b = 0.3F * joint;
        float progressX = leashX * progressPercent;
        float progressY = leashY > 0.0F ? leashY * progressPercent * progressPercent : leashY - leashY * (1.0F - progressPercent) * (1.0F - progressPercent);
        float progressZ = leashZ * progressPercent;
        vertexConsumer.vertex(matrix4f, progressX - fZ, progressY + widthX, progressZ + fX).color(r, g, b, 1.0F).uv2(k).endVertex();
        vertexConsumer.vertex(matrix4f, progressX + fZ, progressY + widthZ - widthX, progressZ - fX).color(r, g, b, 1.0F).uv2(k).endVertex();
    }
    @Override
    public abstract ResourceLocation getTextureLocation(T zombie);
}
