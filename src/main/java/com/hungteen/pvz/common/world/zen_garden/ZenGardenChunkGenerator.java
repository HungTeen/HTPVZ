package com.hungteen.pvz.common.world.zen_garden;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.util.Util;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.*;
import net.minecraft.resources.RegistryOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.*;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.*;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.levelgen.structure.StructureSet;

import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

/**Temporary work. Will be replaced somewhen.*/
public class ZenGardenChunkGenerator extends ChunkGenerator {


    private static final Codec<Settings> SETTINGS_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("base").forGetter(Settings::baseHeight),
                    Codec.FLOAT.fieldOf("verticalvariance").forGetter(Settings::verticalVariance),
                    Codec.FLOAT.fieldOf("horizontalvariance").forGetter(Settings::horizontalVariance),
                    RegistryCodecs.homogeneousList(Registry.STRUCTURE_SET_REGISTRY).optionalFieldOf("structure_overrides").forGetter(Settings::structureOverrides)
            ).apply(instance, Settings::new));

    public static final Codec<ZenGardenChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RegistryOps.retrieveRegistry(Registry.STRUCTURE_SET_REGISTRY).forGetter(ZenGardenChunkGenerator::getStructureSetRegistry),
                    RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(ZenGardenChunkGenerator::getBiomeRegistry),
                    SETTINGS_CODEC.fieldOf("settings").forGetter(ZenGardenChunkGenerator::getSettings)
            ).apply(instance, ZenGardenChunkGenerator::new));

    private final Settings settings;

    private Random random = null;
    private Vec3i mainIslandPos = null;
    private List<Vec3i> floatIslands = null;
    private Vec3i riverCircle = null;
    private final Map<Pair<Integer, Integer>, Pair<Integer, Integer>> smallVectorTable = new HashMap<>();
    private final Map<Pair<Integer, Integer>, Pair<Integer, Integer>> bigVectorTable = new HashMap<>();
    private final BlockState stone = Blocks.STONE.defaultBlockState();
    private final BlockState dirt = Blocks.DIRT.defaultBlockState();
    private final BlockState water = Blocks.WATER.defaultBlockState();
    private final BlockState grass = Blocks.GRASS_BLOCK.defaultBlockState();
    private final BlockState mycelium = Blocks.MYCELIUM.defaultBlockState();

    //TODO still not finished: 1) wisdom tree. 2) mobs (Garden Bee, Redstone Bug, Snail, Snailrillum). 3) BGM. 4) multi main islands.


    public ZenGardenChunkGenerator(Registry<StructureSet> structureSetRegistry, Registry<Biome> registry, Settings settings) {
        super(structureSetRegistry, settings.structureOverrides(), new ZenGardenBiomeSource(registry));
        this.settings = settings;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public Settings getSettings() {
        return settings;
    }

    public Registry<Biome> getBiomeRegistry() {
        return ((ZenGardenBiomeSource) biomeSource).getBiomeRegistry();
    }

    public Registry<StructureSet> getStructureSetRegistry() {
        return structureSets;
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager featureManager, RandomState randomState, ChunkAccess chunk) {
        init(randomState);
        ChunkPos chunkPos = chunk.getPos();
        if (chunkPos.x * chunkPos.x + chunkPos.z * chunkPos.z > 200) {
            return;
        }
        Pair<Integer, Integer> yRegion;
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                int riverDepth = riverDepth(chunkPos.x * 16 + x, chunkPos.z * 16 + z, 9, 7);
                yRegion = getBlockHeight(chunkPos, x, z, mainIslandPos, randomState, 150, 60);
                fillInIsland(chunk, yRegion, riverDepth, x, z, getSeaLevel(), grass);
                riverDepth = riverDepth(chunkPos.x * 16 + x, chunkPos.z * 16 + z, 5, 2);
                for (Vec3i island : floatIslands) {
                    if (Math.abs(x + chunkPos.x * 16 - island.getX()) < 50 && Math.abs(z + chunkPos.z * 16 - island.getZ()) < 50) {
                        yRegion = getBlockHeight(chunkPos, x, z, island, randomState, 40, 23);
                        fillInIsland(chunk, yRegion, riverDepth, x, z, island.getY() - 2, mycelium);
                    }
                }
            }
        }
        PVZMod.LOGGER.info("generated: (" + chunkPos.x + ", " + chunkPos.z + ")");
    }

    public void init(RandomState randomState) {
        if (random == null) {
            random = new Random();

            mainIslandPos = new Vec3i(0, 80, 0);
            random.setSeed(randomState.legacyLevelSeed());
            riverCircle = new Vec3i((random.nextInt(10) + 15) * (random.nextBoolean() ? 1 : -1),
                    random.nextInt(30) + 100,
                    (random.nextInt(10) + 15) * (random.nextBoolean() ? 1 : -1));
            floatIslands = new ArrayList<>();
            double angle = random.nextFloat() * 6.28;
            floatIslands.add(new Vec3i(riverCircle.getX() + riverCircle.getY() * Math.sin(angle),
                    150, riverCircle.getZ() + riverCircle.getY() * Math.cos(angle)));
            angle = random.nextFloat() * 2.5 + 3.14;
            floatIslands.add(new Vec3i(riverCircle.getX() + riverCircle.getY() * Math.sin(angle),
                    175, riverCircle.getZ() + riverCircle.getY() * Math.cos(angle)));
            angle = random.nextFloat() * 2.5;
            floatIslands.add(new Vec3i(riverCircle.getX() + riverCircle.getY() * Math.sin(angle),
                    175, riverCircle.getZ() + riverCircle.getY() * Math.cos(angle)));
            angle = random.nextFloat() * 6.28;
            floatIslands.add(new Vec3i(riverCircle.getX() + riverCircle.getY() * Math.sin(angle),
                    200, riverCircle.getZ() + riverCircle.getY() * Math.cos(angle)));
        }
    }
    private int riverDepth(int x, int z, int width, int maxDepth) {
        int depth = width - Math.abs((int) Math.pow((x - riverCircle.getX()) * (x - riverCircle.getX())
                + (z - riverCircle.getZ()) * (z - riverCircle.getZ()), 0.5) - riverCircle.getY());
        return Math.min(Math.max(depth, 0), maxDepth);
    }
    private void fillInIsland(ChunkAccess chunk, Pair<Integer, Integer> yRegion, int riverDepth, int x, int z, int seaLevel, BlockState surface) {
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        if (yRegion.getFirst() <= yRegion.getSecond()) {
            if (riverDepth > 0) {
                for (int y = yRegion.getFirst(); y < yRegion.getSecond() - riverDepth; ++ y) {
                    chunk.setBlockState(pos.set(x, y, z), (double) (y - yRegion.getFirst()) / (yRegion.getSecond() - yRegion.getFirst()) > 0.8 ? dirt : stone, false);
                }
                chunk.setBlockState(pos.set(x, yRegion.getSecond() - riverDepth, z), surface, false);
                for (int y = yRegion.getSecond() - riverDepth + 1; y < seaLevel; ++ y) {
                    chunk.setBlockState(pos.set(x, y, z), water, false);
                }
            } else {
                for (int y = yRegion.getFirst(); y < yRegion.getSecond(); ++y) {
                    chunk.setBlockState(pos.set(x, y, z), (double) (y - yRegion.getFirst()) / (yRegion.getSecond() - yRegion.getFirst()) > 0.8 ? dirt : stone, false);
                }
                chunk.setBlockState(pos.set(x, yRegion.getSecond(), z), surface, false);
            }
        }
    }

    private Pair<Integer, Integer> getBlockHeight(ChunkPos pos, int x, int z, Vec3i islandPosition, RandomState randomState, int width, int height) {
        int chunkx = pos.x;
        int chunkz = pos.z;
        List<Pair<Integer, Integer>> smallVectorList = new ArrayList<>();
        List<Pair<Integer, Integer>> bigVectorList = new ArrayList<>();
        for (int cx = 0; cx < 4; cx ++) {
            for (int cz = 0; cz < 4; cz ++) {
                smallVectorList.add(getChunkVector(chunkx - 1 + cx, chunkz - 1 + cz, randomState));
            }
        }
        if (width > 100) {
            for (int cx = 0; cx < 4; cx ++) {
                for (int cz = 0; cz < 4; cz ++) {
                    bigVectorList.add(getBigChunkVector((chunkx / 4) - 1 + cx, (chunkz / 4) - 1 + cz, randomState));
                }
            }
        }
        float from = islandPosition.getY() - height;
        float to = islandPosition.getY();
        for (int i = 0; i < smallVectorList.size(); i++) {
            int affx = x - 16 * (i / 4 - 1) - smallVectorList.get(i).getFirst();
            int affz = z - 16 * (i % 4 - 1) - smallVectorList.get(i).getSecond();
            int dist = affx * affx + affz * affz - 256;
            from -= dist < 0 ? (float) dist / 50 : 0;
            to += dist < 0 ? (float) dist / 300: 0;
        }
        if (width > 100) {
            for (int i = 0; i < bigVectorList.size(); i++) {
                int affx = x + chunkx % 4 * 16 - 64 * (i / 4 - 1) - bigVectorList.get(i).getFirst();
                int affz = z + chunkz % 4 * 16 - 64 * (i % 4 - 1) - bigVectorList.get(i).getSecond();
                int dist = affx * affx + affz * affz - 4096;
                from -= dist < 0 ? (float) dist / 400 : 0;
                to -= dist < 0 ? (float) dist / 10000 : 0;
            }
        }
        int dist = (islandPosition.getX() - chunkx * 16 - x) * (islandPosition.getX() - chunkx * 16 - x) +
                (islandPosition.getZ() - chunkz * 16 - z) * (islandPosition.getZ() - chunkz * 16 - z);
        int affy = height * dist / (width * width);
        to += 0.02 * affy;
        from += affy;
        if (from < 0) {
            from = 0;
        }
        return Pair.of((int) from, (int) to);
    }
    private Pair<Integer, Integer> getChunkVector(int x, int z, RandomState randomState){
        if (smallVectorTable.containsKey(Pair.of(x, z))) {
            return smallVectorTable.get(Pair.of(x, z));
        }
        random.setSeed((randomState.legacyLevelSeed() >> 48) * x + (randomState.legacyLevelSeed() >> 47) * z);
        Pair<Integer, Integer> vector = Pair.of(random.nextInt(32) - 16, random.nextInt(32) - 16);
        smallVectorTable.put(Pair.of(x, z), vector);
        return vector;
    }
    private Pair<Integer, Integer> getBigChunkVector(int x, int z, RandomState randomState){
        if (bigVectorTable.containsKey(Pair.of(x, z))) {
            return bigVectorTable.get(Pair.of(x, z));
        }
        random.setSeed((randomState.legacyLevelSeed() >> 47) * x + (randomState.legacyLevelSeed() >> 48) * z);
        Pair<Integer, Integer> vector = Pair.of(random.nextInt(128) - 64, random.nextInt(128) - 64);
        bigVectorTable.put(Pair.of(x, z), vector);
        return vector;
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Executor executor, Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunkAccess) {
        return CompletableFuture.completedFuture(chunkAccess);
    }

    // This makes sure passive mob spawning works for generated chunks. i.e. mobs that spawn during the creation of chunks themselves.
    @Override
    public void spawnOriginalMobs(WorldGenRegion level) {
        ChunkPos chunkpos = level.getCenter();
        Holder<Biome> biome = level.getBiome(chunkpos.getWorldPosition().atY(level.getMaxBuildHeight() - 1));
        WorldgenRandom worldgenrandom = new WorldgenRandom(new LegacyRandomSource(RandomSupport.generateUniqueSeed()));
        worldgenrandom.setDecorationSeed(level.getSeed(), chunkpos.getMinBlockX(), chunkpos.getMinBlockZ());
        NaturalSpawner.spawnMobsForChunkGeneration(level, biome, chunkpos, worldgenrandom);
    }

    @Override
    public int getMinY() {
        return 0;
    }

    // Make sure this is correctly implemented so that structures and features can use this.
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types types, LevelHeightAccessor accessor, RandomState randomState) {
        init(randomState);
        Pair<Integer, Integer> pair = getBlockHeight(new ChunkPos(x / 16, z / 16), x % 16, z % 16, mainIslandPos, randomState, 150, 60);
        return pair.getSecond() > pair.getFirst() ? pair.getSecond() + 1 : 257;
    }

    // Make sure this is correctly implemented so that structures and features can use this.
    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        int y = getBaseHeight(x, z, Heightmap.Types.WORLD_SURFACE_WG, levelHeightAccessor, randomState);
        BlockState stone = Blocks.STONE.defaultBlockState();
        BlockState[] states = new BlockState[y];
        states[0] = Blocks.BEDROCK.defaultBlockState();
        for (int i = 1; i < y; i++) {
            states[i] = stone;
        }
        return new NoiseColumn(levelHeightAccessor.getMinBuildHeight(), states);
    }

    @Override
    public void addDebugScreenInfo(List<String> list, RandomState randomState, BlockPos pos) {

    }

    @Override
    public int getGenDepth() {
        return 256;
    }

    @Override
    public int getSeaLevel() {
        return 77;
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long p_223044_, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
        // Carvers only work correctly in combination with NoiseBasedChunkGenerator so we keep this empty here.
    }

    private record Settings(int baseHeight, float verticalVariance, float horizontalVariance, Optional<HolderSet<StructureSet>> structureOverrides) {
    }
}
