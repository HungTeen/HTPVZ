package com.hungteen.pvz.common.item;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.api.interfaces.IHaveSkills;
import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.api.interfaces.IPlantShovelable;
import com.hungteen.pvz.common.entity.creatures.Sprout;
import com.hungteen.pvz.common.entity.plants.MariGold;
import com.hungteen.pvz.common.register.PVZItems;
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
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Map;

public class SeedDispensaryItem extends Item implements IPlantShovelable {
    public SeedDispensaryItem(Properties p_41383_) {
        super(p_41383_);
    }

    @Override
    public void onPlantShoveled(ItemStack seedDispensary, Player player, LivingEntity target, InteractionHand hand) {
        boolean gameRule = PVZConfig.PVZGameRules.getBoolean(player.level, PVZConfig.Common.seedDispensaryGiveSprout);
        ItemStack itemStack = gameRule ? target instanceof MariGold ? PVZItems.MARIGOLD_SPROUT.get().getDefaultInstance()
                        : SproutItem.getTaggedItem(PVZItems.SPROUT.get().getDefaultInstance()
                        , target.getType().getDescriptionId()
                        , Map.of(ForgeRegistries.ENTITY_TYPES.getKey(target.getType()).toString(), 5))
                : target.getPickResult();
        if (! itemStack.isEmpty()) {
            if (itemStack.getItem() instanceof SeedPacketItem<?>) {
                if (target instanceof IHaveSkills iHaveSkills) {
                    iHaveSkills.saveSkills(itemStack.getOrCreateTag());
                }
            }
            itemStack.getOrCreateTag().putBoolean("ToolGenerated", true);
            if (! player.getInventory().add(itemStack)) {
                var itementity = player.drop(itemStack, false);
                if (itementity != null) {
                    itementity.setNoPickUpDelay();
                    itementity.setOwner(player.getUUID());
                }
            }
            seedDispensary.shrink(1);
        }
    }

    @Override
    public boolean canShovel(LivingEntity target, ItemStack itemStack) {
        return target instanceof IPlant && ! (target instanceof Sprout);
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void appendHoverText(ItemStack stack, Level level, List<Component> tooltip, TooltipFlag flagIn) {
        tooltip.add(Component.translatable("tooltip.pvz.seed_dispensary").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)));
        super.appendHoverText(stack, level, tooltip, flagIn);
    }
}
