package com.hungteen.pvz.common.world.invasion;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.storage.loot.LootTable;
import org.jetbrains.annotations.Nullable;

import java.util.HashSet;
import java.util.Set;
import java.util.function.BiPredicate;
import java.util.function.Consumer;
import java.util.logging.Level;

/**Server only. These are invasions read from data pack.**/
public class InvasionType {
    public static Set<InvasionType> invasionTypes;

    public ResourceLocation location;
    public LootTable awards;
    public Set<BiPredicate<Level, Player>> conditions;
    public Set<Consumer<Entity>> entityModifiers;
    public CompoundTag flagZombie;
    public boolean isAddition = false;
    public int weight;
    public int radius;

    @Nullable
    public static InvasionType getInvasionType(ResourceLocation location) {
        for (InvasionType type : invasionTypes) {
            if (type.location.equals(location)) {
                return type;
            }
        }
        return null;
    }

    public record EnemyType(CompoundTag entityData, Set<BiPredicate<Level, Player>> conditions, int threat, int weight, boolean isElite, float startFrom) {

        public EnemyType(CompoundTag entityData, int threat) {
            this(entityData, new HashSet<>(), threat, 10, false, 0);
        }
    }
}
