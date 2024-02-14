package com.hungteen.pvz.common.menu;

import com.hungteen.pvz.common.block.entity.EssenceFurnaceBlockEntity;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.OtherRegisters;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraftforge.items.SlotItemHandler;

public class EssenceFurnaceMenu extends RecipeBookMenu<Container>{
    private EssenceFurnaceBlockEntity blockEntity;
    private final ContainerLevelAccess access;
    private final ContainerData containerData;

    public EssenceFurnaceMenu(Inventory inventory, int id, BlockPos pos) {
        this(inventory, id, new SimpleContainerData(2), ContainerLevelAccess.create(ClientProxy.MC.level, pos));
    }

    public EssenceFurnaceMenu(Inventory inventory, int id, ContainerData containerData, ContainerLevelAccess access) {
        super(PVZMenus.ESSENCE_FURNACE.get(), id);
        this.access = access;
        this.containerData = containerData;
        this.addDataSlots(containerData);
        this.access.execute((level, pos) -> {
            BlockEntity b = level.getBlockEntity(pos);
            this.blockEntity = b instanceof EssenceFurnaceBlockEntity be ? be : null;
        });

        //slots.
        this.addSlot(new SlotItemHandler(blockEntity.handler, 0, 56, 17));
        this.addSlot(new SlotItemHandler(blockEntity.handler, 1, 56, 53));
        this.addSlot(new SlotItemHandler(blockEntity.handler, 2, 116, 35) {
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return false;
            }
        });
        for(int i = 0; i < 3; ++ i) {
            for(int j = 0; j < 9; ++ j) {
                this.addSlot(new Slot(inventory, j + i * 9 + 9, 8 + 18 * j, 84 + 18 * i));
            }
        }
        for(int i = 0; i < 9; ++ i) {
            this.addSlot(new Slot(inventory, i, 8 + 18 * i, 142));
        }

    }

    @Override
    public ItemStack quickMoveStack(Player player, int num) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(num);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (num == 2) {
                if (!this.moveItemStackTo(itemstack1, 3, 39, true)) {
                    return ItemStack.EMPTY;
                }

                slot.onQuickCraft(itemstack1, itemstack);
            } else if (num != 1 && num != 0) {
                    if (num >= 3 && num < 30) {
                    if (!this.moveItemStackTo(itemstack1, 30, 39, false)) {
                        return ItemStack.EMPTY;
                    }
                } else if (num >= 30 && num < 39 && !this.moveItemStackTo(itemstack1, 3, 30, false)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(itemstack1, 3, 39, false)) {
                return ItemStack.EMPTY;
            }

            if (itemstack1.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (itemstack1.getCount() == itemstack.getCount()) {
                return ItemStack.EMPTY;
            }

            slot.onTake(player, itemstack1);
        }

        return itemstack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, PVZBlocks.ESSENCE_FURNACE.get());
    }

    public boolean isLit() {
        return getBurnProgress() > 0;
    }

    public float getBurnProgress() {
        if (containerData.get(1) <= 0) {
            return 0;
        }
        float progress = (float) (this.containerData.get(0) - 1) / containerData.get(1);
        return progress < 0 ? 0 : progress;
    }


    //RecipeBookMenu
    @Override
    public void fillCraftSlotsStackedContents(StackedContents contents) {
        for (Slot slot : slots) {
            contents.accountSimpleStack(slot.getItem());
        }
    }

    @Override
    public void clearCraftingContent() {
        this.getSlot(0).set(ItemStack.EMPTY);
        this.getSlot(1).set(ItemStack.EMPTY);
        this.getSlot(2).set(ItemStack.EMPTY);
    }

    @Override
    public boolean recipeMatches(Recipe<? super Container> recipe) {
        //TODO Proper?
        NonNullList<Ingredient> ingredients = recipe.getIngredients();
        return ingredients.get(0).test(slots.get(0).getItem()) && ingredients.get(1).test(slots.get(1).getItem());
    }

    @Override
    public int getResultSlotIndex() {
        return 2;
    }

    @Override
    public int getGridWidth() {
        return 1;
    }

    @Override
    public int getGridHeight() {
        return 2;
    }

    @Override
    public int getSize() {
        return 3;
    }

    @Override
    public RecipeBookType getRecipeBookType() {
        return OtherRegisters.essenceFurnaceRecipeBookType;
    }

    @Override
    public boolean shouldMoveToInventory(int index) {
        return true;
    }

}
