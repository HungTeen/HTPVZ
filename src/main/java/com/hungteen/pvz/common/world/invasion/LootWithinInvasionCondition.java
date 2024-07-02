package com.hungteen.pvz.common.world.invasion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

public class LootWithinInvasionCondition implements LootItemCondition {
    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ENTITY_PROPERTIES;
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        final boolean[] result = new boolean[1];
        entity.getCapability(PVZEntityCapability.CAP).ifPresent((cap) -> result[0] = cap.isInInvasion());
        return result[0];
    }


    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootWithinInvasionCondition> {
        @Override
        public void serialize(JsonObject json, LootWithinInvasionCondition condition, JsonSerializationContext context) {
        }

        @Override
        public LootWithinInvasionCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            return new LootWithinInvasionCondition();
        }
    }
}
