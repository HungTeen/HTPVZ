package com.hungteen.pvz.common.menu;

import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.NotNull;

public class PennyMerchantContainer implements Container {
    private final Merchant merchant;
    public int currentPage = 0;
    private final NonNullList<ItemStack> itemStacks = NonNullList.withSize(8, ItemStack.EMPTY);
    public PennyMerchantContainer(Merchant merchant) {
        this.merchant = merchant;
    }

    @Override
    public int getContainerSize() {
        return this.itemStacks.size();
    }

    @Override
    public boolean isEmpty() {
        for(ItemStack itemstack : this.itemStacks) {
            if (!itemstack.isEmpty()) {
                return false;
            }
        }
        return true;
    }

    @Override
    public @NotNull ItemStack getItem(int p_40008_) {
        return this.itemStacks.get(p_40008_);
    }

    @Override
    public @NotNull ItemStack removeItem(int id, int count) {
        ItemStack itemStack = ContainerHelper.removeItem(this.itemStacks, id, this.itemStacks.get(id).getCount());
        return itemStack;
    }

    @Override
    public @NotNull ItemStack removeItemNoUpdate(int id) {
        return ContainerHelper.takeItem(this.itemStacks, id);
    }

    public void setItem(int id, @NotNull ItemStack itemStack) {
        this.itemStacks.set(id, itemStack);
        if (!itemStack.isEmpty() && itemStack.getCount() > this.getMaxStackSize()) {
            itemStack.setCount(this.getMaxStackSize());
        }
    }

    @Override
    public void setChanged() {
        this.updateSellItem();
    }

    public void updateSellItem() {
        MerchantOffers offers = this.merchant.getOffers();
        currentPage = Math.min(currentPage, (int) Math.floor((double) offers.size() / 8));
        for (int i = 0; i < 8; i ++) {
            if (currentPage * 8 + i >= offers.size()) {
                this.setItem(i, ItemStack.EMPTY);
            } else {
                MerchantOffer offer = offers.get(currentPage * 8 + i);
                this.setItem(i, offer.getResult().copy());
            }
        }
    }

    @Override
    public boolean stillValid(@NotNull Player player) {
        return this.merchant.getTradingPlayer() == player;
    }

    @Override
    public void clearContent() {
        this.itemStacks.clear();
    }

    public MerchantOffer getOffer(int index) {
        MerchantOffers offers = this.merchant.getOffers();
        currentPage = Math.min(currentPage, (int) Math.floor((double) offers.size() / 8));
        return offers.get(currentPage * 8 + index);
    }
}
