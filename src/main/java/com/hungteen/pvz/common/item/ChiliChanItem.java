package com.hungteen.pvz.common.item;

import com.hungteen.pvz.common.register.PVZItems;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.ItemModelShaper;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.ResourceManagerReloadListener;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShovelItem;
import net.minecraft.world.item.Tier;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.ForgeItemModelShaper;

import java.util.Calendar;
import java.util.List;

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
            Vec3 vec3 = user.getViewVector(0);
            AttributeInstance attribute = target.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            double knockBackModifier = 1;
            if (attribute != null) {
                knockBackModifier = 1 - attribute.getValue() * 0.5;
            }
            target.setDeltaMovement(target.getDeltaMovement().add(vec3.normalize().scale(Math.min((0.5 + 2 / vec3.distanceTo(Vec3.ZERO)) * knockBackModifier, 1))));
            ((ServerLevel) user.level).sendParticles(ParticleTypes.EXPLOSION,
                    target.getX(), target.getY(), target.getZ(), 5, 0.5, 0.5D, 0.5D, 0.0D);
        }
        return super.hurtEnemy(itemStack, target, user);
    }
    @Override
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("tooltip.pvz.chili_chan").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        super.appendHoverText(stack, level, tooltip, flagIn);
    }

    public static class EasterEggListener implements ResourceManagerReloadListener {
        @Override
        @OnlyIn(Dist.CLIENT)
        public void onResourceManagerReload(ResourceManager p_10758_) {
            Calendar calendar = Calendar.getInstance();
            if (calendar.get(Calendar.MONTH) + 1 == 4 && calendar.get(Calendar.DATE) <= 2) {
                ItemModelShaper shaper = Minecraft.getInstance().getItemRenderer().getItemModelShaper();
                if (shaper instanceof ForgeItemModelShaper forgeItemModelShaper) {
                    forgeItemModelShaper.register(PVZItems.CHILI_CHAN.get(), new ModelResourceLocation("pvz:hot_sauce#inventory"));
                }
            }
        }
    }
}
