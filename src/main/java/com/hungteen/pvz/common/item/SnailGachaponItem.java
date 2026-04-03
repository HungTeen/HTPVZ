package com.hungteen.pvz.common.item;

import com.hungteen.pvz.common.register.PVZCriteriaTriggers;
import com.hungteen.pvz.common.register.PVZEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;

import java.util.Objects;

public class SnailGachaponItem extends Item {
    public SnailGachaponItem(Properties p_41383_) {
        super(p_41383_);
    }

    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        } else {
            ItemStack itemstack = context.getItemInHand();
            BlockPos blockpos = context.getClickedPos();
            Direction direction = context.getClickedFace();
            Player player = context.getPlayer();
            BlockState blockstate = level.getBlockState(blockpos);

            BlockPos blockpos1;
            if (blockstate.getCollisionShape(level, blockpos).isEmpty()) {
                blockpos1 = blockpos;
            } else {
                blockpos1 = blockpos.relative(direction);
            }

            EntityType<?> entitytype = this.getType(player != null ? player.getRandom() : null);
            if (player instanceof ServerPlayer player1 && entitytype == PVZEntities.SNAIL.get()) {
                PVZCriteriaTriggers.SNAIL.trigger(player1);
            }
            if (entitytype.spawn((ServerLevel)level, itemstack, context.getPlayer(), blockpos1, MobSpawnType.SPAWN_EGG, true, !Objects.equals(blockpos, blockpos1) && direction == Direction.UP) != null) {
                itemstack.shrink(1);
                level.gameEvent(context.getPlayer(), GameEvent.ENTITY_PLACE, blockpos);
            }

            return InteractionResult.CONSUME;
        }
    }

    public EntityType<?> getType(RandomSource random) {
        if (random == null) {
            return PVZEntities.WALL_NAIL.get();
        }
        int result = random.nextInt(50);
        if (result == 0) {
            return PVZEntities.SNAIL.get();
        } else if (result < 35) {
            return PVZEntities.WALL_NAIL.get();
        } else {
            return PVZEntities.FUNGICICOLIDAE.get();
        }
    }
}
