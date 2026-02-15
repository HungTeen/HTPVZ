package com.hungteen.pvz.api.interfaces;


import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.RangedAttackMob;

/**IShooter contains pvz shooters and pults.*/
public interface IShooter extends RangedAttackMob {

	void performRangedAttack(LivingEntity target, float p_33318_);

	/**
	 * shoot bullet to attack
	 */
	void shootBullet();

	/**
	 * get current shoot CD
	 */
	int getShootCD();
	
	/**
	 * bullet initial move speed
	 */
	float getBulletSpeed();

	/**
	 * is suitable angle
	 */
	boolean isHeightAvailable(Entity target);
	
}
