package com.hungteen.pvz.common.enchantment;

import com.hungteen.pvz.common.register.PVZEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.enchantment.Enchantment;

import java.util.Calendar;

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

   public boolean isTreasureOnly() {
      return true;
   }

   public boolean isCurse() {
      return true;
   }

   @Override
   public Component getFullname(int p_44701_) {
      Calendar calendar = Calendar.getInstance();
      boolean isFoolDay = calendar.get(2) + 1 == 4 && calendar.get(5) <= 3;
      MutableComponent mutablecomponent = isFoolDay ? Component.translatable("enchantment.pvz.loofish_curse") : Component.translatable(this.getDescriptionId());
      if (this.isCurse()) {
         mutablecomponent.withStyle(ChatFormatting.RED);
      } else {
         mutablecomponent.withStyle(ChatFormatting.GRAY);
      }

      if (p_44701_ != 1 || this.getMaxLevel() != 1) {
         mutablecomponent.append(" ").append(Component.translatable("enchantment.level." + p_44701_));
      }

      return mutablecomponent;
   }
}