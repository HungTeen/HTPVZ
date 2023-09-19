package com.hungteen.pvz.api;

import net.minecraft.world.item.Item;

import java.util.function.Supplier;

/**
 This class Skill is a pass if this entity can use this skill, how skill works is written in the Entity itself.
 <br>That means you can't make skills for exist entities, however, you can still make skills for your entities.
 */
public class Skill {
    /**component of the skill.*/
    public String name;

    /**type of essences or non-essences used when adding skill to card.*/
    public Supplier<Item> item;

    /**amount of the cost item.*/
    public int costItem;
    public int addCostSun = 0;
    public int costSeedPacket = 0;
    public int addCoolDown = 0;

    public Skill(String name, Supplier<Item> item, int costItem, int costSeedPacket){
        this.name = name;
        this.item = item;
        this.costItem = costItem;
        this.costSeedPacket = costSeedPacket;
    }
    public Skill(String name, Supplier<Item> item, int costItem, int costSeedPacket, int addCostSun, int addCoolDown){
        this.name = name;
        this.item = item;
        this.costItem = costItem;
        this.costSeedPacket = costSeedPacket;
        this.addCostSun = addCostSun;
        this.addCoolDown = addCoolDown;
    }

}
