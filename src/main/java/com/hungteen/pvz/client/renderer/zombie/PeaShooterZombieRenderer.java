package com.hungteen.pvz.client.renderer.zombie;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.model.plants.animation.PeaShooterAnimation;
import com.hungteen.pvz.common.entity.zombies.zombotany.PeaShooterZombie;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class PeaShooterZombieRenderer extends ZombotanyRenderer<PeaShooterZombie> {

    public PeaShooterZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void setupPlantAnim(PeaShooterZombie zombotanyEntity, EntityModel<?> plantModel, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (plantModel instanceof HierarchicalModel<?> hierarchicalModel) {
            ModelPart root = hierarchicalModel.root();
            // 获取所需的部件

            // 使用新的findPartPathToRoot方法查找各个部件
            List<ModelPart> headPath = findPartPathToRoot("head", root);
            List<ModelPart> bodyPath = findPartPathToRoot("body", root);
            List<ModelPart> barrelPath = findPartPathToRoot("barrel", root);
            List<ModelPart> glassPath = findPartPathToRoot("glass", root);

            // 重置所有部件的姿势
            root.getAllParts().forEach(ModelPart::resetPose);

            // 设置狙击手模式相关的可见性
            boolean flag = false; // 暂时禁用狙击手模式
            if (!barrelPath.isEmpty()) barrelPath.get(0).visible = flag;
            if (!glassPath.isEmpty()) glassPath.get(0).visible = flag;

            // 应用动画状态
            if (zombotanyEntity.getAnimationState("idle") != null) {
                animate(zombotanyEntity.getAnimationState("idle"), PeaShooterAnimation.idle, ageInTicks, hierarchicalModel);
            }
            if (zombotanyEntity.getAnimationState("shoot") != null) {
                animate(zombotanyEntity.getAnimationState("shoot"), PeaShooterAnimation.shoot, ageInTicks, hierarchicalModel);
            }

            // 应用姿势调整
            if (!bodyPath.isEmpty()) bodyPath.get(0).xRot -= flag ? 0.25F : 0;
            if (!headPath.isEmpty()) headPath.get(0).xRot += flag ? 0.25F : 0;
        }
    }
}