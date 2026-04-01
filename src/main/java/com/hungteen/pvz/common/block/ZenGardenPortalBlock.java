package com.hungteen.pvz.common.block;

import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.world.zen_garden.ZenGardenTeleporter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.DirectionProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

public class ZenGardenPortalBlock extends HorizontalDirectionalBlock implements SimpleWaterloggedBlock {
    public static final DirectionProperty FACING = HorizontalDirectionalBlock.FACING;
    private static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;
    private static final VoxelShape SHAPE = Block.box(4.0D, 0.0D, 4.0D, 12.0D, 9.0D, 12.0D);
    public ZenGardenPortalBlock(Properties p_49795_) {
        super(p_49795_);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(FACING, Direction.NORTH)
                .setValue(WATERLOGGED, Boolean.FALSE));
    }

    public InteractionResult use(BlockState blockState, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (! level.isClientSide) {
            this.transport(player, pos);
//            this.playSound(player, level, pos, true); TODO add sound.
            level.gameEvent(player, GameEvent.TELEPORT, pos);
        }
        return InteractionResult.sidedSuccess(level.isClientSide);
    }

    public void transport(Player player, BlockPos pos) {
        if (player.level instanceof ServerLevel serverlevel) {
            MinecraftServer server = serverlevel.getServer();
            ResourceKey<Level> resourcekey = player.level.dimension() == ZenGardenTeleporter.GARDEN ? Level.OVERWORLD : ZenGardenTeleporter.GARDEN;
            ServerLevel destWorld = server.getLevel(resourcekey);
            if (destWorld != null) {
                player.setPortalCooldown();
                player.changeDimension(destWorld, new ZenGardenTeleporter(destWorld));
                PVZPlayerCapability.setTeleportPos(player, Vec3.atBottomCenterOf(pos), destWorld);
            }
        }
    }

    public void animateTick(BlockState p_221794_, Level p_221795_, BlockPos p_221796_, RandomSource p_221797_) {
        if (p_221797_.nextInt(100) == 0) {
            p_221795_.playLocalSound((double)p_221796_.getX() + 0.5D, (double)p_221796_.getY() + 0.5D, (double)p_221796_.getZ() + 0.5D, SoundEvents.PORTAL_AMBIENT, SoundSource.BLOCKS, 0.5F, p_221797_.nextFloat() * 0.4F + 0.8F, false);
        }

        for(int i = 0; i < 4; ++i) {
            double d0 = (double)p_221796_.getX() + p_221797_.nextDouble();
            double d1 = (double)p_221796_.getY() + p_221797_.nextDouble();
            double d2 = (double)p_221796_.getZ() + p_221797_.nextDouble();
            double d3 = ((double)p_221797_.nextFloat() - 0.5D) * 0.5D;
            double d4 = ((double)p_221797_.nextFloat() - 0.5D) * 0.5D;
            double d5 = ((double)p_221797_.nextFloat() - 0.5D) * 0.5D;
            int j = p_221797_.nextInt(2) * 2 - 1;
            if (!p_221795_.getBlockState(p_221796_.west()).is(this) && !p_221795_.getBlockState(p_221796_.east()).is(this)) {
                d0 = (double)p_221796_.getX() + 0.5D + 0.25D * (double)j;
                d3 = p_221797_.nextFloat() * 2.0F * (float)j;
            } else {
                d2 = (double)p_221796_.getZ() + 0.5D + 0.25D * (double)j;
                d5 = p_221797_.nextFloat() * 2.0F * (float)j;
            }

            p_221795_.addParticle(ParticleTypes.PORTAL, d0, d1, d2, d3, d4, d5);
        }

    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        FluidState fluidstate = context.getLevel().getFluidState(context.getClickedPos());
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(WATERLOGGED, fluidstate.isSourceOfType(Fluids.WATER));
    }
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_51385_) {
        super.createBlockStateDefinition(p_51385_);
        p_51385_.add(FACING);
        p_51385_.add(WATERLOGGED);
    }
    @Override
    public VoxelShape getCollisionShape(BlockState p_49760_, BlockGetter p_49761_, BlockPos p_49762_, CollisionContext p_49763_) {
        return SHAPE;
    }
    @Override
    public VoxelShape getShape(BlockState p_49755_, BlockGetter p_49756_, BlockPos p_49757_, CollisionContext p_49758_) {
        return SHAPE;
    }
    @Override
    public RenderShape getRenderShape(BlockState p_49753_) {
        return RenderShape.MODEL;
    }
    @Override
    public boolean placeLiquid(LevelAccessor level, BlockPos blockPos, BlockState blockState, FluidState fluidState) {
        if (!blockState.getValue(BlockStateProperties.WATERLOGGED) && fluidState.getType() == Fluids.WATER) {
            if (! level.isClientSide()) {
                level.setBlock(blockPos, blockState.setValue(BlockStateProperties.WATERLOGGED, Boolean.TRUE), 3);
                level.scheduleTick(blockPos, fluidState.getType(), fluidState.getType().getTickDelay(level));
            }
            return true;
        } else {
            return false;
        }
    }
    @Override
    public boolean canPlaceLiquid(BlockGetter level, BlockPos blockPos, BlockState blockState, Fluid fluidState) {
        return ! blockState.getValue(BlockStateProperties.WATERLOGGED) && fluidState == Fluids.WATER;
    }
    @Override
    public FluidState getFluidState(BlockState p_56397_) {
        return p_56397_.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(p_56397_);
    }
}
