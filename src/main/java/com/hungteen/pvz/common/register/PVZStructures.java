package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.tags.PVZBiomeTags;
import com.hungteen.pvz.common.world.structures.SacrificialVenueStructure;
import com.hungteen.pvz.common.world.structures.SacrificialVenueStructurePiece;
import com.hungteen.pvz.util.Util;
import com.mojang.datafixers.util.Pair;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.VerticalAnchor;
import net.minecraft.world.level.levelgen.heightproviders.ConstantHeight;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceType;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.pools.SinglePoolElement;
import net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool;
import net.minecraft.world.level.levelgen.structure.structures.JigsawStructure;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

import static net.minecraft.world.level.levelgen.structure.pools.StructureTemplatePool.Projection;

public class PVZStructures {
    public static final DeferredRegister<StructureSet> STRUCTURE_SETS = DeferredRegister.create(Registry.STRUCTURE_SET_REGISTRY, PVZMod.MODID);
    public static final DeferredRegister<Structure> STRUCTURES = DeferredRegister.create(Registry.STRUCTURE_REGISTRY, PVZMod.MODID);
    public static final DeferredRegister<StructureTemplatePool> TEMPLATE_POOLS = DeferredRegister.create(Registry.TEMPLATE_POOL_REGISTRY, PVZMod.MODID);
    public static final DeferredRegister<StructureType<?>> STRUCTURE_TYPES = DeferredRegister.create(Registry.STRUCTURE_TYPE_REGISTRY, PVZMod.MODID);
    public static final DeferredRegister<StructurePieceType> STRUCTURE_PIECE_TYPES = DeferredRegister.create(Registry.STRUCTURE_PIECE_REGISTRY, PVZMod.MODID);

    //garden_green_houses
    public static final RegistryObject<StructureTemplatePool> GREEN_HOUSE_POOL = TEMPLATE_POOLS.register("green_house", () -> new StructureTemplatePool(Util.prefix("green_house"),
            new ResourceLocation("empty"), List.of(
            Pair.of(SinglePoolElement.single("pvz:garden_house_1").apply(Projection.RIGID), 1),
            Pair.of(SinglePoolElement.single("pvz:garden_house_2").apply(Projection.RIGID), 1)
    )));
    public static final RegistryObject<Structure> GREEN_HOUSE = STRUCTURES.register("green_house", () -> new JigsawStructure(new Structure.StructureSettings(
            biomes(PVZBiomeTags.HAS_GREEN_HOUSE), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.BEARD_THIN
    ), GREEN_HOUSE_POOL.getHolder().get(),7, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
    public static final RegistryObject<StructureSet> GREEN_HOUSE_SET = STRUCTURE_SETS.register("green_house", () ->
            new StructureSet(GREEN_HOUSE.getHolder().get(), new RandomSpreadStructurePlacement(8, 7, RandomSpreadType.LINEAR, 125627497)));

    //garden_shelves
    public static final RegistryObject<StructureTemplatePool> GARDEN_SHELVES_POOL = TEMPLATE_POOLS.register("garden_shelves", () -> new StructureTemplatePool(Util.prefix("garden_shelves"),
            new ResourceLocation("empty"), List.of(
            Pair.of(SinglePoolElement.single("pvz:garden_shelves_1").apply(Projection.RIGID), 1),
            Pair.of(SinglePoolElement.single("pvz:garden_shelves_2").apply(Projection.RIGID), 1),
            Pair.of(SinglePoolElement.single("pvz:garden_shelves_3").apply(Projection.RIGID), 1),
            Pair.of(SinglePoolElement.single("pvz:garden_shelves_4").apply(Projection.RIGID), 1),
            Pair.of(SinglePoolElement.single("pvz:garden_shelves_5").apply(Projection.RIGID), 1)
    )));
    public static final RegistryObject<Structure> GARDEN_SHELVES = STRUCTURES.register("garden_shelves", () -> new JigsawStructure(new Structure.StructureSettings(
            biomes(PVZBiomeTags.HAS_GARDEN_SHELVES), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.BEARD_THIN
    ), GARDEN_SHELVES_POOL.getHolder().get(),7, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
    public static final RegistryObject<StructureSet> GARDEN_SHELVES_SET = STRUCTURE_SETS.register("garden_shelves", () ->
            new StructureSet(GARDEN_SHELVES.getHolder().get(), new RandomSpreadStructurePlacement(4, 2, RandomSpreadType.LINEAR, 113382974)));

    //garden_portal
    public static final RegistryObject<StructureTemplatePool> GARDEN_PORTAL_POOL = TEMPLATE_POOLS.register("garden_portal", () -> new StructureTemplatePool(Util.prefix("garden_portal"),
            new ResourceLocation("empty"), List.of(
            Pair.of(SinglePoolElement.single("pvz:garden_portal").apply(Projection.RIGID), 1)
    )));
    public static final RegistryObject<Structure> GARDEN_PORTAL = STRUCTURES.register("garden_portal", () -> new JigsawStructure(new Structure.StructureSettings(
            biomes(PVZBiomeTags.HAS_GARDEN_PORTAL), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.BEARD_THIN
    ), GARDEN_PORTAL_POOL.getHolder().get(),7, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
    public static final RegistryObject<StructureSet> GARDEN_PORTAL_SET = STRUCTURE_SETS.register("garden_portal", () ->
            new StructureSet(GARDEN_PORTAL.getHolder().get(), new RandomSpreadStructurePlacement(6, 5, RandomSpreadType.LINEAR, 105325493)));

    //sacrificial_venue
    public static final RegistryObject<StructurePieceType> SACRIFICIAL_VENUE_PIECE = STRUCTURE_PIECE_TYPES.register("sacrificial_venue", () -> StructurePieceType.setTemplatePieceId(SacrificialVenueStructurePiece::new, "PVZSV"));
    public static final RegistryObject<StructureTemplatePool> SACRIFICIAL_VENUE_POOL = TEMPLATE_POOLS.register("sacrificial_venue", () -> new StructureTemplatePool(Util.prefix("sacrificial_venue"),
            new ResourceLocation("empty"), List.of(
            Pair.of(SinglePoolElement.single("pvz:sacrificial_venue").apply(Projection.RIGID), 1)
    )));
    public static final RegistryObject<StructureType<?>> SACRIFICIAL_VENUE_TYPE = STRUCTURE_TYPES.register("sacrificial_venue", () -> () -> SacrificialVenueStructure.CODEC);
    public static final RegistryObject<Structure> SACRIFICIAL_VENUE = STRUCTURES.register("sacrificial_venue", () -> new SacrificialVenueStructure(new Structure.StructureSettings(
            biomes(PVZBiomeTags.HAS_SACRIFICIAL_VENUE), Map.of(), GenerationStep.Decoration.UNDERGROUND_STRUCTURES, TerrainAdjustment.NONE)));
    public static final RegistryObject<StructureSet> SACRIFICIAL_VENUE_SET = STRUCTURE_SETS.register("sacrificial_venue", () ->
            new StructureSet(SACRIFICIAL_VENUE.getHolder().get(), new RandomSpreadStructurePlacement(24, 2, RandomSpreadType.LINEAR, 103563853)));

    //ender_zomboss_platform
    public static final RegistryObject<StructureTemplatePool> ENDER_ZOMBOSS_PLATFORM_POOL = TEMPLATE_POOLS.register("ender_zomboss_platform", () -> new StructureTemplatePool(Util.prefix("ender_zomboss_platform"),
            new ResourceLocation("empty"), List.of(
            Pair.of(SinglePoolElement.single("pvz:ender_zomboss_platform").apply(Projection.RIGID), 1)
    )));
    public static final RegistryObject<Structure> ENDER_ZOMBOSS_PLATFORM = STRUCTURES.register("ender_zomboss_platform", () -> new JigsawStructure(new Structure.StructureSettings(
            biomes(BiomeTags.HAS_END_CITY), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.BEARD_THIN
    ), ENDER_ZOMBOSS_PLATFORM_POOL.getHolder().get(),7, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
    public static final RegistryObject<StructureSet> ENDER_ZOMBOSS_PLATFORM_SET = STRUCTURE_SETS.register("ender_zomboss_platform", () ->
            new StructureSet(ENDER_ZOMBOSS_PLATFORM.getHolder().get(), new RandomSpreadStructurePlacement(24, 23, RandomSpreadType.LINEAR, 145189832)));

    //invasion_ruin
    public static final RegistryObject<StructureTemplatePool> INVASION_RUIN_POOL = TEMPLATE_POOLS.register("invasion_ruin", () -> new StructureTemplatePool(Util.prefix("invasion_ruin"),
            new ResourceLocation("empty"), List.of(
            Pair.of(SinglePoolElement.single("pvz:invasion_ruin_chamber").apply(Projection.RIGID), 1),
            Pair.of(SinglePoolElement.single("pvz:invasion_ruin_corner").apply(Projection.RIGID), 1),
            Pair.of(SinglePoolElement.single("pvz:invasion_ruin_hallway").apply(Projection.RIGID), 1),
            Pair.of(SinglePoolElement.single("pvz:invasion_ruin_tower").apply(Projection.RIGID), 1)
    )));
    public static final RegistryObject<Structure> INVASION_RUIN = STRUCTURES.register("invasion_ruin", () -> new JigsawStructure(new Structure.StructureSettings(
            biomes(PVZBiomeTags.HAS_INVASION_RUIN), Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, TerrainAdjustment.BEARD_THIN
    ), INVASION_RUIN_POOL.getHolder().get(),7, ConstantHeight.of(VerticalAnchor.absolute(0)), true, Heightmap.Types.WORLD_SURFACE_WG));
    public static final RegistryObject<StructureSet> INVASION_RUIN_SET = STRUCTURE_SETS.register("invasion_ruin", () ->
            new StructureSet(INVASION_RUIN.getHolder().get(), new RandomSpreadStructurePlacement(20, 18, RandomSpreadType.LINEAR, 125796538)));


    private static HolderSet<Biome> biomes(TagKey<Biome> tagKey) {
        return BuiltinRegistries.BIOME.getOrCreateTag(tagKey);
    }

    public static void register(IEventBus modBus) {
        STRUCTURE_SETS.register(modBus);
        STRUCTURES.register(modBus);
        STRUCTURE_TYPES.register(modBus);
        STRUCTURE_PIECE_TYPES.register(modBus);
        TEMPLATE_POOLS.register(modBus);
    }
}
