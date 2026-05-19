package com.hungteen.pvz.common.event;

import com.google.common.collect.ImmutableMap;
import com.hungteen.pvz.common.world.invasion.Invasion;
import com.hungteen.pvz.common.world.invasion.InvasionEntityModifiers;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.util.TriPredicate;
import net.minecraftforge.eventbus.api.Event;
import org.jetbrains.annotations.Nullable;

/**Register invasion entity modifiers. See {@link com.hungteen.pvz.common.world.invasion.InvasionType#invasionEntityModifiers InvasionType#invasionEntityModifiers}.*/
public class RegisterInvasionEntityModifiersEvent extends Event {
    public final ImmutableMap.Builder<ResourceLocation, TriPredicate<Invasion, Entity, Integer>> builder = new ImmutableMap.Builder<>();

    public RegisterInvasionEntityModifiersEvent() {
        builder.put(InvasionEntityModifiers.BABYLIZE, InvasionEntityModifiers::babylize)
                .put(InvasionEntityModifiers.ADD_LIFEBUOY, InvasionEntityModifiers::addLifeBuoy)
                .put(InvasionEntityModifiers.FINALIZE_SPAWN, InvasionEntityModifiers::finalizeSpawn)
                .put(InvasionEntityModifiers.CHECK_SPAWN_RULES, InvasionEntityModifiers::checkSpawnRules)
                .put(InvasionEntityModifiers.WITH_FOG, InvasionEntityModifiers::withFog)
                .put(InvasionEntityModifiers.WITH_TACO, InvasionEntityModifiers::withTaco)
                .put(InvasionEntityModifiers.HOLD_RANDOM_JEWEL, InvasionEntityModifiers::holdRandomJewel)
                .put(InvasionEntityModifiers.POWER_JACK_IN_A_BOX_ZOMBIE, InvasionEntityModifiers::powerJackInABoxZombie)
                .put(InvasionEntityModifiers.HOLD_RANDOM_MATERIAL, InvasionEntityModifiers::holdRandomMaterial);
    }
    public static ImmutableMap<ResourceLocation, TriPredicate<@Nullable Invasion, Entity, Integer>> get() {
        RegisterInvasionEntityModifiersEvent event = new RegisterInvasionEntityModifiersEvent();
        MinecraftForge.EVENT_BUS.post(event);
        return event.builder.build();
    }
}
