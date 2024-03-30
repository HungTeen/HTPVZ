package com.hungteen.pvz.client.renderer;

import com.hungteen.pvz.common.entity.ModelPartEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ModelPartRenderer extends EntityRenderer<ModelPartEntity> {
    public ModelPartRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
    }

    @Override
    public void render(ModelPartEntity entity, float p_114486_, float p_114487_, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale(-1, -1, 1);
        poseStack.translate(0.0, 0.0, 0.0);

        VertexConsumer vertexConsumer = buffer.getBuffer(
                RenderType.entityTranslucent(getTextureLocation(entity)));
        
        entity.model.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ModelPartEntity entity) {
        return entity.texture;
    }
}
