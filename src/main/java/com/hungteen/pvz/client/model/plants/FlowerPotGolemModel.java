package com.hungteen.pvz.client.model.plants;

import com.hungteen.pvz.client.model.plants.animation.FlowerPotModelAnimation;
import com.hungteen.pvz.common.entity.creatures.FlowerPotGolem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ArmedModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.LayerDefinition;
import net.minecraft.world.entity.HumanoidArm;

/**
 * @author PangTeen
 * @program HTPVZ
 * @create 2026/10/26 23:00
 **/
public class FlowerPotGolemModel<T extends FlowerPotGolem> extends HierarchicalModel<T> implements ArmedModel {

    private final ModelPart root;

    public FlowerPotGolemModel(ModelPart root) {
        this.root = root.getChild("total");
    }

    public static LayerDefinition createBodyLayer() {
        return FlowerPotModel.createBodyLayer();
    }

    @Override
    public void setupAnim(T golem, float limbSwing, float limbSwingAmount, float ageInTicks,
                          float netHeadYaw, float headPitch) {
        this.root.getAllParts().forEach(ModelPart::resetPose);
        this.animate(golem.idleAnimationState, FlowerPotModelAnimation.idle, ageInTicks);
    }

    @Override
    public void translateToHand(HumanoidArm arm, PoseStack stack) {
        this.root.translateAndRotate(stack);
        stack.translate(0.0D, 0.55D, 0.32D);
    }

    @Override
    public ModelPart root() {
        return this.root;
    }
}
