package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.world.zen_garden.NutTreeGrower;
import com.hungteen.pvz.common.world.zen_garden.ZenGardenBiomeSource;
import net.minecraft.sounds.Music;
import net.minecraft.util.Mth;
import net.minecraft.world.level.biome.*;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import javax.annotation.Nullable;

public class PVZBiomes {

    public static final DeferredRegister<Biome> BIOMES = DeferredRegister.create(ForgeRegistries.BIOMES, PVZMod.MODID);

    public static final RegistryObject<Biome> GARDEN_PLAINS = BIOMES.register("garden_plains", ZenGardenBiomeSource::gardenPlains);
    public static boolean features = false;



    //difinitions

    public static Biome biome(Biome.Precipitation precipitation, float skyColor, float downfall, MobSpawnSettings.Builder mobSpawnBuilder, BiomeGenerationSettings.Builder biomeGenBuilder, @Nullable Music music) {
        return biome(precipitation, skyColor, downfall, 4159204, 329011, mobSpawnBuilder, biomeGenBuilder, music);
    }

    public static Biome biome(Biome.Precipitation precipitation, float skyColor, float downfall, int waterColor, int waterFogColor, MobSpawnSettings.Builder mobSpawnBuilder, BiomeGenerationSettings.Builder biomeGenBuilder, @Nullable Music backgroundMusic) {
        return (new Biome.BiomeBuilder()).precipitation(precipitation).temperature(skyColor).downfall(downfall)
                .specialEffects(
                        (new BiomeSpecialEffects.Builder())
                        .waterColor(waterColor).waterFogColor(waterFogColor)
                        .fogColor(12638463).skyColor(calculateSkyColor(skyColor))
                        .ambientMoodSound(AmbientMoodSettings.LEGACY_CAVE_SETTINGS)
                        .backgroundMusic(backgroundMusic).build()
                )
                .mobSpawnSettings(mobSpawnBuilder.build())
                .generationSettings(biomeGenBuilder.build())
                .build();
    }

    protected static int calculateSkyColor(float p_194844_) {
        float $$1 = p_194844_ / 3.0F;
        $$1 = Mth.clamp($$1, -1.0F, 1.0F);
        return Mth.hsvToRgb(0.62222224F - $$1 * 0.05F, 0.5F + $$1 * 0.1F, 1.0F);
    }

    public static void checkFeatures(){
        if (!features){
            NutTreeGrower.init();
        }
    }
}
