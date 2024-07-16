package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.entity.Entity;

/**{@link com.hungteen.pvz.common.entity.ai.goal.AttractEnemyGoal AttractEnemyGoal} determine who absorbs hate more with this interface.
 * <br> the basic value is 10, which is the value of most living entities without this goal.*/
public interface IAttractsEnemy {
    default float getAttractStrength(Entity attacker) {
        return 50;
    }
}
