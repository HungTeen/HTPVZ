package com.hungteen.pvz.generator.tag;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZBiomes;
import com.hungteen.pvz.common.tags.PVZBiomeTags;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.tags.BiomeTagsProvider;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.level.biome.Biomes;
import net.minecraftforge.common.data.ExistingFileHelper;
import org.jetbrains.annotations.Nullable;

public class BiomeTagGen extends BiomeTagsProvider {
    public BiomeTagGen(DataGenerator p_211094_, @Nullable ExistingFileHelper existingFileHelper) {
        super(p_211094_, PVZMod.MODID, existingFileHelper);
    }

    @Override
    public void addTags() {
        this.tag(PVZBiomeTags.HAS_GREEN_HOUSE).add(PVZBiomes.GARDEN_PLAINS.get());
        this.tag(PVZBiomeTags.HAS_GARDEN_PORTAL).add(PVZBiomes.GARDEN_PLAINS.get());
        this.tag(PVZBiomeTags.HAS_GARDEN_SHELVES).add(PVZBiomes.GARDEN_PLAINS.get(), PVZBiomes.GARDEN_MUSHROOM.get());
        this.tag(PVZBiomeTags.HAS_SACRIFICIAL_VENUE).add(Biomes.NETHER_WASTES, Biomes.SOUL_SAND_VALLEY);
        this.tag(PVZBiomeTags.HAS_INVASION_RUIN).add(Biomes.PLAINS, Biomes.SUNFLOWER_PLAINS, Biomes.SNOWY_PLAINS, Biomes.ICE_SPIKES, Biomes.DESERT
                , Biomes.SWAMP, Biomes.MANGROVE_SWAMP, Biomes.FOREST, Biomes.FLOWER_FOREST, Biomes.BIRCH_FOREST, Biomes.DARK_FOREST
                , Biomes.OLD_GROWTH_BIRCH_FOREST, Biomes.OLD_GROWTH_PINE_TAIGA, Biomes.OLD_GROWTH_SPRUCE_TAIGA
                , Biomes.TAIGA, Biomes.SNOWY_TAIGA, Biomes.SAVANNA, Biomes.SAVANNA_PLATEAU
                , Biomes.WINDSWEPT_HILLS, Biomes.WINDSWEPT_GRAVELLY_HILLS, Biomes.WINDSWEPT_FOREST, Biomes.WINDSWEPT_SAVANNA
                , Biomes.JUNGLE, Biomes.SPARSE_JUNGLE, Biomes.BAMBOO_JUNGLE, Biomes.BADLANDS, Biomes.ERODED_BADLANDS, Biomes.WOODED_BADLANDS
                , Biomes.MEADOW, Biomes.GROVE, Biomes.SNOWY_SLOPES);
        this.tag(BiomeTags.HAS_CLOSER_WATER_FOG).add(PVZBiomes.GARDEN_PLAINS.get(), PVZBiomes.GARDEN_MUSHROOM.get(), PVZBiomes.GARDEN_RIVER.get());
        this.tag(PVZBiomeTags.UNABLE_SUN_PRODUCTION).add(PVZBiomes.GARDEN_PLAINS.get(), PVZBiomes.GARDEN_ISLAND.get(), PVZBiomes.GARDEN_MUSHROOM.get(), PVZBiomes.GARDEN_RIVER.get());
        this.tag(PVZBiomeTags.UNABLE_SUN_FALLING).add(PVZBiomes.GARDEN_PLAINS.get(), PVZBiomes.GARDEN_ISLAND.get(), PVZBiomes.GARDEN_MUSHROOM.get(), PVZBiomes.GARDEN_RIVER.get());
        this.tag(PVZBiomeTags.UNABLE_MOOBLOOM_SPAWNING).add(Biomes.END_MIDLANDS);
        this.tag(PVZBiomeTags.EXTRA_MOOBLOOM_SPAWNING).add(Biomes.MEADOW, Biomes.FLOWER_FOREST, Biomes.SUNFLOWER_PLAINS);
    }
}
