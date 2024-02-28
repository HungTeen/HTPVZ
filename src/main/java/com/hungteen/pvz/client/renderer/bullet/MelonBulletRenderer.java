package com.hungteen.pvz.client.renderer.bullet;

import com.hungteen.pvz.client.model.bullet.MelonBulletModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.bullet.MelonBullet;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class MelonBulletRenderer <T extends MelonBullet> extends EntityRenderer<T> {

    private final MelonBulletModel<MelonBullet> model;
    private final ResourceLocation TEXTURE = Util.prefix("textures/entity/bullet/melon_bullet.png");

    public MelonBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new MelonBulletModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("melon_bullet:main")));
    }


    public void render(T bullet, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLightIn) {
        poseStack.pushPose();
        poseStack.scale(-1, -1, 1);
        poseStack.translate(0.0, -1.5, 0.0);
        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(bullet)));
        this.model.setupAnim(bullet, 0, 0, bullet.tickCount + partialTicks, 0, 0);
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.popPose();
        super.render(bullet, entityYaw, partialTicks, poseStack, buffer, packedLightIn);
    }

    @Override
    public ResourceLocation getTextureLocation(T bullet) {
        return TEXTURE;
    }
}
