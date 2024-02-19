package com.hungteen.pvz.common.item;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.IHaveSkills;
import com.hungteen.pvz.api.interfaces.INeedSafeSituation;
import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.client.gui.PVZOverlayHandler;
import com.hungteen.pvz.client.gui.components.SunImageToolTipComponent;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.event.PVZResourceEvent;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
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
import net.minecraftforge.fluids.IFluidBlock;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class SeedPacketItem<T extends Entity> extends Item implements IHaveSkills{

    //entitySupplier is unchangeable. the rest three can be adjusted with command.
    public static List<SeedPacketItem<?>> seedPacketItemList = new ArrayList<>();
    protected final Supplier<EntityType<T>> entitySupplier;
    private final String resource;
    private final int cost;
    private final int coolDown;
    private final List<Skill> skillList;
    private boolean creativeOnly;

    public SeedPacketItem(Properties p_41383_, Supplier<EntityType<T>> entitySupplier, List<Skill> skillList, String resource, int cost, int coolDown, boolean creativeOnly) {
        super(p_41383_);
        this.entitySupplier = entitySupplier;
        this.skillList = skillList;
        this.resource = resource;
        this.cost = cost;
        this.coolDown = coolDown;
        this.creativeOnly = creativeOnly;
        seedPacketItemList.add(this);
    }

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

    @Override
    public List<Skill> getStaticSkillList(){
        return skillList;
    }

    @Override
    public Component getName(ItemStack itemStack) {
        return Component.translatable("item.pvz.seed_packet", Component.translatable(entitySupplier.get().getDescriptionId()));
    }

    public boolean canBoost(){
        return true;
    }

    protected void used(ItemStack itemstack, Player player, InteractionHand hand) {
        itemstack.hurtAndBreak(1, player, (entity1) -> entity1.broadcastBreakEvent(hand));
    }

    /**
     * checks if sun is enough for planting here, and then test situations of planting on fluids.<br>
     * see {@link SeedPacketItem#useOn(UseOnContext)} for planting a plant on non-fluid blocks.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand handIn) {
        //sun check
        if (level.isClientSide() && getResource(player.getItemInHand(handIn)).equals(PVZPlayerCapNBT.SUN)) {
            PVZResourceEvent.CheckResourceEvent event = new PVZResourceEvent.CheckResourceEvent(player, player.getItemInHand(handIn));
            MinecraftForge.EVENT_BUS.post(event);
            if (event.cost > PVZPlayerCapability.getValue(player, event.resource)) {
                PVZOverlayHandler.notEnoughHint = 3;
            }
            return InteractionResultHolder.fail(player.getItemInHand(handIn));
        }
        //planting.
        //reason of not using Item.useOn() for not supporting planting on fluid.
        BlockHitResult result = getPlayerPOVHitResult(level, player, ClipContext.Fluid.SOURCE_ONLY);
        if (result.getType() != HitResult.Type.MISS) {
            BlockPos pos = result.getBlockPos();
            if (! (level.getBlockState(pos).getBlock() instanceof IFluidBlock)) {
                pos = pos.offset(result.getDirection().getNormal()).below();
            }
            ItemStack itemStack = player.getItemInHand(handIn);
            player.getCooldowns().addCooldown(itemStack.getItem(), 1);//to prevent bug of also planting on floor behind entity while clicking it.
            //check entity.
            Entity entity = getEntity().create((ServerLevel) level, null,
                    itemStack.hasCustomHoverName() ? getName(itemStack) : null,
                    player, pos, MobSpawnType.SPAWN_EGG, false, false);
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
            //check position.
            MutableComponent posCheck = entity instanceof INeedSafeSituation ? ((INeedSafeSituation) entity).isPositionSafe(entity.level, pos, true) : null;
            if (posCheck == null) {
                PVZResourceEvent.CheckPlantConditionEvent event = new PVZResourceEvent.CheckPlantConditionEvent(player, itemStack, entity);
                MinecraftForge.EVENT_BUS.post(event);
                //check sun.
                if (event.cost <= PVZPlayerCapability.getValue(player, event.resource)) {
                    //plant.
                    PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> nbt.addValue(event.resource, -event.cost));
                    if (event.coolDown > 0) {
                        SeedPacketItem.seedPacketItemList.forEach(item -> {
                            if (item.getEntity().equals(((SeedPacketItem<?>) itemStack.getItem()).entitySupplier.get())) {
                                player.getCooldowns().addCooldown(item, event.coolDown);
                            }
                        });
                    }
                    ((ServerLevel) level).addFreshEntityWithPassengers(entity);
                    if (cap != null) {
                        cap.cost = event.cost;
                        cap.resource = event.resource;
                    }
                    used(itemStack, player, handIn);
                    return InteractionResultHolder.consume(itemStack);
                }
                //display massage when not have enough resource.
                player.displayClientMessage(Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource)), true);
                entity.discard();
                return InteractionResultHolder.fail(itemStack);
            }
            //display massage when not on a proper place.
            player.displayClientMessage(posCheck, true);
            if (entity != null) {
                entity.discard();
                return InteractionResultHolder.fail(itemStack);
            }
        }
        return super.use(level, player, handIn);
    }

    /**Situation of interacting with mobs.*/
    @SubscribeEvent
    public static void useOnMob(PlayerInteractEvent.EntityInteract ev) {
        Level level = ev.getLevel();
        if (!level.isClientSide()) {
            ItemStack itemStack = ev.getItemStack();
            Player player = ev.getEntity();
            if (itemStack.getItem() instanceof SeedPacketItem<?> item && !player.getCooldowns().isOnCooldown(item)) {
                player.getCooldowns().addCooldown(item, 1);//to prevent bug of also planting on floor behind entity while clicking it.
                Entity target = ev.getTarget();
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
                //check position.
                MutableComponent targetCheck = entity instanceof INeedSafeSituation ? ((INeedSafeSituation) entity).isVehicleSafe(target, true) : null;
                if (targetCheck == null) {
                    PVZResourceEvent.CheckPlantConditionEvent event = new PVZResourceEvent.CheckPlantConditionEvent(player, itemStack, entity);
                    MinecraftForge.EVENT_BUS.post(event);
                    //check sun.
                    if (event.cost <= PVZPlayerCapability.getValue(player, event.resource)) {
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
                        item.used(itemStack, player, ev.getHand());
                    } else {
                        //display massage when not have enough resource.
                        player.displayClientMessage(Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource)), true);
                        entity.discard();
                    }
                } else {
                    //display massage when not in a proper place.
                    player.displayClientMessage(targetCheck, true);
                    if (entity != null) {
                        entity.discard();
                    }
                }
            }
        }
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
        if (creativeOnly && ClientProxy.getPlayer().isCreative()) {
            tooltip.add(Component.translatable("hint.pvz.creative_only").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
        Player player = !(ClientProxy.MC.getCameraEntity() instanceof Player) ? null : ClientProxy.getPlayer();
        if (! player.isCreative() && ! player.isSpectator()) {
            PVZResourceEvent.CheckResourceEvent event = new PVZResourceEvent.CheckResourceEvent(player, itemStack);
            MinecraftForge.EVENT_BUS.post(event);
            return Optional.of(new SunImageToolTipComponent(event.cost, event.coolDown, Objects.equals(getResource(itemStack), PVZPlayerCapNBT.SUN), false, true));
        }
        return Optional.empty();
    }

}
