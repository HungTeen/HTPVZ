package com.hungteen.pvz.common.tags;

import com.hungteen.pvz.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class PVZBiomeTags {

    public static TagKey<Biome> HAS_GREEN_HOUSE = pvzTag("has_structure/green_house");
    public static TagKey<Biome> HAS_GARDEN_SHELVES = pvzTag("has_structure/garden_shelves");

    //definition

    public static TagKey<Biome> pvzTag(String name) {
        return TagKey.create(Registry.BIOME_REGISTRY, Util.prefix(name));
    }
}