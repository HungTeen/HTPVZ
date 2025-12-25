package com.hungteen.pvz.common.world;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapStats;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.item.PVZShieldItem;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.common.register.PVZSeedPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraftforge.event.entity.living.MobEffectEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.registries.ForgeRegistries;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class PVZWorldEvents {
    @SubscribeEvent
    public static void treeGrowEventHandler(SaplingGrowTreeEvent ev) {
        if (ev.getRandomSource().nextInt(6) == 0) {
            ev.getLevel().setBlock(ev.getPos().below(), PVZBlocks.ORIGIN_ORE.get().defaultBlockState(), 2);
        }
    }
    @SubscribeEvent
    public static void playerDestroyShield(PlayerDestroyItemEvent ev) {
        if (ev.getHand() != null && ev.getEntity().getItemInHand(ev.getHand()).getItem() instanceof PVZShieldItem item) {
            item.clientBroken(ev.getEntity().position(), ev.getEntity().level);
        }
    }
    @SubscribeEvent
    public static void checkEffectApplicable(MobEffectEvent.Applicable event) {
        LivingEntity entity = event.getEntity();
        ResourceLocation location = ForgeRegistries.MOB_EFFECTS.getKey(event.getEffectInstance().getEffect());
        if (PVZMobEffects.unappliableMap.containsKey(location) && PVZMobEffects.unappliableMap.get(location).test(entity, event.getEffectInstance())) {
            event.setResult(Event.Result.DENY);
        }
    }

    @SubscribeEvent
    public static void playerRespawnCoolDown(PlayerEvent.PlayerRespawnEvent event) {
        if (! event.getEntity().level.isClientSide && ! event.isEndConquered()) {
            if (PVZPlayerCapability.getValue(event.getEntity(), PVZPlayerCapStats.PLANT_HAVE_CD) == 0) {
                return;
            }
            ItemCooldowns cooldowns = event.getEntity().getCooldowns();
            for (Item item : PVZSeedPackets.dataMap.keySet()) {
                if (item instanceof SeedPacketItem<?> item1) {
                    cooldowns.addCooldown(item, item1.getBaseCoolDown(null));
                }
            }
        }
    }
    //for test
//    @SubscribeEvent
//    public static void plantOnZombie(PVZPlantConditionMatchingEvent.OnEntity ev) {
//        if (ev.target instanceof Zombie) {
//            if (ev.isPlanting) {
//                ev.getEntity().startRiding(ev.target);
//            }
//            ev.result = null;
//        }
//    }
}
