package com.hungteen.pvz.common.tags;

import com.hungteen.pvz.PVZMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class PVZEntityTags {

    /**Basic tags.*/
    public static TagKey<EntityType<?>> PLANT = pvzTag("plant");
    public static TagKey<EntityType<?>> ZOMBIE = pvzTag("zombie");

    /** For non-pvz entities, with this tag will it be regarded as iron. <br>
     * {@link com.hungteen.pvz.api.interfaces.IIronEntity} has the same effect and is more controllable.*/
    public static TagKey<EntityType<?>> IRON = pvzTag("iron");

    //definition

    public static TagKey<EntityType<?>> pvzTag(String name) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(PVZMod.MODID + ":" + name));
    }
}
