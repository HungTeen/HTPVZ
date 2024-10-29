package com.hungteen.pvz.api.events;


import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.EntityEvent;

/**Judges if an entity is in sculk situation. Called both on server and client.*/
public class SculkJudgmentEvent extends EntityEvent {
    public boolean result;
    public SculkJudgmentEvent(LivingEntity entity, Boolean result) {
        super(entity);
        this.result = result;
    }
}
