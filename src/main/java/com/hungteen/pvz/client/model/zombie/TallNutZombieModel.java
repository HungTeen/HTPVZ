package com.hungteen.pvz.client.model.zombie;

import com.hungteen.pvz.common.entity.zombies.zombotany.TallNutZombie;
import net.minecraft.client.model.geom.ModelPart;

public class TallNutZombieModel<T extends TallNutZombie> extends ZombotanyModel<T>{
    public TallNutZombieModel(ModelPart modelPart) {
        super(modelPart);
    }

    @Override
    public void setupAnim(T zombie, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (this.attachedHead != null) {
            if (attachedHead.hasChild("eye_close")) {
                attachedHead.getChild("eye_close").z = zombie.tickCount % 120 < 2 ? 0F : 0.1F;
            }
            attachedHead.getChild("vine").visible = false;
        }
        super.setupAnim(zombie, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
    }
}
