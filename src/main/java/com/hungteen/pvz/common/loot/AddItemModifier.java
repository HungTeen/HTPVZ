package com.hungteen.pvz.common.loot;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Random;

public class AddItemModifier extends LootModifier {
    private static Random random = new Random();
    public static final Codec<AddItemModifier> CODEC = RecordCodecBuilder.create(instance -> codecStart(instance)
            .and(Codec.list(ItemStack.CODEC).fieldOf("items").forGetter(modifier -> modifier.itemStack))
            .apply(instance, AddItemModifier::new));
    private final List<ItemStack> itemStack;
    public AddItemModifier(LootItemCondition[] conditionsIn, List<ItemStack> itemStack) {
        super(conditionsIn);
        this.itemStack = itemStack;
    }

    @Override
    protected @NotNull ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> generatedLoot, LootContext context) {
        generatedLoot.addAll(this.itemStack);
        return generatedLoot;
    }

    @Override
    public Codec<? extends IGlobalLootModifier> codec() {
        return CODEC;
    }
}
