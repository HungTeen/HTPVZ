package com.hungteen.pvz.common.world;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.interfaces.IArmorEntity;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.LivingKnockBackEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

import static com.hungteen.pvz.common.capability.owned.PVZOwnedCapability.isTeammate;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class PVZDamageSource {


    public static final DamageSource PLANT_WILT = (new DamageSource("plant_wilt")).bypassArmor();
    public static final DamageSource SPIKE_WEED = new DamageSource("spike_weed");

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

    //damageSource types
    public static DamageSource projectileDamageSource(String name, Entity projectile, Entity owner) {
        return new IndirectEntityDamageSource(name, projectile, owner instanceof LivingEntity ? owner : projectile).setProjectile();
    }

    //variables and methods used
    private static DamageSource teamFilterSource = null;

    private static DamageSource knockBackSource = null;
    private static Entity knockBackEntity = null;
    private static float knockBackStrength = 1;
    private static DamageSource ignoreInvTimeSource = null;
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
