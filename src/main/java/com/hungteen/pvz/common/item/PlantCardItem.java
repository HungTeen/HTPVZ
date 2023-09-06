package com.hungteen.pvz.common.item;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.gui.PVZOverlayHandler;
import com.hungteen.pvz.common.capability.owned.PVZOwnedCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.INeedSafeSituation;
import com.hungteen.pvz.common.event.CheckResourceEnoughEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class PlantCardItem<T extends Entity> extends Item {

    //entitySupplier is unchangeable. the rest three can be adjusted with command.
    private final Supplier<EntityType<T>> entitySupplier;
    private final String resource;
    private final int cost;
    private final int coolDown;

    public PlantCardItem(Properties p_41383_, Supplier<EntityType<T>> entitySupplier, String resource, int cost, int coolDown) {
        super(p_41383_.stacksTo(1));
        this.entitySupplier = entitySupplier;
        this.resource = resource;
        this.cost = cost;
        this.coolDown = coolDown;
    }

    public EntityType<T> getEntity(){
        return entitySupplier.get();
    }

    public int getCost(@Nullable ItemStack itemStack){
        if (itemStack != null && itemStack.getItem() instanceof PlantCardItem && itemStack.getTag() != null) {
            return itemStack.getTag().contains("cost") ? itemStack.getTag().getInt("cost") : cost;
        }
        return cost;
    }
    public int getCoolDown(@Nullable ItemStack itemStack){
        if (itemStack != null && itemStack.getItem() instanceof PlantCardItem && itemStack.getTag() != null) {
            return itemStack.getTag().contains("cool_down") ? itemStack.getTag().getInt("cool_down") : coolDown;
        }
        return coolDown;
    }
    public String getResource(@Nullable ItemStack itemStack){
        if (itemStack != null && itemStack.getItem() instanceof PlantCardItem && itemStack.getTag() != null) {
            return itemStack.getTag().contains("resource") ? itemStack.getTag().getString("resource") : resource;
        }
        return resource;
    }

    //TODO add a cooling down hint.

    @Override
    public Component getName(ItemStack itemStack) {
        return Component.translatable("item.pvz.plant_card", Component.translatable(entitySupplier.get().getDescriptionId()));
    }

    /**
     * Only checks if sun is enough for planting here.
     * check {@link PlantCardItem#useOn(UseOnContext)} for planting a plant.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand handIn) {
        if (level.isClientSide() && getResource(player.getItemInHand(handIn)).equals(PVZPlayerCapNBT.SUN)) {
            CheckResourceEnoughEvent event = new CheckResourceEnoughEvent(player, player.getItemInHand(handIn));
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
            MutableComponent posCheck = entity instanceof INeedSafeSituation ? ((INeedSafeSituation) entity).isPositionSafe(entity.level, pos.below()) : null;
            if (entity != null && posCheck == null) {
                CheckResourceEnoughEvent.CheckPlantableEvent event = new CheckResourceEnoughEvent.CheckPlantableEvent(player, context.getItemInHand());
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
                    return InteractionResult.SUCCESS;
                }
                player.displayClientMessage(Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource)), true);
                entity.remove(Entity.RemovalReason.DISCARDED);
                return InteractionResult.FAIL;
            }
            player.displayClientMessage(posCheck, true);
            if (entity != null) {
                entity.remove(Entity.RemovalReason.DISCARDED);
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
            if (itemStack.getItem() instanceof PlantCardItem<?> && !player.getCooldowns().isOnCooldown(itemStack.getItem())) {
                Entity target = ev.getTarget();
                Entity entity = ((PlantCardItem<?>) itemStack.getItem()).getEntity().create((ServerLevel) level, null,
                        itemStack.hasCustomHoverName() ? ((PlantCardItem<?>) itemStack.getItem()).getName(itemStack) : null,
                        player, target.blockPosition(), MobSpawnType.SPAWN_EGG, false, false);
                if (entity != null && itemStack.hasCustomHoverName()) {
                    entity.setCustomName(itemStack.getHoverName());
                }
                MutableComponent targetCheck = entity instanceof INeedSafeSituation ? ((INeedSafeSituation) entity).isVehicleSafe(target) : null;
                if (entity != null && targetCheck == null) {
                    CheckResourceEnoughEvent.CheckPlantableEvent event = new CheckResourceEnoughEvent.CheckPlantableEvent(player, itemStack);
                    MinecraftForge.EVENT_BUS.post(event);
                    if (event.cost <= PVZPlayerCapability.getValue(player, event.resource)) {
                        PVZPlayerCapability.getPlayerData(player).ifPresent((nbt) -> nbt.addValue(event.resource, -event.cost));
                        player.getCooldowns().addCooldown(itemStack.getItem(), event.coolDown);
                        ((ServerLevel) level).addFreshEntityWithPassengers(entity);
                        entity.moveTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), 0.0F);
                        entity.startRiding(target);
                        PVZOwnedCapability cap = PVZOwnedCapability.getCap(entity);
                        cap.setOwner(player);
                        cap.cost = event.cost;
                        cap.resource = event.resource;
                    } else {
                        player.displayClientMessage(Component.translatable("hint.pvz.plant.no_enough_resource", Component.translatable(event.resource)), true);
                        entity.remove(Entity.RemovalReason.DISCARDED);
                    }
                } else {
                    player.displayClientMessage(targetCheck, true);
                    if (entity != null) {
                        entity.remove(Entity.RemovalReason.DISCARDED);
                    }
                }
            }

        }
    }
}
