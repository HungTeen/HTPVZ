package com.hungteen.pvz.client.renderer.zombie;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.model.plants.animation.JalapenoAnimation;
import com.hungteen.pvz.common.entity.zombies.zombotany.JalapenoZombie;
import com.hungteen.pvz.common.entity.zombies.zombotany.PeaShooterZombie;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

import java.util.List;
import java.util.Random;

public class JalapenoZombieRenderer extends ZombotanyRenderer<JalapenoZombie> {

    public JalapenoZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    protected void setupPlantAnim(JalapenoZombie zombotanyEntity, EntityModel<?> plantModel, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (plantModel instanceof HierarchicalModel<?> hierarchicalModel) {
            ModelPart root = hierarchicalModel.root();

            root.getAllParts().forEach(ModelPart::resetPose);

            // 应用爆炸动画
            if (zombotanyEntity.isExploding()) {
                if (!zombotanyEntity.explodeAnimationState.isStarted()) {
                    zombotanyEntity.explodeAnimationState.start(zombotanyEntity.tickCount);
                }
                List<ModelPart> innerPath = findPartPathToRoot("inner", root);
                if(!innerPath.isEmpty()){
                    innerPath.get(0).x = (float) (new Random().nextFloat()  * 0.5 - 0.25);
                    innerPath.get(0).z = (float) (new Random().nextFloat() * 0.5 - 0.25);
                }
                animate(zombotanyEntity.explodeAnimationState, JalapenoAnimation.explode, ageInTicks, hierarchicalModel);
            } else {
                // 如果不是爆炸状态，停止爆炸动画
                if (zombotanyEntity.explodeAnimationState.isStarted()) {
                    zombotanyEntity.setupPresentationAnim();
                }
            }
        }
    }
    
    @Override
    protected String getPlantModelPartName(){
        return "total";
    }

} 