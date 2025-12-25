package com.hungteen.pvz.common.item;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class DisappearableItem extends Item {
    private final Item changeTo;
    public DisappearableItem(Properties p_41383_, @Nullable Supplier<Item> changeTo) {
        super(p_41383_);
        this.changeTo = changeTo.get();
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(itemStack, level, entity, slotId, isSelected);
        if (! level.isClientSide && entity instanceof LivingEntity living && entity.tickCount % 50 == 0) {
            itemStack.hurtAndBreak(1, living, entity1 -> entity1.getSlot(slotId).set(changeTo.getDefaultInstance()));
        }
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack itemStack, ItemEntity entity) {
        if (! entity.level.isClientSide && entity.tickCount % 50 == 0) {
            itemStack.setDamageValue(itemStack.getDamageValue() + 1);
            if (itemStack.getDamageValue() >= itemStack.getMaxDamage()) {
                ItemStack changeToStack = changeTo.getDefaultInstance();
                if (changeToStack.isEmpty()) {
                    entity.discard();
                } else {
                    entity.setItem(changeTo.getDefaultInstance());
                }
            }
        }
        return super.onEntityItemUpdate(itemStack, entity);
    }
}
