package com.hungteen.pvz.common.block.entity;

import com.hungteen.pvz.api.interfaces.ISun;
import com.hungteen.pvz.api.interfaces.ISunAbsorber;
import com.hungteen.pvz.api.interfaces.ISunContainer;
import com.hungteen.pvz.api.interfaces.ICanChargeSun;
import com.hungteen.pvz.common.menu.SunCollectorMenu;
import com.hungteen.pvz.common.register.PVZBlockEntities;
import com.hungteen.pvz.common.register.PVZEnchantments;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.inventory.ContainerLevelAccess;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BaseContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.Collections;

/**
 * @author PangTeen
 * @program HTPVZ
 * @create 2026/10/24 16:00
 **/
public class SunCollectorBlockEntity extends BaseContainerBlockEntity implements ISunAbsorber, ISunContainer {

    public static final int SLOT_COUNT = 9;
    public static final int COLLECT_RADIUS = 6;
    public static final int MAX_SUN_AMOUNT = 10000;
    public static final int SUN_AMOUNT_PER_REPAIR = 25;
    public static final int SUN_AMOUNT_PER_CHARGE = 25;
    private static final int COLLECT_INTERVAL = 20;
    private static final int REPAIR_INTERVAL = 10;

    private final NonNullList<ItemStack> items = NonNullList.withSize(SLOT_COUNT, ItemStack.EMPTY);
    private int sunAmount;
    public int tickCount;

    public final ContainerData dataAccess = new ContainerData() {
        @Override
        public int get(int index) {
            return switch (index) {
                case 0 -> SunCollectorBlockEntity.this.sunAmount;
                default -> 0;
            };
        }

        @Override
        public void set(int index, int value) {
            if (index == 0) {
                SunCollectorBlockEntity.this.sunAmount = value;
            }
        }

        @Override
        public int getCount() {
            return 1;
        }
    };

    public SunCollectorBlockEntity(BlockPos pos, BlockState blockState) {
        super(PVZBlockEntities.SUN_COLLECTOR.get(), pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState blockState, SunCollectorBlockEntity blockEntity) {
        if (level.isClientSide) {
            return;
        }

        ++ blockEntity.tickCount;
        if (blockEntity.tickCount % COLLECT_INTERVAL == 0) {
            // 需要寻找阳光并吸收
            if (blockEntity.getAmount() < blockEntity.getCapacity()) {
                Vec3 center = Vec3.atCenterOf(pos);
                AABB aabb = new AABB(center.x - COLLECT_RADIUS, center.y - COLLECT_RADIUS, center.z - COLLECT_RADIUS, center.x + COLLECT_RADIUS,
                        center.y + COLLECT_RADIUS, center.z + COLLECT_RADIUS);
                level.getEntities(null, aabb).stream()
                    .filter(ISun.class::isInstance)
                    .map(ISun.class::cast)
                    .filter(sun -> sun.canAttractThis(blockEntity))
                    .forEach(sun -> sun.trySetAttractor(blockEntity));
            }
            Vec3 center = Vec3.atCenterOf(pos);
            AABB aabb = new AABB(center.x - COLLECT_RADIUS, center.y - COLLECT_RADIUS, center.z - COLLECT_RADIUS, center.x + COLLECT_RADIUS,
                    center.y + COLLECT_RADIUS, center.z + COLLECT_RADIUS);
            blockEntity.chargeNearbyContainers(level, aabb);
        }
        if (blockEntity.getAmount() >= SUN_AMOUNT_PER_REPAIR && blockEntity.tickCount % REPAIR_INTERVAL == 1) {
            // 寻找需要修补的物品
            for (int slot = 0; slot < SLOT_COUNT; ++slot) {
                ItemStack stack = blockEntity.getItem(slot);
                if (stack.isDamaged()
                    && EnchantmentHelper.getItemEnchantmentLevel(PVZEnchantments.SUN_MENDING.get(), stack) > 0) {
                    stack.setDamageValue(stack.getDamageValue() - 1);
                    blockEntity.setAmount(blockEntity.getAmount() - SUN_AMOUNT_PER_REPAIR);
                    blockEntity.setChanged();
                    break;
                }
            }
        }
    }

    private void chargeNearbyContainers(Level level, AABB aabb) {
        if (this.getAmount() <= 0) {
            return;
        }
        for (Entity entity : level.getEntitiesOfClass(Entity.class, aabb,
                entity -> entity instanceof ICanChargeSun chargeable && chargeable.canChargeSun())) {
            ICanChargeSun chargeable = (ICanChargeSun) entity;
            int accepted = chargeable.chargeSun(Math.min(SUN_AMOUNT_PER_CHARGE, this.getAmount()));
            if (accepted > 0) {
                this.setAmount(this.getAmount() - accepted);
                this.setChanged();
            }
            if (this.getAmount() <= 0) {
                break;
            }
        }
    }

    public static boolean canStore(ItemStack stack) {
        return !stack.isEmpty()
                && stack.isDamageableItem()
                && EnchantmentHelper.getItemEnchantmentLevel(PVZEnchantments.SUN_MENDING.get(), stack) > 0;
    }

    @Override
    protected Component getDefaultName() {
        return Component.translatable("block.pvz.sun_collector");
    }

    @Override
    protected AbstractContainerMenu createMenu(int id, Inventory inventory) {
        return new SunCollectorMenu(inventory, id, this,
                ContainerLevelAccess.create(inventory.player.level, this.worldPosition));
    }

    @Override
    public int getContainerSize() {
        return SLOT_COUNT;
    }

    @Override
    public boolean isEmpty() {
        return this.items.stream().allMatch(ItemStack::isEmpty);
    }

    @Override
    public ItemStack getItem(int slot) {
        return this.items.get(slot);
    }

    @Override
    public ItemStack removeItem(int slot, int amount) {
        ItemStack stack = ContainerHelper.removeItem(this.items, slot, amount);
        if (!stack.isEmpty()) {
            this.setChanged();
        }
        return stack;
    }

    @Override
    public ItemStack removeItemNoUpdate(int slot) {
        return ContainerHelper.takeItem(this.items, slot);
    }

    @Override
    public void setItem(int slot, ItemStack stack) {
        this.items.set(slot, stack);
        if (stack.getCount() > this.getMaxStackSize()) {
            stack.setCount(this.getMaxStackSize());
        }
        this.setChanged();
    }

    @Override
    public boolean stillValid(Player player) {
        return this.level != null
                && this.level.getBlockEntity(this.worldPosition) == this
                && player.distanceToSqr(
                this.worldPosition.getX() + 0.5D,
                this.worldPosition.getY() + 0.5D,
                this.worldPosition.getZ() + 0.5D) <= 64.0D;
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return canStore(stack);
    }

    @Override
    public void clearContent() {
        Collections.fill(this.items, ItemStack.EMPTY);
        this.setChanged();
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        ContainerHelper.saveAllItems(tag, this.items);
        tag.putInt("SunAmount", this.sunAmount);
        tag.putInt("TickCount", this.tickCount);
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        Collections.fill(this.items, ItemStack.EMPTY);
        ContainerHelper.loadAllItems(tag, this.items);
        this.setAmount(tag.getInt("SunAmount"));
        this.tickCount = tag.getInt("TickCount");
    }

    @Override
    public void setAmount(int num) {
        this.sunAmount = Math.max(0, Math.min(MAX_SUN_AMOUNT, num));
    }

    @Override
    public int getAmount() {
        return this.sunAmount;
    }

    @Override
    public int getCapacity() {
        return MAX_SUN_AMOUNT;
    }

    @Override
    public int getAbsorbPriority() {
        return ISunAbsorber.COLLECTOR_ABSORB_PRIORITY;
    }

    @Override
    public boolean canAbsorb(ISun sun) {
        return getAmount() < getCapacity();
    }

    @Override
    public void onAbsorb(ISun sun) {
        int consumeAmount = Math.min(sun.getAmount(), this.getCapacity() - this.sunAmount - sun.getAmount());
        this.sunAmount = Math.min(this.getCapacity(), this.sunAmount + consumeAmount);
        sun.onSunConsume(consumeAmount);
        this.setChanged();
    }

    @Override
    public Vec3 getAbsorbPosition() {
        return Vec3.atCenterOf(this.getBlockPos());
    }

}
