package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.tags.PVZEntityTags;
import com.hungteen.pvz.util.Util;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraftforge.common.extensions.IForgePotion;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@SuppressWarnings("all")
public class PVZMobEffects {

    public static final PVZMobEffects reflector = new PVZMobEffects();
    public static final DeferredRegister<net.minecraft.world.effect.MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, PVZMod.MODID);
    public static final DeferredRegister<Potion> POTIONS = DeferredRegister.create(ForgeRegistries.POTIONS, PVZMod.MODID);
    public static Map<String, RegistryObject<Potion>> potionMap = new HashMap<>();
    public static RegistryObject<net.minecraft.world.effect.MobEffect> handlingEffect = null;

    /** For function of brightness mobEffect, see {@link PVZPlayerCapability#tick(TickEvent.ServerTickEvent)}.
     */


    public static RegistryObject<net.minecraft.world.effect.MobEffect> BRIGHTNESS = effect("brightness", () ->
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xffffc1)
    ).registerPotion(300, true).registerPotion("long_brightness", 800, 0, true).build();
    public static RegistryObject<net.minecraft.world.effect.MobEffect> EXCITEMENT = effect("excitement", () ->
            new InstantenousMobEffect(MobEffectCategory.BENEFICIAL, 0xdddddd)
    ).build();
    public static final UUID FREEZE_EFFECT_UUID = UUID.fromString("40984c66-9786-ce36-2f2a-b9015b8e54cb");
    public static RegistryObject<net.minecraft.world.effect.MobEffect> FREEZE = effect("freeze", () ->
            new FrozenMobEffect(MobEffectCategory.HARMFUL, 0x92eae2)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, FREEZE_EFFECT_UUID.toString(), -1, AttributeModifier.Operation.MULTIPLY_TOTAL)
                    .addAttributeModifier(Attributes.JUMP_STRENGTH, FREEZE_EFFECT_UUID.toString(), -1, AttributeModifier.Operation.MULTIPLY_TOTAL)
    ).registerPotion(120).registerPotion("long_freeze", 240, 0, true).build();

    public static final UUID BUTTER_EFFECT_UUID = UUID.fromString("a8e46cba-102c-a828-4342-305ece91d14e");
    public static RegistryObject<net.minecraft.world.effect.MobEffect> BUTTER = effect("butter", () ->
            new ButterMobEffect(MobEffectCategory.HARMFUL, 0xffe054)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, BUTTER_EFFECT_UUID.toString(), -0.3F, AttributeModifier.Operation.MULTIPLY_TOTAL)
    ).registerPotion(150, false).registerPotion("long_butter", 300, 0, false).build();

    public static void addMixs() {
        PotionBrewing.addMix(Potions.AWKWARD, PVZBlocks.PLANTERN.get().asItem(), potionMap.get("brightness").get());
        PotionBrewing.addMix(potionMap.get("brightness").get(), Items.REDSTONE, potionMap.get("long_brightness").get());
        PotionBrewing.addMix(potionMap.get("butter").get(), Items.REDSTONE, potionMap.get("long_butter").get());
    }

    //methods
    public RegistryObject<net.minecraft.world.effect.MobEffect> build() {
        return handlingEffect;
    }
    public static PVZMobEffects effect(String name, Supplier<net.minecraft.world.effect.MobEffect> supplier) {
        handlingEffect = EFFECTS.register(name, supplier);
        return reflector;
    }
    public static PVZMobEffects registerPotion(String name, String potionName, int length, int strength, boolean foil) {
        RegistryObject<net.minecraft.world.effect.MobEffect> effect = handlingEffect;
        potionMap.put(name, POTIONS.register(name, () -> new Potion(potionName, new MobEffectInstance(effect.get(), length, strength)) {
            @Override
            public boolean isFoil(ItemStack stack) {
                return foil;
            };
        }));
        return reflector;
    }
    public static PVZMobEffects registerPotion(String name, int length, int strength, boolean foil) {
        return registerPotion(name, Util.name(handlingEffect), length, strength, foil);
    }
    public static PVZMobEffects registerPotion(int length, boolean foil) {
        return registerPotion(Util.name(handlingEffect), Util.name(handlingEffect), length, 0, foil);
    }
    public static PVZMobEffects registerPotion(int length) {
        return registerPotion(length, true);
    }

    public static class MobEffect extends net.minecraft.world.effect.MobEffect {
        public MobEffect(MobEffectCategory p_19451_, int p_19452_) {
            super(p_19451_, p_19452_);
        }
    }



    //effects
    public static class ButterMobEffect extends MobEffect {
        protected ButterMobEffect(MobEffectCategory p_19451_, int p_19452_) {
            super(p_19451_, p_19452_);
        }
        public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int level) {
            if (entity instanceof Mob mob) {
                mob.targetSelector.enableControlFlag(Goal.Flag.TARGET);
            }
            super.removeAttributeModifiers(entity, attributeMap, level);
        }

        public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int level) {
            if (entity instanceof Mob mob) {
                mob.targetSelector.disableControlFlag(Goal.Flag.TARGET);
                mob.setTarget(null);
                mob.getNavigation().stop();
            }
            super.addAttributeModifiers(entity, attributeMap, level);
        }
        public boolean isDurationEffectTick(int p_19455_, int p_19456_) {
            return true;
        }

        public void applyEffectTick(LivingEntity entity, int level) {
            if (entity.getType().is(PVZEntityTags.BUTTER_INVULNERABLE)) {
                entity.removeEffect(BUTTER.get());
            }
            if (entity instanceof Mob mob) {
                mob.setTarget(null);
                mob.getNavigation().stop();
                //two systems controlling gravity...
                mob.setNoGravity(false);
                if (mob instanceof FlyingMob) {
                    mob.setDeltaMovement(mob.getDeltaMovement().add(0, -0.105, 0));
                }
            }
        }
    }
    public static class FrozenMobEffect extends MobEffect {
        protected FrozenMobEffect(MobEffectCategory p_19451_, int p_19452_) {
            super(p_19451_, p_19452_);
        }
        public boolean isDurationEffectTick(int p_19455_, int p_19456_) {
            return true;
        }

        public void applyEffectTick(LivingEntity entity, int level) {
            if (! entity.canFreeze()) {
                entity.removeEffect(FREEZE.get());
            }
            entity.setTicksFrozen(350);
            if (entity.isOnFire()) {
                entity.removeEffect(FREEZE.get());
            }
        }
    }
}
