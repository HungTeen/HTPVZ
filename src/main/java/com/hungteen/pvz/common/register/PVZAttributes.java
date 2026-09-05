package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.common.Tags;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PVZAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTE = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, PVZMod.MODID);

    public static final RegistryObject<Attribute> MAX_SUN = ATTRIBUTE.register("max_sun", () -> (new RangedAttribute("attribute.pvz.generic.max_sun", 200D, 0D, 3000D)).setSyncable(true));
    public static final RegistryObject<Attribute> PLANT_HURT_RESISTANCE = ATTRIBUTE.register("plant_hurt_resistance", () -> (new RangedAttribute("attribute.pvz.generic.plant_hurt_resistance", 0D, 0D, 1D)));
    public static final RegistryObject<Attribute> ENEMY_ATTRACTION = ATTRIBUTE.register("enemy_attraction", () -> (new RangedAttribute("attribute.pvz.generic.enemy_attraction", 5D, 0D, 1024D)));
    public static final RegistryObject<Attribute> ENEMY_ATTRACTION_LEVEL = ATTRIBUTE.register("enemy_attraction_level", () -> (new RangedAttribute("attribute.pvz.generic.enemy_attraction_level", 4D, 0D, 16D)));


    public static void addAttributes(EntityAttributeModificationEvent ev) {
        ev.add(EntityType.PLAYER, MAX_SUN.get());
        ev.add(EntityType.PLAYER, ENEMY_ATTRACTION.get(), 6);
        ev.add(EntityType.PLAYER, ENEMY_ATTRACTION_LEVEL.get(), 4);
        ev.getTypes().forEach(entityType -> {
            if (! ev.has(entityType, PLANT_HURT_RESISTANCE.get())) ev.add(entityType, PLANT_HURT_RESISTANCE.get());
            if (entityType.is(Tags.EntityTypes.BOSSES)) ev.add(entityType, PLANT_HURT_RESISTANCE.get(), 0.8F);
        });
        ev.add(EntityType.ENDER_DRAGON, PLANT_HURT_RESISTANCE.get(), 0.75F);
        ev.add(EntityType.WITHER, PLANT_HURT_RESISTANCE.get(), 0.9F);
    }

}