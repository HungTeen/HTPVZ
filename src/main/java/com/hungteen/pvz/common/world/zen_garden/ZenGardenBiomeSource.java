package com.hungteen.pvz.common.world.zen_garden;

import com.hungteen.pvz.common.register.PVZBiomes;
import com.hungteen.pvz.common.register.PVZEntities;
import com.mojang.serialization.Codec;
import net.minecraft.core.*;
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
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
    private final Vec3i riverCircle;
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
        Random random = new Random(735629912);
        riverCircle = new Vec3i((random.nextInt(10) + 15) * (random.nextBoolean() ? 1 : -1),
                random.nextInt(30) + 100,
                (random.nextInt(10) + 15) * (random.nextBoolean() ? 1 : -1));
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
        if (x * x + z * z >= 30000) {
            return this.end;
        } else if (y < 65) {
            return this.island;
        } else if (y > 190) {
        return this.mushroom;
        } else {
            if (isOnRiver(x, z)) {
                return this.river;
            }
             else if (y < 140) {
                return this.plain;
            } else {
                return this.mushroom;
            }
        }
    }

    private boolean isOnRiver(int x, int z) {
        int depth = 8 - Math.abs((int) Math.pow((x - riverCircle.getX()) * (x - riverCircle.getX())
                + (z - riverCircle.getZ()) * (z - riverCircle.getZ()), 0.5) - riverCircle.getY());
        return depth > 0;
    }


    //biomes

    public static Biome gardenPlains() {
        MobSpawnSettings.Builder mobSpawnBuilder = new MobSpawnSettings.Builder();
        mobSpawnBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(PVZEntities.WALL_NUT.get(), 15, 4, 8));
        BiomeGenerationSettings.Builder BiomeGenBuilder = new BiomeGenerationSettings.Builder();
        addNutTrees(BiomeGenBuilder);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.SURFACE_STRUCTURES, LunarStoneFeature.LUNAR_STONE_PF);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_SUNFLOWER);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.FLOWER_MEADOW);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_GRASS_PLAIN);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, GARDEN_ROOTED_AZALEA_TREE);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.CLASSIC_VINES);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.GLOW_LICHEN);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.LUSH_CAVES_VEGETATION);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, GARDEN_CLAY);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIRT);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_CLAY);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES);
        return PVZBiomes.biome(Biome.Precipitation.RAIN, 0x67c6a6, 0x93ced5, 0x47bbc5, 0x053134, 0.5F, mobSpawnBuilder, BiomeGenBuilder, music);
    }
    public static Biome gardenMushroom() {
        MobSpawnSettings.Builder mobSpawnBuilder = new MobSpawnSettings.Builder();
        mobSpawnBuilder.addSpawn(MobCategory.CREATURE, new MobSpawnSettings.SpawnerData(EntityType.MOOSHROOM, 25, 4, 8));
        BiomeGenerationSettings.Builder BiomeGenBuilder = new BiomeGenerationSettings.Builder();
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.CLASSIC_VINES);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.GLOW_LICHEN);
        BiomeDefaultFeatures.addMushroomFieldVegetation(BiomeGenBuilder);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, HUGE_MUSHROOMS_GARDEN);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, GARDEN_CLAY);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_DIRT);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_CLAY);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES);
        return PVZBiomes.biome(Biome.Precipitation.RAIN, 0x7575df, 0xad7ee6, 0x47bbc5, 0x053134, 0.5F, mobSpawnBuilder, BiomeGenBuilder, music);
    }

    public static Biome gardenIsland(){
        MobSpawnSettings.Builder mobSpawnBuilder = new MobSpawnSettings.Builder();
        BiomeGenerationSettings.Builder BiomeGenBuilder = new BiomeGenerationSettings.Builder();
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, GARDEN_ROOTED_AZALEA_TREE);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.CLASSIC_VINES);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, CavePlacements.CAVE_VINES);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(BiomeGenBuilder);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_CLAY);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES);
        return PVZBiomes.biome(Biome.Precipitation.RAIN, 0x67c6a6, 0x93ced5, 0x47bbc5, 0x053134, 0.5F, mobSpawnBuilder, BiomeGenBuilder, music);
    }
    public static Biome gardenRiver(){
        MobSpawnSettings.Builder mobSpawnBuilder = new MobSpawnSettings.Builder();
        BiomeGenerationSettings.Builder BiomeGenBuilder = new BiomeGenerationSettings.Builder();
        BiomeDefaultFeatures.addWaterTrees(BiomeGenBuilder);
        BiomeDefaultFeatures.addDefaultFlowers(BiomeGenBuilder);
        BiomeDefaultFeatures.addDefaultGrass(BiomeGenBuilder);
        BiomeDefaultFeatures.addDefaultExtraVegetation(BiomeGenBuilder);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, VegetationPlacements.PATCH_WATERLILY);
        BiomeDefaultFeatures.addDefaultSoftDisks(BiomeGenBuilder);
        BiomeDefaultFeatures.addDefaultUndergroundVariety(BiomeGenBuilder);
        BiomeGenBuilder.addFeature(GenerationStep.Decoration.UNDERGROUND_ORES, OrePlacements.ORE_CLAY);
        Music music = Musics.createGameMusic(SoundEvents.MUSIC_BIOME_LUSH_CAVES);
        return PVZBiomes.biome(Biome.Precipitation.RAIN, 0x67c6a6, 0x93ced5, 0x47bbc5, 0x053134, 0.5F, mobSpawnBuilder, BiomeGenBuilder, music);
    }

    public static void addNutTrees(BiomeGenerationSettings.Builder builder) {
        PVZBiomes.checkFeatures();
        builder.addFeature(GenerationStep.Decoration.VEGETAL_DECORATION, NutTreeGrower.TREES_NUT_PF);
    }
}
