package com.hungteen.pvz.common.world;

import com.hungteen.pvz.PVZMod;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraft.world.level.storage.DimensionDataStorage;
import net.minecraft.world.scores.Scoreboard;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import org.jetbrains.annotations.NotNull;

import java.util.*;

@Mod.EventBusSubscriber
public class PVZSavedData extends SavedData {
    private static final Map<Scoreboard, PVZSavedData> byScoreboard = new HashMap<>();
    private final Set<String> evilList = new HashSet<>();
    private long serverLifetime = 0; //TODO scheduleCommand里面有原版的游戏时间引用？能换吗？
    /**Team evilness information is synced from server every second. Do not call in server.*/
    public static Set<String> clientEvilList = new HashSet<>();
    public static boolean isTeamBattleOn;
    public static void tick() {
        byScoreboard.forEach((scoreBoard, data) -> {
            Set<String> evilList = Set.copyOf(data.evilList);
            Collection<String> names = scoreBoard.getTeamNames();
            for (String name : evilList) {
                if (! names.contains(name)) {
                    data.evilList.remove(name);
                }
            }
            data.serverLifetime ++;
            data.setDirty();
        });
    }

    public static long getServerTime(Scoreboard scoreboard) {
        return byScoreboard.containsKey(scoreboard) ? byScoreboard.get(scoreboard).serverLifetime : 0;
    }

    public static int setEvil(Scoreboard scoreboard, String name, boolean isEvil) {
        PVZSavedData data = byScoreboard.get(scoreboard);
        if (data != null) {
            data.setDirty();
            if (isEvil) {
                if (! data.evilList.contains(name)) {
                    data.evilList.add(name);
                    return 1;
                }
            } else {
                if (data.evilList.contains(name)) {
                    data.evilList.remove(name);
                    return 1;
                }
            }
            return 0;
        } else {
            PVZMod.LOGGER.error("Scoreboard of Scoreboard " + scoreboard + " is not registered as modified!");
            return -1;
        }
    }

    public static boolean isEvil(Scoreboard scoreboard, String name) {
        PVZSavedData data = byScoreboard.get(scoreboard);
        if (data != null) {
            return data.evilList.contains(name);
        }
        PVZMod.LOGGER.error("Scoreboard of Scoreboard " + scoreboard + " is not registered as modified!");
        return false;
    }

    @SubscribeEvent
    public static void onLevelLoad(LevelEvent.Load event) {
        LevelAccessor accessor = event.getLevel();
        if (accessor instanceof ServerLevel level && level.dimension() == Level.OVERWORLD) {
            DimensionDataStorage dimensiondatastorage = level.getDataStorage();
            PVZSavedData data = dimensiondatastorage.computeIfAbsent(PVZSavedData::load, PVZSavedData::create, "pvz.team_data");
            Scoreboard scoreboard = level.getServer().getScoreboard();
            if (! byScoreboard.containsKey(scoreboard)) {
                byScoreboard.put(scoreboard, data);
            }
        }
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag save) {
        ListTag evil = new ListTag();
        this.evilList.forEach(name -> evil.add(StringTag.valueOf(name)));
        save.put("evil", evil);

        save.putLong("server_time", this.serverLifetime);

        return save;
    }

    public static PVZSavedData create() {
        PVZSavedData data = new PVZSavedData();
        return data;
    }
    public static PVZSavedData load(CompoundTag save) {
        PVZSavedData data = new PVZSavedData();
        if (save.contains("evil")) {
            ListTag evil = save.getList("evil", Tag.TAG_STRING);
            evil.forEach(name -> data.evilList.add(name.getAsString()));
        }
        if (save.contains("server_time")) {
            data.serverLifetime = save.getLong("server_time");
        }
        return data;
    }
}
