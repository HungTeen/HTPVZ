package com.hungteen.pvz.common.item;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.events.IsInInventoryEvent;
import com.hungteen.pvz.client.renderer.item.EnderSeedBundleItemRenderer;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.network.EnderSeedBundleContainerPacket;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.SlotAccess;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.function.Consumer;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class EnderSeedBundleItem extends Item {
    /**Methods to set/get of the contained items in the bundle, see
     * {@link com.hungteen.pvz.common.capability.player.PVZPlayerCapability#setEnderSeedBundleSlot(Player, int, ItemStack) setEnderSeedBundleSlot(Player, int, ItemStack)} and
     * {@link com.hungteen.pvz.common.capability.player.PVZPlayerCapability#getEnderSeedBundleSlot(Player, int) getEnderSeedBundleSlot(Player, int)}.*/
    public EnderSeedBundleItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bundle = player.getItemInHand(hand);
        if (bundle.getItem() instanceof EnderSeedBundleItem) {
            int slot = getPointer(player.getItemInHand(hand));
            ItemStack packet = PVZPlayerCapability.getEnderSeedBundleSlot(player, slot);
            if (packet.getItem() instanceof SeedPacketItem<?> packetItem) {
                if (! level.isClientSide) {
                    player.setItemInHand(hand, packet);
                    var result = packetItem.use(level, player, hand);
                    result.object = bundle;
                    player.setItemInHand(hand, bundle);
                    EnderSeedBundleContainerPacket.syncToClient((ServerPlayer) player, slot);
                    return result;
                }
            }
        }
        return super.use(level, player, hand);
    }

    @net.minecraftforge.eventbus.api.SubscribeEvent
    public static void interactEntity(PlayerInteractEvent.EntityInteract ev) {
        Player player = ev.getEntity();
        InteractionHand hand = ev.getHand();
        ItemStack bundle = player.getItemInHand(ev.getHand());
        if (bundle.getItem() instanceof EnderSeedBundleItem bundleItem) {
            int slot = bundleItem.getPointer(player.getItemInHand(hand));
            ItemStack packet = PVZPlayerCapability.getEnderSeedBundleSlot(player, slot);
            if (packet.getItem() instanceof SeedPacketItem<?> packetItem) {
                if (! player.level.isClientSide) {
                    player.setItemInHand(hand, packet);
                    packetItem.interactEntity(ev);
                    player.setItemInHand(hand, bundle);
                    EnderSeedBundleContainerPacket.syncToClient((ServerPlayer) player, slot);
                }
            }
        }
    }

    @Override
    public boolean overrideStackedOnOther(ItemStack bundle, Slot slot, ClickAction clickAction, Player player) {
        if (clickAction != ClickAction.SECONDARY) {
            return false;
        } else {
            ItemStack other = slot.getItem();
            if (other.isEmpty()) {
                this.playRemoveOneSound(player);
                ItemStack item1 = getNextItem(player, bundle, true);
                if (! item1.isEmpty()) slot.safeInsert(item1);
            } else if (other.getItem().canFitInsideContainerItems()) {
                boolean put = putItem(player, bundle, other);// add(bundle, slot.safeTake(other.getCount(), i, player));
                if (put) {
                    this.playInsertSound(player);
                }
            }

            return true;
        }
    }

    @Override
    public boolean overrideOtherStackedOnMe(ItemStack bundle, ItemStack other, Slot slot, ClickAction clickAction, Player player, SlotAccess slotAccess) {
        if (clickAction == ClickAction.SECONDARY && slot.allowModification(player)) {
            if (other.isEmpty()) {
                ItemStack itemStack = getNextItem(player, bundle, true);
                if (! itemStack.isEmpty()) {
                    this.playRemoveOneSound(player);
                    slotAccess.set(itemStack);
                }
            } else {
                boolean put = putItem(player, bundle, other);
                if (put) {
                    this.playInsertSound(player);
                }
            }

            return true;
        } else {
            return false;
        }
    }
    public int getPointer(@Nullable ItemStack itemStack) {
        if (itemStack != null && itemStack.getItem() instanceof EnderSeedBundleItem && itemStack.getTag() != null) {
            return itemStack.getTag().contains("Pointer") ? itemStack.getTag().getByte("Pointer") : 0;
        }
        return 0;
    }

    public void setPointer(ItemStack itemStack, int slot) {
        if (itemStack != null && itemStack.getItem() instanceof EnderSeedBundleItem) {
            itemStack.getOrCreateTag().putByte("Pointer", (byte) slot);
        }
    }

    public void playRemoveOneSound(Entity p_186343_) {
        p_186343_.playSound(SoundEvents.BUNDLE_REMOVE_ONE, 0.8F, 0.8F + p_186343_.getLevel().getRandom().nextFloat() * 0.4F);
    }

    public void playInsertSound(Entity p_186352_) {
        p_186352_.playSound(SoundEvents.BUNDLE_INSERT, 0.8F, 0.8F + p_186352_.getLevel().getRandom().nextFloat() * 0.4F);
    }

    public void playDropContentsSound(Entity p_186354_) {
        p_186354_.playSound(SoundEvents.BUNDLE_DROP_CONTENTS, 0.8F, 0.8F + p_186354_.getLevel().getRandom().nextFloat() * 0.4F);
    }

    public boolean putItem(Player player, ItemStack bundle, ItemStack item) {
        if (! (item.getItem() instanceof SeedPacketItem<?>)) return false;
        int count = item.getCount();
        for (int i = 0; i < 9; i ++) {
            if (putItem(player, bundle, item, i)) {
                if (item.isEmpty() || item.getCount() == 0) return true;
            }
        }
        return count != item.getCount();
    }

    public boolean putItem(Player player, @Nullable ItemStack bundle, ItemStack item, int slot) {
        if (! (item.getItem() instanceof SeedPacketItem<?>)) return false;
        if (! item.isEmpty()) {
            ItemStack current = PVZPlayerCapability.getEnderSeedBundleSlot(player, slot);
            int count = Math.min(item.getCount(), item.getMaxStackSize() - current.getCount());
            if (current.isEmpty()) {
                PVZPlayerCapability.setEnderSeedBundleSlot(player, slot, item.copy());
                item.shrink(item.getCount());
                if (bundle != null) setPointer(bundle, slot);
            } else if (count > 0 && ItemStack.isSameItemSameTags(current, item)) {
                item.shrink(count);
                current.grow(count);
                PVZPlayerCapability.setEnderSeedBundleSlot(player, slot, current);
                if (bundle != null) setPointer(bundle, slot);
            }
            return count > 0;
        } else {
            return false;
        }
    }

    public ItemStack getNextItem(Player player, ItemStack bundle, boolean remove) {
        int slot = getPointer(bundle);
        ItemStack result = ItemStack.EMPTY;
        for (int i = 0; i < 9; i ++) {
            result = PVZPlayerCapability.getEnderSeedBundleSlot(player, slot);
            if (! result.isEmpty()) break;
            slot ++;
            if (slot > 8) slot -= 9;
        }
        if (! result.isEmpty()) {
            if (remove) {
                PVZPlayerCapability.setEnderSeedBundleSlot(player, slot, ItemStack.EMPTY);
                slot ++;
                if (slot > 8) slot -= 9;
            }
            setPointer(bundle, slot);
            return result;
        }
        return result;
    }

    public static boolean isInInventory(Player player, ItemStack itemStack) {
        for (int i = 0; i < 41; i ++) {
            if (ClientProxy.getPlayer().getInventory().getItem(i) == itemStack) {
                return true;
            }
        }
        IsInInventoryEvent event = new IsInInventoryEvent(player, itemStack);
        MinecraftForge.EVENT_BUS.post(event);
        return event.getResult() == Event.Result.ALLOW;
    }

    public static boolean selectEnderSeedBundle(Player player) {
        int slot = getHotBarEnderSeedBundleSlot(player);
        if (Inventory.isHotbarSlot(slot)) {
            player.getInventory().selected = slot;
            return true;
        }
        return false;
    }

    public static int getHotBarEnderSeedBundleSlot(Player player) {
        Inventory inventory = player.getInventory();
        if (player.getMainHandItem().getItem() instanceof EnderSeedBundleItem) return inventory.selected;
        if (player.getOffhandItem().getItem() instanceof EnderSeedBundleItem) return 40;
        for (int i = 0; i < 9; i ++) {
            if (inventory.getItem(i).getItem() instanceof EnderSeedBundleItem) {
                return i;
            }
        }
        return -1;
    }

    public static ItemStack getHoldingEnderSeedBundle(Player player) {
        ItemStack mainHandItem = player.getMainHandItem();
        ItemStack offHandItem = player.getOffhandItem();
        if (mainHandItem.getItem() instanceof EnderSeedBundleItem) return mainHandItem;
        if (offHandItem.getItem() instanceof EnderSeedBundleItem) return offHandItem;
        return ItemStack.EMPTY;
    }

    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(SeedPacketBundleClients.INSTANCE);
    }

    private static class SeedPacketBundleClients implements IClientItemExtensions {
        private static final SeedPacketBundleClients INSTANCE = new SeedPacketBundleClients();

        @Override
        public BlockEntityWithoutLevelRenderer getCustomRenderer() {
            return EnderSeedBundleItemRenderer.INASTANCE;
        }
    }
}
