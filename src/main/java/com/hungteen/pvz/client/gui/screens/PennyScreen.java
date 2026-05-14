package com.hungteen.pvz.client.gui.screens;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.common.entity.npcs.Penny;
import com.hungteen.pvz.common.menu.PennyMenu;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.MerchantScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.contents.TranslatableContents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.SimpleContainer;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.MerchantMenu;
import net.minecraft.world.item.trading.MerchantOffer;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class PennyScreen extends AbstractContainerScreen<PennyMenu> {
    public final @Nullable PennifiedMerchantScreen vanillaScreen;
    private static final ResourceLocation LOCATION = Util.prefix("textures/gui/container/penny.png");
    public PennyScreen(PennyMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.imageWidth = 276;
        this.inventoryLabelX = 107;
        if (PVZConfig.Client.renderPVZTypePennyGUI.get() && ! menu.isVanillaUI) {
            this.vanillaScreen = null;
        } else {
            this.vanillaScreen = new PennifiedMerchantScreen(this.getMenu().vanillaMenu, inventory, component, this);
        }
    }
    @Override
    protected void renderBg(@NotNull PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        if (this.vanillaScreen == null) {
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
            RenderSystem.setShaderTexture(0, LOCATION);
            int x = (this.width - this.imageWidth) / 2;
            int y = (this.height - this.imageHeight) / 2;
            blit(poseStack, x, y, this.getBlitOffset(), 0.0F, 0.0F, this.imageWidth, this.imageHeight, 512, 256);
            for (int i = 0; i < 4; i ++) {
                for (int j = 0; j < 2; j ++) {
                    int index = j * 4 + i + 8 * menu.merchantContainer.currentPage;
                    if (menu.getOffers().size() <= index) continue;
                    MerchantOffer offer = menu.getOffers().get(index);
                    if (offer.isOutOfStock()) {
                        blit(poseStack, x + i * 36 + j * 4 + 120, y + j * 26 + 33, this.getBlitOffset()
                                , 35, 166, 9, 9, 512, 256);
                    } else {
                        blit(poseStack, x + i * 36 + j * 4 + 124, y + j * 26 + 36, this.getBlitOffset()
                                , 30, 166, 5, 5, 512, 256);
                    }
                }
            }
            if (menu.getOffers().size() > 8) {
                boolean isOnPrevButton = mouseX - x - 100 > 0 && mouseX - x - 100 < 10 && mouseY - y - 45 > 0 && mouseY - y - 45 < 15;
                blit(poseStack, x + 100, y + 45, this.getBlitOffset()
                        , menu.merchantContainer.currentPage <= 0 ? 20 : isOnPrevButton ? 10 : 0, 181
                        , 10, 15, 512, 256);
                boolean isOnNextButton = mouseX - x - 243 > 0 && mouseX - x - 243 < 10 && mouseY - y - 45 > 0 && mouseY - y - 45 < 15;
                blit(poseStack, x + 243, y + 45, this.getBlitOffset()
                        , menu.merchantContainer.currentPage >= (menu.getOffers().size() - 1) / 8 ? 20 : isOnNextButton ? 10 : 0, 166
                        , 10, 15, 512, 256);
            }
            if (hoveredSlot != null && hoveredSlot.index >= 39
                    && hoveredSlot.index - 39 + 8 * menu.merchantContainer.currentPage < menu.getOffers().size()) {
                MerchantOffer offer = menu.getOffers().get(hoveredSlot.index - 39 + 8 * menu.merchantContainer.currentPage);
                blit(poseStack, x + 8, y + 10, this.getBlitOffset()
                        , offer.getCostB().isEmpty() ? 367 : 276, 0.0F
                        , 91, 145, 512, 256);
                if (offer.isOutOfStock()) {
                    blit(poseStack
                            , x + (offer.getCostB().isEmpty() ? 50 : 59), y + 21, this.getBlitOffset()
                            , 35, 166, 9, 9, 512, 256);
                }
                this.itemRenderer.renderAndDecorateFakeItem(offer.getCostA(), x + 19, y + 18);
                this.itemRenderer.renderGuiItemDecorations(this.font, offer.getCostA(), x + 19, y + 18);
                this.itemRenderer.renderAndDecorateFakeItem(offer.getResult(), x + 73, y + 18);
                this.itemRenderer.renderGuiItemDecorations(this.font, offer.getResult(), x + 73, y + 18);
                if (! offer.getCostB().isEmpty()) {
                    this.itemRenderer.renderAndDecorateFakeItem(offer.getCostB(), x + 37, y + 18);
                    this.itemRenderer.renderGuiItemDecorations(this.font, offer.getCostB(), x + 37, y + 18);
                }
            }
        }
    }

    @Override
    protected void renderLabels(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        if (this.vanillaScreen == null) {
            this.font.draw(poseStack, this.playerInventoryTitle, (float)this.inventoryLabelX, (float)this.inventoryLabelY, 0x404040);
            for (int i = 0; i < 4; i ++) {
                for (int j = 0; j < 2; j ++) {
                    int index = j * 4 + i + 8 * menu.merchantContainer.currentPage;
                    if (menu.getOffers().size() <= index) continue;
                    MerchantOffer offer = menu.getOffers().get(index);
                    if (offer.isOutOfStock()) continue;
                    String count = "" + offer.getCostA().getCount();
                    boolean costMatch = true;
                    if (ClientProxy.getPlayer() != null) {
                        costMatch = ClientProxy.getPlayer().getInventory().clearOrCountMatchingItems(k -> k.getItem() == offer.getCostA().getItem(), 0, new SimpleContainer(0)) >= offer.getCostA().getCount();
                    }
                    int width = this.font.width(count);
                    this.font.draw(poseStack, Component.literal(count)
                            , 117 - width / 2 + i * 36 + j * 4, 35 + j * 26, 0x704040);
                    this.font.draw(poseStack, Component.literal(count)
                            , 116 - width / 2 + i * 36 + j * 4, 34 + j * 26, costMatch ? 0xffffff : 0xff0000);
                }
            }
            if (hoveredSlot != null && hoveredSlot.index >= 39
                    && hoveredSlot.index - 39 + 8 * menu.merchantContainer.currentPage < menu.getOffers().size()) {
                MerchantOffer offer = menu.getOffers().get(hoveredSlot.index - 39 + 8 * menu.merchantContainer.currentPage);
                Component desc = Component.translatable(offer.getResult().getDescriptionId() + ".penny_desc");
                if (desc.getContents() instanceof TranslatableContents c && desc.getString().equals(c.getKey())) {
                    desc = Component.translatable("hint.pvz.penny_default");
                }
                List<FormattedCharSequence> components = font.split(desc, 87);
                int j = 0;
                for (FormattedCharSequence i : components) {
                    this.font.draw(poseStack, i, 11, 50 + j * 9, 0x404040);
                    j ++;
                }
            }
        }
    }

    @Override
    public boolean mouseScrolled(double p_99127_, double p_99128_, double p_99129_) {
        if (this.vanillaScreen != null) {
            return vanillaScreen.mouseScrolled(p_99127_, p_99128_, p_99129_);
        }
        return super.mouseScrolled(p_99127_, p_99128_, p_99129_);
    }

    @Override
    public boolean mouseDragged(double p_99135_, double p_99136_, int p_99137_, double p_99138_, double p_99139_) {
        if (this.vanillaScreen != null) {
            return vanillaScreen.mouseDragged(p_99135_, p_99136_, p_99137_, p_99138_, p_99139_);
        }
        return super.mouseDragged(p_99135_, p_99136_, p_99137_, p_99138_, p_99139_);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.vanillaScreen != null) {
            return vanillaScreen.mouseClicked(mouseX, mouseY, button);
        }
        int x = (this.width - this.imageWidth) / 2;
        int y = (this.height - this.imageHeight) / 2;
        if (mouseX - x - 100 > 0 && mouseX - x - 100 < 10 && mouseY - y - 45 > 0 && mouseY - y - 45 < 15) {
            if (menu.merchantContainer.currentPage > 0) {
                menu.merchantContainer.currentPage --;
                menu.merchantContainer.updateSellItem();
                sendSignal(- menu.merchantContainer.currentPage - 1);
            }
        } else if (mouseX - x - 243 > 0 && mouseX - x - 243 < 10 && mouseY - y - 45 > 0 && mouseY - y - 45 < 15) {
            if (menu.merchantContainer.currentPage < (menu.getOffers().size() - 1) / 8) {
                menu.merchantContainer.currentPage ++;
                menu.merchantContainer.updateSellItem();
                sendSignal(- menu.merchantContainer.currentPage - 1);
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double p_99131_, double p_99132_, int p_99133_) {
        if (this.vanillaScreen != null) {
            return vanillaScreen.mouseReleased(p_99131_, p_99132_, p_99133_);
        }
        return super.mouseReleased(p_99131_, p_99132_, p_99133_);
    }

    @Override
    public void render(PoseStack poseStack, int mouseX, int mouseY, float p_99151_) {
        if (vanillaScreen != null) {
            if (vanillaScreen.getMinecraft() == null) {
                vanillaScreen.init(this.minecraft, this.width, this.height);
            }
            vanillaScreen.render(poseStack, mouseX, mouseY, p_99151_);
        } else {
            this.renderBackground(poseStack);
            super.render(poseStack, mouseX, mouseY, p_99151_);
            this.renderTooltip(poseStack, mouseX, mouseY);
        }
    }

    public void sendSignal(int val) {
        Player player = ClientProxy.getPlayer();
        if (player != null) {
            Penny penny = player.level.getNearestEntity(Penny.class, TargetingConditions.DEFAULT
                    , player, player.getX(), player.getY(), player.getZ()
                    , player.getBoundingBox().inflate(10));
            if (penny != null) {
                penny.sendPVZPacketToServer(val);
            }
        }
    }

    public static class PennifiedMerchantScreen extends MerchantScreen {
        final PennyScreen mainScreen;

        public PennifiedMerchantScreen(MerchantMenu p_99123_, Inventory p_99124_, Component p_99125_, PennyScreen mainScreen) {
            super(p_99123_, p_99124_, p_99125_);
            this.mainScreen = mainScreen;
        }

        @Override
        protected void init() {
            super.init();
            int i = (this.width - this.imageWidth) / 2;
            int j = (this.height - this.imageHeight) / 2;
            int k = j + 16 + 2;

            for(int l = 0; l < 7; ++l) {
                this.tradeOfferButtons[l] = this.addRenderableWidget(new TradeOfferButton(i + 5, k, l, (p_99174_) -> {
                    if (p_99174_ instanceof TradeOfferButton) {
                        this.shopItem = ((TradeOfferButton)p_99174_).getIndex() + this.scrollOff;
                        this.postButtonClick();
                    }
                }));
                k += 20;
            }

        }

        private void postButtonClick() {
            this.mainScreen.menu.vanillaMenu.setSelectionHint(this.mainScreen.vanillaScreen.shopItem);
            this.mainScreen.menu.vanillaMenu.tryMoveItems(this.mainScreen.vanillaScreen.shopItem);
            this.mainScreen.sendSignal(this.shopItem);
        }
    }
}
