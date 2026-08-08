package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.tags.PVZBiomeTags;
import com.mojang.serialization.Codec;
import net.minecraft.core.Holder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.BiomeTags;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.level.biome.MobSpawnSettings;
import net.minecraftforge.common.world.BiomeModifier;
import net.minecraftforge.common.world.ModifiableBiomeInfo;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import static net.minecraft.world.level.biome.Biomes.LUSH_CAVES;
import static net.minecraft.world.level.biome.Biomes.SUNFLOWER_PLAINS;


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
            if (biome.is(LUSH_CAVES)) addGrassCarpSpawn(builder);
            if (! biome.is(PVZBiomeTags.UNABLE_MOOBLOOM_SPAWNING)) {
                if (biome.is(PVZBiomes.GARDEN_PLAINS.getKey())) {
                    addMooBloomSpawn(builder, 5);
                } else if (biome.is(SUNFLOWER_PLAINS)) {
                    addMooBloomSpawn(builder, 50);
                } else if (builder.getMobSpawnSettings().getEntityTypes().contains(EntityType.COW))
                    addMooBloomSpawn(builder, biome.is(PVZBiomeTags.EXTRA_MOOBLOOM_SPAWNING) ? 15 : 5);
            }
            if (biome.is(BiomeTags.IS_OVERWORLD) && ! biome.is(PVZBiomeTags.UNABLE_INVASION)) {
                builder.getMobSpawnSettings().getSpawner(MobCategory.MONSTER)
                        .add(new MobSpawnSettings.SpawnerData(PVZEntities.ZOMBIE.get(), 20, 1,1));
                builder.getMobSpawnSettings().getSpawner(MobCategory.MONSTER)
                        .add(new MobSpawnSettings.SpawnerData(PVZEntities.POLE_VAULTING_ZOMBIE.get(), 10, 1,1));
                builder.getMobSpawnSettings().getSpawner(MobCategory.MONSTER)
                        .add(new MobSpawnSettings.SpawnerData(PVZEntities.SNORKEL_ZOMBIE.get(), 10, 1,1));
            }
            if (biome == Biomes.WARPED_FOREST) {
                builder.getMobSpawnSettings().getSpawner(MobCategory.MONSTER)
                        .add(new MobSpawnSettings.SpawnerData(PVZEntities.JACK_IN_A_BOX_ZOMBIE.get(), 2, 1,1));
            }
            if (biome == Biomes.CRIMSON_FOREST) {
                builder.getMobSpawnSettings().getSpawner(MobCategory.MONSTER)
                        .add(new MobSpawnSettings.SpawnerData(PVZEntities.FIRE_IMP.get(), 1, 1,1));
            }
            if (biome == Biomes.BASALT_DELTAS) {
                builder.getMobSpawnSettings().getSpawner(MobCategory.MONSTER)
                        .add(new MobSpawnSettings.SpawnerData(PVZEntities.FIRE_IMP.get(), 1, 1,1));
            }
            if (biome == Biomes.NETHER_WASTES) {
                builder.getMobSpawnSettings().getSpawner(MobCategory.MONSTER)
                        .add(new MobSpawnSettings.SpawnerData(PVZEntities.FIRE_IMP.get(), 1, 1,1));
                builder.getMobSpawnSettings().getSpawner(MobCategory.MONSTER)
                        .add(new MobSpawnSettings.SpawnerData(PVZEntities.IMP.get(), 2, 1,1));
            }
        }
    }

    @Override
    public Codec<? extends BiomeModifier> codec() {
        return SERIALIZER.get();
    }

    public static void addMooBloomSpawn(ModifiableBiomeInfo.BiomeInfo.Builder builder, int weight) {
        builder.getMobSpawnSettings().getSpawner(MobCategory.CREATURE)
                .add(new MobSpawnSettings.SpawnerData(PVZEntities.MOOBLOOM.get(), weight, 1, 2));
    }
    public static void addGrassCarpSpawn(ModifiableBiomeInfo.BiomeInfo.Builder builder) {
        builder.getMobSpawnSettings().getSpawner(MobCategory.AXOLOTLS)
                .add(new MobSpawnSettings.SpawnerData(PVZEntities.GRASSCARP.get(), 30, 1,1));
    }
}
