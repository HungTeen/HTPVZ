package com.hungteen.pvz.api.interfaces;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.events.PVZPlantConditionMatchingEvent;
import com.hungteen.pvz.api.events.PVZResourceEvent;
import com.hungteen.pvz.common.entity.SimplePlant;
import com.hungteen.pvz.common.register.PVZSeedPackets;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.MinecraftForge;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/**Use this interface to identify if an entity is a plant .<br>
 * If need skills, implements {@link IHaveSkills} .<br>
 * Also, override {@link net.minecraft.world.entity.Mob#removeWhenFarAway(double)} .
 */

public interface IPlant extends INeedSafeSituation, IHaveSkills {
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

    /**Called in {@link com.hungteen.pvz.common.entity.SimplePlant#mobInteract(Player, InteractionHand)}  SimplePlant#handleShovel(EntityInteract)} .<br>
     Is effective for all IPlant.<br>
     For plants not extending SimplePlant, can use {@link com.hungteen.pvz.common.entity.SimplePlant#onBeingShoveled(Player, InteractionHand, LivingEntity) SimplePlant#onBeingShoveled(Player, InteractionHand, LivingEntity)}.*/
    boolean onBeingShoveled(Player player, InteractionHand handIn);

    /**Whether garden flower pot should be water pot when a sprout transformed into this plant in Zen Garden.*/
    default boolean needWaterPotInGarden() {
        return false;
    }

    /**blockTags this plant can plant on. if {@link SimplePlant#ROOT} is false then any block is accepted.*/
    default Set<TagKey<Block>> getAcceptableTags() {
        return Set.of(PVZBlockTags.PLANTABLE_DIRT);
    }
    /**@return if the plant can be planted on the block state*/
    default boolean plantableOn(BlockState blockState) {
        if (this instanceof LivingEntity plant) {
            return !plant.getEntityData().get(((IPlant) plant).root()) || ((IPlant) plant).getAcceptableTags().stream().anyMatch(blockState::is);
        }
        return true;
    }
    /**called when a sprout transform into this plant in Zen Garden. Only in client side.*/
    default void setupPresentationAnim() {}
    @Override
    default List<Skill> getStaticSkillList() {
        List<Skill> list = new ArrayList<>(getBasicStaticSkillList());
        List<Skill> additional = PVZSeedPackets.additionalSkills.get(((Entity) this).getType());
        if (additional != null) list.addAll(additional);
        return list;
    }
    List<Skill> getBasicStaticSkillList();
    /**minimum ticks the plant will live before it can be removed naturally. set it to a number less than 0 to disable natural disappearance for the plant.*/
    default int getDisappearTicks() {
        return 12000;
    }

    default MutableComponent isPositionSafe(@Nullable PVZResourceEvent.CheckPlantConditionEvent event, Level level, BlockPos pos, @Nullable Direction direction, boolean isPlanting) {
        PVZPlantConditionMatchingEvent.OnBlock preCondition = new PVZPlantConditionMatchingEvent.OnBlock(
                (Entity) this, event, null, level, pos, direction, true, PVZPlantConditionMatchingEvent.Phase.PRE);
        MinecraftForge.EVENT_BUS.post(preCondition);
        if (preCondition.isCanceled()) {
            return preCondition.result;
        }

        MutableComponent result = this.customPositionSafe(event, level, pos, direction, isPlanting);

        PVZPlantConditionMatchingEvent.OnBlock postCondition = new PVZPlantConditionMatchingEvent.OnBlock(
                (Entity) this, event, result, level, pos, direction, true, PVZPlantConditionMatchingEvent.Phase.POST);
        MinecraftForge.EVENT_BUS.post(postCondition);
        return postCondition.result;
    }


    default MutableComponent isVehicleSafe(@Nullable PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        PVZPlantConditionMatchingEvent.OnEntity preCondition = new PVZPlantConditionMatchingEvent.OnEntity(
                (Entity) this, event, null, target, true, PVZPlantConditionMatchingEvent.Phase.PRE);
        MinecraftForge.EVENT_BUS.post(preCondition);
        if (preCondition.isCanceled()) {
            return preCondition.result;
        }

        MutableComponent result = this.customVehicleSafe(event, target, isPlanting);

        PVZPlantConditionMatchingEvent.OnEntity postCondition = new PVZPlantConditionMatchingEvent.OnEntity(
                (Entity) this, event, result, target, true, PVZPlantConditionMatchingEvent.Phase.POST);
        MinecraftForge.EVENT_BUS.post(postCondition);
        return postCondition.result;
    }

    default MutableComponent customPositionSafe(@Nullable PVZResourceEvent.CheckPlantConditionEvent event, Level level, BlockPos pos, @Nullable Direction direction, boolean isPlanting) {
        return null;
    }
    default MutableComponent customVehicleSafe(@javax.annotation.Nullable PVZResourceEvent.CheckPlantConditionEvent event, Entity target, boolean isPlanting) {
        return null;
    }

    interface IWaterPlant extends IPlant {
        @Override
        default boolean needWaterPotInGarden() {
            return true;
        }
    }
}
