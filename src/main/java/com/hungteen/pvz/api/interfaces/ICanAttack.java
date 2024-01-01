package com.hungteen.pvz.api.interfaces;

import com.hungteen.pvz.api.Skill;

import java.util.List;

public interface ICanAttack {
    default List<Skill> getStaticSkillList() {
        return null;
    }

    int getAttackTime(Object obj/*to support items*/);
    void setAttackTime(Object obj,int value);

    default boolean canAttack(Object obj, boolean enable) {
        return (enable);
    }

    default int addAttackTime(Object obj, int id) {
        int result = getAttackTime(obj);
        if(canAttack(obj,true))  result = getAttackTime(obj)+id;
        return result;
    }

}
