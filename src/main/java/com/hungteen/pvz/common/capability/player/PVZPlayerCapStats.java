package com.hungteen.pvz.common.capability.player;

import com.hungteen.pvz.common.network.PlayerCapStatsPacket;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.*;

//TODO handle situation when player is not available.

/**It's ok to add values here. Don't to let the limit larger than 32767 unless it's used only in the server.*/
public class PVZPlayerCapStats {
    public Player player;
    private Map<String, Integer> dataMap = new HashMap<>();
    private Map<String, Pair<Integer, Integer>> dataLimitMap = new HashMap<>();
    private List<String> dirtyList = new ArrayList<>();
    private List<String> syncableList = new ArrayList<>();

    public static final String SUN = "pvz.sun";
    public static final String CAN_PLANT = "can_plant";
    public static final String PLANT_HAVE_COST = "plant_have_cost";
    public static final String AUTO_SET_COST_AND_CD = "auto_set_cost_and_cd";
    public static final String PLANT_HAVE_CD = "plant_have_cd";
    public static final String INVASION_DIFFICULTY = "invasion_difficulty";
    public static final String LAST_INVASION = "last_invasion";
    public static final String SUMMONED_PENNY = "summoned_penny";

    //sun effect count
    public int sunCountDown = 0;

    public PVZPlayerCapStats() {
        initBasicValues();
    }

    public void setPlayer(Player player){
        this.player = player;
    }

    public void initBasicValues() {
        //basic
        initValue(CAN_PLANT, 1, 0, 1);//naturally always 1. if 0, player can't plant.
        initValueNoSync(AUTO_SET_COST_AND_CD, 1, 0, 1);//naturally always 1. if 1, "plant_cost_sun" and "plant_have_cd" of this player will change with gamemode.
        initValue(PLANT_HAVE_COST, 1, 0, 1);//naturally creative:0, survival:1.
        initValue(PLANT_HAVE_CD, 1, 0, 1);//naturally creative:0, survival:1.
        initValueNoSync(SUMMONED_PENNY, 0, 0, 1);//if the player has summoned Penny recently.
        initValueNoSync(INVASION_DIFFICULTY, 0, 0, 100);//invasion difficulty.
        initValueNoSync(LAST_INVASION, 0, 0, 10000);//time since last invasion occurred on this player.
        //resource
        initValue(SUN, 50, 0, 200);
    }

    //values
    public void setValue(String key, Integer value) {
        Pair<Integer, Integer> limit = getValueLimit(key);
        var currValue = dataMap.get(key);
        if (limit != null) {
            if (value > limit.getSecond()) {
                value = limit.getSecond();
            } else if (value < limit.getFirst()) {
                value = limit.getFirst();
            }
        }
        if (currValue == null || ! currValue.equals(value)) {
            dataMap.put(key, value);
            if (syncableList.contains(key)) dirtyList.add(key);
        }
    }

    public void initValue(String key, Integer value, Integer minLimit, Integer maxLimit) {
        initValueNoSync(key, value, minLimit, maxLimit);
        syncableList.add(key);
    }

    public void initValueNoSync(String key, Integer value, Integer minLimit, Integer maxLimit) {
        dataMap.put(key, value);
        dataLimitMap.put(key, Pair.of(minLimit, maxLimit));
    }

    public Integer getValue(String key) {
        return dataMap.getOrDefault(key, 0);
    }

    public boolean addValue(String key, Integer value) {
        boolean valueAlreadyExists;
        if (dataMap.containsKey(key)) {
            setValue(key, dataMap.get(key) + value);
            valueAlreadyExists = true;
        } else {
            setValue(key, value);
            valueAlreadyExists = false;
        }
        return valueAlreadyExists;
    }

    public void setValueLimit(String key, Integer min, Integer max) {
        var currValue = dataLimitMap.get(key);
        Pair<Integer, Integer> pair = Pair.of(min, max);
        if (currValue == null || ! currValue.equals(pair)) {
            dataLimitMap.put(key, pair);
            int value = getValue(key);
            if (value > max) {
                setValue(key, max);
            } else if (value < min) {
                setValue(key, min);
            }
            if (syncableList.contains(key)) dirtyList.add(key);
        }
    }

    public void setValueLimit(String key, Integer max) {
        int curMinValue = dataLimitMap.containsKey(key) ? dataLimitMap.get(key).getFirst() : 0;
        setValueLimit(key, curMinValue, max);
    }

    public Pair<Integer, Integer> getValueLimit(String key) {
        return dataLimitMap.getOrDefault(key, Pair.of(0, 0));
    }

    public List<String> getDirtyList() {
        return this.dirtyList;
    }

    public Set<String> getKeySet() {
        return this.dataMap.keySet();
    }

    public void cleanDirt() {
        this.dirtyList.clear();
    }


    //saving
    public CompoundTag serializeNBT() {
        CompoundTag baseTag = new CompoundTag();
        int count = 0;
        {//save values.
            CompoundTag valueTag = new CompoundTag();
            for (String key : dataMap.keySet()) {
                valueTag.putString("name" + count, key);
                valueTag.putInt(key, dataMap.get(key));
                count ++;
            }
            baseTag.put("values", valueTag);
        }
        {//save limits.
            CompoundTag limitsTag = new CompoundTag();
            count = 0;
            for (String key : dataLimitMap.keySet()) {
                limitsTag.putString("name" + count, key);
                limitsTag.putInt(key + "_min", dataLimitMap.get(key).getFirst());
                limitsTag.putInt(key + "_max", dataLimitMap.get(key).getSecond());
                count ++;
            }
            baseTag.put("limits", limitsTag);
        }
        return baseTag;
    }

    public void deserializeNBT(CompoundTag baseTag) {
        String name = "";
        int count = 0;
        //TODO change these to tag.gatAllTags().
        if (baseTag.contains("values")) {
            CompoundTag valueTag = baseTag.getCompound("values");
            while (valueTag.contains("name" + count)) {
                name = valueTag.getString("name" + count);
                if (valueTag.contains(name)) {
                    dataMap.put(name, valueTag.getInt(name));
                }
                count ++;
            }
        }
        if (baseTag.contains("limits")) {
            count = 0;
            CompoundTag limitsTag = baseTag.getCompound("limits");
            while (limitsTag.contains("name" + count)) {
                name = limitsTag.getString("name" + count);
                if (limitsTag.contains(name + "_min")) {
                    dataLimitMap.put(name, Pair.of(limitsTag.getInt(name + "_min"), limitsTag.getInt(name + "_max")));
                }
                count ++;
            }
        }
    }
    public void sync(boolean syncAll) {
        if (player instanceof ServerPlayer serverPlayer) {
            PlayerCapStatsPacket.sync(serverPlayer, this, syncAll);
            cleanDirt();
        }
    }

}
