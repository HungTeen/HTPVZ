package com.hungteen.pvz.common.world;

import com.hungteen.pvz.PVZMod;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.ArrayList;
import java.util.List;

import static com.hungteen.pvz.common.capability.owned.PVZOwnedCapability.isTeammate;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class PVZDamageHandler {


    public static final DamageSource PLANT_WILT = (new DamageSource("plant_wilt")).bypassArmor();



    private static DamageSource sourceToHandle = null;
    public static DamageSource teamFilter(DamageSource source) {

        sourceToHandle = source;
        return source;
    }

    @SubscribeEvent
    public static void handleHurt(LivingAttackEvent ev){
        if (ev.getSource() == sourceToHandle){
            if (ev.getSource().getEntity() != null && isTeammate(ev.getSource().getEntity(), ev.getEntity())) {
                ev.setCanceled(true);
            }
        }
    }
    @SubscribeEvent
    public static void handleExplosion(ExplosionEvent.Detonate ev) {
        if (ev.getExplosion().getDamageSource() == sourceToHandle) {
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
