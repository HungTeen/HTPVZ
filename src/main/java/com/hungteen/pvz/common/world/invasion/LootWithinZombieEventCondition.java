package com.hungteen.pvz.common.world.invasion;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.hungteen.pvz.api.PVZAPI;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.capability.level.PVZZombieEventCapability;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditionType;
import net.minecraft.world.level.storage.loot.predicates.LootItemConditions;

import java.util.concurrent.atomic.AtomicBoolean;

public class LootWithinZombieEventCondition implements LootItemCondition {
    final ResourceLocation eventType;
    public LootWithinZombieEventCondition(ResourceLocation eventType) {
        this.eventType = eventType;
    }
    @Override
    public LootItemConditionType getType() {
        return LootItemConditions.ENTITY_PROPERTIES;
    }

    @Override
    public boolean test(LootContext lootContext) {
        Entity entity = lootContext.getParamOrNull(LootContextParams.THIS_ENTITY);
        if (entity == null) return false;
        AtomicBoolean result = new AtomicBoolean(false);
        PVZZombieEventCapability levelCap = PVZZombieEventCapability.fromLevel(entity.level);
        if (levelCap == null) return false;
        entity.getCapability(PVZEntityCapability.CAP).ifPresent((cap) -> result.set(cap.isInZombieEvent() &&
                cap.zombieEventUUIDs.stream().anyMatch(uuid -> levelCap.hasEvent(uuid)
                        && PVZAPI.get().getZombieEventType(levelCap.getEvent(uuid)).equals(eventType))));
        return result.get();
    }


    public static class Serializer implements net.minecraft.world.level.storage.loot.Serializer<LootWithinZombieEventCondition> {
        @Override
        public void serialize(JsonObject json, LootWithinZombieEventCondition condition, JsonSerializationContext context) {
            json.addProperty("event_type", condition.eventType.toString());
        }

        @Override
        public LootWithinZombieEventCondition deserialize(JsonObject json, JsonDeserializationContext context) {
            ResourceLocation eventType = new ResourceLocation(json.get("event_type").getAsString());
            return new LootWithinZombieEventCondition(eventType);
        }
    }
}
