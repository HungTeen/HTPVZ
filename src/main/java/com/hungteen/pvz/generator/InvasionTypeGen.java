package com.hungteen.pvz.generator;

import com.google.gson.JsonElement;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.common.event.RegisterSproutsEvent;
import com.hungteen.pvz.common.item.SeedItem;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.world.invasion.InvasionCondition;
import com.hungteen.pvz.common.world.invasion.InvasionEntityModifiers;
import com.hungteen.pvz.common.world.invasion.InvasionType;
import com.hungteen.pvz.util.Util;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class InvasionTypeGen implements DataProvider {
    private final DataGenerator.PathProvider pathProvider;
    public static Map<ResourceLocation, LootTable.Builder> loots = new HashMap<>();
    private static final int ZOMBIE = 100;
    private static final int IMP = 200;
    private static final int POLE = 200;
    private static final int CONE = 300;
    private static final int DOOR = 500;
    private static final int BUCKET = 700;
    private static final int GARG = 1000;
    private static final int SLIME = 100;
    public InvasionTypeGen(DataGenerator generator) {
        this.pathProvider = generator.createPathProvider(DataGenerator.Target.DATA_PACK, "invasion_types");
    }
    @Override
    public void run(@NotNull CachedOutput output) {
        Map<ResourceLocation, InvasionType> map = this.getTypes();
        map.forEach((location, type) -> {
            Path path = this.pathProvider.json(location);
            Optional<JsonElement> jsonOptional = JsonOps.INSTANCE.withEncoder(InvasionType.CODEC).apply(type).result();
            if (jsonOptional.isEmpty()) {
                PVZMod.LOGGER.error("Couldn't serialize invasion type {}", path);
            } else {
                try {
                    DataProvider.saveStable(output, jsonOptional.get(), path);
                } catch (IOException ioexception) {
                    PVZMod.LOGGER.error("Couldn't save invasion type {}", path, ioexception);
                }
            }
        });
    }

    public Map<ResourceLocation, InvasionType> getTypes() {
        Map<ResourceLocation, InvasionType> map = new HashMap<>();
        map.put(Util.prefix("babylize"), new InvasionType(loot(),
                conditions(
                ),
                entityModifiers(InvasionEntityModifiers.BABYLIZE),
                Optional.empty(), List.of(), true, 1.5F, 1,50
        ));
        map.put(Util.prefix("overworld_underground"), new InvasionType(loot(),
                conditions(
                        put(new InvasionCondition.IsUndergroundCondition()),
                        put(new InvasionCondition.InDimensionCondition(), "minecraft:overworld")
                ),
                entityModifiers(), Optional.empty(), List.of(
                new InvasionType.EnemyType(
                        EntityBuilder.of(PVZEntities.DIGGER_ZOMBIE.get()).get(),
                        List.of(), DOOR, 10, false, 0.4F
                ),
                new InvasionType.EnemyType(
                        EntityBuilder.of(PVZEntities.BUNGEE_ZOMBIE.get()).get(),
                        List.of(), DOOR, 2, false, 0.8F
                )
        ), true, 1F, 1,10000
        ));
        map.put(Util.prefix("overworld_less_weight_thick_zombie"), new InvasionType(
                loot("pvz:invasion/overworld_common",
                        LootTable.lootTable().withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(4.0F, 8.0F))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.PEA_SHOOTER.get())).setWeight(15))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.WALL_NUT.get())).setWeight(15))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(3.0F, 6.0F))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.CABBAGE_PULT.get())).setWeight(15))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.VELOCI_RADISH.get())).setWeight(15))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(0.0F, 3.0F))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.SPLIT_PEA.get())).setWeight(15))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(0.0F, 2.0F))
                                        .add(LootItem.lootTableItem(PVZItems.POP_SMARTS.get()).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 10.0F))))
                                        .add(LootItem.lootTableItem(Items.POTATO).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                                        .add(LootItem.lootTableItem(Items.CARROT).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                                        .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1))
                                        .add(LootItem.lootTableItem(PVZItems.SPROUT.get()).setWeight(15)
                                                .apply(RegisterSproutsEvent.SetSproutTypeFunction.Builder.of("sprout.pvz.common")))
                                        .add(LootItem.lootTableItem(PVZItems.SPROUT.get()).setWeight(5)
                                                .apply(RegisterSproutsEvent.SetSproutTypeFunction.Builder.of("sprout.pvz.water")))
                        )
                ),
                conditions(
                        put(new InvasionCondition.InDimensionCondition(), "minecraft:overworld")
                ),
                entityModifiers(InvasionEntityModifiers.ADD_LIFEBUOY, InvasionEntityModifiers.FINALIZE_SPAWN, InvasionEntityModifiers.CHECK_SPAWN_RULES, InvasionEntityModifiers.WITH_TACO),
                Optional.of(
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD,PVZZombie.getOverworldBanner()).get(),
                                List.of(), ZOMBIE, 20, true, 0F
                        )
                ),
                List.of(
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).get(),
                                List.of(), ZOMBIE, 26, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(EntityType.ZOMBIE).get(),
                                List.of(), ZOMBIE, 4, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance()).get(),
                                List.of(), CONE, 8, false, 0.1F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(EntityType.ZOMBIE).equip(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance()).get(),
                                List.of(), CONE, 2, false, 0.1F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance()).get(),
                                List.of(), BUCKET, 5, false, 0.3F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.JACK_IN_A_BOX_ZOMBIE.get()).get(),
                                List.of(), CONE, 5, false, 0.5F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.MAINHAND, PVZItems.SCREEN_DOOR_SHIELD.get().getDefaultInstance()).get(),
                                List.of(), DOOR, 10, false, 0.4F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.POLE_VAULTING_ZOMBIE.get()).get(),
                                List.of(), POLE, 20, false, 0.1F
                        )
                ),
                false, 1, 1,100
        ));
        map.put(Util.prefix("overworld_swamp"), new InvasionType(
                loot("pvz:invasion/overworld_swamp",
                        LootTable.lootTable().withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(4.0F, 8.0F))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.LILY_PAD.get())).setWeight(15))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.TANGLE_KELP.get())).setWeight(15))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(3.0F, 6.0F))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.PEA_SHOOTER.get())).setWeight(10))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.LILY_PAD.get())).setWeight(15))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(0.0F, 3.0F))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.TANGLE_KELP.get())).setWeight(15))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(0.0F, 2.0F))
                                        .add(LootItem.lootTableItem(Items.BROWN_MUSHROOM).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 12.0F))))
                                        .add(LootItem.lootTableItem(Items.RED_MUSHROOM).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(3.0F, 12.0F))))
                                        .add(LootItem.lootTableItem(Items.CLAY_BALL).setWeight(20).apply(SetItemCountFunction.setCount(UniformGenerator.between(6.0F, 16.0F))))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1))
                                        .add(LootItem.lootTableItem(PVZItems.SPROUT.get()).setWeight(15)
                                                .apply(RegisterSproutsEvent.SetSproutTypeFunction.Builder.of("sprout.pvz.water")))
                        )
                ),
                conditions(
                        put(new InvasionCondition.InDimensionCondition(), "minecraft:overworld"),
                        put(new InvasionCondition.InBiomeCondition(), "minecraft:swamp", "minecraft:mangrove_swamp")
                ),
                entityModifiers(InvasionEntityModifiers.ADD_LIFEBUOY, InvasionEntityModifiers.FINALIZE_SPAWN, InvasionEntityModifiers.CHECK_SPAWN_RULES, InvasionEntityModifiers.WITH_TACO),
                Optional.of(
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD,PVZZombie.getOverworldBanner()).get(),
                                List.of(), ZOMBIE, 20, true, 0F
                        )
                ),
                List.of(
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).get(),
                                List.of(), ZOMBIE, 26, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(EntityType.ZOMBIE).get(),
                                List.of(), ZOMBIE, 4, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance()).get(),
                                List.of(), CONE, 8, false, 0.1F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(EntityType.ZOMBIE).equip(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance()).get(),
                                List.of(), CONE, 2, false, 0.1F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance()).get(),
                                List.of(), BUCKET, 5, false, 0.3F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.JACK_IN_A_BOX_ZOMBIE.get()).get(),
                                List.of(), CONE, 5, false, 0.5F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.SNORKEL_ZOMBIE.get()).get(),
                                List.of(), CONE, 15, false, 0.3F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(EntityType.SLIME)
                                        .passenger(EntityBuilder.of(PVZEntities.ZOMBIE.get()))
                                        .modify(entity -> entity.putInt("Size", 2)).get(),
                                List.of(), ZOMBIE + SLIME, 10, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(EntityType.SLIME)
                                        .passenger(EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance()))
                                        .modify(entity -> entity.putInt("Size", 3)).get(),
                                List.of(), BUCKET + SLIME, 5, true, 0.3F
                        )
                ),
                false, 1, 1,500
        ));
        map.put(Util.prefix("overworld_desert"), new InvasionType(
                loot("pvz:invasion/overworld_desert",
                        LootTable.lootTable().withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(4.0F, 8.0F))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.POTATO_MINE.get())).setWeight(15))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.WALL_NUT.get())).setWeight(15))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(3.0F, 6.0F))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.POTATO_MINE.get())).setWeight(10))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.FLOWER_POT.get())).setWeight(15))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(0.0F, 3.0F))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.CABBAGE_PULT.get())).setWeight(15))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(0.0F, 2.0F))
                                        .add(LootItem.lootTableItem(Items.DEAD_BUSH).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 6.0F))))
                                        .add(LootItem.lootTableItem(Items.ROTTEN_FLESH).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 16.0F))))
                                        .add(LootItem.lootTableItem(Items.IRON_INGOT).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(0.0F, 2.0F))
                                        .add(LootItem.lootTableItem(PVZItems.FERTILIZER.get()).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 5.0F))))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1))
                                        .add(LootItem.lootTableItem(PVZItems.SPROUT.get()).setWeight(15)
                                                .apply(RegisterSproutsEvent.SetSproutTypeFunction.Builder.of("sprout.pvz.common")))
                        )
                ),
                conditions(
                        put(new InvasionCondition.InDimensionCondition(), "minecraft:overworld"),
                        put(new InvasionCondition.InBiomeCondition(), "minecraft:badlands", "minecraft:desert", "minecraft:wooded_badlands", "minecraft:eroded_badlands")
                ),
                entityModifiers(InvasionEntityModifiers.ADD_LIFEBUOY, InvasionEntityModifiers.FINALIZE_SPAWN, InvasionEntityModifiers.CHECK_SPAWN_RULES, InvasionEntityModifiers.WITH_TACO),
                Optional.of(
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD,PVZZombie.getOverworldBanner()).get(),
                                List.of(), ZOMBIE, 20, true, 0F
                        )
                ),
                List.of(
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).get(),
                                List.of(), ZOMBIE, 20, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(EntityType.HUSK).get(),
                                List.of(), ZOMBIE, 30, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance()).get(),
                                List.of(), CONE, 10, false, 0.1F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(EntityType.HUSK).equip(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance()).get(),
                                List.of(), CONE, 10, false, 0.1F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(EntityType.HUSK).equip(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance()).get(),
                                List.of(), BUCKET, 10, false, 0.3F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(EntityType.HUSK).equip(EquipmentSlot.MAINHAND, PVZItems.SCREEN_DOOR_SHIELD.get().getDefaultInstance()).get(),
                                List.of(), DOOR, 10, false, 0.4F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.JACK_IN_A_BOX_ZOMBIE.get()).get(),
                                List.of(), CONE, 5, false, 0.5F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.POLE_VAULTING_ZOMBIE.get()).get(),
                                List.of(), POLE, 20, false, 0.1F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.IMP.get()).get(),
                                List.of(), IMP, 10, false, 0.2F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.GARGANTUAR.get()).get(),
                                List.of(), GARG, 10, true, 0.5F
                        )
                ),
                false, 1, 1,500
        ));
        map.put(Util.prefix("overworld_more_weight_thick_zombie"), new InvasionType(loot("pvz:invasion/overworld_common"),
                conditions(
                        put(new InvasionCondition.InDimensionCondition(), "minecraft:overworld")
                ),
                entityModifiers(InvasionEntityModifiers.ADD_LIFEBUOY, InvasionEntityModifiers.FINALIZE_SPAWN, InvasionEntityModifiers.CHECK_SPAWN_RULES, InvasionEntityModifiers.WITH_TACO),
                Optional.of(
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD,PVZZombie.getOverworldBanner()).get(),
                                List.of(), ZOMBIE, 20, true, 0F
                        )
                ),
                List.of(
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).get(),
                                List.of(), ZOMBIE, 6, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(EntityType.ZOMBIE).get(),
                                List.of(), ZOMBIE, 2, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance()).get(),
                                List.of(), CONE, 16, false, 0.1F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(EntityType.ZOMBIE).equip(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance()).get(),
                                List.of(), CONE, 4, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance()).get(),
                                List.of(), BUCKET, 24, false, 0.3F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(EntityType.ZOMBIE).equip(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance()).get(),
                                List.of(), BUCKET, 6, false, 0.3F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.MAINHAND, PVZItems.SCREEN_DOOR_SHIELD.get().getDefaultInstance()).get(),
                                List.of(), DOOR, 30, false, 0.4F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get())
                                        .equip(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance())
                                        .equip(EquipmentSlot.MAINHAND, PVZItems.SCREEN_DOOR_SHIELD.get().getDefaultInstance()).get(),
                                List.of(), 900, 10, false, 0.5F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.POLE_VAULTING_ZOMBIE.get()).get(),
                                List.of(), POLE, 25, false, 0.1F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.SNORKEL_ZOMBIE.get()).get(),
                                List.of(), POLE, 15, false, 0.3F
                        )
                ),
                false, 1, 1,100
        ));
        map.put(Util.prefix("overworld_with_gargantuar"), new InvasionType(loot("pvz:invasion/overworld_common"),
                conditions(
                        put(new InvasionCondition.InDimensionCondition(), "minecraft:overworld")
                ),
                entityModifiers(InvasionEntityModifiers.ADD_LIFEBUOY, InvasionEntityModifiers.FINALIZE_SPAWN, InvasionEntityModifiers.CHECK_SPAWN_RULES, InvasionEntityModifiers.WITH_TACO),
                Optional.of(
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD,PVZZombie.getOverworldBanner()).get(),
                                List.of(), ZOMBIE, 20, true, 0F
                        )
                ),
                List.of(
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).get(),
                                List.of(), ZOMBIE, 20, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance()).get(),
                                List.of(), CONE, 15, false, 0.1F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance()).get(),
                                List.of(), BUCKET, 5, false, 0.4F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.MAINHAND, PVZItems.SCREEN_DOOR_SHIELD.get().getDefaultInstance()).get(),
                                List.of(), DOOR, 5, false, 0.4F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.POLE_VAULTING_ZOMBIE.get()).get(),
                                List.of(), POLE, 15, false, 0.3F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.SNORKEL_ZOMBIE.get()).get(),
                                List.of(), POLE, 15, false, 0.3F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.IMP.get()).get(),
                                List.of(), IMP, 10, false, 0.2F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.GARGANTUAR.get()).get(),
                                List.of(), GARG, 10, true, 0.5F
                        )
                ),
                false, 1, 1F,300
        ));


        //nether
        map.put(Util.prefix("nether_soul_sand"), new InvasionType(
                loot("pvz:invasion/nether_basic",
                        LootTable.lootTable().withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(4.0F, 8.0F))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.TORCH_WOOD.get())).setWeight(15))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.UMBRELLA_LEAF.get())).setWeight(15))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(3.0F, 6.0F))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.REPEATER.get())).setWeight(10))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.SPIKE_WEED.get())).setWeight(15))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(0.0F, 2.0F))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.UMBRELLA_LEAF.get())).setWeight(15))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(0.0F, 2.0F))
                                        .add(LootItem.lootTableItem(Items.GHAST_TEAR).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                                        .add(LootItem.lootTableItem(PVZItems.POP_SMARTS.get()).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(2.0F, 10.0F))))
                                        .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F))))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1))
                                        .add(LootItem.lootTableItem(PVZItems.SPROUT.get()).setWeight(15)
                                                .apply(RegisterSproutsEvent.SetSproutTypeFunction.Builder.of("sprout.pvz.nether_aggressive")))
                        )
                ),
                conditions(
                        put(new InvasionCondition.InDimensionCondition(), "minecraft:the_nether"),
                        put(new InvasionCondition.InBiomeCondition(), "minecraft:soul_sand_valley")
                ),
                entityModifiers(InvasionEntityModifiers.FINALIZE_SPAWN, InvasionEntityModifiers.CHECK_SPAWN_RULES, InvasionEntityModifiers.WITH_TACO, InvasionEntityModifiers.WITH_FOG),
                Optional.of(
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZZombie.getNetherBanner()).get(),
                                List.of(), ZOMBIE, 20, true, 0F
                        )
                ),
                List.of(
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).get(),
                                List.of(), ZOMBIE, 20, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.IMP.get()).get(),
                                List.of(), ZOMBIE, 10, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance()).get(),
                                List.of(), CONE, 20, false, 0.2F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance()).get(),
                                List.of(), BUCKET, 5, false, 0.5F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.FIRE_IMP.get()).get(),
                                List.of(), BUCKET, 10, false, 0.3F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.BUNGEE_ZOMBIE.get()).get(),
                                List.of(), CONE, 10, false, 0.3F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.LAVA_DIVER_ZOMBIE.get()).get(),
                                List.of(), CONE, 15, false, 0.2F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(EntityType.SKELETON).equip(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance()).get(),
                                List.of(), 400, 5, false, 0.4F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(EntityType.SKELETON).equip(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance()).get(),
                                List.of(), BUCKET, 5, false, 0.8F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.LAVA_GHASTLING.get()).passenger(EntityBuilder.of(PVZEntities.FIRE_IMP.get())).get(),
                                List.of(), BUCKET, 15, true, 0.4F
                        )
                ),
                false, 1, 1.2F,500
        ));
        map.put(Util.prefix("nether_magma"), new InvasionType(
                loot("pvz:invasion/nether_magma",
                        LootTable.lootTable().withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(4.0F, 8.0F))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.TALL_NUT.get())).setWeight(15))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.PLANTERN.get())).setWeight(15))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(3.0F, 6.0F))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.PLANTERN.get())).setWeight(10))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.TORCH_WOOD.get())).setWeight(15))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(0.0F, 2.0F))
                                        .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.UMBRELLA_LEAF.get())).setWeight(15))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(0.0F, 2.0F))
                                        .add(LootItem.lootTableItem(Items.MAGMA_CREAM).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 10.0F))))
                                        .add(LootItem.lootTableItem(Items.GOLD_INGOT).setWeight(5).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 5.0F))))
                                        .add(LootItem.lootTableItem(PVZItems.FLAME_PEA.get()).setWeight(10).apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 16.0F))))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(UniformGenerator.between(0.0F, 2.0F))
                                        .add(LootItem.lootTableItem(PVZItems.FERTILIZER.get()).setWeight(15).apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F))))
                        ).withPool(
                                LootPool.lootPool()
                                        .setRolls(ConstantValue.exactly(1))
                                        .add(LootItem.lootTableItem(PVZItems.SPROUT.get()).setWeight(15)
                                                .apply(RegisterSproutsEvent.SetSproutTypeFunction.Builder.of("sprout.pvz.nether_defensive")))
                        )),
                conditions(
                        put(new InvasionCondition.InDimensionCondition(), "minecraft:the_nether"),
                        put(new InvasionCondition.Not(), "$pvz:in_biome", "minecraft:basalt_deltas")
                ),
                entityModifiers(InvasionEntityModifiers.FINALIZE_SPAWN, InvasionEntityModifiers.CHECK_SPAWN_RULES, InvasionEntityModifiers.WITH_TACO, InvasionEntityModifiers.WITH_FOG),
                Optional.of(
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZZombie.getNetherBanner()).get(),
                                List.of(), ZOMBIE, 20, true, 0F
                        )
                ),
                List.of(
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).get(),
                                List.of(), ZOMBIE, 20, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.IMP.get()).get(),
                                List.of(), ZOMBIE, 10, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance()).get(),
                                List.of(), CONE, 20, false, 0.2F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance()).get(),
                                List.of(), BUCKET, 5, false, 0.5F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.FIRE_IMP.get()).get(),
                                List.of(), BUCKET, 10, false, 0.3F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.BUNGEE_ZOMBIE.get()).get(),
                                List.of(), DOOR, 5, false, 0.3F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.POLE_VAULTING_ZOMBIE.get()).get(),
                                List.of(), POLE, 15, false, 0.3F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.LAVA_DIVER_ZOMBIE.get()).get(),
                                List.of(), CONE, 15, false, 0.2F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(EntityType.MAGMA_CUBE)
                                        .passenger(EntityBuilder.of(PVZEntities.ZOMBIE.get()))
                                        .modify(entity -> entity.putInt("Size", 2)).get(),
                                List.of(), ZOMBIE + SLIME, 10, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(EntityType.MAGMA_CUBE)
                                        .passenger(EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance()))
                                        .modify(entity -> entity.putInt("Size", 3)).get(),
                                List.of(), BUCKET + SLIME, 5, true, 0.5F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.GARGANTUAR.get()).passenger(EntityBuilder.of(PVZEntities.FIRE_IMP.get())).get(),
                                List.of(), GARG + CONE, 15, true, 0.5F
                        )
                ),
                false, 1, 1.2F,300
        ));
        map.put(Util.prefix("nether_basic"), new InvasionType(
                loot("pvz:invasion/nether_basic"),
                conditions(
                        put(new InvasionCondition.InDimensionCondition(), "minecraft:the_nether")
                ),
                entityModifiers(InvasionEntityModifiers.FINALIZE_SPAWN, InvasionEntityModifiers.CHECK_SPAWN_RULES, InvasionEntityModifiers.WITH_TACO, InvasionEntityModifiers.WITH_FOG),
                Optional.of(
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD,PVZZombie.getNetherBanner()).get(),
                                List.of(), ZOMBIE, 20, true, 0F
                        )
                ),
                List.of(
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).get(),
                                List.of(), ZOMBIE, 20, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.IMP.get()).get(),
                                List.of(), ZOMBIE, 10, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance()).get(),
                                List.of(), CONE, 20, false, 0.2F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance()).get(),
                                List.of(), BUCKET, 5, false, 0.5F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.FIRE_IMP.get()).get(),
                                List.of(), BUCKET, 10, false, 0.3F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.BUNGEE_ZOMBIE.get()).get(),
                                List.of(), DOOR, 5, false, 0.3F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.POLE_VAULTING_ZOMBIE.get()).get(),
                                List.of(), POLE, 15, false, 0.3F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.LAVA_DIVER_ZOMBIE.get()).get(),
                                List.of(), CONE, 15, false, 0.2F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.GARGANTUAR.get()).passenger(EntityBuilder.of(PVZEntities.FIRE_IMP.get())).get(),
                                List.of(), GARG + CONE, 15, true, 0.5F
                        )
                ),
                false, 1, 1.2F,500
        ));
        //TODO need one on lava seas?
        return map;
    }

    @Override
    public @NotNull String getName() {
        return "PVZInvasionTypes";
    }


    //serializing tools
    @SafeVarargs
    protected final List<Pair<ResourceLocation, List<String>>> conditions(Pair<ResourceLocation, List<String>>... conditions) {
        return List.of(conditions);
    }

    protected Pair<ResourceLocation, List<String>> put(InvasionCondition condition, String... arguments) {
        return Pair.of(condition.getName(), Arrays.stream(arguments).toList());
    }

    protected String arg(InvasionCondition condition) {
        return "$" + condition.getName().toString();
    }

    protected String arg(TagKey tag) {
        return "#" + tag.location().toString();
    }

    protected String arg(RegistryObject object) {
        return object.getId().toString();
    }

    protected String arg(Item item) {
        return ForgeRegistries.ITEMS.getKey(item).toString();
    }

    protected String arg(Block block) {
        return ForgeRegistries.BLOCKS.getKey(block).toString();
    }

    protected String arg(EntityType type) {
        return ForgeRegistries.ENTITY_TYPES.getKey(type).toString();
    }

    protected List<ResourceLocation> entityModifiers() {
        return List.of();
    }
    protected List<ResourceLocation> entityModifiers(String... modifiers) {
        return Arrays.stream(modifiers).map(string -> {
            ResourceLocation location = new ResourceLocation(string);
            if (! InvasionType.invasionEntityModifiers.containsKey(location)) {
                PVZMod.LOGGER.error("added incorrect entity modifier name in {}", location);
                throw new RuntimeException();
            }
            return location;
        }).toList();
    }

    protected List<ResourceLocation> entityModifiers(ResourceLocation... modifiers) {
        return Arrays.stream(modifiers).peek(location -> {
            if (! InvasionType.invasionEntityModifiers.containsKey(location)) {
                PVZMod.LOGGER.error("added incorrect entity modifier name in {}", location);
                throw new RuntimeException();
            }
        }).toList();
    }

    protected Optional<ResourceLocation> loot() {
        return Optional.empty();
    }
    protected Optional<ResourceLocation> loot(String string) {
        return loot(new ResourceLocation(string));
    }
    protected Optional<ResourceLocation> loot(String string, LootTable.Builder lootTable) {
        return loot(new ResourceLocation(string), lootTable);
    }
    protected Optional<ResourceLocation> loot(ResourceLocation location) {
        return Optional.of(location);
    }
    protected Optional<ResourceLocation> loot(ResourceLocation location, LootTable.Builder lootTable) {
        loots.put(location, lootTable);
        return Optional.of(location);
    }

    public static class EntityBuilder<E extends Entity> extends CompoundTag {
        public static <E extends Entity> EntityBuilder<E> of(EntityType<E> type) {
            EntityBuilder<E> builder = new EntityBuilder<>();
            builder.putString("id", ForgeRegistries.ENTITY_TYPES.getKey(type).toString());
            return builder;
        }
        public static <E extends Entity> EntityBuilder<E> withTag(EntityType<E> type) {
            EntityBuilder<E> builder = new EntityBuilder<>();
            builder.putString("id", ForgeRegistries.ENTITY_TYPES.getKey(type).toString());
            return builder;
        }

        public EntityBuilder<E> equip(EquipmentSlot slot, ItemStack item) {
            String type = slot.getType() == EquipmentSlot.Type.ARMOR ? "ArmorItems" : "HandItems";
            ListTag slots = ((ListTag) this.get(type));
            List<Tag> list = new ArrayList<>(slots == null ?
                    (slot.getType() == EquipmentSlot.Type.ARMOR ?
                            Stream.of(new CompoundTag(), new CompoundTag(), new CompoundTag(), new CompoundTag()) :
                            Stream.of(new CompoundTag(), new CompoundTag())).toList() : slots.stream().toList());
            CompoundTag itemTag = new CompoundTag();
            list.set(slot.getIndex(), item.save(itemTag));
            ListTag newTag = new ListTag();
            newTag.addAll(list);
            this.put(type, newTag);
            return this;
        }
        public EntityBuilder<E> attribute(Attribute attribute, double value) {
            if (! this.contains("Attributes")) {
                this.put("Attributes", new ListTag());
            }
            CompoundTag attributeTag = new CompoundTag();
            attributeTag.putString("Name", attribute.getDescriptionId());
            attributeTag.putDouble("Base", value);
            ((ListTag) this.get("Attributes")).add(attributeTag);
            return this;
        }
        public EntityBuilder<E> passenger(EntityBuilder<? extends Entity> passenger) {
            if (! this.contains("Passengers")) {
                this.put("Passengers", new ListTag());
            }
            ((ListTag) this.get("Passengers")).add(passenger.get());
            return this;
        }
        @Deprecated
        public EntityBuilder<E> effect(MobEffectInstance effect) {
            //TODO unfinished yet.
            return this;
        }
        public EntityBuilder<E> modify(Consumer<CompoundTag> consumer) {
            consumer.accept(this);
            return this;
        }

        public CompoundTag get() {
            return this.copy();
        }
    }
}
