package com.hungteen.pvz.common.event;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZMobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PVZMod.MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class ForgeEventSubscriber {
    @SubscribeEvent
    public static void onPotionEvent(LivingEvent.LivingTickEvent event)
    {
        LivingEntity entity = event.getEntity();
        if(entity.level.isClientSide()) return;

        if(entity.getEffect(PVZMobEffects.BUTTER.get())!=null && entity instanceof Mob mob)
        {
            mob.targetSelector.disableControlFlag(Goal.Flag.TARGET);
        }

    }
}
