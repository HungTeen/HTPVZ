package com.hungteen.pvz.common.world.zen_garden;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.Registry;
import net.minecraft.core.Vec3i;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.KeyDispatchDataCodec;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.synth.NormalNoise;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executor;

public class ZenGardenChunkGenerator extends NoiseBasedChunkGenerator {

    public static final Codec<ZenGardenChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RegistryOps.retrieveRegistry(Registry.STRUCTURE_SET_REGISTRY).forGetter(ZenGardenChunkGenerator::getStructureSetRegistry),
                    RegistryOps.retrieveRegistry(Registry.NOISE_REGISTRY).forGetter(g -> g.noises),
                    RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(ZenGardenChunkGenerator::getBiomeRegistry),
                    NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(g -> g.settings)
            ).apply(instance, ZenGardenChunkGenerator::new));
    public static final int ISLAND_DISTANCE = 128;

    private final Registry<NormalNoise.NoiseParameters> noises;
    private final IslandsFunction islands;
    private volatile long islandSeed = 0;

    public ZenGardenChunkGenerator(Registry<StructureSet> structureSetRegistry, Registry<NormalNoise.NoiseParameters> noises,
                                   Registry<Biome> biomeRegistry, Holder<NoiseGeneratorSettings> settings) {
        super(structureSetRegistry, noises, new ZenGardenBiomeSource(biomeRegistry), settings);
        this.noises = noises;
        this.islands = findIslands(settings.value().noiseRouter().finalDensity());
    }

    private static IslandsFunction findIslands(DensityFunction function) {
        if (function instanceof IslandsFunction islands) {
            return islands;
        }
        if (function instanceof DensityFunctions.HolderHolder holder) {
            return findIslands(holder.function().value());
        }
        return null;
    }

    private void updateIslandRandom(RandomState randomState) {
        if (this.islands == null) {
            return;
        }
        long seed = randomState.legacyLevelSeed();
        if (seed != this.islandSeed) {
            this.islands.setRandomFactory(new LegacyRandomSource(seed).forkPositional());
            this.islandSeed = seed;
        }
    }

    @Override
    protected @NotNull Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public Registry<Biome> getBiomeRegistry() {
        return ((ZenGardenBiomeSource) biomeSource).getBiomeRegistry();
    }

    public Registry<StructureSet> getStructureSetRegistry() {
        return structureSets;
    }

    public static Vec3i getMainIslandPos(double chunkX, double chunkZ) {
        return new Vec3i(
                Math.round(chunkX / ISLAND_DISTANCE) * ISLAND_DISTANCE * 16,
                80,
                Math.round(chunkZ / ISLAND_DISTANCE) * ISLAND_DISTANCE * 16);
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        this.updateIslandRandom(randomState);
        return super.getBaseHeight(x, z, types, levelHeightAccessor, randomState);
    }

    @Override
    public @NotNull NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        this.updateIslandRandom(randomState);
        return super.getBaseColumn(x, z, levelHeightAccessor, randomState);
    }

    @Override
    public @NotNull CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        this.updateIslandRandom(randomState);
        return super.fillFromNoise(executor, blender, randomState, structureManager, chunk);
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structureManager, RandomState randomState, ChunkAccess chunk) {
        this.updateIslandRandom(randomState);
        super.buildSurface(region, structureManager, randomState, chunk);
        this.fillRingRiverWater(chunk);
    }

    private void fillRingRiverWater(ChunkAccess chunk) {
        IslandsFunction islands = this.islands;
        if (islands == null) {
            return;
        }
        BlockState water = Blocks.WATER.defaultBlockState();
        ChunkPos chunkPos = chunk.getPos();
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int worldX = chunkPos.getMinBlockX() + x;
                int worldZ = chunkPos.getMinBlockZ() + z;
                for (int waterTop : islands.ringRiverWaterSurfaces(worldX, worldZ)) {
                    for (int y = waterTop; y >= chunk.getMinBuildHeight(); y--) {
                        pos.set(worldX, y, worldZ);
                        if (!chunk.getBlockState(pos).isAir()) {
                            break;
                        }
                        chunk.setBlockState(pos, water, false);
                    }
                }
            }
        }
    }

    public static class IslandsFunction implements DensityFunction.SimpleFunction {

        public static final KeyDispatchDataCodec<IslandsFunction> CODEC = KeyDispatchDataCodec.of(
                RecordCodecBuilder.mapCodec(instance -> instance.group(
                        Codec.STRING.fieldOf("random_factory").forGetter(IslandsFunction::getFactoryName)
                ).apply(instance, IslandsFunction::new)));

        private static final int SEA_LEVEL = 77;
        private static final PositionalRandomFactory FALLBACK_FACTORY =
                new LegacyRandomSource(0L).forkPositional();

        private final String factoryName;

        private volatile PositionalRandomFactory randomFactory;
        private final Map<Long, IslandData> islandDataCache = new ConcurrentHashMap<>();
        private final Map<Long, int[]> smallJitterCache = new ConcurrentHashMap<>();
        private final Map<Long, int[]> bigJitterCache = new ConcurrentHashMap<>();

        public IslandsFunction(String factoryName) {
            this.factoryName = factoryName;
        }

        public String getFactoryName() {
            return factoryName;
        }

        private RandomSource randomAt(int x, int z) {
            PositionalRandomFactory factory = this.randomFactory;
            return (factory == null ? FALLBACK_FACTORY : factory).at(x, 0, z);
        }

        private int[] jitter(int cellX, int cellZ, int range) {
            Map<Long, int[]> cache = range == 16 ? this.smallJitterCache : this.bigJitterCache;
            return cache.computeIfAbsent(pack(cellX, cellZ), k -> {
                RandomSource random = randomAt(cellX, cellZ);
                return new int[]{random.nextInt(range * 2 + 1) - range, random.nextInt(range * 2 + 1) - range};
            });
        }

        private IslandData islandData(int islandCenterX, int islandCenterZ) {
            return this.islandDataCache.computeIfAbsent(pack(islandCenterX, islandCenterZ), k -> {
                RandomSource random = randomAt(islandCenterX, islandCenterZ);
                int ringX = islandCenterX + (random.nextInt(10) + 15) * (random.nextBoolean() ? 1 : -1);
                int ringRadius = random.nextInt(30) + 60;
                int ringZ = islandCenterZ + (random.nextInt(10) + 15) * (random.nextBoolean() ? 1 : -1);
                List<Vec3> floats = new ArrayList<>();
                float angle = random.nextFloat() * 2.5F + 3.14F;
                floats.add(new Vec3(ringX + ringRadius * Mth.sin(angle), 150, ringZ + ringRadius * Mth.cos(angle)));
                angle = random.nextFloat() * 2.5F + 3.14F;
                floats.add(new Vec3(ringX + ringRadius * Mth.sin(angle), 175, ringZ + ringRadius * Mth.cos(angle)));
                angle = random.nextFloat() * 2.5F;
                floats.add(new Vec3(ringX + ringRadius * Mth.sin(angle), 175, ringZ + ringRadius * Mth.cos(angle)));
                angle = random.nextFloat() * 2.5F;
                floats.add(new Vec3(ringX + ringRadius * Mth.sin(angle), 200, ringZ + ringRadius * Mth.cos(angle)));
                return new IslandData(ringX, ringRadius, ringZ, floats);
            });
        }

        private static long pack(int x, int z) {
            return ((long) x << 32) | (z & 0xffffffffL);
        }

        @Override
        public double compute(DensityFunction.FunctionContext context) {
            final int x = context.blockX();
            final int y = context.blockY();
            final int z = context.blockZ();

            Vec3i mainIsland = getMainIslandPos(x / 16.0, z / 16.0);
            IslandData data = islandData(mainIsland.getX(), mainIsland.getZ());

            double density = islandDensity(x, y, z, mainIsland, 100, 60, data, 7, 5);
            for (Vec3 island : data.floatIslands) {
                if (Math.abs(x - island.x) < 50 && Math.abs(z - island.z) < 50) {
                    double floatDensity = islandDensity(x, y, z,
                            new Vec3i((int) island.x, (int) island.y, (int) island.z), 40, 15, data,
                            4, 3);
                    density = Math.max(density, floatDensity);
                }
            }
            return density;
        }

        public List<Integer> ringRiverWaterSurfaces(int x, int z) {
            List<Integer> surfaces = new ArrayList<>();
            Vec3i mainIsland = getMainIslandPos(x / 16.0, z / 16.0);
            IslandData data = islandData(mainIsland.getX(), mainIsland.getZ());
            addWaterSurface(surfaces, riverWaterSurface(x, z, mainIsland, 100, 60, data, 7, 5, SEA_LEVEL));
            for (Vec3 island : data.floatIslands) {
                if (Math.abs(x - island.x) < 50 && Math.abs(z - island.z) < 50) {
                    addWaterSurface(surfaces, riverWaterSurface(x, z,
                            new Vec3i((int) island.x, (int) island.y, (int) island.z), 40, 15, data,
                            4, 3, (int) island.y - 1));
                }
            }
            return surfaces;
        }

        private static void addWaterSurface(List<Integer> surfaces, int surface) {
            if (surface != Integer.MIN_VALUE) {
                surfaces.add(surface);
            }
        }

        private int riverWaterSurface(int x, int z, Vec3i center, int width, int height, IslandData data,
                                      int riverWidth, int maxRiverDepth, int seaLevel) {
            float[] bounds = columnBounds(x, z, center, width, height);
            int depth = riverDepth(x, z, data, bounds[0], bounds[1], riverWidth, maxRiverDepth);
            if (depth <= 0) {
                return Integer.MIN_VALUE;
            }
            if (Mth.floor(bounds[1] - depth) < bounds[0]) {
                return Integer.MIN_VALUE;
            }
            if (bounds[1] < seaLevel) {
                return Integer.MIN_VALUE;
            }
            return seaLevel - 1;
        }

        private double islandDensity(int x, int y, int z, Vec3i center, int width, int height,
                                     IslandData data, int riverWidth, int maxRiverDepth) {
            float[] bounds = columnBounds(x, z, center, width, height);
            float from = bounds[0];
            float to = bounds[1];

            if (y > to || y < from) {
                return -1;
            }
            int depth = riverDepth(x, z, data, from, to, riverWidth, maxRiverDepth);
            if (depth > 0 && y > to - depth) {
                return -1.0;
            }
            return Math.min(1, (to - y) * 0.25 + 0.5);
        }

        private float[] columnBounds(int x, int z, Vec3i center, int width, int height) {
            int cellX = Math.floorDiv(x, 16);
            int cellZ = Math.floorDiv(z, 16);
            int localX = x - cellX * 16;
            int localZ = z - cellZ * 16;

            float from = center.getY() - height;
            float to = center.getY();
            // small noice
            for (int cx = 0; cx < 4; cx++) {
                for (int cz = 0; cz < 4; cz++) {
                    int[] vec = jitter(cellX - 1 + cx, cellZ - 1 + cz, 16);
                    int affX = localX - 16 * (cx - 1) - vec[0];
                    int affZ = localZ - 16 * (cz - 1) - vec[1];
                    int dist = affX * affX + affZ * affZ - 256;
                    if (dist < 0) {
                        from -= (float) dist / 50;
                        to += (float) dist / 300;
                    }
                }
            }
            // large noise for big island
            if (width > 100) {
                int bigCellX = Math.floorDiv(cellX, 4);
                int bigCellZ = Math.floorDiv(cellZ, 4);
                for (int cx = 0; cx < 4; cx++) {
                    for (int cz = 0; cz < 4; cz++) {
                        int[] vec = jitter(bigCellX - 1 + cx, bigCellZ - 1 + cz, 64);
                        int affX = x - bigCellX * 64 - 64 * (cx - 1) - vec[0];
                        int affZ = z - bigCellZ * 64 - 64 * (cz - 1) - vec[1];
                        int dist = affX * affX + affZ * affZ - 4096;
                        if (dist < 0) {
                            from -= (float) dist / 400;
                            to -= (float) dist / 10000;
                        }
                    }
                }
            }
            int dist = (center.getX() - x) * (center.getX() - x) + (center.getZ() - z) * (center.getZ() - z);
            int affY = height * dist / (width * width);
            to += 0.02f * affY;
            from = Math.max(from + affY, 0);
            return new float[]{from, to};
        }

        private int riverDepth(int x, int z, IslandData data, float from, float to, int riverWidth, int maxRiverDepth) {
            double ringDist = Math.sqrt((x - data.ringX) * (x - data.ringX) + (z - data.ringZ) * (z - data.ringZ));
            int depth = Mth.clamp((int) (riverWidth - Math.abs(ringDist - data.ringRadius)), 0, maxRiverDepth);
            if (depth > 0 && depth >= to - from) {
                depth = Math.max((int) (to - from) - 1, 1);
            }
            return depth;
        }

        @Override
        public double minValue() {
            return -1;
        }

        @Override
        public double maxValue() {
            return 1;
        }

        @Override
        public @NotNull KeyDispatchDataCodec<? extends DensityFunction> codec() {
            return CODEC;
        }

        public void setRandomFactory(PositionalRandomFactory factory) {
            this.randomFactory = factory;
            this.islandDataCache.clear();
            this.smallJitterCache.clear();
            this.bigJitterCache.clear();
        }

        private record IslandData(int ringX, int ringRadius, int ringZ, List<Vec3> floatIslands) {
        }
    }

    public static NoiseGeneratorSettings zenGardenNoiseSettings(Registry<NormalNoise.NoiseParameters> noises) {
        NoiseSettings noiseSettings = NoiseSettings.create(0, 256, 1, 2);
        DensityFunction islands = new IslandsFunction("zen_garden_islands");
        DensityFunction zero = DensityFunctions.zero();
        DensityFunction barrier = DensityFunctions.zero();
        DensityFunction floodedness = DensityFunctions.constant(-1);
        DensityFunction spread = DensityFunctions.zero();
        DensityFunction lava = DensityFunctions.zero();
        NoiseRouter router = new NoiseRouter(barrier, floodedness, spread, lava, zero, zero, zero, zero, zero, zero, islands, islands, zero, zero, zero);
        SurfaceRules.RuleSource ruleSource = SurfaceRules.sequence(
                SurfaceRules.ifTrue(SurfaceRules.isBiome(ResourceKey.create(Registry.BIOME_REGISTRY, com.hungteen.pvz.util.Util.prefix("garden_mushroom")))
                        , SurfaceRules.state(Blocks.MYCELIUM.defaultBlockState())),
                SurfaceRules.ifTrue(SurfaceRules.ON_FLOOR, SurfaceRules.state(Blocks.GRASS_BLOCK.defaultBlockState())),
                SurfaceRules.ifTrue(SurfaceRules.UNDER_FLOOR, SurfaceRules.state(Blocks.DIRT.defaultBlockState())));
        return new NoiseGeneratorSettings(noiseSettings, Blocks.STONE.defaultBlockState(), Blocks.WATER.defaultBlockState(), router, ruleSource
                , List.of(), 77, false, true, false, false);
    }

}
