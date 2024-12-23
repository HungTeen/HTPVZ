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
        this.tag(BiomeTags.HAS_CLOSER_WATER_FOG).add(PVZBiomes.GARDEN_PLAINS.get(), PVZBiomes.GARDEN_MUSHROOM.get(), PVZBiomes.GARDEN_RIVER.get());
        this.tag(PVZBiomeTags.UNABLE_SUN_PRODUCTION).add(PVZBiomes.GARDEN_PLAINS.get(), PVZBiomes.GARDEN_ISLAND.get(), PVZBiomes.GARDEN_MUSHROOM.get(), PVZBiomes.GARDEN_RIVER.get());
        this.tag(PVZBiomeTags.UNABLE_SUN_FALLING).add(PVZBiomes.GARDEN_PLAINS.get(), PVZBiomes.GARDEN_ISLAND.get(), PVZBiomes.GARDEN_MUSHROOM.get(), PVZBiomes.GARDEN_RIVER.get());
        this.tag(PVZBiomeTags.UNABLE_MOOBLOOM_SPAWNING).add(Biomes.END_MIDLANDS);
        this.tag(PVZBiomeTags.EXTRA_MOOBLOOM_SPAWNING).add(Biomes.MEADOW, Biomes.FLOWER_FOREST, Biomes.SUNFLOWER_PLAINS);
    }
}
