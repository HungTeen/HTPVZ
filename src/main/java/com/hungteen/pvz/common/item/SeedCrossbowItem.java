package com.hungteen.pvz.common.item;

import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.CrossbowAttackMob;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.item.CrossbowItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;

public class SeedCrossbowItem extends CrossbowItem {
    public SeedCrossbowItem(Properties properties) {
        super(properties);
    }
    public InteractionResultHolder<ItemStack> use(Level p_40920_, Player p_40921_, InteractionHand p_40922_) {
        ItemStack itemstack = p_40921_.getItemInHand(p_40922_);
        if (isCharged(itemstack)) {
            performShooting(p_40920_, p_40921_, p_40922_, itemstack, 3.14F, 1.0F);
            setCharged(itemstack, false);
            return InteractionResultHolder.consume(itemstack);
        } else if (!p_40921_.getProjectile(itemstack).isEmpty()) {
            if (!isCharged(itemstack)) {
               // this.startSoundPlayed = false;
                //this.midLoadSoundPlayed = false;
                p_40921_.startUsingItem(p_40922_);
            }

            return InteractionResultHolder.consume(itemstack);
        } else {
            return InteractionResultHolder.fail(itemstack);
        }
    }

    public static void shootProjectile(Level p_40895_, LivingEntity p_40896_, InteractionHand p_40897_, ItemStack p_40898_, ItemStack p_40899_, float p_40900_, boolean p_40901_, float p_40902_, float p_40903_, float p_40904_) {
        if (!p_40895_.isClientSide) {
            boolean flag = p_40899_.is(Items.FIREWORK_ROCKET);
            Projectile projectile;

                projectile = new FireworkRocketEntity(p_40895_, p_40899_, p_40896_, p_40896_.getX(), p_40896_.getEyeY() - (double)0.15F, p_40896_.getZ(), true);


            if (p_40896_ instanceof CrossbowAttackMob) {
                CrossbowAttackMob crossbowattackmob = (CrossbowAttackMob)p_40896_;
                crossbowattackmob.shootCrossbowProjectile(crossbowattackmob.getTarget(), p_40898_, projectile, p_40904_);
            } else {
                Vec3 vec31 = p_40896_.getUpVector(1.0F);
                Quaternion quaternion = new Quaternion(new Vector3f(vec31), p_40904_, true);
                Vec3 vec3 = p_40896_.getViewVector(1.0F);
                Vector3f vector3f = new Vector3f(vec3);
                vector3f.transform(quaternion);
                projectile.shoot((double)vector3f.x(), (double)vector3f.y(), (double)vector3f.z(), p_40902_, p_40903_);
            }

            p_40898_.hurtAndBreak(flag ? 3 : 1, p_40896_, (p_40858_) -> {
                p_40858_.broadcastBreakEvent(p_40897_);
            });
            p_40895_.addFreshEntity(projectile);
            p_40895_.playSound((Player)null, p_40896_.getX(), p_40896_.getY(), p_40896_.getZ(), SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, p_40900_);
        }
    }
    public static void performShooting(Level p_40888_, LivingEntity p_40889_, InteractionHand p_40890_, ItemStack p_40891_, float p_40892_, float p_40893_) {
        if (p_40889_ instanceof Player player) {
            if (ForgeEventFactory.onArrowLoose(p_40891_, p_40889_.level, player, 1, true) < 0) {
                return;
            }
        }

        List<ItemStack> list = getChargedProjectiles(p_40891_);
        float[] afloat = getShotPitches(p_40889_.getRandom());

        for(int i = 0; i < list.size(); ++i) {
            ItemStack itemstack = (ItemStack)list.get(i);
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

}
