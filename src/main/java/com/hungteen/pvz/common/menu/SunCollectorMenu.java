package com.hungteen.pvz.common.menu;

import com.hungteen.pvz.common.block.entity.SunCollectorBlockEntity;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZMenus;
import net.minecraft.core.BlockPos;
import net.minecraft.world.Container;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.inventory.SimpleContainerData;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;
import org.jetbrains.annotations.Nullable;

/**
 * @author PangTeen
 * @program HTPVZ
 * @create 2026/10/24 16:00
 **/
public class SunCollectorMenu extends AbstractContainerMenu {

    private final ContainerLevelAccess access;
    private final ContainerData data;
    @Nullable
    private SunCollectorBlockEntity blockEntity;

    public SunCollectorMenu(Inventory inventory, int id, BlockPos pos) {
        this(inventory, id, new SimpleContainerData(2),
                ContainerLevelAccess.create(ClientProxy.getLevel(), pos), null, pos);
    }

    public SunCollectorMenu(Inventory inventory, int id, SunCollectorBlockEntity blockEntity,
                              ContainerLevelAccess access) {
        this(inventory, id, blockEntity.dataAccess, access, blockEntity, blockEntity.getBlockPos());
    }

    private SunCollectorMenu(Inventory inventory, int id, ContainerData data, ContainerLevelAccess access,
                               @Nullable SunCollectorBlockEntity directBlockEntity, BlockPos pos) {
        super(PVZMenus.SUN_COLLECTOR.get(), id);
        this.access = access;
        this.data = data;
        this.blockEntity = directBlockEntity;
        this.addDataSlots(data);

        if (this.blockEntity == null) {
            this.access.execute((level, blockPos) -> {
                BlockEntity blockEntity = level.getBlockEntity(pos);
                if (blockEntity instanceof SunCollectorBlockEntity collector) {
                    this.blockEntity = collector;
                }
            });
        }

        Container collectorSlots = this.blockEntity == null
                ? new SimpleContainer(SunCollectorBlockEntity.SLOT_COUNT)
                : this.blockEntity;
        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 3; ++column) {
                this.addSlot(new Slot(collectorSlots, column + row * 3,
                        62 + column * 18, 17 + row * 18) {
                    @Override
                    public boolean mayPlace(ItemStack stack) {
                        return SunCollectorBlockEntity.canStore(stack);
                    }
                });
            }
        }

        for (int row = 0; row < 3; ++row) {
            for (int column = 0; column < 9; ++column) {
                this.addSlot(new Slot(inventory, column + row * 9 + 9, 8 + column * 18, 84 + row * 18));
            }
        }
        for (int column = 0; column < 9; ++column) {
            this.addSlot(new Slot(inventory, column, 8 + column * 18, 142));
        }
    }

    @Override
    public ItemStack quickMoveStack(Player player, int slotId) {
        ItemStack movedStack = ItemStack.EMPTY;
        Slot slot = this.slots.get(slotId);
        if (slot.hasItem()) {
            ItemStack slotStack = slot.getItem();
            movedStack = slotStack.copy();
            if (slotId < SunCollectorBlockEntity.SLOT_COUNT) {
                if (!this.moveItemStackTo(slotStack, SunCollectorBlockEntity.SLOT_COUNT, this.slots.size(), true)) {
                    return ItemStack.EMPTY;
                }
            } else if (!this.moveItemStackTo(slotStack, 0, SunCollectorBlockEntity.SLOT_COUNT, false)) {
                return ItemStack.EMPTY;
            }

            if (slotStack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }

            if (slotStack.getCount() == movedStack.getCount()) {
                return ItemStack.EMPTY;
            }
            slot.onTake(player, slotStack);
        }
        return movedStack;
    }

    @Override
    public boolean stillValid(Player player) {
        return stillValid(this.access, player, PVZBlocks.SUN_COLLECTOR.get());
    }

    public int getSunlight() {
        return this.data.get(0);
    }

    public int getMaxSunlight() {
        return SunCollectorBlockEntity.MAX_SUN_AMOUNT;
    }
}
