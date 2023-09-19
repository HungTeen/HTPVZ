package com.hungteen.pvz.common.enchantment;

import com.hungteen.pvz.common.register.PVZEnchantments;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

public class FoolishCurseEnchantment extends Enchantment {
   public FoolishCurseEnchantment() {
      super(Enchantment.Rarity.VERY_RARE, PVZEnchantments.SUMMON_CARD, EquipmentSlot.values());
   }

   public int getMinCost(int p_44616_) {
      return 25;
   }

   public int getMaxCost(int p_44619_) {
      return 50;
   }

   public int getMaxLevel() {
      return 1;
   }

   public boolean isTreasureOnly() {
      return true;
   }

   public boolean isCurse() {
      return true;
   }
}