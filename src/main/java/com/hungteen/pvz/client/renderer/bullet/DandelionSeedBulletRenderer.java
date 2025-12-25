package com.hungteen.pvz.client.renderer.bullet;

import com.hungteen.pvz.client.layer.LightLayer;
import com.hungteen.pvz.client.model.bullet.DandelionSeedBulletModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.bullet.DandelionSeedBullet;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class DandelionSeedBulletRenderer <T extends DandelionSeedBullet, M extends DandelionSeedBulletModel<T>>
        extends EntityRenderer<T> implements RenderLayerParent<T, M> {

    private final M model;
    private final LightLayer<T, M> lightLayer;
    private final ResourceLocation TEXTURE = Util.prefix("textures/entity/bullet/dandelion_seed.png");

    public DandelionSeedBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = (M) new DandelionSeedBulletModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("dandelion_seed_bullet:main")));
        this.lightLayer = new LightLayer<>(this, Util.prefix("textures/entity/bullet/dandelion_seed_light.png"),
                (bullet, partialTicks, ageInTicks) -> bullet.tickCount % 20 < 4 ? 1F : 0);
    }


    public void render(T bullet, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLightIn) {
        poseStack.pushPose();
        poseStack.scale(-1, -1, 1);
        poseStack.translate(0.0, -1.5, 0.0);
        VertexConsumer vertexConsumer = buffer.getBuffer(this.model.renderType(this.getTextureLocation(bullet)));
        this.model.setupAnim(bullet, 0, 0, bullet.tickCount + partialTicks, 0, 0);
        this.model.renderToBuffer(poseStack, vertexConsumer, packedLightIn, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        this.lightLayer.render(poseStack,buffer,packedLightIn, bullet, 0, 0, partialTicks, 0, entityYaw, 0);
        poseStack.popPose();
        super.render(bullet, entityYaw, partialTicks, poseStack, buffer, packedLightIn);
    }

    @Override
    public M getModel() {
        return this.model;
    }

    @Override
    public ResourceLocation getTextureLocation(T bullet) {
        return TEXTURE;
    }
}