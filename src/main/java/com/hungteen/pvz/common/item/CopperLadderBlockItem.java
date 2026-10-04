package com.hungteen.pvz.common.item;

import com.hungteen.pvz.common.block.CopperLadderBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;

/**
 * Right-click a copper ladder with a copper ladder to stack another one on the
 * top of the whole column, just like scaffolding does. Sneaking stacks downwards
 * instead. Only the first free spot within {@link com.hungteen.pvz.common.block.CopperLadderBlock#MAX_SEARCH_DISTANCE} blocks
 * around the clicked ladder can be reached.
 *
 * @program: HTPVZ2
 * @author: HungTeen
 * @create: 2026-10-01
 */
public class CopperLadderBlockItem extends BlockItem {

    public CopperLadderBlockItem(Block block, Properties properties) {
        super(block, properties);
    }

    @Nullable
    @Override
    public BlockPlaceContext updatePlacementContext(BlockPlaceContext context) {
        //getClickedPos() may return the position beside the clicked face, restore the real one.
        BlockPos clickedPos = context.getClickedPos();
        if (!context.replacingClickedOnBlock()) {
            clickedPos = clickedPos.relative(context.getClickedFace().getOpposite());
        }
        Level level = context.getLevel();
        if (!CopperLadderBlock.isCopperLadder(level.getBlockState(clickedPos))) {
            return context;
        }
        //stack upwards, downwards when sneaking.
        Direction direction = context.isSecondaryUseActive() ? Direction.DOWN : Direction.UP;
        BlockPos.MutableBlockPos pos = clickedPos.mutable();
        for (int i = 0; i < CopperLadderBlock.MAX_SEARCH_DISTANCE; i++) {
            pos.move(direction);
            if (!level.isClientSide && !level.isInWorldBounds(pos)) {
                Player player = context.getPlayer();
                int maxBuildHeight = level.getMaxBuildHeight();
                if (direction == Direction.UP && player instanceof ServerPlayer serverPlayer && pos.getY() >= maxBuildHeight) {
                    serverPlayer.sendSystemMessage(Component.translatable("build.tooHigh", maxBuildHeight - 1).withStyle(ChatFormatting.RED), true);
                }
                return null;
            }
            BlockState state = level.getBlockState(pos);
            //the first free spot of the column has been reached.
            if (!CopperLadderBlock.isCopperLadder(state)) {
                return state.canBeReplaced(context) ? BlockPlaceContext.at(context, pos, direction) : null;
            }
        }
        return null;
    }
}
