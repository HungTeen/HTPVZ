package com.hungteen.pvz.common.enchantment;

import com.hungteen.pvz.common.register.PVZEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

public class QuickCoolDownEnchantment extends Enchantment {
    public QuickCoolDownEnchantment() {
        super(Enchantment.Rarity.UNCOMMON, PVZEnchantments.SUMMON_CARD, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
    }
    public int getMinCost(int p_45171_) {
        return 12 + (p_45171_ - 1) * 20;
    }

    public int getMaxCost(int p_45173_) {
        return 50;
    }

    public int getMaxLevel() {
        return 3;
    }
}
