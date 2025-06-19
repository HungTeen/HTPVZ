package com.hungteen.pvz.common.world;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.common.register.PVZSeedPackets;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DataSkillManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = (new GsonBuilder()).create();
    public DataSkillManager() {
        super(GSON, "skills");
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> jsonMap, ResourceManager manager, ProfilerFiller filler) {
        Map<EntityType<?>, List<Skill>> map = new HashMap<>();
        jsonMap.forEach(((location, json) -> {
            if (json instanceof JsonObject obj) {
                EntityType<?> entityType = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(obj.get("entity_type").getAsString()));
                List<Skill> list = new ArrayList<>();
                if (obj.get("skills") instanceof JsonObject skills) {
                    String name = skills.get("name").getAsString();
                    Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(skills.get("item").getAsString()));
                    int costItem = skills.has("cost_item") ? 1 : skills.get("cost_item").getAsInt();
                    int seedCount = skills.has("cost_seed") ? 1 : skills.get("cost_seed").getAsInt();
                    int addCostResource = skills.has("add_cost_resource") ? 1 : skills.get("add_cost_resource").getAsInt();
                    int addCoolDown = skills.has("add_cool_down") ? 1 : skills.get("add_cool_down").getAsInt();
                    list.add(new Skill(name, () -> item, costItem, seedCount, addCostResource, addCoolDown));
                }
                map.put(entityType, list);
            }
        }));
        PVZSeedPackets.additionalSkills = map;
    }
}
