package com.hungteen.pvz.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.hungteen.pvz.common.entity.zombies.Gargantuar;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;

import java.util.List;
import java.util.UUID;
import java.util.function.Consumer;

public class AnvilHammerItem extends SwordItem {
    private static final UUID modifierUuid = UUID.fromString("70580191-35bc-68f7-c1f0-b133ca9ff778");
    public AnvilHammerItem(Properties p_43117_) {
        super(new Tier() {
            @Override
            public int getUses() {
                return 150;
            }

            @Override
            public float getSpeed() {
                return 0F;
            }

            @Override
            public float getAttackDamageBonus() {
                return 0F;
            }

            @Override
            public int getLevel() {
                return 0;
            }

            @Override
            public int getEnchantmentValue() {
                return 10;
            }

            @Override
            public Ingredient getRepairIngredient() {
                return Ingredient.of(Items.ANVIL);
            }
        }, 14, -3.7F, p_43117_);
    }
    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity user) {
        if (! (user instanceof Player player) || player.getAttackStrengthScale(0.5F) == 1) {
            List<Entity> list = user.level.getEntities((Entity) null,
                    new AABB(target.position().add(-0.8, 0, -0.8), target.position().add(0.8, 1, 0.8)),
                    (entity -> entity instanceof LivingEntity && EntityUtil.checkCanEntityBeAttack(user, entity)));
            ((ServerLevel) user.level).sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY(0.5D), target.getZ(), 5, 1, 0.0D, 1, 0.0D);

            list.forEach((entity) -> {
                double distSqr = user.distanceToSqr(entity);
                double horizontalMovement = 2.5 / distSqr > 0.3 ? 2.5 / distSqr : 0;
                horizontalMovement = horizontalMovement > 1 ? 1 : horizontalMovement;
                Vec3 vec3 = entity.position().subtract(user.position()).multiply(1, 0, 1).normalize()
                        .multiply(horizontalMovement, 0, horizontalMovement).add(0, 0.4, 0);
                entity.setDeltaMovement(entity.getDeltaMovement().add(vec3));
            });
        }
        return super.hurtEnemy(itemStack, target, user);
    }
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            super.getAttributeModifiers(slot, stack).forEach(builder::put);
            builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(modifierUuid, "anvil_hammer",-0.4, AttributeModifier.Operation.MULTIPLY_TOTAL));
            return builder.build();
        }
        return super.getAttributeModifiers(slot, stack);
    }
}
