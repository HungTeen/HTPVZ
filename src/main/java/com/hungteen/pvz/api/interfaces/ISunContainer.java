package com.hungteen.pvz.api.interfaces;

import com.hungteen.pvz.api.events.PVZResourceEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.item.ItemCooldowns;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;

/**ISunContainer includes all object that can store sun. When use player as a sun container, use the wrapper {@link com.hungteen.pvz.common.world.PlayerSunContainerWrapper PlayerSunContainerWrapper}. */
public interface ISunContainer {
    int getAmount();
    /**If this is a LivingEntity, use attribute {@link com.hungteen.pvz.common.register.PVZAttributes#MAX_SUN SUN} as capacity of this entity.*/
    int getCapacity();
    default Vec3 position() {
        return this instanceof Entity ? ((Entity) this).position().add(0, ((Entity) this).getBbHeight() / 2, 0) :
                Vec3.atCenterOf(((BlockEntity) this).getBlockPos());
    }
    default @Nullable Entity getEntity() {
        return null;
    }
    /**@return when returns null, the sun container will not be able to plant, judged by seed packets.*/
    default @Nullable ItemCooldowns getCooldowns() {
        return null;
    }

    default @Nullable Vec3 getPosition() {
        return getEntity() != null ? getEntity().position() : Vec3.ZERO;
    }

    default @Nullable Level getLevel() {
        return getEntity() != null ? getEntity().level : null;
    }

    /**Fired before the sun container plant. if result is not null, the whole planting method will not run.*/
    default MutableComponent prePlantOnBlock(PVZResourceEvent.CheckPlantConditionEvent event, Entity entity
            , ItemStack plantCard, Level level, BlockPos pos, Direction side) {
        return null;
    }
    /**Fired before the sun container plant. if result is not null, the whole planting method will not run.*/
    default MutableComponent prePlantOnEntity(PVZResourceEvent.CheckPlantConditionEvent event, Entity entity
            , ItemStack plantCard, Entity target) {
        return null;
    }

    /**Fired after the sun container plant. can not cancel planting*/
    default void postPlantOnBlock(PVZResourceEvent.CheckPlantConditionEvent event, Entity entity
            , ItemStack plantCard, MutableComponent posCheck, Level level, BlockPos pos, Direction side) {
    }

    default void postPlantOnEntity(PVZResourceEvent.CheckPlantConditionEvent event, Entity entity
            , ItemStack plantCard, MutableComponent entityCheck, Entity target) {
    }

}
