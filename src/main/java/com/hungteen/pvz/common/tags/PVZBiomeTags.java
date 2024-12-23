package com.hungteen.pvz.common.tags;

import com.hungteen.pvz.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.tags.TagKey;
import net.minecraft.world.level.biome.Biome;

public class PVZBiomeTags {

    public static TagKey<Biome> HAS_GREEN_HOUSE = pvzTag("has_structure/green_house");
    public static TagKey<Biome> HAS_GARDEN_SHELVES = pvzTag("has_structure/garden_shelves");
    public static TagKey<Biome> HAS_GARDEN_PORTAL = pvzTag("has_structure/garden_portal");
    public static TagKey<Biome> UNABLE_SUN_PRODUCTION = pvzTag("unable_sun_production");
    public static TagKey<Biome> UNABLE_SUN_FALLING = pvzTag("unable_sun_falling");
    public static TagKey<Biome> UNABLE_STAR_FALLING = pvzTag("unable_star_falling");
    public static TagKey<Biome> UNABLE_MOOBLOOM_SPAWNING = pvzTag("unable_moobloom_spawning");
    public static TagKey<Biome> EXTRA_MOOBLOOM_SPAWNING = pvzTag("extra_moobloom_spawning");


    //definition
    public static TagKey<Biome> pvzTag(String name) {
        return TagKey.create(Registry.BIOME_REGISTRY, Util.prefix(name));
    }
}