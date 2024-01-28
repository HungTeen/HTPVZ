package com.hungteen.pvz.common.world.zen_garden;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.DimensionSpecialEffects;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.event.RegisterDimensionSpecialEffectsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class ZenGardenEffects extends DimensionSpecialEffects {

    public ZenGardenEffects() {
        super(96F, false, DimensionSpecialEffects.SkyType.NORMAL, false, true);
    }

    @Override
    public Vec3 getBrightnessDependentFogColor(Vec3 vec3, float brightness) {
        return vec3.multiply(brightness * 0.94F + 0.06F, brightness * 0.94F + 0.06F, brightness * 0.91F + 0.09F);
    }

    @Override
    public boolean isFoggyAt(int p_108874_, int p_108875_) {
        return true;
    }

    @SubscribeEvent
    public static void register(RegisterDimensionSpecialEffectsEvent ev) {
        ev.register(Util.prefix("zen_garden"), new ZenGardenEffects());
    }
}
