package com.hungteen.pvz.client.layer;

import com.hungteen.pvz.client.model.attached.ButterHeadModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HeadedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;

public class ButterHeadLayer<T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private final LivingEntityRenderer<T, M> renderer;
    private final ButterHeadModel butterHeadModel;
    public ButterHeadLayer(RenderLayerParent<T, M> p_117346_) {
        super(p_117346_);
        renderer = (LivingEntityRenderer<T, M>) p_117346_;
        butterHeadModel = new ButterHeadModel<>(ClientProxy.MC.getEntityModels().bakeLayer(PVZLayerHandler.LayerLocationMap.get("butter:main")));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity, float entityYaw, float partialTicks, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
        EntityModel<T> model = renderer.getModel();
        ModelPart main = butterHeadModel.root();
        int packedOverlay = OverlayTexture.NO_OVERLAY;
        poseStack.pushPose();
        VertexConsumer vertexConsumer = bufferSource.getBuffer(model.renderType(Util.prefix("textures/models/butter/butter_head.png")));
        if (entity.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(PVZMobEffects.BUTTER_EFFECT_UUID) != null) {
            if (model instanceof HierarchicalModel<?> && hasHead(((HierarchicalModel<?>) model).root())) {
                renderHead(((HierarchicalModel<?>) model).root(), main, poseStack, vertexConsumer, packedLight, packedOverlay, 1, 1, 1, 1);
            } else if (model instanceof HeadedModel model1) {
                ModelPart head = model1.getHead();
                head.translateAndRotate(poseStack);
                poseStack.translate(0, -getBoneHeight(head) / 16, 0);
                main.compile(poseStack.last(), vertexConsumer, packedLight, packedOverlay, 1, 1, 1, 1);
                main.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1, 1, 1, 1);
            } else if (model instanceof QuadrupedModel model1) {
                model1.head.translateAndRotate(poseStack);
                poseStack.translate(0, -getBoneHeight(model1.head) / 16, 0);
                main.compile(poseStack.last(), vertexConsumer, packedLight, packedOverlay, 1, 1, 1, 1);
                main.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1, 1, 1, 1);
            } else {
                poseStack.translate(0, 1.5 - entity.getBbHeight(), 0);
                main.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1, 1, 1, 1);
            }
        }
        poseStack.popPose();
    }


    public boolean hasHead(ModelPart root) {
        for (String name: root.children.keySet()) {
            if (name.contains("head")) {
                return true;
            }
        }
        for (ModelPart part: root.children.values()) {
            if (hasHead(part)) {
                return true;
            }
        }
        return false;
    }

    public void renderHead(ModelPart root, ModelPart main, PoseStack stack,
                           VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        stack.pushPose();
        root.translateAndRotate(stack);
        for (String name: root.children.keySet()) {
            if (name.contains("head")) {
                stack.pushPose();
                root.getChild(name).translateAndRotate(stack);
                stack.translate(0, - getBoneHeight(root.getChild(name)) / 16 - 0.125, 0);
                main.compile(stack.last(), vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                main.render(stack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                stack.popPose();
            }
        }
        for (ModelPart part: root.children.values()) {
            renderHead(part, main, stack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
        }
        stack.popPose();
    }

    private float getBoneHeight(ModelPart part) {
        float result = 0;
        for (ModelPart.Cube cube : part.cubes) {
            result = Math.max(Math.max(cube.maxY, cube.maxY - cube.minY), result);
        }
        result *= part.yScale;
        return result;
    }
}
