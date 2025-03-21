package com.hungteen.pvz.generator.loot;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.data.loot.EntityLoot;
import net.minecraft.world.entity.EntityType;
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
        for (RegistryObject<EntityType<?>> obj: PVZEntities.noLootList) {
            this.add(obj.get(), LootTable.lootTable());
        }

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
        this.add(PVZEntities.JACK_IN_A_BOX_ZOMBIE.get(), basicZombieLootTable());
        this.add(PVZEntities.DIGGER_ZOMBIE.get(), basicZombieLootTable());
        this.add(PVZEntities.BUNGEE_ZOMBIE.get(), basicZombieLootTable());
        this.add(PVZEntities.IMP.get(), basicZombieLootTable());
        this.add(PVZEntities.TACO_IMP.get(), LootTable.lootTable()
                .withPool(LootPool.lootPool().setRolls(ConstantValue.exactly(1F))
                        .add(LootItem.lootTableItem(PVZItems.GOLDEN_TACO.get())))
        );
        this.add(PVZEntities.GARGANTUAR.get(), basicZombieLootTable());
        //enter here
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
                        .when(LootItemRandomChanceWithLootingCondition.randomChanceAndLootingBoost(0.01F, 0.01F)));
    }

    protected Iterable<EntityType<?>> getKnownEntities() {
        List<EntityType<?>> list = new ArrayList();
        PVZEntities.ENTITIES.getEntries().stream().toList().forEach((obj) -> list.add(obj.get()));
        return list;
    }

    private void outPut(EntityType<?> entityType){
        PVZMod.LOGGER.info("Gen Entity Loot Table: " + entityType);
    }
}
