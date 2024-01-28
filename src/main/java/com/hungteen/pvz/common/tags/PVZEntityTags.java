package com.hungteen.pvz.common.tags;

import com.hungteen.pvz.PVZMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class PVZEntityTags {

    public static TagKey<EntityType<?>> PLANT = pvzTag("plant");
    public static TagKey<EntityType<?>> ZOMBIE = pvzTag("zombie");

    //definition

    public static TagKey<EntityType<?>> pvzTag(String name) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(PVZMod.MODID + ":" + name));
    }
}
