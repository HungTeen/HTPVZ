package com.hungteen.pvz.generator;

import com.hungteen.pvz.generator.loot.BlockLootGen;
import com.hungteen.pvz.generator.loot.EntityLootGen;
import com.hungteen.pvz.generator.loot.InvasionLootGen;
import com.mojang.datafixers.util.Pair;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.LootTables;
import net.minecraft.world.level.storage.loot.ValidationContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSet;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;

import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class LootGen extends LootTableProvider {
    public LootGen(DataGenerator p_124437_) {
        super(p_124437_);
    }

    @Override
    public List<Pair<Supplier<Consumer<BiConsumer<ResourceLocation, LootTable.Builder>>>, LootContextParamSet>> getTables() {
        return List.of(
                Pair.of(BlockLootGen::new , LootContextParamSets.BLOCK),
                Pair.of(EntityLootGen::new , LootContextParamSets.ENTITY),
                Pair.of(InvasionLootGen::new , LootContextParamSets.CHEST)
        );
    }
    @Override
    protected void validate(Map<ResourceLocation, LootTable> map, ValidationContext validationtracker) {
        map.forEach((id, builder) -> LootTables.validate(validationtracker, id, builder));
    }
}
