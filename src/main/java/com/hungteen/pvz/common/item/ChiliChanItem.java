package com.hungteen.pvz.common.item;

import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.Vec3;

public class ChiliChanItem extends ShovelItem {
    public ChiliChanItem(Properties p_43117_) {
        super(new Tier() {
            @Override
            public int getUses() {
                return 233;
            }

            @Override
            public float getSpeed() {
                return 4F;
            }

            @Override
            public float getAttackDamageBonus() {
                return 2F;
            }

            @Override
            public int getLevel() {
                return 3;
            }

            @Override
            public int getEnchantmentValue() {
                return 10;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(PVZItems.VENTUS_ESSENCE.get());
            }
        }, 1.5F, -3.2F, p_43117_);
    }
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity user) {
        if (! (user instanceof Player player) || player.getAttackStrengthScale(0.5F) == 1) {
            Vec3 vec3 = target.position().subtract(user.position());
            target.setDeltaMovement(target.getDeltaMovement().add(vec3.normalize().scale(Math.min(2 / vec3.distanceTo(Vec3.ZERO), 1))));
            ((ServerLevel) user.level).sendParticles(ParticleTypes.EXPLOSION,
                    target.getX(), target.getY(), target.getZ(), 5, 0.5, 0.5D, 0.5D, 0.0D);
        }
        return super.hurtEnemy(itemStack, target, user);
    }
}
