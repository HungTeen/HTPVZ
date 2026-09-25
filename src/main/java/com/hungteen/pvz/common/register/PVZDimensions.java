package com.hungteen.pvz.common.register;

import com.hungteen.pvz.common.world.zen_garden.ZenGardenBiomeSource;
import com.hungteen.pvz.common.world.zen_garden.ZenGardenChunkGenerator;
import com.hungteen.pvz.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

public class PVZDimensions {

    public static final ResourceLocation ZEN_GARDEN = Util.prefix("zen_garden");

    public static void register() {
        Registry.register(Registry.CHUNK_GENERATOR, Util.prefix("zen_garden_chunk_gen"), ZenGardenChunkGenerator.CODEC);
        Registry.register(Registry.BIOME_SOURCE, Util.prefix("zen_garden_biomes"), ZenGardenBiomeSource.CODEC);
        // Register the custom density function type and the noise settings so the
        // zen garden dimension runs on the vanilla noise pipeline.
        Registry.register(Registry.DENSITY_FUNCTION_TYPES, Util.prefix("zen_garden_islands"),
                ZenGardenChunkGenerator.IslandsFunction.CODEC.codec());
        BuiltinRegistries.register(BuiltinRegistries.NOISE_GENERATOR_SETTINGS,
                ResourceKey.create(Registry.NOISE_GENERATOR_SETTINGS_REGISTRY, Util.prefix("zen_garden_noise")),
                ZenGardenChunkGenerator.zenGardenNoiseSettings(BuiltinRegistries.NOISE));
    }
}
