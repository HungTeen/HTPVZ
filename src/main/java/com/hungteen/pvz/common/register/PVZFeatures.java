package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.world.zen_garden.LunarStoneFeature;
import net.minecraft.world.level.levelgen.feature.Feature;
import net.minecraft.world.level.levelgen.feature.configurations.NoneFeatureConfiguration;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PVZFeatures {

    public static final DeferredRegister<Feature<?>> FEATURES = DeferredRegister.create(ForgeRegistries.FEATURES, PVZMod.MODID);

    //zen garden.
    //public static final RegistryObject<Feature<NoneFeatureConfiguration>> LUNAR_STONE =  FEATURES.register("lunar_stone", () -> new LunarStoneFeature(NoneFeatureConfiguration.CODEC));

}
