package com.hungteen.pvz.common.world;

import com.google.gson.*;
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
                JsonArray jsonArray = obj.getAsJsonArray("skills");
                for (int i = 0; i < jsonArray.size(); i ++ ) {
                    if (jsonArray.get(i) instanceof JsonObject skill) {
                        String name = skill.get("name").getAsString();
                        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(skill.get("item").getAsString()));
                        int costItem = skill.has("cost_item") ? skill.get("cost_item").getAsInt() : 1;
                        int seedCount = skill.has("cost_seed") ? skill.get("cost_seed").getAsInt() : 1;
                        int addCostResource = skill.has("add_cost_resource") ? skill.get("add_cost_resource").getAsInt() : 0;
                        int addCoolDown = skill.has("add_cool_down") ? skill.get("add_cool_down").getAsInt() : 0;
                        list.add(new Skill(name, () -> item, costItem, seedCount, addCostResource, addCoolDown));
                    }
                }
                map.put(entityType, list);
            }
        }));
        PVZSeedPackets.additionalSkills = map;
    }
}
