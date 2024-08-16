package com.hungteen.pvz.generator;

import com.google.gson.JsonElement;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.common.world.invasion.InvasionCondition;
import com.hungteen.pvz.common.world.invasion.InvasionEntityModifiers;
import com.hungteen.pvz.common.world.invasion.InvasionType;
import com.hungteen.pvz.util.Util;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.DataProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.nio.file.Path;
import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Stream;

public class InvasionTypeGen implements DataProvider {
    private final DataGenerator.PathProvider pathProvider;
    public InvasionTypeGen(DataGenerator generator) {
        this.pathProvider = generator.createPathProvider(DataGenerator.Target.DATA_PACK, "invasion_types");
    }
    @Override
    public void run(@NotNull CachedOutput output) {
        Map<ResourceLocation, InvasionType> map = this.getTypes();
        map.forEach((location, type) -> {
            Path path = this.pathProvider.json(location);
            Optional<JsonElement> jsonOptional = JsonOps.INSTANCE.withEncoder(InvasionType.CODEC).apply(type).result();
            if (jsonOptional.isEmpty()) {
                PVZMod.LOGGER.error("Couldn't serialize invasion type {}", path);
            } else {
                try {
                    DataProvider.saveStable(output, jsonOptional.get(), path);
                } catch (IOException ioexception) {
                    PVZMod.LOGGER.error("Couldn't save invasion type {}", path, ioexception);
                }
            }
        });
    }

    public Map<ResourceLocation, InvasionType> getTypes() {
        Map<ResourceLocation, InvasionType> map = new HashMap<>();
        map.put(Util.prefix("babylize"), new InvasionType(loot(), List.of(),
                entityModifiers(InvasionEntityModifiers.BABYLIZE),
                Optional.empty(), List.of(), true, 1,0.005F
        ));
        map.put(Util.prefix("test"), new InvasionType(loot("chests/spawn_bonus_chest"),
                conditions(
                        put(new InvasionCondition.InDimensionCondition(), "minecraft:overworld")
                ),
                entityModifiers(InvasionEntityModifiers.ADD_LIFEBUOY, InvasionEntityModifiers.FINALIZE_SPAWN),
                Optional.of(EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZZombie.getOverworldBanner()).get()),
                List.of(
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).get(),
                                List.of(), 100, 20, false, 0
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.CONE_HELMET.get().getDefaultInstance()).get(),
                                List.of(), 300, 15, false, 0.1F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.HEAD, PVZItems.BUCKET_HELMET.get().getDefaultInstance()).get(),
                                List.of(), 500, 5, false, 0.4F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.ZOMBIE.get()).equip(EquipmentSlot.MAINHAND, PVZItems.SCREEN_DOOR_SHIELD.get().getDefaultInstance()).get(),
                                List.of(), 500, 5, false, 0.4F
                        ),
                        new InvasionType.EnemyType(
                                EntityBuilder.of(PVZEntities.GARGANTUAR.get()).get(),
                                List.of(), 800, 10, true, 0.5F
                        )
                ),
                false, 1,1
        ));
        return map;
    }

    @Override
    public @NotNull String getName() {
        return "PVZInvasionTypes";
    }


    //serializing tools
    @SafeVarargs
    protected final List<Pair<ResourceLocation, List<String>>> conditions(Pair<ResourceLocation, List<String>>... conditions) {
        return List.of(conditions);
    }

    protected Pair<ResourceLocation, List<String>> put(InvasionCondition condition, String... arguments) {
        return Pair.of(condition.getName(), Arrays.stream(arguments).toList());
    }

    protected List<ResourceLocation> entityModifiers(String... modifiers) {
        return Arrays.stream(modifiers).map(string -> {
            ResourceLocation location = new ResourceLocation(string);
            if (! InvasionType.invasionEntityModifiers.containsKey(location)) {
                PVZMod.LOGGER.error("added incorrect entity modifier name in {}", location);
                throw new RuntimeException();
            }
            return location;
        }).toList();
    }

    protected List<ResourceLocation> entityModifiers(ResourceLocation... modifiers) {
        return Arrays.stream(modifiers).peek(location -> {
            if (! InvasionType.invasionEntityModifiers.containsKey(location)) {
                PVZMod.LOGGER.error("added incorrect entity modifier name in {}", location);
                throw new RuntimeException();
            }
        }).toList();
    }

    protected Optional<ResourceLocation> loot() {
        return Optional.empty();
    }
    protected Optional<ResourceLocation> loot(String string) {
        return loot(new ResourceLocation(string));
    }
    protected Optional<ResourceLocation> loot(ResourceLocation location) {
        return Optional.of(location);
    }

    protected static class EntityBuilder<E extends Entity> {
        private final CompoundTag tag = new CompoundTag();
        static <E extends Entity> EntityBuilder<E> of(EntityType<E> type) {
            EntityBuilder<E> builder = new EntityBuilder<>();
            builder.tag.putString("id", ForgeRegistries.ENTITY_TYPES.getKey(type).toString());
            return builder;
        }

        public EntityBuilder<E> equip(EquipmentSlot slot, ItemStack item) {
            String type = slot.getType() == EquipmentSlot.Type.ARMOR ? "ArmorItems" : "HandItems";
            ListTag slots = ((ListTag) this.tag.get(type));
            List<Tag> list = new ArrayList<>(slots == null ?
                    (slot.getType() == EquipmentSlot.Type.ARMOR ?
                            Stream.of(new CompoundTag(), new CompoundTag(), new CompoundTag(), new CompoundTag()) :
                            Stream.of(new CompoundTag(), new CompoundTag())).toList() : slots.stream().toList());
            CompoundTag itemTag = new CompoundTag();
            list.set(slot.getIndex(), item.save(itemTag));
            ListTag newTag = new ListTag();
            newTag.addAll(list);
            this.tag.put(type, newTag);
            return this;
        }
        public EntityBuilder<E> attribute(Attribute attribute, double value) {
            if (! this.tag.contains("Attributes")) {
                this.tag.put("Attributes", new ListTag());
            }
            CompoundTag attributeTag = new CompoundTag();
            attributeTag.putString("Name", attribute.getDescriptionId());
            attributeTag.putDouble("Base", value);
            ((ListTag) this.tag.get("Attributes")).add(attributeTag);
            return this;
        }
        public EntityBuilder<E> putTag(String string, CompoundTag tag) {
            tag.put(string, tag);
            return this;
        }
        public EntityBuilder<E> modifyTag(String string, Consumer<CompoundTag> tag) {
            tag.accept(this.tag.contains(string) ? this.tag.getCompound(string) : null);
            return this;
        }
        public EntityBuilder<E> passenger(EntityBuilder<? extends Entity> passenger) {
            if (! this.tag.contains("Passengers")) {
                this.tag.put("Passengers", new ListTag());
            }
            ((ListTag) this.tag.get("Passengers")).add(passenger.get());
            return this;
        }

        CompoundTag get() {
            return tag;
        }
    }
}
