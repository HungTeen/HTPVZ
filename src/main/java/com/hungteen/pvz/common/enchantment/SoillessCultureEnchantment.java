package com.hungteen.pvz.common.enchantment;

import com.hungteen.pvz.common.register.PVZEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

public class SoillessCultureEnchantment extends Enchantment {
    public SoillessCultureEnchantment() {
        super(Rarity.RARE, PVZEnchantments.SUMMON_CARD, new EquipmentSlot[]{EquipmentSlot.MAINHAND});
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
    public int getMaxLevel() {
        return 1;
    }
}
