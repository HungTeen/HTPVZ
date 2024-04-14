package com.hungteen.pvz.common.world.zen_garden;

import com.mojang.serialization.Codec;
import net.minecraft.core.Registry;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.levelgen.structure.Structure;

public class ZenGardenStructures {
    Registry<Structure> registry;
    public static final Codec<ZenGardenStructures> CODEC = RegistryOps.retrieveRegistry(Registry.STRUCTURE_REGISTRY)
            .xmap(ZenGardenStructures::new, ZenGardenStructures::getRegistry).codec();

    private Registry<Structure> getRegistry() {
        return registry;
    }

    public ZenGardenStructures(Registry<Structure> registry) {
        this.registry = registry;
    }
}