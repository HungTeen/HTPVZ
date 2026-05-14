package com.hungteen.pvz.common.item;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.common.register.PVZItems;
import com.hungteen.pvz.util.EntityUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.item.ItemProperties;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.AreaEffectCloud;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.UseAnim;
import net.minecraft.world.level.Explosion;
import net.minecraft.world.level.Level;
import net.minecraftforge.fml.DistExecutor;

import java.util.Collection;

public class JackInTheBoxItem extends Item {
    public final boolean destructive;
    public JackInTheBoxItem(Properties p_41383_, boolean destructive) {
        super(p_41383_);
        this.destructive = destructive;
    }
    public void releaseUsing(ItemStack itemStack, Level level, LivingEntity livingEntity, int duration) {
        if (this.getUseDuration(itemStack) - duration > 50) {
            explode(level, livingEntity, itemStack);
        }
    }
    public void explode(Level level, Entity user, ItemStack itemStack) {
        CompoundTag tag = itemStack.getOrCreateTag();
        int strength;
        if (tag.contains("strength")) {
            strength = tag.getInt("strength");
        } else {
            strength = 2;
        }
        if (EntityUtil.isEntityValid(user) && ! level.isClientSide) {
            Explosion.BlockInteraction explosion$blockinteraction = destructive &&
                    net.minecraftforge.event.ForgeEventFactory.getMobGriefingEvent(level, user) && PVZConfig.PVZGameRules.getBoolean(level, PVZConfig.Common.jackInTheBoxGriefing) ?
                            Explosion.BlockInteraction.DESTROY : Explosion.BlockInteraction.NONE;
            level.explode(null, user.getX(), user.getY(), user.getZ(), strength, explosion$blockinteraction);
            if (! user.isAlive()) {
                spawnLingeringCloud(user);
            }
            if (! (user instanceof Player player) || ! player.getAbilities().instabuild) {
                itemStack.shrink(1);
            }
        }
        if (! EntityUtil.isEntityValid(user) && ! (user instanceof Player)) {
            user.discard();
        }
    }


    private void spawnLingeringCloud(Entity user) {
        if (user instanceof LivingEntity living) {
            Collection<MobEffectInstance> collection = living.getActiveEffects();
            if (!collection.isEmpty()) {
                AreaEffectCloud areaeffectcloud = new AreaEffectCloud(user.level, user.getX(), user.getY(), user.getZ());
                areaeffectcloud.setRadius(2.5F);
                areaeffectcloud.setRadiusOnUse(-0.5F);
                areaeffectcloud.setWaitTime(10);
                areaeffectcloud.setDuration(areaeffectcloud.getDuration() / 2);
                areaeffectcloud.setRadiusPerTick(-areaeffectcloud.getRadius() / (float)areaeffectcloud.getDuration());

                for(MobEffectInstance mobeffectinstance : collection) {
                    areaeffectcloud.addEffect(new MobEffectInstance(mobeffectinstance));
                }

                user.level.addFreshEntity(areaeffectcloud);
            }
        }
    }

    public UseAnim getUseAnimation(ItemStack itemStack) {
        return DistExecutor.unsafeRunForDist(() -> () -> Minecraft.getInstance().options.getCameraType().isFirstPerson() ? UseAnim.NONE : UseAnim.BOW
                , () -> () -> UseAnim.BOW);
    }
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        player.startUsingItem(hand);
        return super.use(level, player, hand);
    }
    public int getUseDuration(ItemStack p_40680_) {
        return 72000;
    }
    public static void registerProperties() {
        ItemProperties.register(PVZItems.JACK_IN_THE_BOX.get(), new ResourceLocation("open"),
                (itemStack, level, entity, seed) -> entity instanceof LivingEntity && entity.isUsingItem() && entity.getUseItem() == itemStack ? 1 : 0);
        ItemProperties.register(PVZItems.JACK_IN_THE_BOX.get(), new ResourceLocation("rollup"),
                (itemStack, level, entity, seed) -> entity instanceof LivingEntity && entity.isUsingItem() && entity.getUseItem() == itemStack ? (float) Math.sin((double) itemStack.getUseDuration() - entity.getUseItemRemainingTicks()) > 0 ? 1 : 0 : 0);
        ItemProperties.register(PVZItems.JACK_IN_THE_BOX.get(), new ResourceLocation("shining"),
                (itemStack, level, entity, seed) -> entity instanceof LivingEntity && entity.isUsingItem() && entity.getUseItem() == itemStack ? (Math.sin(Math.pow((float) (itemStack.getUseDuration() - entity.getUseItemRemainingTicks()), 1.4) / 4) < 0.05 || (itemStack.getUseDuration() - entity.getUseItemRemainingTicks()) > 50 ? 1 : 0) : 0);
    }
}
