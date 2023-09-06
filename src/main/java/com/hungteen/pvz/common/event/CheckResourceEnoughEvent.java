package com.hungteen.pvz.common.event;

import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.item.PlantCardItem;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.Cancelable;

/**
 * fired when whether the sun is enough is checked, possibly fired on both server and client.
 */
public class CheckResourceEnoughEvent extends PlayerEvent {
    public int cost;
    public String resource;
    public final ItemStack plantCard;
    public CheckResourceEnoughEvent(Player player, ItemStack plantCard) {
        super(player);
        this.plantCard = plantCard;
        PlantCardItem<?> item = (PlantCardItem<?>) plantCard.getItem();
        resource = item.getResource(plantCard);
        cost = (resource.equals(PVZPlayerCapNBT.SUN)
                && PVZPlayerCapability.getValue(player, "plant_have_cost") == 0) ?
                0 : item.getCost(plantCard);
    }

    /**
     * fired whenever a plant is planted by a {@link PlantCardItem#useOn(UseOnContext)}.
     * <br>this event is cancelable. If cancel this event, the plant won't br planted.
     * <br>if you want to change the resource cost, also subscribe {@link CheckResourceEnoughEvent}.
     * <br>fired only on the server.
     */
    @Cancelable
    public static class CheckPlantableEvent extends CheckResourceEnoughEvent {
        public int coolDown;

        public CheckPlantableEvent(Player player, ItemStack plantCard) {
            super(player, plantCard);
            PlantCardItem<?> item = (PlantCardItem<?>) plantCard.getItem();
            coolDown = PVZPlayerCapability.getValue(player, "plant_have_cd") == 0 ?
                    0 : item.getCoolDown(plantCard);
        }
    }
}
