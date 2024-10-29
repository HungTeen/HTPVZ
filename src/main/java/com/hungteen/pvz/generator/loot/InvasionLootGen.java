package com.hungteen.pvz.generator.loot;

import com.hungteen.pvz.generator.InvasionTypeGen;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.storage.loot.LootTable;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public class InvasionLootGen implements Consumer<BiConsumer<ResourceLocation, LootTable.Builder>> {
    @Override
    public void accept(BiConsumer<ResourceLocation, LootTable.Builder> build) {
        InvasionTypeGen.loots.forEach(build);
    }
}
