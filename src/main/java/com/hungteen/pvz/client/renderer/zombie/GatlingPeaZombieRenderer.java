package com.hungteen.pvz.client.renderer.zombie;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.model.plants.animation.GatlingPeaAnimation;
import com.hungteen.pvz.common.entity.zombies.zombotany.GatlingPeaZombie;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.List;

public class GatlingPeaZombieRenderer extends ZombotanyRenderer<GatlingPeaZombie> {

    public GatlingPeaZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void setupPlantAnim(GatlingPeaZombie zombotanyEntity, EntityModel<?> plantModel, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (plantModel instanceof HierarchicalModel<?> hierarchicalModel) {
            ModelPart root = hierarchicalModel.root();
            // 获取所需的部件
            List<ModelPart> headPath = findPartPathToRoot("head", root);
            List<ModelPart> bodyPath = findPartPathToRoot("body", root);
            List<ModelPart> barrelPath = findPartPathToRoot("barrel", root);
            List<ModelPart> glassPath = findPartPathToRoot("glass", root);

            // 重置所有部件的姿势
            root.getAllParts().forEach(ModelPart::resetPose);

            // 应用动画状态
            if (zombotanyEntity.getAnimationState("idle") != null) {
                animate(zombotanyEntity.getAnimationState("idle"), GatlingPeaAnimation.idle, ageInTicks, hierarchicalModel);
            }
            if (zombotanyEntity.getAnimationState("shoot") != null) {
                animate(zombotanyEntity.getAnimationState("shoot"), GatlingPeaAnimation.shoot, ageInTicks, hierarchicalModel);
            }
            if (zombotanyEntity.getAnimationState("controlled") != null) {
                animate(zombotanyEntity.getAnimationState("controlled"), GatlingPeaAnimation.controlled_shoot, ageInTicks, hierarchicalModel);
            }
        }
    }
}