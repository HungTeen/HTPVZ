package com.hungteen.pvz.common.block.entity;

import com.hungteen.pvz.common.block.EssenceFurnaceBlock;
import com.hungteen.pvz.common.menu.EssenceFurnaceRecipe;
import com.hungteen.pvz.common.register.PVZBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.Nameable;
import net.minecraft.world.WorldlyContainer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ContainerData;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeManager;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.items.ItemStackHandler;
import org.jetbrains.annotations.Nullable;

import static com.hungteen.pvz.common.register.OtherRegisters.essenceFurnaceRecipeType;

public class EssenceFurnaceBlockEntity extends BlockEntity implements Nameable, WorldlyContainer {
    private Component name;
    private short progress = 0;
    public final ItemStackHandler handler = new ItemStackHandler(3);
    private final RecipeManager.CachedCheck<Container, EssenceFurnaceRecipe> quickCheck;
    private EssenceFurnaceRecipe recipe = null;
    private static final int[] SLOTS_FOR_UP = new int[]{0};
    private static final int[] SLOTS_FOR_DOWN = new int[]{2, 1};
    private static final int[] SLOTS_FOR_SIDES = new int[]{1};
    public final ContainerData dataAccess = new ContainerData() {
        public int get(int num) {
            return num == 0 ? progress : (recipe != null ? recipe.needTime : -1);
        }
        public void set(int num, int value) {
            if (num == 0) {
                progress = (short) value;
            }
        }
        public int getCount() {
            return 2;
        }
    };

    public EssenceFurnaceBlockEntity(BlockPos pos, BlockState blockState) {
        super(PVZBlockEntities.ESSENCE_FURNACE.get(), pos, blockState);
        this.quickCheck = RecipeManager.createCheck(essenceFurnaceRecipeType.get());
    }


    public static void tick(Level level, BlockPos pos, BlockState blockState, EssenceFurnaceBlockEntity blockEntity) {
        if (!blockEntity.handler.getStackInSlot(0).isEmpty() && !blockEntity.handler.getStackInSlot(1).isEmpty()) {
            EssenceFurnaceRecipe recipe = blockEntity.quickCheck.getRecipeFor(blockEntity, level).orElse(null);
            if (blockEntity.recipe != recipe) {
                blockEntity.recipe = recipe;
                blockEntity.progress = 1;
            }
            if (recipe != null) {
                short recipeTime = recipe.needTime;
                ItemStack result = blockEntity.handler.getStackInSlot(2);
                if (((result.is(recipe.getResultItem().getItem()) && result.getCount() < result.getMaxStackSize()) ||
                        result.isEmpty()) &&
                        ++blockEntity.progress > recipeTime) {
                    blockEntity.progress = 1;
                    if (! level.isClientSide()) {
                        if (result.isEmpty()) {
                            blockEntity.handler.setStackInSlot(2, recipe.getResultItem().copy());
                        } else {
                            result.setCount(result.getCount() + 1);
                        }
                        if (recipe.costFuel) {
                            ItemStack fuel = blockEntity.handler.getStackInSlot(1);
                            if (fuel.isDamageableItem()) {
                                fuel.hurtAndBreak(1, null, (player)->{});
                            } else {
                                fuel.shrink(1);
                            }
                        }
                        if (recipe.costIngredient) {
                            ItemStack ingredient = blockEntity.handler.getStackInSlot(0);
                            if (ingredient.isDamageableItem()) {
                                ingredient.hurtAndBreak(1, null, (player)->{});
                            } else {
                                ingredient.shrink(1);
                            }
                        }
                    }
                }
            } else {
                blockEntity.progress = 0;
            }
        } else {
            blockEntity.progress = 0;
        }
        if (!level.isClientSide()) {
            level.setBlock(pos, blockState.setValue(EssenceFurnaceBlock.LIT, blockEntity.progress != 0), 3);
        }
    }

    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (this.hasCustomName()) {
            tag.putString("CustomName", Component.Serializer.toJson(this.name));
        }
            tag.put("slots", this.handler.serializeNBT());
            tag.putShort("progress", this.progress);
    }

    public void load(CompoundTag tag) {
        super.load(tag);
        if (tag.contains("CustomName", 8)) {
            this.name = Component.Serializer.fromJson(tag.getString("CustomName"));
        }
        if (tag.contains("slots")) {
            this.handler.deserializeNBT(tag.getCompound("slots"));
        }
        if(tag.contains("progress")) {
            this.progress = tag.getShort("progress");
        }

    }

    //Nameable
    @Override
    public Component getName() {
        return this.hasCustomName() ? this.getCustomName() : Component.translatable("block.pvz.essence_furnace");
    }
    @Override
    public Component getCustomName() {
        return name;
    }

    public void setCustomName(@javax.annotation.Nullable Component component) {
        this.name = component;
    }

    public float getBurnProgress() {
        if (recipe == null) {
            return 0;
        }
        float progress = (float) (this.progress - 1) / recipe.needTime;
        return progress < 0 ? 0 : progress;
    }

    //worldlyContainer
    @Override
    public int[] getSlotsForFace(Direction direction) {
        if (direction == Direction.DOWN) {
            return SLOTS_FOR_DOWN;
        } else {
            return direction == Direction.UP ? SLOTS_FOR_UP : SLOTS_FOR_SIDES;
        }
    }

    @Override
    public boolean canPlaceItemThroughFace(int p_19235_, ItemStack p_19236_, @Nullable Direction p_19237_) {
        return this.canPlaceItem(p_19235_, p_19236_);
    }

    @Override
    public boolean canTakeItemThroughFace(int p_19239_, ItemStack itemStack, Direction direction) {
        return true;
    }

    @Override
    public int getContainerSize() {
        return this.handler.getSlots();
    }

    @Override
    public boolean isEmpty() {
        for(int i = 0; i < this.handler.getSlots(); i ++) {
            if (!this.handler.getStackInSlot(i).isEmpty()) {
                return false;
            }
        }

        return true;
    }

    @Override
    public ItemStack getItem(int num) {
        return this.handler.getStackInSlot(num);
    }

    @Override
    public ItemStack removeItem(int p_18942_, int p_18943_) {
        NonNullList<ItemStack> items = NonNullList.create();
        for (int i = 0; i < this.handler.getSlots(); i ++) {
            items.add(this.handler.getStackInSlot(i));
        }
        return ContainerHelper.removeItem(items, p_18942_, p_18943_);
    }

    @Override
    public ItemStack removeItemNoUpdate(int p_18951_) {
        NonNullList<ItemStack> items = NonNullList.create();
        for (int i = 0; i < this.handler.getSlots(); i ++) {
            items.add(this.handler.getStackInSlot(i));
        }
        return ContainerHelper.takeItem(items, p_18951_);
    }

    @Override
    public void setItem(int num, ItemStack itemstack) {
        ItemStack itemstack1 = this.handler.getStackInSlot(num);
        boolean flag = !itemstack.isEmpty() && itemstack1.sameItem(itemstack1) && ItemStack.tagMatches(itemstack, itemstack1);
        this.handler.setStackInSlot(num, itemstack);
        if (itemstack.getCount() > this.getMaxStackSize()) {
            itemstack.setCount(this.getMaxStackSize());
        }

        if (num == 0 && !flag) {
            this.recipe = quickCheck.getRecipeFor(this, this.level).orElse(null);
            this.progress = 0;
            this.setChanged();
        }
    }

    @Override
    public boolean stillValid(Player player) {
        if (this.level.getBlockEntity(this.worldPosition) != this) {
            return false;
        } else {
            return player.distanceToSqr((double)this.worldPosition.getX() + 0.5D, (double)this.worldPosition.getY() + 0.5D, (double)this.worldPosition.getZ() + 0.5D) <= 64.0D;
        }
    }

    @Override
    public void clearContent() {
        for (int i = 0; i < this.handler.getSlots(); i ++) {
            handler.setStackInSlot(i, ItemStack.EMPTY);
        }
    }
}
