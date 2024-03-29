package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.RangedAttribute;
import net.minecraftforge.event.entity.EntityAttributeModificationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class PVZAttributes {

    public static final DeferredRegister<Attribute> ATTRIBUTE = DeferredRegister.create(ForgeRegistries.ATTRIBUTES, PVZMod.MODID);

    public static final RegistryObject<Attribute> SUN = ATTRIBUTE.register("max_sun", () -> (new RangedAttribute("pvz.generic.max_sun", 200.0D, 0.0D, 1500.0D)).setSyncable(true));

    public static void addAttributes(EntityAttributeModificationEvent ev) {
        ev.add(EntityType.PLAYER, SUN.get());
    }

}