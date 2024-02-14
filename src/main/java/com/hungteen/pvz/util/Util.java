package com.hungteen.pvz.util;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.common.network.ClientProxy;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.registries.RegistryObject;

public class Util {

    //object names and resource_location tools
    public static ResourceLocation prefix(String name){
        return new ResourceLocation(PVZMod.MODID, name);
    }
    public static String name(EntityType<? extends Entity> entityType){
        String string = entityType.getDescriptionId();
        while (string.contains(".")) {
            string = string.substring(string.indexOf(".") + 1);
        }
        return string;
    }
    public static String name(Block block){
        String string = block.getDescriptionId();
        while (string.contains(".")) {
            string = string.substring(string.indexOf(".") + 1);
        }
        return string;
    }
    public static String name(Item item){
        String string = item.getDescriptionId();
        while (string.contains(".")) {
            string = string.substring(string.indexOf(".") + 1);
        }
        return string;
    }
    public static String name(RegistryObject obj){
        return obj.getId().getPath();
    }


    //rendering tools
    @OnlyIn(Dist.CLIENT)
    public static void setTexture(ResourceLocation texture){
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, texture);
    }

    public static void drawCenteredScaledString(PoseStack stack, Font font, String string, int x, int y, int color,
                                                float scale) {
        int width = font.width(string);
        stack.pushPose();
        stack.scale(scale, scale, scale);
        font.draw(stack, string, (x - width / 2 * scale) / scale, y / scale, color);
        stack.popPose();
    }

    public static void GuiBiltScaled(PoseStack stack, int drawX, int drawY, int uvx, int uvy, int uvw, int uvh, float scale){
        ClientProxy.MC.gui.blit(stack, (int) (drawX*scale), (int) (drawY*scale), (int) (uvx*scale), (int) (uvy*scale), (int) (uvw*scale), (int) (uvh*scale));
    }
}
