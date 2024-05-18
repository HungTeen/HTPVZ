package com.hungteen.pvz.common.world;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.tags.PVZItemTags;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.level.SaplingGrowTreeEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.logging.Level;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class PVZWorldEvents {
    @SubscribeEvent
    public static void treeGrowEventHandler(SaplingGrowTreeEvent ev) {
        if (ev.getRandomSource().nextInt(6) == 0) {
            ev.getLevel().setBlock(ev.getPos().below(), PVZBlocks.ORIGIN_ORE.get().defaultBlockState(), 2);
        }
    }

    @SubscribeEvent
    public static void PVZShieldBlock(ShieldBlockEvent ev) {
        LivingEntity entity = ev.getEntity();
        if (! (entity instanceof Player)) {
            ItemStack item = entity.getUseItem();

            Vec3 dmgPos = ev.getDamageSource().getSourcePosition();
            if (dmgPos != null) {
                Vec3 facing = entity.getViewVector(1.0F).scale(1);
                Vec3 dmgDirection = dmgPos.vectorTo(entity.position()).normalize();
                dmgDirection = new Vec3(dmgDirection.x, 0.0D, dmgDirection.z).scale(1);
                if (dmgDirection.dot(facing) > -0.25D) {
                    ev.setCanceled(true);
                    return;
                }
            }
            //code below are from Player#hurtCurrentlyUsedShield(dmg).
            if (item.is(PVZItemTags.ENTITY_DAMAGEABLE_SHIELDS) && item.isDamageableItem()) {
                int dmg = 1 + Mth.floor(ev.getBlockedDamage());
                InteractionHand interactionhand = entity.getUsedItemHand();
                item.hurtAndBreak(dmg, entity, (entity1) -> entity1.broadcastBreakEvent(interactionhand));
                if (item.isEmpty()) {
                    if (interactionhand == InteractionHand.MAIN_HAND) {
                        entity.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                    } else {
                        entity.setItemSlot(EquipmentSlot.OFFHAND, ItemStack.EMPTY);
                    }

                    entity.stopUsingItem();
                    entity.playSound(SoundEvents.SHIELD_BREAK, 0.8F, 0.8F + entity.level.random.nextFloat() * 0.4F);
                }
            }
        }
    }
}
