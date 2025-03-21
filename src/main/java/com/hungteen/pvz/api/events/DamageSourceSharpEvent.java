package com.hungteen.pvz.api.events;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.EntityEvent;

/**Use this event to judge if a damage source is sharp. Sharp sources breaks wheels and balloons.
 * @apiNote not used because there is still no balloons or wheels in the game XD
 * */
public class DamageSourceSharpEvent extends EntityEvent {
    public final DamageSource source;
    public boolean result;

    public DamageSourceSharpEvent(Entity entity, DamageSource source, boolean result) {
       super(entity);
       this.source = source;
       this.result = result;
    }
}
