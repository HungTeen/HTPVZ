package com.hungteen.pvz.common.register;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.interfaces.IArmorEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.boss.enderdragon.EnderDragon;
import net.minecraft.world.entity.player.Player;
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


    public static final DamageSource PLANT_WILT = (new DamageSource("plant_wilt")).bypassArmor();
    public static final DamageSource SPIKE_WEED = new DamageSource("spike_weed");
    public static final DamageSource NUT_COLLIDE = knockBack(new DamageSource("nut_collide"), 2F);
    public static final DamageSource TANGLE_KELP = setSharp(new DamageSource("tangle_kelp").bypassArmor());

    //TODO need a decorator for AOE damages?
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
    public static DamageSource ignoreInvTime(DamageSource source) {
        ignoreInvTimeSource = source;
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
        return sharpSource == source || storedSharpSources.contains(source);
    }

    //damageSource types
    public static DamageSource projectileDamageSource(String name, Entity projectile, Entity owner) {
        return new IndirectEntityDamageSource(name, projectile, owner instanceof LivingEntity ? owner : projectile).setProjectile();
    }
    public static DamageSource chomperHurt(LivingEntity source) {
        return teamFilter(PVZDamageSource.setSharp(DamageSource.mobAttack(source)).bypassArmor());
    }

    //variables and methods used
    private static DamageSource teamFilterSource = null;
    private static DamageSource multiplierSource = null;
    private static float multiplier = 1;
    private static DamageSource knockBackSource = null;
    private static DamageSource sharpSource = null;
    private static Entity knockBackEntity = null;
    private static float knockBackStrength = 1;
    private static DamageSource ignoreInvTimeSource = null;
    private static Set<DamageSource> storedSharpSources = Set.of(DamageSource.CACTUS);
    private static int invTime = 0;


    @SubscribeEvent
    public static void handleAttack(LivingAttackEvent ev) {
        //handle IArmorEntity
        if (ev.getEntity().getVehicle() instanceof IArmorEntity vehicle && vehicle.canRecieveDamage(ev.getSource(), ev.getAmount(), ev.getEntity())) {
            ev.setCanceled(true);
            ((Entity) vehicle).hurt(ev.getSource(), ev.getAmount());
            return;
        }
        //handle damageSource decorators.
        if (ev.getEntity() instanceof EnderDragon) {
            if (! (ev.getSource().getEntity() instanceof Player) || ! ev.getSource().isExplosion()) {
                ev.getEntity().hurt(new DamageSource(ev.getSource().msgId).setExplosion(), ev.getAmount() * 0.01F);
            }
        }
        if (ev.getSource() == teamFilterSource) {
            if (ev.getSource().getEntity() != null && isTeammate(ev.getSource().getEntity(), ev.getEntity())) {
                ev.setCanceled(true);
            }
        }
        if (ev.getSource() == knockBackSource) {
            knockBackEntity = ev.getEntity();
        }
        if (ev.getSource() == ignoreInvTimeSource && ev.getEntity() instanceof Player) {
            invTime = ev.getEntity().invulnerableTime;
            ev.getEntity().invulnerableTime = 0;
        }
    }

    @SubscribeEvent
    public static void handleHurt(LivingHurtEvent ev){
        if (ev.getSource() == ignoreInvTimeSource) {
            ev.getEntity().invulnerableTime = invTime;
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
