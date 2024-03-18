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
    public int coolDown;
    public String resource;

    public PVZResourceEvent(Player player, String resource, int cost, int coolDown) {
        super(player);
        this.cost = cost;
        this.resource = resource;
        this.coolDown = coolDown;
    }

    /**
     * fired whenever the cost of a seedPacket is needed, on both server and client.
     * <p> if you want to refresh the number shows in gui, call <br> {@link com.hungteen.pvz.client.gui.PVZOverlayHandler#refreshMainHandItemStack(Player)}
     * or <br> {@link com.hungteen.pvz.client.gui.PVZOverlayHandler#refreshOffHandItemStack(Player)}.
     */
    public static class CheckResourceEvent extends PVZResourceEvent {
        public final ItemStack seedPacket;
        public CheckResourceEvent(Player player, ItemStack plantCard) {
            super(player, null, 0, 0);
            this.seedPacket = plantCard;
            SeedPacketItem<?> item = (SeedPacketItem<?>) plantCard.getItem();
            resource = item.getResource(plantCard);
            cost = (resource.equals(PVZPlayerCapNBT.SUN)
                    && PVZPlayerCapability.getValue(player, "plant_have_cost") == 0) ?
                    0 : item.getBaseCost(plantCard);
            coolDown = PVZPlayerCapability.getValue(player, "plant_have_cd") == 0 ?
                    1 : item.getBaseCoolDown(plantCard);
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
        public Entity spawningEntity;

        public CheckPlantConditionEvent(Player player, ItemStack plantCard, Entity spawningEntity) {
            super(player, plantCard);
            this.spawningEntity = spawningEntity;
        }
    }
}
