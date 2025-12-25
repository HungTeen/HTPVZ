package com.hungteen.pvz.api;

import net.minecraft.world.item.Item;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

/**
 This class Skill is a pass if this entity can use this skill, how skill works is written in the Entity itself.
 <br>That means you can't make skills for existing entities, however, you can still make skills for your entities.
 */

public class Skill {
    /**component of the skill.*/
    public final String name;

    /**type of essences or non-essences used when adding skill to card.*/
    public final Supplier<Item> item;

    /**amount of the cost item.*/
    public final int costItem;
    public final int costSeed;
    /**add num of the cost of resource. can be negative.*/
    public final int addCostResource;
    public final int addCoolDown;
    public final Set<String> avoidSkills = new HashSet<>();
    public final Set<String> requireSkills = new HashSet<>();
    public Skill(String name, Supplier<Item> item, int costItem, int costSeed) {
        this(name, item, costItem, costSeed, 0,0);
    }
    public Skill(String name, Supplier<Item> item, int costItem, int costSeed, int addCostResource, int addCoolDown){
        this.name = name;
        this.item = item;
        this.costItem = costItem;
        this.costSeed = costSeed;
        this.addCostResource = addCostResource;
        this.addCoolDown = addCoolDown;
    }

    public Skill avoidSkills(String... skills) {
        avoidSkills.addAll(Arrays.asList(skills));
        return this;
    }
    public Skill requireSkills(String... skills) {
        requireSkills.addAll(Arrays.asList(skills));
        return this;
    }
}
