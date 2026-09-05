package com.hungteen.pvz.client.layer;

import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.bullet.ArrowWithATarget;
import com.hungteen.pvz.common.network.ClientProxy;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.QuadrupedModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

public class StuckArrowWithATargetLayer <T extends LivingEntity, M extends EntityModel<T>> extends RenderLayer<T, M> {
    private static final Comparator<String> cpr = Comparator.comparingInt(StuckArrowWithATargetLayer::stringToInt);
    public StuckArrowWithATargetLayer(RenderLayerParent<T, M> p_117346_) {
        super(p_117346_);
    }

    public int numStuck(T entity) {
        AtomicInteger result = new AtomicInteger(0);
        entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> result.set(cap.getStuckArrowWithATarget()));
        return result.get();
    }
    protected void renderStuckItem(PoseStack p_116569_, MultiBufferSource p_116570_, int p_116571_, T entity, float p_116573_, float p_116574_, float p_116575_, float p_116576_) {
        float f = Mth.sqrt(p_116573_ * p_116573_ + p_116575_ * p_116575_);
        ArrowWithATarget arrow = new ArrowWithATarget(entity.level, entity);
        arrow.setYRot((float)(Math.atan2(p_116573_, p_116575_) * (double)(180F / (float)Math.PI)));
        arrow.setXRot((float)(Math.atan2(p_116574_, f) * (double)(180F / (float)Math.PI)));
        arrow.yRotO = arrow.getYRot();
        arrow.xRotO = arrow.getXRot();
        ClientProxy.MC.getEntityRenderDispatcher().render(arrow, 0.0D, 0.0D, 0.0D, 0.0F, p_116576_, p_116569_, p_116570_, p_116571_);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int p_117351_, T entity, float p_117353_, float p_117354_, float p_117355_, float p_117356_, float p_117357_, float p_117358_) {
        int i = this.numStuck(entity);
        RandomSource random = RandomSource.create((long)entity.getId() + 820162326);
        if (i > 0) {
            for(int j = 0; j < i; j ++) {
                poseStack.pushPose();
                ModelPart modelpart;
                M model = this.getParentModel();
                if (model instanceof HumanoidModel<?> model1) {
                    modelpart = List.of(model1.body, model1.head, model1.leftArm, model1.leftLeg, model1.rightArm, model1.rightLeg)
                            .get(random.nextInt(6));
                    modelpart.translateAndRotate(poseStack);
                } else if (model instanceof HierarchicalModel<?> model1) {
                    modelpart = rotateToRandomPart(poseStack, model1.root(), random);
                } else if (model instanceof QuadrupedModel<?> model1) {
                    modelpart = List.of(model1.body, model1.head, model1.leftFrontLeg, model1.leftHindLeg, model1.rightFrontLeg, model1.rightHindLeg)
                            .get(random.nextInt(6));
                    modelpart.translateAndRotate(poseStack);
                } else {
                    poseStack.popPose();
                    return;
                }
                if (modelpart == null || ! modelpart.visible) {
                    poseStack.popPose();
                    return;
                }
                float f = random.nextFloat();
                float f1 = random.nextFloat();
                float f2 = random.nextFloat();
                if (! modelpart.cubes.isEmpty()) {
                    ModelPart.Cube modelpart$cube = modelpart.getRandomCube(random);
                    float f3 = Mth.lerp(f, modelpart$cube.minX, modelpart$cube.maxX) / 16.0F;
                    float f4 = Mth.lerp(f1, modelpart$cube.minY, modelpart$cube.maxY) / 16.0F;
                    float f5 = Mth.lerp(f2, modelpart$cube.minZ, modelpart$cube.maxZ) / 16.0F;
                    poseStack.translate(f3, f4, f5);
                    f = -1.0F * (f * 2.0F - 1.0F);
                    f1 = -1.0F * (f1 * 2.0F - 1.0F);
                    f2 = -1.0F * (f2 * 2.0F - 1.0F);
                }
                this.renderStuckItem(poseStack, bufferSource, p_117351_, entity, f, f1, f2, p_117355_);
                poseStack.popPose();
            }

        }
    }

    private static int stringToInt(String str) {
        int i = 0;
        for (int j = 0; j < str.length(); j ++) {
            i += str.charAt(j);
        }
        return i;
    }

    private ModelPart rotateToRandomPart(PoseStack poseStack, ModelPart part, RandomSource source) {
        if (part.children.isEmpty()) {
            part.translateAndRotate(poseStack);
            return part;
        }
        List<ModelPart> parts = new ArrayList<>(Set.copyOf(part.children.keySet()).stream().sorted(cpr).map(str -> part.children.get(str)).toList());
        if (! part.cubes.isEmpty()) {
            parts.add(part);
        }
        while (! parts.isEmpty()) {
            ModelPart selectedPart = parts.get(source.nextInt(parts.size()));
            part.translateAndRotate(poseStack);
            if (part == selectedPart) {
                return part;
            }
            ModelPart result = rotateToRandomPart(poseStack, selectedPart, source);
            if (result == null) {
                parts.remove(selectedPart);
            } else {
                return result;
            }
        }
        return null;
    }
}
