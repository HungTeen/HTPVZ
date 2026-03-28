package com.hungteen.pvz.client.layer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.EyesLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;

import java.util.function.Predicate;

public class VanillaLightLayer<T extends Entity, M extends EntityModel<T>> extends EyesLayer<T, M> {
    private final RenderType renderType;
    private final M model;
    private final Predicate<T> predicate;

    public VanillaLightLayer(RenderLayerParent<T, M> p_116981_, M model, ResourceLocation texture, Predicate<T> predicate) {
        super(p_116981_);
        this.model = model;
        renderType = RenderType.eyes(texture);
        this.predicate = predicate;
    }
    public VanillaLightLayer(RenderLayerParent<T, M> p_116981_, ResourceLocation texture) {
        this(p_116981_, p_116981_.getModel(), texture);
    }
    public VanillaLightLayer(RenderLayerParent<T, M> p_116981_, M model, ResourceLocation texture) {
        this(p_116981_, model, texture, t -> true);
    }
    public VanillaLightLayer(RenderLayerParent<T, M> p_116981_, ResourceLocation texture, Predicate<T> predicate) {
        this(p_116981_, p_116981_.getModel(), texture, predicate);
    }

    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLightIn, T entity,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks, float netHeadYaw, float headPitch) {
        if (predicate.test(entity)) {
            VertexConsumer vertexconsumer = bufferSource.getBuffer(this.renderType());
            model.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            model.renderToBuffer(poseStack, vertexconsumer, 15728640, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }

    public RenderType renderType() {
        return renderType;
    }
}
