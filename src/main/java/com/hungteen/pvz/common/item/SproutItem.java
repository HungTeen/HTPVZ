package com.hungteen.pvz.common.item;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.block.GardenFlowerPotBlock;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.creatures.Sprout;
import com.hungteen.pvz.common.event.RegisterSproutsEvent;
import com.hungteen.pvz.common.register.PVZDimensions;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZStats;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SproutItem extends Item {
    boolean isMarigold;
    public SproutItem(Properties p_41383_, boolean isMarigold) {
        super(p_41383_);
        this.isMarigold = isMarigold;
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide) {
            if (PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.gardenOnlySprouts)
                    && ! level.dimension().location().equals(PVZDimensions.ZEN_GARDEN)) {
                return super.useOn(context);
            }
            return InteractionResult.SUCCESS;
        }
        if (PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.gardenOnlySprouts)
                && ! level.dimension().location().equals(PVZDimensions.ZEN_GARDEN)) {
            if (context.getPlayer() != null) {
                context.getPlayer().displayClientMessage(Component.translatable("hint.pvz.sprout.must_in_zen_garden"), true);
            }
            return super.useOn(context);
        }
        if (context.getPlayer() instanceof ServerPlayer player) {
            BlockPos pos = context.getClickedPos();
            BlockState blockState = level.getBlockState(pos);
            if (blockState.getBlock() instanceof GardenFlowerPotBlock) {
                if (level.getEntitiesOfClass(LivingEntity.class, new AABB(pos).move(0,
                                blockState.getCollisionShape(level, pos).bounds().maxY, 0),
                                (entity -> ! (entity instanceof Player))).isEmpty()) {
                    ItemStack itemstack = context.getItemInHand();
                    Sprout sprout = (Sprout) PVZEntities.SPROUT.get().spawn((ServerLevel)level, itemstack, context.getPlayer(), pos, MobSpawnType.SPAWN_EGG, true, false);
                    if (sprout != null) {
                        sprout.transformChance = getTransformChance(context.getItemInHand());
                        sprout.setMarigold(this.isMarigold);
                        sprout.getCapability(PVZEntityCapability.CAP).ifPresent((cap) -> cap.setOwner(player));
                        sprout.renewWaterPot();
                        player.awardStat(PVZStats.PLANT_SPROUTS);
                        itemstack.shrink(1);
                        level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, pos);
                    }
                }
            }
        }
        return super.useOn(context);
    }

    public Map<String, Integer> getTransformChance(ItemStack stack) {
        return fromTagToChanceMap((CompoundTag) stack.getOrCreateTag().get("transform_chances"));
    }
    public static Map<String, Integer> fromTagToChanceMap(CompoundTag tag) {
        Map<String, Integer> map = new HashMap<>();
        if (tag == null) {
            return map;
        }
        for (String name: tag.getAllKeys()) {
            map.put(name, tag.getInt(name));
        }
        return map;
    }
    public static CompoundTag fromChanceMapToTag(Map<String, Integer> map) {
        CompoundTag tag = new CompoundTag();
        for (String name : map.keySet()) {
            tag.putInt(name, map.get(name));
        }
        return tag;
    }
    public static ItemStack getTaggedItem(SproutItem item, String name, Map<String, Integer> transformChance) {
        ItemStack itemStack = item.getDefaultInstance();
        itemStack.getOrCreateTag().putString("sprout_type", name);
        itemStack.getOrCreateTag().put("transform_chances", fromChanceMapToTag(transformChance));
        return itemStack;
    }
    public static ItemStack getTaggedItem(ItemStack itemStack, String name, Map<String, Integer> transformChance) {
        itemStack.getOrCreateTag().putString("sprout_type", name);
        itemStack.getOrCreateTag().put("transform_chances", fromChanceMapToTag(transformChance));
        return itemStack;
    }
    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flagIn){
        super.appendHoverText(stack, level, tooltip, flagIn);
        if (stack.getOrCreateTag().contains("sprout_type")) {
            tooltip.add(Component.translatable(stack.getOrCreateTag().getString("sprout_type")).withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        }
    }
    public void fillItemCategory(CreativeModeTab tab, NonNullList<ItemStack> list) {
        if (! this.isMarigold) {
            if (this.allowedIn(tab)) {
                for (String name : RegisterSproutsEvent.sproutsMap.keySet()) {
                    Map<String, Integer> transformChance = RegisterSproutsEvent.sproutsMap.get(name);
                    for (String i : transformChance.keySet()) {
                        if (! ForgeRegistries.ENTITY_TYPES.containsKey(new ResourceLocation(i)) ||
                                ! ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(i)).canSummon()) {
                            PVZMod.LOGGER.error("Trying to fill unsummonable entity " + i + " in sprout item nbt!");
                            transformChance.put(i, 0);
                        }
                    }
                    list.add(getTaggedItem(this, name, transformChance));
                }
            }
        } else {
            super.fillItemCategory(tab, list);
        }
    }
}
