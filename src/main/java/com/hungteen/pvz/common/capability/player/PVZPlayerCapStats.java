package com.hungteen.pvz.common.capability.player;

import com.hungteen.pvz.common.network.PlayerCapStatsPacket;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

//TODO handle situation when player is not available.
public class PVZPlayerCapStats {
    public Player player;
    private Map<String, Integer> dataMap = new HashMap<>();
    private Map<String, Pair<Integer, Integer>> dataLimitMap = new HashMap<>();

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
        setValue(CAN_PLANT, 1, 0, 1);//naturally always 1. if 0, player can't plant.
        setValue(AUTO_SET_COST_AND_CD, 1, 0, 1);//naturally always 1. if 1, "plant_cost_sun" and "plant_have_cd" of this player will change with gamemode.
        setValue(PLANT_HAVE_COST, 1, 0, 1);//naturally creative:0, survival:1.
        setValue(PLANT_HAVE_CD, 1, 0, 1);//naturally creative:0, survival:1.
        setValue(SUMMONED_PENNY, 0, 0, 1);//if the player has summoned Penny recently.
        setValue(INVASION_DIFFICULTY, 0, 0, 100);//invasion difficulty.
        setValue(LAST_INVASION, 0, 0, 1000);//time since last invasion occurred on this player.
        //resource
        setValue(SUN, 50, 0, 200);
    }

    //values
    public void setValue(String key, Integer value) {
        Pair<Integer, Integer> limit = getValueLimit(key);
        if (limit != null) {
            if (value > limit.getSecond()) {
                value = limit.getSecond();
            } else if (value < limit.getFirst()) {
                value = limit.getFirst();
            }
        }
        dataMap.put(key, value);
        if (player instanceof ServerPlayer){
            PlayerCapStatsPacket.sync((ServerPlayer) player, key, true);
        }
    }

    public void setValue(String key, Integer value, Integer minLimit, Integer maxLimit) {
        setValue(key, value);
        setValueLimit(key, minLimit, maxLimit);
    }

    public Integer getValue(String key) {
        return dataMap.get(key);
    }

    public boolean addValue(String key, Integer value) {
        Boolean valueAlreadyExists;
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
        dataLimitMap.put(key, Pair.of(min, max));
        int value = getValue(key);
        if (value > max) {
            setValue(key, max);
        } else if (value < min) {
            setValue(key, min);
        }
        if (player instanceof ServerPlayer){
            PlayerCapStatsPacket.sync((ServerPlayer) player, key, false);
        }
    }

    public void setValueLimit(String key, Integer max) {
        int curMinValue = dataLimitMap.containsKey(key) ? dataLimitMap.get(key).getFirst() : 0;
        setValueLimit(key, curMinValue, max);
    }

    public Pair<Integer, Integer> getValueLimit(String key) {
        return dataLimitMap.get(key);
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

    public static void cloneData(Player oldPlayer, Player newPlayer) {
        if (oldPlayer != null && newPlayer != null){
            AtomicReference<CompoundTag> tmp = null;
            PVZPlayerCapability.getPlayerData(oldPlayer).ifPresent(nbt -> tmp.set(nbt.serializeNBT()));
            PVZPlayerCapability.getPlayerData(newPlayer).ifPresent(nbt -> nbt.deserializeNBT(tmp.get()));
        }
    }

    public void syncAll(){
        if (player instanceof ServerPlayer) {
            for (String key : dataMap.keySet()) {
                PlayerCapStatsPacket.sync((ServerPlayer) player, key, true);
            }
            for (String key : dataLimitMap.keySet()){
                PlayerCapStatsPacket.sync((ServerPlayer) player, key, false);
            }
        }
    }

}
