package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.ZombieEvent;
import com.hungteen.pvz.common.world.invasion.Invasion;
import com.hungteen.pvz.util.Util;
import net.minecraft.core.Registry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.*;

import java.util.UUID;
import java.util.function.Supplier;

public class PVZZombieEvents {

    public static final ResourceKey<Registry<Class<? extends ZombieEvent>>> REGISTRY_KEY = ResourceKey.createRegistryKey(Util.prefix("zombie_events"));
    public static final DeferredRegister<Class<? extends ZombieEvent>> ZOMBIE_EVENTS = DeferredRegister.create(REGISTRY_KEY, PVZMod.MODID);
    public static final Supplier<IForgeRegistry<Class<? extends ZombieEvent>>> REGISTRY = ZOMBIE_EVENTS.makeRegistry(RegistryBuilder::new);

    //registries
    public static final RegistryObject<Class<? extends ZombieEvent>> INVASION = ZOMBIE_EVENTS.register("invasion", () -> Invasion.class);


    /**@return a {@link ZombieEvent} loaded from a CompoundTag.*/
    public static ZombieEvent fromTag(Level level, UUID uuid, CompoundTag tag) {
        ResourceLocation name = new ResourceLocation(tag.getString("event_type"));
        Class<? extends ZombieEvent> evClass = PVZZombieEvents.REGISTRY.get().getValue(name);
        if (evClass == null) {
            PVZMod.LOGGER.error("Can't find ZombieEvent" + name + "!");
            return null;
        } else {
            try {
                return evClass.getConstructor(Level.class, UUID.class, CompoundTag.class)
                        .newInstance(level, uuid, tag);
            } catch (Exception ignored) {
                PVZMod.LOGGER.error("Lacking constructor for " + name + "!");
                return null;
            }
        }
    }

    /**@return the type of a {@link ZombieEvent}.*/
    public static ResourceLocation getType(ZombieEvent event) {
        for (ResourceLocation type : PVZZombieEvents.REGISTRY.get().getKeys()) {
            if (PVZZombieEvents.REGISTRY.get().getValue(type) == event.getClass()) {
                return type;
            }
        }
        return null;
    }
}
