package com.hungteen.pvz.api.events;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**In order to put the event into api packet, the rapid ways are moved to {@link com.hungteen.pvz.util.Util Util} .
 * <br>If you are writing a mod relying on this one, check Util.java above.*/
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
     * <br><br> {@link com.hungteen.pvz.util.Util#checkPlantResourceEvent(Player, ItemStack) <b>rapid method</b>} here.
     */
    public static class CheckResourceEvent extends PVZResourceEvent {
        public final ItemStack seedPacket;
        public CheckResourceEvent(Player player, ItemStack plantCard, String resource, int cost, int coolDown) {
            super(player, resource, cost, coolDown);
            this.seedPacket = plantCard;
        }
    }

    /**
     * fired whenever a plant is planted by a {@link com.hungteen.pvz.common.item.SeedPacketItem#useOn(UseOnContext) SeedPacketItem#useOn(context)}.
     * <br>this event is cancelable. If cancel this event, the plant won't br planted.
     * <br>if you want to change the resource cost, subscribe {@link CheckResourceEvent}.
     * <br>fired only on the server.
     * <br><br> {@link com.hungteen.pvz.util.Util#checkPlantConditionEvent(Player, ItemStack, Entity) <b>rapid method</b>} here.
     */
    @Cancelable
    public static class CheckPlantConditionEvent extends CheckResourceEvent {
        public Entity spawningEntity;

        public CheckPlantConditionEvent(Player player, ItemStack plantCard, Entity spawningEntity, String resource, int cost, int coolDown) {
            super(player, plantCard, resource, cost, coolDown);
            this.spawningEntity = spawningEntity;
        }
    }
}
