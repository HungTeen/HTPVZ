package com.hungteen.pvz.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

public class NegativeBlockLayer<T extends Entity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    RenderLayerParent<T, M> parent;
    ResourceLocation location;
    public NegativeBlockLayer(RenderLayerParent<T, M> p_117346_) {
        super(p_117346_);
        parent = p_117346_;
    }
    public NegativeBlockLayer(RenderLayerParent<T, M> p_117346_, ResourceLocation location) {
        super(p_117346_);
        parent = p_117346_;
        this.location = location;
    }
    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int p_117351_, T entity, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
        VertexConsumer vertexconsumer = bufferSource.getBuffer(RenderType.entityTranslucentCull(location == null ? parent.getTextureLocation(entity) : location));
        parent.getModel().renderToBuffer(poseStack, vertexconsumer, p_117351_, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1.0F);
    }
}
