package com.hungteen.pvz.api.events;

import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;

public class CheckReteamableToOwnerEvent extends EntityEvent {
    public final Entity owner;
    public boolean result;
    public CheckReteamableToOwnerEvent(Entity entity, Entity owner, Boolean result) {
        super(entity);
        this.owner = owner;
        this.result = result;
    }
}
