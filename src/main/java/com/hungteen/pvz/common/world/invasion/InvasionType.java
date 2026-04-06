package com.hungteen.pvz.common.world.invasion;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.event.RegisterInvasionEntityModifiersEvent;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.common.util.TriPredicate;
import org.jetbrains.annotations.Nullable;

import java.util.*;

/**Server only. These are invasion types read from data pack, stored in {@link InvasionType#invasionTypes}.
 * @param loot The loot table of this invasion type. When an invasion contains multiple invasion types, only use the loot table of the main type.
 * @param weight The chance whether the invasion can take place. If larger than 1, As long as it doesn't conflict with other types, it will sure happen.
 * @param length The length the invasion lasts. The smaller the number is, the shorter time the invasion lasts.
 * @param isAddition Whether this type is additional invasion type. Additional invasion types can't be selected single, while only one non-additional types con be selected.
 * @param conditions See {@link InvasionCondition}.**/
public record InvasionType(Optional<ResourceLocation> loot, List<Pair<ResourceLocation, List<String>>> conditions, List<ResourceLocation> entityModifiers,
                           Optional<EnemyType> flagEnemy, List<EnemyType> enemies, boolean isAddition, boolean disableDirector, float threatFactor, float length, int weight) {
    public static Codec<InvasionType> CODEC = RecordCodecBuilder.create(builder -> builder.group(
            ResourceLocation.CODEC.optionalFieldOf("loot").forGetter(InvasionType::loot),
            Codec.compoundList(ResourceLocation.CODEC, Codec.STRING.listOf()).optionalFieldOf("conditions", List.of()).forGetter(InvasionType::conditions),
            ResourceLocation.CODEC.listOf().optionalFieldOf("entity_modifiers", List.of()).forGetter(InvasionType::entityModifiers),
            EnemyType.CODEC.optionalFieldOf("flag_enemy").forGetter(InvasionType::flagEnemy),
            EnemyType.CODEC.listOf().optionalFieldOf("enemies", List.of()).forGetter(InvasionType::enemies),
            Codec.BOOL.optionalFieldOf("is_addition", false).forGetter(InvasionType::isAddition),
            Codec.BOOL.optionalFieldOf("disable_director", false).forGetter(InvasionType::disableDirector),
            Codec.FLOAT.optionalFieldOf("threat_factor", 1F).forGetter(InvasionType::threatFactor),
            Codec.FLOAT.optionalFieldOf("length", 1F).forGetter(InvasionType::length),
            Codec.INT.optionalFieldOf("weight", 100).forGetter(InvasionType::weight)
        ).apply(builder, InvasionType::new)
    );
    public static Map<ResourceLocation, InvasionType> invasionTypes;
    private static final Random random = new Random();

    public static final Map<ResourceLocation, TriPredicate<Invasion, Entity, Integer>> invasionEntityModifiers = RegisterInvasionEntityModifiersEvent.get();

    public InvasionType(Optional<ResourceLocation> loot, List<Pair<ResourceLocation, List<String>>> conditions, List<ResourceLocation> entityModifiers,
                               Optional<EnemyType> flagEnemy, List<EnemyType> enemies, boolean isAddition, float threatFactor, float length, int weight) {
        this(loot, conditions, entityModifiers, flagEnemy, enemies, isAddition, false, threatFactor, length, weight);
    }

    //Methods

    public static List<InvasionType> generateTypes(LivingEntity target) {
        List<InvasionType> types = new ArrayList<>();
        //main
        int allWeight = 0;
        List<InvasionType> toChoose = new ArrayList<>();
        for (InvasionType invasionType : invasionTypes.values()) {
            if (! invasionType.isAddition && invasionType.isAvailable(target, types) && invasionType.weight > 0) {
                toChoose.add(invasionType);
                allWeight += invasionType.weight;
            }
        }
        if (toChoose.isEmpty()) {
            return types;
        }
        int chooseWeight = random.nextInt(allWeight);
        for (InvasionType invasionType : toChoose) {
            chooseWeight -= invasionType.weight;
            if (chooseWeight <= 0) {
                types.add(invasionType);
                break;
            }
        }
        //addition
        for (InvasionType invasionType : invasionTypes.values()) {
            if (invasionType.isAddition && invasionType.isAvailable(target, types) && random.nextInt(10000) < invasionType.weight) {
                types.add(invasionType);
                break;
            }
        }
        return types;
    }

    @Nullable
    public static InvasionType getInvasionType(ResourceLocation location) {
        if (invasionTypes.containsKey(location)) {
            return invasionTypes.get(location);
        }
        return null;
    }

    public boolean isAvailable(LivingEntity target, List<InvasionType> selectedTypes) {
        if (this.conditions != null) {
            for (Pair<ResourceLocation, List<String>> pair : this.conditions) {
                InvasionCondition condition = InvasionCondition.invasionConditions.get(pair.getFirst());
                if (condition == null) {
                    PVZMod.LOGGER.error("Found unavailable condition " + pair.getFirst() + " in invasion type " + this.getName() + "!");
                    return false;
                } else {
                    int argumentLength = condition.getArgLength(target, pair.getSecond(), this, selectedTypes);
                    if (! condition.test(target, pair.getSecond().subList(0, argumentLength), this, selectedTypes)) {
                        return false;
                    }
                }
            }
        }
        return true;
    }

    public ResourceLocation getName() {
        for (ResourceLocation location : invasionTypes.keySet()) {
            if (invasionTypes.get(location) == this) {
                return location;
            }
        }
        return null;
    }

    public List<TriPredicate<Invasion, Entity, Integer>> getModifiers() {
        return this.entityModifiers.stream().map(invasionEntityModifiers::get).toList();
    }

    //enemy type
    public record EnemyType(CompoundTag entityData, @Nullable List<Pair<ResourceLocation, List<String>>> conditions, int threat, int weight, boolean isElite, float startFrom, float endAt) {
        public static final Codec<EnemyType> CODEC = RecordCodecBuilder.create(builder -> builder.group(
                CompoundTag.CODEC.fieldOf("entity").forGetter(EnemyType::entityData),
                Codec.compoundList(ResourceLocation.CODEC, Codec.STRING.listOf()).optionalFieldOf("conditions", List.of()).forGetter(EnemyType::conditions),
                Codec.INT.fieldOf("threat").forGetter(EnemyType::threat),
                Codec.INT.optionalFieldOf("weight", 10).forGetter(EnemyType::weight),
                Codec.BOOL.optionalFieldOf("is_elite", false).forGetter(EnemyType::isElite),
                Codec.FLOAT.optionalFieldOf("start_from", 0F).forGetter(EnemyType::startFrom),
                Codec.FLOAT.optionalFieldOf("end_at", 1F).forGetter(EnemyType::endAt)
            ).apply(builder, EnemyType::new)
        );

        public EnemyType(CompoundTag entityData, @Nullable List<Pair<ResourceLocation, List<String>>> conditions, int threat, int weight, boolean isElite, float startFrom) {
            this(entityData, conditions, threat, weight, isElite, startFrom, 1F);
        }

        public EnemyType(CompoundTag entityData, int threat, int weight, boolean isElite, float startFrom) {
            this(entityData, List.of(), threat, weight, isElite, startFrom);
        }
    }
}
