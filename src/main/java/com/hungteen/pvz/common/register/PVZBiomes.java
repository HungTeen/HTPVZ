package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.world.OverWorldFeatures;
import com.hungteen.pvz.common.world.zen_garden.NutTreeGrower;
import com.hungteen.pvz.common.world.zen_garden.ZenGardenBiomeSource;
import net.minecraft.sounds.Music;
import net.minecraft.world.level.biome.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class PVZBiomes {

    public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(ForgeRegistries.BIOMES, PVZMod.MODID);

    public static final RegistryObject<Biome> GARDEN_PLAINS = BIOMES.register("garden_plains", ZenGardenBiomeSource::gardenPlains);
    public static final RegistryObject<Biome> GARDEN_ISLAND = BIOMES.register("garden_island", ZenGardenBiomeSource::gardenIsland);
    public static final RegistryObject<Biome> GARDEN_RIVER = BIOMES.register("garden_river", ZenGardenBiomeSource::gardenRiver);
    public static final RegistryObject<Biome> GARDEN_MUSHROOM = BIOMES.register("garden_mushroom", ZenGardenBiomeSource::gardenMushroom);
    public static boolean features = false;



    //definitions

    public static Biome biome(Biome.Precipitation precipitation, int skyColor, int fogColor, float downfall, MobSpawnSettings.Builder mobSpawnBuilder, BiomeGenerationSettings.Builder biomeGenBuilder, @Nullable Music music) {
        return biome(precipitation, skyColor, fogColor, 0x3f76e4, 0x050533, downfall, mobSpawnBuilder, biomeGenBuilder, null, music);
    }

    public static Biome biome(Biome.Precipitation precipitation, int skyColor, int fogColor, int waterColor, int waterFogColor, float downfall, MobSpawnSettings.Builder mobSpawnBuilder, BiomeGenerationSettings.Builder biomeGenBuilder, @Nullable AmbientParticleSettings particleSettings, @Nullable Music backgroundMusic) {
        return (new Biome.BiomeBuilder()).precipitation(precipitation).temperature(skyColor).downfall(downfall)
                .specialEffects(
                        (new BiomeSpecialEffects.Builder())
                        .waterColor(waterColor).waterFogColor(waterFogColor)
                        .fogColor(fogColor).skyColor(skyColor)
                        .ambientParticle(particleSettings)
                        .backgroundMusic(backgroundMusic).build()
                )
                .mobSpawnSettings(mobSpawnBuilder.build())
                .generationSettings(biomeGenBuilder.build())
                .build();
    }

    public static void checkFeatures(){
        if (!features){
            NutTreeGrower.init();
            OverWorldFeatures.init();
        }
    }
}
