package com.hungteen.pvz.common.world.structures;

import com.hungteen.pvz.common.register.PVZStructures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.EmptyBlockGetter;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.levelgen.WorldGenerationContext;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.heightproviders.HeightProvider;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;

import java.util.Optional;

public class NetherInvasionRuinStructure extends Structure {
    public static final Codec<NetherInvasionRuinStructure> CODEC = RecordCodecBuilder.create((p_228585_) -> {
        return p_228585_.group(settingsCodec(p_228585_), HeightProvider.CODEC.fieldOf("height").forGetter((p_228583_) -> {
            return p_228583_.height;
        })).apply(p_228585_, NetherInvasionRuinStructure::new);
    });
    public final HeightProvider height;

    public NetherInvasionRuinStructure(Structure.StructureSettings p_228573_, HeightProvider p_228574_) {
        super(p_228573_);
        this.height = p_228574_;
    }

    public Optional<GenerationStub> findGenerationPoint(Structure.GenerationContext p_228576_) {
        WorldgenRandom worldgenrandom = p_228576_.random();
        int i = p_228576_.chunkPos().getMinBlockX() + worldgenrandom.nextInt(16);
        int j = p_228576_.chunkPos().getMinBlockZ() + worldgenrandom.nextInt(16);
        int k = p_228576_.chunkGenerator().getSeaLevel();
        WorldGenerationContext worldgenerationcontext = new WorldGenerationContext(p_228576_.chunkGenerator(), p_228576_.heightAccessor());
        int l = this.height.sample(worldgenrandom, worldgenerationcontext);
        NoiseColumn noisecolumn = p_228576_.chunkGenerator().getBaseColumn(i, j, p_228576_.heightAccessor(), p_228576_.randomState());
        BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos(i, l, j);

        while(l > k) {
            BlockState blockstate = noisecolumn.getBlock(l);
            --l;
            BlockState blockstate1 = noisecolumn.getBlock(l);
            if (blockstate.isAir() && (blockstate1.is(Blocks.SOUL_SAND) || blockstate1.isFaceSturdy(EmptyBlockGetter.INSTANCE, blockpos$mutableblockpos.setY(l), Direction.UP))) {
                break;
            }
        }

        if (l <= k) {
            return Optional.empty();
        } else {
            BlockPos blockpos = new BlockPos(i, l, j);
            return Optional.of(new Structure.GenerationStub(blockpos, (p_228581_) -> {
                NetherInvasionRuinStructurePieces.addPieces(p_228576_.structureTemplateManager(), p_228581_, worldgenrandom, blockpos);
            }));
        }
    }

    public StructureType<?> type() {
        return PVZStructures.NETHER_INVASION_RUIN_TYPE.get();
    }
}