package com.hungteen.pvz.common.world.zen_garden;

import com.hungteen.pvz.common.register.PVZBiomes;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.core.SectionPos;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.DensityFunction;
import net.minecraft.world.level.levelgen.GenerationStep;

import java.util.List;
import java.util.stream.Collectors;

import static net.minecraft.data.worldgen.placement.VegetationPlacements.treePlacement;

public class ZenGardenBiomeSource extends BiomeSource {

    public static final Codec<ZenGardenBiomeSource> CODEC = RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY)
            .xmap(ZenGardenBiomeSource::new, ZenGardenBiomeSource::getBiomeRegistry).codec();
    private final Holder<Biome> end;
    private final Holder<Biome> midlands;
    private final Holder<Biome> highlands;
    private final Holder<Biome> islands;
    private final Registry<Biome> biomeRegistry;
    private static final List<ResourceKey<Biome>> SPAWN = List.of(PVZBiomes.GARDEN_PLAINS.getKey());


    public ZenGardenBiomeSource(Registry<Biome> biomeRegistry) {
        super(getStartBiomes(biomeRegistry));
        this.biomeRegistry = biomeRegistry;
        end = biomeRegistry.getHolderOrThrow(Biomes.THE_VOID);
        midlands = biomeRegistry.getHolderOrThrow(PVZBiomes.GARDEN_PLAINS.getKey());
        highlands = biomeRegistry.getHolderOrThrow(PVZBiomes.GARDEN_PLAINS.getKey());
        islands = biomeRegistry.getHolderOrThrow(Biomes.SMALL_END_ISLANDS);
    }

    private static List<Holder<Biome>> getStartBiomes(Registry<Biome> registry) {
        return SPAWN.stream().map(s -> registry.getHolderOrThrow(ResourceKey.create(BuiltinRegistries.BIOME.key(), s.location()))).collect(Collectors.toList());
    }

    public Registry<Biome> getBiomeRegistry() {
        return biomeRegistry;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

//    @Override
//    public BiomeSource withSeed(long seed) {
//        return this;
//    }

    @Override
    public Holder<Biome> getNoiseBiome(int ix, int iy, int iz, Climate.Sampler sampler) {
        int x = QuartPos.toBlock(ix);
        int y = QuartPos.toBlock(iy);
        int z = QuartPos.toBlock(iz);
        int a = SectionPos.blockToSectionCoord(x);
        int b = SectionPos.blockToSectionCoord(z);
        if ((long)a * (long)a + (long)b * (long)b >= 4096L) {
            return this.end;
        } else {
            int x1 = (SectionPos.blockToSectionCoord(x) * 2 + 1) * 8;
            int z1 = (SectionPos.blockToSectionCoord(z) * 2 + 1) * 8;
            double weight = sampler.erosion().compute(new DensityFunction.SinglePointContext(x1, y, z1));
            if (weight > 0.25D) {
                return this.highlands;
            } else if (weight >= -0.0625D) {
                return this.midlands;
            } else {
                return this.islands;
            }
        }
    }



    //biomes

    public static Biome gardenPlains() {
        MobSpawnSettings.Builder mobSpawnBuilder = new MobSpawnSettings.Builder();
//        mobSpawnBuilder.addSpawn(MobCategory.AXOLOTLS, new MobSpawnSettings.SpawnerData(EntityType.AXOLOTL, 10, 4, 6));
//        mobSpawnBuilder.addSpawn(MobCategory.WATER_AMBIENT, new MobSpawnSettings.SpawnerData(EntityType.TROPICAL_FISH, 25, 8, 8));
        BiomeDefaultFeatures.commonSpawns(mobSpawnBuilder);
        BiomeGenerationSettings.Builder BiomeGenBuilder = new BiomeGenerationSettings.Builder();
//        globalOverworldGeneration(BiomeGenBuilder);
        BiomeDefaultFeatures.addPlainGrass(BiomeGenBuilder);
        BiomeDefaultFeatures.addDefaultFlowers(BiomeGenBuilder);
        addNutTrees(BiomeGenBuilder);
        BiomeDefaultFeatures.addLushCavesVegetationFeatures(BiomeGenBuilder);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES);
        return PVZBiomes.biome(Biome.Precipitation.RAIN, 0.9F, 0.5F, mobSpawnBuilder, BiomeGenBuilder, music);
    }

    public static void addNutTrees(BiomeGenerationSettings.Builder builder) {
        PVZBiomes.checkFeatures();
        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, NutTreeGrower.TREES_NUT_PF);
    }

}
