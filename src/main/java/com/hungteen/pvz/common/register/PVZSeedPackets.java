package com.hungteen.pvz.common.register;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.events.RegisterSeedPacketsEvent;
import com.hungteen.pvz.common.entity.plants.*;
import com.mojang.logging.LogUtils;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.RegistryObject;
import org.slf4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**This class is used for convenience only when registering. Then all the data in PVZSeedPackets.seedPackets will be released.
 */
public class PVZSeedPackets {
    private static final Logger LOGGER = LogUtils.getLogger();

    public static final int VERY_FAST = 10; //will a plant really use this?
    public static final int FAST = 40;
    public static final int MEDIUM = 300;
    public static final int SLOW = 500;
    public static final int VERY_SLOW = 1000;
    @Deprecated //clear after registry.
    public static final List<RegisterSeedPacketsEvent.SeedPacketData<?>> seedPacketData = new ArrayList<>();

    public static volatile Map<Item, RegisterSeedPacketsEvent.SeedPacketData<?>> dataMap = Map.of();
    public static volatile List<Item> tabsSurvival = List.of();
    public static volatile List<Item> tabsCreative = List.of();
    public static volatile Map<Item, List<Item>> sortedSurvival = Map.of();
    public static volatile Map<Item, List<Item>> sortedCreative = Map.of();
    public static volatile Map<EntityType<?>, List<Skill>> additionalSkills = Map.of();

    private static boolean initialized = false;
    private static boolean sorted = false;

    private PVZSeedPackets() {
    }

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        //pvz packets.

        //overworld
        add(PVZEntities.PEA_SHOOTER).cost(100).coolDown(FAST).skillList(PeaShooter.staticSkillList)
                .recipe(PVZItems.PEA, PVZItems.FLOWER_SEED_PACKET, PVZItems.VENTUS_ESSENCE);
        add(PVZEntities.SUN_FLOWER).cost(50).coolDown(MEDIUM).skillList(SunFlower.staticSkillList)
                .recipe(Items.SUNFLOWER, PVZItems.FLOWER_SEED_PACKET, PVZItems.LUX_ESSENCE);
        add(PVZEntities.WALL_NUT).cost(50).coolDown(MEDIUM).skillList(WallNut.staticSkillList)
                .recipe(PVZItems.NUT, PVZItems.FLOWER_SEED_PACKET, PVZItems.TERRA_ESSENCE);
        add(PVZEntities.POTATO_MINE).cost(25).coolDown(MEDIUM).skillList(PotatoMine.staticSkillList)
                .recipe(Items.POTATO, PVZItems.FLOWER_SEED_PACKET, PVZItems.TERRA_ESSENCE)
                .note(Component.translatable("container.pvz.almanac.can_plant_on_dirt"));
        add(PVZEntities.SNOW_PEA).cost(175).coolDown(FAST).skillList(SnowPea.staticSkillList)
                .recipe(getData(PVZEntities.PEA_SHOOTER), PVZItems.FLOWER_SEED_PACKET, PVZItems.GELUM_ESSENCE);
        add(PVZEntities.LILY_PAD).cost(25).coolDown(FAST).skillList(LilyPad.staticSkillList)
                .recipe(Items.LILY_PAD, PVZItems.FLOWER_SEED_PACKET, PVZItems.AQUA_ESSENCE)
                .note(Component.translatable("container.pvz.almanac.can_only_plant_on_water"));
        add(PVZEntities.TANGLE_KELP).cost(25).coolDown(SLOW).skillList(TangleKelp.staticSkillList)
                .recipe(Items.KELP, PVZItems.FLOWER_SEED_PACKET, PVZItems.AQUA_ESSENCE)
                .note(Component.translatable("container.pvz.almanac.can_only_plant_on_water"));
        add(PVZEntities.PLANTERN).cost(25).coolDown(VERY_SLOW).skillList(Plantern.staticSkillList)
                .recipeBlock(PVZBlocks.PLANTERN, PVZItems.FLOWER_SEED_PACKET, PVZItems.LUX_ESSENCE);
        add(PVZEntities.SPLIT_PEA).cost(150).coolDown(MEDIUM).skillList(SplitPea.staticSkillList)
                .recipe(PVZItems.FLOWER_SEED_PACKET);
        add(PVZEntities.CABBAGE_PULT).cost(100).coolDown(FAST).skillList(CabbagePult.staticSkillList)
                .recipe(PVZItems.CABBAGE, PVZItems.FLOWER_SEED_PACKET, PVZItems.VENTUS_ESSENCE);
        add(PVZEntities.FLOWER_POT).cost(25).coolDown(FAST).skillList(FlowerPot.staticSkillList)
                .recipe(Items.FLOWER_POT, PVZItems.FLOWER_SEED_PACKET, PVZItems.TERRA_ESSENCE)
                .note(Component.translatable("container.pvz.almanac.can_plant_on_most_places"));
        add(PVZEntities.MARIGOLD).cost(75).coolDown(VERY_SLOW).skillList(List.of())//No skills.
                .recipe(PVZItems.FLOWER_SEED_PACKET).noAutoRecipe().setCreativeOnly();
        add(PVZEntities.VELOCI_RADISH).cost(50).coolDown(FAST).skillList(VelociRadish.staticSkillList)
                .recipe(PVZItems.FLOWER_SEED_PACKET)
                .note(Component.translatable("container.pvz.almanac.can_plant_on_most_places"));
        //the nether
        add(PVZEntities.REPEATER).cost(200).coolDown(FAST).skillList(Repeater.staticSkillList)
                .recipe(getData(PVZEntities.PEA_SHOOTER), PVZItems.NETHER_WART_SEED_PACKET, PVZItems.VENTUS_ESSENCE);
        add(PVZEntities.HYPNO_SHROOM).cost(75).coolDown(SLOW).skillList(HypnoShroom.staticSkillList)
                .recipe(PVZItems.NETHER_WART_SEED_PACKET).setCreativeOnly()
                .note(Component.translatable("container.pvz.almanac.can_plant_on_stone"))
                .note(Component.translatable("container.pvz.almanac.can_sculkificate"))
                .note(Component.translatable("container.pvz.almanac.sleep_at_day"));
        add(PVZEntities.JALAPENO).cost(125).coolDown(VERY_SLOW).skillList(Jalapeno.staticSkillList)
                .recipe(PVZItems.PEPPER, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.IGNIS_ESSENCE)
                .note(Component.translatable("container.pvz.almanac.can_plant_on_most_places"))
                .note(Component.translatable("container.pvz.almanac.friendly_fire"));
        add(PVZEntities.SPIKE_WEED).cost(100).coolDown(MEDIUM).skillList(SpikeWeed.staticSkillList)
                .recipe(Items.GLOW_LICHEN, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.TERRA_ESSENCE)
                .note(Component.translatable("container.pvz.almanac.can_plant_on_stone"));
        add(PVZEntities.TORCH_WOOD).cost(300).coolDown(SLOW).skillList(TorchWood.staticSkillList)
                .recipeBlock(PVZBlocks.woodList.get(0)/*Nut wood*/.get(PVZBlocks.WoodSet.Log), PVZItems.NETHER_WART_SEED_PACKET, PVZItems.IGNIS_ESSENCE)
                .note(Component.translatable("container.pvz.almanac.can_plant_on_stone"));
        add(PVZEntities.TALL_NUT).cost(125).coolDown(VERY_SLOW).skillList(TallNut.staticSkillList)
                .recipe(PVZItems.NETHER_WART_SEED_PACKET);
        add(PVZEntities.STARFRUIT).cost(200).coolDown(FAST).skillList(Starfruit.staticSkillList)
                .recipe(PVZItems.STARFRUIT, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.VENTUS_ESSENCE);
        add(PVZEntities.PUMPKIN).cost(125).coolDown(SLOW).skillList(Pumpkin.staticSkillList)
                .recipe(Items.PUMPKIN, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.TERRA_ESSENCE);
        add(PVZEntities.UMBRELLA_LEAF).cost(100).coolDown(MEDIUM).skillList(UmbrellaLeaf.staticSkillList)
                .recipe(Items.BIG_DRIPLEAF, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.VENTUS_ESSENCE);
        add(PVZEntities.MELON_PULT).cost(550).coolDown(SLOW).skillList(MelonPult.staticSkillList)
                .recipe(Items.MELON, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.VENTUS_ESSENCE);
        add(PVZEntities.ICEBERG_LETTUCE).cost(0).coolDown(FAST).skillList(IcebergLettuce.staticSkillList)
                .recipe(PVZItems.CABBAGE, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.GELUM_ESSENCE)
                .note(Component.translatable("container.pvz.almanac.can_plant_on_snow"))
                .note(Component.translatable("container.pvz.almanac.can_plant_on_stone"));
        //the end
        add(PVZEntities.GATLING_PEA).advanced().cost(400).coolDown(VERY_SLOW).skillList(GatlingPea.staticSkillList)
                .recipe(PVZItems.CHORUS_FRUIT_SEED_PACKET)
                .note(Component.translatable("container.pvz.almanac.can_only_plant_on", Component.translatable("entity.pvz.repeater").withStyle(ChatFormatting.RED)));
        //creative
        add(PVZEntities.CHOMPER).cost(150).coolDown(MEDIUM).skillList(Chomper.staticSkillList)
                .recipe(PVZItems.FLUORESCENT_DAISY_SEED_PACKET).setCreativeOnly()
                .note(Component.translatable("container.pvz.almanac.can_sculkificate")); //TODO change to chomper block in abyss.
        add(PVZEntities.KERNEL_PULT).cost(100).coolDown(MEDIUM).skillList(KernelPult.staticSkillList)
                .recipe(PVZItems.CORN, PVZItems.FLUORESCENT_DAISY_SEED_PACKET, PVZItems.VENTUS_ESSENCE).setCreativeOnly();
        add(PVZEntities.DANDELION).cost(200).coolDown(MEDIUM).skillList(Dandelion.staticSkillList)
                .recipe(Items.DANDELION, PVZItems.FLUORESCENT_DAISY_SEED_PACKET, PVZItems.VENTUS_ESSENCE).setCreativeOnly();
        add(PVZEntities.GOLD_BLOOM).cost(0).coolDown(VERY_SLOW).skillList(GoldBloom.staticSkillList)
                .recipe(Items.GLOW_BERRIES, PVZItems.FLUORESCENT_DAISY_SEED_PACKET, PVZItems.LUX_ESSENCE).setCreativeOnly()
                .note(Component.translatable("container.pvz.almanac.can_plant_on_most_places"));

        //for other mods.
        RegisterSeedPacketsEvent event = new RegisterSeedPacketsEvent();
        MinecraftForge.EVENT_BUS.post(event);
        seedPacketData.addAll(event.get());

        //create.
        PVZItems.createSeedPackets();
    }

    public static synchronized <T extends LivingEntity> RecipeSeedPacketData<T> add(Supplier<EntityType<T>> goalEntity) {
        RecipeSeedPacketData<T> newPacket = new RecipeSeedPacketData<>(goalEntity);
        seedPacketData.add(newPacket);
        return newPacket;
    }

    @SuppressWarnings("unchecked")
    public static synchronized <T extends LivingEntity> RegisterSeedPacketsEvent.SeedPacketData<T> getData(Supplier<EntityType<T>> entity) {
        for (RegisterSeedPacketsEvent.SeedPacketData<?> data : seedPacketData) {
            if (data.entitySupplier.equals(entity)) {
                return (RegisterSeedPacketsEvent.SeedPacketData<T>) data;
            }
        }
        for (RegisterSeedPacketsEvent.SeedPacketData<?> data : dataMap.values()) {
            if (data.entitySupplier.equals(entity)) {
                return (RegisterSeedPacketsEvent.SeedPacketData<T>) data;
            }
        }
        return null;
    }

    public static synchronized void sortAndClear() {
        if (sorted) return;
        if (! initialized) {
            LOGGER.error("PVZSeedPackets.sortAndClear() called before init(), ignoring.");
            return;
        }
        sorted = true;

        Map<Item, RegisterSeedPacketsEvent.SeedPacketData<?>> newDataMap = new HashMap<>();
        Map<Item, List<Item>> newSortedCreative = new LinkedHashMap<>();
        Map<Item, List<Item>> newSortedSurvival = new LinkedHashMap<>();
        List<Item> newTabsCreative = new ArrayList<>();
        List<Item> newTabsSurvival = new ArrayList<>();
        for (RegisterSeedPacketsEvent.SeedPacketData<?> data : seedPacketData) {
            RegistryObject<Item> packetObject = PVZItems.seedPacketMap.get(data);
            RegistryObject<Item> seedObject = PVZItems.seedMap.get(data);
            if (packetObject == null || seedObject == null) {
                LOGGER.error("No seed packet item was created for " + data.entitySupplier + ". It was registered after the ITEMS registry event.");
                continue;
            }
            Item packet = packetObject.get();
            Item seed = seedObject.get();
            newDataMap.put(packet, data);
            newDataMap.put(seed, data);
            Item base = data.basePacketSupplier == null ? null : data.basePacketSupplier.get();
            if (base == null) {
                LOGGER.warn("Seed packet " + packet + " has no base packet and is skipped in the almanac tabs.");
                continue;
            }
            if (newSortedCreative.containsKey(base)) {
                newSortedCreative.get(base).add(packet);
            } else {
                List<Item> items = new ArrayList<>();
                newTabsCreative.add(base);
                items.add(packet);
                newSortedCreative.put(base, items);
            }
            if (!data.creativeOnly) {
                if (newSortedSurvival.containsKey(base)) {
                    newSortedSurvival.get(base).add(packet);
                } else {
                    List<Item> items = new ArrayList<>();
                    newTabsSurvival.add(base);
                    items.add(packet);
                    newSortedSurvival.put(base, items);
                }
            }
        }

        dataMap = Map.copyOf(newDataMap);
        sortedCreative = freeze(newSortedCreative);
        sortedSurvival = freeze(newSortedSurvival);
        tabsCreative = List.copyOf(newTabsCreative);
        tabsSurvival = List.copyOf(newTabsSurvival);

        seedPacketData.clear();
    }

    private static Map<Item, List<Item>> freeze(Map<Item, List<Item>> source) {
        Map<Item, List<Item>> copy = new LinkedHashMap<>();
        source.forEach((key, value) -> copy.put(key, List.copyOf(value)));
        return Collections.unmodifiableMap(copy);
    }

    public static class RecipeSeedPacketData<T extends LivingEntity> extends RegisterSeedPacketsEvent.SeedPacketData<T> {

        //for recipe generator.
        public Map<String, ?> recipe = null;
        public boolean noAutoRecipe = false;
        public RecipeSeedPacketData(Supplier<EntityType<T>> entitySupplier) {
            super(entitySupplier);
        }
        public RecipeSeedPacketData<T> resource(String resource) {
            this.resource = resource;
            return this;
        }
        public RecipeSeedPacketData<T> cost(int cost) {
            this.cost = cost;
            return this;
        }
        public RecipeSeedPacketData<T> coolDown(int coolDown) {
            this.coolDown = coolDown;
            return this;
        }
        public RecipeSeedPacketData<T> advanced() {
            super.advanced();
            return this;
        }
        public RecipeSeedPacketData<T> skillList(List<Skill> list) {
            this.skillList = list;
            return this;
        }
        public RecipeSeedPacketData<T> recipe(RegistryObject<Item> packet) {
            this.recipe = Map.of("packet", packet);
            this.basePacketSupplier = packet;
            return this;
        }
        public RecipeSeedPacketData<T> recipe(RegistryObject<Item> seed, RegistryObject<Item> packet, RegistryObject<Item> essence) {
            this.recipe = Map.of("seed", seed, "packet", packet, "essence", essence);
            this.packet(packet);
            return this;
        }
        public RecipeSeedPacketData<T> recipe(RegistryObject<Item> seed, RecipeSeedPacketData<? extends LivingEntity> packet, RegistryObject<Item> essence) {
            this.recipe = Map.of("seed", seed, "packet", packet, "essence", essence);
            this.packet(packet.basePacketSupplier);
            return this;
        }
        public <S extends LivingEntity> RecipeSeedPacketData<T> recipe(RegisterSeedPacketsEvent.SeedPacketData<? extends LivingEntity> seed, RegistryObject<Item> packet, RegistryObject<Item> essence) {
            this.recipe = Map.of("seed", seed, "packet", packet, "essence", essence);
            this.packet(packet);
            return this;
        }
        public RecipeSeedPacketData<T> recipeBlock(RegistryObject<Block> seed, RegistryObject<Item> packet, RegistryObject<Item> essence) {
            this.recipe = Map.of("seed", seed, "packet", packet, "essence", essence);
            this.packet(packet);
            return this;
        }
        public RecipeSeedPacketData<T> recipe(Item seed, RegistryObject<Item> packet, RegistryObject<Item> essence) {
            this.recipe = Map.of("seed", seed, "packet", packet, "essence", essence);
            this.packet(packet);
            return this;
        }
        public RecipeSeedPacketData<T> noAutoRecipe() {
            noAutoRecipe = true;
            return this;
        }
        @SuppressWarnings("unchecked")
        public RegistryObject<Item> getBackCard() {
            Object packet = recipe.get("packet");
            if (packet instanceof RegistryObject) {
                return (RegistryObject<Item>) packet;
            }
            return ((RecipeSeedPacketData<?>) packet).getBackCard();
        }
    }
}
