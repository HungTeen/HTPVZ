package com.hungteen.pvz.client;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.renderer.PVZEntityRenderHandler;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = PVZMod.MODID, value = Dist.CLIENT)
public class PVZClientEventHandler {
    @SubscribeEvent
    public static void onLivingRender(@SuppressWarnings("rawtypes") RenderLivingEvent.Pre ev) {
        final PoseStack stack = ev.getPoseStack();
        final MultiBufferSource buffer = ev.getMultiBufferSource();
        final int light = ev.getPackedLight();

        PVZEntityRenderHandler.checkAndRenderButter(ev.getRenderer(), ev.getEntity(), stack, buffer, light);
    }
}
