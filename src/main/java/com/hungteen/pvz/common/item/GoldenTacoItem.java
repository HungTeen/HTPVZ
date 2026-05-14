package com.hungteen.pvz.common.item;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.api.ZombieEvent;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.function.Supplier;

public class GoldenTacoItem extends Item {
    private final Item changeTo;
    public GoldenTacoItem(Properties p_41383_, @Nullable Supplier<Item> changeTo) {
        super(p_41383_);
        this.changeTo = changeTo.get();
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(itemStack, level, entity, slotId, isSelected);
        if (! PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.goldenTacoDeteriorate)) return;
        if (! level.isClientSide && entity instanceof LivingEntity living && entity.tickCount % 20 == 0 && living.getRandom().nextInt(4) == 0) {
            var cap = PVZZombieEventCapability.fromLevel(level);
            if (cap == null) {
                living.getSlot(slotId).set(changeTo.getDefaultInstance());
                return;
            }
            ZombieEvent event = cap.getNearestEventRanged(ZombieEvent.class, entity.blockPosition(), ZombieEvent::isMainEvent);
            if (event == null || event.position.distSqr(living.blockPosition()) > event.range * event.range)
                living.getSlot(slotId).set(changeTo.getDefaultInstance());
        }
    }

    @Override
    public boolean onEntityItemUpdate(ItemStack itemStack, ItemEntity entity) {
        if (! entity.level.isClientSide && PVZConfig.PVZGameRules.getBoolean(entity.level, PVZConfig.Common.goldenTacoDeteriorate)
                && ! entity.level.isClientSide && entity.tickCount % 60 == 0) {
            var cap = PVZZombieEventCapability.fromLevel(entity.level);
            if (cap == null) {
                entity.setItem(changeTo.getDefaultInstance());
            } else {
                ZombieEvent event = cap.getNearestEventRanged(ZombieEvent.class, entity.blockPosition(), ZombieEvent::isMainEvent);
                if (event == null || event.position.distSqr(entity.blockPosition()) > event.range * event.range)
                    entity.setItem(changeTo.getDefaultInstance());
            }
        }
        return super.onEntityItemUpdate(itemStack, entity);
    }
}
