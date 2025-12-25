package com.hungteen.pvz.common.item;

import com.hungteen.pvz.api.interfaces.IHaveSkills;
import com.hungteen.pvz.api.interfaces.IPlantShovelable;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.List;

public class SeedDispensaryItem extends Item implements IPlantShovelable {
    public SeedDispensaryItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public void onPlantShoveled(ItemStack seedDispensary, Player player, LivingEntity target, InteractionHand hand) {
        ItemStack itemStack = target.getPickResult();
        if (itemStack != null && !itemStack.isEmpty()) {
            if (itemStack.getItem() instanceof SeedPacketItem<?>) {
                if (target instanceof IHaveSkills iHaveSkills) {
                    iHaveSkills.saveSkills(itemStack.getOrCreateTag());
                }
            }
            itemStack.getOrCreateTag().putBoolean("ToolGenerated", true);
            player.getInventory().add(itemStack);
            seedDispensary.shrink(1);
        }
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("tooltip.pvz.seed_dispensary").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        super.appendHoverText(stack, level, tooltip, flagIn);
    }
}
