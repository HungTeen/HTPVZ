package com.hungteen.pvz.common.item;

import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;

import java.util.function.Supplier;

public class SeedItem<T extends Entity> extends SeedPacketItem<T>{
    public SeedItem(Properties p_41383_, Supplier<EntityType<T>> entitySupplier, String resource, int cost, int coolDown) {
        super(p_41383_, entitySupplier, resource, cost, coolDown);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        return Component.translatable("item.pvz.seed", Component.translatable(entitySupplier.get().getDescriptionId()));
    }

    @Override
    public boolean canBoost() {
        return false;
    }

    protected void used(ItemStack itemstack, Player player, InteractionHand hand) {
        player.awardStat(Stats.ITEM_USED.get(itemstack.getItem()));
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
