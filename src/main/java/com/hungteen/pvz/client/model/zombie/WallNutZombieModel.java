package com.hungteen.pvz.client.model.zombie;

import com.hungteen.pvz.common.entity.zombies.zombotany.WallNutZombie;
import net.minecraft.client.model.geom.ModelPart;

public class WallNutZombieModel<T extends WallNutZombie> extends ZombotanyModel<T>{
    public WallNutZombieModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void setupAnim(T zombie, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.attachedHead != null) {
            int tmp = zombie.tickCount % 120;
            if (attachedHead.hasChild("body")) {
                ModelPart body1 = attachedHead.getChild("body");
                body1.getChild("in0").z = !(tmp == 1 || tmp == 2) ? 0F : 0.1F;
                body1.getChild("in1").z = tmp == 2 ? 0F : 0.1F;
                body1.getChild("in2").z = tmp == 1 ? 0F : 0.1F;
            }
        }
        super.setupAnim(zombie, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}
