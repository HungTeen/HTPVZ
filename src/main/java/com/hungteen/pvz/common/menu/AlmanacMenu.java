package com.hungteen.pvz.common.menu;

import com.hungteen.pvz.common.register.PVZMenus;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

public class AlmanacMenu extends AbstractContainerMenu {
    public AlmanacMenu(@Nullable MenuType<?> p_38851_, int p_38852_) {
        super(p_38851_, p_38852_);
    }
    public AlmanacMenu(Inventory inventory, int id) {
        super(PVZMenus.ALMANAC.get(), id);
        //slots.

    }
    @Override
    public ItemStack quickMoveStack(Player p_38941_, int p_38942_) {
        return null;
    }

    @Override
    public boolean stillValid(Player p_38874_) {
        return true;
    }
}
