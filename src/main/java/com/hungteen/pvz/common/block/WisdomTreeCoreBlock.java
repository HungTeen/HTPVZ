package com.hungteen.pvz.common.block;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.tags.PVZBlockTags;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RotatedPillarBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;

import javax.annotation.Nullable;
import java.util.List;

public class WisdomTreeCoreBlock extends HorizontalDirectionalBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    public static final IntegerProperty LV = IntegerProperty.create("level", 1, 10);

    public WisdomTreeCoreBlock(Properties p_54120_) {
        super(p_54120_);
        this.registerDefaultState(this.stateDefinition.any().setValue(FACING, Direction.NORTH));
        this.registerDefaultState(this.stateDefinition.any().setValue(LV, 1));
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection().getOpposite())
                .setValue(LV, 1);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_51385_) {
        super.createBlockStateDefinition(p_51385_);
        p_51385_.add(FACING);
        p_51385_.add(LV);
    }

    public void animateTick(BlockState blockState, Level level, BlockPos blockPos, RandomSource random) {
        if ((long)level.random.nextInt(5) <= 5) {
            level.addParticle(ParticleTypes.ENCHANT,
                    (double)blockPos.getX() + 0.5D, (double)blockPos.getY() + 2.0D, (double)blockPos.getZ() + 0.5D,
                    (double)(random.nextFloat() + random.nextInt(7)) - 3.5D, random.nextFloat() + random.nextInt(5) - 3.0F,
                    (double)(random.nextFloat() + random.nextInt(7)) - 3.5D);
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable BlockGetter level, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("hint.pvz.block.wisdom_tree_core").withStyle(ChatFormatting.GRAY));
        super.appendHoverText(stack, level, tooltip, flagIn);
    }

    public boolean growAt(Level level, BlockPos pos, int rootLv, int lv) {
        if (level.getBlockState(pos).isAir() || level.getBlockState(pos).is(PVZBlockTags.WISDOM_TREE_REPLACEABLE)) {
            level.setBlockAndUpdate(pos, PVZBlocks.WISDOM_TREE_LOG.get().defaultBlockState()
                    .setValue(WisdomTreeLogBlock.LV, lv - 1)
                    .setValue(WisdomTreeLogBlock.ROOT_LV, rootLv)
                    .setValue(WisdomTreeLogBlock.DISTANCE, rootLv)
                    .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                    .setValue(WisdomTreeLogBlock.PERSISTENT, true));
            return true;
        } else if (level.getBlockState(pos).is(PVZBlocks.WISDOM_TREE_LOG.get()) &&
                level.getBlockState(pos.above()).isAir() || level.getBlockState(pos.above()).is(PVZBlockTags.WISDOM_TREE_REPLACEABLE)) {
            level.setBlockAndUpdate(pos.above(), PVZBlocks.WISDOM_TREE_LOG.get().defaultBlockState()
                    .setValue(WisdomTreeLogBlock.LV, lv - 1)
                    .setValue(WisdomTreeLogBlock.ROOT_LV, rootLv)
                    .setValue(WisdomTreeLogBlock.DISTANCE, rootLv - 1)
                    .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
            return true;
        } else {
            return false;
        }
    }

    public void updateLevel(BlockState blockState, ServerLevel level, BlockPos pos) {

    }

    @Override
    public void randomTick(BlockState blockState, ServerLevel level, BlockPos pos, RandomSource random) {
        super.randomTick(blockState, level, pos, random);
        int lv = blockState.getValue(LV);
        if (!PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.killWisdomTree) && random.nextInt(5) == 0) {
            if (level.getBlockState(pos.above()).isAir() || level.getBlockState(pos.above()).is(PVZBlockTags.WISDOM_TREE_REPLACEABLE)) {
                level.setBlockAndUpdate(pos.above(), PVZBlocks.WISDOM_TREE_LOG.get().defaultBlockState()
                        .setValue(WisdomTreeLogBlock.LV, lv)
                        .setValue(WisdomTreeLogBlock.ROOT_LV, lv)
                        .setValue(WisdomTreeLogBlock.DISTANCE, lv)
                        .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y));
            } else if (lv > 3) {
                Direction direction = blockState.getValue(FACING);
                if (random.nextBoolean()) {
                    direction = random.nextBoolean() ? direction : direction.getOpposite();
                } else {
                    direction = random.nextBoolean() ? direction.getClockWise() :
                            direction.getClockWise().getOpposite();
                }
                if (direction != blockState.getValue(FACING)) {
                    if (! growAt(level, pos.relative(direction), lv, lv - 1)) {
                        if (lv > 5) {
                            BlockPos pos1 = pos.relative(direction).relative(random.nextBoolean() ? direction.getClockWise() : direction.getClockWise().getOpposite());
                            if (! growAt(level, pos1, lv, lv - 2)) {
                                if (lv > 7) {
                                    if (level.getBlockState(pos.relative(direction).relative(direction)).isAir() ||
                                            level.getBlockState(pos.relative(direction).relative(direction)).is(PVZBlockTags.WISDOM_TREE_REPLACEABLE)) {
                                        level.setBlockAndUpdate(pos.relative(direction).relative(direction), PVZBlocks.WISDOM_TREE_LOG.get().defaultBlockState()
                                                .setValue(WisdomTreeLogBlock.LV, 1)
                                                .setValue(WisdomTreeLogBlock.ROOT_LV, lv)
                                                .setValue(WisdomTreeLogBlock.DISTANCE, lv)
                                                .setValue(RotatedPillarBlock.AXIS, Direction.Axis.Y)
                                                .setValue(WisdomTreeLogBlock.PERSISTENT, true));
                                    }
                                }
                            }
                        }
                    }
                } else {
                    growAt(level, pos.relative(direction).above().above(), lv, lv - 1);
                }
            }
        }
    }
}
