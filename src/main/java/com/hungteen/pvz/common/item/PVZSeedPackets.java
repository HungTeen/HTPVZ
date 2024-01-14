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
    public static final int FAST = 60;
    public static final int MIDDLE = 200;
    public static final int SLOW = 400;
    public static final int VERY_SLOW = 750;
    @Deprecated //clear after registry.
    public static List<RegisterSeedPacketsEvent.SeedPacketData> seedPackets = new ArrayList<>();

    static {
        //pvz packets.
        add(PVZEntities.SUN_FLOWER).cost(50).coolDown(SLOW).skillList(List.of())//TODO add skills.
                .recipe(Items.SUNFLOWER, PVZItems.FLOWER_SEED_PACKET, PVZItems.LUX_ESSENCE);
        add(PVZEntities.PEA_SHOOTER).cost(100).coolDown(FAST).skillList(PeaShooter.staticSkillList)
                .recipe(PVZItems.PEA, PVZItems.FLOWER_SEED_PACKET, PVZItems.VENTUS_ESSENCE);
        add(PVZEntities.WALL_NUT).cost(50).coolDown(SLOW).skillList(WallNut.staticSkillList)
                .recipe(PVZItems.NUT, PVZItems.FLOWER_SEED_PACKET, PVZItems.TERRA_ESSENCE);
        add(PVZEntities.REPEATER).cost(150).coolDown(FAST).skillList(Repeater.staticSkillList)
                .recipe(PVZItems.PEA, PVZItems.NETHER_WART_SEED_PACKET, PVZItems.VENTUS_ESSENCE);
        add(PVZEntities.TALL_NUT).cost(125).coolDown(SLOW).skillList(TallNut.staticSkillList)
                .recipe(PVZItems.FLOWER_SEED_PACKET);
        add(PVZEntities.MARIGOLD).cost(75).coolDown(VERY_SLOW).skillList(List.of())//TODO add skills.
                .recipe(Items.OXEYE_DAISY, PVZItems.FLOWER_SEED_PACKET, PVZItems.LUX_ESSENCE);

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

    public static <T extends LivingEntity> List<RegisterSeedPacketsEvent.SeedPacketData<T>> getPacket(EntityType<T> entity) {
        List<RegisterSeedPacketsEvent.SeedPacketData<T>> list = new ArrayList<>();
        for (RegisterSeedPacketsEvent.SeedPacketData<T> card : seedPackets) {
            if (card.entitySupplier.get().equals(entity.getDescriptionId())) {
                list.add(card);
            }
        }
        return list;
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
        public RecipeSeedPacketData<T> recipe(RegistryObject<Item> seed, RecipeSeedPacketData<T> packet, RegistryObject<Item> essence) {
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
