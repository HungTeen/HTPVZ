package com.hungteen.pvz.api.events;

import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerEvent;

/**This event is fired only on server.
 * <br> {@link Phase#PRE} is fired whenever a player try to shovel a plant. Cancel this event to stop shovelling.
 * <br> {@link Phase#POST} is fired when the plant is already shoveled. The event is not cancellable at this phase.*/
@net.minecraftforge.eventbus.api.Cancelable
public class PlantShoveledEvent extends PlayerEvent {
    public Phase phase;
    public InteractionHand handIn;
    public LivingEntity target;
    public PlantShoveledEvent(Player player, InteractionHand handIn, LivingEntity target, Phase phase) {
        super(player);
        this.handIn = handIn;
        this.target = target;
        this.phase = phase;
    }

    @Override
    public boolean isCancelable() {
        return super.isCancelable() && this.phase == Phase.PRE;
    }
    public enum Phase {
        PRE, POST
    }
}
