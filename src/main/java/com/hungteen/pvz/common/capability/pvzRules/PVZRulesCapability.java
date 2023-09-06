package com.hungteen.pvz.common.capability.pvzRules;

import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.LazyOptional;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.Map;

public class PVZRulesCapability implements ICapabilitySerializable<CompoundTag> {

    private static PVZRulesCapability cap = null;

    //rules.
    public Map<String, Boolean> booleanMap;


    public PVZRulesCapability() {
        cap = this;
        booleanMap = initBooleanMap();
    }
    @Override
    public @NotNull <T> LazyOptional<T> getCapability(@NotNull Capability<T> cap, @Nullable Direction side) {
        return LazyOptional.of(() -> this).cast();
    }

    public static HashMap<String, Boolean> initBooleanMap() {
        HashMap<String, Boolean> map = new HashMap<>();
        register(map, "shovelPermission", true);//if on, player can shovel plants planted by not only itself.
        register(map, "sunDisappear", true);//if on, sun will automatically disappear.
        register(map, "teamBattle", false);//if on, plants in different team will attack each other.
        return map;
    }

    public static PVZRulesCapability get(){
            return cap;
    }

    public static boolean get(String key){
        return get().booleanMap.get(key);
    }

    public static void register(HashMap<String, Boolean> map, String name, Boolean defaultValue){
        map.put(name, defaultValue);
    }

    @Override
    public CompoundTag serializeNBT() {
        CompoundTag basicTag = new CompoundTag();
        for (String key : booleanMap.keySet()) {
            basicTag.putBoolean(key, booleanMap.get(key));
        }
        return basicTag;
    }

    @Override
    public void deserializeNBT(CompoundTag nbt) {
        for (String key : booleanMap.keySet()) {
            if (booleanMap.containsKey(key)) {
                booleanMap.put(key, nbt.getBoolean(key));
            }
        }
    }
}
