package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.events.DamageSourceSharpEvent;
import com.hungteen.pvz.api.interfaces.IArmorEntity;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.entity.Sun;
import com.hungteen.pvz.common.item.ExtraHealthArmorItem;
import com.hungteen.pvz.common.tags.PVZItemTags;
import com.hungteen.pvz.common.world.zen_garden.ZenGardenTeleporter;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.Tags;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static com.hungteen.pvz.util.EntityUtil.isTeammate;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)

public class PVZDamageSource {


    /**For compatibility pvz damages are not changing any vanilla damage types but added decorations to them to meet PvZ's need.
     * <br>However, thus the damage sources decorated by PvZ damageSource decorators are unable to safely reuse
     * because they may be still stored in PVZDamageSource and may be affected by PvZ damage-related events.
     * Before reusing them, call {@link PVZDamageSource#clear()}.
     * */
    public static final DamageSource PLANT_WILT = (new DamageSource("plant_wilt")).bypassArmor();
    public static final DamageSource FALLEN_STAR = new DamageSource("fallen_star");
    public static final DamageSource SILVER_SWORD = new DamageSource("silver_sword");

    public static ShieldBlockEvent onShieldBlock(LivingEntity blocker, DamageSource source, float blocked, boolean isShieldItemBlocking) {
        isVanillaShieldBlocking = isShieldItemBlocking;
        ShieldBlockEvent result = ForgeHooks.onShieldBlock(blocker, source, blocked);
        isVanillaShieldBlocking = true;
        return result;
    }
    //damageSource types
    public static DamageSource projectileDamageSource(String name, Entity projectile, Entity owner) {
        return new OwnedIndirectDamageSource(name, projectile, owner instanceof LivingEntity ? owner : projectile).setProjectile();
    }
    public static DamageSource wallNutCollide(LivingEntity source, Entity target) {
        return hitBossWithProportion(knockBack(new OwnedDamageSource("nut_collide", source), 2F), target);
    }
    public static DamageSource tallNutThornsDamage(LivingEntity source, Entity target) {
        return hitBossWithProportion(DamageSource.thorns(source), target);
    }
    public static DamageSource chomperHurt(LivingEntity source, Entity target) {
        return hitBossWithProportion(teamFilter(setSharp(owned("eaten", source))), target);
    }
    public static DamageSource spikeWeedHurt(LivingEntity source, Entity target) {
        return hitBossWithProportion(setSharp(anoOwned("spike_weed", source)), target);
    }
    public static DamageSource gargantuarCrash(LivingEntity source) {
        return setNotEating(new EntityDamageSource("crush", source));
    }
    public static DamageSource tangleKelpHurt(LivingEntity source, Entity target) {
        return hitBossWithProportion(anoOwned("tangle_kelp", source).bypassArmor(), target);
    }

    public static DamageSource owned(String name, LivingEntity source) {
        return new OwnedDamageSource(name, source);
    }

    public static DamageSource anoOwned(String name, LivingEntity source) {
        return new AnonymousOwnedDamageSource(name, source);
    }

    //damageSource decorators
    /**With this decorator, the damage source will not apply on teammates. Used for explosion.*/
    public static DamageSource teamFilter(DamageSource source) {
        teamFilterSource = source;
        return source;
    }
    /**Transfer the killer to another entity as the mob is killed.*/
    public static DamageSource transferKiller(DamageSource source, Entity entity) {
        if (! EntityUtil.isEntityValid(entity)) {
            return source;
        }
        transferEntitySource = source;
        transferredEntity = entity;
        return source;
    }
    /**Set the strength of knocking back of the damage source. Compared with vanilla.*/
    public static DamageSource knockBack(DamageSource source, float strength) {
        knockBackSource = source;
        knockBackStrength = strength;
        return source;
    }
    /**Set the rate of damage apply on bosses and make the damage available for ender dragons. To avoid dealing too much damage that instantly kill bosses.*/
    public static DamageSource hitBossWithProportion(DamageSource source, Entity target) {
        return hitBossWithProportion(source, target, 1 - EntityUtil.getPlantDamageResistance(target));
    }
    public static DamageSource hitBossWithProportion(DamageSource source, Entity target, float factor) {
        if (target.getType().is(Tags.EntityTypes.BOSSES) || (target instanceof PartEntity<?> part && part.getParent().getType().is(Tags.EntityTypes.BOSSES))) {
            hurtBossSource = new DamageSource(source.getMsgId()).setExplosion();
            if (source.getEntity() != null) {
                Entity owner = PVZEntityCapability.getOwner(source.getEntity());
                if (EntityUtil.isEntityValid(PVZEntityCapability.getOwner(source.getEntity()))) {
                    hurtBossSource = new IndirectEntityDamageSource(source.getMsgId(), source.getEntity(), owner);
                }
            }
            if (source.isProjectile()) {
                hurtBossSource.setProjectile();
            }
            //TODO change to copy();
            bossFactor = factor;
            return hurtBossSource;
        }
        return source;
    }
    /**Allow the damage to go pass shields without ignoring the armor. For plants like Fume-Shrooms.*/
    public static DamageSource bypassShield(DamageSource source) {
        byPassShieldSource = source;
        return source;
    }
    public static boolean isBypassShield(DamageSource source) {
        return source == byPassShieldSource;
    }
    /**Ignore invaluable time.*/
    public static DamageSource ignoreInvTime(DamageSource source) {
        return ignoreInvTime(source, 20);
    }
    public static DamageSource ignoreInvTime(DamageSource source, int invTimeLessThan) {
        ignoreInvTimeSource = source;
        PVZDamageSource.invTimeLessThan = invTimeLessThan;
        return source;
    }
    /**Damage multiplier. Used for explosion.*/
    public static DamageSource multiply(DamageSource source, float multiplier) {
        multiplierSource = source;
        PVZDamageSource.multiplier = multiplier;
        return source;
    }
    /**Set the damage source sharp, so it can break wheels and balloons.*/
    public static DamageSource setSharp(DamageSource source) {
        if (source instanceof EntityDamageSource source1) {
            source1.setThorns();
        } else {
            sharpSource = source;
        }
        return source;
    }
    public static boolean isSharp(DamageSource source, Entity target) {
        boolean flag = sharpSource == source || source == DamageSource.CACTUS || source == PVZDamageSource.SILVER_SWORD;
        DamageSourceSharpEvent event = new DamageSourceSharpEvent(target, source, flag);
        MinecraftForge.EVENT_BUS.post(event);
        flag = event.result;
        return flag;
    }
    /**Set the damage source electric, so it will ignore armor tagged made with iron material.*/
    public static DamageSource setElectric(DamageSource source) {
        electricSource = source;
        return source;
    }
    public static boolean isElectric(DamageSource source) {
        return electricSource == source || staticElectricSources.contains(source);
    }
    /**Set direct damage source not eating to avoid attacker hypnotized by Hypno-Shrooms unexpectedly.*/
    public static DamageSource setNotEating(DamageSource source) {
        notEatingSource = source;
        return source;
    }
    public static boolean isEating(DamageSource source) {
        return notEatingSource != source && ! source.isExplosion();
    }
    public static DamageSource setInterrupting(DamageSource source) {
        interruptingSource = source;
        return source;
    }


    //variables and methods used
    public static DamageSource teamFilterSource = null;
    public static DamageSource byPassShieldSource = null;

    public static DamageSource multiplierSource = null;
    private static float multiplier = 1;

    private static DamageSource hurtBossSource = null;
    private static float bossFactor = 1;

    public static DamageSource knockBackSource = null;
    private static Entity knockBackEntity = null;
    private static Vec3 preKnockBackDeltaMovement = null;
    private static float knockBackStrength = 1;

    public static DamageSource ignoreInvTimeSource = null;
    public static int invTimeLessThan = 0;
    private static int invTimeTmp = 0;

    public static DamageSource sharpSource = null;
    public static DamageSource electricSource = null;
    public static DamageSource notEatingSource = null;
    public static Set<DamageSource> staticElectricSources = Set.of(DamageSource.LIGHTNING_BOLT);
    public static DamageSource interruptingSource = null;

    public static DamageSource transferEntitySource = null;
    public static Entity transferredEntity = null;

    public static boolean isVanillaShieldBlocking = true;



    public static void clear() {
        sharpSource = null;
        electricSource = null;
        notEatingSource = null;
        hurtBossSource = null;
        multiplierSource = null;
        ignoreInvTimeSource = null;
        knockBackSource = null;
        teamFilterSource = null;
        byPassShieldSource = null;
    }
    public static DamageSource copy(DamageSource source) {
        return source;//TODO complete this.
    }

    @SubscribeEvent
    public static void handleAttack(LivingAttackEvent ev) {
        //handle damageSource decorators.
        Entity attacker = ev.getSource().getEntity();
        LivingEntity target = ev.getEntity();
        if (ev.getSource().isFire()) {
            target.setTicksFrozen(0);
        }
        if (PVZDamageSource.isElectric(ev.getSource())) {
            ev.setCanceled(true);
            if (target.getUseItem().is(PVZItemTags.IRON)) {
                target.stopUsingItem();
            }
            Map<EquipmentSlot, ItemStack> invalidItems = new HashMap<>();
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack item = target.getItemBySlot(slot);
                if (item.is(PVZItemTags.IRON)){
                    invalidItems.put(slot, item);
                    target.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
            if (electricSource == ev.getSource()) {
                electricSource = null;
                target.hurt(ev.getSource(), ev.getAmount());
            } else {
                target.hurt(new DamageSource(ev.getSource().getMsgId()), ev.getAmount()); //TODO change to copy().
            }
            for (EquipmentSlot slot : invalidItems.keySet()) {
                target.setItemSlot(slot, invalidItems.get(slot));
            }
            return;
        }
        if (ev.getSource() == interruptingSource) {
            if (target.getUseItem().is(PVZItemTags.IRON)) {
                target.stopUsingItem();
            }
        }
        if (ev.getSource() == teamFilterSource) {
            if (attacker != null && isTeammate(attacker, target)) {
                ev.setCanceled(true);
            }
        }
        if (ev.getSource() == knockBackSource) {
            knockBackEntity = target;
            if (ev.getSource().isExplosion()) {
                preKnockBackDeltaMovement = target.getDeltaMovement();
            }
        }
        if (ev.getSource() == ignoreInvTimeSource) {
            if (target.invulnerableTime <= (target instanceof Player ? Math.min(15, invTimeLessThan) : invTimeLessThan)) {
                invTimeTmp = target.invulnerableTime;
                target.invulnerableTime = 0;
            }
        }
    }

    @SubscribeEvent
    public static void handleHurt(LivingHurtEvent ev) {
        LivingEntity entity = ev.getEntity();
        //handle ZenGarden fallout
        if (entity instanceof ServerPlayer player && ev.getSource() == DamageSource.OUT_OF_WORLD
                && entity.getLevel().dimension().location().equals(PVZDimensions.ZEN_GARDEN)) {
            ServerLevel destWorld = entity.getLevel().getServer().getLevel(
                    player.getRespawnDimension().equals(ZenGardenTeleporter.GARDEN) ? Level.OVERWORLD : player.getRespawnDimension());
            if (destWorld != null) {
                ev.setCanceled(true);
                player.setPortalCooldown();
                player.resetFallDistance();
                player.changeDimension(destWorld, new ZenGardenTeleporter(destWorld));
                player.removeEffect(PVZMobEffects.DISTANCE_EFFECT.get());
                return;
            }
        }
        //owned plant attack regard as player attack
        Entity attacker = ev.getSource() instanceof IndirectEntityDamageSource indSource
                ? indSource.owner : ev.getSource().getEntity();
        if (attacker != null && PVZEntityCapability.getOwner(attacker) instanceof Player player) {
            entity.setLastHurtByPlayer(player);
        }
        //sun blood effect
        if (entity.hasEffect(PVZMobEffects.SUN_BLOOD.get())) {
            double amplifier = entity.getEffect(PVZMobEffects.SUN_BLOOD.get()).getAmplifier() * 0.3 + 1;
            Sun.spawnSunWithEffects(entity.level, (int) (Math.max(5, ev.getAmount() * 3) * amplifier)
                    , entity.blockPosition().offset(0, entity.getEyeHeight(), 0), 0.2F);
        }
        //handle IArmorEntity and ExtraHealthArmorItem
        if (! ev.getSource().isBypassArmor() || ev.getSource() == DamageSource.FREEZE || isBypassShield(ev.getSource())) {
            if (entity.getVehicle() instanceof IArmorEntity vehicle && vehicle.canRecieveDamage(ev.getSource(), ev.getAmount(), entity)) {
                ShieldBlockEvent blockEvent = PVZDamageSource.onShieldBlock(entity, ev.getSource(), ev.getAmount(), false);
                if (! blockEvent.isCanceled()) {
                    var blocked = blockEvent.getBlockedDamage();
                    ((Entity) vehicle).hurt(ev.getSource(), ev.getAmount());
                    ev.setAmount(ev.getAmount() - blocked);
                    if (ev.getAmount() <= 0) {
                        return;
                    }
                }
            }
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                    ItemStack stack = entity.getItemBySlot(slot);
                    if (stack.getItem() instanceof ExtraHealthArmorItem item) {
                        item.handleHurt(ev);
                    }
                }
            }
        }
        //handle damageSource decorations
        if (entity.getType().is(Tags.EntityTypes.BOSSES)) {
            if (ev.getSource() == hurtBossSource) {
                ev.setAmount(ev.getAmount() * bossFactor);
            }
        }
        if (ev.getSource() == ignoreInvTimeSource && invTimeTmp > 0) {
            entity.invulnerableTime = invTimeTmp;
        }
        if (ev.getSource() == multiplierSource) {
            ev.setAmount(ev.getAmount() * multiplier);
        }
    }
    @SubscribeEvent
    public static void handleShield(ShieldBlockEvent ev) {
        if (isBypassShield(ev.getDamageSource())) {
            ev.setCanceled(true);
        }
    }
    @SubscribeEvent
    public static void handleKnockBack(LivingKnockBackEvent ev) {
        if (ev.getEntity() == knockBackEntity) {
            ev.setStrength(knockBackStrength * ev.getStrength());
            knockBackEntity = null;
            preKnockBackDeltaMovement = null;
        }
    }
    @SubscribeEvent
    public static void handleExplosion(ExplosionEvent.Detonate ev) {
        if (ev.getExplosion().getDamageSource() == teamFilterSource) {
            if (ev.getExplosion().getDamageSource().getEntity() != null) {
                List<Entity> list = new ArrayList<>();
                ev.getAffectedEntities().forEach((entity) -> {
                    if (isTeammate(ev.getExplosion().getDamageSource().getEntity(), entity)) {
                        list.add(entity);
                    }
                });
                list.forEach((entity) -> ev.getAffectedEntities().remove(entity));
            }
        }
    }
    @SubscribeEvent
    public static void handleDeath(LivingDeathEvent event) {
        DamageSource source = event.getSource();
        if (source == transferEntitySource && EntityUtil.isEntityValid(transferredEntity)) {
            if (transferredEntity instanceof Player player) {
                event.getEntity().setLastHurtByPlayer(player);
            }
            if (source instanceof IndirectEntityDamageSource source1) {
                source1.owner = transferredEntity;
            } else if (source instanceof EntityDamageSource source1) {
                source1.entity = transferredEntity;
            }
            //TODO solve the problem that killer get mixed.
        }
    }

    public static class OwnedDamageSource extends EntityDamageSource {

        public OwnedDamageSource(String p_19394_, Entity p_19395_) {
            super(p_19394_, p_19395_);
        }

        @Override
        public Component getLocalizedDeathMessage(LivingEntity killed) {
            LivingEntity livingentity = killed.getKillCredit();
            if (livingentity != null) {
                String s = "death.attack" + (this.msgId.isEmpty() ? "" : "." + msgId) + ".owned";
                Entity owner = PVZEntityCapability.getOwner(livingentity);
                return EntityUtil.isEntityValid(owner) ?
                        Component.translatable(s, killed.getDisplayName(), livingentity.getDisplayName(), owner.getDisplayName()) : super.getLocalizedDeathMessage(killed);
            } else {
                return super.getLocalizedDeathMessage(killed);
            }
        }
    }

    public static class AnonymousOwnedDamageSource extends DamageSource {
        public Entity entity;

        public AnonymousOwnedDamageSource(String p_19394_, Entity p_19395_) {
            super(p_19394_);
            this.entity = p_19395_;
        }

        @Override
        public Component getLocalizedDeathMessage(LivingEntity killed) {
            if (entity != null) {
                String s = "death.attack" + (this.msgId.isEmpty() ? "" : "." + msgId) + ".owned";
                Entity owner = PVZEntityCapability.getOwner(entity);
                return EntityUtil.isEntityValid(owner) ?
                        Component.translatable(s, killed.getDisplayName(), entity.getDisplayName(), owner.getDisplayName()) : super.getLocalizedDeathMessage(killed);
            } else {
                return super.getLocalizedDeathMessage(killed);
            }
        }
    }

    public static class OwnedIndirectDamageSource extends IndirectEntityDamageSource {

        public OwnedIndirectDamageSource(String p_19406_, Entity p_19407_, @Nullable Entity p_19408_) {
            super(p_19406_, p_19407_, p_19408_);
        }

        @Override
        public Component getLocalizedDeathMessage(LivingEntity killed) {
            LivingEntity livingentity = killed.getKillCredit();
            if (livingentity != null) {
                Entity owner = PVZEntityCapability.getOwner(livingentity);
                if (EntityUtil.isEntityValid(owner)) {
                    Component killerName = this.owner == null ? this.entity.getDisplayName() : this.owner.getDisplayName();
                    ItemStack itemstack = this.owner instanceof LivingEntity ? ((LivingEntity)this.owner).getMainHandItem() : ItemStack.EMPTY;
                    String s = "death.attack" + (this.msgId.isEmpty() ? "" : "." + this.msgId);
                    String s1 = s + ".item";
                    s += ".owned";
                    s1 += ".owned";
                    return !itemstack.isEmpty() && itemstack.hasCustomHoverName() ?
                            Component.translatable(s1, killed.getDisplayName(), killerName, owner.getDisplayName(), itemstack.getDisplayName()) :
                            Component.translatable(s, killed.getDisplayName(), killerName, owner.getDisplayName());
                }
            }
            return super.getLocalizedDeathMessage(killed);
        }
    }
}
