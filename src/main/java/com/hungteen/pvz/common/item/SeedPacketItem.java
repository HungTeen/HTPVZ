package com.hungteen.pvz.common.item;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.events.PVZPlantConditionMatchingEvent;
import com.hungteen.pvz.api.interfaces.IHaveSkills;
import com.hungteen.pvz.api.interfaces.INeedSafeSituation;
import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.client.gui.PVZOverlayHandler;
import com.hungteen.pvz.client.gui.components.SunImageToolTipComponent;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZEnchantments;
import com.hungteen.pvz.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.*;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class SeedPacketItem<T extends Entity> extends Item implements IHaveSkills{
    public static List<SeedPacketItem<?>> seedPacketItemList = new ArrayList<>();

    //entitySupplier is unchangeable. the rest three can be adjusted with command.
    protected final Supplier<EntityType<T>> entitySupplier;
    private final String resource;
    private final int cost;
    private final int coolDown;
    private final List<Skill> skillList;
    private final boolean creativeOnly;

    public SeedPacketItem(Properties p_41383_, Supplier<EntityType<T>> entitySupplier, List<Skill> skillList, String resource, int cost, int coolDown, boolean creativeOnly) {
        super(p_41383_);
        this.entitySupplier = entitySupplier;
        this.skillList = skillList;
        this.resource = resource;
        this.cost = cost;
        this.coolDown = coolDown;
        this.creativeOnly = creativeOnly;
        if (this.getClass() == SeedPacketItem.class) seedPacketItemList.add(this);
    }


    //methods
    public EntityType<T> getEntity(){
        return entitySupplier.get();
    }

    /** This method returns the original cost of the itemStack, not including the effects of enchantments and buffs. To get the accurate number, use {@link PVZResourceEvent.CheckResourceEvent}.
     */
    public int getBaseCost(@Nullable ItemStack itemStack){
        if (itemStack != null && itemStack.getItem() instanceof SeedPacketItem && itemStack.getTag() != null) {
            return itemStack.getTag().contains("Cost") ? itemStack.getTag().getInt("Cost") : cost;
        }
        return cost;
    }

    /** This method returns the original cool down of the itemStack, not including the effects of enchantments and buffs. To get the accurate number, use {@link PVZResourceEvent.CheckResourceEvent}.
     */
    public int getBaseCoolDown(@Nullable ItemStack itemStack){
        if (itemStack != null && itemStack.getItem() instanceof SeedPacketItem && itemStack.getTag() != null) {
            return itemStack.getTag().contains("CoolDown") ? itemStack.getTag().getInt("CoolDown") : coolDown;
        }
        return coolDown;
    }

    /**SeedPacket may not cost sun but other resource instead. */
    public String getResource(@Nullable ItemStack itemStack){
        if (itemStack != null && itemStack.getItem() instanceof SeedPacketItem && itemStack.getTag() != null) {
            return itemStack.getTag().contains("Resource") ? itemStack.getTag().getString("Resource") : resource;
        }
        return resource;
    }

    public static SeedPacketItem getSeedPacket(EntityType<?> entityType) {
        AtomicReference<SeedPacketItem> packetItem = new AtomicReference<>();
        SeedPacketItem.seedPacketItemList.forEach(item -> {
            if (item.getEntity().equals(entityType)) {
                packetItem.set(item);
            }});
        return packetItem.get();
    }


    //definitions
    @Override
    public List<Skill> getStaticSkillList(){
        return skillList;
    }


    public boolean canBoost(){
        return true;
    }

    @Override
    public Component getName(ItemStack itemStack) {
        return Component.translatable("item.pvz.seed_packet", Component.translatable(entitySupplier.get().getDescriptionId()));
    }

    public int getSkillVal(Object obj) {
        if (obj instanceof ItemStack itemStack && itemStack.getItem() instanceof SeedPacketItem && itemStack.getTag() != null){
            CompoundTag tag = itemStack.getTag();
            if (tag.contains("Skill")) {
                return tag.getInt("Skill");
            }
        }
        return 0;
    }

    @Override
    public void setSkillVal(Object obj, int value) {
        if (obj instanceof ItemStack itemStack && itemStack.getItem() instanceof SeedPacketItem) {
            itemStack.getTag().putInt("Skill", value);
        }
    }

    //TODO do not use if main hand interacting result consumes action.
    protected void used(ItemStack itemstack, Player player) {
        itemstack.hurtAndBreak(1, player, (entity1) ->
                player.broadcastBreakEvent(entity1.getItemInHand(InteractionHand.MAIN_HAND) == itemstack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
    }

    /**
     * checks if sun is enough for planting here, and then test situations of planting on fluids.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand handIn) {
        //sun check
        if (level.isClientSide() && getResource(player.getItemInHand(handIn)).equals(PVZPlayerCapNBT.SUN)) {
            PVZResourceEvent.CheckResourceEvent event = Util.checkPlantResourceEvent(player, player.getItemInHand(handIn));
            MinecraftForge.EVENT_BUS.post(event);
            if (event.cost > PVZPlayerCapability.getValue(player, event.resource)) {
                PVZOverlayHandler.notEnoughHint = 1.5F;
            }
            return InteractionResultHolder.fail(player.getItemInHand(handIn));
        }
        //planting. not using Item.useOn() for not supporting planting on fluid.
        BlockHitResult fluidResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        BlockHitResult blockResult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.NONE);
        if (fluidResult.getType() != HitResult.Type.MISS || blockResult.getType() != HitResult.Type.MISS) {
            MutableComponent plantResult;
            if (! level.getBlockState(player.blockPosition().above()).getFluidState().isEmpty()) {
                plantResult = plantOnBlock(player, player.getItemInHand(handIn), level, blockResult.getBlockPos(), blockResult.getDirection()); // on ground
            } else {
                if (blockResult.getBlockPos().distSqr(player.blockPosition()) > fluidResult.getBlockPos().distSqr(player.blockPosition())) {
                    plantResult = plantOnBlock(player, player.getItemInHand(handIn), level, fluidResult.getBlockPos(), null); // in fluid
                } else {
                    plantResult = plantOnBlock(player, player.getItemInHand(handIn), level, blockResult.getBlockPos(), blockResult.getDirection()); // on ground
                }
            }
            if (plantResult == null) {
                used(player.getItemInHand(handIn), player);
            } else {
                if (! player.getCooldowns().isOnCooldown(this)) {
                    player.getCooldowns().addCooldown(this, 1);//to prevent bug of also planting on floor behind entity while clicking it.
                }
                //display massage when not in a proper place.
                player.displayClientMessage(plantResult, true);
            }
            return InteractionResultHolder.consume(player.getItemInHand(handIn));
        }
        return super.use(level, player, handIn);
    }

    public MutableComponent plantOnBlock(Player player, ItemStack itemStack, Level level, BlockPos pos, Direction direction) {
        if (itemStack.getItem() instanceof SeedPacketItem<?> item && !player.getCooldowns().isOnCooldown(this)) {
            //check entity.
            Entity entity = getEntity().create((ServerLevel) level, null,
                    itemStack.hasCustomHoverName() ? getName(itemStack) : null,
                    player, pos.above(), MobSpawnType.SPAWN_EGG, false, false);
            if (entity != null && itemStack.hasCustomHoverName()) {
                entity.setCustomName(itemStack.getHoverName());
            }
            PVZOwnedCapability cap = entity.getCapability(PVZOwnedCapability.CAP).orElse(null);
            if (cap != null) {
                cap.setOwner(player);
            }
            //enchantment.
            if (entity instanceof IPlant && (EnchantmentHelper.getTagEnchantmentLevel(PVZEnchantments.SOILLESS_CULTURE.get(), itemStack) > 0 ||
                    itemStack.getOrCreateTag().contains("CanPlaceOn"))) {
                entity.getEntityData().set(((IPlant) entity).root(), false);
            }
            //handle skills.
            if (canBoost() && entity instanceof IHaveSkills) {
                ((IHaveSkills) entity).setSkillVal(entity, getSkillVal(itemStack));
            }
            //check sun and position.
            PVZResourceEvent.CheckPlantConditionEvent event = Util.checkPlantConditionEvent(player, itemStack, entity);
            MinecraftForge.EVENT_BUS.post(event);

            MutableComponent posCheck = entity instanceof INeedSafeSituation ?
                    ((INeedSafeSituation) entity).isPositionSafe(event, entity.level, pos, direction, true) : null;

            if (posCheck == null) {
                //plant.
                PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> nbt.addValue(event.resource, - event.cost));
                if (event.coolDown > 0) {
                    SeedPacketItem.seedPacketItemList.forEach(item1 -> {
                        if (item1.getEntity().equals(item.entitySupplier.get())) {
                            player.getCooldowns().addCooldown(item1, event.coolDown);
                        }
                    });
                }
                ((ServerLevel) level).addFreshEntityWithPassengers(entity);
                if (cap != null) {
                    cap.cost = event.cost;
                    cap.resource = event.resource;
                }
                return null;
            }
            //display massage when not on a proper place.
            player.displayClientMessage(posCheck, true);
            if (entity != null) {
                entity.discard();
                return posCheck;
            }
        }
        return Component.translatable("");
    }

    /**Situation of interacting with mobs.*/

    @SubscribeEvent
    public static void interactEntity(PlayerInteractEvent.EntityInteract ev) {
        Level level = ev.getLevel();
        ItemStack itemStack = ev.getEntity().getItemInHand(ev.getHand());
        if (ev.getEntity().getItemInHand(ev.getHand()).getItem() instanceof SeedPacketItem<?> item) {
            MutableComponent plantResult = item.plantOnEntity(ev.getEntity(), itemStack, level, ev.getTarget());
            if (plantResult == null) {
                item.used(itemStack, ev.getEntity());
                ev.setCancellationResult(InteractionResult.CONSUME);
                ev.setCanceled(true);
            } else {
                if (! ev.getEntity().getCooldowns().isOnCooldown(item)) {
                    ev.getEntity().getCooldowns().addCooldown(item, 1);//to prevent bug of also planting on floor behind entity while clicking it.
                }
                ev.setCancellationResult(InteractionResult.SUCCESS);
                //display massage when not in a proper place.
                ev.getEntity().displayClientMessage(plantResult, true);
            }
        }
    }

    public MutableComponent plantOnEntity(Player player, ItemStack itemStack, Level level, Entity target) {
        if (! level.isClientSide && itemStack.getItem() instanceof SeedPacketItem<?> item && !player.getCooldowns().isOnCooldown(item)) {
            //check entity.
            Entity entity = item.getEntity().create((ServerLevel) level, null,
                    itemStack.hasCustomHoverName() ? item.getName(itemStack) : null,
                    player, target.blockPosition(), MobSpawnType.SPAWN_EGG, false, false);
            if (entity != null && itemStack.hasCustomHoverName()) {
                entity.setCustomName(itemStack.getHoverName());
            }
            PVZOwnedCapability cap = entity.getCapability(PVZOwnedCapability.CAP).orElse(null);
            if (cap != null) {
                cap.setOwner(player);
            }
            //enchantment.
            if (entity instanceof IPlant && (EnchantmentHelper.getTagEnchantmentLevel(PVZEnchantments.SOILLESS_CULTURE.get(), itemStack) > 0 ||
                    itemStack.getOrCreateTag().contains("CanPlaceOn"))) {
                entity.getEntityData().set(((IPlant) entity).root(), false);
            }
            //handle skills.
            if (item.canBoost() && entity instanceof IHaveSkills) {
                ((IHaveSkills) entity).setSkillVal(entity, item.getSkillVal(itemStack));
            }
            //check sun and position.
            PVZResourceEvent.CheckPlantConditionEvent event = Util.checkPlantConditionEvent(player, itemStack, entity);
            MinecraftForge.EVENT_BUS.post(event);

            MutableComponent targetCheck = entity instanceof INeedSafeSituation ?
                    ((INeedSafeSituation) entity).isVehicleSafe(event, target, true) : null;

            if (targetCheck == null) {
                //plant.
                PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> nbt.addValue(event.resource, -event.cost));
                if (event.coolDown > 0) {
                    SeedPacketItem.seedPacketItemList.forEach(itemToCD -> {
                        if (itemToCD.getEntity().equals(item.entitySupplier.get())) {
                            player.getCooldowns().addCooldown(itemToCD, event.coolDown);
                        }
                    });
                }
                ((ServerLevel) level).addFreshEntityWithPassengers(entity);
                if (cap != null) {
                    cap.setOwner(player);
                    cap.cost = event.cost;
                    cap.resource = event.resource;
                }
                item.used(itemStack, player);
                return null;
            } else {
                if (entity != null) {
                    entity.discard();
                    return targetCheck;
                }
            }
        }
        return Component.translatable("");
    }

    @SubscribeEvent
    public static void HandlePlantConditions(PVZResourceEvent.CheckResourceEvent ev) {
        if (! (ev.seedPacket.getItem() instanceof SeedPacketItem<?> item) || item.canBoost()) {
            SeedPacketItem<?> item = ((SeedPacketItem<?>) ev.seedPacket.getItem());
            for (int i : item.getSkills(ev.seedPacket)) {
                if (item.getStaticSkillList().size() - 1 >= i) {
                    if (PVZPlayerCapability.getValue(ev.getEntity(), "plant_have_cost") > 0) {
                        ev.cost += item.getStaticSkillList().get(i).addCostResource;
                    }
                    if (PVZPlayerCapability.getValue(ev.getEntity(), "plant_have_cd") > 0) {
                        ev.coolDown += item.getStaticSkillList().get(i).addCoolDown;
                    }
                }
            }
            if (ev instanceof PVZResourceEvent.CheckPlantConditionEvent e) {
                int level = EnchantmentHelper.getTagEnchantmentLevel(PVZEnchantments.QUICK_COOL_DOWN.get(), e.seedPacket);
                if (level > 0) {
                    e.coolDown = e.coolDown * (10 - level) / 10;
                }
            }
        }
    }

    @Override
    public int getEnchantmentValue(ItemStack stack){
        return 10;
    }
    @Override
    public boolean isValidRepairItem(ItemStack itemToFix, ItemStack material) {
        return ((itemToFix.getItem() instanceof SeedPacketItem<?> && material.getItem() instanceof SeedItem<?>) &&
                ((SeedPacketItem<?>) itemToFix.getItem()).entitySupplier.get().equals(((SeedItem<?>) material.getItem()).entitySupplier.get()))
                || super.isValidRepairItem(itemToFix, material);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flagIn){
        super.appendHoverText(stack, level, tooltip, flagIn);
        for (int i : getSkills(stack)) {
            if (getStaticSkillList().size() - 1 >= i) {
                tooltip.add(Component.translatable(getStaticSkillList().get(i).name).withStyle(ChatFormatting.DARK_AQUA));
            }
        }
        if (ClientProxy.getPlayer() != null && creativeOnly && ClientProxy.getPlayer().isCreative()) {
            tooltip.add(Component.translatable("hint.pvz.creative_only").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
        Player player = !(ClientProxy.MC.getCameraEntity() instanceof Player) ? null : ClientProxy.getPlayer();
        if (! player.isCreative() && ! player.isSpectator()) {
            PVZResourceEvent.CheckResourceEvent event = Util.checkPlantResourceEvent(player, itemStack);
            MinecraftForge.EVENT_BUS.post(event);
            return Optional.of(new SunImageToolTipComponent(event.cost, event.coolDown, getResource(itemStack).equals(PVZPlayerCapNBT.SUN), false, true));
        }
        return Optional.empty();
    }

}
