package com.hungteen.pvz.api.interfaces;

public interface ICanAttack {
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
