package com.hungteen.pvz.api.events;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.eventbus.api.Cancelable;

import javax.annotation.Nullable;

/**When SeedPacketItem checking if a plant can be planted or SimplePlant checking is position is safe, this event is fired both before and after vanilla pvz methods.
 * <br>If {@link Phase Phase} is PRE, the event is cancellable.
 * <br>When cancelled, vanilla PVZ methods on testing plant condition won't launch, but POST event still launches.
 * <p><b>If the plant is planted within this event, cancel it.</b></p>
 * <p><b>result</b> : the fail reason of planting. Null for success.</p>*/
@Cancelable
public class PVZPlantConditionMatchingEvent extends EntityEvent {
    public final Phase phase;
    public final PVZResourceEvent.CheckPlantConditionEvent event;
    public boolean isPlanting;
    public MutableComponent result;
    public PVZPlantConditionMatchingEvent(Entity entity, PVZResourceEvent.CheckPlantConditionEvent event
            , MutableComponent result, boolean isPlanting, Phase phase) {
        super(entity);
        this.phase = phase;
        this.event = event;
        this.isPlanting = isPlanting;
        this.result = result;
    }

    /**@see com.hungteen.pvz.common.item.SeedPacketItem#plantOnBlock(Player, ItemStack, Level, BlockPos, Direction) SeedPacketItem#plantOnBlock()
     * @see com.hungteen.pvz.api.interfaces.INeedSafeSituation#isPositionSafe(PVZResourceEvent.CheckPlantConditionEvent, Level, BlockPos, Direction, boolean) isPositionSafe()*/
    public static class OnBlock extends PVZPlantConditionMatchingEvent {
        public Level level;
        public BlockPos pos;
        public Direction direction;
        public OnBlock(Entity entity, PVZResourceEvent.CheckPlantConditionEvent event, MutableComponent result
                , Level level, BlockPos pos, @Nullable Direction direction, boolean isPlanting, Phase phase) {
            super(entity, event, result, isPlanting, phase);
            this.level = level;
            this.pos = pos;
            this.direction = direction;
        }
    }

    /**@see com.hungteen.pvz.common.item.SeedPacketItem#plantOnEntity(Player, ItemStack, Level, Entity)  SeedPacketItem#plantOnEntity()
     * @see com.hungteen.pvz.api.interfaces.INeedSafeSituation#isVehicleSafe(PVZResourceEvent.CheckPlantConditionEvent, Entity, boolean) isVehicleSafe() */
    public static class OnEntity extends PVZPlantConditionMatchingEvent {
        public Entity target;
        public OnEntity(Entity entity, PVZResourceEvent.CheckPlantConditionEvent event, MutableComponent result
                , Entity target, boolean isPlanting, Phase phase) {
            super(entity, event, result, isPlanting, phase);
            this.target = target;
        }
    }

    public enum Phase {
        PRE, POST
    }
}
