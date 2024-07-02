package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.world.ZombieEvent;
import com.hungteen.pvz.common.world.invasion.Invasion;
import com.hungteen.pvz.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceKey;
import net.minecraftforge.registries.*;

import java.util.function.Supplier;

public class PVZZombieEvents {

    public static final ResourceKey<Registry<Class<? extends ZombieEvent>>> REGISTRY_KEY = ResourceKey.createRegistryKey(Util.prefix("zombie_events"));
    public static final DeferredRegister<Class<? extends ZombieEvent>> ZOMBIE_EVENTS = DeferredRegister.create(REGISTRY_KEY, PVZMod.MODID);
    public static final Supplier<IForgeRegistry<Class<? extends ZombieEvent>>> REGISTRY = ZOMBIE_EVENTS.makeRegistry(RegistryBuilder::new);

    //registries
    public static final RegistryObject<Class<? extends ZombieEvent>> INVASION = ZOMBIE_EVENTS.register("invasion", () -> Invasion.class);
}
