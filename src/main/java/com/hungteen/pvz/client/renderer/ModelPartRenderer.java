package com.hungteen.pvz.client.renderer;

import com.hungteen.pvz.common.entity.ModelPartEntity;
import com.hungteen.pvz.common.network.ClientProxy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;

public class ModelPartRenderer extends EntityRenderer<ModelPartEntity> {
    private final ItemInHandRenderer itemInHandRenderer;
    public ModelPartRenderer(EntityRendererProvider.Context p_174008_) {
        super(p_174008_);
        this.itemInHandRenderer = p_174008_.getItemInHandRenderer();
    }

    @Override
    public void render(ModelPartEntity entity, float p_114486_, float p_114487_, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.scale((float) (-1 * entity.originalScale.x), (float) (-1 * entity.originalScale.y), (float) (1 * entity.originalScale.z));
        poseStack.translate(0.0, 0.0, 0.0);

        if (entity.model != null) { // render ModelPart
            VertexConsumer vertexConsumer = buffer.getBuffer(
                    RenderType.entityTranslucent(getTextureLocation(entity)));
            entity.model.render(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY);
        } else if (entity.itemStack != null && ! entity.itemStack.isEmpty()) { // render Item
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
//            poseStack.scale((float) entity.originalScale.x, (float) entity.originalScale.y, (float) entity.originalScale.z);
            poseStack.translate(0, 1, 0);
            poseStack.pushPose();
            poseStack.mulPose(Vector3f.ZP.rotationDegrees((float) entity.currentRotation.z));
            poseStack.mulPose(Vector3f.YP.rotationDegrees((float) entity.currentRotation.y));
            poseStack.mulPose(Vector3f.XP.rotationDegrees((float) entity.currentRotation.x));
            this.itemInHandRenderer.renderItem(ClientProxy.getPlayer(), entity.itemStack,
                    ItemTransforms.TransformType.THIRD_PERSON_RIGHT_HAND, true,
                    poseStack, buffer, packedLight);
            poseStack.popPose();
            poseStack.popPose();
        }
        poseStack.popPose();
    }

    @Override
    public ResourceLocation getTextureLocation(ModelPartEntity entity) {
        return entity.texture;
    }
}
