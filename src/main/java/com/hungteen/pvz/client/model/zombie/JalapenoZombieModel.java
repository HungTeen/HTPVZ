package com.hungteen.pvz.client.model.zombie;

import com.hungteen.pvz.client.model.plants.animation.JalapenoAnimation;
import com.hungteen.pvz.common.entity.zombies.zombotany.JalapenoZombie;
import net.minecraft.client.model.geom.ModelPart;

public class JalapenoZombieModel<T extends JalapenoZombie> extends ZombotanyModel<T>{
    public JalapenoZombieModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void setupAnim(T zombie, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.attachedHead != null) {
            this.attachedHead.getAllParts().forEach(ModelPart::resetPose);
            if (attachedHead.hasChild("inner")) {
                ModelPart inner = attachedHead.getChild("inner");
                inner.x = (float) (zombie.getRandom().nextFloat() * 0.5 - 0.25);
                inner.z = (float) (zombie.getRandom().nextFloat() * 0.5 - 0.25);
            }
            this.animate(zombie.explodeAnimationState, JalapenoAnimation.explode, ageInTicks);
        }
        super.setupAnim(zombie, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}
