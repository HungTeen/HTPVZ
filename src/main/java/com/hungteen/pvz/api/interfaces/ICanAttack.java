package com.hungteen.pvz.api.interfaces;

public interface ICanAttack {
    int getAttackTime();
    void setAttackTime(int value);

    default boolean canAttack(boolean enable) {
        return (enable);
    }

    default int addAttackTime(int id) {
        int result = getAttackTime();
        if(canAttack(true))  result = getAttackTime()+id;
        return result;
    }

}
