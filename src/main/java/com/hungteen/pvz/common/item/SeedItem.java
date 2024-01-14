package com.hungteen.pvz.common.item;

import com.hungteen.pvz.client.gui.components.SunImageToolTipComponent;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.event.PVZResourceEvent;
import com.hungteen.pvz.common.network.ClientProxy;
import net.minecraft.network.chat.Component;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.tooltip.TooltipComponent;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.MinecraftForge;

import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Supplier;

public class SeedItem<T extends Entity> extends SeedPacketItem<T>{
    public SeedItem(Properties p_41383_, Supplier<EntityType<T>> entitySupplier, String resource, int cost, int coolDown) {
        super(p_41383_, entitySupplier, List.of(), resource, cost, coolDown);
    }

    @Override
    public Component getName(ItemStack itemStack) {
        return Component.translatable("item.pvz.seed", Component.translatable(entitySupplier.get().getDescriptionId()));
    }

    @Override
    public boolean canBoost() {
        return false;
    }

    protected void used(ItemStack itemstack, Player player, InteractionHand hand) {
        player.awardStat(Stats.ITEM_USED.get(itemstack.getItem()));
        if (!player.getAbilities().instabuild) {
            itemstack.shrink(1);
        }
    }

    @Override
    public boolean isEnchantable(ItemStack itemStack) {
        return false;
    }

    @Override
    public int getEnchantmentValue(ItemStack itemStack) {
        return 0;
    }

    @Override
    public boolean isValidRepairItem(ItemStack itemToFix, ItemStack material) {
        return false;
    }

    @Override
    public Optional<TooltipComponent> getTooltipImage(ItemStack itemStack) {
        Player player = !(ClientProxy.MC.getCameraEntity() instanceof Player) ? null : ClientProxy.getPlayer();
        if (! player.isCreative() && ! player.isSpectator()) {
            PVZResourceEvent.CheckResourceEvent event = new PVZResourceEvent.CheckResourceEvent(player, itemStack);
            MinecraftForge.EVENT_BUS.post(event);
            return Optional.of(new SunImageToolTipComponent(event.cost, event.coolDown, Objects.equals(getResource(itemStack), PVZPlayerCapNBT.SUN), false, false));
        }
        return Optional.empty();
    }
}
