package com.hungteen.pvz.api.events;

import com.hungteen.pvz.api.PVZAPI;
import com.hungteen.pvz.api.Skill;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.Item;
import net.minecraftforge.eventbus.api.Event;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;

/**
 This event is for the mods that doesn't rely on this mod. If relies on HTPVZ, you can also use {@link com.hungteen.pvz.common.item.SeedPacketItem SeedPacketItem}.
 Can't automatically create recipes or models.
 */
public class RegisterSeedPacketsEvent extends Event {
    private final Set<SeedPacketData<?>> packetSet = new HashSet<>();

    public void add(Supplier<EntityType<? extends LivingEntity>> entitySupplier, String resource, int cost, int coolDown) {
        packetSet.add(new SeedPacketData(entitySupplier, resource, cost, coolDown));
    }
    public void add(Supplier<EntityType<? extends LivingEntity>> entitySupplier, int cost, int coolDown) {
        add(entitySupplier, PVZAPI.get().getSunResourceName(), cost, coolDown);
    }

    /**Returns all newly-added data.*/
    public Set<SeedPacketData<?>> get() {
        return packetSet;
    }

    public boolean contains(Supplier<EntityType> entitySupplier) {
        for (SeedPacketData data : packetSet) {
            if (data.entitySupplier.equals(entitySupplier)) {
                return true;
            }
        }
        return false;
    }

    public static class SeedPacketData<T extends LivingEntity> {
        public final Supplier<EntityType<T>> entitySupplier;
        public Supplier<Item> basePacketSupplier;
        public List<Skill> skillList = List.of();
        public List<Component> notes = new ArrayList<>();
        public int cost = 0;
        public int coolDown = 10;
        public String resource = PVZAPI.get().getSunResourceName();
        public boolean creativeOnly;
        public boolean advanced;

        public SeedPacketData(Supplier<EntityType<T>> entitySupplier){
            this.entitySupplier = entitySupplier;
            this.advanced = false;
        }

        public SeedPacketData(Supplier<EntityType<T>> entitySupplier, String resource, int cost, int coolDown) {
            this(entitySupplier);
            this.cost = cost;
            this.resource = resource;
            this.coolDown = coolDown;
        }
        public SeedPacketData<T> setCreativeOnly(){
            creativeOnly = true;
            return this;
        }
        public SeedPacketData<T> advanced() {
            advanced = true;
            return this;
        }
        public SeedPacketData<T> packet(Supplier<Item> packet) {
            this.basePacketSupplier = packet;
            return this;
        }
        public SeedPacketData<T> note(Component component) {
            this.notes.add(component);
            return this;
        }
    }
}
