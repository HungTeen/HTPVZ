package com.hungteen.pvz.common.item;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.api.ZombieEvent;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.Tags;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Supplier;

public class GoldenTacoItem extends Item {
    private final Item changeTo;
    public GoldenTacoItem(Properties p_41383_, @Nullable Supplier<Item> changeTo) {
        super(p_41383_);
        this.changeTo = changeTo.get();
    }

    @Override
    public void inventoryTick(ItemStack itemStack, Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(itemStack, level, entity, slotId, isSelected);
        if (! PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.goldenTacoDeteriorate)) return;
        if (! level.isClientSide && entity instanceof LivingEntity living && entity.tickCount % 20 == 0 && living.getRandom().nextInt(10) == 0) {
            if (entity.level.getNearestEntity(LivingEntity.class, TargetingConditions.forCombat().selector(e -> e.getType().is(Tags.EntityTypes.BOSSES))
                    , living, entity.getX(), entity.getY(), entity.getZ(), entity.getBoundingBox().inflate(50, 25, 50)) != null) {
                return;
            }
            var cap = PVZZombieEventCapability.fromLevel(level);
            if (cap != null) {
                ZombieEvent event = cap.getNearestEventRanged(ZombieEvent.class, entity.blockPosition(), ZombieEvent::isMainEvent);
                if (event != null && event.position.distSqr(living.blockPosition()) < event.range * event.range) {
                    return;
                }
            }
            living.getSlot(slotId).set(changeTo.getDefaultInstance());
        }
    }

    @Override
    public void appendHoverText(ItemStack p_41421_, @Nullable Level p_41422_, List<Component> components, TooltipFlag p_41424_) {
        components.add(Component.translatable("tooltip.pvz.golden_taco").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
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
