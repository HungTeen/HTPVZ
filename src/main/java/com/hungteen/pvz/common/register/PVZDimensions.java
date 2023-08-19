package com.hungteen.pvz.common.register;

import com.hungteen.pvz.Util;
import com.hungteen.pvz.common.world.zen_garden.ZenGardenBiomeSource;
import com.hungteen.pvz.common.world.zen_garden.ZenGardenChunkGenerator;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class PVZDimensions {
    public static final ResourceKey<Level> ZEN_GARDEN = ResourceKey.create(Registry.DIMENSION_REGISTRY, Util.prefix("zen_garden"));

    public static void register(){
        Registry.register(Registry.CHUNK_GENERATOR, Util.prefix("zen_garden_chunk_gen"), ZenGardenChunkGenerator.CODEC);
        Registry.register(Registry.BIOME_SOURCE, Util.prefix("zen_garden_biomes"), ZenGardenBiomeSource.CODEC);
    }
}
