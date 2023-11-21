package com.hungteen.pvz.common.item;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.IHaveSkills;
import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.client.gui.PVZOverlayHandler;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.api.interfaces.INeedSafeSituation;
import com.hungteen.pvz.common.event.PVZResourceEvent;
import com.hungteen.pvz.common.register.PVZEnchantments;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class SeedPacketItem<T extends Entity> extends Item implements IHaveSkills{

    //entitySupplier is unchangeable. the rest three can be adjusted with command.
    public static List<SeedPacketItem<?>> seedPacketItemList = new ArrayList<>();
    protected final Supplier<EntityType<T>> entitySupplier;
    private final String resource;
    private final int cost;
    private final int coolDown;
    private List<Skill> skillList;

    public SeedPacketItem(Properties p_41383_, Supplier<EntityType<T>> entitySupplier, String resource, int cost, int coolDown) {
        super(p_41383_);
        this.entitySupplier = entitySupplier;
        this.resource = resource;
        this.cost = cost;
        this.coolDown = coolDown;
        seedPacketItemList.add(this);
    }

    public EntityType<T> getEntity(){
        return entitySupplier.get();
    }

    /** This method returns the original cost of the itemStack, not including the effects of enchantments and buffs. To get the accurate number, use {@link PVZResourceEvent.CheckResourceEvent}.
     */
    public int getCost(@Nullable ItemStack itemStack){
        if (itemStack != null && itemStack.getItem() instanceof SeedPacketItem && itemStack.getTag() != null) {
            return itemStack.getTag().contains("Cost") ? itemStack.getTag().getInt("Cost") : cost;
        }
        return cost;
    }

    /** This method returns the original cool down of the itemStack, not including the effects of enchantments and buffs. To get the accurate number, use {@link com.hungteen.pvz.common.event.PVZResourceEvent.CheckPlantConditionEvent}.
     */
    public int getCoolDown(@Nullable ItemStack itemStack){
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

    public void updateSkillList(Entity entity) {
        this.skillList = entity instanceof IHaveSkills e ? e.getStaticSkillList() : new ArrayList<>();
    }

    @Override
    public Component getName(ItemStack itemStack) {
        return Component.translatable("item.pvz.seed_packet", Component.translatable(entitySupplier.get().getDescriptionId()));
    }

    public boolean canBoost(){
        return true;
    }

    /**
     * Only checks if sun is enough for planting here.
     * check {@link SeedPacketItem#useOn(UseOnContext)} for planting a plant.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand handIn) {
        if (level.isClientSide() && getResource(player.getItemInHand(handIn)).equals(PVZPlayerCapNBT.SUN)) {
            PVZResourceEvent.CheckResourceEvent event = new PVZResourceEvent.CheckResourceEvent(player, player.getItemInHand(handIn));
            MinecraftForge.EVENT_BUS.post(event);
            if (event.cost > PVZPlayerCapability.getValue(player, event.resource)) {
                PVZOverlayHandler.notEnoughHint = 3;
            }
            return InteractionResultHolder.success(player.getItemInHand(handIn));
        }
        return super.use(level, player, handIn);
    }

    protected void used(ItemStack itemstack, Player player, InteractionHand hand) {
        itemstack.hurtAndBreak(1, player, (entity1) -> {
            entity1.broadcastBreakEvent(hand);
        });
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        Level level = context.getLevel();
        if (player != null && !level.isClientSide()) {
            //handle position.
            BlockPos pos = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY).getBlockPos();
            if (pos.equals(context.getClickedPos())) {
                pos = pos.offset(context.getClickedFace().getNormal());
            }
            //check entity.
            Entity entity = getEntity().create((ServerLevel) level, null,
                    context.getItemInHand().hasCustomHoverName() ? getName(context.getItemInHand()) : null,
                    player, pos, MobSpawnType.SPAWN_EGG, false, false);
            if (entity != null && context.getItemInHand().hasCustomHoverName()) {
                entity.setCustomName(context.getItemInHand().getHoverName());
            }
            //check pos.
            if (entity instanceof IPlant && (EnchantmentHelper.getTagEnchantmentLevel(PVZEnchantments.SOILLESS_CULTURE.get(), context.getItemInHand()) > 0 ||
                    context.getItemInHand().getOrCreateTag().contains("CanPlaceOn"))) {
                entity.getEntityData().set(((IPlant) entity).root(), false);
            }
            //handle skills.
            if (canBoost() && entity instanceof IHaveSkills) {
                ((IHaveSkills) entity).setSkillVal(entity, getSkillVal(context.getItemInHand()));
            }
            MutableComponent posCheck = entity instanceof INeedSafeSituation ? ((INeedSafeSituation) entity).isPositionSafe(entity.level, pos.below()) : null;
            if (entity != null && posCheck == null) {
                PVZResourceEvent.CheckPlantConditionEvent event = new PVZResourceEvent.CheckPlantConditionEvent(player, context.getItemInHand(), entity);
                MinecraftForge.EVENT_BUS.post(event);
                //check sun.
                if (event.cost <= PVZPlayerCapability.getValue(player, event.resource)) {
                    //plant.
                    PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> nbt.addValue(event.resource, -event.cost));
                    if (event.coolDown > 0) {
                        SeedPacketItem.seedPacketItemList.forEach(item -> {
                            if (item.getEntity().equals(((SeedPacketItem<?>) context.getItemInHand().getItem()).entitySupplier.get())) {
                                player.getCooldowns().addCooldown(item, event.coolDown);
                            }
                        });
                    }
                    entity.moveTo(
                            pos.getX() + 0.5,
                            pos.below().getY() + (level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty() ? 0 :
                                    level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).bounds().maxY),
                            pos.getZ() + 0.5);
                    ((ServerLevel) level).addFreshEntityWithPassengers(entity);
                    PVZOwnedCapability cap = entity.getCapability(PVZOwnedCapability.CAP).orElse(null);
                    if (cap != null) {
                        cap.setOwner(player);
                        cap.cost = event.cost;
                        cap.resource = event.resource;
                    }
                    //TODO add particles.
                    used(context.getItemInHand(), player, context.getHand());
                    return InteractionResult.SUCCESS;
                }
                //display massage when not have enough resource.
                player.displayClientMessage(Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource)), true);
                entity.discard();
                return InteractionResult.FAIL;
            }
            //display massage when not on a proper place.
            player.displayClientMessage(posCheck, true);
            if (entity != null) {
                entity.discard();
            }
            return InteractionResult.FAIL;
        }
        return super.useOn(context);
    }
    @SubscribeEvent
    public static void useOnMob(PlayerInteractEvent.EntityInteract ev) {
        Level level = ev.getLevel();
        if (!level.isClientSide()) {
            ItemStack itemStack = ev.getItemStack();
            Player player = ev.getEntity();
            if (itemStack.getItem() instanceof SeedPacketItem<?> item && !player.getCooldowns().isOnCooldown(item)) {
                Entity target = ev.getTarget();
                //check entity.
                Entity entity = item.getEntity().create((ServerLevel) level, null,
                        itemStack.hasCustomHoverName() ? item.getName(itemStack) : null,
                        player, target.blockPosition(), MobSpawnType.SPAWN_EGG, false, false);
                if (entity != null && itemStack.hasCustomHoverName()) {
                    entity.setCustomName(itemStack.getHoverName());
                }
                //check pos.
                if (entity instanceof IPlant && (EnchantmentHelper.getTagEnchantmentLevel(PVZEnchantments.SOILLESS_CULTURE.get(), itemStack) > 0 ||
                        itemStack.getOrCreateTag().contains("CanPlaceOn"))) {
                    entity.getEntityData().set(((IPlant) entity).root(), false);
                }
                //handle skills.
                if (item.canBoost() && entity instanceof IHaveSkills) {
                    ((IHaveSkills) entity).setSkillVal(entity, item.getSkillVal(itemStack));
                }
                MutableComponent targetCheck = entity instanceof INeedSafeSituation ? ((INeedSafeSituation) entity).isVehicleSafe(target) : null;
                if (entity != null && targetCheck == null) {
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
                        entity.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), 0.0F);
                        entity.startRiding(target);//TODO let plant decide this.
                        PVZOwnedCapability cap = entity.getCapability(PVZOwnedCapability.CAP).orElse(null);
                        if (cap != null) {
                            cap.setOwner(player);
                            cap.cost = event.cost;
                            cap.resource = event.resource;
                        }
                        //TODO add particles.
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
            if (ev.getEntity().level.isClientSide() && PVZPlayerCapability.getValue(ev.getEntity(), "plant_have_cost") > 0) {
                Entity entity = ((SeedPacketItem<?>) ev.seedPacket.getItem()).entitySupplier.get().create(ev.getEntity().level);
                if (entity != null) {
                    if (((SeedPacketItem<?>) ev.seedPacket.getItem()).getSkillVal(ev.seedPacket) > 0) {
                        if (entity instanceof IHaveSkills e) {
                            for (int i : e.getSkills(e)) {
                                ev.cost += e.getStaticSkillList().get(i).addCostResource;
                            }
                        }
                    }
                    entity.discard();
                }
            } else if (ev instanceof PVZResourceEvent.CheckPlantConditionEvent e) {
                int level = EnchantmentHelper.getTagEnchantmentLevel(PVZEnchantments.QUICK_COOL_DOWN.get(), e.seedPacket);
                if (level > 0) {
                    e.coolDown = e.coolDown * (10 - level) / 10;
                }
                if (PVZPlayerCapability.getValue(e.getEntity(), "plant_have_cost") > 0 &&
                        e.spawningEntity instanceof IHaveSkills entity){
                    for (int i : entity.getSkills(e.spawningEntity)) {
                        e.cost += entity.getStaticSkillList().get(i).addCostResource;
                        e.coolDown += entity.getStaticSkillList().get(i).addCoolDown;
                    }
                }
            }
        }
    }

    @Override
    public int getEnchantmentValue(ItemStack stack){
        return 15;
    }
    @Override
    public boolean isValidRepairItem(ItemStack itemToFix, ItemStack material) {
        return ((itemToFix.getItem() instanceof SeedPacketItem<?> && material.getItem() instanceof SeedItem<?>) &&
                ((SeedPacketItem<?>) itemToFix.getItem()).entitySupplier.get().equals(((SeedItem<?>) material.getItem()).entitySupplier.get()))
                || super.isValidRepairItem(itemToFix, material);
    }

    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flagIn){
        super.appendHoverText(stack, level, tooltip, flagIn);
        if (getStaticSkillList() == null) {
            updateSkillList(getEntity().create(level));
        }
        for (int i : getSkills(stack)) {
            if (getStaticSkillList().size() - 1 >= i) {
                tooltip.add(Component.translatable(getStaticSkillList().get(i).name).withStyle(ChatFormatting.DARK_AQUA));
            }
        }
    }

}
