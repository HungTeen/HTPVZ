package com.hungteen.pvz.common.item;

import com.google.common.collect.ImmutableMultimap;
import com.google.common.collect.Multimap;
import com.hungteen.pvz.common.entity.zombies.Gargantuar;
import com.hungteen.pvz.common.network.PlayerKnockBackPacket;
import com.hungteen.pvz.common.register.PVZDamageSource;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.network.chat.TextColor;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
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
        },
                4, -3.7F, p_43117_);
    }
    @Override
    public boolean hurtEnemy(ItemStack itemStack, LivingEntity target, LivingEntity user) {
        if (user instanceof Player player && player.getAttackStrengthScale(0.5F) == 1) {
            target.hurt(PVZDamageSource.ignoreInvTime(DamageSource.playerAttack(player).bypassArmor()), (float) user.getAttributeValue(Attributes.ATTACK_DAMAGE) * 2F);
            List<Entity> list = player.level.getEntities((Entity) null,
                    new AABB(target.position().add(-0.8, 0, -0.8), target.position().add(0.8, 1, 0.8)),
                    (entity -> entity instanceof LivingEntity && EntityUtil.checkCanEntityBeAttack(user, entity)));
            ((ServerLevel) player.level).sendParticles(ParticleTypes.EXPLOSION, target.getX(), target.getY(0.5D), target.getZ(), 5, 1, 0.0D, 1, 0.0D);
            list.forEach((entity) -> {
                double distSqr = player.distanceToSqr(entity);
                double horizontalMovement = 2.5 / distSqr > 0.3 ? 2.5 / distSqr : 0;
                horizontalMovement = horizontalMovement > 1 ? 1 : horizontalMovement;
                AttributeInstance attribute = target.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
                double knockBackModifier = 0;
                if (attribute != null) {
                    knockBackModifier = attribute.getValue();
                }
                Vec3 vec3 = entity.position().subtract(player.position()).multiply(1, 0, 1).normalize()
                        .multiply(horizontalMovement, 0, horizontalMovement).add(0, 0.4, 0)
                        .multiply(1 - knockBackModifier, 1 - knockBackModifier * 0.5, 1 - knockBackModifier);
                if (entity instanceof ServerPlayer player1) {
                    PlayerKnockBackPacket.knockBack(player1, vec3, true);
                } else {
                    entity.setDeltaMovement(entity.getDeltaMovement().add(vec3));
                }
            });
        }
        return super.hurtEnemy(itemStack, target, user);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flagIn){
        tooltip.add(Component.translatable("tooltip.pvz.anvil_hammer").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        super.appendHoverText(stack, level, tooltip, flagIn);
    }
    @Override
    public Multimap<Attribute, AttributeModifier> getAttributeModifiers(EquipmentSlot slot, ItemStack stack) {
        if (slot == EquipmentSlot.MAINHAND || slot == EquipmentSlot.OFFHAND) {
            ImmutableMultimap.Builder<Attribute, AttributeModifier> builder = ImmutableMultimap.builder();
            super.getAttributeModifiers(slot, stack).forEach(builder::put);
            builder.put(Attributes.MOVEMENT_SPEED, new AttributeModifier(modifierUuid, "anvil_hammer", -0.4, AttributeModifier.Operation.MULTIPLY_TOTAL));
            return builder.build();
        }
        return super.getAttributeModifiers(slot, stack);
    }
}
