package com.hungteen.pvz.common.world.zen_garden;

import com.hungteen.pvz.Util;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
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

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.Executor;

public class ZenGardenChunkGenerator extends ChunkGenerator {


    private static final Codec<Settings> SETTINGS_CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    Codec.INT.fieldOf("base").forGetter(Settings::baseHeight),
                    Codec.FLOAT.fieldOf("verticalvariance").forGetter(Settings::verticalVariance),
                    Codec.FLOAT.fieldOf("horizontalvariance").forGetter(Settings::horizontalVariance)
            ).apply(instance, Settings::new));

    public static final Codec<ZenGardenChunkGenerator> CODEC = RecordCodecBuilder.create(instance ->
            instance.group(
                    RegistryOps.retrieveRegistry(Registry.STRUCTURE_SET_REGISTRY).forGetter(ZenGardenChunkGenerator::getStructureSetRegistry),
                    RegistryOps.retrieveRegistry(Registry.BIOME_REGISTRY).forGetter(ZenGardenChunkGenerator::getBiomeRegistry),
                    SETTINGS_CODEC.fieldOf("settings").forGetter(ZenGardenChunkGenerator::getSettings)
            ).apply(instance, ZenGardenChunkGenerator::new));

    public static final ResourceLocation ZEN_GARDEN_DIMENSION_SET = Util.prefix("zen_garden_dimension_structure_set");
    private final Settings settings;


    public ZenGardenChunkGenerator(Registry<StructureSet> structureSetRegistry, Registry<Biome> registry, Settings settings) {
        super(structureSetRegistry, getSet(structureSetRegistry), new ZenGardenBiomeSource(registry));
        this.settings = settings;
    }

    @Override
    protected Codec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    private static Optional<HolderSet<StructureSet>> getSet(Registry<StructureSet> structureSetRegistry) {
        HolderSet.Named<StructureSet> structureSet = structureSetRegistry.getOrCreateTag(TagKey.create(Registry.STRUCTURE_SET_REGISTRY, ZEN_GARDEN_DIMENSION_SET));
        return Optional.of(structureSet);
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
    public void buildSurface(WorldGenRegion region, StructureManager featureManager, RandomState p_223052_, ChunkAccess chunk) {
        BlockState bedrock = Blocks.BEDROCK.defaultBlockState();
        BlockState stone = Blocks.STONE.defaultBlockState();
        ChunkPos chunkPos = chunk.getPos();
//        genChunk(region, chunk, chunkPos.x, chunkPos.z);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                chunk.setBlockState(pos.set(x, 0, z), bedrock, false);

//                final int height = ChunkManager.getChunkHeight(chunkPos.x, chunkPos.z, x, z) + 1;
//                int height = getHeightAt(baseHeight, verticalVariance, horizontalVariance, realx, realz);
                for (int y = 1; y < 20; ++y) {
                    chunk.setBlockState(pos.set(x, y, z), stone, false);
                }
//                chunk.setBlockState(pos.set(x, 50, z), bedrock, false);
//                chunk.setBlockState(pos.set(x, 100, z), stone, false);
//                chunk.setBlockState(pos.set(x, 200, z), stone, false);
            }
        }
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
    public int getBaseHeight(int x, int z, Heightmap.Types types, LevelHeightAccessor levelHeightAccessor, RandomState randomState) {
        int baseHeight = settings.baseHeight();
        float verticalVariance = settings.verticalVariance();
        float horizontalVariance = settings.horizontalVariance();
        return getHeightAt(baseHeight, verticalVariance, horizontalVariance, x, z);
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
        return 63;
    }

    @Override
    public void applyCarvers(WorldGenRegion worldGenRegion, long p_223044_, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunkAccess, GenerationStep.Carving carving) {
        // Carvers only work correctly in combination with NoiseBasedChunkGenerator so we keep this empty here.
    }


//    @Override
//    public ChunkGenerator withSeed(long seed) {
//        return new ZenGardenChunkGenerator(getStructureSetRegistry(), getBiomeRegistry(), settings);
//    }

    private int getHeightAt(int baseHeight, float verticalVariance, float horizontalVariance, int x, int z) {
        return (int) (baseHeight + Math.sin(x / horizontalVariance) * verticalVariance + Math.cos(z / horizontalVariance) * verticalVariance);
    }

    private record Settings(int baseHeight, float verticalVariance, float horizontalVariance) {
    }


//    public void genChunk(WorldGenRegion region, ChunkAccess chunk, int chunkX, int chunkZ) {
//        int[][][][] arrows = new int[4][4][19][3];
//        int[][][] blocks = new int[16][300][16];//decide whether a block well be (gen?)ed
//        double tmp;//temp
//        for (int x = 0; x < 4; ++x) {
//            for (int z = 0; z < 4; ++z) {
//                for (int y = 0; y < 19; ++y) {//used rect coordinates
//                    arrows[x][z][y][0] = MathUtil.getRandomInRange(region.getRandom(), 8);//x dimention
//                    arrows[x][z][y][1] = MathUtil.getRandomInRange(region.getRandom(), 8);//z dimention
//                    arrows[x][z][y][2] = MathUtil.getRandomInRange(region.getRandom(), 8);//y dimention
//                }
//            }
//        }
////        System.out.println("what");
//        for (int w = 0; w < 16; ++ w) {//devided into 16 subchunks vertically, marked as w
//            for (int x = 0; x < 16; ++ x) {
//                for (int z = 0; z < 16; ++ z) {
//                    for (int y = 0; y < 16; ++ y) {
//                        blocks[x][y + w * 16][z] = 0;
//                        for (int i = 0; i < 4; ++i) {
//                            for (int j = 0; j < 4; ++j) {
//                                for (int k = 0; k < 4; ++k) {
//                                    tmp = 40 - Math.pow(Math.pow(x - (i - 1) * 16 - arrows[i][j][k + w][0], 2)
//                                            + Math.pow(x - (i - 1) * 16 - arrows[i][j][k + w][1], 2)
//                                            + Math.pow(x - (i - 1) * 16 - arrows[i][j][k + w][2], 2), 0.5);
//                                    if (tmp > 0) {
//                                        blocks[x][y + w * 16][z] +=tmp;
//                                    }
//                                }
//                            }
//                        }
//                        if (blocks[x][y + w * 16][z] < 900) {
//                            chunk.setBlockState(new BlockPos(x, w * 16 + y, z), Blocks.STONE.defaultBlockState(), false);
//                        }
//                    }
//                }
//            }
////            System.out.println(blocks[1][1 + w * 16][1]);
//        }
//    }

//    @Override
//    public Climate.Sampler climateSampler() {
//        return new Climate.Sampler(DensityFunctions.constant(0.0), DensityFunctions.constant(0.0), DensityFunctions.constant(0.0), DensityFunctions.constant(0.0),
//                DensityFunctions.constant(0.0), DensityFunctions.constant(0.0), Collections.emptyList());
//    }
}
