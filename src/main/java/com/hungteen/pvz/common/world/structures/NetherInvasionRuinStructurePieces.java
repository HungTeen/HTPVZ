package com.hungteen.pvz.common.world.structures;

import com.hungteen.pvz.common.register.PVZStructures;
import net.minecraft.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;

public class NetherInvasionRuinStructurePieces {
    private static final ResourceLocation[] PIECES = new ResourceLocation[] {
            new ResourceLocation("pvz:nether_invasion_ruin_chamber"),
            new ResourceLocation("pvz:nether_invasion_ruin_corner"),
            new ResourceLocation("pvz:nether_invasion_ruin_hallway"),
            new ResourceLocation("pvz:nether_invasion_ruin_altar")
    };

    public static void addPieces(StructureTemplateManager p_228535_, StructurePieceAccessor p_228536_, RandomSource p_228537_, BlockPos p_228538_) {
        Rotation rotation = Rotation.getRandom(p_228537_);
        p_228536_.addPiece(new NetherInvasionRuinStructurePiece(p_228535_, Util.getRandom(PIECES, p_228537_), p_228538_, rotation));
    }

    public static class NetherInvasionRuinStructurePiece extends TemplateStructurePiece {
        public NetherInvasionRuinStructurePiece(StructureTemplateManager p_228540_, ResourceLocation p_228541_, BlockPos p_228542_, Rotation p_228543_) {
            super(PVZStructures.NETHER_INVASION_RUIN_PIECE.get(), 0, p_228540_, p_228541_, p_228541_.toString(), makeSettings(p_228543_), p_228542_);
        }

        public NetherInvasionRuinStructurePiece(StructureTemplateManager p_228545_, CompoundTag p_228546_) {
            super(PVZStructures.NETHER_INVASION_RUIN_PIECE.get(), p_228546_, p_228545_, (p_228568_) -> {
                return makeSettings(Rotation.valueOf(p_228546_.getString("Rot")));
            });
        }

        private static StructurePlaceSettings makeSettings(Rotation p_228556_) {
            return (new StructurePlaceSettings()).setRotation(p_228556_).setMirror(Mirror.NONE).addProcessor(BlockIgnoreProcessor.STRUCTURE_AND_AIR);
        }

        protected void addAdditionalSaveData(StructurePieceSerializationContext p_228558_, CompoundTag p_228559_) {
            super.addAdditionalSaveData(p_228558_, p_228559_);
            p_228559_.putString("Rot", this.placeSettings.getRotation().name());
        }

        protected void handleDataMarker(String p_228561_, BlockPos p_228562_, ServerLevelAccessor p_228563_, RandomSource p_228564_, BoundingBox p_228565_) {
        }

        public void postProcess(WorldGenLevel p_228548_, StructureManager p_228549_, ChunkGenerator p_228550_, RandomSource p_228551_, BoundingBox p_228552_, ChunkPos p_228553_, BlockPos p_228554_) {
            p_228552_.encapsulate(this.template.getBoundingBox(this.placeSettings, this.templatePosition));
            super.postProcess(p_228548_, p_228549_, p_228550_, p_228551_, p_228552_, p_228553_, p_228554_);
        }
    }
}