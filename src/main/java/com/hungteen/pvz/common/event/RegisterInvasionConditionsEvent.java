package com.hungteen.pvz.common.event;

import com.google.common.collect.ImmutableMap;
import com.hungteen.pvz.common.world.invasion.InvasionCondition;
import com.hungteen.pvz.util.Util;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.eventbus.api.Event;

public class RegisterInvasionConditionsEvent extends Event {
    public final ImmutableMap.Builder<ResourceLocation, InvasionCondition> builder = new ImmutableMap.Builder<>();

    public RegisterInvasionConditionsEvent() {
        this.builder.put(Util.prefix("and"), new InvasionCondition.And())
                .put(Util.prefix("or"), new InvasionCondition.Or())
                .put(Util.prefix("not"), new InvasionCondition.Not())
                .put(Util.prefix("nand"), new InvasionCondition.Nand())
                .put(Util.prefix("nor"), new InvasionCondition.Nor())
                .put(Util.prefix("xor"), new InvasionCondition.Xor())
                .put(Util.prefix("xnor"), new InvasionCondition.Xnor())
                .put(Util.prefix("conflict_with"), new InvasionCondition.ConflictWithCondition())
                .put(Util.prefix("is_underground"), new InvasionCondition.IsUndergroundCondition())
                .put(Util.prefix("obtained_advancement"), new InvasionCondition.ObtainedAdvancementCondition())
                .put(Util.prefix("has_item"), new InvasionCondition.HasItemCondition())
                .put(Util.prefix("in_dimension"), new InvasionCondition.InDimensionCondition())
                .put(Util.prefix("in_biome"), new InvasionCondition.InBiomeCondition())
                .put(Util.prefix("around_entities_cost"), new InvasionCondition.AroundEntitiesCostCondition());
    }
}
