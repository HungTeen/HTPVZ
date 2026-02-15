package com.hungteen.pvz.common.world.zen_garden;

import com.hungteen.pvz.common.register.OtherRegisters;
import com.hungteen.pvz.common.register.PVZBiomes;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZParticles;
import com.mojang.serialization.Codec;
import net.minecraft.core.Direction;
import net.minecraft.core.Holder;
import net.minecraft.core.QuartPos;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.data.worldgen.BiomeDefaultFeatures;
import net.minecraft.data.worldgen.features.CaveFeatures;
import net.minecraft.data.worldgen.features.VegetationFeatures;
import net.minecraft.data.worldgen.placement.CavePlacements;
import net.minecraft.data.worldgen.placement.OrePlacements;
import net.minecraft.data.worldgen.placement.PlacementUtils;
import net.minecraft.data.worldgen.placement.VegetationPlacements;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.Music;
import net.minecraft.sounds.Musics;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.valueproviders.ConstantInt;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.*;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.blockpredicates.BlockPredicate;
import net.minecraft.world.level.levelgen.placement.*;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.hungteen.pvz.common.world.zen_garden.ZenGardenChunkGenerator.ISLAND_DISTANCE;
import static net.minecraft.data.worldgen.placement.VegetationPlacements.treePlacement;

/**Temporary work. will be replaced somewhen.*/
public class ZenGardenBiomeSource extends BiomeSource {

    public static final Codec<ZenGardenBiomeSource> CODEC = RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY)
            .xmap(ZenGardenBiomeSource::new, ZenGardenBiomeSource::getBiomeRegistry).codec();
    private final Holder<Biome> end;
    private final Holder<Biome> plain;
    private final Holder<Biome> mushroom;
    private final Holder<Biome> river;
    private final Holder<Biome> island;
    private final Registry<Biome> biomeRegistry;
    public static final Holder<PlacedFeature> GARDEN_CLAY = PlacementUtils.register("pvz:garden_clay", CaveFeatures.LUSH_CAVES_CLAY,
            CountPlacement.of(8), InSquarePlacement.spread(),
            PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT, EnvironmentScanPlacement.scanningFor(
                    Direction.DOWN, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
            RandomOffsetPlacement.vertical(ConstantInt.of(1)), BiomeFilter.biome());
    public static final Holder<PlacedFeature> GARDEN_ROOTED_AZALEA_TREE = PlacementUtils.register("pvz:garden_rooted_azalea_tree", CaveFeatures.ROOTED_AZALEA_TREE,
            CountPlacement.of(4), InSquarePlacement.spread(),
            PlacementUtils.RANGE_BOTTOM_TO_MAX_TERRAIN_HEIGHT, EnvironmentScanPlacement.scanningFor(
                    Direction.UP, BlockPredicate.solid(), BlockPredicate.ONLY_IN_AIR_PREDICATE, 12),
            RandomOffsetPlacement.vertical(ConstantInt.of(-1)), BiomeFilter.biome());
    public static final Holder<PlacedFeature> HUGE_MUSHROOMS_GARDEN = PlacementUtils.register("pvz:huge_mushrooms_garden", VegetationFeatures.MUSHROOM_ISLAND_VEGETATION,
            List.copyOf(treePlacement(PlacementUtils.countExtra(12, 0.1F, 1))));


    public ZenGardenBiomeSource(Registry<Biome> biomeRegistry) {
        super(() -> getStartBiomes(biomeRegistry));
        this.biomeRegistry = biomeRegistry;
        end = biomeRegistry.getHolderOrThrow(Biomes.THE_VOID);
        plain = biomeRegistry.getHolderOrThrow(PVZBiomes.GARDEN_PLAINS.getKey());
        mushroom = biomeRegistry.getHolderOrThrow(PVZBiomes.GARDEN_MUSHROOM.getKey());
        river = biomeRegistry.getHolderOrThrow(PVZBiomes.GARDEN_RIVER.getKey());
        island = biomeRegistry.getHolderOrThrow(PVZBiomes.GARDEN_ISLAND.getKey());
    }

    private static List<Holder<Biome>> getStartBiomes(Registry<Biome> registry) {
        return Stream.of(Biomes.THE_VOID, PVZBiomes.GARDEN_PLAINS.getKey(), PVZBiomes.GARDEN_MUSHROOM.getKey(), PVZBiomes.GARDEN_RIVER.getKey(), PVZBiomes.GARDEN_ISLAND.getKey())
                .map(s -> registry.getHolderOrThrow(ResourceKey.create(BuiltinRegistries.BIOME.key(), s.location()))).collect(Collectors.toList());
    }

    public Registry<Biome> getBiomeRegistry() {
        return biomeRegistry;
    }

    @Override
    protected Codec<? extends BiomeSource> codec() {
        return CODEC;
    }

    @Override
    public Holder<Biome> getNoiseBiome(int ix, int iy, int iz, Climate.Sampler sampler) {
        int x = QuartPos.toBlock(ix);
        int y = QuartPos.toBlock(iy);
        int z = QuartPos.toBlock(iz);
        x = x - Math.round(((float) (x / 16)) / ISLAND_DISTANCE) * ISLAND_DISTANCE * 16;
        z = z - Math.round(((float) (z / 16)) / ISLAND_DISTANCE) * ISLAND_DISTANCE * 16;
        if (x * x + z * z >= 25000) {
            return this.end;
        } else if (y < 65) {
            return this.island;
        } else if (y > 190) {
        return this.mushroom;
        } else {
//            if (isOnRiver(x, z)) {
//                return this.river;
//            } else
                if (y < 140) {
                return this.plain;
            } else {
                return this.mushroom;
            }
        }
    }

    //biomes

    public static Biome gardenPlains() {
        MobSpawnSettings.Builder mobSpawnBuilder = new MobSpawnSettings.Builder();
        mobSpawnBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(PVZEntities.MOOBLOOM.get(), 50, 3, 4));
        mobSpawnBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(PVZEntities.SNAIL.get(), 10, 3, 4));
        mobSpawnBuilder.addSpawn(OtherRegisters.PVZPlantMobCategory, new MobSpawnSettings.SpawnerData(PVZEntities.VELOCI_RADISH.get(), 80, 5, 8));
        mobSpawnBuilder.addSpawn(OtherRegisters.PVZPlantMobCategory, new MobSpawnSettings.SpawnerData(PVZEntities.PEA_SHOOTER.get(), 15, 2, 3));
        mobSpawnBuilder.addSpawn(OtherRegisters.PVZPlantMobCategory, new MobSpawnSettings.SpawnerData(PVZEntities.SUN_FLOWER.get(), 40, 4, 6));
        mobSpawnBuilder.addSpawn(OtherRegisters.PVZPlantMobCategory, new MobSpawnSettings.SpawnerData(PVZEntities.WALL_NUT.get(), 20, 4, 6));
        mobSpawnBuilder.addSpawn(OtherRegisters.PVZPlantMobCategory, new MobSpawnSettings.SpawnerData(PVZEntities.TALL_NUT.get(), 1, 1, 1));
        BiomeGenerationSettings.Builder biomeGenBuilder = new BiomeGenerationSettings.Builder();
        addNutTrees(biomeGenBuilder);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUNFLOWER);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_MEADOW);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_PLAIN);
        BiomeDefaultFeatures.addWaterTrees(biomeGenBuilder);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_WATERLILY);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, GARDEN_ROOTED_AZALEA_TREE);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.CLASSIC_VINES);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.GLOW_LICHEN);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.LUSH_CAVES_VEGETATION);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, GARDEN_CLAY);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIRT);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_CLAY);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES);
        return PVZBiomes.biome(Biome.Precipitation.NONE, 0x67c6a6, 0x93ced5, 0x47bbc5, 0x053134,
                0.8F, 0.5F, mobSpawnBuilder, biomeGenBuilder,
                new AmbientParticleSettings(PVZParticles.FOG.get(), 0.00005F), music);
    }
    public static Biome gardenMushroom() {
        MobSpawnSettings.Builder mobSpawnBuilder = new MobSpawnSettings.Builder();
        mobSpawnBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.MOOSHROOM, 25, 4, 8));
        BiomeGenerationSettings.Builder biomeGenBuilder = new BiomeGenerationSettings.Builder();
        biomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.CLASSIC_VINES);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.GLOW_LICHEN);
        BiomeDefaultFeatures.addMushroomFieldVegetation(biomeGenBuilder);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, HUGE_MUSHROOMS_GARDEN);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, GARDEN_CLAY);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIRT);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_CLAY);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES);
        return PVZBiomes.biome(Biome.Precipitation.NONE, 0x7575df, 0xad7ee6, 0x47bbc5, 0x053134,
                0.8F, 0.5F, mobSpawnBuilder, biomeGenBuilder,
                new AmbientParticleSettings(PVZParticles.FOG.get(), 0.00005F), music);
    }

    public static Biome gardenIsland(){
        MobSpawnSettings.Builder mobSpawnBuilder = new MobSpawnSettings.Builder();
        BiomeGenerationSettings.Builder biomeGenBuilder = new BiomeGenerationSettings.Builder();
        biomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, GARDEN_ROOTED_AZALEA_TREE);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.CLASSIC_VINES);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.CAVE_VINES);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(biomeGenBuilder);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_CLAY);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES);
        return PVZBiomes.biome(Biome.Precipitation.NONE, 0x67c6a6, 0x93ced5, 0x47bbc5, 0x053134,
                0.8F, 0.5F, mobSpawnBuilder, biomeGenBuilder,
                new AmbientParticleSettings(PVZParticles.FOG.get(), 0.00005F), music);
    }
    public static Biome gardenRiver(){
        MobSpawnSettings.Builder mobSpawnBuilder = new MobSpawnSettings.Builder();
        BiomeGenerationSettings.Builder biomeGenBuilder = new BiomeGenerationSettings.Builder();
        BiomeDefaultFeatures.addWaterTrees(biomeGenBuilder);
        BiomeDefaultFeatures.addDefaultFlowers(biomeGenBuilder);
        BiomeDefaultFeatures.addDefaultGrass(biomeGenBuilder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(biomeGenBuilder);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_WATERLILY);
        BiomeDefaultFeatures.addDefaultSoftDisks(biomeGenBuilder);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(biomeGenBuilder);
        biomeGenBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_CLAY);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES);
        return PVZBiomes.biome(Biome.Precipitation.RAIN, 0x67c6a6, 0x93ced5, 0x47bbc5, 0x053134,
                0.8F, 0.5F, mobSpawnBuilder, biomeGenBuilder,
                new AmbientParticleSettings(PVZParticles.FOG.get(), 0.00005F), music);
    }

    public static void addNutTrees(BiomeGenerationSettings.Builder builder) {
        PVZBiomes.checkFeatures();
        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, NutTreeGrower.TREES_NUT_PF);
    }
}
