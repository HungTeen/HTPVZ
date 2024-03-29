package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;


public class PVZBiomeModifier implements BiomeModifier {

    private static final RegistryObject<Codec<? extends BiomeModifier>> SERIALIZER =
            RegistryObject.create(new ResourceLocation(PVZMod.MODID, "mob_spawns"),
                    ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, PVZMod.MODID);
    public static final DeferredRegister<Codec<? extends BiomeModifier>> BIOME_MODIFIER = DeferredRegister.create(ForgeRegistries.Keys.BIOME_MODIFIER_SERIALIZERS, PVZMod.MODID);

    static {
        BIOME_MODIFIER.register("mob_spawns", () -> Codec.unit(PVZBiomeModifier::new));
    }

    @Override
    public void modify(Holder<Biome> biome, Phase phase, ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        if (phase == Phase.ADD){
            addSpawn(builder);
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return SERIALIZER.get();
    }

    public static void addSpawn(ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        builder.getMobSpawnSettings().getSpawner(MobCategory.CREATURE)
                .add(new MobSpawnSettings.SpawnerData(PVZEntities.MOOBLOOM.get(), 50, 1, 2));
        builder.getMobSpawnSettings().getSpawner(MobCategory.WATER_AMBIENT)
                .add(new MobSpawnSettings.SpawnerData(PVZEntities.GRASSCARP.get(), 20, 1,1));
    }
}
