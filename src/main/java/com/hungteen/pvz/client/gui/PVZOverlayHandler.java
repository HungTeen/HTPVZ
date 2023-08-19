package com.hungteen.pvz.client.gui;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.Util;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapability;
import com.hungteen.pvz.common.network.ClientProxy;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderGuiOverlayEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PVZMod.MODID, value = Dist.CLIENT)
@OnlyIn(Dist.CLIENT)
public class PVZOverlayHandler {

    private static int bufferSunAmount = 0;
    private static int bufferSunBarLength = 0;

    @SubscribeEvent
    public static void onPostRenderOverlay(RenderGuiOverlayEvent.Post ev){
        if(!ClientProxy.MC.options.hideGui && ClientProxy.MC.screen == null && ClientProxy.MC.player != null && ! ClientProxy.MC.player.isSpectator()){
            if(!ClientProxy.MC.options.renderDebug && PVZConfig.renderSunAsBar()){
                // render sun as bar on the screen
                PVZOverlayHandler.renderSunAsBar(ev.getPoseStack(), ev.getWindow().getGuiScaledWidth(), ev.getWindow().getGuiScaledHeight());
            } else {
                // render sun at stats bar//TODO should it be put here?
//                PVZOverlayHandler.renderSunAsStats(ev.getPoseStack());
            }
        }
    }

    public static void tick(float tickTime){
        float speed = 0.1F;//count animation with time?
        int now = PVZPlayerCapability.getValue(ClientProxy.getPlayer(), "sun");
        int barLength = 94*bufferSunAmount/PVZPlayerCapability.getValueLimit(ClientProxy.getPlayer(), "sun").getSecond();
        bufferSunAmount = (int)(now*speed + bufferSunAmount*(1-speed));
        bufferSunBarLength = (int)(barLength*speed + bufferSunBarLength*(1-speed));
        if (Math.abs(bufferSunAmount - now) <= 10) {
            bufferSunAmount = now;
        }
        if (Math.abs(bufferSunBarLength - barLength) <= 10) {
            bufferSunBarLength = barLength;
        }
    }

    private static void renderSunAsBar(PoseStack stack, int width, int height){
        stack.pushPose();
        RenderSystem.enableBlend();
        int x = PVZConfig.renderSunBarX();
        int y = PVZConfig.renderSunBarY();
        double scale = PVZConfig.renderSunBarScale();

//        int now = PVZPlayerCapability.getValue(ClientProxy.getPlayer(), "sun");
//        int barLength = 94*now/PVZPlayerCapability.getValueLimit(ClientProxy.getPlayer(), "sun").getSecond();

        stack.scale((float) (0.5F*scale), (float) (0.5F*scale), (float) (0.5F*scale));
        Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));

        int drawX = x >= 0 ? x + 35 : (int) (width/scale) - 134 + x;
        int drawY = y >= 0 ? y : (int) (height/scale) - 15 + y;
        Util.GuiBiltScaled(stack, drawX, drawY, 28, 112, 100, 16, 2);
        Util.GuiBiltScaled(stack, drawX + 3, drawY, 31, 96, bufferSunBarLength, 16, 2);
        Util.GuiBiltScaled(stack, x >= 0 ? x : (int) (width/scale) - 33 + x, y >= 0 ? y : (int) (height/scale) - 33 + y, 94, 62, 34, 34, 2);

        Util.drawCenteredScaledString(stack, ClientProxy.MC.font, bufferSunAmount + "", (x >= 0 ? x + 86 : (int) (width/scale) - 84 + x)*2, (y >= 0 ? y + 5 : (int) (height/scale) - 11 + y)*2, 0x663600, 2f);
        Util.drawCenteredScaledString(stack, ClientProxy.MC.font, bufferSunAmount + "", (x >= 0 ? x + 85 : (int) (width/scale) - 85 + x)*2, (y >= 0 ? y + 4 : (int) (height/scale) - 12 + y)*2, 0xFFFFFF, 2f);

        RenderSystem.disableBlend();
        stack.popPose();
    }

}
