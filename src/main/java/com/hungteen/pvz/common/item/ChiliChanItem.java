package com.hungteen.pvz.common.item;

import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Explosion;

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
        }, 1.5F, -3F, p_43117_);
    }
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity user) {
        target.level.explode(user, PVZDamageSource.ignoreInvTime(PVZDamageSource.multiply(PVZDamageSource.knockBack(DamageSource.explosion(user), 0F), 0F)), null,
                user.getX(), user.getY(), user.getZ(), 3F, false, Explosion.BlockInteraction.NONE);
        return super.hurtEnemy(itemStack, target, user);
    }
}
