package com.hungteen.pvz.common.item;

import com.hungteen.pvz.api.interfaces.IGardenPlant;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.Random;

public class WateringPotItem extends BlockItem {
    public WateringPotItem(Properties p_40566_) {
        super(PVZBlocks.WATERING_POT.get(), p_40566_);
    }

    public ItemStack getDefaultInstance() {
        //omg they hardly used this method so why defined this??
        ItemStack stack = new ItemStack(this);
        stack.setDamageValue(5);
        return stack;
    }

    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        BlockHitResult blockhitresult = getPlayerPOVHitResult(level, player, ClipContext.Fluid.ANY);
        if (blockhitresult.getType() == HitResult.Type.MISS) {
            return InteractionResultHolder.pass(itemstack);
        } else if (blockhitresult.getType() != HitResult.Type.BLOCK) {
            return InteractionResultHolder.pass(itemstack);
        } else if (level.getBlockState(blockhitresult.getBlockPos()).getFluidState().is(Fluids.WATER)) {
            BlockPos blockpos = blockhitresult.getBlockPos();
            Direction direction = blockhitresult.getDirection();
            BlockPos blockpos1 = blockpos.relative(direction);
            if (level.mayInteract(player, blockpos) && player.mayUseItemAt(blockpos1, direction, itemstack)) {
                level.gameEvent(player, GameEvent.FLUID_PICKUP, blockpos);
                if (! level.isClientSide) {
                    itemstack.setDamageValue(0);
                }
                return InteractionResultHolder.sidedSuccess(itemstack, level.isClientSide());
            }
            return InteractionResultHolder.fail(itemstack);
        } else {
            return super.use(level, player, hand);
        }
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Player player = context.getPlayer();
        ItemStack itemStack = context.getItemInHand();
        if (player != null && ! player.isShiftKeyDown()) {
            BlockHitResult fluidResult = getPlayerPOVHitResult(context.getLevel(), player, ClipContext.Fluid.ANY);
            BlockPos blockpos = fluidResult.getBlockPos();
            Level level = context.getLevel();
            if (level.getBlockState(fluidResult.getBlockPos()).getFluidState().is(Fluids.WATER)) {
                if (level.mayInteract(player, blockpos)
                        && player.mayUseItemAt(blockpos, context.getClickedFace(), itemStack)) {
                    if (! context.getLevel().isClientSide) {
                        level.gameEvent(player, GameEvent.FLUID_PICKUP, blockpos);
                        itemStack.setDamageValue(0);
                        return InteractionResult.CONSUME;
                    } else {
                        BlockPos pos = fluidResult.getBlockPos();
                        Random random = new Random();
                        for (int i = 0; i < 5; i ++) {
                            level.addParticle(ParticleTypes.BUBBLE,
                                    pos.getX() + random.nextFloat() * 0.8 + 0.1,
                                    pos.getY() + 1,
                                    pos.getZ() + random.nextFloat() * 0.8 + 0.1,
                                    0, 0, 0);
                        }
                        for (int i = 0; i < 10; i ++) {
                            level.addParticle(ParticleTypes.SPLASH,
                                    pos.getX() + random.nextFloat() * 1,
                                    pos.getY() + random.nextFloat() * 1 + 1,
                                    pos.getZ() + random.nextFloat() * 1,
                                    0, 0, 0);
                        }
                        return InteractionResult.SUCCESS;
                    }
                }
            } else {
                return context.getLevel().isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
            }
        }
        return super.useOn(context);
    }

    @Override
    public @NotNull InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
        if (itemStack.getMaxDamage() - itemStack.getDamageValue() >= 1) {
            water(target, player, itemStack);
            return player.getLevel().isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
        } else {
            return InteractionResult.PASS;
        }
    }

    @Override
    public void onCraftedBy(ItemStack itemStack, Level level, Player player) {
        super.onCraftedBy(itemStack, level, player);
        itemStack.setDamageValue(this.getMaxDamage(itemStack));
    }

    public InteractionResult water(LivingEntity target, @Nullable Player player, ItemStack itemStack) {
        if (target instanceof IGardenPlant plant) {
            plant.onWatered(player, itemStack);
        }
        if (! target.level.isClientSide && itemStack.getMaxDamage() >= itemStack.getDamageValue()) {
            itemStack.hurt(1, target.getRandom(), (ServerPlayer) player);
            ((ServerLevel) target.level).sendParticles(ParticleTypes.DRIPPING_WATER,
                    target.getX(), target.getY() + target.getBbHeight(), target.getZ(),
                    3, 0.4, 0.5, 0.4, 0);
            ((ServerLevel) target.level).sendParticles(ParticleTypes.SPLASH,
                    target.getX(), target.getY() + target.getBbHeight(), target.getZ(),
                    20, 0.5, 0.5, 0.5, 0);
        }
        return target.getLevel().isClientSide ? InteractionResult.SUCCESS : InteractionResult.CONSUME;
    }

    public static void registerProperties() {
        ItemProperties.register(PVZItems.WATERING_POT.get(), new ResourceLocation("water"),
                (itemStack, level, entity, seed) -> 5 - itemStack.getDamageValue());
    }
}
