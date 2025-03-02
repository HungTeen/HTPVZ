package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.tags.PVZEntityTags;
import com.hungteen.pvz.common.world.invasion.Invasion;
import com.hungteen.pvz.common.world.invasion.InvasionType;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.*;
import net.minecraft.world.entity.FlyingMob;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.*;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.memory.MemoryModuleType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionBrewing;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.scores.PlayerTeam;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.*;
import java.util.function.Supplier;

@SuppressWarnings("all")
@Mod.EventBusSubscriber(modid = PVZMod.MODID)
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
                    .addAttributeModifier(Attributes.ATTACK_SPEED, FREEZE_EFFECT_UUID.toString(), -1, AttributeModifier.Operation.MULTIPLY_TOTAL)
    ).registerPotion(120).registerPotion("long_freeze", 240, 0, true).build();

    public static final UUID BUTTER_EFFECT_UUID = UUID.fromString("a8e46cba-102c-a828-4342-305ece91d14e");
    public static RegistryObject<net.minecraft.world.effect.MobEffect> BUTTER = effect("butter", () ->
            new ButterMobEffect(MobEffectCategory.HARMFUL, 0xffe054)
                    .addAttributeModifier(Attributes.MOVEMENT_SPEED, BUTTER_EFFECT_UUID.toString(), -0.3F, AttributeModifier.Operation.MULTIPLY_TOTAL)
    ).registerPotion(150, false).registerPotion("long_butter", 300, 0, false).build();

    public static final RegistryObject<net.minecraft.world.effect.MobEffect> PHYTOTOXIN = effect("phytotoxin", () ->
            new PhytoToxinEffect(MobEffectCategory.HARMFUL, 5149489/*Same as poison effect*/))
            .registerPotion(400)
            .registerPotion("long_phytotoxin", 1000, 0, true)
            .registerPotion("strong_phytotoxin", 400, 1, true).build();

    public static final RegistryObject<net.minecraft.world.effect.MobEffect> INVASION_OMEN = effect("invasion_omen", () ->
            new InvasionOmenEffect(MobEffectCategory.HARMFUL, 0x2d4a3e)).build();

    /**To use this effect, use {@link PVZMobEffects#hypnotizeWithTeam(LivingEntity, String, int)} for conveinence.*/
    public static final UUID HYPNOTIZED_EFFECT_UUID = UUID.fromString("8ee427fa-6f9d-2aa5-6d52-76b37472bfc1");
    public static final RegistryObject<net.minecraft.world.effect.MobEffect> HYPNOTISED = effect("hypnotized", () ->
            new HypnotisedEffect(MobEffectCategory.HARMFUL, 0xff9dc0)
                    .addAttributeModifier(Attributes.ARMOR, HYPNOTIZED_EFFECT_UUID.toString(), 2F, AttributeModifier.Operation.ADDITION))
            .build();

    public static void addMixs() {
        PotionBrewing.addMix(Potions.AWKWARD, PVZBlocks.PLANTERN.get().asItem(), potionMap.get("brightness").get());
        PotionBrewing.addMix(Potions.AWKWARD, Items.POISONOUS_POTATO, potionMap.get("phytotoxin").get());
        PotionBrewing.addMix(potionMap.get("brightness").get(), Items.REDSTONE, potionMap.get("long_brightness").get());
        PotionBrewing.addMix(potionMap.get("butter").get(), Items.REDSTONE, potionMap.get("long_butter").get());
        PotionBrewing.addMix(potionMap.get("phytotoxin").get(), Items.REDSTONE, potionMap.get("long_phytotoxin").get());
        PotionBrewing.addMix(potionMap.get("phytotoxin").get(), Items.GLOWSTONE_DUST, potionMap.get("strong_phytotoxin").get());
    }

    @SubscribeEvent
    public static void checkEffectAppliable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        net.minecraft.world.effect.MobEffect effect = event.getEffectInstance().getEffect();
        if (effect instanceof ButterMobEffect && entity.getType().is(PVZEntityTags.BUTTER_INVULNERABLE)) {
            event.setResult(Event.Result.DENY);
        }
        if (effect instanceof PhytoToxinEffect && entity.getType().is(PVZEntityTags.PLANT)) {
            event.setResult(Event.Result.DENY);
        }
        if (effect instanceof FrozenMobEffect && ! entity.canFreeze()) {
            event.setResult(Event.Result.DENY);
        }
        if (effect instanceof HypnotisedEffect && entity.getType().is(PVZEntityTags.HYPNOTISED_INVULNERABLE)) {
            event.setResult(Event.Result.DENY);
        }
    }


    //methods
    public static void hypnotizeWithTeam(LivingEntity target, String teamName, int length) {
        target.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> {
            cap.hypnosisTempTeam = teamName;
        });
        target.addEffect(new MobEffectInstance(PVZMobEffects.HYPNOTISED.get(), length));
    }
    public static void hypnotizeWithTeam(LivingEntity target, LivingEntity hypnotizer, int length) {
        target.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> {
            cap.hypnosisTempTeam = hypnotizer.getTeam() == null ? null : hypnotizer.getTeam().getName();
        });
        target.addEffect(new MobEffectInstance(PVZMobEffects.HYPNOTISED.get(), length));
    }


    //
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
            if (entity instanceof Mob mob) {
                mob.setTarget(null);
                mob.getNavigation().stop();
                //two systems controlling gravity...
                mob.setNoGravity(false);
                if (mob instanceof FlyingMob) {
                    mob.setDeltaMovement(mob.getDeltaMovement().add(0, -0.105, 0));
                }
                //reset attack target.
                try {
                    Optional<LivingEntity> opt = entity.getBrain().getMemory(MemoryModuleType.ATTACK_TARGET);
                    if (opt.isEmpty() || EntityUtil.isTeammate(entity, opt.get())) {
                        entity.getBrain().setMemory(MemoryModuleType.ATTACK_TARGET, Optional.empty());
                        entity.getBrain().setMemory(MemoryModuleType.ANGRY_AT, Optional.empty());
                        entity.getBrain().setMemory(MemoryModuleType.HURT_BY_ENTITY, Optional.empty());
                    }
                } catch (Exception ignored) {
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
            entity.setRemainingFireTicks(0);
            entity.setTicksFrozen(350);
            if (entity.isOnFire()) {
                entity.removeEffect(FREEZE.get());
            }
        }
    }
    public static class PhytoToxinEffect extends MobEffect {
        protected PhytoToxinEffect(MobEffectCategory p_19451_, int p_19452_) {
            super(p_19451_, p_19452_);
        }

        public void applyEffectTick(LivingEntity entity, int level) {
            if (entity.getType().is(PVZEntityTags.PLANT)) {
                entity.removeEffect(PHYTOTOXIN.get());
                return;
            }
            if (entity.getHealth() > 1.0F) {
                entity.hurt(DamageSource.MAGIC, 1.0F);
            }
        }

        public boolean isDurationEffectTick(int p_19455_, int p_19456_) {
            int j = 25 >> p_19456_;
            if (j > 0) {
                return p_19455_ % j == 0;
            } else {
                return true;
            }
        }
    }
    public static class InvasionOmenEffect extends MobEffect {
        static LivingEntity removed = null;

        public InvasionOmenEffect(MobEffectCategory p_19451_, int p_19452_) {
            super(p_19451_, p_19452_);
        }

        public void applyEffectTick(LivingEntity entity, int amplifier) {
            if (! entity.level.isClientSide && entity != removed && entity instanceof Player) {
                List<InvasionType> types = InvasionType.generateTypes(entity);
                if (entity instanceof ServerPlayer player) {
                    if ((types.isEmpty() || ! Invasion.canHappenInvasion(entity))) {
                        player.displayClientMessage(Component.translatable("hint.pvz.invasion.no_available_invasion"), true);
                    } else {
                        entity.level.getCapability(PVZZombieEventCapability.CAP).ifPresent(cap -> {
                            cap.addEvent(new Invasion(entity.level, types, entity, entity.blockPosition(), Math.min(11, Math.max(1, amplifier))));
                        });
                    }
                }
            }
        }

        public boolean isDurationEffectTick(int duration, int amplifier) {
            return duration == 1;
        }
    }
    /**About how Hypnotzed effect work, see {@link com.hungteen.pvz.common.entity.ai.goal.HypnotizedTargetGoal HypnotizedTargetGoal}.*/
    public static class HypnotisedEffect extends MobEffect {

        public HypnotisedEffect(MobEffectCategory p_19451_, int p_19452_) {
            super(p_19451_, p_19452_);
        }
        public void addAttributeModifiers(LivingEntity entity, AttributeMap map, int amplifier) {
            super.addAttributeModifiers(entity, map, amplifier);
            final PlayerTeam friendlyTeam = entity.level.getScoreboard().getPlayerTeam(PVZMod.FRIENDLY_TEAM);
            entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> {
                if (entity instanceof Player) {
                    return;
                }
                String tmp = cap.hypnosisTempTeam;
                cap.hypnosisTempTeam = entity.getTeam() == null ? null : entity.getTeam().getName();
                entity.level.getScoreboard().addPlayerToTeam(entity.getScoreboardName(),
                        tmp == null ? friendlyTeam : entity.level.getScoreboard().getPlayerTeam(tmp));
            });
        }
        public void removeAttributeModifiers(LivingEntity entity, AttributeMap attributeMap, int amplifier) {
            super.removeAttributeModifiers(entity, attributeMap, amplifier);
            entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> {
                if (entity instanceof Player) {
                    return;
                }
                if (cap.hypnosisTempTeam == null) {
                    entity.level.getScoreboard().removePlayerFromTeam(entity.getScoreboardName());
                } else {
                    entity.level.getScoreboard().addPlayerToTeam(entity.getScoreboardName(), entity.level.getScoreboard().getPlayerTeam(cap.hypnosisTempTeam));
                }
                cap.hypnosisTempTeam = null;
            });
        }
        public boolean isDurationEffectTick(int p_19455_, int p_19456_) {
            return true;
        }

        public void applyEffectTick(LivingEntity entity, int level) {
            if (entity instanceof LocalPlayer player && player.portalTime <= 0) {
                player.portalTime = player.getRandom().nextFloat() / 2;
                player.oPortalTime = player.getRandom().nextFloat() / 2;
            }
        }
    }
}
