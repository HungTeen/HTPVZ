package com.hungteen.pvz.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.*;
import net.minecraft.client.model.geom.ModelPart;

import java.util.*;
import java.util.function.BiPredicate;

public class ClientUtil {

    //model tools
    public static ModelPart getPartMatchingPredicate(BiPredicate<String/*name*/, ModelPart> predicate, ModelPart root) {
        Queue<ModelPart> queue = new LinkedList<>();
        queue.offer(root);
        // 使用广度优先搜索，这样可以优先找到最接近根节点的指定部件
        while (!queue.isEmpty()) {
            ModelPart current = queue.poll();
            // 检查当前节点的直接子节点
            for (String name : current.children.keySet()) {
                ModelPart part = current.children.get(name);
                if (predicate.test(name, part)) {
                    return part;
                }
                queue.offer(part);
            }
        }
        return null;
    }
    public static List<ModelPart> getPartsMatchingPredicate(BiPredicate<String/*name*/, ModelPart> predicate, ModelPart root) {
        Queue<ModelPart> queue = new LinkedList<>();
        queue.offer(root);
        List<ModelPart> parts = new ArrayList<>();
        // 使用广度优先搜索，这样可以优先找到最接近根节点的指定部件
        while (!queue.isEmpty()) {
            ModelPart current = queue.poll();
            // 检查当前节点的直接子节点
            for (String name : current.children.keySet()) {
                ModelPart part = current.children.get(name);
                if (predicate.test(name, part)) {
                    parts.add(part);
                }
                queue.offer(part);
            }
        }
        return parts;
    }
    public static ModelPart getPartFromString(String partName, ModelPart root) { // susen, 强！
        Queue<ModelPart> queue = new LinkedList<>();
        queue.offer(root);
        // 使用广度优先搜索，这样可以优先找到最接近根节点的指定部件
        while (!queue.isEmpty()) {
            ModelPart current = queue.poll();
            // 检查当前节点的直接子节点
            try {
                return current.getChild(partName);
            } catch (Exception ignored) {}
            // 将所有子节点加入队列
            current.children.forEach((name, part) -> queue.offer(part));
        }
        return null;
    }
    public static boolean hasHead(ModelPart root) {
        return getFirstHead(root) != root;
    }
    public static ModelPart getFirstHead(ModelPart root) {
        ModelPart part = getPartMatchingPredicate((string, modelPart) -> string.contains("head"), root);
        return part == null ? root : part;
    }
    public static List<ModelPart> getHeads(ModelPart root) {
        List<ModelPart> parts = getPartsMatchingPredicate((string, modelPart) -> string.contains("head"), root);
        if (parts.isEmpty()) {
            parts.add(root);
        }
        return parts;
    }
    public static int getNumberOfHeads(ModelPart root) {
        return getHeads(root).size();
    }
    public static ModelPart getFirstHead(EntityModel<?> model) {
        //omg why cant they all be the Hierarchical ones?
        if (model instanceof HierarchicalModel<?> model1) {
            return getFirstHead(model1.root());
        } else if (model instanceof HeadedModel model1) {
            return model1.getHead();
        } else if (model instanceof QuadrupedModel<?> model1) {
            return model1.head;
        } else {
            return null;
        }
    }
    public static List<ModelPart> getHeads(EntityModel<?> model) {
        //omg why cant they all be the Hierarchical ones?
        if (model instanceof HierarchicalModel<?> model1) {
            return getHeads(model1.root());
        } else if (model instanceof HeadedModel model1) {
            return List.of(model1.getHead());
        } else if (model instanceof QuadrupedModel<?> model1) {
            return List.of(model1.head);
        } else {
            return List.of();
        }
    }

    public static void renderOnHead(ModelPart root, ModelPart toRender, PoseStack stack,
                                    VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        stack.pushPose();
        root.translateAndRotate(stack);
        if (root.visible) {
            for (String name: root.children.keySet()) {
                if (name.contains("head")) {
                    stack.pushPose();
                    root.getChild(name).translateAndRotate(stack);
                    stack.translate(0, - getBoneHeight(root.getChild(name)) / 16 - 0.125, 0);
                    toRender.compile(stack.last(), vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                    toRender.render(stack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
                    stack.popPose();
                }
            }
            for (ModelPart part: root.children.values()) {
                renderOnHead(part, toRender, stack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
            }
        }
        stack.popPose();
    }

    public static void translateAgeable(PoseStack poseStack, AgeableListModel model) {
        if (model.scaleHead) {
            float f = 1.5F / model.babyHeadScale;
            poseStack.scale(f, f, f);
        }
        poseStack.translate(0.0D, model.babyYHeadOffset / 16.0F, model.babyZHeadOffset / 16.0F);
    }

    public static float getBoneHeight(ModelPart part) {
        float result = 0;
        for (ModelPart.Cube cube : part.cubes) {
            result = Math.max(cube.maxY - cube.minY, result);
        }
        return result;
    }

    public static ModelPart copyModelPart(ModelPart original) {
        Map<String, ModelPart> children = new HashMap<>();
        for (String part : original.children.keySet()) {
            children.put(part, copyModelPart(original.children.get(part)));
        }
        ModelPart newPart = new ModelPart(original.cubes, children);
        newPart.xScale = original.xScale;
        newPart.yScale = original.yScale;
        newPart.zScale = original.zScale;
        newPart.visible = original.visible;
        newPart.setInitialPose(original.getInitialPose());
        newPart.resetPose();
        return newPart;
    }

}
