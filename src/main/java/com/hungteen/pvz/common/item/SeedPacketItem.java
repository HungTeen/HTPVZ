package com.hungteen.pvz.common.item;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.api.events.SeedPacketPlantEvent;
import com.hungteen.pvz.api.interfaces.*;
import com.hungteen.pvz.client.gui.PVZOverlayHandler;
import com.hungteen.pvz.client.gui.components.SunImageToolTipComponent;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapStats;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZEnchantments;
import com.hungteen.pvz.common.register.PVZSeedPackets;
import com.hungteen.pvz.common.register.PVZSoundEvents;
import com.hungteen.pvz.common.register.PVZStats;
import com.hungteen.pvz.common.world.PlayerSunContainerWrapper;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.Util;
import net.minecraft.ChatFormatting;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.BlockParticleOption;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundSource;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Supplier;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class SeedPacketItem<T extends Entity> extends Item implements IHaveSkills {
    /**Contains all kinds of SeedPacketItem.*/
    public static List<SeedPacketItem<?>> seedPacketItemList = new ArrayList<>();

    //entitySupplier is unchangeable. the rest three can be adjusted with command.
    protected final Supplier<EntityType<T>> entitySupplier;
    private final String resource;
    private final int cost;
    private final int coolDown;
    private final List<Skill> skillList;
    public final boolean creativeOnly;
    public final boolean extraCost;

    public SeedPacketItem(Properties p_41383_, Supplier<EntityType<T>> entitySupplier, List<Skill> skillList, String resource, int cost, int coolDown, boolean creativeOnly, boolean extraCost) {
        super(p_41383_);
        this.entitySupplier = entitySupplier;
        this.skillList = skillList;
        this.resource = resource;
        this.cost = cost;
        this.coolDown = coolDown;
        this.creativeOnly = creativeOnly;
        this.extraCost = extraCost;
        seedPacketItemList.add(this);
    }


    //methods
    public EntityType<T> getEntity(){
        return entitySupplier.get();
    }

    /** This method returns the original cost of the itemStack, not including the effects of enchantments and buffs. To get the accurate number, use {@link PVZResourceEvent.CheckResourceEvent}.
     */
    public int getBaseCost(@Nullable ItemStack itemStack) {
        if (itemStack != null && itemStack.getItem() instanceof SeedPacketItem && itemStack.getTag() != null) {
            return itemStack.getTag().contains("Cost") ? itemStack.getTag().getInt("Cost") : cost;
        }
        return cost;
    }

    public int getTotalExtraCost(ICanUseSeedPack planter, ItemStack itemStack) {
        return getNumExtraCost(planter) + getSkillExtraCost(itemStack);
    }

    public int getSingleNumExtraCost(ICanUseSeedPack container, T entity) {
        return 50;
    }

    public int getNumExtraCost(@Nullable ICanUseSeedPack container) {
        AtomicInteger result = new AtomicInteger();
        int range = Util.getSeedPacketExtraCostRange(container);
        if (range >= 0 && container != null && container.getLevel() != null && container.getPosition() != null) {
            container.getLevel().getEntities((Entity) null
                            , new AABB(container.getPosition()
                                    .add(-1, -1, -1), container.getPosition().add(1, 1 ,1)).inflate(range)
                            , entity1 -> (entity1.getType() == getEntity() && extraCost
                                    && (container.getEntity() == null || EntityUtil.isTeammate(container.getEntity(), entity1))))
                    .forEach(entity1 -> result.addAndGet(getSingleNumExtraCost(container, (T) entity1)));
        }
        return result.get();
    }

    public int getSkillExtraCost(ItemStack itemStack) {
        int addition = 0;
        for (int i : this.getSkills(itemStack)) {
            if (this.getStaticSkillList().size() - 1 >= i) {
                addition += this.getStaticSkillList().get(i).addCostResource;
            }
        }
        return addition;
    }

    /** This method returns the original cool down of the itemStack, not including the effects of enchantments and buffs. To get the accurate number, use {@link PVZResourceEvent.CheckResourceEvent}.
     */
    public int getBaseCoolDown(@Nullable ItemStack itemStack) {
        if (itemStack != null && itemStack.getItem() instanceof SeedPacketItem && itemStack.getTag() != null) {
            return itemStack.getTag().contains("CoolDown") ? itemStack.getTag().getInt("CoolDown") : coolDown;
        }
        return coolDown;
    }

    public int getTotalExtraCoolDown(@Nullable ISunContainer container, ItemStack itemStack) {
        return getEnchantExtraCoolDown(itemStack) + getSkillExtraCoolDown(itemStack);
    }

    public int getEnchantExtraCoolDown(ItemStack itemStack) {
        int level = EnchantmentHelper.getTagEnchantmentLevel(PVZEnchantments.QUICK_COOL_DOWN.get(), itemStack);
        if (level > 0) {
            return getBaseCoolDown(itemStack) * (10 - level) / 10;
        }
        return 0;
    }

    public int getSkillExtraCoolDown(ItemStack itemStack) {
        int addition = 0;
        for (int i : this.getSkills(itemStack)) {
            if (this.getStaticSkillList().size() - 1 >= i) {
                addition += this.getStaticSkillList().get(i).addCoolDown;
            }
        }
        return addition;
    }

    /**SeedPacket may not cost sun but other resource instead. */
    public String getResource(@Nullable ItemStack itemStack) {
        if (itemStack != null && itemStack.getItem() instanceof SeedPacketItem && itemStack.getTag() != null) {
            return itemStack.getTag().contains("Resource") ? itemStack.getTag().getString("Resource") : resource;
        }
        return resource;
    }

    public boolean shouldDefineOwner(ItemStack itemStack) {
        if (itemStack != null && itemStack.getItem() instanceof SeedPacketItem && itemStack.getTag() != null) {
            return ! itemStack.getTag().contains("ShouldOwn") || itemStack.getTag().getBoolean("ShouldOwn");
        }
        return true;
    }

    public static SeedPacketItem<?> getSeedPacket(EntityType<?> entityType) {
        for (SeedPacketItem<?> item : seedPacketItemList) {
            if (item.getEntity().equals(entityType)) {
                return item;
            }
        }
        return null;
    }


    //definitions
    @Override
    public List<Skill> getStaticSkillList(){
        List<Skill> list = new ArrayList<>(skillList);
        List<Skill> additional = PVZSeedPackets.additionalSkills.get(getEntity());
        if (additional != null) list.addAll(additional);
        return list;
    }


    public boolean canBoost(){
        return true;
    }

    @Override
    public Component getName(ItemStack itemStack) {
        Component original = super.getName(itemStack);
        if (original.getContents() instanceof TranslatableContents contents && original.getString().equals(contents.getKey())) {
            return Component.translatable("item.pvz.seed_packet", Component.translatable(entitySupplier.get().getDescriptionId()));
        }
        return original;
    }

    public int getSkillVal(Object obj) {
        if (obj instanceof ItemStack itemStack && itemStack.getItem() instanceof SeedPacketItem && itemStack.getTag() != null) {
            CompoundTag tag = itemStack.getTag();
            return getSkillValFromNames(tag.getList("PVZSkills", Tag.TAG_STRING).stream().map(Tag::getAsString).toList());
        }
        return 0;
    }

    @Override
    public void setSkillVal(Object obj, int value) {
        if (obj instanceof ItemStack itemStack && itemStack.getItem() instanceof SeedPacketItem) {
            CompoundTag tag = itemStack.getTag();
            ListTag skills = new ListTag();
            for (String name : getSkillNames(value)) {
                skills.add(StringTag.valueOf(name));
            }
            tag.put("PVZSkills", skills);
        }
    }

    //TODO do not use if main hand interacting result consumes action.
    protected void used(ItemStack itemstack, Player player) {
        player.awardStat(Stats.ITEM_USED.get(itemstack.getItem()));
        player.awardStat(PVZStats.PLANT);
        if (!PVZConfig.PVZGameRules.getBoolean(player.level, PVZConfig.Common.plantNeedsDurability)) {
            return;
        }
        itemstack.hurtAndBreak(1, player, (entity1) ->
                player.broadcastBreakEvent(entity1.getItemInHand(InteractionHand.MAIN_HAND) == itemstack ? InteractionHand.MAIN_HAND : InteractionHand.OFF_HAND));
    }

    /**
     * checks if sun is enough for planting here, and then test situations of planting on fluids.
     */
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand handIn) {
        if (PVZPlayerCapability.getValue(player, PVZPlayerCapStats.CAN_PLANT) == 0) return super.use(level, player, handIn);
        //sun check
        if (level.isClientSide() && getResource(player.getItemInHand(handIn)).equals(PVZPlayerCapStats.SUN)) {
            PVZResourceEvent.CheckResourceEvent event = Util.checkPlantResourceEvent(player, player.getItemInHand(handIn));
            MinecraftForge.EVENT_BUS.post(event);
            if (event.cost > PVZPlayerCapability.getValue(player, event.resource)) {
                PVZOverlayHandler.notEnoughHint = 1.5F;
            }
            return InteractionResultHolder.fail(player.getItemInHand(handIn));
        }
        if (level.isClientSide()) return InteractionResultHolder.consume(player.getItemInHand(handIn));
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
        return plantOnBlock(new PlayerSunContainerWrapper(player), itemStack, level, pos, direction);
    }

    public MutableComponent plantOnBlock(ICanUseSeedPack sunContainer, ItemStack itemStack, Level level, BlockPos pos, Direction direction) {
        if (! level.isClientSide && itemStack.getItem() instanceof SeedPacketItem<?>
                && sunContainer.getCooldowns() != null && ! sunContainer.getCooldowns().isOnCooldown(this)) {
            Entity planter = sunContainer.getEntity();
            //check entity.
            Entity entity = getEntity().create((ServerLevel) level, null,
                    itemStack.hasCustomHoverName() ? getName(itemStack) : null,
                    planter instanceof Player p ? p : null, pos.above(), MobSpawnType.SPAWN_EGG, false, false);
            if (entity == null) return Component.translatable("hint.pvz.plant.player_cant_plant");
            if (itemStack.hasCustomHoverName()) {
                entity.setCustomName(itemStack.getHoverName());
            }
            //CanPlaceOn test
            if (itemStack.getOrCreateTag().contains("CanPlaceOn")) {
                ListTag list = itemStack.getTag().getList("CanPlaceOn", Tag.TAG_STRING);
                Block block = level.getBlockState(pos).getBlock();
                if (list.stream().noneMatch(tag -> ForgeRegistries.BLOCKS.getKey(block).toString().equals(tag.getAsString()))) {
                    return Component.translatable("hint.pvz.plant.cant_plant_on", entity.getName(), block.getName());
                }
            }
            //enchantment.
            if (entity instanceof IPlant && (EnchantmentHelper.getTagEnchantmentLevel(PVZEnchantments.SOILLESS_CULTURE.get(), itemStack) > 0 ||
                    itemStack.getOrCreateTag().contains("CanPlaceOn"))) {
                entity.getEntityData().set(((IPlant) entity).root(), false);
            }
            //handle skills.
            if (entity instanceof IHaveSkills) {
                ((IHaveSkills) entity).setSkillVal(entity, getSkillVal(itemStack));
            }
            //check sun and position.
            PVZResourceEvent.CheckPlantConditionEvent event = Util.checkPlantConditionEvent(sunContainer, itemStack, entity);
            MutableComponent posCheck = sunContainer.prePlantOnBlock(event, entity, itemStack, level, pos, direction);
            if (posCheck == null) {
                MinecraftForge.EVENT_BUS.post(event);
                posCheck = entity instanceof INeedSafeSituation ?
                        ((INeedSafeSituation) entity).isPositionSafe(event, entity.level, pos, direction, true) : null;
                sunContainer.postPlantOnBlock(event, entity, itemStack, posCheck, level, pos, direction);
            }
            if (posCheck == null) {
                //plant.
                ((ServerLevel) level).sendParticles(new BlockParticleOption(ParticleTypes.BLOCK, level.getBlockState(entity.getOnPos()))
                        .setPos(entity.getOnPos()), entity.getX(), entity.getY(), entity.getZ(), 5, 0.0D, 0.0D, 0.0D, 0.25F);
                level.playSound(null, pos, level.getBlockState(pos).getFluidState().isEmpty() ? PVZSoundEvents.PLANT.get() : PVZSoundEvents.PLANT_WATER.get(), SoundSource.PLAYERS,1, 1);
                if (! entity.isRemoved()) {
                    MinecraftForge.EVENT_BUS.post(new SeedPacketPlantEvent(sunContainer, itemStack, entity));
                    ((ServerLevel) level).addFreshEntityWithPassengers(entity);
                }
                PVZEntityCapability cap = entity.getCapability(PVZEntityCapability.CAP).orElse(null);
                if (cap != null) {
                    cap.cost = event.cost;
                    cap.resource = event.resource;
                }
                return null;
            }
            entity.discard();
            return posCheck;
        }
        return Component.translatable("hint.pvz.plant.player_cant_plant");
    }

    /**Situation of interacting with mobs.*/
    @SubscribeEvent
    public static void interactEntity(PlayerInteractEvent.EntityInteract ev) {
        Level level = ev.getLevel();
        ItemStack itemStack = ev.getEntity().getItemInHand(ev.getHand());
        if (ev.getEntity().getItemInHand(ev.getHand()).getItem() instanceof SeedPacketItem<?> item) {
            if (level.isClientSide()) return;
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
        return plantOnEntity(new PlayerSunContainerWrapper(player), itemStack,  level, target);
    }

    public MutableComponent plantOnEntity(ICanUseSeedPack sunContainer, ItemStack itemStack, Level level, Entity target) {
        if (! level.isClientSide && itemStack.getItem() instanceof SeedPacketItem<?>
                && sunContainer.getCooldowns() != null && ! sunContainer.getCooldowns().isOnCooldown(this)) {
            Entity planter = sunContainer.getEntity();
            //check entity.
            Entity entity = this.getEntity().create((ServerLevel) level, null,
                    itemStack.hasCustomHoverName() ? this.getName(itemStack) : null,
                    planter instanceof Player p ? p : null, target.blockPosition(), MobSpawnType.SPAWN_EGG, false, false);
            if (entity == null) return Component.translatable("hint.pvz.plant.player_cant_plant");
            if (itemStack.hasCustomHoverName()) {
                entity.setCustomName(itemStack.getHoverName());
            }
            PVZEntityCapability cap = entity.getCapability(PVZEntityCapability.CAP).orElse(null);
            if (cap != null && shouldDefineOwner(itemStack)) {
                cap.setOwner(planter);
            }
            //enchantment.
            if (entity instanceof IPlant && (EnchantmentHelper.getTagEnchantmentLevel(PVZEnchantments.SOILLESS_CULTURE.get(), itemStack) > 0 ||
                    itemStack.getOrCreateTag().contains("CanPlaceOn"))) {
                entity.getEntityData().set(((IPlant) entity).root(), false);
            }
            //handle skills.
            if (entity instanceof IHaveSkills) {
                ((IHaveSkills) entity).setSkillVal(entity, this.getSkillVal(itemStack));
            }
            //check sun and position.

            PVZResourceEvent.CheckPlantConditionEvent event = Util.checkPlantConditionEvent(sunContainer, itemStack, entity);
            MutableComponent targetCheck = sunContainer.prePlantOnEntity(event, entity, itemStack, entity);
            if (targetCheck == null) {
                MinecraftForge.EVENT_BUS.post(event);
                targetCheck = entity instanceof INeedSafeSituation ?
                        ((INeedSafeSituation) entity).isVehicleSafe(event, target, true) : null;
                sunContainer.postPlantOnEntity(event, entity, itemStack, targetCheck, target);
            }
            if (targetCheck == null) {
                //plant.
                level.playSound(null, entity.blockPosition(), level.getBlockState(entity.blockPosition()).getFluidState().isEmpty() ? PVZSoundEvents.PLANT.get() : PVZSoundEvents.PLANT_WATER.get(), SoundSource.PLAYERS,1, 1);
                if (! entity.isRemoved()) {
                    MinecraftForge.EVENT_BUS.post(new SeedPacketPlantEvent(sunContainer, itemStack, entity));
                    ((ServerLevel) level).addFreshEntityWithPassengers(entity);
                }
                if (cap != null && shouldDefineOwner(itemStack)) {
                    cap.cost = event.cost;
                    cap.resource = event.resource;
                }
                return null;
            }
            entity.discard();
            return targetCheck;
        }
        return Component.translatable("hint.pvz.plant.player_cant_plant");
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
    public Optional<TooltipComponent> getTooltipImage(@NotNull ItemStack itemStack) {
        Player player = !(ClientProxy.MC.getCameraEntity() instanceof Player) ? null : ClientProxy.getPlayer();
        if (player != null && !player.isCreative() && !player.isSpectator()) {
            PVZResourceEvent.CheckResourceEvent event = Util.checkPlantResourceEvent(player, itemStack);
            MinecraftForge.EVENT_BUS.post(event);
            boolean hasExtraCost = this.extraCost && Util.getSeedPacketExtraCostRange(new PlayerSunContainerWrapper(player)) >= 0; //TODO let tooltip show whether it has advanced plant extra cost.
            return Optional.of(new SunImageToolTipComponent(event.cost, event.coolDown, getResource(itemStack).equals(PVZPlayerCapStats.SUN)
                    , false, true, hasExtraCost));
        }
        return Optional.empty();
    }

    public static void registerProperties() {
        seedPacketItemList.forEach(item -> {
            ItemProperties.register(item, Util.prefix("skilled"),
                    (itemStack, level, entity, seed) -> item.getSkillVal(itemStack) > 0 ? 1 : 0);
        });
    }
}
