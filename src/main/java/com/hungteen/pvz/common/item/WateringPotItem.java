package com.hungteen.pvz.common.item;

import com.hungteen.pvz.api.interfaces.IGardenPlant;
import com.hungteen.pvz.common.register.PVZBlocks;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.BlockHitResult;

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
    public InteractionResult useOn(UseOnContext context) {
        if (context.getItemInHand().getDamageValue() != 0 && context.getPlayer() != null) {
            BlockHitResult fluidResult = getPlayerPOVHitResult(context.getLevel(), context.getPlayer(), ClipContext.Fluid.ANY);
            Level level = context.getLevel();
            if (level.getBlockState(fluidResult.getBlockPos()).getFluidState().is(Fluids.WATER)) {
                if (! context.getLevel().isClientSide) {
                    context.getItemInHand().setDamageValue(0);
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
            } else {
                return super.useOn(context);
            }
        } else {
            return super.useOn(context);
        }
    }
    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
        if (itemStack.getMaxDamage() - itemStack.getDamageValue() >= 1) {
            if (target instanceof IGardenPlant plant) {
                plant.onWatered(player, itemStack);
            }
            if (! player.level.isClientSide) {
                itemStack.hurt(1, player.getRandom(), (ServerPlayer) player);
            } else {
                RandomSource random = target.getRandom();
                for (int i = 0; i < 3; i ++) {
                    player.level.addParticle(ParticleTypes.DRIPPING_WATER,
                            target.getX() + random.nextFloat() * 0.8 - 0.4,
                            target.getY() + target.getBbHeight() + random.nextFloat() * 0.5 - 0.5,
                            target.getZ() + random.nextFloat() * 0.8 - 0.4,
                            0, 0, 0);
                }
                for (int i = 0; i < 20; i ++) {
                    player.level.addParticle(ParticleTypes.SPLASH,
                            target.getX() + random.nextFloat() * 1 - 0.5,
                            target.getY() + target.getBbHeight() + random.nextFloat() * 1 - 0.5,
                            target.getZ() + random.nextFloat() * 1 - 0.5,
                            0, 0, 0);
                }
            }
            return InteractionResult.CONSUME;
        } else {
            return InteractionResult.PASS;
        }
    }

    public static void registerProperties() {
        ItemProperties.register(PVZItems.WATERING_POT.get(), new ResourceLocation("water"),
                (itemStack, level, entity, seed) -> 5 - itemStack.getDamageValue());
    }
}
