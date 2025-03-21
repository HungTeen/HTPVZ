package com.hungteen.pvz.common.world.team;

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
public class PVZTeamData extends SavedData {
    private static final Map<Scoreboard, PVZTeamData> byScoreboard = new HashMap<>();

    private final Set<String> evilList = new HashSet<>();

    public static void register(Scoreboard scoreboard, PVZTeamData data) {
        byScoreboard.put(scoreboard, data);
    }

    public static void tick() {
        byScoreboard.forEach((scoreBoard, data) -> {
            Set<String> evilList = Set.copyOf(data.evilList);
            Collection<String> names = scoreBoard.getTeamNames();
            for (String name : evilList) {
                if (! names.contains(name)) {
                    data.evilList.remove(name);
                }
            }
        });
    }

    public static int setEvil(Scoreboard scoreboard, String name, boolean isEvil) {
        PVZTeamData data = byScoreboard.get(scoreboard);
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
        PVZTeamData data = byScoreboard.get(scoreboard);
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
            PVZTeamData data = dimensiondatastorage.computeIfAbsent(PVZTeamData::load, PVZTeamData::create, "pvz.team_data");
            Scoreboard scoreboard = level.getServer().getScoreboard();
            register(scoreboard, data);
        }
    }

    @Override
    public @NotNull CompoundTag save(CompoundTag save) {
        CompoundTag tag = new CompoundTag();
        ListTag evil = new ListTag();
        this.evilList.forEach(name -> evil.add(StringTag.valueOf(name)));
        tag.put("evil", evil);

        save.put("data", tag);
        return save;
    }

    public static PVZTeamData create() {
        PVZTeamData data = new PVZTeamData();
        return data;
    }
    public static PVZTeamData load(CompoundTag save) {
        PVZTeamData data = new PVZTeamData();
        if (save.contains("data")) {
            CompoundTag tag = save.getCompound("data");
            ListTag evil = tag.getList("evil", Tag.TAG_STRING);
            evil.forEach(name -> data.evilList.add(name.getAsString()));
        } else {
            PVZMod.LOGGER.error("PVZTeamData is not found when loading!");
        }
        return data;
    }
}
