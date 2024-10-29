package com.hungteen.pvz.api.interfaces;

import com.hungteen.pvz.api.Skill;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
    default int getSkillVal() {
        return getSkillVal(this);
    }
    void setSkillVal(Object obj,int value);
    default void setSkillVal(int value) {
        setSkillVal(this, value);
    }

    default boolean hasSkill(Object obj, int id) {
        return (1 << id & getSkillVal(obj)) == 1 << id;
    }
    default boolean hasSkill(int id) {
        return hasSkill(this, id);
    }

    default boolean hasSkill(Object obj, String name) {
        short id = -1;
        for (short i = 0; i < this.getStaticSkillList().size(); i ++) {
            Skill skill = this.getStaticSkillList().get(i);
            if (name.equals(skill.name)) {
                id = i;
                break;
            }
        }
        if (id == -1) {
            return false;
        }
        return hasSkill(obj, id);
    }
    default boolean hasSkill(String name) {
        return hasSkill(this, name);
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

    default List<String> getSkillNames() {
        List<Skill> skillList = this.getStaticSkillList();
        if (skillList != null) {
            List<String> list = new ArrayList<>();
            for (int skill : this.getSkills(this)) {
                list.add(skillList.get(skill).name);
            }
            return list;
        }
        return List.of();
    }

    default short getSkillId(Skill skill) {
        short index = 0;
        for (Skill i : getStaticSkillList()) {
            if (i == skill) {
                return index;
            }
            index ++;
        }
        return -1;
    }

    default int getSkillValFromNames(List<String> names) {
        int result = 0;
        for (String name : names) {
            short id = getSkillId(getSkillFromName(name));
            if (id >= 0) {
                result = result | 1 << id;
            }
        }
        return result;
    }
    default Skill getSkillFromName(String name) {
        for (Skill i : getStaticSkillList()) {
            if (i.name.equals(name)) {
                return i;
            }
        }
        return null;
    }
    default boolean addSkill(Object obj, int id) {
        if (hasSkill(obj, id)) {
            return false;
        }
        setSkillVal(obj, getSkillVal(obj) | 1 << id);
        return true;
    }
    default boolean removeSkill(Object obj, int id) {
        if (! hasSkill(obj, id)) {
            return false;
        }
        setSkillVal(obj, ~(~ getSkillVal(obj) | 1 << id));
        return true;
    }
    default boolean addSkill(Object obj, Skill skill) {
        short id = getSkillId(skill);
        return addSkill(obj, id);
    }
    default boolean removeSkill(Object obj, Skill skill) {
        short id = getSkillId(skill);
        return removeSkill(obj, id);
    }
    default void setSkill(Object obj, int id, boolean enable) {
        if (enable) {
            setSkillVal(obj, getSkillVal(obj) | 1 << id);
        } else {
            setSkillVal(obj, ~(~ getSkillVal(obj) | 1 << id));
        }
    }
    default Skill getNotCompatibleWith(Object obj, Skill skill) {
        for (Skill i : getStaticSkillList()) {
            if (hasSkill(obj, getSkillId(i))) {
                if (i.avoidSkills.contains(skill.name) || skill.avoidSkills.contains(i.name)) {
                    return i;
                }
            }
        }
        return null;
    }

    default Skill getStillRequire(Object obj, Skill skill) {
        for (String name : skill.requireSkills) {
            Skill skill1 = getSkillFromName(name);
            if (! hasSkill(obj, getSkillId(skill1))) {
                return skill1;
            }
        }
        return null;
    }
}
