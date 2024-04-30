package com.hungteen.pvz.api.events;

import net.minecraftforge.eventbus.api.Event;

import java.util.HashMap;
import java.util.Map;

public class RegisterSproutsEvent extends Event {
    public Map<String, Map<String, Integer>> sproutsMap = new HashMap<>();

    /**this map below are only for Creative Item Tab. loots are defined in data packs.*/
    public static final Map<String, Integer> COMMON = Map.of("pvz:split_pea", 8, "pvz:pea_shooter", 5, "pvz:wall_nut", 3, "pvz:potato_mine", 3);
    public static final Map<String, Integer> ICY = Map.of("pvz:iceberg_lettuce", 5, "pvz:potato_mine", 3, "pvz:snow_pea", 3, "pvz:flower_pot", 3);
    public static final Map<String, Integer> WATER = Map.of("pvz:lily_pad", 5, "pvz:tangle_kelp", 2);

    public static final Map<String, Integer> NETHER_AGGRESSIVE = Map.of("pvz:repeater", 5, "pvz:torch_wood", 3, "pvz:spike_weed", 3);
    public static final Map<String, Integer> NETHER_DEFENCIVE = Map.of("pvz:pumpkin", 3, "pvz:tall_nut", 5, "pvz:umbrella_leaf", 5, "pvz:plantern", 3);

    public static final Map<String, Integer> ENDER = Map.of("pvz:gatling_pea", 1);

    public RegisterSproutsEvent() {
        this.sproutsMap.putAll(Map.of("sprout.pvz.common", COMMON,
                "sprout.pvz.icy", ICY,
                "sprout.pvz.water", WATER,
                "sprout.pvz.nether_aggressive", NETHER_AGGRESSIVE,
                "sprout.pvz.nether_defensive", NETHER_DEFENCIVE,
                "sprout.pvz.ender", ENDER));
    }
}
