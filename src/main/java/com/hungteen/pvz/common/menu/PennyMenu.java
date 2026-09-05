package com.hungteen.pvz.common.menu;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.common.register.PVZMenus;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.npc.ClientSideMerchant;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffers;
import org.jetbrains.annotations.NotNull;

public class PennyMenu extends AbstractContainerMenu {
    public final @NotNull MerchantMenu vanillaMenu;
    public final @NotNull PennyMerchantContainer merchantContainer;
    public boolean isVanillaUI = false;
    final Merchant trader;

    public PennyMenu(Inventory inventory, int id) {
        this(id, inventory, new ClientSideMerchant(inventory.player));
    }

    public PennyMenu(int id, Inventory inventory, Merchant merchant) {
        super(PVZMenus.PENNY.get(), id);
        this.vanillaMenu = new MerchantMenu(id, inventory, merchant);
        this.trader = merchant;
        this.merchantContainer = new PennyMerchantContainer(trader);
        if (inventory.player.level.isClientSide && PVZConfig.Client.renderPVZTypePennyGUI.get()) {
            for (int i = 0; i < 3; ++ i) {
                SimpleContainer unshownContainer = new SimpleContainer(3);
                this.addSlot(new Slot(unshownContainer, i, -100000, -100000));
            }
        } else {
            isVanillaUI = true;
            this.addSlot(vanillaMenu.getSlot(0));
            this.addSlot(vanillaMenu.getSlot(1));
            this.addSlot(vanillaMenu.getSlot(2));
        }
        for (int i = 0; i < 3; ++ i) {
            for(int j = 0; j < 9; ++j) {
                this.addSlot(vanillaMenu.getSlot(j + i * 9 + 3));
            }
        }
        for (int k = 0; k < 9; ++ k) {
            this.addSlot(vanillaMenu.getSlot(k + 30));
        }
        if (isVanillaUI) {
            for (int i = 0; i < 8; ++ i) {
                this.addSlot(new PennyMerchantResultSlot(inventory.player, trader, merchantContainer, i, -100000, -100000));
            }
        } else {
            for (int i = 0; i < 2; i ++) {
                for (int j = 0; j < 4; j ++) {
                    this.addSlot(new PennyMerchantResultSlot(inventory.player, trader, merchantContainer,
                            j + i * 4, 111 + 36 * j + 4 * i, 17 + 26 * i));
                }
            }
        }
    }

    public boolean canTakeItemForPickAll(ItemStack itemStack, Slot slot) {
        return false;
    }

    public void slotsChanged(Container container) {
        if (isVanillaUI) {
            vanillaMenu.slotsChanged(container);
        }
        super.slotsChanged(container);
    }

    @Override
    public @NotNull ItemStack quickMoveStack(@NotNull Player player, int index) {
        Slot slot = this.slots.get(index);
        if (! isVanillaUI) {
            ItemStack itemStack = slot.getItem();
            ItemStack result = itemStack.copy();
            if (slot.hasItem()) {
                if (index >= 39) {
                    return ItemStack.EMPTY;
                }
            } else if (! this.moveItemStackTo(itemStack, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
            if (itemStack.getCount() == result.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, itemStack);
            return result;
        } else {
            return vanillaMenu.quickMoveStack(player, index);
        }
    }

    @Override
    public void setCarried(ItemStack itemStack) {
        super.setCarried(itemStack);
        this.vanillaMenu.setCarried(itemStack);
    }

    public void setOffers(MerchantOffers offers) {
        this.vanillaMenu.setOffers(offers);
    }

    public MerchantOffers getOffers() {
        return this.vanillaMenu.getOffers();
    }

    public void setMerchantLevel(int p_40070_) {
        this.vanillaMenu.setMerchantLevel(p_40070_);
    }

    public void setShowProgressBar(boolean p_40049_) {
        this.vanillaMenu.setShowProgressBar(p_40049_);
    }

    public void setCanRestock(boolean p_40049_) {
        this.vanillaMenu.setCanRestock(p_40049_);
    }

    public void setXp(int p_40067_) {
        this.trader.overrideXp(p_40067_);
    }

    public void setSelectionHint(int p_40064_) {
        this.vanillaMenu.setSelectionHint(p_40064_);
    }

    public void tryMoveItems(int p_40073_) {
        this.vanillaMenu.tryMoveItems(p_40073_);
    }

    public void playTradeSound() {
        this.vanillaMenu.playTradeSound();
    }

    @Override
    public void removed(Player player) {
        super.removed(player);
        this.trader.setTradingPlayer(null);
        if (isVanillaUI) {
            this.vanillaMenu.removed(player);
        }
    }

    @Override
    public boolean stillValid(Player player) {
        return this.trader.getTradingPlayer() == player;
    }
}
