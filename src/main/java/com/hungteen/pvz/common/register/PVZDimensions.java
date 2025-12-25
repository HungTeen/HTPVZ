package com.hungteen.pvz.common.register;

import com.hungteen.pvz.common.world.zen_garden.ZenGardenBiomeSource;
import com.hungteen.pvz.common.world.zen_garden.ZenGardenChunkGenerator;
import com.hungteen.pvz.util.Util;
import net.minecraft.core.Registry;

public class PVZDimensions {

    public static void register() {
        Registry.register(Registry.CHUNK_GENERATOR, Util.prefix("zen_garden_chunk_gen"), ZenGardenChunkGenerator.CODEC);
        Registry.register(Registry.BIOME_SOURCE, Util.prefix("zen_garden_biomes"), ZenGardenBiomeSource.CODEC);
    }
}
