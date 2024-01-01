package com.hungteen.pvz.client.renderer.bullet;

import com.hungteen.pvz.client.model.bullet.CommonBulletModel;
import com.hungteen.pvz.common.entity.bullet.AbstractBulletEntity;
import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;

public abstract class CommonBulletRenderer <T extends AbstractBulletEntity> extends EntityRenderer<T>{

    private final CommonBulletModel<PeaBullet> model;
    public CommonBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.model = new CommonBulletModel<>(context.bakeLayer(CommonBulletModel.LAYER_LOCATION));
    }

    // protected int getBlockLightLevel(DriftProjectileEntity p_115869_, BlockPos p_115870_) {
    //     return 15;
    // }

    public void render(T bullet, float p_115863_, float p_115864_, PoseStack poseStack, MultiBufferSource p_115866_, int p_115867_) {
        poseStack.pushPose();
        VertexConsumer vertexconsumer = p_115866_.getBuffer(this.model.renderType(this.getTextureLocation(bullet)));
        this.model.renderToBuffer(poseStack, vertexconsumer, p_115867_, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        poseStack.scale(0.5f,0.5f,0.5f);
        poseStack.popPose();
        super.render(bullet, p_115863_, p_115864_, poseStack, p_115866_, p_115867_);
    }

}