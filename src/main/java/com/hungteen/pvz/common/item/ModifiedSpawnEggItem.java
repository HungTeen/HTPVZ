package com.hungteen.pvz.common.item;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.BaseSpawner;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.SpawnerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraftforge.common.ForgeSpawnEggItem;

import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;

public class ModifiedSpawnEggItem extends ForgeSpawnEggItem {
    protected Consumer<Entity> entityModifier;
    public ModifiedSpawnEggItem(Supplier<? extends EntityType<? extends Mob>> type, Consumer<Entity> entityModifier, int backgroundColor, int highlightColor, Properties props) {
        super(type, backgroundColor, highlightColor, props);
        this.entityModifier = entityModifier;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level p_43225_, Player p_43226_, InteractionHand p_43227_) {
        ItemStack itemstack = p_43226_.getItemInHand(p_43227_);
        HitResult hitresult = getPlayerPOVHitResult(p_43225_, p_43226_, ClipContext.Fluid.SOURCE_ONLY);
        if (hitresult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemstack);
        } else if (!(p_43225_ instanceof ServerLevel)) {
            return InteractionResultHolder.success(itemstack);
        } else {
            BlockHitResult blockhitresult = (BlockHitResult)hitresult;
            BlockPos blockpos = blockhitresult.getBlockPos();
            if (!(p_43225_.getBlockState(blockpos).getBlock() instanceof LiquidBlock)) {
                return InteractionResultHolder.pass(itemstack);
            } else if (p_43225_.mayInteract(p_43226_, blockpos) && p_43226_.mayUseItemAt(blockpos, blockhitresult.getDirection(), itemstack)) {
                EntityType<?> entitytype = this.getType(itemstack.getTag());
                Entity entity = entitytype.spawn((ServerLevel)p_43225_, itemstack, p_43226_, blockpos, MobSpawnType.SPAWN_EGG, false, false);
                if (entity == null) {
                    return InteractionResultHolder.pass(itemstack);
                } else {


                    entityModifier.accept(entity); //pvz added.


                    if (!p_43226_.getAbilities().instabuild) {
                        itemstack.shrink(1);
                    }

                    p_43226_.awardStat(Stats.ITEM_USED.get(this));
                    p_43225_.gameEvent(p_43226_, GameEvent.ENTITY_PLACE, entity.position());
                    return InteractionResultHolder.consume(itemstack);
                }
            } else {
                return InteractionResultHolder.fail(itemstack);
            }
        }
    }
    public InteractionResult useOn(UseOnContext p_43223_) {
        Level level = p_43223_.getLevel();
        if (!(level instanceof ServerLevel)) {
            return InteractionResult.SUCCESS;
        } else {
            ItemStack itemstack = p_43223_.getItemInHand();
            BlockPos blockpos = p_43223_.getClickedPos();
            Direction direction = p_43223_.getClickedFace();
            BlockState blockstate = level.getBlockState(blockpos);
            if (blockstate.is(Blocks.SPAWNER)) {

                //TODO handle nbt when interacting with Spawner.
                BlockEntity blockentity = level.getBlockEntity(blockpos);
                if (blockentity instanceof SpawnerBlockEntity) {
                    BaseSpawner basespawner = ((SpawnerBlockEntity)blockentity).getSpawner();
                    EntityType<?> entitytype1 = this.getType(itemstack.getTag());
                    basespawner.setEntityId(entitytype1);
                    blockentity.setChanged();
                    level.sendBlockUpdated(blockpos, blockstate, blockstate, 3);
                    level.gameEvent(p_43223_.getPlayer(), GameEvent.BLOCK_CHANGE, blockpos);
                    itemstack.shrink(1);
                    return InteractionResult.CONSUME;
                }
            }

            BlockPos blockpos1;
            if (blockstate.getCollisionShape(level, blockpos).isEmpty()) {
                blockpos1 = blockpos;
            } else {
                blockpos1 = blockpos.relative(direction);
            }

            EntityType<?> entitytype = this.getType(itemstack.getTag());


            Entity entity = entitytype.spawn((ServerLevel)level, itemstack, p_43223_.getPlayer(), blockpos1, MobSpawnType.SPAWN_EGG, true, !Objects.equals(blockpos, blockpos1) && direction == Direction.UP);
            if (entity != null) {
                entityModifier.accept(entity); //pvz added.


                itemstack.shrink(1);
                level.gameEvent(p_43223_.getPlayer(), GameEvent.ENTITY_PLACE, blockpos);
            }

            return InteractionResult.CONSUME;
        }
    }
}
