package com.hungteen.pvz.common.item;

import com.hungteen.pvz.common.capability.player.PVZPlayerCapNBT;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.common.Tags;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;


public class PVZPlantCards {
    public static final int FAST = 60;
    public static final int MIDDLE = 200;
    public static final int SLOW = 500;
    public static final int VERY_SLOW = 1200;
    public static List<PlantCard> plantCards = new ArrayList<>();

    static {
        add(PVZEntities.WALL_NUT).cost(50).coolDown(SLOW)
                .recipe(PVZItems.NUT, PVZItems.FLOWER_CARD, PVZItems.TERRA_ESSENCE);
    }

    public static <T extends LivingEntity> PlantCard<T> add(Supplier<EntityType<T>> goalEntity) {
        PlantCard<T> newCard = new PlantCard<>(goalEntity);
        plantCards.add(newCard);
        return newCard;
    }

    public static <T extends LivingEntity> List<PlantCard<T>> getCard(EntityType<T> entity) {
        List<PlantCard<T>> list = new ArrayList<>();
        for (PlantCard<T> card : plantCards) {
            if (card.goalEntity.get().equals(entity.getDescriptionId())) {
                list.add(card);
            }
        }
        return list;
    }

    public static class PlantCard<T extends LivingEntity> {
        public final Supplier<EntityType<T>> goalEntity;
        public String resource = PVZPlayerCapNBT.SUN;
        public int coolDown = 0;
        public int cost = 0;

        //for recipe generator.
        public Map<String, ?> recipe = null;
        public PlantCard(Supplier<EntityType<T>> goalEntity) {
            this.goalEntity = goalEntity;
        }
        public PlantCard<T> resource(String resource) {
            this.resource = resource;
            return this;
        }
        public PlantCard<T> cost(int cost) {
            this.cost = cost;
            return this;
        }
        public PlantCard<T> coolDown(int coolDown) {
            this.coolDown = coolDown;
            return this;
        }
        public PlantCard<T> recipe(RegistryObject<Item> seed, RegistryObject<Item> card, RegistryObject<Item> essence) {
            this.recipe = Map.of("seed", seed, "card", card, "essence", essence);
            return this;
        }
        public PlantCard<T> recipe(RegistryObject<Item> seed, PlantCard<T> card, RegistryObject<Item> essence) {
            this.recipe = Map.of("seed", seed, "card", card, "essence", essence);
            return this;
        }
        public RegistryObject<Item> getBackCard() {
            Object card = recipe.get("card");
            if (card instanceof RegistryObject) {
                return (RegistryObject<Item>) card;
            }
            return ((PlantCard) card).getBackCard();
        }
    }
}
