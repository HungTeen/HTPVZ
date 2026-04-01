package com.hungteen.pvz.generator.loot;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.item.SeedItem;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.data.loot.EntityLoot;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.LootingEnchantFunction;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.functions.SmeltItemFunction;
import net.minecraft.world.level.storage.loot.predicates.LootItemEntityPropertyCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemRandomChanceWithLootingCondition;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;

public class EntityLootGen extends EntityLoot {
    @Override
    public void addTables(){
        for (RegistryObject<EntityType<?>> obj: PVZEntities.ENTITIES.getEntries()) {
            this.add(PVZEntities.MOOBLOOM.get(), LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(Items.LEATHER)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F)))
                                    .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F)))))
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(Items.BEEF)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 3.0F)))
                                    .apply(SmeltItemFunction.smelted().when(LootItemEntityPropertyCondition.hasProperties(LootContext.EntityTarget.THIS, ENTITY_ON_FIRE)))
                                    .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F)))))
            );
            this.add(PVZEntities.GRASSCARP.get(), LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(Items.KELP)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(1.0F, 2.0F)))
                                    .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F)))))
            );
            this.add(PVZEntities.ZOMBIE.get(), basicZombieLootTable());
            this.add(PVZEntities.POLE_VAULTING_ZOMBIE.get(), basicZombieLootTable());
            this.add(PVZEntities.SNORKEL_ZOMBIE.get(), basicZombieLootTable());
            this.add(PVZEntities.JACK_IN_A_BOX_ZOMBIE.get(), basicZombieLootTable());
            this.add(PVZEntities.DIGGER_ZOMBIE.get(), basicZombieLootTable());
            this.add(PVZEntities.BUNGEE_ZOMBIE.get(), basicZombieLootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                            .add(LootItem.lootTableItem(PVZItems.ARROW_WITH_A_TARGET.get())
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 2.0F)))
                                    .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))));
            this.add(PVZEntities.IMP.get(), basicZombieLootTable());
            this.add(PVZEntities.FIRE_IMP.get(), basicZombieLootTable());
            this.add(PVZEntities.TACO_IMP.get(), LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                            .add(LootItem.lootTableItem(PVZItems.GOLDEN_TACO.get())))
            );
            this.add(PVZEntities.GARGANTUAR.get(), basicZombieLootTable());
            this.add(PVZEntities.GHAST_RIDER.get(), LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                            .add(LootItem.lootTableItem(PVZItems.PEPPER.get())))
            );
            this.add(PVZEntities.ENDER_ZOMBOSS.get(), LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                            .add(LootItem.lootTableItem(PVZItems.SPATIOTEMPORAL_UNIT.get())))
            );
            this.add(PVZEntities.LAVA_DIVER_ZOMBIE.get(), basicZombieLootTable());
            this.add(PVZEntities.LAVA_GHASTLING.get(), LootTable.lootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                            .add(LootItem.lootTableItem(Items.GHAST_TEAR)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 0.2F)))
                                    .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F)))))
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(Items.GUNPOWDER)
                                    .apply(SetItemCountFunction.setCount(UniformGenerator.between(0.0F, 1.0F)))
                                    .apply(LootingEnchantFunction.lootingMultiplier(UniformGenerator.between(0.0F, 1.0F))))));
            this.add(PVZEntities.PEA_SHOOTER_ZOMBIE.get(), basicZombieLootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.PEA_SHOOTER.get())))
                            .when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.03F, 0.03F))));
            this.add(PVZEntities.SNOW_PEA_ZOMBIE.get(), basicZombieLootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.SNOW_PEA.get())))
                            .when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.03F, 0.03F))));
            this.add(PVZEntities.GATLING_PEA.get(), basicZombieLootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.GATLING_PEA.get())))
                            .when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.03F, 0.03F))));
            this.add(PVZEntities.WALL_NUT_ZOMBIE.get(), basicZombieLootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.WALL_NUT.get())))
                            .when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.03F, 0.03F))));
            this.add(PVZEntities.TALL_NUT_ZOMBIE.get(), basicZombieLootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.TALL_NUT.get())))
                            .when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.03F, 0.03F))));
            this.add(PVZEntities.PUMPKIN_ZOMBIE.get(), basicZombieLootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.PUMPKIN.get())))
                            .when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.03F, 0.03F))));
            this.add(PVZEntities.JALAPENO_ZOMBIE.get(), basicZombieLootTable()
                    .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                            .add(LootItem.lootTableItem(SeedItem.getSeed(PVZEntities.JALAPENO.get())))
                            .when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.03F, 0.03F))));
            //enter here

            if (! map.containsKey(obj.get().getDefaultLootTable())) {
                this.add(obj.get(), LootTable.lootTable());
            }
        }

    }

    protected void add(EntityType<?> entityType, LootTable.Builder builder) {
        outPut(entityType);
        this.add(entityType.getDefaultLootTable(), builder);
    }

    private LootTable.Builder basicZombieLootTable() {
        return LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(PVZItems.POP_SMARTS.get()))
                        .when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.03F, 0.03F)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(PVZItems.NUT.get()))
                        .when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.02F, 0.02F)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(Items.POTATO))
                        .when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.01F, 0.01F)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(PVZItems.CHOCOLATE.get()))
                        .when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.01F, 0.01F)))
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1.0F))
                        .add(LootItem.lootTableItem(PVZItems.JEWEL.get()))
                        .when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.005F, 0.005F)));
    }

    protected Iterable<EntityType<?>> getKnownEntities() {
        List<EntityType<?>> list = new ArrayList();
        PVZEntities.ENTITIES.getEntries().stream().toList().forEach((obj) -> {
            if (LivingEntity.class.isAssignableFrom(obj.get().getBaseClass())) {
                list.add(obj.get());
            }
        });
        return list;
    }

    private void outPut(Object content) {
        PVZMod.LOGGER.info("Gen Entity Loot Table: " + content);
    }
}
