package com.hungteen.pvz.common.world;

import com.hungteen.pvz.PVZMod;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.IndirectEntityDamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

import static com.hungteen.pvz.common.capability.owned.PVZOwnedCapability.isTeammate;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class PVZDamageSource {


    public static final DamageSource PLANT_WILT = (new DamageSource("plant_wilt")).bypassArmor();



    private static DamageSource teamFilterSource = null;
    private static DamageSource decreaseKnockBackSource = null;
    private static float decreaseKnockBackStrength = 1;
    public static DamageSource teamFilter(DamageSource source) {
        teamFilterSource = source;
        return source;
    }
    public static DamageSource decreaseKnockBack(DamageSource source, float strength) {
        decreaseKnockBackSource = source;
        decreaseKnockBackStrength = strength;
        return source;
    }
    public static DamageSource projectileDamageSource(String name, Entity projectile, Entity owner) {
        return new IndirectEntityDamageSource(name, projectile, owner instanceof LivingEntity ? (LivingEntity) owner : projectile).setProjectile();
    }

    @SubscribeEvent
    public static void handleHurt(LivingAttackEvent ev){
        if (ev.getSource() == teamFilterSource){
            if (ev.getSource().getEntity() != null && isTeammate(ev.getSource().getEntity(), ev.getEntity())) {
                ev.setCanceled(true);
            }
        }
        if (ev.getSource() == decreaseKnockBackSource) {
            //TODO handle knock back.
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
