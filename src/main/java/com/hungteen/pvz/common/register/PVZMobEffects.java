package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import net.minecraft.world.effect.InstantenousMobEffect;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.FlyingAnimal;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.UUID;

public class PVZMobEffects {

    public static final DeferredRegister<net.minecraft.world.effect.MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, PVZMod.MODID);

    /** For function of brightness mobEffect, see {@link PVZPlayerCapability#tick(TickEvent.ServerTickEvent)}.
     */


    public static RegistryObject<MobEffect> BRIGHTNESS = EFFECTS.register("brightness", () ->
            new MobEffect(MobEffectCategory.BENEFICIAL, 0xffd857)
    );
    public static RegistryObject<InstantenousMobEffect> EXCITEMENT = EFFECTS.register("excitement", () ->
            new InstantenousMobEffect(MobEffectCategory.BENEFICIAL, 0xdddddd)
    );
    public static final UUID FREEZE_EFFECT_UUID = UUID.fromString("40984c66-9786-ce36-2f2a-b9015b8e54cb");
    public static RegistryObject<net.minecraft.world.effect.MobEffect> FREEZE = EFFECTS.register("freeze", () ->
            new MobEffect(MobEffectCategory.HARMFUL, 0x92eae2)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, FREEZE_EFFECT_UUID.toString(), -1, AttributeModifier.Operation.MULTIPLY_TOTAL)
                    .addAttributeModifier(Attributes.JUMP_STRENGTH, FREEZE_EFFECT_UUID.toString(), -1, AttributeModifier.Operation.MULTIPLY_TOTAL)
            // not used for now.
    );

    public static final UUID BUTTER_EFFECT_UUID = UUID.fromString("a8e46cba-102c-a828-4342-305ece91d14e");
    public static RegistryObject<net.minecraft.world.effect.MobEffect> BUTTER = EFFECTS.register("butter", () ->
            new ButterMobEffect(MobEffectCategory.HARMFUL, 0xffe054)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, BUTTER_EFFECT_UUID.toString(), -0.15F, AttributeModifier.Operation.MULTIPLY_TOTAL)
    );


    public static class MobEffect extends net.minecraft.world.effect.MobEffect {

        public MobEffect(MobEffectCategory p_19451_, int p_19452_) {
            super(p_19451_, p_19452_);
        }
    }

    public static class ButterMobEffect extends MobEffect {
        protected ButterMobEffect(MobEffectCategory p_19451_, int p_19452_) {
            super(p_19451_, p_19452_);
        }
        public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int level) {
            if(entity instanceof Mob mob) {
                mob.targetSelector.enableControlFlag(Goal.Flag.TARGET);
            }
            super.removeAttributeModifiers(entity, attributeMap, level);
        }

        public void addAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int level) {
            if(entity instanceof Mob mob) {
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
            if(entity instanceof Mob mob) {
                mob.setTarget(null);
                mob.getNavigation().stop();
                if (mob instanceof FlyingMob || mob instanceof FlyingAnimal && mob.isEffectiveAi()) {
                    mob.setDeltaMovement(mob.getDeltaMovement().add(0, -0.1, 0));
                }
            }
        }
    }
}
