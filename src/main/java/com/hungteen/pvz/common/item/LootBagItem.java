package com.hungteen.pvz.common.item;

import com.hungteen.pvz.common.register.PVZSoundEvents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;

public class LootBagItem extends Item {
    public LootBagItem(Properties p_41383_) {
        super(p_41383_);
    }

    public static ItemStack lootBag(ItemStack itemStack, ResourceLocation location, int size) {
        if (itemStack.getItem() instanceof LootBagItem) {
            itemStack.getOrCreateTag().putString("loot_table", location.toString());
            itemStack.getOrCreateTag().putInt("size", size);
        }
        return itemStack;
    }


    public List<ItemStack> getLoots(ItemStack itemStack, ServerLevel level, Entity user, Vec3 position) {
        CompoundTag tag = itemStack.getOrCreateTag();
        int size = tag.contains("size") ? tag.getInt("size") : 1;
        LootTable lootTable = tag.contains("loot_table") ?
                level.getServer().getLootTables().get(new ResourceLocation(tag.getString("loot_table"))) : LootTable.EMPTY;
        if (user != null) {
            LootContext.Builder builder = (new LootContext.Builder(level))
                    .withParameter(LootContextParams.ORIGIN, position)
                    .withParameter(LootContextParams.THIS_ENTITY, user);
            LootContext context = builder.create(LootContextParamSets.SELECTOR);
            List<ItemStack> itemStacks = new ArrayList<>();
            Iterator<ItemStack> tmpLoots = new HashSet<>(lootTable.getRandomItems(context)).iterator();
            if (! tmpLoots.hasNext()) {
                return itemStacks;
            }
            while (itemStacks.size() < size) {
                if (! tmpLoots.hasNext()) {
                    tmpLoots =  new HashSet<>(lootTable.getRandomItems(context)).iterator();
                    if (! tmpLoots.hasNext()) {
                        break;
                    }
                }
                ItemStack loot = tmpLoots.next();
                if (! loot.isEmpty()) {
                    itemStacks.add(loot);
                }
            }
            return itemStacks;
        }
        return List.of();
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack lootBag = player.getItemInHand(hand);
        player.awardStat(Stats.ITEM_USED.get(lootBag.getItem()));
        player.playSound(PVZSoundEvents.LOOT_BAG_USE.get());
        if (level instanceof ServerLevel serverLevel) {
            List<ItemStack> itemStacks = this.getLoots(lootBag, serverLevel, player, player.position());
            if (itemStacks.isEmpty()) {
                player.displayClientMessage(Component.translatable("hint.pvz.loot_bag_empty"), true);
            } else {
                itemStacks.forEach(itemStack -> player.drop(itemStack, false));
                lootBag.shrink(1);
            }
            return InteractionResultHolder.success(lootBag);
        }
        return InteractionResultHolder.consume(lootBag);
    }
}
