package com.hungteen.pvz.client.model.zombie;

import com.hungteen.pvz.client.model.plants.animation.GatlingPeaModelAnimation;
import com.hungteen.pvz.common.entity.zombies.zombotany.GatlingPeaZombie;
import net.minecraft.client.model.geom.ModelPart;

public class GatlingPeaZombieModel<T extends GatlingPeaZombie> extends ZombotanyModel<T>{
    public GatlingPeaZombieModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void setupAnim(T zombie, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.attachedHead != null) {
            this.attachedHead.getAllParts().forEach(ModelPart::resetPose);
            super.setupAnim(zombie, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.animate(zombie.idleAnimationState, GatlingPeaModelAnimation.idle, ageInTicks);
            this.animate(zombie.shootAnimationState, GatlingPeaModelAnimation.shoot, ageInTicks);
            return;
        }
        super.setupAnim(zombie, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}
