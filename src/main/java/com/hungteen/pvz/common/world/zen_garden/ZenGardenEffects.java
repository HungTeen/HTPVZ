package com.hungteen.pvz.common.world.zen_garden;

import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;

public class ZenGardenEffects extends DimensionSpecialEffects {


    public ZenGardenEffects() {
        super(64F, false, DimensionSpecialEffects.SkyType.NORMAL, false, false);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 p_108878_, float p_108879_) {
        return null;
    }

    @Override
    public boolean isFoggyAt(int p_108874_, int p_108875_) {
        return true;
    }
}
