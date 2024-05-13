package com.hungteen.pvz;

import com.hungteen.pvz.api.interfaces.ISunAbsorber;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.register.PVZAttributes;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.player.Player;

public class PVZAPI implements com.hungteen.pvz.api.PVZAPI.IPVZAPI {
    @Override
    public String getSunString() {
        return PVZPlayerCapNBT.SUN;
    }
    @Override
    public int getSun(Player player) {
        return PVZPlayerCapability.getValue(player, getSunString());
    }
    @Override
    public boolean plantHaveCost(Player player) {
        return PVZPlayerCapability.getValue(player, "plant_have_cost") == 1;
    }
    @Override
    public boolean plantHaveCD(Player player) {
        return PVZPlayerCapability.getValue(player, "plant_have_cd") == 1;
    }
    @Override
    public boolean autoSetCostAndCD(Player player) {
        return PVZPlayerCapability.getValue(player, "auto_set_cost_and_cd") == 1;
    }
    @Override
    public void setPlantHaveCost(Player player, boolean value) {
        PVZPlayerCapability.getPlayerData(player).ifPresent((data) -> data.setValue("plant_have_cost", value ? 1 : 0));
    }
    @Override
    public void setPlantHaveCD(Player player, boolean value) {
        PVZPlayerCapability.getPlayerData(player).ifPresent((data) -> data.setValue("plant_have_cd", value ? 1 : 0));
    }
    @Override
    public void setAutoSetCostAndCD(Player player, boolean value) {
        PVZPlayerCapability.getPlayerData(player).ifPresent((data) -> data.setValue("auto_set_cost_and_cd", value ? 1 : 0));
    }
    @Override
    public Attribute getMaxSunAttribute() {
        return PVZAttributes.SUN.get();
    }

    @Override
    public boolean isTeammate(Entity A, Entity B) {
        return EntityUtil.isTeammate(A, B);
    }

    @Override
    public boolean canAttack(Entity attacker, Entity target) {
        return EntityUtil.checkCanEntityBeAttack(attacker, target);
    }
}
