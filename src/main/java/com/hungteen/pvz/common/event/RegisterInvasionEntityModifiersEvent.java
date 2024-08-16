package com.hungteen.pvz.common.event;

import com.google.common.collect.ImmutableMap;
import com.hungteen.pvz.common.world.invasion.Invasion;
import com.hungteen.pvz.common.world.invasion.InvasionEntityModifiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import org.apache.logging.log4j.util.TriConsumer;

/**Register invasion entity modifiers. See {@link com.hungteen.pvz.common.world.invasion.InvasionType#invasionEntityModifiers InvasionType#invasionEntityModifiers}.*/
public class RegisterInvasionEntityModifiersEvent extends Event {
    public final ImmutableMap.Builder<ResourceLocation, TriConsumer<Invasion, Entity, Integer>> builder = new ImmutableMap.Builder<>();

    public RegisterInvasionEntityModifiersEvent() {
        builder.put(InvasionEntityModifiers.BABYLIZE, InvasionEntityModifiers::babylize)
                .put(InvasionEntityModifiers.ADD_LIFEBUOY, InvasionEntityModifiers::addLifeBuoy)
                .put(InvasionEntityModifiers.FINALIZE_SPAWN, InvasionEntityModifiers::finalizeSpawn);
    }
    public static ImmutableMap<ResourceLocation, TriConsumer<Invasion, Entity, Integer>> get() {
        RegisterInvasionEntityModifiersEvent event = new RegisterInvasionEntityModifiersEvent();
        MinecraftForge.EVENT_BUS.post(event);
        return event.builder.build();
    }
}
