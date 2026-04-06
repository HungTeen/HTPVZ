package com.hungteen.pvz.common.world.invasion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.world.DataSkillManager;
import com.hungteen.pvz.util.Util;
import com.mojang.serialization.JsonOps;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class InvasionTypeManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).create();
    public InvasionTypeManager() {
        super(GSON, "invasion_types");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller filler) {
        Map<ResourceLocation, InvasionType> map = new HashMap<>();
        map.put(Util.prefix("empty"), new InvasionType(Optional.empty(), List.of(), List.of(), Optional.empty(), List.of(), false, 1, 1, 0));
        jsonMap.forEach((location, json) -> InvasionType.CODEC.parse(JsonOps.INSTANCE, json)
                .resultOrPartial(errorMsg -> PVZMod.LOGGER.error("Could not decode InvasionType with json id {} - error: {}", location, errorMsg))
                .ifPresent(invasionType -> map.put(location, invasionType)));
        InvasionType.invasionTypes = map;
    }

    @SubscribeEvent
    public static void addListener(AddReloadListenerEvent ev) {
        ev.addListener(new InvasionTypeManager());
        ev.addListener(new DataSkillManager());
    }
}
