package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.structure.GreenHouseStructure;
import com.hungteen.pvz.common.tags.PVZBiomeTags;
import com.hungteen.pvz.util.Util;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderSet;
import net.minecraft.core.Registry;
import net.minecraft.data.BuiltinRegistries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.GenerationStep;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureSet;
import net.minecraft.world.level.levelgen.structure.StructureSpawnOverride;
import net.minecraft.world.level.levelgen.structure.TerrainAdjustment;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadStructurePlacement;
import net.minecraft.world.level.levelgen.structure.placement.RandomSpreadType;
import net.minecraft.world.level.levelgen.structure.structures.JungleTempleStructure;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;

public class PVZStructures {
//    public static Map<ResourceKey<StructureSet>, Holder<StructureSet>> structureSets = new HashMap<>();
//    public static Map<ResourceKey<Structure>, Holder<Structure>> structures = new HashMap<>();
    public static final DeferredRegister<Structure> STRUCTURES = DeferredRegister.create(BuiltinRegistries.STRUCTURES.key(), PVZMod.MODID);
    public static final DeferredRegister<StructureSet> STRUCTURE_SETS = DeferredRegister.create(BuiltinRegistries.STRUCTURE_SETS.key(), PVZMod.MODID);
    public static RegistryObject<Structure> GREEN_HOUSE = STRUCTURES.register("green_house", () -> new JungleTempleStructure(stSetting(PVZBiomeTags.HAS_GREEN_HOUSE, TerrainAdjustment.NONE)));
    public static RegistryObject<StructureSet> GREEN_HOUSES = STRUCTURE_SETS.register("green_house", () -> new StructureSet(GREEN_HOUSE.getHolder().get(),
            new RandomSpreadStructurePlacement(32, 8, RandomSpreadType.LINEAR, 23358558)));


//    private static ResourceKey<StructureSet> set(String name, StructureSet structureSet) {
//        ResourceKey<StructureSet> key = createSetKey(name);
//        structureSets.put(key, BuiltinRegistries.register(BuiltinRegistries.STRUCTURE_SETS, key, structureSet));
//        return key;
//    }
//    private static ResourceKey<StructureSet> set(String name, Holder<Structure> structure, StructurePlacement placement) {
//        return set(name, new StructureSet(structure, placement));
//    }
//    private static ResourceKey<Structure> structure(String name, Structure structure) {
//        ResourceKey<Structure> key = createKey(name);
//        structures.put(key, BuiltinRegistries.register(BuiltinRegistries.STRUCTURES, key, structure));
//        return key;
//    }

//    private static Holder<Structure> stHolder(ResourceKey<Structure> key) {
//        return structures.get(key);
//    }

    private static Structure.StructureSettings stSetting(TagKey<Biome> p_236546_, Map<MobCategory, StructureSpawnOverride> p_236547_, GenerationStep.Decoration p_236548_, TerrainAdjustment p_236549_) {
        return new Structure.StructureSettings(biomes(p_236546_), p_236547_, p_236548_, p_236549_);
    }
    private static Structure.StructureSettings stSetting(TagKey<Biome> p_236539_, GenerationStep.Decoration p_236540_, TerrainAdjustment p_236541_) {
        return stSetting(p_236539_, Map.of(), p_236540_, p_236541_);
    }
    private static Structure.StructureSettings stSetting(TagKey<Biome> p_236543_, TerrainAdjustment p_236544_) {
        return stSetting(p_236543_, Map.of(), GenerationStep.Decoration.SURFACE_STRUCTURES, p_236544_);
    }
    private static HolderSet<Biome> biomes(TagKey<Biome> tagKey) {
        Set<ResourceLocation> biomes = ForgeRegistries.BIOMES.getKeys();
        List<Holder<Biome>> list = new ArrayList<>();
        for (ResourceLocation location : biomes) {
            Optional<Holder<Biome>> optional = ForgeRegistries.BIOMES.getHolder(location);
            if (optional.isPresent() && optional.get().is(tagKey)) {
                list.add(optional.get());
            }
        }
        return HolderSet.direct(list);
    }

    private static ResourceKey<Structure> createKey(String p_209839_) {
        return ResourceKey.create(Registry.STRUCTURE_REGISTRY, Util.prefix(p_209839_));
    }
    private static ResourceKey<StructureSet> createSetKey(String p_209839_) {
        return ResourceKey.create(Registry.STRUCTURE_SET_REGISTRY, Util.prefix(p_209839_));
    }
}
