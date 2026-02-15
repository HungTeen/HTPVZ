package com.hungteen.pvz.common.item;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.register.PVZStats;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.stats.Stats;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class SeedItem<T extends Entity> extends SeedPacketItem<T> {

    public static List<SeedPacketItem<?>> seedItem = new ArrayList<>();
    public SeedItem(Properties p_41383_, Supplier<EntityType<T>> entitySupplier, String resource, int cost, int coolDown, boolean creativeOnly, boolean extraCost) {
        this(p_41383_, entitySupplier, List.of(), resource, cost, coolDown, creativeOnly, extraCost);
    }
    public SeedItem(Properties p_41383_, Supplier<EntityType<T>> entitySupplier, List<Skill> skillList, String resource, int cost, int coolDown, boolean creativeOnly, boolean extraCost) {
        super(p_41383_, entitySupplier, skillList, resource, cost, coolDown, creativeOnly, extraCost);
        if (this.getClass() == SeedItem.class) seedItem.add(this);
    }

    //methods
    public static SeedPacketItem getSeed(EntityType<?> entityType) {
        AtomicReference<SeedPacketItem> packetItem = new AtomicReference<>();
        seedItem.forEach(item -> {
            if (item.getEntity().equals(entityType)) {
                packetItem.set(item);
            }});
        return packetItem.get();
    }


    //definitions
    @Override
    public Component getName(ItemStack itemStack) {
        Component original = Component.translatable(this.getDescriptionId(itemStack));
        if (original.getContents() instanceof TranslatableContents contents && original.getString().equals(contents.getKey())) {
            return Component.translatable("item.pvz.seed", Component.translatable(entitySupplier.get().getDescriptionId()));
        }
        return original;
    }

    @Override
    public boolean canBoost() {
        return false;
    }

    @Override
    protected void used(ItemStack itemstack, Player player) {
        player.awardStat(Stats.ITEM_USED.get(itemstack.getItem()));
        player.awardStat(PVZStats.PLANT);
        if (!player.getAbilities().instabuild) {
            itemstack.shrink(1);
        }
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return false;
    }

    @Override
    public int getEnchantmentValue(ItemStack itemStack) {
        return 0;
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemToFix, ItemStack material) {
        return false;
    }
}
