package com.hungteen.pvz.common.register;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.events.RegisterSeedPacketsEvent;
import com.hungteen.pvz.common.entity.plants.*;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**This class is used for convenience only when registering. Then all the data in PVZSeedPackets.seedPackets will be released.
 */
public class PVZSeedPackets {
    public static final int VERY_FAST = 10; //will a plant really use this?
    public static final int FAST = 60;
    public static final int MEDIUM = 200;
    public static final int SLOW = 500;
    public static final int VERY_SLOW = 1000;
    @Deprecated //clear after registry.
    public static List<RegisterSeedPacketsEvent.SeedPacketData<?>> seedPacketData = new ArrayList<>();
    public static Map<Item, RegisterSeedPacketsEvent.SeedPacketData<?>> dataMap = new HashMap<>();
    public static List<Item> tabsSurvival = new ArrayList<>();
    public static List<Item> tabsCreative = new ArrayList<>();
    public static Map<Item, List<Item>> sortedSurvival = new HashMap<>();
    public static Map<Item, List<Item>> sortedCreative = new HashMap<>();
    public static Map<EntityType<?>, List<Skill>> additionalSkills = new HashMap<>();

    static {
        //pvz packets.

        //overworld
        add(PVZEntities.PEA_SHOOTER).cost(75).coolDown(FAST).skillList(PeaShooter.staticSkillList)
                .recipe(PVZItems.PEA, PVZItems.FLOWER_SEED_PACKET, PVZItems.VENTUS_ESSENCE);
        add(PVZEntities.SUN_FLOWER).cost(50).coolDown(SLOW).skillList(SunFlower.staticSkillList)
                .recipe(Items.SUNFLOWER, PVZItems.FLOWER_SEED_PACKET, PVZItems.LUX_ESSENCE);
        add(PVZEntities.WALL_NUT).cost(50).coolDown(SLOW).skillList(WallNut.staticSkillList)
                .recipe(PVZItems.NUT, PVZItems.FLOWER_SEED_PACKET, PVZItems.TERRA_ESSENCE);
        add(PVZEntities.POTATO_MINE).cost(25).coolDown(MEDIUM).skillList(PotatoMine.staticSkillList)
                .recipe(Items.POTATO, PVZItems.FLOWER_SEED_PACKET, PVZItems.TERRA_ESSENCE)
                .note(Component.translatable("container.pvz.almanac.can_plant_on_dirt"));
        add(PVZEntities.SNOW_PEA).cost(150).coolDown(MEDIUM).skillList(SnowPea.staticSkillList)
                .recipe(PVZItems.FLOWER_SEED_PACKET);
        add(PVZEntities.LILY_PAD).cost(25).coolDown(FAST).skillList(LilyPad.staticSkillList)
                .recipe(Items.LILY_PAD, PVZItems.FLOWER_SEED_PACKET, PVZItems.AQUA_ESSENCE)
                .note(Component.translatable("container.pvz.almanac.can_only_plant_on_water"));
        add(PVZEntities.TANGLE_KELP).cost(25).coolDown(SLOW).skillList(TangleKelp.staticSkillList)
                .recipe(Items.KELP, PVZItems.FLOWER_SEED_PACKET, PVZItems.AQUA_ESSENCE)
                .note(Component.translatable("container.pvz.almanac.can_only_plant_on_water"));
        add(PVZEntities.SPLIT_PEA).cost(150).coolDown(MEDIUM).skillList(SplitPea.staticSkillList)
                .recipe(PVZItems.FLOWER_SEED_PACKET);
        add(PVZEntities.CABBAGE_PULT).cost(100).coolDown(FAST).skillList(CabbagePult.staticSkillList)
                .recipe(PVZItems.CABBAGE, PVZItems.FLOWER_SEED_PACKET, PVZItems.VENTUS_ESSENCE);
        add(PVZEntities.FLOWER_POT).cost(25).coolDown(FAST).skillList(FlowerPot.staticSkillList)
                .recipe(Items.FLOWER_POT, PVZItems.FLOWER_SEED_PACKET, PVZItems.TERRA_ESSENCE)
                .note(Component.translatable("container.pvz.almanac.can_plant_on_stone"));
        add(PVZEntities.MARIGOLD).cost(75).coolDown(VERY_SLOW).skillList(List.of())//No skills.
                .recipe(PVZItems.FLOWER_SEED_PACKET).noAutoRecipe().setCreativeOnly();
        add(PVZEntities.ICEBERG_LETTUCE).cost(0).coolDown(FAST).skillList(IcebergLettuce.staticSkillList)
                .recipe(PVZItems.CABBAGE, PVZItems.FLOWER_SEED_PACKET, PVZItems.GELUM_ESSENCE)
                .note(Component.translatable("container.pvz.almanac.can_plant_on_snow"))
                .note(Component.translatable("container.pvz.almanac.can_plant_on_stone"));
        add(PVZEntities.VELOCI_RADISH).cost(50).coolDown(MEDIUM).skillList(VelociRadish.staticSkillList)
                .recipe(PVZItems.FLOWER_SEED_PACKET)
                .note(Component.translatable("container.pvz.almanac.can_plant_on_stone"));
        //the nether
        add(PVZEntities.REPEATER).cost(150).coolDown(FAST).skillList(Repeater.staticSkillList)
                .recipe(PVZItems.NETHER_WART_SEED_PACKET);
        add(PVZEntities.JALAPENO).cost(125).coolDown(VERY_SLOW).skillList(Jalapeno.staticSkillList)
                .recipe(PVZItems.PEPPER, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.IGNIS_ESSENCE)
                .note(Component.translatable("container.pvz.almanac.can_plant_on_stone"))
                .note(Component.translatable("container.pvz.almanac.friendly_fire"));
        add(PVZEntities.SPIKE_WEED).cost(100).coolDown(MEDIUM).skillList(SpikeWeed.staticSkillList)
                .recipe(Items.GLOW_LICHEN, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.TERRA_ESSENCE)
                .note(Component.translatable("container.pvz.almanac.can_plant_on_stone"));
        add(PVZEntities.TORCH_WOOD).cost(300).coolDown(SLOW).skillList(TorchWood.staticSkillList)
                .recipeBlock(PVZBlocks.woodList.get(0)/*Nut wood*/.get(PVZBlocks.WoodSet.Log), PVZItems.NETHER_WART_SEED_PACKET, PVZItems.IGNIS_ESSENCE)
                .note(Component.translatable("container.pvz.almanac.can_plant_on_stone"));
        add(PVZEntities.TALL_NUT).cost(125).coolDown(VERY_SLOW).skillList(TallNut.staticSkillList)
                .recipe(PVZItems.NETHER_WART_SEED_PACKET);
        add(PVZEntities.PLANTERN).cost(25).coolDown(VERY_SLOW).skillList(Plantern.staticSkillList)
                .recipeBlock(PVZBlocks.PLANTERN, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.LUX_ESSENCE);
        add(PVZEntities.STARFRUIT).cost(200).coolDown(MEDIUM).skillList(Starfruit.staticSkillList)
                .recipe(PVZItems.STARFRUIT, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.VENTUS_ESSENCE);
        add(PVZEntities.PUMPKIN).cost(125).coolDown(SLOW).skillList(Pumpkin.staticSkillList)
                .recipe(Items.PUMPKIN, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.TERRA_ESSENCE);
        add(PVZEntities.UMBRELLA_LEAF).cost(100).coolDown(MEDIUM).skillList(UmbrellaLeaf.staticSkillList)
                .recipe(Items.BIG_DRIPLEAF, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.VENTUS_ESSENCE);
        add(PVZEntities.MELON_PULT).cost(450).coolDown(SLOW).skillList(MelonPult.staticSkillList)
                .recipe(Items.MELON, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.VENTUS_ESSENCE);
        //the end
        add(PVZEntities.GATLING_PEA).advanced().cost(400).coolDown(VERY_SLOW).skillList(GatlingPea.staticSkillList)
                .recipe(PVZItems.CHORUS_FRUIT_SEED_PACKET)
                .note(Component.translatable("container.pvz.almanac.can_only_plant_on", Component.translatable("entity.pvz.repeater").withStyle(ChatFormatting.RED)));
        //abyss
        add(PVZEntities.CHOMPER).cost(150).coolDown(MEDIUM).skillList(Chomper.staticSkillList)
                .recipe(PVZItems.FLUORESCENT_DAISY_SEED_PACKET).setCreativeOnly()
                .note(Component.translatable("container.pvz.almanac.can_sculkificate")); //TODO change to chomper block in abyss.
        add(PVZEntities.HYPNO_SHROOM).cost(75).coolDown(SLOW).skillList(HypnoShroom.staticSkillList)
                .recipe(PVZItems.FLUORESCENT_DAISY_SEED_PACKET).setCreativeOnly()
                .note(Component.translatable("container.pvz.almanac.can_plant_on_stone"))
                .note(Component.translatable("container.pvz.almanac.can_sculkificate"))
                .note(Component.translatable("container.pvz.almanac.sleep_at_day"));
        add(PVZEntities.KERNEL_PULT).cost(100).coolDown(MEDIUM).skillList(KernelPult.staticSkillList)
                .recipe(PVZItems.CORN, PVZItems.FLUORESCENT_DAISY_SEED_PACKET, PVZItems.VENTUS_ESSENCE).setCreativeOnly();
        add(PVZEntities.DANDELION).cost(200).coolDown(MEDIUM).skillList(Dandelion.staticSkillList)
                .recipe(Items.DANDELION, PVZItems.FLUORESCENT_DAISY_SEED_PACKET, PVZItems.VENTUS_ESSENCE).setCreativeOnly();
        add(PVZEntities.GOLD_BLOOM).cost(0).coolDown(VERY_SLOW).skillList(GoldBloom.staticSkillList)
                .recipe(Items.GLOW_BERRIES, PVZItems.FLUORESCENT_DAISY_SEED_PACKET, PVZItems.LUX_ESSENCE).setCreativeOnly()
                .note(Component.translatable("container.pvz.almanac.can_plant_on_stone"));

        //for other mods.
        RegisterSeedPacketsEvent event = new RegisterSeedPacketsEvent();
        MinecraftForge.EVENT_BUS.post(event);
        seedPacketData.addAll(event.get());
    }

    public static <T extends LivingEntity> RecipeSeedPacketData<T> add(Supplier<EntityType<T>> goalEntity) {
        RecipeSeedPacketData<T> newPacket = new RecipeSeedPacketData<>(goalEntity);
        seedPacketData.add(newPacket);
        return newPacket;
    }

    public static void sortAndClear() {
        seedPacketData.forEach(data -> {
            PVZSeedPackets.dataMap.put(PVZItems.seedPacketMap.get(data).get(), data);
            PVZSeedPackets.dataMap.put(PVZItems.seedMap.get(data).get(), data);
            Item base = data.basePacketSupplier.get();
            if (sortedCreative.containsKey(base)) {
                sortedCreative.get(base).add(PVZItems.seedPacketMap.get(data).get());
            } else {
                List<Item> items = new ArrayList<>();
                tabsCreative.add(base);
                items.add(PVZItems.seedPacketMap.get(data).get());
                sortedCreative.put(base, items);
            }
            if (! data.creativeOnly) {
                if (sortedSurvival.containsKey(base)) {
                    sortedSurvival.get(base).add(PVZItems.seedPacketMap.get(data).get());
                } else {
                    List<Item> items = new ArrayList<>();
                    tabsSurvival.add(base);
                    items.add(PVZItems.seedPacketMap.get(data).get());
                    sortedSurvival.put(base, items);
                }
            }
        });
        seedPacketData.clear();
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
        public RegistryObject<Item> getBackCard() {
            Object packet = recipe.get("packet");
            if (packet instanceof RegistryObject) {
                return (RegistryObject<Item>) packet;
            }
            return ((RecipeSeedPacketData) packet).getBackCard();
        }
    }
}
