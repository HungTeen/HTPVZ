package com.hungteen.pvz.client.gui;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.common.network.ClientProxy;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.datafixers.util.Pair;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.IItemDecorator;

public class PVZItemDecorator implements IItemDecorator {
    public static PVZItemDecorator INSTANCE = new PVZItemDecorator();
    @Override
    public boolean render(Font font, ItemStack itemStack, int xOffset, int yOffset, float blitOffset) {
        Player player = ClientProxy.getPlayer();
        if (player != null && ! (ClientProxy.MC.screen instanceof AbstractContainerScreen<?>)) {
            Item item = itemStack.getItem();
            PoseStack posestack = new PoseStack();
            posestack.translate(xOffset, yOffset, blitOffset + 201.0F);
            //render cooldown
            if (PVZConfig.renderCoolDownValue()) {
                if (player.getCooldowns().isOnCooldown(item)) {
                    String count = ((int) ((float) player.getCooldowns().cooldowns.get(item).endTime - player.getCooldowns().tickCount) / 2) + "";
                    count = (count.length() == 1 ? "0" : count.substring(0, count.length() - 1) + "") + (count.length() > 2 ? "" : "." + count.charAt(count.length() - 1));
                    int w = ClientProxy.MC.font.width(count);
                    if (! count.equals("0.0")) {
                        ClientProxy.MC.font.draw(posestack, count, 9 - (float) w / 2, 10, 0x444444);
                        ClientProxy.MC.font.draw(posestack, count, 7 - (float) w / 2, 10, 0x444444);
                        ClientProxy.MC.font.draw(posestack, count, 8 - (float) w / 2, 11, 0x444444);
                        ClientProxy.MC.font.draw(posestack, count, 8 - (float) w / 2, 9, 0x444444);
                        ClientProxy.MC.font.draw(posestack, count, 8 - (float) w / 2, 10, 0xFFFFFF);
                    }
                }
            }
            //render cost
            if (player.getItemBySlot(EquipmentSlot.MAINHAND) == itemStack || player.getItemBySlot(EquipmentSlot.OFFHAND) == itemStack) {
                PVZOverlayHandler.itemsToDrawCost.put(itemStack.copy(), Pair.of(xOffset, yOffset));
            }
        }
        return false;
    }
}
