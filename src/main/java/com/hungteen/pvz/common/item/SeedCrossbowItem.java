package com.hungteen.pvz.common.item;

import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.entity.bullet.SeedArrow;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.Util;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.ArrowItem;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Predicate;

public class SeedCrossbowItem extends CrossbowItem {
    private static List<Integer> enderBundleFilledWithSeeds = new ArrayList<>();
    public static final Predicate<ItemStack> ACCEPTABLE_BULLETS = ARROW_OR_FIREWORK.or((itemStack) -> {
        if (itemStack.getItem() instanceof SeedPacketItem<?>) return true;
        if (itemStack.getItem() instanceof EnderSeedBundleItem item) return enderBundleFilledWithSeeds.contains(item.getPointer(itemStack));
        return false;
    });

    public SeedCrossbowItem(Properties properties) {
        super(properties);
    }
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack itemstack = player.getItemInHand(hand);
        for (int k = 0; k < 9; k ++) {
            if (! PVZPlayerCapability.getEnderSeedBundleSlot(player, k).isEmpty()) enderBundleFilledWithSeeds.add(k);
        }
        if (isCharged(itemstack)) {
            performShooting(level, player, hand, itemstack, 3.14F, 1.0F);
            setCharged(itemstack, false);
            enderBundleFilledWithSeeds.clear();
            return InteractionResultHolder.consume(itemstack);
        } else if (!player.getProjectile(itemstack).isEmpty()) {
            if (!isCharged(itemstack)) {
//                this.startSoundPlayed = false;
//                this.midLoadSoundPlayed = false;
                player.startUsingItem(hand);
            }
            enderBundleFilledWithSeeds.clear();
            return InteractionResultHolder.consume(itemstack);
        } else {
            enderBundleFilledWithSeeds.clear();
            return InteractionResultHolder.fail(itemstack);
        }
    }

    public Predicate<ItemStack> getSupportedHeldProjectiles() {
        return ACCEPTABLE_BULLETS;
    }

    public static void shootProjectile(Level level, LivingEntity entity, InteractionHand hand, ItemStack itemStack, ItemStack bullet, float p_40900_, boolean p_40901_, float p_40902_, float p_40903_, float p_40904_) {
        if (!level.isClientSide) {
            boolean flag = bullet.is(Items.FIREWORK_ROCKET);
            Projectile projectile;
            if (flag) {
                projectile = new FireworkRocketEntity(level, bullet, entity, entity.getX(), entity.getEyeY() - (double)0.15F, entity.getZ(), true);
            } else if (bullet.getItem() instanceof SeedPacketItem<?>) {
                if (entity instanceof Player) {
                    projectile = new SeedArrow<>(level, entity, bullet);
                    projectile.setPos(entity.getX(), entity.getEyeY() - (double)0.15F, entity.getZ());
                } else {
                    projectile = new SeedArrow<>(PVZEntities.SEED_ARROW.get(), level);
                    projectile.setPos(entity.getX(), entity.getEyeY() - (double)0.15F, entity.getZ());
                }
            } else {
                projectile = getArrow(level, entity, itemStack, bullet);
                if (p_40901_ || p_40904_ != 0.0F) {
                    ((AbstractArrow)projectile).pickup = AbstractArrow.Pickup.CREATIVE_ONLY;
                }
            }

            if (entity instanceof CrossbowAttackMob) {
                CrossbowAttackMob crossbowattackmob = (CrossbowAttackMob)entity;
                crossbowattackmob.shootCrossbowProjectile(crossbowattackmob.getTarget(), itemStack, projectile, p_40904_);
            } else {
                Vec3 vec31 = entity.getUpVector(1.0F);
                Quaternion quaternion = new Quaternion(new Vector3f(vec31), p_40904_, true);
                Vec3 vec3 = entity.getViewVector(1.0F);
                Vector3f vector3f = new Vector3f(vec3);
                vector3f.transform(quaternion);
                projectile.shoot(vector3f.x(), vector3f.y(), vector3f.z(), p_40902_, p_40903_);
            }

            itemStack.hurtAndBreak(flag ? 3 : 1, entity, (p_40858_) -> {
                p_40858_.broadcastBreakEvent(hand);
            });
            level.addFreshEntity(projectile);
            level.playSound(null, entity.getX(), entity.getY(), entity.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, p_40900_);
        }
    }

    public static void performShooting(Level p_40888_, LivingEntity p_40889_, InteractionHand p_40890_, ItemStack p_40891_, float p_40892_, float p_40893_) {
        if (p_40889_ instanceof Player player && net.minecraftforge.event.ForgeEventFactory.onArrowLoose(p_40891_, p_40889_.level, player, 1, true) < 0) return;
        List<ItemStack> list = getChargedProjectiles(p_40891_);
        float[] afloat = getShotPitches(p_40889_.getRandom());

        for(int i = 0; i < list.size(); ++i) {
            ItemStack itemstack = list.get(i);
            boolean flag = p_40889_ instanceof Player && ((Player)p_40889_).getAbilities().instabuild;
            if (!itemstack.isEmpty()) {
                if (i == 0) {
                    shootProjectile(p_40888_, p_40889_, p_40890_, p_40891_, itemstack, afloat[i], flag, p_40892_, p_40893_, 0.0F);
                } else if (i == 1) {
                    shootProjectile(p_40888_, p_40889_, p_40890_, p_40891_, itemstack, afloat[i], flag, p_40892_, p_40893_, -10.0F);
                } else if (i == 2) {
                    shootProjectile(p_40888_, p_40889_, p_40890_, p_40891_, itemstack, afloat[i], flag, p_40892_, p_40893_, 10.0F);
                }
            }
        }

        onCrossbowShot(p_40888_, p_40889_, p_40891_);
    }

    public void releaseUsing(ItemStack p_40875_, Level p_40876_, LivingEntity p_40877_, int p_40878_) {
        int i = this.getUseDuration(p_40875_) - p_40878_;
        float f = getPowerForTime(i, p_40875_);
        if (f >= 1.0F && !isCharged(p_40875_) && tryLoadProjectiles(p_40877_, p_40875_)) {
            setCharged(p_40875_, true);
            SoundSource soundsource = p_40877_ instanceof Player ? SoundSource.PLAYERS : SoundSource.HOSTILE;
            p_40876_.playSound(null, p_40877_.getX(), p_40877_.getY(), p_40877_.getZ(), SoundEvents.CROSSBOW_LOADING_END, soundsource, 1.0F, 1.0F / (p_40876_.getRandom().nextFloat() * 0.5F + 1.0F) + 0.2F);
        }

    }

    private static boolean tryLoadProjectiles(LivingEntity shooter, ItemStack crossbow) {
        int i = EnchantmentHelper.getItemEnchantmentLevel(Enchantments.MULTISHOT, crossbow);
        int j = i == 0 ? 1 : 3;
        boolean instaBuild = shooter instanceof Player && ((Player)shooter).getAbilities().instabuild;

        if (shooter instanceof Player player) {
            for (int k = 0; k < 9; k ++) {
                if (! PVZPlayerCapability.getEnderSeedBundleSlot(player, k).isEmpty()) enderBundleFilledWithSeeds.add(k);
            }
        }
        ItemStack bullet = shooter.getProjectile(crossbow);
        enderBundleFilledWithSeeds.clear();
        ItemStack itemstack1 = bullet.copy();

        for(int k = 0; k < j; ++k) {
            if (k > 0) {
                bullet = itemstack1.copy();
            }

            if (bullet.isEmpty() && instaBuild) {
                bullet = new ItemStack(Items.ARROW);
                itemstack1 = bullet.copy();
            }

            if (! loadProjectile(shooter, crossbow, bullet, k > 0, instaBuild)) {
                return false;
            }
        }

        return true;
    }

    public static boolean loadProjectile(LivingEntity shooter, ItemStack crossBow, ItemStack bullet, boolean p_40866_, boolean instaBuild) {
        if (bullet.isEmpty()) {
            return false;
        } else {
            if (bullet.getItem() instanceof EnderSeedBundleItem item && shooter instanceof Player player) {
                bullet = PVZPlayerCapability.getEnderSeedBundleSlot(player, item.getPointer(bullet));
            }
            boolean isArrow = instaBuild && bullet.getItem() instanceof ArrowItem;
            boolean isSeed = bullet.getItem() instanceof SeedPacketItem<?>;
            ItemStack itemstack;
            if (! isArrow && ! instaBuild && ! p_40866_) {
                if (! isSeed) {
                    itemstack = bullet.split(1);
                    if (bullet.isEmpty() && shooter instanceof Player) {
                        ((Player)shooter).getInventory().removeItem(bullet);
                    }
                } else {
                    itemstack = bullet.copy();
                    if (! bullet.isEmpty() && shooter instanceof Player) {
                        ((SeedPacketItem<?>) bullet.getItem()).used(bullet, (Player) shooter);
                    }
                }
            } else {
                itemstack = bullet.copy();
            }

            addChargedProjectile(crossBow, itemstack);
            return true;
        }
    }

    public static boolean containsSeed(ItemStack p_40872_) {
        return getChargedProjectiles(p_40872_).stream().anyMatch((p_40870_) -> {
            return p_40870_.getItem() instanceof SeedPacketItem<?>;
        });
    }

    public static void registerProperties() {
        ItemProperties.register(PVZItems.SEED_CROSSBOW.get(), new ResourceLocation("pull"), (itemStack, level, entity, seed) -> {
            if (entity == null) {
                return 0.0F;
            } else {
                return CrossbowItem.isCharged(itemStack) ? 0.0F : (float)(itemStack.getUseDuration() - entity.getUseItemRemainingTicks()) / (float)CrossbowItem.getChargeDuration(itemStack);
            }
        });
        ItemProperties.register(PVZItems.SEED_CROSSBOW.get(), new ResourceLocation("pulling"), (itemStack, level, entity, seed) -> {
            return entity != null && entity.isUsingItem() && entity.getUseItem() == itemStack && !CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F;
        });
        ItemProperties.register(PVZItems.SEED_CROSSBOW.get(), new ResourceLocation("charged"), (itemStack, level, entity, seed) -> {
            return entity != null && CrossbowItem.isCharged(itemStack) ? 1.0F : 0.0F;
        });
        ItemProperties.register(PVZItems.SEED_CROSSBOW.get(), new ResourceLocation("firework"), (itemStack, level, entity, seed) -> {
            return entity != null && CrossbowItem.isCharged(itemStack) && CrossbowItem.containsChargedProjectile(itemStack, Items.FIREWORK_ROCKET) ? 1.0F : 0.0F;
        });
        ItemProperties.register(PVZItems.SEED_CROSSBOW.get(), Util.prefix("seed"), (itemStack, level, entity, seed) -> {
            return entity != null && CrossbowItem.isCharged(itemStack) && containsSeed(itemStack) ? 1.0F : 0.0F;
        });
    }
}