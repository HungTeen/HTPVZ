package com.hungteen.pvz.api.interfaces;


import net.minecraft.world.entity.Entity;

public interface IShooter {

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
