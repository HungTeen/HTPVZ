package com.hungteen.pvz.common.event;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.hungteen.pvz.common.item.SproutItem;
import com.hungteen.pvz.common.register.OtherRegisters;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;
import java.util.Map;

public class RegisterSproutsEvent extends Event {
    public static Map<String, Map<String, Integer>> sproutsMap = new HashMap<>();

    /**these maps below are only for Creative Item Tab. loots are defined in data packs.*/
    public static final Map<String, Integer> COMMON = Map.of("pvz:split_pea", 6, "pvz:pea_shooter", 5, "pvz:wall_nut", 3, "pvz:potato_mine", 3, "pvz:marigold", 4);
    public static final Map<String, Integer> ICY = Map.of("pvz:iceberg_lettuce", 5, "pvz:potato_mine", 3, "pvz:snow_pea", 3, "pvz:flower_pot", 3);
    public static final Map<String, Integer> WATER = Map.of("pvz:lily_pad", 5, "pvz:tangle_kelp", 3);

    public static final Map<String, Integer> NETHER_AGGRESSIVE = Map.of("pvz:repeater", 5, "pvz:torch_wood", 3, "pvz:spike_weed", 3, "pvz:melon_pult", 3);
    public static final Map<String, Integer> NETHER_DEFENCIVE = Map.of("pvz:umbrella_leaf", 5, "pvz:tall_nut", 5, "pvz:pumpkin", 3, "pvz:plantern", 3);

    public static final Map<String, Integer> ENDER = Map.of("pvz:gatling_pea", 1);

    public RegisterSproutsEvent() {
        sproutsMap.clear();
        sproutsMap.putAll(Map.of("sprout.pvz.common", COMMON,
                "sprout.pvz.icy", ICY,
                "sprout.pvz.water", WATER,
                "sprout.pvz.nether_aggressive", NETHER_AGGRESSIVE,
                "sprout.pvz.nether_defensive", NETHER_DEFENCIVE,
                "sprout.pvz.ender", ENDER));
    }

    public static class SetSproutTypeFunction extends LootItemConditionalFunction {
        final String type;
        final Map<String, Integer> pool;

        static {
            RegisterSproutsEvent sproutEvent = new RegisterSproutsEvent();
            MinecraftForge.EVENT_BUS.post(sproutEvent);
        }

        protected SetSproutTypeFunction(LootItemCondition[] p_80678_, String type) {
            super(p_80678_);
            this.type = type;
            this.pool = null;
        }
        protected SetSproutTypeFunction(LootItemCondition[] p_80678_, String type, Map<String, Integer> pool) {
            super(p_80678_);
            this.type = type;
            this.pool = pool;
        }

        @Override
        protected ItemStack run(ItemStack itemStack, LootContext context) {
            if (pool == null) {
                return SproutItem.getTaggedItem(itemStack, type, sproutsMap.get(type));
            } else {
                return SproutItem.getTaggedItem(itemStack, type, pool);
            }
        }
        @Override
        public LootItemFunctionType getType() {
            return OtherRegisters.SET_SPROUT.get();
        }

        public static class Serializer extends LootItemConditionalFunction.Serializer<SetSproutTypeFunction> {
            public void serialize(JsonObject json, SetSproutTypeFunction function, JsonSerializationContext context) {
                super.serialize(json, function, context);
                json.addProperty("sprout_type", function.type);
                if (function.pool != null) {
                    JsonObject object = new JsonObject();
                    for (String type : function.pool.keySet()) {
                        object.addProperty(type, function.pool.get(type));
                    }
                    json.add("pool", object);
                }
            }
            @Override
            public SetSproutTypeFunction deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
                if (json.keySet().contains("pool")) {
                    JsonObject poolJson = json.getAsJsonObject("pool");
                    Map<String, Integer> pool = new HashMap<>();
                    for (String name : poolJson.keySet()) {
                        pool.put(name, pool.get(name));
                    }
                    return new SetSproutTypeFunction(conditions, json.get("sprout_type").getAsString(), pool);
                }
                return new SetSproutTypeFunction(conditions, json.get("sprout_type").getAsString());
            }
        }

        public static class Builder extends LootItemConditionalFunction.Builder<SetSproutTypeFunction.Builder> {
            String type;
            Map<String, Integer> pool = new HashMap<>();;
            public static Builder of(String type) {
                Builder builder = new Builder();
                builder.type = type;
                return builder;
            }
            public Builder withWeight(String type, int weight) {
                pool.put(type, weight);
                return this;
            }
            @Override
            protected Builder getThis() {
                return this;
            }
            @Override
            public SetSproutTypeFunction build() {
                return pool.isEmpty() ?
                        new SetSproutTypeFunction(this.getConditions(), type) :
                        new SetSproutTypeFunction(this.getConditions(), type, pool);
            }
        }
    }
}
