package com.hungteen.pvz.common.menu;

import net.minecraft.stats.Stats;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;

public class PennyMerchantResultSlot extends Slot {
    private final PennyMerchantContainer slots;
    private final Player player;
    private int removeCount;
    private final Merchant merchant;

    public PennyMerchantResultSlot(Player p_40083_, Merchant p_40084_, PennyMerchantContainer p_40085_, int p_40086_, int p_40087_, int p_40088_) {
        super(p_40085_, p_40086_, p_40087_, p_40088_);
        this.player = p_40083_;
        this.merchant = p_40084_;
        this.slots = p_40085_;
    }

    @Override
    public boolean mayPlace(ItemStack p_40095_) {
        return false;
    }

    @Override
    public ItemStack remove(int p_40090_) {
        if (this.hasItem()) {
            this.removeCount += Math.min(p_40090_, this.getItem().getCount());
        }
        return super.remove(p_40090_);
    }

    @Override
    protected void onQuickCraft(ItemStack p_40097_, int p_40098_) {
        this.removeCount += p_40098_;
        this.checkTakeAchievements(p_40097_);
    }

    @Override
    protected void checkTakeAchievements(ItemStack p_40100_) {
        p_40100_.onCraftedBy(this.player.level, this.player, this.removeCount);
        this.removeCount = 0;
    }

    @Override
    public boolean mayPickup(Player player) {
        if (super.mayPickup(player)) {
            MerchantOffer offer = this.slots.getOffer(this.getSlotIndex());
            if (offer != null && ! offer.isOutOfStock()) {
                Container tmpContainer = new SimpleContainer(1);
                int aMatch = player.getInventory().clearOrCountMatchingItems(i -> i.getItem() == offer.getCostA().getItem(), 0, tmpContainer) - offer.getCostA().getCount();
                int bMatch = offer.getCostB().isEmpty() ? 1 : player.getInventory().clearOrCountMatchingItems(i -> i.getItem() == offer.getCostB().getItem(), 0, tmpContainer) - offer.getCostB().getCount();
                if (aMatch >= 0 && bMatch >= 0) {
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void onTake(Player player, ItemStack itemStack) {
        this.checkTakeAchievements(itemStack);
        MerchantOffer offer = this.slots.getOffer(this.getSlotIndex());
        Container tmpContainer = new SimpleContainer(1);
        this.merchant.notifyTrade(offer);
        player.awardStat(Stats.TRADED_WITH_VILLAGER);
        player.getInventory().clearOrCountMatchingItems(i -> i.getItem() == offer.getCostA().getItem(), offer.getCostA().getCount(), tmpContainer);
        if (! offer.getCostB().isEmpty()) {
            player.getInventory().clearOrCountMatchingItems(i -> i.getItem() == offer.getCostB().getItem(), offer.getCostB().getCount(), tmpContainer);
        }
        this.merchant.overrideXp(this.merchant.getVillagerXp() + offer.getXp());
    }
}
