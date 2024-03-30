package com.hungteen.pvz.api.interfaces;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import org.jetbrains.annotations.Nullable;

/**Use this interface to identify if an entity is a plant .<br>
 * If need skills, implements {@link IHaveSkills} .<br>
 * Also, override {@link net.minecraft.world.entity.Mob#removeWhenFarAway(double)} .
 */
public interface IPlant{

    /**EntityData controlling if the plant need proper block to plant on.*/
    EntityDataAccessor<Boolean> root();

    /**These two methods are direction and blockPos used for testing whether this situation is safe,
     * especially for wall-attaching plants like {@link com.hungteen.pvz.common.entity.plants.SpikeWeed SpikeWeed}.
     * @see com.hungteen.pvz.common.entity.SimplePlant#baseTick() SimplePlant#baseTick() */
    @Nullable
    default Direction getGrowDirection() {
        return Direction.UP;
    }
    default BlockPos getRootBlockPos() {
        return ((Entity) this).getOnPos();
    }

    /**Contorlling if this plant can occupy space so other plants can't plant on.*/
    boolean takesCoincideDmg();

    /**Called in {@link com.hungteen.pvz.common.entity.SimplePlant#handleShovel(PlayerInteractEvent.EntityInteract) SimplePlant#handleShovel(EntityInteract)} .<br>
     Is effective for all IPlant.<br>
     For plants not extending SimplePlant, can use {@link com.hungteen.pvz.common.entity.SimplePlant#onBeingShoveled(Player, InteractionHand, LivingEntity) SimplePlant#onBeingShoveled(Player, InteractionHand, LivingEntity)}.*/
    boolean onBeingShoveled(Player player, InteractionHand handIn);
}
