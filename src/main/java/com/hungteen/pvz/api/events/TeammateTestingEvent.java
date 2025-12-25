package com.hungteen.pvz.api.events;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.eventbus.api.Event;

/**Fired when {@link com.hungteen.pvz.util.EntityUtil#isTeammate(Entity, Entity) isTeammate(A, B)} get a result.*/
public class TeammateTestingEvent extends Event {
    public final Entity A;
    public final Entity B;
    public boolean currentResult;
    public final boolean forCombat;
    public TeammateTestingEvent(Entity A, Entity B, boolean currentResult, boolean forCombat) {
        this.A = A;
        this.B = B;
        this.currentResult = currentResult;
        this.forCombat = forCombat;
    }
}
