package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

public class PVZMobEffects {

    public static final DeferredRegister<net.minecraft.world.effect.MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, PVZMod.MODID);

    /** For function of this mobEffect, see {@link PVZPlayerCapability#tick()}.
     */
    public static final UUID BUTTER_EFFECT_UUID = UUID.fromString("01c75056-5e45-11eb-ae93-0242ac130002");

    public static RegistryObject<MobEffect> BRIGHTNESS = EFFECTS.register("brightness", () ->
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xffd857)
    );
    public static RegistryObject<InstantenousMobEffect> EXCITEMENT = EFFECTS.register("excitement", () ->
            new InstantenousMobEffect(MobEffectCategory.BENEFICIAL, 0xdddddd)
    );
    public static RegistryObject<MobEffect> FREEZE = EFFECTS.register("freeze", () ->
            new MobEffect(MobEffectCategory.HARMFUL, 0x92eae2)// not used for now.
    );
    public static RegistryObject<net.minecraft.world.effect.MobEffect> BUTTER = EFFECTS.register("butter", () ->
            new MobEffect(MobEffectCategory.HARMFUL, 0xfff8dc).addAttributeModifier(Attributes.MOVEMENT_SPEED,
                    BUTTER_EFFECT_UUID.toString(), -1f, AttributeModifier.Operation.MULTIPLY_TOTAL));
    public static class MobEffect extends net.minecraft.world.effect.MobEffect {

        public MobEffect(MobEffectCategory p_19451_, int p_19452_) {
            super(p_19451_, p_19452_);
        }
    }
}
