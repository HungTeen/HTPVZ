package com.hungteen.pvz.client.layer;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.client.model.attached.ButterBottomModel;
import com.hungteen.pvz.client.model.attached.ButterHeadModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.*;
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
    private final ButterBottomModel butterBottomModel;
    public ButterHeadLayer(RenderLayerParent<T, M> p_117346_) {
        super(p_117346_);
        renderer = (LivingEntityRenderer<T, M>) p_117346_;
        butterHeadModel = new ButterHeadModel<>(ClientProxy.MC.getEntityModels().bakeLayer(PVZLayerHandler.LayerLocationMap.get("butter_head:main")));
        butterBottomModel = new ButterBottomModel<>(ClientProxy.MC.getEntityModels().bakeLayer(PVZLayerHandler.LayerLocationMap.get("butter_bottom:main")));
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, T entity, float entityYaw, float partialTicks, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
        EntityModel<T> model = renderer.getModel();
        ModelPart main;
        VertexConsumer vertexConsumer;
        int packedOverlay = OverlayTexture.NO_OVERLAY;
        poseStack.pushPose();
        if (entity.getAttribute(Attributes.MOVEMENT_SPEED).getModifier(PVZMobEffects.BUTTER_EFFECT_UUID) != null) {
            if (PVZConfig.renderButterOnHead()) {
                if (model.young && model instanceof AgeableListModel<T> model1) {
                    translateAgeable(poseStack, model1);
                }
                main = butterHeadModel.root();
                vertexConsumer = bufferSource.getBuffer(model.renderType(Util.prefix("textures/models/butter/butter_head.png")));
                //omg why cant they all be the Hierarchical ones?
                if (model instanceof HierarchicalModel<?> model1) {
                    if (hasHead((model1.root()))) {
                        renderHead(model1.root(), main, poseStack, vertexConsumer, packedLight, packedOverlay, 1, 1, 1, 1);
                    } else {
                        poseStack.translate(0, 1 - getBoneHeight(model1.root()) / 16, 0);//TODO why should +1 ?
                        main.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1, 1, 1, 1);
                    }
                } else if (model instanceof HeadedModel model1) {
                    ModelPart head = model1.getHead();
                    head.translateAndRotate(poseStack);
                    poseStack.translate(0, -getBoneHeight(head) / 16, 0);
                    main.compile(poseStack.last(), vertexConsumer, packedLight, packedOverlay, 1, 1, 1, 1);
                    main.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1, 1, 1, 1);
                } else if (model instanceof QuadrupedModel<?> model1) {
                    model1.head.translateAndRotate(poseStack);
                    poseStack.translate(0, -getBoneHeight(model1.head) / 16, 0);
                    main.compile(poseStack.last(), vertexConsumer, packedLight, packedOverlay, 1, 1, 1, 1);
                    main.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1, 1, 1, 1);
                } else {
                    poseStack.translate(0, 1.5 - entity.getBbHeight(), 0);
                    main.render(poseStack, vertexConsumer, packedLight, packedOverlay, 1, 1, 1, 1);
                }
                if (model.young && model instanceof AgeableListModel<T> model1) {
                    poseStack.popPose();
                }
            } else if (entity.isAlive()){
                main = butterBottomModel.root();
                vertexConsumer = bufferSource.getBuffer(model.renderType(Util.prefix("textures/models/butter/butter_bottom.png")));
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

    private void translateAgeable(PoseStack poseStack, AgeableListModel model) {
        poseStack.pushPose();
        if (model.scaleHead) {
            float f = 1.5F / model.babyHeadScale;
            poseStack.scale(f, f, f);
        }
        poseStack.translate(0.0D, model.babyYHeadOffset / 16.0F, model.babyZHeadOffset / 16.0F);
    }

    private float getBoneHeight(ModelPart part) {
        float result = 0;
        for (ModelPart.Cube cube : part.cubes) {
            result = Math.max(cube.maxY - cube.minY, result);
        }
        return result;
    }
}
