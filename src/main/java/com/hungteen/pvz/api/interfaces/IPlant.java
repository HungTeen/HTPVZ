package com.hungteen.pvz.api.interfaces;

import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

/**Use this interface to identify if an entity is a plant .<br>
 * If need skills, implements {@link IHaveSkills} .<br>
 * Also, override {@link net.minecraft.world.entity.Mob#removeWhenFarAway(double)} .
 */
public interface IPlant{

    /**EntityData controlling if the plant need proper block to plant on.*/
    EntityDataAccessor<Boolean> root();

    /**Contorlling if this plant can occupy space so other plants can't plant on.*/
    boolean takesCoincideDmg();

    /**Called in {@link com.hungteen.pvz.common.entity.SimplePlant#handleShovel(PlayerInteractEvent.EntityInteract)} .<br>
     Is effective for all IPlant.*/
    boolean onBeingShoveled(Player player, InteractionHand handIn);

}
