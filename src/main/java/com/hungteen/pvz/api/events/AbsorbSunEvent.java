package com.hungteen.pvz.api.events;

import com.hungteen.pvz.api.interfaces.ISun;
import com.hungteen.pvz.api.interfaces.ISunAbsorber;
import net.minecraftforge.eventbus.api.Cancelable;
import net.minecraftforge.eventbus.api.Event;

/**
 * This event is fired before and after a {@link ISun} is absorbed, using {@link Phase} to distinguish.
 * It is <b>ONLY</b> cancelable when {@link Phase} is {@link Phase#Start Start}.
 * When canceled, the sun will not be absorbed by the subject.
 * @apiNote Custom {@link ISun} should add the event manually to {@link ISun#onAbsorbedBy(com.hungteen.pvz.api.interfaces.ISunAbsorber) onAbsorbedBy(ISunAbsorber)} to ensure this event is called as expected.
 */
@Cancelable
public class AbsorbSunEvent extends Event {
    public final ISun sun;
    public final ISunAbsorber sunAbsorber;
    public final Phase phase;

    public AbsorbSunEvent(ISun sun, ISunAbsorber sunAbsorber, Phase phase) {
        this.sun = sun;
        this.sunAbsorber = sunAbsorber;
        this.phase = phase;
    }

    @Override
    public boolean isCancelable() {
        return this.phase == Phase.Start && super.isCancelable();
    }

    public enum Phase {
        Start, End
    }
}
