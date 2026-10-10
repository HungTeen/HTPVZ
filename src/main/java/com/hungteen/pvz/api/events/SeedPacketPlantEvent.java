package com.hungteen.pvz.api.events;

import com.hungteen.pvz.api.interfaces.ISunContainer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.eventbus.api.Event;

/**This event is called on server whenever a player plants with a seed packet and passed checking. With this event you can modify the entity. This event is not cancellable.
 * @see com.hungteen.pvz.common.item.SeedPacketItem#plantOnEntity(Player, ItemStack, Level, Entity) plantOnEntity(...)
 * @see com.hungteen.pvz.common.item.SeedPacketItem#plantOnBlock(Player, ItemStack, Level, BlockPos, Direction) plantOnBlock(...)*/
public class SeedPacketPlantEvent extends Event {
    public final ItemStack itemStack;
    public final Entity entity;
    public final ISunContainer planter;
    public SeedPacketPlantEvent(ISunContainer container, ItemStack itemStack, Entity entity) {
        this.itemStack = itemStack;
        this.entity = entity;
        this.planter = container;
    }

}
