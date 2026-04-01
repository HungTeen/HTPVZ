package com.hungteen.pvz.common.world.invasion;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.PVZAPI;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapStats;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.event.RegisterInvasionConditionsEvent;
import net.minecraft.ResourceLocationException;
import net.minecraft.advancements.Advancement;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.TagKey;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicInteger;

/**InvasionConditions are predicates deciding whether an invasion type can be accepted at the situation the player is in.
 * An invasion type is accepted only when all conditions are met.
 * <br>However, you can still use conditions like {@link And}, {@link Or} and {@link Not} to enable more complex conditions.
 * <br>Sometimes invasion types need to know how many arguments are needed because of the nested conditions.
 * Use {@link InvasionCondition#getArgLength(LivingEntity, List, InvasionType, List) getArgLength()} to define now many arguments this condition need, And the redundant conditions will be ignored.*/
public interface InvasionCondition {
    Map<ResourceLocation, InvasionCondition> invasionConditions = registerConditions();
    boolean test(LivingEntity target, List<String> arguments, InvasionType type, List<InvasionType> selectedTypes);
    default int getArgLength(LivingEntity target, List<String> allProvidedArgs, InvasionType type, List<InvasionType> selectedTypes) {
        //accepts all following ResourceLocations, stops at the end, non-ResourceLocation and non-tag argument.
        int i = 0;
        while (i < allProvidedArgs.size()) {
            try {
                String arg = allProvidedArgs.get(i);
                if (! arg.startsWith("#")) {
                    new ResourceLocation(arg);
                    if (arg.startsWith("$")) {
                        throw new ResourceLocationException(arg);
                    }
                }
            } catch (ResourceLocationException exception) {
                return i;
            }
            i ++;
        }
        return i;
    }

    default ResourceLocation getName() {
        final ResourceLocation[] location = new ResourceLocation[1];
        invasionConditions.keySet().forEach(key -> {
            if (invasionConditions.get(key).getClass().equals(this.getClass())) {
                location[0] = key;
            }
        });
        return location[0];
    }

    static Map<ResourceLocation, InvasionCondition> registerConditions() {
        RegisterInvasionConditionsEvent event = new RegisterInvasionConditionsEvent();
        MinecraftForge.EVENT_BUS.post(event);
        return event.builder.build();
    }

    /**Detects which dimension target is in. Accepts only 1 argument.*/
    class InDimensionCondition implements InvasionCondition {
        @Override
        public boolean test(LivingEntity target, List<String> arguments, InvasionType type, List<InvasionType> selectedTypes) {
            return target.level.dimension().location().equals(new ResourceLocation(arguments.get(0)));
        }

        @Override
        public int getArgLength(LivingEntity target, List<String> allProvidedArgs, InvasionType type, List<InvasionType> selectedTypes) {
            return 1;
        }
    }

    /**Detects whether an invasion type is chosen, accepting multiple arguments. When having more than 1 argument, all of them should not be selected.
     * <br>You can use {@link Not} to select this type when another type is selected, but notice that the types are detected in random order.*/
    class ConflictWithCondition implements InvasionCondition {
        @Override
        public boolean test(LivingEntity target, List<String> arguments, InvasionType type, List<InvasionType> selectedTypes) {
            if (arguments.isEmpty()) {
                PVZMod.LOGGER.warn("Condition conflict_with of " + type.getName() + "received no arguments.");
            }
            List<ResourceLocation> conflistList = arguments.stream().map(ResourceLocation::new).toList();
            for (InvasionType selected : selectedTypes) {
                for (ResourceLocation location : conflistList) {
                    if (selected.getName().equals(location)) {
                        return false;
                    }
                }
            }
            return true;
        }
    }

    /**Detects whether target is in a wanted biome. Accepts multiple arguments.*/
    class InBiomeCondition implements InvasionCondition {
        @Override
        public boolean test(LivingEntity target, List<String> arguments, InvasionType type, List<InvasionType> selectedTypes) {
            if (arguments.isEmpty()) {
                PVZMod.LOGGER.warn("Condition in_biome of " + type.getName() + " received no arguments.");
            }
            for (String argument : arguments) {
                if (argument.startsWith("#")) {
                    ResourceLocation resourcelocation = new ResourceLocation(argument.substring(1));
                    TagKey<Biome> tagkey = TagKey.create(Registry.BIOME_REGISTRY, resourcelocation);
                    if (target.level.getBiome(target.blockPosition()).is(tagkey)) {
                        return true;
                    }
                } else {
                    if (target.level.getBiome(target.blockPosition()).unwrap()
                            .map(key -> key.location().toString().equals(argument), biome -> false)) {
                        return true;
                    }
                }
            }
            return false;
        }
    }

    class InvasionDifficultyGreaterThanCondition implements InvasionCondition {
        @Override
        public int getArgLength(LivingEntity target, List<String> allProvidedArgs, InvasionType type, List<InvasionType> selectedTypes) {
            try {
                if (! allProvidedArgs.isEmpty()) {
                    Integer.parseInt(allProvidedArgs.get(0));
                    return 1;
                }
            } catch (Exception ignored) {
            }
            return 0;
        }
        @Override
        public boolean test(LivingEntity target, List<String> arguments, InvasionType type, List<InvasionType> selectedTypes) {
            int value = 0;
            try {
                if (! arguments.isEmpty()) {
                    value = Integer.parseInt(arguments.get(0));
                }
            } catch (Exception ignored) {
            }
            if (target instanceof Player player) {
                return player.isCreative() ? true : value >= PVZPlayerCapability.getValue(player, PVZPlayerCapStats.INVASION_DIFFICULTY);
            }
            return false;
        }
    }

    class IsUndergroundCondition implements InvasionCondition {
        @Override
        public boolean test(LivingEntity target, List<String> arguments, InvasionType type, List<InvasionType> selectedTypes) {
            return target.level.getHeight(Heightmap.Types.WORLD_SURFACE, (int) target.getX(), (int) target.getZ()) > target.getY() + 20;
        }
        @Override
        public int getArgLength(LivingEntity target, List<String> allProvidedArgs, InvasionType type, List<InvasionType> selectedTypes) {
            return 0;
        }
    }

    class AroundEntitiesCostCondition implements InvasionCondition {
        @Override
        public boolean test(LivingEntity target, List<String> arguments, InvasionType type, List<InvasionType> selectedTypes) {
            String resource;
            int cost;
            int numAt = 0;
            try {
                if (arguments.isEmpty()) {
                    numAt = -1;
                } else {
                    Integer.parseInt(arguments.get(0));
                }
            } catch (Exception ignored) {
                numAt = arguments.size() > 1 ? 1 : -1;
            }
            resource = numAt == 1 ? arguments.get(0) : PVZAPI.get().getSunResourceName();
            cost = numAt >= 0 ? Integer.parseInt(arguments.get(numAt)) : 500;
            if (target instanceof Player player && player.isCreative()) {
                return true;
            }
            List<Entity> entities = target.level.getEntities(target, target.getBoundingBox().inflate(20));
            AtomicInteger totalCost = new AtomicInteger();
            entities.forEach(entity -> entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap ->
                    totalCost.addAndGet(cap.resource.equals(resource) ? cap.cost : 0)));
            return totalCost.get() < cost;
        }
        @Override
        public int getArgLength(LivingEntity target, List<String> allProvidedArgs, InvasionType type, List<InvasionType> selectedTypes) {
            try {
                if (allProvidedArgs.isEmpty()) {
                    return 0;
                } else {
                    Integer.parseInt(allProvidedArgs.get(0));
                    return 1;
                }
            } catch (Exception ignored) {
                return 2;
            }
        }
    }

    /**Detects whether target player has achieved given advancement, accepting multiple arguments. When having more than 1 argument, all of them should be obtained.*/
    class ObtainedAdvancementCondition implements InvasionCondition {
        @Override
        public boolean test(LivingEntity target, List<String> arguments, InvasionType type, List<InvasionType> selectedTypes) {
            if (arguments.isEmpty()) {
                PVZMod.LOGGER.warn("Condition obtained_advancement of " + type.getName() + "received no arguments.");
            }
            if (target instanceof ServerPlayer player) {
                for (String argument : arguments) {
                    Advancement advancement = ((ServerLevel) player.level).getServer().getAdvancements().getAdvancement(new ResourceLocation(argument));
                    if (advancement == null) {
                        PVZMod.LOGGER.error("Argument achievement " + argument + " in obtained_advancement condition of " + type.getName() + " is not available.");
                        return false;
                    }
                    if (!player.getAdvancements().getOrStartProgress(advancement).isDone()) {
                        return false;
                    }
                }
                return true;
            }
            return false;
        }
    }

    /**Detects whether target player has given items, accepting multiple arguments. When having more than 1 argument, all of them should be detected.*/
    class HasItemCondition implements InvasionCondition {
        @Override
        public boolean test(LivingEntity target, List<String> arguments, InvasionType type, List<InvasionType> selectedTypes) {
            if (arguments.isEmpty()) {
                PVZMod.LOGGER.warn("Condition has_item of " + type.getName() + "received no arguments.");
            }
            if (target instanceof Player player) {
                for (String string : arguments) {
                    if (string.startsWith("#")) {
                        ResourceLocation resourcelocation = new ResourceLocation(string.substring(1));
                        TagKey<Item> tagkey = TagKey.create(Registry.ITEM_REGISTRY, resourcelocation);
                        if (! player.getInventory().contains(tagkey)) {
                            return false;
                        }
                    } else {
                        Item item = ForgeRegistries.ITEMS.getValue(new ResourceLocation(string));
                        if (item == null) {
                            PVZMod.LOGGER.error("Argument item " + string + " in has_item condition of " + type.getName() + " is not available.");
                            return false;
                        }
                        if (! player.getInventory().hasAnyOf(Set.of(item))) {
                            return false;
                        }
                    }
                }
                return true;
            }
            return false;
        }
    }

    //TODO add a time condition to limit some invasions happening only at night.

    /**Logic conditions.*/
    class And implements InvasionCondition {
        @Override
        public boolean test(LivingEntity target, List<String> arguments, InvasionType type, List<InvasionType> selectedTypes) {
            if (arguments.isEmpty()) {
                return false;
            }
            InvasionCondition first = invasionConditions.get(new ResourceLocation(arguments.get(0).substring(1)));
            int firstLength = first.getArgLength(target, arguments.subList(1, arguments.size()), type, selectedTypes);
            InvasionCondition second = invasionConditions.get(new ResourceLocation(arguments.get(firstLength + 2).substring(1)));
            int secondLength = second.getArgLength(target, arguments.subList(firstLength + 3, arguments.size()), type, selectedTypes);
            return first.test(target, arguments.subList(1, firstLength + 1), type, selectedTypes) &&
                    second.test(target, arguments.subList(firstLength + 3, firstLength + 3 + secondLength), type, selectedTypes);
        }

        @Override
        public int getArgLength(LivingEntity target, List<String> allProvidedArgs, InvasionType type, List<InvasionType> selectedTypes) {
            if (allProvidedArgs.isEmpty() || ! allProvidedArgs.get(0).startsWith("$")) {
                PVZMod.LOGGER.error("First condition in \"and\" condition of " + type.getName() + " not found, expected condition starting with \"$\".");
                return 0;
            }
            InvasionCondition first = invasionConditions.get(new ResourceLocation(allProvidedArgs.get(0).substring(1)));
            if (first == null) {
                PVZMod.LOGGER.error("First condition " + new ResourceLocation(allProvidedArgs.get(0)) +
                        " in \"and\" condition of " + type.getName() + "invasion type is not available!");
                return 0;
            }
            int afterFirst = first.getArgLength(target, allProvidedArgs.subList(1, allProvidedArgs.size()), type, selectedTypes) + 1;
            if (! allProvidedArgs.get(afterFirst).equals("&&")) {
                PVZMod.LOGGER.error("Found " + allProvidedArgs.get(afterFirst) + " after first condition in \"and\" condition of " + type.getName() + ", expected \"&&\".");
                return 0;
            }
            if (! allProvidedArgs.get(afterFirst + 1).startsWith("$")) {
                PVZMod.LOGGER.error("Second condition in \"and\" condition of " + type.getName() + " not found, expected condition starting with \"$\".");
                return 0;
            }
            InvasionCondition second = invasionConditions.get(new ResourceLocation(allProvidedArgs.get(afterFirst + 1).substring(1)));
            if (second == null) {
                PVZMod.LOGGER.error("Second condition " + new ResourceLocation(allProvidedArgs.get(afterFirst + 1)) +
                        " in \"and\" condition of " + type.getName() + "invasion type is not available!");
                return 0;
            }
            return afterFirst + second.getArgLength(target, allProvidedArgs.subList(afterFirst + 2, allProvidedArgs.size()), type, selectedTypes) + 2;
        }
    }

    class Or implements InvasionCondition {
        @Override
        public boolean test(LivingEntity target, List<String> arguments, InvasionType type, List<InvasionType> selectedTypes) {
            if (arguments.isEmpty()) {
                return false;
            }
            InvasionCondition first = invasionConditions.get(new ResourceLocation(arguments.get(0).substring(1)));
            int firstLength = first.getArgLength(target, arguments.subList(1, arguments.size()), type, selectedTypes);
            InvasionCondition second = invasionConditions.get(new ResourceLocation(arguments.get(firstLength + 2).substring(1)));
            int secondLength = second.getArgLength(target, arguments.subList(firstLength + 3, arguments.size()), type, selectedTypes);
            return first.test(target, arguments.subList(1, firstLength + 1), type, selectedTypes) ||
                    second.test(target, arguments.subList(firstLength + 3, firstLength + 3 + secondLength), type, selectedTypes);
        }

        @Override
        public int getArgLength(LivingEntity target, List<String> allProvidedArgs, InvasionType type, List<InvasionType> selectedTypes) {
            if (allProvidedArgs.isEmpty() || ! allProvidedArgs.get(0).startsWith("$")) {
                PVZMod.LOGGER.error("First condition in \"or\" condition of " + type.getName() + " not found, expected condition starting with \"$\".");
                return 0;
            }
            InvasionCondition first = invasionConditions.get(new ResourceLocation(allProvidedArgs.get(0).substring(1)));
            if (first == null) {
                PVZMod.LOGGER.error("First condition " + new ResourceLocation(allProvidedArgs.get(0)) +
                        " in \"or\" condition of " + type.getName() + "invasion type is not available!");
                return 0;
            }
            int afterFirst = first.getArgLength(target, allProvidedArgs.subList(1, allProvidedArgs.size()), type, selectedTypes) + 1;
            if (! allProvidedArgs.get(afterFirst).equals("||")) {
                PVZMod.LOGGER.error("Found " + allProvidedArgs.get(afterFirst) + " after first condition in \"or\" condition of " + type.getName() + ", expected \"||\".");
                return 0;
            }
            if (! allProvidedArgs.get(afterFirst + 1).startsWith("$")) {
                PVZMod.LOGGER.error("Second condition in \"or\" condition of " + type.getName() + " not found, expected condition starting with \"$\".");
                return 0;
            }
            InvasionCondition second = invasionConditions.get(new ResourceLocation(allProvidedArgs.get(afterFirst + 1).substring(1)));
            if (second == null) {
                PVZMod.LOGGER.error("Second condition " + new ResourceLocation(allProvidedArgs.get(afterFirst + 1)) +
                        " in \"or\" condition of " + type.getName() + "invasion type is not available!");
                return 0;
            }
            return afterFirst + second.getArgLength(target, allProvidedArgs.subList(afterFirst + 2, allProvidedArgs.size()), type, selectedTypes) + 2;
        }
    }

    class Not implements InvasionCondition {
        @Override
        public boolean test(LivingEntity target, List<String> arguments, InvasionType type, List<InvasionType> selectedTypes) {
            if (arguments.isEmpty()) {
                return false;
            }
            InvasionCondition condition = invasionConditions.get(new ResourceLocation(arguments.get(0).substring(1)));
            return ! condition.test(target, arguments.subList(1, arguments.size()), type, selectedTypes);
        }

        @Override
        public int getArgLength(LivingEntity target, List<String> allProvidedArgs, InvasionType type, List<InvasionType> selectedTypes) {
            if (allProvidedArgs.isEmpty() || ! allProvidedArgs.get(0).startsWith("$")) {
                PVZMod.LOGGER.error("Condition in \"not\" condition of " + type.getName() + " not found, expected condition starting with \"$\".");
                return 0;
            }
            InvasionCondition condition = invasionConditions.get(new ResourceLocation(allProvidedArgs.get(0).substring(1)));
            if (condition == null) {
                PVZMod.LOGGER.error("Condition " + new ResourceLocation(allProvidedArgs.get(0)) +
                        " in \"not\" condition of " + type.getName() + "invasion type is not available!");
                return 0;
            }
            return condition.getArgLength(target, allProvidedArgs.subList(1, allProvidedArgs.size()), type, selectedTypes) + 1;
        }
    }

    class Xor implements InvasionCondition {
        @Override
        public boolean test(LivingEntity target, List<String> arguments, InvasionType type, List<InvasionType> selectedTypes) {
        if (arguments.isEmpty()) {
            return false;
        }
        InvasionCondition first = invasionConditions.get(new ResourceLocation(arguments.get(0).substring(1)));
        int firstLength = first.getArgLength(target, arguments.subList(1, arguments.size()), type, selectedTypes);
        InvasionCondition second = invasionConditions.get(new ResourceLocation(arguments.get(firstLength + 2).substring(1)));
        int secondLength = second.getArgLength(target, arguments.subList(firstLength + 3, arguments.size()), type, selectedTypes);
        return first.test(target, arguments.subList(1, firstLength + 1), type, selectedTypes) !=
                second.test(target, arguments.subList(firstLength + 3, firstLength + 3 + secondLength), type, selectedTypes);
    }

        @Override
        public int getArgLength(LivingEntity target, List<String> allProvidedArgs, InvasionType type, List<InvasionType> selectedTypes) {
            if (allProvidedArgs.isEmpty() || ! allProvidedArgs.get(0).startsWith("$")) {
                PVZMod.LOGGER.error("First condition in \"and\" condition of " + type.getName() + " not found, expected condition starting with \"$\".");
                return 0;
            }
            InvasionCondition first = invasionConditions.get(new ResourceLocation(allProvidedArgs.get(0).substring(1)));
            if (first == null) {
                PVZMod.LOGGER.error("First condition " + new ResourceLocation(allProvidedArgs.get(0)) +
                        " in \"and\" condition of " + type.getName() + "invasion type is not available!");
                return 0;
            }
            int afterFirst = first.getArgLength(target, allProvidedArgs.subList(1, allProvidedArgs.size()), type, selectedTypes) + 1;
            if (! allProvidedArgs.get(afterFirst).equals("&&")) {
                PVZMod.LOGGER.error("Found " + allProvidedArgs.get(afterFirst) + " after first condition in \"and\" condition of " + type.getName() + ", expected \"&&\".");
                return 0;
            }
            if (! allProvidedArgs.get(afterFirst + 1).startsWith("$")) {
                PVZMod.LOGGER.error("Second condition in \"and\" condition of " + type.getName() + " not found, expected condition starting with \"$\".");
                return 0;
            }
            InvasionCondition second = invasionConditions.get(new ResourceLocation(allProvidedArgs.get(afterFirst + 1).substring(1)));
            if (second == null) {
                PVZMod.LOGGER.error("Second condition " + new ResourceLocation(allProvidedArgs.get(afterFirst + 1)) +
                        " in \"and\" condition of " + type.getName() + "invasion type is not available!");
                return 0;
            }
            return afterFirst + second.getArgLength(target, allProvidedArgs.subList(afterFirst + 2, allProvidedArgs.size()), type, selectedTypes) + 2;
        }
    }

    class Nand extends And {
        @Override
        public boolean test(LivingEntity target, List<String> arguments, InvasionType type, List<InvasionType> selectedTypes) {
            return ! super.test(target, arguments, type, selectedTypes);
        }
    }

    class Nor extends Or {
        @Override
        public boolean test(LivingEntity target, List<String> arguments, InvasionType type, List<InvasionType> selectedTypes) {
            return ! super.test(target, arguments, type, selectedTypes);
        }
    }

    class Xnor extends Xor {
        @Override
        public boolean test(LivingEntity target, List<String> arguments, InvasionType type, List<InvasionType> selectedTypes) {
            return ! super.test(target, arguments, type, selectedTypes);
        }
    }
}
