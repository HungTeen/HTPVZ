package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZConfig;
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

    public static final RegistryObject<Attribute> SUN = ATTRIBUTE.register("max_sun", () -> (new RangedAttribute("pvz.generic.max_sun", 200D, 0D, 3000D)).setSyncable(true));

    public static void addAttributes(EntityAttributeModificationEvent ev) {
        ev.add(EntityType.PLAYER, SUN.get());
    }

}