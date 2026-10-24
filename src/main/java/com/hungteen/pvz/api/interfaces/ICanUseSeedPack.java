package com.hungteen.pvz.api.interfaces;

import com.hungteen.pvz.api.events.PVZResourceEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**
 * PT：阳光容器不一定都能放置植物，需要单独拆一个接口
 * @author PangTeen
 * @program HTPVZ
 * @create 2026/10/24 9:39
 **/
public interface ICanUseSeedPack extends ISunContainer {

    /** @return when returns null, the sun container will not be able to plant, judged by seed packets. */
    default @Nullable ItemCooldowns getCooldowns() {
        return null;
    }

    default @Nullable Entity getEntity() {
        return null;
    }

    default @Nullable Vec3 getPosition() {
        return getEntity() != null ? getEntity().position() : Vec3.ZERO;
    }

    default @Nullable Level getLevel() {
        return getEntity() != null ? getEntity().level : null;
    }

    /** Fired before the sun container plant. if result is not null, the whole planting method will not run. */
    default MutableComponent prePlantOnBlock(PVZResourceEvent.CheckPlantConditionEvent event, Entity entity,
        ItemStack plantCard, Level level, BlockPos pos, Direction side) {
        return null;
    }

    /** Fired before the sun container plant. if result is not null, the whole planting method will not run. */
    default MutableComponent prePlantOnEntity(PVZResourceEvent.CheckPlantConditionEvent event, Entity entity,
        ItemStack plantCard, Entity target) {
        return null;
    }

    /** Fired after the sun container plant. can not cancel planting */
    default void postPlantOnBlock(PVZResourceEvent.CheckPlantConditionEvent event, Entity entity, ItemStack plantCard,
        MutableComponent posCheck, Level level, BlockPos pos, Direction side) {
    }

    default void postPlantOnEntity(PVZResourceEvent.CheckPlantConditionEvent event, Entity entity, ItemStack plantCard,
        MutableComponent entityCheck, Entity target) {
    }
}
