package com.hungteen.pvz.api.interfaces;

import com.hungteen.pvz.api.Skill;

import java.util.*;

public interface IHaveSkills {
    /**
     * If you want to add a PVZ skill for your plant or any mob else (or even not mob?), use this interface. Skills can't be attached on exist entities. Don't support skill level.
     * <br>Remember to save and load the skills.
     * <br><br>Store as a single Integer.
     */

    default List<Skill> getStaticSkillList() {
        return null;
    }

    int getSkillVal(Object obj/*to support items*/);
    void setSkillVal(Object obj,int value);

    default boolean hasSkill(Object obj, int id) {
        return (1 << id & getSkillVal(obj)) == 1 << id;
    }
    default Set<Integer> getSkills(Object obj) {
        Set<Integer> result = new HashSet<>();
        for (int i = 0; i < 32; i ++){
            if (hasSkill(obj, i)) {
                result.add(i);
            }
        }
        return result;
    }
    default boolean addSkill(Object obj, int id) {
        if (hasSkill(obj, id)) {
            return false;
        }
        setSkillVal(obj, getSkillVal(obj) | 1 << id);
        return true;
    }
    default void setSkill(Object obj, int id, boolean enable) {
        if (enable) {
            setSkillVal(obj, getSkillVal(obj) | 1 << id);
        } else {
            setSkillVal(obj, ~(~ getSkillVal(obj) | 1 << id));
        }
    }
}
