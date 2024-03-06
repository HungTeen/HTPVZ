package com.hungteen.pvz.client.layer.fullskin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;

public  abstract class PVZFullSkinLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {

    protected RenderLayerParent<T, M> entityRender;
    protected EntityModel<T> entityModel;
    protected float scale = 1F;

    public PVZFullSkinLayer(RenderLayerParent<T, M> entityRendererIn) {
        super(entityRendererIn);
        this.entityRender = entityRendererIn;
        this.entityModel = this.entityRender.getModel();
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferIn, int packedLightIn, T livingEntity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw,
                       float headPitch) {
        if (this.canRender(livingEntity)) {
            poseStack.pushPose();
            poseStack.scale(this.scale, this.scale, this.scale);
            float f = (float) livingEntity.tickCount + partialTicks;
            VertexConsumer ivertexbuilder = bufferIn.getBuffer(RenderType.energySwirl(this.getResourceLocation(livingEntity), this.getU(f), this.getV(f)));
            entityModel.renderToBuffer(poseStack, ivertexbuilder, packedLightIn, OverlayTexture.NO_OVERLAY, 0.5F, 0.5F, 0.5F, 1F);
            poseStack.popPose();
        }
    }
    protected float getU(float f) {
        return 0f;
    }

    protected float getV(float f) {
        return 0f;
    }

    protected abstract boolean canRender(T entity);

    protected abstract ResourceLocation getResourceLocation(T entity);

}
