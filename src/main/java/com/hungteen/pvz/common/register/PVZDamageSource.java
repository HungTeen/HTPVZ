package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.interfaces.IArmorEntity;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.item.ExtraHealthArmorItem;
import com.hungteen.pvz.common.tags.PVZItemTags;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.EntityDamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.common.Tags;
import net.minecraftforge.entity.PartEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.*;

import static com.hungteen.pvz.util.EntityUtil.isTeammate;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)

public class PVZDamageSource {


    /**For compatibility pvz damages are not changing any vanilla damage types but added decorations to them to meet PvZ's need.
     * <br>However, thus the damage sources decorated by PvZ damageSource decorators are unable to safely reuse due because they may be still stored in PVZDamageSource and may be affected by PvZ damage-related events. Before reuse them, call {@link PVZDamageSource#clear()}.
     * <br><br>For damage that are sharp, use {@link PVZDamageSource#staticSharpSources}.*/
    public static final DamageSource PLANT_WILT = (new DamageSource("plant_wilt")).bypassArmor();
    public static final DamageSource SPIKE_WEED = new DamageSource("spike_weed");
    public static final DamageSource FALLEN_STAR = new DamageSource("fallen_star");
    public static final DamageSource TANGLE_KELP = setSharp(new DamageSource("tangle_kelp").bypassArmor());

    //damageSource decorators
    public static DamageSource teamFilter(DamageSource source) {
        teamFilterSource = source;
        return source;
    }
    public static DamageSource knockBack(DamageSource source, float strength) {
        knockBackSource = source;
        knockBackStrength = strength;
        return source;
    }
    public static DamageSource hitBossWithProportion(DamageSource source, Entity target, float factor) {
        if (target.getType().is(Tags.EntityTypes.BOSSES) || (target instanceof PartEntity<?> part && part.getParent().getType().is(Tags.EntityTypes.BOSSES))) {
            hurtBossSource = new DamageSource(source.getMsgId()).setExplosion();
            if (source.getEntity() != null) {
                PVZEntityCapability cap = source.getEntity().getCapability(PVZEntityCapability.CAP).orElse(null);
                if (cap != null && EntityUtil.isEntityValid(cap.getOwner())) {
                    hurtBossSource = new IndirectEntityDamageSource(source.getMsgId(), source.getEntity(), cap.getOwner());
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
    public static DamageSource bypassShield(DamageSource source) {
        byPassShieldSource = source;
        return source;
    }
    public static DamageSource ignoreInvTime(DamageSource source) {
        return ignoreInvTime(source, 0);
    }
    public static DamageSource ignoreInvTime(DamageSource source, int invTimeLessThan) {
        ignoreInvTimeSource = source;
        invTime = invTimeLessThan;
        return source;
    }
    public static DamageSource multiply(DamageSource source, float multiplier) {
        multiplierSource = source;
        PVZDamageSource.multiplier = multiplier;
        return source;
    }
    public static DamageSource setSharp(DamageSource source) {
        sharpSource = source;
        return source;
    }
    public static boolean isSharp(DamageSource source) {
        return sharpSource == source || staticSharpSources.contains(source);
    }
    public static DamageSource setElectric(DamageSource source) {
        electricSource = source;
        return source;
    }
    public static boolean isElectric(DamageSource source) {
        return electricSource == source || staticElectricSources.contains(source);
    }
    public static DamageSource setNotEating(DamageSource source) {
        notEatingSource = source;
        return source;
    }
    public static boolean isEating(DamageSource source) {
        return notEatingSource != source && ! source.isExplosion();
    }



    //damageSource types
    public static DamageSource projectileDamageSource(String name, Entity projectile, Entity owner) {
        return new IndirectEntityDamageSource(name, projectile, owner instanceof LivingEntity ? owner : projectile).setProjectile();
    }
    public static DamageSource chomperHurt(LivingEntity source) {
        return teamFilter(PVZDamageSource.setSharp(DamageSource.mobAttack(source)));
    }
    public static DamageSource gargantuarCrash(LivingEntity source) {
        return setNotEating(new EntityDamageSource("gargantuar_crush", source));
    }

    public static DamageSource wallNutCollide(LivingEntity source) {
        return knockBack(new EntityDamageSource("nut_collide", source), 2F);
    }


    //variables and methods used
    private static DamageSource teamFilterSource = null;
    private static DamageSource byPassShieldSource = null;

    private static DamageSource multiplierSource = null;
    private static float multiplier = 1;

    private static DamageSource hurtBossSource = null;
    private static float bossFactor = 1;

    private static DamageSource knockBackSource = null;
    private static Entity knockBackEntity = null;
    private static float knockBackStrength = 1;

    private static DamageSource ignoreInvTimeSource = null;
    private static int invTime = 0;

    private static DamageSource sharpSource = null;
    private static DamageSource electricSource = null;

    private static DamageSource notEatingSource = null;
    public static Set<DamageSource> staticSharpSources = Set.of(DamageSource.CACTUS);
    public static Set<DamageSource> staticElectricSources = Set.of(DamageSource.LIGHTNING_BOLT);

    public static void clear() {
        sharpSource = null;
        hurtBossSource = null;
        multiplierSource = null;
        ignoreInvTimeSource = null;
        knockBackSource = null;
        teamFilterSource = null;
    }
    public static DamageSource copy(DamageSource source) {
        return source;//TODO complete this.
    }

    @SubscribeEvent
    public static void handleAttack(LivingAttackEvent ev) {
        //handle damageSource decorators.
        if (PVZDamageSource.isElectric(ev.getSource())) {
            ev.setCanceled(true);
            LivingEntity entity = ev.getEntity();
            if (entity.getUseItem().is(PVZItemTags.IRON)) {
                entity.stopUsingItem();
            }
            Map<EquipmentSlot, ItemStack> invalidItems = new HashMap<>();
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                ItemStack item = entity.getItemBySlot(slot);
                if (item.is(PVZItemTags.IRON)){
                    invalidItems.put(slot, item);
                    entity.setItemSlot(slot, ItemStack.EMPTY);
                }
            }
            if (electricSource == ev.getSource()) {
                electricSource = null;
                entity.hurt(ev.getSource(), ev.getAmount());
            } else {
                entity.hurt(new DamageSource(ev.getSource().getMsgId()), ev.getAmount()); //TODO change to copy().
            }
            for (EquipmentSlot slot : invalidItems.keySet()) {
                entity.setItemSlot(slot, invalidItems.get(slot));
            }
            return;
        }
        if (ev.getSource() == teamFilterSource) {
            if (ev.getSource().getEntity() != null && isTeammate(ev.getSource().getEntity(), ev.getEntity())) {
                ev.setCanceled(true);
            }
        }
        if (ev.getSource() == byPassShieldSource) {
            ev.getSource().bypassArmor();
        }
        if (ev.getSource() == knockBackSource) {
            knockBackEntity = ev.getEntity();
        }
        if (ev.getSource() == ignoreInvTimeSource && ! (ev.getEntity() instanceof Player)) {
            if (ev.getEntity().invulnerableTime > invTime) {
                //can not activate.
                invTime = 0;
                ignoreInvTimeSource = null;
            } else {
                invTime = ev.getEntity().invulnerableTime;
                ev.getEntity().invulnerableTime = 0;
            }
        }
    }

    @SubscribeEvent
    public static void handleHurt(LivingHurtEvent ev) {
        //handle IArmorEntity
        if (ev.getEntity().getVehicle() instanceof IArmorEntity vehicle && vehicle.canRecieveDamage(ev.getSource(), ev.getAmount(), ev.getEntity())) {
            ev.setAmount(0);
            ((Entity) vehicle).hurt(ev.getSource(), ev.getAmount());
            return;
        }
        //handle ExtraHealthArmorItem
        if (! ev.getSource().isBypassArmor() || ev.getSource() == DamageSource.FREEZE) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                if (slot.getType() == EquipmentSlot.Type.ARMOR) {
                    ItemStack stack = ev.getEntity().getItemBySlot(slot);
                    if (stack.getItem() instanceof ExtraHealthArmorItem item) {
                        item.handleHurt(ev);
                    }
                }
            }
        }
        //handle damageSource decorations
        if (ev.getEntity().getType().is(Tags.EntityTypes.BOSSES)) {
            if (ev.getSource() == hurtBossSource) {
                ev.setAmount(ev.getAmount() * bossFactor);
            }
        }
        if (ev.getSource() == byPassShieldSource) {
            ev.getSource().bypassArmor = false;
        }
        if (ev.getSource() == ignoreInvTimeSource) {
            int tmp = ev.getEntity().invulnerableTime;
            ev.getEntity().invulnerableTime = invTime;
            invTime = tmp;
        }
        if (ev.getSource() == multiplierSource) {
            ev.setAmount(ev.getAmount() * multiplier);
        }
    }
    @SubscribeEvent
    public static void handleKnockBack(LivingKnockBackEvent ev) {
        if (ev.getEntity() == knockBackEntity) {
            ev.setStrength(knockBackStrength * ev.getStrength());
            knockBackEntity = null;
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
}
