package com.hungteen.pvz.common.world.structures;

import com.hungteen.pvz.common.register.PVZStructures;
import com.hungteen.pvz.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class SacrificialVenueStructurePiece extends TemplateStructurePiece {
    public SacrificialVenueStructurePiece(StructureTemplateManager templateManager, StructurePlaceSettings settings, BlockPos pos) {
        super(PVZStructures.SACRIFICIAL_VENUE_PIECE.get(), 0, templateManager,
                Util.prefix("sacrificial_venue"), "pvz_sv", settings, pos);
    }

    public SacrificialVenueStructurePiece(StructureTemplateManager structureTemplateManager, CompoundTag tag) {
        super(PVZStructures.SACRIFICIAL_VENUE_PIECE.get(), tag, structureTemplateManager, (p_227512_) -> new StructurePlaceSettings());
    }

    @Override
    protected void handleDataMarker(String p_226906_, BlockPos p_226907_, ServerLevelAccessor p_226908_, RandomSource p_226909_, BoundingBox p_226910_) {
    }
}
