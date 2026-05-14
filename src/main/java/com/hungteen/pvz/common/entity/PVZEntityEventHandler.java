package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.interfaces.IDropWhenBroken;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapStats;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.ai.goal.HypnotizedTargetGoal;
import com.hungteen.pvz.common.entity.plants.Plantern;
import com.hungteen.pvz.common.network.DropDamagedArmorPacket;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.common.register.PVZMobEffects;
import com.hungteen.pvz.common.tags.PVZItemTags;
import com.hungteen.pvz.common.world.invasion.InvasionTeam;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class PVZEntityEventHandler {
    @SubscribeEvent
    public static void addGoals(EntityJoinLevelEvent ev) {
        if (ev.getEntity() instanceof Mob mob) {
            mob.goalSelector.addGoal(0, new HypnotizedTargetGoal(mob));
            if (mob instanceof WanderingTrader trader) {
                trader.goalSelector.addGoal(2, new AvoidEntityGoal<>(trader, Plantern.class, entity -> entity.level.isNight(),
                        8.0F, 0.5D, 0.5D, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test));
            }
        }
    }

    @SubscribeEvent
    public static void avoidLivingJump(LivingEvent.LivingJumpEvent event) {
        if (event.getEntity().hasEffect(PVZMobEffects.FREEZE.get())) {
            event.getEntity().setDeltaMovement(event.getEntity().getDeltaMovement().multiply(1, 0, 1));
        }
    }

    @SubscribeEvent
    public static void onLivingDie(LivingDeathEvent event) {
        if (! event.getEntity().level.isClientSide) {
            //occur invasion.
            var cap = PVZZombieEventCapability.fromLevel(event.getEntity().level);
            if (cap != null) {
                InvasionTeam team = cap.getNearestEventRanged(InvasionTeam.class, event.getEntity().blockPosition()
                        , t -> t.leaderUUID != null && t.leaderUUID.equals(event.getEntity().getUUID()));
                if (team != null) team.onLeaderDie(event.getEntity(), event.getSource());
            }
            //player lose sun.
            if (event.getEntity() instanceof Player player && player.level.getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) {
                int fall = PVZPlayerCapability.getValue(player, PVZPlayerCapStats.SUN) - 50;
                if (fall > 0) {
                    Sun.spawnSunsWithEffectsByAmount(player.level, player.getOnPos().above(), fall, 0, 0.4F);
                }
            }
        }
    }

    @SubscribeEvent
    public static void PVZShieldBlock(ShieldBlockEvent ev) {
        if (! PVZDamageSource.isVanillaShieldBlocking) return;
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
                if (ev.shieldTakesDamage()) {
                    item.hurtAndBreak(dmg, entity, (entity1) -> {
                        if (item.getItem() instanceof IDropWhenBroken item1) {
                            DropDamagedArmorPacket.drop(item1, entity.level,
                                    entity.position().add(0, 1, 0));
                        }
                        entity.broadcastBreakEvent(interactionhand);
                    });
                }
                if (ev.getDamageSource().getEntity() instanceof LivingEntity sourceEntity &&
                        sourceEntity.getMainHandItem().canDisableShield(entity.getUseItem(), entity, sourceEntity)) {
                    entity.stopUsingItem();
                }
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
