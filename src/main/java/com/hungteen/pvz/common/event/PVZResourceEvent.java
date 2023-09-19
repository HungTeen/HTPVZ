package com.hungteen.pvz.common.event;

import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.item.SeedPacketItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

public class PVZResourceEvent extends PlayerEvent {
    public int cost;
    public String resource;

    public PVZResourceEvent(Player player, String resource, int cost) {
        super(player);
        this.cost = cost;
        this.resource = resource;
    }

    /**
     * fired whenever the cost of a seedPacket is needed, fired on both server and client.
     * <p> if you want to refresh the number shows in gui, call {@link com.hungteen.pvz.client.gui.PVZOverlayHandler#refreshItemStack(Player, ItemStack)}.
     */
    public static class CheckResourceEvent extends PVZResourceEvent {
        public final ItemStack seedPacket;
        public CheckResourceEvent(Player player, ItemStack plantCard) {
            super(player, null, 0);
            this.seedPacket = plantCard;
            SeedPacketItem<?> item = (SeedPacketItem<?>) plantCard.getItem();
            resource = item.getResource(plantCard);
            cost = (resource.equals(PVZPlayerCapNBT.SUN)
                    && PVZPlayerCapability.getValue(player, "plant_have_cost") == 0) ?
                    0 : item.getCost(plantCard);
        }
    }

    /**
     * fired whenever a plant is planted by a {@link SeedPacketItem#useOn(UseOnContext)}.
     * <br>this event is cancelable. If cancel this event, the plant won't br planted.
     * <br>if you want to change the resource cost, also subscribe {@link CheckResourceEvent}.
     * <br>fired only on the server.
     */
    @Cancelable
    public static class CheckPlantConditionEvent extends CheckResourceEvent {
        public int coolDown;
        public Entity spawningEntity;

        public CheckPlantConditionEvent(Player player, ItemStack plantCard, Entity spawningEntity) {
            super(player, plantCard);
            SeedPacketItem<?> item = (SeedPacketItem<?>) plantCard.getItem();
            coolDown = PVZPlayerCapability.getValue(player, "plant_have_cd") == 0 ?
                    0 : item.getCoolDown(plantCard);
            this.spawningEntity = spawningEntity;
        }
    }
}
