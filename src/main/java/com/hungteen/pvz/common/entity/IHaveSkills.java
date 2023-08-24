package com.hungteen.pvz.common.entity;

import net.minecraft.nbt.CompoundTag;

import java.util.ArrayList;
import java.util.List;

public interface IHaveSkills {
    /**
     * If you want to add a PVZ skill for your plant or any mob else (or even not mob?), use this interface.
     * <br>Remember to save and load the skills. See {@link IHaveSkills#saveSkills(CompoundTag)} and {@link IHaveSkills#loadSkills(CompoundTag)}.
     */
    List<String> skillList = new ArrayList<>();

    default List<String> getSkillList(){
        return skillList;
    }

    default boolean hasSkill(String skill){
        return skillList.contains(skill);
    }

    default boolean attachSkill(String skill){
        if (this.hasSkill(skill)){
            return false;
        } else {
            skillList.add(skill);
            return true;
        }
    }

    default boolean removeSkill(String skill){
        if (this.hasSkill(skill)){
            skillList.remove(skill);
            return true;
        } else {
            return false;
        }
    }

    default void saveSkills(CompoundTag tag) {
    }

    default void loadSkills(CompoundTag tag) {
    }
    //TODO save and load from nbt.
}
