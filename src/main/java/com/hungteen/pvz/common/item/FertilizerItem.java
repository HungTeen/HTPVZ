package com.hungteen.pvz.common.item;

import com.hungteen.pvz.api.interfaces.IGardenPlant;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;

public class FertilizerItem extends Item {
    public FertilizerItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public InteractionResult interactLivingEntity(ItemStack itemStack, Player player, LivingEntity target, InteractionHand hand) {
        return fertilise(target, player, itemStack);
    }

    public InteractionResult fertilise(LivingEntity target, @Nullable Player player, ItemStack itemStack) {
        if (target instanceof IGardenPlant plant) {
            InteractionResult result = plant.onFertilized(player, itemStack);
            if (result.consumesAction()) {
                if (! target.level.isClientSide) {
                    itemStack.shrink(1);
                    ((ServerLevel) target.level).sendParticles(ParticleTypes.COMPOSTER,
                            target.getX(), target.getY() + target.getBbHeight(), target.getZ(),
                            10, 0.5, 0.5, 0.5, 0);
                }
            }
            return result;
        } else {
            return InteractionResult.PASS;
        }
    }
}
