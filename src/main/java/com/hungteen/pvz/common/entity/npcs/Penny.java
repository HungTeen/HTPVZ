package com.hungteen.pvz.common.entity.npcs;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.entity.IEntityPacketHandler;
import com.hungteen.pvz.common.entity.plants.base.SimplePlant;
import com.hungteen.pvz.common.menu.PennyMenu;
import com.hungteen.pvz.common.network.PVZPacketHandler;
import com.hungteen.pvz.common.network.PennyOffersPacket;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZEnchantments;
import com.hungteen.pvz.common.register.PVZItems;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.SimpleMenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.AnimationState;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.npc.Npc;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.EnchantedBookItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.trading.Merchant;
import net.minecraft.world.item.trading.MerchantOffer;
import net.minecraft.world.item.trading.MerchantOffers;
import net.minecraft.world.level.Level;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.OptionalInt;
import java.util.Set;

public class Penny extends Mob implements Npc, Merchant, IEntityPacketHandler {
    @Nullable
    private Player tradingPlayer;
    public AnimationState idleAnimationState = new AnimationState();
    private int despawnTime;
    @Nullable
    protected MerchantOffers offers;
    private final Set<Pair<Integer, ItemStack>> valuables = Set.of(
            Pair.of(2, PVZItems.ALMANAC.get().getDefaultInstance()),
            Pair.of(3, new ItemStack(PVZItems.SEED_DISPENSARY.get())),
            Pair.of(6, new ItemStack(PVZBlocks.GARDEN_FLOWER_POT.get(), 1)),
            Pair.of(6, PVZItems.SHELL_STARTUP.get().getDefaultInstance()),
            Pair.of(6, PVZBlocks.SILVER_SWORD_ORNAMENT.get().asItem().getDefaultInstance()),
            Pair.of(18, new ItemStack(PVZItems.SNAIL_GACHAPON.get(), 3))
    );
    private final Set<Pair<Integer, ItemStack>> commons = Set.of(
            Pair.of(2, new ItemStack(Items.LEAD, 2)),
            Pair.of(2, new ItemStack(Items.NAME_TAG)),
            Pair.of(3, new ItemStack(PVZItems.TACO.get(), 4)),
            Pair.of(12, new ItemStack(Items.NETHERITE_SCRAP)),
            Pair.of(18, EnchantedBookItem.createForEnchantment(new EnchantmentInstance(PVZEnchantments.SUN_MENDING.get(), 1))),
            Pair.of(18, EnchantedBookItem.createForEnchantment(new EnchantmentInstance(PVZEnchantments.SUN_SHOVEL.get(), 1))),
            Pair.of(18, EnchantedBookItem.createForEnchantment(new EnchantmentInstance(PVZEnchantments.SOILLESS_CULTURE.get(), 1))),
            Pair.of(20, new ItemStack(Items.TOTEM_OF_UNDYING))
    );
    private final Set<Pair<Integer, ItemStack>> materials = Set.of(
            Pair.of(1, new ItemStack(Items.EMERALD, 8)),
            Pair.of(1, new ItemStack(Items.EMERALD, 8)),
            Pair.of(1, new ItemStack(Items.CLAY_BALL, 8)),
            Pair.of(1, new ItemStack(Items.SNOWBALL, 8)),
            Pair.of(1, new ItemStack(Items.NAUTILUS_SHELL, 1)),
            Pair.of(2, new ItemStack(PVZItems.ORIGIN_ESSENCE.get(), 8)),
            Pair.of(2, new ItemStack(Items.PHANTOM_MEMBRANE, 3)),
            Pair.of(2, new ItemStack(Items.ENDER_PEARL, 5)),
            Pair.of(2, new ItemStack(PVZItems.FALLEN_STAR.get(), 8)),
            Pair.of(3, new ItemStack(Items.GLOWSTONE_DUST, 8)),
            Pair.of(3, new ItemStack(Items.DIAMOND, 3)),
            Pair.of(3, new ItemStack(Items.DIAMOND, 3)),
            Pair.of(3, new ItemStack(PVZItems.FOG_IN_BOTTLE.get(), 4)),
            Pair.of(4, new ItemStack(Items.PRISMARINE_CRYSTALS, 8)),
            Pair.of(4, new ItemStack(Items.BLAZE_POWDER, 8))
    );

    public Penny(EntityType<? extends Mob> p_21368_, Level p_21369_) {
        super(p_21368_, p_21369_);
        this.idleAnimationState.start(this.tickCount);
        int interval = PVZConfig.PVZGameRules.getInt(this.level, PVZConfig.Common.naturallySpawnPennyInterval);
        this.setDespawnTime(interval / 2);
    }

    public void setDespawnTime(int time) {
        this.despawnTime = Math.toIntExact(this.getLevel().getGameTime() + time);
    }

    @Override
    public boolean removeWhenFarAway(double p_27598_) {
        return false;
    }

    @Override
    public void aiStep() {
        super.aiStep();
        if (!this.level.isClientSide) {
            this.maybeDespawn();
        }
    }

    private void maybeDespawn() {
        if (! this.level.isClientSide && this.despawnTime < this.level.getGameTime()
                && ! this.isTrading() && level.getNearestPlayer(this, 20) == null) {
            this.discard();
        }
    }

    public boolean canBeLeashed(Player p_21418_) {
        return false;
    }

    public @NotNull InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (this.isAlive() && ! this.isTrading() && ! this.isSleeping() && !player.isSecondaryUseActive()) {
            if (! this.getOffers().isEmpty()) {
                if (!this.level.isClientSide && ! this.getOffers().isEmpty()) {
                    this.startTrading(player);
                }

            }
            return InteractionResult.sidedSuccess(this.level.isClientSide);
        } else {
            return super.mobInteract(player, hand);
        }
    }

    public static AttributeSupplier.Builder createAttributes() {
        return SimplePlant.createAttributes()
                .add(Attributes.MAX_HEALTH, 200D)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1D)
                .add(Attributes.ARMOR, 25D)
                .add(Attributes.ARMOR_TOUGHNESS, 20D);
    }

    public void die(DamageSource p_35270_) {
        super.die(p_35270_);
        this.stopTrading();
    }

    @Nullable
    public Entity changeDimension(ServerLevel level, net.minecraftforge.common.util.ITeleporter teleporter) {
        this.stopTrading();
        return super.changeDimension(level, teleporter);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("DespawnDelay", this.despawnTime);
        MerchantOffers merchantoffers = this.getOffers();
        if (!merchantoffers.isEmpty()) {
            tag.put("Offers", merchantoffers.createTag());
        }
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("DespawnDelay", 99)) {
            this.despawnTime = tag.getInt("DespawnDelay");
        }
        if (tag.contains("Offers", 10)) {
            this.offers = new MerchantOffers(tag.getCompound("Offers"));
        }
    }

    @Override
    public boolean isPushable(){
        return false;
    }

    //trade
    public boolean isTrading() {
        return this.tradingPlayer != null;
    }

    private void startTrading(Player player) {
        this.setTradingPlayer(player);
        this.openTradingScreen(player, this.getDisplayName(), 0);
    }

    protected void stopTrading() {
        this.setTradingPlayer(null);
    }

    @Override
    public void setTradingPlayer(@Nullable Player player) {
        boolean flag = this.getTradingPlayer() != null && player == null;
        this.tradingPlayer = player;
        if (flag) {
            this.stopTrading();
        }
    }

    @Override
    public @Nullable Player getTradingPlayer() {
        return this.tradingPlayer;
    }
    @Override
    public void openTradingScreen(Player player, Component p_45303_, int p_45304_) {
        OptionalInt optionalint = player.openMenu(new SimpleMenuProvider((p_45298_, p_45299_, p_45300_) ->
                new PennyMenu(p_45298_, p_45299_, this), p_45303_));
        if (optionalint.isPresent() && player instanceof ServerPlayer player1) {
            if (player1.containerMenu instanceof PennyMenu menu && menu.isVanillaUI) {
                menu.merchantContainer.updateSellItem();
            }
            MerchantOffers merchantoffers = this.getOffers();
            if (! merchantoffers.isEmpty()) {
                PVZPacketHandler.sendToClient(player1,
                        new PennyOffersPacket(optionalint.getAsInt(), merchantoffers, p_45304_, this.getVillagerXp(), this.showProgressBar(), this.canRestock()));
            }
        }

    }
    @Override
    public @NotNull MerchantOffers getOffers() {
        if (this.offers == null) {
            this.offers = new MerchantOffers();
            this.updateTrades();
        }
        return this.offers;
    }

    public void updateTrades() {
        MerchantOffers offers = this.offers;
        ItemStack jewel = PVZItems.JEWEL.get().getDefaultInstance();
        jewel.setCount(2);
        for (int i = 0; i < 3; i ++) {
            offers.add(new MerchantOffer(jewel.copy(), PVZItems.MARIGOLD_SPROUT.get().getDefaultInstance()
                    , 1, 0, 0));
        }
        jewel.setCount(2);
        offers.add(new MerchantOffer(jewel.copy(), new ItemStack(PVZItems.FERTILIZER.get(), 8)
                , 1, 0, 0));
        Pair<Integer, ItemStack> pair;
        pair = this.valuables.stream().findFirst().get();
        jewel.setCount(pair.getFirst());
        offers.add(new MerchantOffer(jewel.copy(), pair.getSecond()
                , 1, 0, 0));
        var iterator = this.commons.stream().iterator();
        for (int i = 0; i < 2; i ++) {
            pair = iterator.next();
            jewel.setCount(pair.getFirst());
            offers.add(new MerchantOffer(jewel.copy(), pair.getSecond()
                    , 1, 0, 0));
        }
        jewel.setCount(3);
        offers.add(new MerchantOffer(jewel.copy(), new ItemStack(PVZItems.ALAYA_RESIN.get(), 3)
                , 1, 0, 0));
        iterator = this.materials.stream().iterator();
        for (int i = 0; i < 4; i ++) {
            pair = iterator.next();
            jewel.setCount(pair.getFirst());
            offers.add(new MerchantOffer(jewel.copy(), pair.getSecond()
                    , 1, 0, 0));
        }
    }

    @Override
    public void overrideOffers(MerchantOffers offers) {
        this.offers = offers;
    }

    @Override
    public void overrideXp(int p_45309_) {
    }

    @Override
    public void notifyTrade(MerchantOffer offer) {
        offer.increaseUses();
        this.ambientSoundTime = - this.getAmbientSoundInterval();
    }

    @Override
    public void notifyTradeUpdated(ItemStack p_45308_) {
    }

    @Override
    public int getVillagerXp() {
        return 0;
    }

    @Override
    public boolean showProgressBar() {
        return false;
    }

    @Override
    public SoundEvent getNotifyTradeSound() {
        return SoundEvents.VILLAGER_YES;
    }

    @Override
    public boolean isClientSide() {
        return this.level.isClientSide;
    }

    @Override
    public void handlePVZPacket(ServerPlayer player, int val) {
        AbstractContainerMenu abstractcontainermenu = player.containerMenu;
        if (abstractcontainermenu instanceof PennyMenu menu) {
            if (val >= 0) {
                if (!menu.stillValid(player)) {
                    PVZMod.LOGGER.debug("Player {} interacted with invalid menu {}", player, menu);
                    return;
                }
                menu.setSelectionHint(val);
                menu.tryMoveItems(val);
            } else {
                menu.merchantContainer.currentPage = - val - 1;
                menu.merchantContainer.updateSellItem();
            }
        }
    }
}
