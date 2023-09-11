package com.hungteen.pvz.common.enchantment;

import com.hungteen.pvz.common.register.PVZEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

public class SunShovelEnchantment extends Enchantment {
    public SunShovelEnchantment() {
        super(Rarity.UNCOMMON, PVZEnchantments.SHOVEL, new EquipmentSlot[] {EquipmentSlot.MAINHAND});
    }
    public static float returnSunPercent(int level) {
        return level == 0 ? 0 : (level == 1 ? 0.5F : (level > 2 ? 0.25F : 0.15F));
    }

    public int getMinCost(int p_44598_) {
        return 12 + (p_44598_ - 1) * 20;
    }

    public int getMaxCost(int p_44600_) {
        return this.getMinCost(p_44600_) + 25;
    }

    @Override
    public int getMaxLevel() {
        return 3;
    }


}
