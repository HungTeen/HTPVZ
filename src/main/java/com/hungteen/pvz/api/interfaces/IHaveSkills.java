package com.hungteen.pvz.api.interfaces;

import com.hungteen.pvz.api.Skill;
import net.minecraft.nbt.CompoundTag;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

public interface IHaveSkills {
    /**
     * If you want to add a PVZ skill for your plant or any mob else (or even not mob?), use this interface. Skills can't be attached on exist entities.
     * <br>Remember to save and load the skills. See {@link IHaveSkills#saveSkill(CompoundTag)} and {@link IHaveSkills#loadSkill(CompoundTag)}.
     */

    default Skill getSkillFromId(int id) {
        return getStaticSkillList().get(id);
    }


    int getSkill();//if you want multiple skills for an entity, define skill id as 2^n.
    boolean setSkill(int id);
    List<Skill> getStaticSkillList();


    default boolean setSkill(Skill skill){
        return setSkill(getStaticSkillList().indexOf(skill));
    }
    default void removeSkill(){
        setSkill(-1);
    }
    default boolean hasSkill(int id) {
        return getSkill() == id;
    }
    default boolean hasSkill(Skill skill) {
        return getStaticSkillList().indexOf(skill) == getSkill();
    }


    default void saveSkill(CompoundTag tag) {
        tag.putInt("Skill", getSkill());
    }
    default void loadSkill(CompoundTag tag) {
        if (tag.contains("Skill")) {
            setSkill(tag.getInt("Skill"));
        }
    }
}
