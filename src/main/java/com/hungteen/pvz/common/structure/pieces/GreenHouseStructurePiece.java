package com.hungteen.pvz.common.structure.pieces;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZStructurePieces;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.ScatteredFeaturePiece;

public class GreenHouseStructurePiece extends ScatteredFeaturePiece {

    public GreenHouseStructurePiece(CompoundTag tag) {
        super(PVZStructurePieces.GREEN_HOUSE.get(), tag);
    }

    public GreenHouseStructurePiece(WorldgenRandom worldgenRandom, int x, int y) {
        super(PVZStructurePieces.GREEN_HOUSE.get(), x, 64, y, 21, 15, 21, getRandomHorizontalDirection(worldgenRandom));
    }

    @Override
    public void postProcess(WorldGenLevel p_226769_, StructureManager p_226770_, ChunkGenerator p_226771_, RandomSource p_226772_, BoundingBox p_226773_, ChunkPos p_226774_, BlockPos p_226775_) {
        PVZMod.LOGGER.info("structure summoned. ");
    }
}
