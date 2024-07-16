package com.hungteen.pvz.api.events;

import com.hungteen.pvz.api.ZombieEvent;
import net.minecraftforge.eventbus.api.Event;

/**Um sry i know this name is a little strange but... whatever it is an event for {@link ZombieEvent ZombieEvents}.
 * <br> This event is not cancelable.*/
public class ZombieEventEvent extends Event {
    /**With {@link com.hungteen.pvz.api.PVZAPI.IPVZAPI#getZombieEventType(ZombieEvent) PVZAPI#getZombieEventType()} the type of the event can be identified.*/
    public final ZombieEvent event;
    public final Phase phase;
    public ZombieEventEvent(ZombieEvent event, Phase phase) {
        this.event = event;
        this.phase = phase;
    }
    public enum Phase {
        New, Load, Tick, Remove
    }
}
