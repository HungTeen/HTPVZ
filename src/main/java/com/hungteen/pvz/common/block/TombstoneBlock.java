package com.hungteen.pvz.common.block;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.block.entity.TombstoneBlockEntity;
import com.hungteen.pvz.common.register.PVZBlockEntities;
import com.hungteen.pvz.common.register.PVZBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.SpawnerBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.pathfinder.BlockPathTypes;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.Event;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.Random;

@Mod.EventBusSubscriber(modid = PVZMod.MODID)
public class TombstoneBlock extends SpawnerBlock {
    public static final EnumProperty<Direction.Axis> AXIS = BlockStateProperties.HORIZONTAL_AXIS;
    public static final IntegerProperty TYPE = IntegerProperty.create("type", 0, 2);
    protected static final VoxelShape X_AXIS_AABB_TALL = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 15.0D, 12.0D);
    protected static final VoxelShape Z_AXIS_AABB_TALL = Block.box(4.0D, 0.0D, 0.0D, 12.0D, 15.0D, 16.0D);
    protected static final VoxelShape X_AXIS_AABB_SHORT = Block.box(0.0D, 0.0D, 4.0D, 16.0D, 12.0D, 12.0D);
    protected static final VoxelShape Z_AXIS_AABB_SHORT = Block.box(4.0D, 0.0D, 0.0D, 12.0D, 12.0D, 16.0D);

    public TombstoneBlock(Properties p_56781_) {
        super(p_56781_);
        this.registerDefaultState(this.defaultBlockState().setValue(AXIS, Direction.Axis.Z));
        this.registerDefaultState(this.defaultBlockState().setValue(TYPE, 0));
    }

    @Override
    public VoxelShape getShape(BlockState blockState, BlockGetter p_54943_, BlockPos p_54944_, CollisionContext p_54945_) {
        return blockState.getValue(AXIS) == Direction.Axis.Z
                ? (blockState.getValue(TYPE) == 2 ? Z_AXIS_AABB_SHORT : Z_AXIS_AABB_TALL)
                : (blockState.getValue(TYPE) == 2 ? X_AXIS_AABB_SHORT : X_AXIS_AABB_TALL);
    }

    @Override
    public BlockState rotate(BlockState blockState, LevelAccessor level, BlockPos pos, Rotation rotation) {
        return blockState.setValue(AXIS, rotation.rotate(Direction.fromAxisAndDirection(blockState.getValue(AXIS), Direction.AxisDirection.POSITIVE)).getAxis());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> p_54935_) {
        p_54935_.add(AXIS);
        p_54935_.add(TYPE);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos p_154687_, BlockState p_154688_) {
        return new TombstoneBlockEntity(p_154687_, p_154688_);
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level p_154683_, BlockState p_154684_, BlockEntityType<T> p_154685_) {
        return createTickerHelper(p_154685_, PVZBlockEntities.TOMBSTONE.get()
                , p_154683_.isClientSide ? TombstoneBlockEntity::clientTick : TombstoneBlockEntity::serverTick);
    }

    @Override
    public int getExpDrop(BlockState state, net.minecraft.world.level.LevelReader world, net.minecraft.util.RandomSource randomSource, BlockPos pos, int fortune, int silktouch) {
        return 5 + randomSource.nextInt(3) + randomSource.nextInt(3);
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(AXIS, context.getHorizontalDirection().getAxis() == Direction.Axis.Z ? Direction.Axis.X : Direction.Axis.Z)
                .setValue(TYPE, new Random(context.getClickedPos().getX() * 13L + context.getClickedPos().getY() * 17L + context.getClickedPos().getZ() * 19L).nextInt(3));
    }

    @Override
    public ItemStack getCloneItemStack(BlockGetter p_56785_, BlockPos p_56786_, BlockState p_56787_) {
        return PVZBlocks.TOMBSTONE.get().asItem().getDefaultInstance();
    }

    @Nullable
    public BlockPathTypes getAdjacentBlockPathType(BlockState state, BlockGetter level, BlockPos pos, @org.jetbrains.annotations.Nullable Mob mob, BlockPathTypes originalType)
    {
        return BlockPathTypes.DANGER_OTHER;
    }

    @SubscribeEvent
    public static void onPlayerInteract(PlayerInteractEvent.RightClickBlock ev) {
        Level level = ev.getLevel();
        if (level.isClientSide) return;
        BlockPos blockpos = ev.getPos();
        BlockState blockstate = level.getBlockState(blockpos);
        if (blockstate.is(PVZBlocks.TOMBSTONE.get())) {
            ItemStack itemstack = ev.getEntity().getItemInHand(ev.getHand());
            if (ev.getEntity().getItemInHand(ev.getHand()).getItem() instanceof SpawnEggItem item) {
                BlockEntity blockentity = level.getBlockEntity(blockpos);
                if (blockentity instanceof SpawnerBlockEntity) {
                    BaseSpawner basespawner = ((SpawnerBlockEntity) blockentity).getSpawner();
                    EntityType<?> entitytype1 = item.getType(itemstack.getTag());
                    if (itemstack.hasTag() && itemstack.getTag().contains("EntityTag")) {
                        CompoundTag tag = itemstack.getTag().getCompound("EntityTag").copy();
                        tag.putString("id", "minecraft:pig");
                        SpawnData data = new SpawnData(tag, Optional.empty());
                        basespawner.setNextSpawnData(level, blockpos, data);
                    } else {
                        basespawner.setNextSpawnData(level, blockpos, new SpawnData(new CompoundTag(), Optional.empty()));
                    }
                    basespawner.setEntityId(entitytype1);
                    blockentity.setChanged();
                    level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
                    level.gameEvent(ev.getEntity(), GameEvent.BLOCK_CHANGE, blockpos);
                    if (! ev.getEntity().getAbilities().instabuild) itemstack.shrink(1);
                    ev.setUseItem(Event.Result.DENY);
                }

            }
        }
    }
}
