package com.hungteen.pvz.common.enchantment;

import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;

public class SunMendingEnchantment extends Enchantment {

    public SunMendingEnchantment() {
        super(Rarity.RARE, EnchantmentCategory.BREAKABLE, EquipmentSlot.values());
    }
    @Override
    public int getMinCost(int p_45102_) {
        return p_45102_ * 25;
    }

    @Override
    public int getMaxCost(int p_45105_) {
        return this.getMinCost(p_45105_) + 50;
    }

    @Override
    public boolean isTreasureOnly() {
        return true;
    }

    @Override
    public int getMaxLevel() {
        return 1;
    }
}
