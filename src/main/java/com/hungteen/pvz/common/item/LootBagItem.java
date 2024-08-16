package com.hungteen.pvz.common.item;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.SimpleContainer;
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
import java.util.List;

public class LootBagItem extends Item {
    public LootBagItem(Properties p_41383_) {
        super(p_41383_);
    }

    public static ItemStack modify(ItemStack itemStack, ResourceLocation location, int number) {
        if (itemStack.getItem() instanceof LootBagItem) {
            itemStack.getOrCreateTag().putString("loot_table", location.toString());
            itemStack.getOrCreateTag().putInt("size", number);
        }
        return itemStack;
    }


    public List<ItemStack> getLoot(ItemStack itemStack, ServerLevel level, Entity user, Vec3 position) {
        CompoundTag tag = itemStack.getOrCreateTag();
        int size = tag.contains("size") ? tag.getInt("size") : 1;
        Container container = new SimpleContainer(size);
        LootTable lootTable = tag.contains("loot_table") ?
                level.getServer().getLootTables().get(new ResourceLocation(tag.getString("loot_table"))) : LootTable.EMPTY;
        if (user != null) {
            LootContext.Builder builder = (new LootContext.Builder(level))
                    .withParameter(LootContextParams.ORIGIN, position)
                    .withParameter(LootContextParams.THIS_ENTITY, user);
            lootTable.fill(container, builder.create(LootContextParamSets.SELECTOR));
            //TODO fix bag of reporting "Tried to over-fill a container".
            List<ItemStack> itemStacks = new ArrayList<>();
            for (int i = 0; i < size; i ++) {
                ItemStack loot = container.getItem(i);
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
        if (level instanceof ServerLevel serverLevel) {
            List<ItemStack> itemStacks = this.getLoot(lootBag, serverLevel, player, player.position());
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
