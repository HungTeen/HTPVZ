package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.registries.DeferredRegister;

public class PVZStats {

    public static final DeferredRegister<ResourceLocation> STATS = DeferredRegister.create(Registry.CUSTOM_STAT_REGISTRY, PVZMod.MODID);
    public static final ResourceLocation COLLECT_SUN = makeStat("collect_sun");
    public static final ResourceLocation COLLECT_SUN_VALUE = makeStat("collect_sun_value");
    public static final ResourceLocation USE_SUN = makeStat("use_sun");
    public static final ResourceLocation INVASION_WAVES = makeStat("invasion_waves");
    public static final ResourceLocation INVASIONS = makeStat("invasions");
    public static final ResourceLocation INVASIONS_WON = makeStat("invasions_won");
    public static final ResourceLocation PLANT = makeStat("plant");
    public static final ResourceLocation SHOVEL_PLANT = makeStat("shovel_plant");

    private static ResourceLocation makeStat(String name) {
        return makeStat(name, Util.prefix(name));
    }
    private static ResourceLocation makeStat(String name, ResourceLocation location) {
        STATS.register(name, () -> location);
        return location;
    }
}
