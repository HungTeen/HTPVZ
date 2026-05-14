package com.hungteen.pvz.api.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**This event is here to judge if an item stack is in player's inventory, fired on both client and server. it's not cancellable and has a result.*/
public class IsInInventoryEvent extends PlayerEvent {
    public final ItemStack itemStack;

    public IsInInventoryEvent(Player player, ItemStack itemStack) {
        super(player);
        this.itemStack = itemStack;
        this.setResult(Result.DENY);
    }
}
