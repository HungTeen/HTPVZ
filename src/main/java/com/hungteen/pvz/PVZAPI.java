package com.hungteen.pvz;

import com.hungteen.pvz.api.ZombieEvent;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapStats;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.network.ZombieEventPacket;
import com.hungteen.pvz.common.register.PVZAttributes;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.common.register.PVZZombieEvents;
import com.hungteen.pvz.common.world.invasion.Invasion;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.Nullable;

public class PVZAPI implements com.hungteen.pvz.api.PVZAPI.IPVZAPI {
    @Override
    public String getSunResourceName() {
        return PVZPlayerCapStats.SUN;
    }
    @Override
    public String getInvasionThreatResourceName() {
        return Invasion.INVASION_THREAT;
    }
    @Override
    public int getSun(Player player) {
        return PVZPlayerCapability.getValue(player, getSunResourceName());
    }
    @Override
    public boolean plantHaveCost(Player player) {
        return PVZPlayerCapability.getValue(player, PVZPlayerCapStats.PLANT_HAVE_COST) == 1;
    }
    @Override
    public boolean plantHaveCD(Player player) {
        return PVZPlayerCapability.getValue(player, PVZPlayerCapStats.PLANT_HAVE_CD) == 1;
    }
    @Override
    public boolean autoSetCostAndCD(Player player) {
        return PVZPlayerCapability.getValue(player, PVZPlayerCapStats.AUTO_SET_COST_AND_CD) == 1;
    }
    @Override
    public void setPlantHaveCost(Player player, boolean value) {
        PVZPlayerCapability.getPlayerData(player).ifPresent((data) -> data.setValue(PVZPlayerCapStats.PLANT_HAVE_COST, value ? 1 : 0));
    }
    @Override
    public void setPlantHaveCD(Player player, boolean value) {
        PVZPlayerCapability.getPlayerData(player).ifPresent((data) -> data.setValue(PVZPlayerCapStats.PLANT_HAVE_CD, value ? 1 : 0));
    }
    @Override
    public void setAutoSetCostAndCD(Player player, boolean value) {
        PVZPlayerCapability.getPlayerData(player).ifPresent((data) -> data.setValue(PVZPlayerCapStats.AUTO_SET_COST_AND_CD, value ? 1 : 0));
    }
    @Override
    public @Nullable Attribute getAttribute(com.hungteen.pvz.api.PVZAPI.PVZAttributes entry) {
        return switch (entry) {
            case MAX_SUN -> PVZAttributes.MAX_SUN.get();
            case PLANT_HURT_RESISTANCE -> PVZAttributes.PLANT_HURT_RESISTANCE.get();
            case LURE_ATTRACTING_ENEMIES -> PVZAttributes.ENEMY_ATTRACTION.get();
            case LEVEL_ATTRACTING_ENEMIES -> PVZAttributes.ENEMY_ATTRACTION_LEVEL.get();
        };
    }
    @Override
    public boolean isTeammate(Entity A, Entity B) {
        return EntityUtil.isTeammate(A, B);
    }

    @Override
    public boolean canAttack(Entity attacker, Entity target) {
        return EntityUtil.checkCanEntityBeAttack(attacker, target);
    }

    @Override
    public boolean isSculk(LivingEntity entity) {
        return EntityUtil.isSculk(entity);
    }

    @Override
    public ResourceLocation getZombieEventType(ZombieEvent event) {
        return PVZZombieEvents.getType(event);
    }

    @Override
    public DamageSource setNotEating(DamageSource damageSource) {
        return PVZDamageSource.setNotEating(damageSource);
    }

    @Override
    public void removeClientZombieEvent(ZombieEvent event) {
        if (! event.level.isClientSide) {
            ZombieEventPacket.removalToClient(event);
        }
    }
}
