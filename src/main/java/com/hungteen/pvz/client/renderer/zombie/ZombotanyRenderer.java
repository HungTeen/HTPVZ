package com.hungteen.pvz.client.renderer.zombie;

import com.google.common.collect.Iterables;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.model.zombie.PVZZombieModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.zombies.zombotany.AbstractZombotanyZombie;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.animation.KeyframeAnimations;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.client.animation.AnimationDefinition;
import net.minecraft.world.entity.AnimationState;

import java.util.*;

public abstract class ZombotanyRenderer<T extends AbstractZombotanyZombie> extends AbstractPVZZombieRenderer<T,PVZZombieModel<T>> {

    private final EntityRendererProvider.Context context;
    protected ModelPart plantHead;
    boolean isRenderingHead = false;
    private List<ModelPart> bodyParts = new ArrayList<>();
    private List<ModelPart> headPathParts = new ArrayList<>();

    public ZombotanyRenderer(EntityRendererProvider.Context context) {
        super(context, new PVZZombieModel<T>(context.bakeLayer(ModelLayers.PLAYER)));
        this.context = context;
    }

    /**
     * 递归添加所有子部件
     * @param part 当前部件
     * @param list 存储列表
     */
    private void addAllChildrenRecursive(ModelPart part, List<ModelPart> list) {
        if (part == null) return;
        list.add(part);
        for (ModelPart child : part.children.values()) {
            addAllChildrenRecursive(child, list);
        }
    }

    /**
     * 查找指定部件到根节点的路径
     * @param partName 要查找的部件名称
     * @param root 根节点
     * @return 从指定部件到根节点的路径列表
     */
    protected List<ModelPart> findPartPathToRoot(String partName, ModelPart root) {
        List<ModelPart> path = new ArrayList<>();
        Map<ModelPart, ModelPart> parentMap = new HashMap<>();

        // 找到目标部件的父节点
        Queue<ModelPart> queue = new LinkedList<>();
        queue.offer(root);
        ModelPart targetParent = null;

        while (!queue.isEmpty()) {
            ModelPart current = queue.poll();

            if (current.children.containsKey(partName)) {
                targetParent = current;
                // 添加目标部件及其所有子部件
                ModelPart targetPart = current.getChild(partName);
                addAllChildrenRecursive(targetPart, path);
                break;
            }

            for (Map.Entry<String, ModelPart> entry : current.children.entrySet()) {
                parentMap.put(entry.getValue(), current);
                queue.offer(entry.getValue());
            }
        }

        // 从目标部件的父节点向上构建路径到根节点
        if (targetParent != null) {
            ModelPart current = targetParent;
            while (current != null) {
                path.add(current);
                current = parentMap.get(current);
            }
        }

        return path;
    }

    private List<ModelPart> findBodyParts(EntityModel<?> model) {
        bodyParts.clear();
        headPathParts.clear();

        // 兼容 HumanoidModel，优先使用其 bodyParts/headParts 方法
        if (model instanceof HumanoidModel<?> humanoid) {
            // 头部相关
            humanoid.headParts().forEach(headPathParts::add);
            // 身体相关
            humanoid.bodyParts().forEach(bodyParts::add);
        } else {
            // 通用递归收集所有部件
            try {
                java.lang.reflect.Field rootField = model.getClass().getDeclaredField("root");
                rootField.setAccessible(true);
                ModelPart root = (ModelPart) rootField.get(model);
                // 递归收集所有部件
                Queue<ModelPart> queue = new LinkedList<>();
                Set<ModelPart> visited = new HashSet<>();
                queue.offer(root);
                while (!queue.isEmpty()) {
                    ModelPart current = queue.poll();
                    if (!visited.contains(current)) {
                        visited.add(current);
                        bodyParts.add(current);
                        current.children.values().forEach(queue::offer);
                    }
                }
            } catch (Exception e) {
                 e.printStackTrace();
            }
        }
        return bodyParts;
    }

    private ModelPart findHeadPart(String partName, ModelPart root) {
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

    /**
     * 处理植物僵尸的植物部分动画
     */
    protected abstract void setupPlantAnim(T zombotanyEntity, EntityModel<?> plantModel, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch);

    /**
     * 获取植物模型中头部的名称，默认为"head"
     */
    protected String getPlantModelPartName() {
        return "head";
    }

    /**
     *  应用动画
     */
    protected void animate(AnimationState animationState, AnimationDefinition animation, float ageInTicks, HierarchicalModel<?> model) {
        if (animationState != null && animation != null) {
            animationState.updateTime(ageInTicks, 1.0F);
            animationState.ifStarted(state -> {
                KeyframeAnimations.animate(model, animation, state.getAccumulatedTime(), 1.0F,  new Vector3f());
            });
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T entity) {
        if (isRenderingHead) {
            return entity.getPlantTextureLocation();
        }
        return new ResourceLocation(PVZMod.MODID, "textures/entity/zombie/minecraft_overworld_zombie.png");
    }

    @Override
    public void render(T entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        if (getModel().head != null) {
            try {
                // 获取植物模型
                Class<?> modelClass = Class.forName(entity.getPlantModelClassName());
                ResourceLocation registryLoc = Registry.ENTITY_TYPE.getKey(entity.getType());
                String registryPath = registryLoc.getPath().replace("_zombie", "");
                ModelLayerLocation layerLocation = PVZLayerHandler.LayerLocationMap.get(registryPath + ":main");

                // 创建植物模型实例
                ModelPart plantModelPart = context.bakeLayer(layerLocation);
                EntityModel<?> plantModel = (EntityModel<?>) modelClass
                        .getDeclaredConstructor(ModelPart.class)
                        .newInstance(plantModelPart);

                // 获取植物头部
                plantHead = findHeadPart(this.getPlantModelPartName(), plantModelPart);

                if (plantHead != null) {
                    // 备份原始头部的变换
                    ModelPart zombieHead = getModel().head;
                    findBodyParts(getModel());

                    // 替换头部
                    getModel().head = plantHead;
                    this.model.head.visible = true;


                    // 设置动画状态
                    float limbSwing = entity.animationPosition;
                    float limbSwingAmount = entity.animationSpeed;
                    float ageInTicks = entity.tickCount + partialTicks;
                    float netHeadYaw = entity.yHeadRot;
                    float headPitch = entity.getXRot();

                    // 应用自定义动画
                    setupPlantAnim(entity, plantModel, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);

                    boolean originalRenderHand = entity.controlledByRenderHand;
                    boolean originalRenderHead = entity.controlledByRenderHead;
                    boolean originalRenderHat = entity.controlledByRenderHat;

                    entity.controlledByRenderHead = false;
                    // 渲染身体（隐藏头部）
                    zombieHead.visible = false;
                    this.totalParts(model).forEach(part -> part.visible = true);
                    model.headParts().forEach(part -> part.visible = false);


                    isRenderingHead = false;
                    super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);

                    // 渲染头部（隐藏身体）
                    this.totalParts(model).forEach(part -> part.visible = false);
                    entity.controlledByRenderHead = originalRenderHead;
                    entity.controlledByRenderHand = false;
                    entity.controlledByRenderHat= false;

                    // 只显示植物头部
                    zombieHead.visible = false;
                    isRenderingHead = true;

                    MultiBufferSource wrappedBuffer = renderType -> {
                        return buffer.getBuffer(RenderType.entityCutoutNoCull(entity.getPlantTextureLocation()));
                    };

                    super.render(entity, entityYaw, partialTicks, poseStack, wrappedBuffer, packedLight);

                    // 恢复所有部件的可见性
                    this.totalParts(model).forEach(part -> part.visible = true);
                    model.headParts().forEach(part -> part.visible = true);
                    zombieHead.visible = true;
                    isRenderingHead = false;
                    // 恢复可见性设置
                    entity.controlledByRenderHand = originalRenderHand;
                    entity.controlledByRenderHead = originalRenderHead;
                    entity.controlledByRenderHat = originalRenderHat;
                }
            } catch (Exception e) {
                e.printStackTrace();
                super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
            }
        } else {
            super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
        }
    }

    public Iterable<ModelPart> totalParts(PVZZombieModel<?> model) {
        return Iterables.concat(model.bodyParts(), model.headParts());
    }
} 