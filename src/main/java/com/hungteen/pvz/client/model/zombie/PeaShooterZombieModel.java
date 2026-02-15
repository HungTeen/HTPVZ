package com.hungteen.pvz.client.model.zombie;

import com.hungteen.pvz.client.model.plants.animation.PeaShooterModelAnimation;
import com.hungteen.pvz.common.entity.zombies.zombotany.PeaShooterZombie;
import net.minecraft.client.model.geom.ModelPart;

public class PeaShooterZombieModel<T extends PeaShooterZombie> extends ZombotanyModel<T>{
    public PeaShooterZombieModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void setupAnim(T zombie, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.attachedHead != null) {
            if (attachedHead.hasChild("barrel")) {
                attachedHead.getChild("barrel").visible = zombie.isInSniperMode;
            }
            if (attachedHead.hasChild("glass")) {
                attachedHead.getChild("glass").visible = zombie.isInSniperMode;
            }
            this.attachedHead.getAllParts().forEach(ModelPart::resetPose);
            super.setupAnim(zombie, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            this.animate(zombie.idleAnimationState, PeaShooterModelAnimation.idle, ageInTicks);
            this.animate(zombie.shootAnimationState, PeaShooterModelAnimation.shoot, ageInTicks);
            return;
        }
        super.setupAnim(zombie, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}
