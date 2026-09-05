package com.hungteen.pvz.client.model.zombie;

import com.hungteen.pvz.client.model.plants.animation.SnowPeaModelAnimation;
import com.hungteen.pvz.common.entity.zombies.zombotany.PeaShooterZombie;
import net.minecraft.client.model.geom.ModelPart;

public class SnowPeaZombieModel<T extends PeaShooterZombie> extends ZombotanyModel<T>{
    public SnowPeaZombieModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void setupAnim(T zombie, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.attachedHead != null) {
            this.attachedHead.getAllParts().forEach(ModelPart::resetPose);
            super.setupAnim(zombie, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.animate(zombie.idleAnimationState, SnowPeaModelAnimation.idle, ageInTicks);
            this.animate(zombie.shootAnimationState, SnowPeaModelAnimation.shoot, ageInTicks);
            return;
        }
        super.setupAnim(zombie, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}
