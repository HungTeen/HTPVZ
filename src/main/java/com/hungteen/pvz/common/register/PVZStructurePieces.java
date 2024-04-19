package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.structure.pieces.GreenHouseStructurePiece;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Locale;

public class PVZStructurePieces {
    public static final DeferredRegister<StructurePieceType> PIECE_TYPES = DeferredRegister.create(Registry.STRUCTURE_PIECE.key(), PVZMod.MODID);
//    public static RegistryObject<StructurePieceType> GREEN_HOUSE = PIECE_TYPES.register("green_house", () -> new GreenHouseStructurePiece::new);
    public static RegistryObject<StructurePieceType> GREEN_HOUSE = setPieceId("green_house", GreenHouseStructurePiece::new);
    private static RegistryObject<StructurePieceType> setCPieceId(String id, StructurePieceType type) {
//        return Registry.register(Registry.STRUCTURE_PIECE, "pvz" + id.toLowerCase(Locale.ROOT), type);
        return PIECE_TYPES.register(id.toLowerCase(Locale.ROOT), () -> type);
    }
    private static RegistryObject<StructurePieceType> setPieceId(String id, StructurePieceType.ContextlessType piece) {
        return setCPieceId(id, piece);
    }
    private static RegistryObject<StructurePieceType> setPieceId(String id, StructurePieceType.StructureTemplateType piece) {
        return setCPieceId(id, piece);
    }
}
