package com.hungteen.pvz.api.interfaces;


import net.minecraft.world.entity.Entity;

/**IShooter contains pvz shooters and pults.*/
public interface IShooter {
	//TODO extend RangedAttackMob

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
