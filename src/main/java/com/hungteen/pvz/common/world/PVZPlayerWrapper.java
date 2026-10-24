package com.hungteen.pvz.common.world;

import com.hungteen.pvz.api.PVZAPI;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.api.interfaces.ICanUseSeedPack;
import com.hungteen.pvz.api.interfaces.ISun;
import com.hungteen.pvz.api.interfaces.ISunAbsorber;
import com.hungteen.pvz.common.block.entity.SunCollectorBlockEntity;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapStats;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZAttributes;
import com.hungteen.pvz.common.register.PVZEnchantments;
import com.hungteen.pvz.common.register.PVZStats;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.Map;
import java.util.concurrent.atomic.AtomicBoolean;

public record PVZPlayerWrapper(Player player) implements ISunAbsorber, ICanUseSeedPack {

    @Override
    public void setAmount(int num) {
        // TODO PT 直接设置 Cap
    }

    @Override
    public int getAmount() {
        return PVZAPI.get().getSun(player);
    }

    @Override
    public int getCapacity() {
        return (int) player.getAttribute(PVZAttributes.MAX_SUN.get()).getValue();
    }

    @Override
    public Player getEntity() {
        return player;
    }

    @Override
    public ItemCooldowns getCooldowns() {
        return player.getCooldowns();
    }

    @Override
    public MutableComponent prePlantOnBlock(PVZResourceEvent.CheckPlantConditionEvent event, Entity entity
            , ItemStack plantCard, Level level, BlockPos pos, Direction side) {
        if (PVZPlayerCapability.getValue(player, PVZPlayerCapStats.CAN_PLANT) == 0)
            return Component.translatable("hint.pvz.plant.player_cant_plant");
        if (plantCard.getItem() instanceof SeedPacketItem<?> item) {
            String resource = item.getResource(plantCard);
            if (resource.equals(PVZPlayerCapStats.SUN) && PVZPlayerCapability.getValue(player, "plant_have_cost") == 0) event.cost = 0;
            if (PVZPlayerCapability.getValue(player, "plant_have_cd") == 0) event.coolDown = 1;
            return null;
        }
        return Component.translatable("hint.pvz.plant.player_cant_plant");
    }

    @Override
    public MutableComponent prePlantOnEntity(PVZResourceEvent.CheckPlantConditionEvent event, Entity entity
            , ItemStack plantCard, Entity target) {
        if (PVZPlayerCapability.getValue(player, PVZPlayerCapStats.CAN_PLANT) == 0)
            return Component.translatable("hint.pvz.plant.player_cant_plant");
        if (plantCard.getItem() instanceof SeedPacketItem<?> item) {
            String resource = item.getResource(plantCard);
            if (resource.equals(PVZPlayerCapStats.SUN) && PVZPlayerCapability.getValue(player, "plant_have_cost") == 0) event.cost = 0;
            if (PVZPlayerCapability.getValue(player, "plant_have_cd") == 0) event.coolDown = 1;
            return null;
        }
        return Component.translatable("hint.pvz.plant.player_cant_plant");
    }

    @Override
    public void postPlantOnBlock(PVZResourceEvent.CheckPlantConditionEvent event, Entity entity, ItemStack itemStack
            , MutableComponent posCheck, Level level, BlockPos pos, Direction direction) {
        if (itemStack.getItem() instanceof SeedPacketItem<?> item) {
            PVZEntityCapability cap = entity.getCapability(PVZEntityCapability.CAP).orElse(null);
            if (cap != null && item.shouldDefineOwner(itemStack)) {
                cap.setOwner(getEntity());
            }
            if (posCheck == null) { // plant success.
                PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> nbt.addValue(event.resource, - event.cost));
                if (event.coolDown > 0) {
                    SeedPacketItem.seedPacketItemList.forEach(item1 -> {
                        if (item1.getEntity().equals(item.getEntity())) {
                            player.getCooldowns().addCooldown(item1, event.coolDown);
                        }
                    });
                }
                if (event.resource.equals(PVZAPI.get().getSunResourceName())) {
                    getEntity().awardStat(PVZStats.USE_SUN, event.cost);
                }
                getEntity().awardStat(PVZStats.PLANT);
            }
        }
    }

    @Override
    public void postPlantOnEntity(PVZResourceEvent.CheckPlantConditionEvent event, Entity entity, ItemStack itemStack
            , MutableComponent entityCheck, Entity target) {
        if (itemStack.getItem() instanceof SeedPacketItem<?> item) {
            String resource = item.getResource(itemStack);
            if (resource.equals(PVZPlayerCapStats.SUN) && PVZPlayerCapability.getValue(player, "plant_have_cost") == 0) event.cost = 0;
            if (PVZPlayerCapability.getValue(player, "plant_have_cd") == 0) event.coolDown = 1;
            PVZEntityCapability cap = entity.getCapability(PVZEntityCapability.CAP).orElse(null);
            if (cap != null && item.shouldDefineOwner(itemStack)) {
                cap.setOwner(getEntity());
            }
            if (entityCheck == null) { // plant success.
                PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> nbt.addValue(event.resource, - event.cost));
                if (event.coolDown > 0) {
                    SeedPacketItem.seedPacketItemList.forEach(item1 -> {
                        if (item1.getEntity().equals(item.getEntity())) {
                            player.getCooldowns().addCooldown(item1, event.coolDown);
                        }
                    });
                }
                if (event.resource.equals(PVZAPI.get().getSunResourceName())) {
                    getEntity().awardStat(PVZStats.USE_SUN, event.cost);
                }
                getEntity().awardStat(PVZStats.PLANT);
            }
        }
    }

    @Override
    public boolean canAbsorb(ISun sun) {
        if (!(sun instanceof Entity entity)) {
            return ISunAbsorber.super.canAbsorb(sun);
        }
        if(!EntityUtil.isEntityValid(player)) {
            return false;
        }
        if (player.isCreative()) {
            return true;
        }
        if (player.level.isClientSide && ClientProxy.getPlayer() != player) {
            return false;
        }
        Map.Entry<EquipmentSlot, ItemStack> entry =
            EnchantmentHelper.getRandomItemWith(PVZEnchantments.SUN_MENDING.get(), player, ItemStack::isDamaged);
        if (entry != null) {
            return true;
        }
        final AtomicBoolean result = new AtomicBoolean();
        PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> result.set(
            nbt.getValue(PVZPlayerCapStats.SUN) < nbt.getValueLimit(PVZPlayerCapStats.SUN).getSecond()));
        return result.get();
    }

    @Override
    public void onAbsorb(ISun sun) {
        PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> {
            int currentSun = nbt.getValue(PVZPlayerCapStats.SUN);
            // sun mending enchantment.
            if (currentSun >= nbt.getValueLimit(PVZPlayerCapStats.SUN).getSecond()) {
                Map.Entry<EquipmentSlot, ItemStack> entry =
                    EnchantmentHelper.getRandomItemWith(PVZEnchantments.SUN_MENDING.get(), player,
                        ItemStack::isDamaged);
                if (entry != null) {
                    ItemStack itemStack = entry.getValue();
                    int amount = sun.getAmount() / SunCollectorBlockEntity.SUN_AMOUNT_PER_REPAIR;
                    sun.onSunConsume(sun.getAmount());
                    itemStack.setDamageValue(Math.max(0, itemStack.getDamageValue() - amount));
                    return;
                }
            }
            //player absorb.
            int num = sun.getAmount();
            nbt.addValue(PVZPlayerCapStats.SUN, num);
            int actualConsume = nbt.getValue(PVZPlayerCapStats.SUN) - currentSun;
            int consumeAmount = player.isCreative() ? num : actualConsume;
            sun.onSunConsume(consumeAmount);
            if (!player.isCreative()) {
                player.awardStat(PVZStats.COLLECT_SUN_VALUE, actualConsume);
            }
            if (actualConsume > 0) {
                player.awardStat(PVZStats.COLLECT_SUN);
            }
        });
    }

    @Override
    public Vec3 getAbsorbPosition() {
        return player.getEyePosition(0.5F);
    }
}
