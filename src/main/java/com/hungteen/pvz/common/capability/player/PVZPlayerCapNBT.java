package com.hungteen.pvz.common.capability.player;

import com.hungteen.pvz.common.network.PlayerCapPacket;
import com.hungteen.pvz.common.world.zen_garden.ZenGardenTeleporter;
import com.mojang.datafixers.util.Pair;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

//TODO handle situation when player is not available.
public class PVZPlayerCapNBT {
    public Player player;
    private Map<String, Integer> dataMap = new HashMap<>();
    private Map<String, Pair<Integer, Integer>> dataLimitMap = new HashMap<>();

    private Pair<Vec3, Vec3> gardenPos = new Pair<>(null, null);
    public static final String SUN = "pvz.sun";

    //sun effect count
    public int sunCountDown = 0;

    public PVZPlayerCapNBT() {
        initBasicValues();
    }

    public void setPlayer(Player player){
        this.player = player;
    }

    public void initBasicValues() {
        //basic
        setValue("can_plant", 1, 0, 1);//naturally always 1. if 0, player can't plant.
        setValue("auto_set_cost_and_cd", 1, 0, 1);//naturally always 1. if 1, "plant_cost_sun" and "plant_have_cd" of this player will change with gamemode.
        setValue("plant_have_cost", 1, 0, 1);//naturally creative:0, survival:1.
        setValue("plant_have_cd", 1, 0, 1);//naturally creative:0, survival:1.
        //resource
        setValue(SUN, 50, 0, 200);// TODO remove sun to another PVZPlanterCap cap.
    }

    public void setTransportPos(Level destWorld, Vec3 pos) {
        this.gardenPos = destWorld.dimension().equals(ZenGardenTeleporter.GARDEN) ? new Pair<>(pos, gardenPos.getSecond()) : new Pair<>(gardenPos.getFirst(), pos);
    }

    public Vec3 getTransportPos(Level destWorld) {
        return destWorld.dimension().equals(ZenGardenTeleporter.GARDEN) ? gardenPos.getSecond() : gardenPos.getFirst();
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
            PlayerCapPacket.sync((ServerPlayer) player, key, true);
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
            PlayerCapPacket.sync((ServerPlayer) player, key, false);
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
        {
            CompoundTag posTag = new CompoundTag();
            if (this.gardenPos.getFirst() != null) {
                posTag.putDouble("overworld_x", this.gardenPos.getFirst().x);
                posTag.putDouble("overworld_y", this.gardenPos.getFirst().y);
                posTag.putDouble("overworld_z", this.gardenPos.getFirst().z);
            }
            if (this.gardenPos.getSecond() != null) {
                posTag.putDouble("garden_x", this.gardenPos.getSecond().x);
                posTag.putDouble("garden_y", this.gardenPos.getSecond().y);
                posTag.putDouble("garden_z", this.gardenPos.getSecond().z);
            }
            baseTag.put("garden_pos", posTag);
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
        if (baseTag.contains("garden_pos")) {
            CompoundTag tag = baseTag.getCompound("garden_pos");
            this.gardenPos = new Pair<>(tag.contains("overworld_x") ? new Vec3(
                    tag.getDouble("overworld_x"),
                    tag.getDouble("overworld_y"),
                    tag.getDouble("overworld_z")
            ) : null, tag.contains("garden_x") ? new Vec3(
                    tag.getDouble("garden_x"),
                    tag.getDouble("garden_y"),
                    tag.getDouble("garden_z")
            ) : null);
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
                PlayerCapPacket.sync((ServerPlayer) player, key, true);
            }
            for (String key : dataLimitMap.keySet()){
                PlayerCapPacket.sync((ServerPlayer) player, key, false);
            }
        }
    }

}
