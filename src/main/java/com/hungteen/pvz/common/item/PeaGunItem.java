package com.hungteen.pvz.common.item;

import com.hungteen.pvz.common.entity.bullet.PeaBullet;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.*;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.client.extensions.common.IClientItemExtensions;
import org.apache.commons.lang3.function.TriFunction;

import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Predicate;

public class PeaGunItem extends ProjectileWeaponItem {

    //add reflections here.
    public static Map<Item, TriFunction<Level, Player, EquipmentSlot, Projectile>> itemMap = Map.of(
            PVZItems.PEA.get(), (level, shooter, slot) -> summonPeaBullet(level, shooter, slot, PVZItems.PEA.get()),
            PVZItems.SNOW_PEA.get(), (level, shooter, slot) -> summonPeaBullet(level, shooter, slot, PVZItems.SNOW_PEA.get()),
            PVZItems.FLAME_PEA.get(), (level, shooter, slot) -> summonPeaBullet(level, shooter, slot, PVZItems.FLAME_PEA.get()));

    public PeaGunItem(Properties p_41383_) {
        super(p_41383_);
    }
    public int getEnchantmentValue() {
        return 8;
    }
    public UseAnim getUseAnimation(ItemStack p_40678_) {
        return super.getUseAnimation(p_40678_);//UseAnim.BOW;
    }
    public boolean canApplyAtEnchantingTable(ItemStack stack, Enchantment enchantment)
    {
        return super.canApplyAtEnchantingTable(stack, enchantment) ||
                enchantment == Enchantments.INFINITY_ARROWS ||
                enchantment == Enchantments.FLAMING_ARROWS ||
                enchantment == Enchantments.QUICK_CHARGE;
    }
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack bulletStack = player.getProjectile(player.getItemInHand(hand));
        if (player.getAbilities().instabuild && bulletStack.getItem() == Items.ARROW) {
            bulletStack = PVZItems.PEA.get().getDefaultInstance();
        }

        if (bulletStack == ItemStack.EMPTY) {
            return super.use(level, player, hand);
        }
        if (itemMap.containsKey(bulletStack.getItem())) {
            if (level.isClientSide) {
                if (! player.isPassenger()) {
                    double resistance = 1 - player.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE);
                    player.setDeltaMovement(player.getDeltaMovement().add(new Vec3(
                            Math.sin(player.getYRot() / 57.3) * 0.5 * Math.cos(player.getXRot() / 57.3),
                            Math.sin((player.getXRot() % 360) / 57.3) * 0.3,
                            - Math.cos(player.getYRot() / 57.3) * 0.5 * Math.cos(player.getXRot() / 57.3))
                            .multiply(resistance, resistance, resistance)));
                }
            } else {
                if (player.isPassenger() && ! player.getVehicle().isPassenger()) {
                    double resistance = player.getVehicle() instanceof LivingEntity living ? 1 - living.getAttributeValue(Attributes.KNOCKBACK_RESISTANCE) : 1;
                    player.getVehicle().setDeltaMovement(player.getVehicle().getDeltaMovement().add(new Vec3(
                            Math.sin(player.getYRot() / 57.3) * 0.25 * Math.cos(player.getXRot() / 57.3),
                            Math.sin((player.getXRot() % 360) / 57.3) * 0.15,
                            - Math.cos(player.getYRot() / 57.3) * 0.25 * Math.cos(player.getXRot() / 57.3))
                            .multiply(resistance, resistance, resistance)));
                }
                ItemStack bullet =
                        player.getAbilities().instabuild || EnchantmentHelper.getTagEnchantmentLevel(Enchantments.INFINITY_ARROWS, player.getItemInHand(hand)) > 0 ?
                        bulletStack.copy().split(1): bulletStack.split(1);
                Projectile projectile = itemMap.get(bullet.getItem()).apply(level, player,
                        hand == InteractionHand.MAIN_HAND ? EquipmentSlot.MAINHAND : EquipmentSlot.OFFHAND);
                projectile.setOwner(player);
                projectile.shootFromRotation(player, player.getXRot(), player.getYRot(), 0.0F, 1.0F, 1.0F);
                level.addFreshEntity(projectile);
                player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), player.getAbilities().instabuild ? 3 :
                        Math.max(0, 30 - EnchantmentHelper.getTagEnchantmentLevel(Enchantments.QUICK_CHARGE, player.getItemInHand(hand)) * 4));
                player.getItemInHand(hand).hurtAndBreak(1, player, p -> p.broadcastBreakEvent(hand));
            }
            return InteractionResultHolder.consume(player.getItemInHand(hand));
        } else {
            player.getCooldowns().addCooldown(player.getItemInHand(hand).getItem(), 3);
            return super.use(level, player, hand);
        }
    }

    @Override
    public Predicate<ItemStack> getAllSupportedProjectiles() {
        return (itemStack) -> itemMap.containsKey(itemStack.getItem());
    }

    @Override
    public int getDefaultProjectileRange() {
        return 32;
    }

    /**only decoration.*/
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, Entity entity)
    {
        return EquipmentSlot.HEAD == armorType;
    }

    private static PeaBullet summonPeaBullet(Level level, Player shooter, EquipmentSlot slot, Item bulletItem) {
        PeaBullet pea = PVZEntities.PEA.get().create(level);
        pea.moveTo(shooter.getX(), shooter.getEyeY() - 0.2, shooter.getZ());
        boolean attachFire = EnchantmentHelper.getTagEnchantmentLevel(Enchantments.FLAMING_ARROWS, shooter.getItemBySlot(slot)) > 0;
        if (bulletItem == PVZItems.PEA.get()) {
            pea.setPeaType(attachFire ? PeaBullet.PeaType.Fire : PeaBullet.PeaType.Common);
            pea.setAttackDamage(6);
        } else if (bulletItem == PVZItems.SNOW_PEA.get()) {
            pea.setPeaType(attachFire ? PeaBullet.PeaType.Common : PeaBullet.PeaType.Ice);
            pea.setAttackDamage(4);
        } else if (bulletItem == PVZItems.FLAME_PEA.get()) {
            pea.setPeaType(PeaBullet.PeaType.Fire);
            pea.setAttackDamage(10);
        }
        pea.setNoGravity(true);
        return pea;
    }
    @Override
    public void initializeClient(Consumer<IClientItemExtensions> consumer) {
        consumer.accept(PeaGunClients.INSTANCE);
    }

    private static class PeaGunClients implements IClientItemExtensions {
        private static final PeaGunClients INSTANCE = new PeaGunClients();
        @Override
        public HumanoidModel.ArmPose getArmPose(LivingEntity entityLiving, InteractionHand hand, ItemStack itemStack)
        {
            return HumanoidModel.ArmPose.BOW_AND_ARROW;
        }
    }

}
