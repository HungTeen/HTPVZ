package com.hungteen.pvz.client.gui.components;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.common.register.PVZSeedPackets;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Matrix4f;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiComponent;
import net.minecraft.client.gui.screens.inventory.tooltip.ClientTooltipComponent;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.locale.Language;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterClientTooltipComponentFactoriesEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.awt.*;

@OnlyIn(Dist.CLIENT)
public class ClientSunImageToolTipComponent implements ClientTooltipComponent {
    int cost;
    int cd;
    boolean renderAsNumber;
    String sunText;
    String cdText;
    private static final ResourceLocation ICON_TEXTURE = Util.prefix("textures/gui/overlay/icons.png");
    public ClientSunImageToolTipComponent(SunImageToolTipComponent component) {
        cost = component.cost;
        renderAsNumber = PVZConfig.renderSunAsNumber() || ! component.isCostSun;
        sunText = (component.isAddition && cost == 0) ? "" : (Language.getInstance().getOrDefault("tooltip.pvz.cost") +
                (component.isAddition ? (cost >= 0 ? ": +" : ": ") : " ") +
                (renderAsNumber ? "" + cost : (cost >= 0 ? "" : "-")));
        cd = component.cd;
        if (component.hasCd && ! (component.isAddition && cd == 0)) {
            cdText = Language.getInstance().getOrDefault("tooltip.pvz.cool_down") +
                    " " + (component.isAddition ? (cd > 0 ? "↑" : "↓") : getCdText(cd));
        } else {
            cdText = "";
        }
    }
    @Override
    public int getHeight() {
        return (sunText.equals("") && cdText.equals("")) ? 0 : 10;
    }

    @Override
    public int getWidth(Font font) {
        return font.width(sunText) + (int) (renderAsNumber ? 0 : (cost > 500 ? 5 : 8) * Math.ceil(Math.abs((float) cost / 100))) + font.width(cdText) + 10;
    }

    @Override
    public void renderText(Font font, int x, int y, Matrix4f matrix4f, MultiBufferSource.BufferSource buffer) {
        font.drawInBatch(sunText, x, y, Color.GRAY.getRGB(), true, matrix4f, buffer, false, 0, 15728880, true);
        font.drawInBatch(sunText, x, y, Color.GRAY.getRGB(), true, matrix4f, buffer, false, 0, 15728880, false);
        font.drawInBatch(cdText, x + getWidth(font) - font.width(cdText), y, Color.GRAY.getRGB(), true, matrix4f, buffer, false, 0, 15728880, true);
        font.drawInBatch(cdText, x + getWidth(font) - font.width(cdText), y, Color.GRAY.getRGB(), true, matrix4f, buffer, false, 0, 15728880, false);
    }

    @Override
    public void renderImage(Font font, int x, int y, PoseStack stack, ItemRenderer renderer, int p_194053_) {
        if (! renderAsNumber) {
            RenderSystem.setShaderTexture(0, ICON_TEXTURE);
            x = x + font.width(sunText);
            y = y - 1;
            cost = cost < 0 ? - cost : cost;
            int xoffset = cost > 500 ? 5 : 9;
            while (cost > 0) {
                if (cost >= 100) {
                    blit(stack, x, y, 40, 0, 9, 9);
                    cost -= 100;
                } else if (cost >= 75) {
                    blit(stack, x, y, 30, 0, 9, 9);
                    break;
                } else if (cost >= 50) {
                    blit(stack, x, y, 20, 0, 9, 9);
                    break;
                } else if (cost >= 25) {
                    blit(stack, x, y, 10, 0, 9, 9);
                    break;
                } else {
                    blit(stack, x, y, 0, 0, 9, 9);
                    break;
                }
                x += xoffset;
            }
        }
    }

    @SubscribeEvent
    public static void register(RegisterClientTooltipComponentFactoriesEvent ev){
        ev.register(SunImageToolTipComponent.class, ClientSunImageToolTipComponent::new);
    }

    private void blit(PoseStack p_194036_, int x, int y, int u, int v, int w, int h) {
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        GuiComponent.blit(p_194036_, x, y, 0, u, v, w, h, 256, 256);
    }

    public String getCdText(int cd) {
        String key;
        if (cd <= PVZSeedPackets.FAST) {
            key = "tooltip.pvz.fast";
        } else if (cd <= PVZSeedPackets.MEDIUM) {
            key = "tooltip.pvz.middle";
        } else if (cd <= PVZSeedPackets.SLOW) {
            key = "tooltip.pvz.slow";
        } else {
            key = "tooltip.pvz.very_slow";
        }
        return Language.getInstance().getOrDefault(key);
    }
}
