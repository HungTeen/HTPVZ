package com.hungteen.pvz.common.item;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.events.RegisterSeedPacketsEvent;
import com.hungteen.pvz.common.entity.plants.*;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

/**This class is used for convenience only when registering. Then all the data in PVZSeedPackets.seedPackets will be released.
 */
public class PVZSeedPackets {
    public static final int VERY_FAST = 10; //will a plant really use this?
    public static final int FAST = 60;
    public static final int MIDDLE = 200;
    public static final int SLOW = 500;
    public static final int VERY_SLOW = 1200;
    @Deprecated //clear after registry.
    public static List<RegisterSeedPacketsEvent.SeedPacketData> seedPackets = new ArrayList<>();

    static {
        //pvz packets.
        add(PVZEntities.PEA_SHOOTER).cost(100).coolDown(FAST).skillList(PeaShooter.staticSkillList)
                .recipe(PVZItems.PEA, PVZItems.FLOWER_SEED_PACKET, PVZItems.VENTUS_ESSENCE);
        add(PVZEntities.SUN_FLOWER).cost(50).coolDown(SLOW).skillList(List.of())//No skills.
                .recipe(Items.SUNFLOWER, PVZItems.FLOWER_SEED_PACKET, PVZItems.LUX_ESSENCE);
        add(PVZEntities.WALL_NUT).cost(50).coolDown(SLOW).skillList(WallNut.staticSkillList)
                .recipe(PVZItems.NUT, PVZItems.FLOWER_SEED_PACKET, PVZItems.TERRA_ESSENCE);
        add(PVZEntities.SNOW_PEA).cost(175).coolDown(MIDDLE).skillList(SnowPea.staticSkillList)
                .recipe(PVZItems.PEA, getRecipePacket(PVZEntities.PEA_SHOOTER), PVZItems.GELUM_ESSENCE);
        add(PVZEntities.POTATO_MINE).cost(25).coolDown(SLOW).skillList(PotatoMine.staticSkillList)
                .recipe(Items.POTATO, PVZItems.FLOWER_SEED_PACKET, PVZItems.TERRA_ESSENCE);
        add(PVZEntities.LILY_PAD).cost(25).coolDown(FAST).skillList(LilyPad.staticSkillList)
                .recipe(Items.LILY_PAD, PVZItems.FLOWER_SEED_PACKET, PVZItems.AQUA_ESSENCE);
        add(PVZEntities.TALL_NUT).cost(125).coolDown(SLOW).skillList(TallNut.staticSkillList)
                .recipe(PVZItems.FLOWER_SEED_PACKET);
            add(PVZEntities.CABBAGE_PULT).cost(100).coolDown(FAST).skillList(CabbagePult.staticSkillList)
                .recipe(PVZItems.CABBAGE, PVZItems.FLOWER_SEED_PACKET, PVZItems.VENTUS_ESSENCE);
        add(PVZEntities.FLOWER_POT).cost(25).coolDown(FAST).skillList(FlowerPot.staticSkillList)
                .recipe(Items.FLOWER_POT, PVZItems.FLOWER_SEED_PACKET, PVZItems.TERRA_ESSENCE);
        add(PVZEntities.VELOCI_TURNIP).cost(50).coolDown(FAST).skillList(VelociTurnip.staticSkillList)
                .recipe(PVZItems.FLOWER_SEED_PACKET);
        add(PVZEntities.MARIGOLD).cost(75).coolDown(VERY_SLOW).skillList(List.of())//No skills.
                .recipe(PVZItems.FLOWER_SEED_PACKET).setCreativeOnly();
        add(PVZEntities.REPEATER).cost(175).coolDown(FAST).skillList(Repeater.staticSkillList)
                .recipe(PVZItems.PEA, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.VENTUS_ESSENCE);
        add(PVZEntities.PLANTERN).cost(25).coolDown(VERY_SLOW).skillList(Plantern.staticSkillList)
                .recipe(Items.GLOW_BERRIES, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.LUX_ESSENCE); //TODO change glow berries to a plantern block.
        add(PVZEntities.PUMPKIN).cost(125).coolDown(SLOW).skillList(Pumpkin.staticSkillList)
                .recipe(Items.PUMPKIN, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.TERRA_ESSENCE);
        add(PVZEntities.GATLING_PEA).cost(400).coolDown(VERY_SLOW).skillList(GatlingPea.staticSkillList)
                .recipe(PVZItems.CHORUS_FRUIT_SEED_PACKET);

        //for other mods.
        RegisterSeedPacketsEvent event = new RegisterSeedPacketsEvent();
        MinecraftForge.EVENT_BUS.post(event);
        seedPackets.addAll(event.get());
    }

    public static <T extends LivingEntity> RecipeSeedPacketData<T> add(Supplier<EntityType<T>> goalEntity) {
        RecipeSeedPacketData<T> newPacket = new RecipeSeedPacketData<>(goalEntity);
        seedPackets.add(newPacket);
        return newPacket;
    }

    public static <T extends LivingEntity> RecipeSeedPacketData<T> getRecipePacket(Supplier<EntityType<T>> goalEntity) {
        for (RegisterSeedPacketsEvent.SeedPacketData<?> card : seedPackets) {
            if (card.entitySupplier.equals(goalEntity) && card instanceof RecipeSeedPacketData) {
                return (RecipeSeedPacketData<T>) card;
            }
        }
        return null;
    }

    public static class RecipeSeedPacketData<T extends LivingEntity> extends RegisterSeedPacketsEvent.SeedPacketData<T> {

        //for recipe generator.
        public Map<String, ?> recipe = null;
        public RecipeSeedPacketData(Supplier<EntityType<T>> entitySupplier) {
            super(entitySupplier);
            this.entitySupplier = entitySupplier;
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
        public RecipeSeedPacketData<T> skillList(List<Skill> list) {
            this.skillList = list;
            return this;
        }
        public RecipeSeedPacketData<T> recipe(RegistryObject<Item> packet) {
            this.recipe = Map.of("packet", packet);
            return this;
        }
        public RecipeSeedPacketData<T> recipe(RegistryObject<Item> seed, RegistryObject<Item> packet, RegistryObject<Item> essence) {
            this.recipe = Map.of("seed", seed, "packet", packet, "essence", essence);
            return this;
        }
        public RecipeSeedPacketData<T> recipe(RegistryObject<Item> seed, RecipeSeedPacketData<? extends LivingEntity> packet, RegistryObject<Item> essence) {
            this.recipe = Map.of("seed", seed, "packet", packet, "essence", essence);
            return this;
        }
        public RecipeSeedPacketData<T> recipe(Item seed, RegistryObject<Item> packet, RegistryObject<Item> essence) {
            this.recipe = Map.of("seed", seed, "packet", packet, "essence", essence);
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
