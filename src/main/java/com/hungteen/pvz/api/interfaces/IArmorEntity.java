package com.hungteen.pvz.api.interfaces;

import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

/**If this entity it rides is attacked, IArmorEntity passes the damage to itself.<br>
 * Used for armor entities like Pumpkin.<br>
 * About how this work, see {@link com.hungteen.pvz.common.world.PVZDamageSource#handleAttack(LivingAttackEvent)}.*/
public interface IArmorEntity {
    default boolean canRecieveDamage(DamageSource source, double amount, Entity target) {
        return ! source.isBypassArmor();
    }
}
