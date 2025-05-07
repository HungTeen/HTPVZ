package com.hungteen.pvz.api.events;

import com.hungteen.pvz.api.interfaces.ISun;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**This event is fired before and after a {@link ISun} is absorbed, using {@link Phase} to distinguish. It is <b>ONLY</b> cancelable when {@link Phase} is {@link Phase#Start Start}. When canceled, the sun will not be absorbed by the subject.
 * @apiNote Custom {@link ISun}s should add the event manually to {@link ISun#onAbsorbedBy(net.minecraft.world.entity.player.Player) onAbsorbedBy(Player)} and {@link ISun#onAbsorbedBy(com.hungteen.pvz.api.interfaces.ISunAbsorber) onAbsorbedBy(ISunAbsorber)} to ensure this event is called as expected.*/
@Cancelable
public class AbsorbSunEvent extends Event {
    public final ISun sun;
    public final Phase phase;
    AbsorbSunEvent(ISun sun, Phase phase) {
        this.sun = sun;
        this.phase = phase;
    }
    @Override
    public boolean isCancelable() {
        return this.phase == Phase.Start && super.isCancelable();
    }

    public static class Player extends AbsorbSunEvent {
        public final net.minecraft.world.entity.player.Player player;
        public Player(ISun sun, net.minecraft.world.entity.player.Player player, Phase phase) {
            super(sun, phase);
            this.player = player;
        }
    }
    public static class ISunAbsorber extends AbsorbSunEvent {
        public final com.hungteen.pvz.api.interfaces.ISunAbsorber sunAbsorber;
        public ISunAbsorber(ISun sun, com.hungteen.pvz.api.interfaces.ISunAbsorber sunAbsorber, Phase phase) {
            super(sun, phase);
            this.sunAbsorber = sunAbsorber;
        }
    }
    public enum Phase {
        Start, End
    }
}
