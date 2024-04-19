package com.hungteen.pvz.common.entity;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.entity.plants.Plantern;
import com.hungteen.pvz.common.network.PVZPacketHandler;
import com.hungteen.pvz.common.network.PlanternRefreshGlowPacket;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.ai.goal.AvoidEntityGoal;
import net.minecraft.world.entity.npc.WanderingTrader;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.entity.EntityTypeTest;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class PVZEntityEventHandler {
    @SubscribeEvent
    public static void addGoals(EntityJoinLevelEvent ev) {
        if (ev.getEntity() instanceof WanderingTrader trader) {
            trader.goalSelector.addGoal(2, new AvoidEntityGoal<>(trader, Plantern.class, entity -> entity.level.isNight(),
                    8.0F, 0.5D, 0.5D, EntitySelector.NO_CREATIVE_OR_SPECTATOR::test));
        }
    }

    @SubscribeEvent
    public static void playerTick(TickEvent.PlayerTickEvent event) {
        if (event.player instanceof ServerPlayer player && event.phase == TickEvent.Phase.END) {
            Set<UUID> set = new HashSet<>();
            List<Plantern> list = player.level.getEntities(EntityTypeTest.forClass(Plantern.class),
                    new AABB(player.getX() - 200, player.getY() - 200, player.getZ() - 200,
                            player.getX() + 200, player.getY() + 200, player.getZ() + 200),
                    (plantern) -> plantern.hasSkill("skill.pvz.plantern.light_house") && EntityUtil.isTeammate(player, plantern));
            list.forEach((plantern) -> set.add(plantern.getUUID()));
            PVZPacketHandler.sendToClient(player, new PlanternRefreshGlowPacket(set));
        }
    }
}
