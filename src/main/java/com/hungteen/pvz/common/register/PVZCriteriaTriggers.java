package com.hungteen.pvz.common.register;

import com.hungteen.pvz.util.Util;
import net.minecraft.advancements.CriteriaTriggers;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.advancements.critereon.PlayerTrigger;

public class PVZCriteriaTriggers {

    public static final PlayerTrigger INVASION = trigger(new PlayerTrigger(Util.prefix("occur_invasion")));
    public static final PlayerTrigger SEED_CROSSBOW_PLANT = trigger(new PlayerTrigger(Util.prefix("seed_crossbow_plant")));
    public static final PlayerTrigger SPUDOW = trigger(new PlayerTrigger(Util.prefix("spudow")));
    public static final PlayerTrigger HARVEST_SPROUT = trigger(new PlayerTrigger(Util.prefix("harvest_sprout")));
    public static final PlayerTrigger HARVEST_MARIGOLD = trigger(new PlayerTrigger(Util.prefix("harvest_marigold")));
    public static final PlayerTrigger REPEATER = trigger(new PlayerTrigger(Util.prefix("repeater")));
    public static final PlayerTrigger STRIKE = trigger(new PlayerTrigger(Util.prefix("strike")));
    public static final PlayerTrigger SNAIL = trigger(new PlayerTrigger(Util.prefix("snail")));

    public static void init() {
    }

    public static <T extends CriterionTrigger<?>> T trigger(T p_10596_) {
        return CriteriaTriggers.register(p_10596_);
    }

}
