package com.hungteen.pvz.client.gui.screens;

import com.hungteen.pvz.common.menu.SunCollectorMenu;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

/**
 * @author PangTeen
 * @program HTPVZ
 * @create 2026/10/24 16:00
 **/
@OnlyIn(Dist.CLIENT)
public class SunCollectorScreen extends AbstractContainerScreen<SunCollectorMenu> {

    private static final ResourceLocation TEXTURE = new ResourceLocation("textures/gui/container/dispenser.png");

    public SunCollectorScreen(SunCollectorMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks) {
        super.render(stack, mouseX, mouseY, partialTicks);
        this.renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        this.blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
    }

    @Override
    protected void renderLabels(PoseStack stack, int mouseX, int mouseY) {
        this.font.draw(stack, this.title, this.titleLabelX, this.titleLabelY, 0x404040);
        this.font.draw(stack, Component.translatable("container.pvz.sun_collector.sunlight",
                this.menu.getSunlight(), this.menu.getMaxSunlight()), 8, 73, 0x404040);
    }
}
