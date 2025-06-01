package com.hungteen.pvz.common.world.structures;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZStructures;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.material.Fluids;

import java.util.Optional;

public class SacrificialVenueStructure extends Structure {
    public static final Codec<Structure> CODEC = RecordCodecBuilder.create((p_229304_) -> {
        return p_229304_.group(settingsCodec(p_229304_)).apply(p_229304_, SacrificialVenueStructure::new);
    });

    public SacrificialVenueStructure(StructureSettings settings) {
        super(settings);
    }
    public Optional<Structure.GenerationStub> findGenerationPoint(Structure.GenerationContext context) {
        BlockPos worldPos = context.chunkPos().getWorldPosition();
        BlockPos pos = new BlockPos(worldPos.getX(), 31, worldPos.getZ());
        boolean summonable = heightAvailable(context, pos);
        PVZMod.LOGGER.info("attempting to generate structure at " + pos + " and " + (summonable ? "succeeded" : "failed"));
        return summonable ? Optional.of(new Structure.GenerationStub(pos, structurePiecesBuilder -> structurePiecesBuilder.addPiece(
//                        new ShipwreckPieces.ShipwreckPiece(context.structureTemplateManager(), new ResourceLocation("shipwreck/with_mast"), pos, Rotation.NONE, true)
                        new SacrificialVenueStructurePiece(context.structureTemplateManager(), new StructurePlaceSettings(), pos)
                ))): Optional.empty();
    }

    private static boolean heightAvailable(Structure.GenerationContext context, BlockPos pos) {
        ChunkGenerator generator = context.chunkGenerator();
        LevelHeightAccessor accessor = context.heightAccessor();
        RandomState randomState = context.randomState();
        for (int x = -1; x < 2; x ++) {
            for (int z = -1; z < 2; z ++) {
                NoiseColumn column = generator.getBaseColumn(pos.getX() + 16 + x * 3, pos.getZ() + 16 + z * 3, accessor, randomState);
                if (! column.getBlock(31).getFluidState().is(Fluids.LAVA) || ! column.getBlock(32).isAir()) return false;
            }
        }
        return true;
    }

    public StructureType<?> type() {
        return PVZStructures.SACRIFICIAL_VENUE_TYPE.get();
    }

}
