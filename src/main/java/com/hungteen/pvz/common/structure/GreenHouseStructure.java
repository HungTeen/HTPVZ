package com.hungteen.pvz.common.structure;

import com.hungteen.pvz.common.structure.pieces.GreenHouseStructurePiece;
import net.minecraft.world.level.levelgen.structure.SinglePieceStructure;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.structures.DesertPyramidPiece;

import java.util.Optional;

public class GreenHouseStructure extends SinglePieceStructure {

    public GreenHouseStructure(PieceConstructor p_226537_, int p_226538_, int p_226539_, StructureSettings p_226540_) {
        super(p_226537_, p_226538_, p_226539_, p_226540_);
    }
    public GreenHouseStructure(Structure.StructureSettings p_227418_) {
        super(GreenHouseStructurePiece::new, 21, 21, p_227418_);
    }
    @Override
    public Optional<GenerationStub> findGenerationPoint(GenerationContext p_226571_) {
        return Optional.empty();
    }

    @Override
    public StructureType<?> type() {
        return null;
    }
}
