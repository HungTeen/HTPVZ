package com.hungteen.pvz.common.item;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.interfaces.IHaveSkills;
import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.client.gui.PVZOverlayHandler;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.api.interfaces.INeedSafeSituation;
import com.hungteen.pvz.common.event.PVZResourceEvent;
import com.hungteen.pvz.common.register.PVZEnchantments;
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
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class SeedPacketItem<T extends Entity> extends Item {

    //entitySupplier is unchangeable. the rest three can be adjusted with command.
    private final Supplier<EntityType<T>> entitySupplier;
    private final String resource;
    private final int cost;
    private final int coolDown;

    public SeedPacketItem(Properties p_41383_, Supplier<EntityType<T>> entitySupplier, String resource, int cost, int coolDown) {
        super(p_41383_.stacksTo(1).defaultDurability(300));
        this.entitySupplier = entitySupplier;
        this.resource = resource;
        this.cost = cost;
        this.coolDown = coolDown;
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

    public String getResource(@Nullable ItemStack itemStack){
        if (itemStack != null && itemStack.getItem() instanceof SeedPacketItem && itemStack.getTag() != null) {
            return itemStack.getTag().contains("Resource") ? itemStack.getTag().getString("Resource") : resource;
        }
        return resource;
    }

    public static int getSkill(ItemStack itemStack) {
        if (itemStack != null && itemStack.getItem() instanceof SeedPacketItem && itemStack.getTag() != null){
            CompoundTag tag = itemStack.getTag();
            if (tag.contains("Skill")) {
                return tag.getInt("Skill");
            }
        }
        return -1;
    }

    //TODO add a cooling down hint.

    @Override
    public Component getName(ItemStack itemStack) {
        return Component.translatable("item.pvz.seed_packet", Component.translatable(entitySupplier.get().getDescriptionId()));
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
            if (entity instanceof IPlant && EnchantmentHelper.getTagEnchantmentLevel(PVZEnchantments.SOILLESS_CULTURE.get(), context.getItemInHand()) > 0) {
                entity.getEntityData().set(((IPlant) entity).root(), false);
            }
            if (entity instanceof IHaveSkills) {
                ((IHaveSkills) entity).setSkill(getSkill(context.getItemInHand()));
            }
            MutableComponent posCheck = entity instanceof INeedSafeSituation ? ((INeedSafeSituation) entity).isPositionSafe(entity.level, pos.below()) : null;
            if (entity != null && posCheck == null) {
                PVZResourceEvent.CheckPlantConditionEvent event = new PVZResourceEvent.CheckPlantConditionEvent(player, context.getItemInHand(), entity);
                MinecraftForge.EVENT_BUS.post(event);
                //check sun.
                if (event.cost <= PVZPlayerCapability.getValue(player, event.resource)) {
                    PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> nbt.addValue(event.resource, -event.cost));
                    player.getCooldowns().addCooldown(this, event.coolDown);
                    entity.moveTo(
                            pos.getX() + 0.5,
                            pos.below().getY() + (level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).isEmpty() ? 0 : level.getBlockState(pos.below()).getCollisionShape(level, pos.below()).bounds().maxY),
                            pos.getZ() + 0.5);
                    ((ServerLevel) level).addFreshEntityWithPassengers(entity);
                    PVZOwnedCapability cap = PVZOwnedCapability.getCap(entity);
                    cap.setOwner(player);
                    cap.cost = event.cost;
                    cap.resource = event.resource;
                    //TODO add particles.
                    context.getItemInHand().hurtAndBreak(1, player, (entity1) -> {
                        entity1.broadcastBreakEvent(context.getHand());
                    });
                    return InteractionResult.SUCCESS;
                }
                player.displayClientMessage(Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource)), true);
                entity.discard();
                return InteractionResult.FAIL;
            }
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
            if (itemStack.getItem() instanceof SeedPacketItem<?> && !player.getCooldowns().isOnCooldown(itemStack.getItem())) {
                Entity target = ev.getTarget();
                Entity entity = ((SeedPacketItem<?>) itemStack.getItem()).getEntity().create((ServerLevel) level, null,
                        itemStack.hasCustomHoverName() ? ((SeedPacketItem<?>) itemStack.getItem()).getName(itemStack) : null,
                        player, target.blockPosition(), MobSpawnType.SPAWN_EGG, false, false);
                if (entity != null && itemStack.hasCustomHoverName()) {
                    entity.setCustomName(itemStack.getHoverName());
                }
                if (entity instanceof IPlant && EnchantmentHelper.getTagEnchantmentLevel(PVZEnchantments.SOILLESS_CULTURE.get(), itemStack) > 0) {
                    entity.getEntityData().set(((IPlant) entity).root(), false);
                }
                if (entity instanceof IHaveSkills) {
                    ((IHaveSkills) entity).setSkill(getSkill(itemStack));
                }
                MutableComponent targetCheck = entity instanceof INeedSafeSituation ? ((INeedSafeSituation) entity).isVehicleSafe(target) : null;
                if (entity != null && targetCheck == null) {
                    PVZResourceEvent.CheckPlantConditionEvent event = new PVZResourceEvent.CheckPlantConditionEvent(player, itemStack, entity);
                    MinecraftForge.EVENT_BUS.post(event);
                    if (event.cost <= PVZPlayerCapability.getValue(player, event.resource)) {
                        PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> nbt.addValue(event.resource, -event.cost));
                        player.getCooldowns().addCooldown(itemStack.getItem(), event.coolDown);
                        ((ServerLevel) level).addFreshEntityWithPassengers(entity);
                        entity.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), 0.0F);
                        entity.startRiding(target);//TODO let plant decide this.
                        PVZOwnedCapability cap = PVZOwnedCapability.getCap(entity);
                        cap.setOwner(player);
                        cap.cost = event.cost;
                        cap.resource = event.resource;
                        //TODO add particles.
                        itemStack.hurtAndBreak(1, player, (entity1) -> {
                            entity1.broadcastBreakEvent(ev.getHand());
                        });
                    } else {
                        player.displayClientMessage(Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource)), true);
                        entity.discard();
                    }
                } else {
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
        if (ev.getEntity().level.isClientSide()) {
            Entity entity = ((SeedPacketItem<?>) ev.seedPacket.getItem()).entitySupplier.get().create(ev.getEntity().level);
            if (entity != null) {
                if (ev.resource.equals(PVZPlayerCapNBT.SUN) && SeedPacketItem.getSkill(ev.seedPacket) >= 0) {
                    if (entity instanceof IHaveSkills) {
                        ev.cost += ((IHaveSkills) entity).getStaticSkillList().get(SeedPacketItem.getSkill(ev.seedPacket)).addCostSun;
                    }
                }
                entity.discard();
            }
        } else if (ev instanceof PVZResourceEvent.CheckPlantConditionEvent) {
            int level = EnchantmentHelper.getTagEnchantmentLevel(PVZEnchantments.QUICK_COOL_DOWN.get(), ev.seedPacket);
            if (level > 0) {
                ((PVZResourceEvent.CheckPlantConditionEvent) ev).coolDown = ((PVZResourceEvent.CheckPlantConditionEvent) ev).coolDown * (10 - level) / 10;
            }
            if (((PVZResourceEvent.CheckPlantConditionEvent) ev).spawningEntity instanceof IHaveSkills){
                ev.cost += ((IHaveSkills) ((PVZResourceEvent.CheckPlantConditionEvent) ev).spawningEntity)
                        .getSkill() >= 0 ?
                        ((IHaveSkills) ((PVZResourceEvent.CheckPlantConditionEvent) ev).spawningEntity)
                        .getStaticSkillList().get(SeedPacketItem.getSkill(ev.seedPacket)).addCostSun :
                        0;
            }
        }
    }

    @Override
    public int getEnchantmentValue(ItemStack stack){
        return 15;
    }

}
