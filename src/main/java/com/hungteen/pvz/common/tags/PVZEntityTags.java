package com.hungteen.pvz.common.tags;

import com.hungteen.pvz.PVZMod;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.EntityType;

public class PVZEntityTags {

    /**Basic tags.*/
    public static TagKey<EntityType<?>> PLANT = pvzTag("pvz_plant");
    public static TagKey<EntityType<?>> ZOMBIE = pvzTag("pvz_zombie");
    /**By adding this tag, mobs will be considered as {@link net.minecraft.world.entity.monster.Enemy} by pvz mobs. Attention that plants <b>WON'T</b> target non-livings. <br>This tag will be covered by {@link PVZEntityTags#FRIENDLY pvz:pvz_friendly}.*/
    public static TagKey<EntityType<?>> ENEMY = pvzTag("pvz_enemy");
    /**By adding this tag, mobs <b>WON'T</b> be considered as {@link net.minecraft.world.entity.monster.Enemy} by pvz mobs. <br>This tag will cover {@link PVZEntityTags#ENEMY pvz:pvz_enemy}.*/
    public static TagKey<EntityType<?>> FRIENDLY = pvzTag("pvz_friendly");

    /** For non-pvz entities, with this tag will it be regarded as iron. <br>
     * {@link com.hungteen.pvz.api.interfaces.IIronEntity} has the same effect and is more controllable.*/
    public static TagKey<EntityType<?>> IRON = pvzTag("iron");
    /**Entities with this tag cannot be planted on Lily Pads, but still can be planted on Flower Pots.*/
    public static TagKey<EntityType<?>> MUST_PLANT_IN_DIRT = pvzTag("must_plant_in_dirt");
    /**With this tag entity will not be affected by butter effect.*/
    public static TagKey<EntityType<?>> BUTTER_INVULNERABLE = pvzTag("butter_invulnerable");
    /**With this tag entity will not be affected by hypnotised effect.*/
    public static TagKey<EntityType<?>> HYPNOTISED_INVULNERABLE = pvzTag("hypnotised_invulnerable");

    //definition

    public static TagKey<EntityType<?>> pvzTag(String name) {
        return TagKey.create(Registry.ENTITY_TYPE_REGISTRY, new ResourceLocation(PVZMod.MODID + ":" + name));
    }
}
