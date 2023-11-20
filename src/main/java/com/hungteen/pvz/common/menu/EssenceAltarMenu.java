package com.hungteen.pvz.common.menu;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.IHaveSkills;
import com.hungteen.pvz.common.item.SeedItem;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZMenus;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nonnull;

public class EssenceAltarMenu extends AbstractContainerMenu {

    private final Container altarSlots = new SimpleContainer(3) {
        public void setChanged() {
            super.setChanged();
            EssenceAltarMenu.this.slotsChanged(this);
        }
    };
    public final ContainerLevelAccess access;

    public EssenceAltarMenu(Inventory inventory, int id) {
        this(inventory, id, ContainerLevelAccess.NULL);
    }

    public EssenceAltarMenu(Inventory inventory, int id, ContainerLevelAccess access) {
        super(PVZMenus.ESSENCE_ALTAR.get(), id);
        this.access = access;

        //slots.
        this.addSlot(new Slot(this.altarSlots, 0, 25, 17){
            @Override
            public boolean mayPlace(@Nonnull ItemStack stack) {
                return super.mayPlace(stack) && stack.getItem() instanceof SeedPacketItem<?> item && ! (item instanceof SeedItem<?>);
            }
        });
        this.addSlot(new Slot(this.altarSlots, 1, 15, 54));
        this.addSlot(new Slot(this.altarSlots, 2, 35, 54){
            @Override
            public boolean mayPlace(@Nonnull ItemStack stack) {
                return super.mayPlace(stack) && stack.getItem() instanceof SeedPacketItem<?> item && item instanceof SeedItem<?>;
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
    public ItemStack quickMoveStack(Player player, int slotId) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotId);
        if (slot.hasItem()) {
            ItemStack itemstack1 = slot.getItem();
            itemstack = itemstack1.copy();
            if (slotId < 4) {
                if (!this.moveItemStackTo(itemstack1, 4, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if(slotId < 4 + 27){
                if (!this.moveItemStackTo(itemstack1, 0, 4, true) && !this.moveItemStackTo(itemstack1, 4 + 27, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else {
                if (!this.moveItemStackTo(itemstack1, 0, 4 + 27, true)) {
                    return ItemStack.EMPTY;
                }
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
        return stillValid(this.access, player, PVZBlocks.ESSENCE_ALTAR.get());
    }

    @Override
    public boolean clickMenuButton(Player player, int skillID) {
        if (slots.get(0).hasItem() && slots.get(0).getItem().getItem() instanceof SeedPacketItem<?> item) {
            if (! (item instanceof SeedItem<?>) && item.getEntity().create(player.level) instanceof IHaveSkills e) {
                if (e.getStaticSkillList().size() <= skillID) {
                    PVZMod.LOGGER.info("Chosen skill of "+ player.getName().getString() +" not exists.");
                }
                Skill skill = e.getStaticSkillList().get(skillID);
                ((Entity)e).discard();
                int costSeedPacket = skill.costSeedPacket;
                int costItem = skill.costItem;
                if ((! player.getAbilities().instabuild) &&
                        ((costSeedPacket > 0 &&
                            ! (this.slots.get(2).hasItem() &&
                                    this.slots.get(2).getItem().getCount() >= costSeedPacket &&
                                    this.slots.get(2).getItem().getItem() instanceof SeedItem<?> s &&
                                    s.getEntity().equals(item.getEntity()))) ||
                        (costItem > 0 &&
                                ! (this.slots.get(1).hasItem() &&
                                        this.slots.get(1).getItem().getCount() >= costItem &&
                                        this.slots.get(1).getItem().getItem().getDescriptionId().equals(
                                                skill.item.get().getDescriptionId()))))) {
                    PVZMod.LOGGER.info(player.getName().getString() +" not match the condition of attaching skill.");
                    return false;
                } else {
                    if (! player.getAbilities().instabuild) {
                        if (slots.get(2).hasItem()) {
                            slots.get(2).getItem().shrink(costSeedPacket);
                        }
                        if (slots.get(1).hasItem()) {
                            slots.get(1).getItem().shrink(costItem);
                        }
                    }
                    slots.get(0).getItem().getOrCreateTag().putInt("Skill", skillID);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public void removed(Player player) {
        if (player instanceof ServerPlayer plr) {
            for (int i = 0; i < 3; i ++) {
                ItemStack itemstack = altarSlots.getItem(i);
                if (!itemstack.isEmpty()) {
                    if (plr.isAlive() && !plr.hasDisconnected()) {
                        plr.getInventory().placeItemBackInInventory(itemstack);
                    } else {
                        plr.drop(itemstack, false);
                    }
                }
            }
        }
        super.removed(player);
    }
}
