package com.hungteen.pvz.common.item;

import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


public class PVZSeedPackets {
    public static final int FAST = 60;
    public static final int MIDDLE = 200;
    public static final int SLOW = 500;
    public static final int VERY_SLOW = 1200;
    public static List<SeedPacket> seedPackets = new ArrayList<>();

    static {
        add(PVZEntities.WALL_NUT).cost(50).coolDown(SLOW)
                .recipe(PVZItems.NUT, PVZItems.FLOWER_SEED_PACKET, PVZItems.TERRA_ESSENCE);
    }

    public static <T extends LivingEntity> SeedPacket<T> add(Supplier<EntityType<T>> goalEntity) {
        SeedPacket<T> newCard = new SeedPacket<>(goalEntity);
        seedPackets.add(newCard);
        return newCard;
    }

    public static <T extends LivingEntity> List<SeedPacket<T>> getPacket(EntityType<T> entity) {
        List<SeedPacket<T>> list = new ArrayList<>();
        for (SeedPacket<T> card : seedPackets) {
            if (card.goalEntity.get().equals(entity.getDescriptionId())) {
                list.add(card);
            }
        }
        return list;
    }

    public static class SeedPacket<T extends LivingEntity> {
        public final Supplier<EntityType<T>> goalEntity;
        public String resource = PVZPlayerCapNBT.SUN;
        public int coolDown = 0;
        public int cost = 0;

        //for recipe generator.
        public Map<String, ?> recipe = null;
        public SeedPacket(Supplier<EntityType<T>> goalEntity) {
            this.goalEntity = goalEntity;
        }
        public SeedPacket<T> resource(String resource) {
            this.resource = resource;
            return this;
        }
        public SeedPacket<T> cost(int cost) {
            this.cost = cost;
            return this;
        }
        public SeedPacket<T> coolDown(int coolDown) {
            this.coolDown = coolDown;
            return this;
        }
        public SeedPacket<T> recipe(RegistryObject<Item> seed, RegistryObject<Item> packet, RegistryObject<Item> essence) {
            this.recipe = Map.of("seed", seed, "packet", packet, "essence", essence);
            return this;
        }
        public SeedPacket<T> recipe(RegistryObject<Item> seed, SeedPacket<T> packet, RegistryObject<Item> essence) {
            this.recipe = Map.of("seed", seed, "packet", packet, "essence", essence);
            return this;
        }
        public RegistryObject<Item> getBackCard() {
            Object packet = recipe.get("packet");
            if (packet instanceof RegistryObject) {
                return (RegistryObject<Item>) packet;
            }
            return ((SeedPacket) packet).getBackCard();
        }
    }
}
