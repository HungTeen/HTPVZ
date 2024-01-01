package com.hungteen.pvz.client.gui.screens;

import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.client.model.FloatEssenceBlockModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.client.renderer.blockentity.EssenceAltarRenderer;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.menu.EssenceAltarMenu;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.network.PVZAddSkillPacket;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Matrix4f;
import com.mojang.math.Vector3f;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import java.util.Arrays;
import java.util.List;

import static java.lang.Math.floor;
import static java.lang.Math.min;

@OnlyIn(Dist.CLIENT)
public class EssenceAltarScreen extends AbstractContainerScreen<EssenceAltarMenu> {
    private static final ResourceLocation TEXTURE = Util.prefix("textures/gui/container/essence_altar.png");
    private static final ResourceLocation BLOCK_TEXTURE = Util.prefix("textures/blockentity/float_essence_block.png");
    private FloatEssenceBlockModel model;
    private int shownFirstSkill;
    private List<Skill> skills = List.of();
    private boolean isDragging = false;


    public EssenceAltarScreen(EssenceAltarMenu p_97741_, Inventory p_97742_, Component p_97743_) {
        super(p_97741_, p_97742_, p_97743_);
    }
    @Override
    public void init() {
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        shownFirstSkill = 0;
        model = new FloatEssenceBlockModel(ClientProxy.MC.getEntityModels().bakeLayer(PVZLayerHandler.LayerLocationMap.get("floating_essence_block:main")));
    }
    @Override
    public void containerTick() {
        this.skills = List.of();
        if (this.getMenu().slots.get(0).hasItem() && this.getMenu().slots.get(0).getItem().getItem() instanceof SeedPacketItem<?> item) {
            if (item.getStaticSkillList().size() > 0) {
                this.skills = item.getStaticSkillList();
            }
        }
        shownFirstSkill = min(shownFirstSkill, skills.size());
    }

    @Override
    protected void renderBg(PoseStack stack, float partialTicks, int mouseX, int mouseY) {
        this.renderBackground(stack);
        Lighting.setupForFlatItems();
        RenderSystem.setShaderTexture(0, TEXTURE);
        stack.pushPose();
        blit(stack, this.leftPos, this.topPos, 0, 0, this.imageWidth, this.imageHeight);
        if (! this.getMenu().slots.get(0).hasItem()) {
            blit(stack, this.leftPos + 25, this.topPos + 19, 177, 36, 16, 16);
        }
        if (! this.getMenu().slots.get(1).hasItem()) {
            blit(stack, this.leftPos + 16, this.topPos + 54, 178, 16, 16, 16);
        }
        if (! this.getMenu().slots.get(2).hasItem()) {
            blit(stack, this.leftPos + 35, this.topPos + 54, 197, 16, 16, 16);
        }
        int scrollTop = this.topPos + 14;
        int scrollBottom = scrollTop + 57 - 15;
        int allowedMax = skills.size() - 3;
        float current = shownFirstSkill / (float) allowedMax * (scrollBottom - scrollTop);
        this.blit(stack, leftPos + 156, scrollTop + (int) current, 191 + (skills.size() > 3 ? 0 : 12), 0, 12, 15);
        //render block.
        int scale = (int)ClientProxy.MC.getWindow().getGuiScale();
        RenderSystem.viewport((this.width - 320) / 2 * scale, (this.height - 240) / 2 * scale, 320 * scale, 240 * scale);
        Matrix4f matrix4f = Matrix4f.createTranslateMatrix(-0.34F, 0.23F, 0.0F);
        matrix4f.multiply(Matrix4f.perspective(90.0D, 1.3333334F, 9.0F, 80.0F));
        RenderSystem.backupProjectionMatrix();
        RenderSystem.setProjectionMatrix(matrix4f);
        PoseStack.Pose posestack$pose = stack.last();
        posestack$pose.pose().setIdentity();
        posestack$pose.normal().setIdentity();
        stack.translate(0F, 0F, 1985.0D);
        stack.scale(5.0F, 5.0F, 5.0F);
        stack.translate(-0.01F, 1.75F, -1F);
        stack.mulPose(Vector3f.YP.rotationDegrees(-90F));
        stack.mulPose(Vector3f.XP.rotationDegrees(180.0F));
        this.model.setupAnim(EssenceAltarRenderer.time * (this.getMenu().slots.get(0).hasItem() ? 1 : 0));
        MultiBufferSource.BufferSource multibuffersource$buffersource = MultiBufferSource.immediate(Tesselator.getInstance().getBuilder());
        VertexConsumer vertexconsumer = multibuffersource$buffersource.getBuffer(this.model.renderType(BLOCK_TEXTURE));
        this.model.renderToBuffer(stack, vertexconsumer, 0xf000f0, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        multibuffersource$buffersource.endBatch();
        stack.popPose();
        RenderSystem.viewport(0, 0, ClientProxy.MC.getWindow().getWidth(), ClientProxy.MC.getWindow().getHeight());
        RenderSystem.restoreProjectionMatrix();
        Lighting.setupFor3DItems();
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        //render buttons.
        if (this.getMenu().slots.get(0).hasItem() && this.getMenu().slots.get(0).getItem().getItem() instanceof SeedPacketItem<?> item) {
            if (skills.size() > 0) {
                int x = leftPos + 60;
                int y = topPos;
                for(int i = 0; i < 3 && i + shownFirstSkill < skills.size(); ++ i) {
                    this.setBlitOffset(0);
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderTexture(0, TEXTURE);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    int costSeedPacket = skills.get(i + shownFirstSkill).costSeed;
                    int costItem = skills.get(i + shownFirstSkill).costItem;
                    Component skillName = Component.translatable(skills.get(i + shownFirstSkill).name);
                    int color;
                    if (getMenu().isSkillAvailable(ClientProxy.getPlayer(), skills, i + shownFirstSkill)) {
                        //available.
                        int mouseRelativeX = mouseX - x;
                        int mouseRelativeY = mouseY - (y + 14 + 19 * i);
                        if (mouseRelativeX >= 0 && mouseRelativeY >= 0 && mouseRelativeX < 92 && mouseRelativeY < 19) {
                            this.blit(stack, x, y + 14 + 19 * i, 0, 204, 92, 19);
                            color = 0xffff80;
                        } else {
                            this.blit(stack, x, y + 14 + 19 * i, 0, 166, 92, 19);
                            color = 0x544c3b;
                        }
                        this.blit(stack, x + 1, y + 15 + 19 * i, 16 * i, 223, 16, 16);
                        this.font.draw(stack, skillName, x + 39, y + 20 + 19 * i, color);
                    } else {
                        //not available.
                        this.blit(stack, x, y + 14 + 19 * i, 0, 185, 92, 19);
                        this.blit(stack, x + 1, y + 15 + 19 * i, 16 * i, 239, 16, 16);
                        color = 0x211d17;
                        this.font.draw(stack, skillName, x + 39, y + 20 + 19 * i, color);
                    }
                    color = 0xffffff;
                    RenderSystem.setShaderTexture(0, TEXTURE);
                    this.blit(stack, x + 2, y + 18 + 19 * i, 94, 168, 11, 10);
                    ItemStack itemStack =  new ItemStack(skills.get(i + shownFirstSkill).item.get());
                    itemStack.setCount(costItem);
                    this.itemRenderer.renderAndDecorateFakeItem(itemStack, x + 17, y + 15 + 19 * i);
                    this.itemRenderer.renderGuiItemDecorations(this.font, itemStack, x + 17, y + 15 + 19 * i);
                    this.font.drawShadow(stack, costSeedPacket + "", x + 16 - this.font.width(costSeedPacket + ""), y + 24 + 19 * i, color);
                }
            }
        }
    }

    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks){
        super.render(stack, mouseX, mouseY, partialTicks);
        int top = this.topPos + 14;
        int bottom = top + 57;
        if (mouseX > leftPos + 60 && mouseX < leftPos + 152 && mouseY > top && mouseY <= bottom) {
            if (skills.size() > (mouseY - top) / 19 + shownFirstSkill) {
                ClientProxy.MC.screen.renderComponentTooltip(stack, Arrays.asList(
                        Component.translatable(skills.get((mouseY - top) / 19 + shownFirstSkill).name),
                        Component.translatable(skills.get((mouseY - top) / 19 + shownFirstSkill).name + ".disc").withStyle(Style.EMPTY.withColor(0x545454))
                ), mouseX, mouseY);
            }
        } else {
            this.renderTooltip(stack, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double p_99127_, double p_99128_, double p_99129_) {
        int skillsNum = skills.size();
        if (skillsNum > 4) {
            int allowedMax = skillsNum - 3;
            this.shownFirstSkill = Mth.clamp((int) ((double) this.shownFirstSkill - p_99129_), 0, allowedMax);
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int p_99137_, double deltaMouseX, double deltaMouseY) {
        int skillNum = this.skills.size();
        if (this.isDragging) {
            int scrollTop = this.topPos + 14;
            int scrollBottom = scrollTop + 57 - 15;
            int allowedMax = skillNum - 3;
            int current = (int)((mouseY - scrollTop) / (scrollBottom - scrollTop) * allowedMax);
            this.shownFirstSkill = Mth.clamp(current, 0, allowedMax);
            return true;
        } else {
            return super.mouseDragged(mouseX, mouseY, p_99137_, deltaMouseX, deltaMouseY);
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int p_99133_) {
        this.isDragging = false;
        int top = this.topPos + 14;
        int bottom = top + 57;
        int allowedMax = skills.size() - 3;
        float current = shownFirstSkill / (float) allowedMax * (bottom - 15 - top);
        if (skills.size() > 4 && mouseX > (double)(leftPos + 156) && mouseX < (double)(leftPos + 168) && mouseY > (double) top + current && mouseY <= (double)(top + current + 15)) {
            this.isDragging = true;
        } else if (mouseX > leftPos + 60 && mouseX < leftPos + 152 && mouseY > top && mouseY <= bottom) {
            if (skills.size() > (mouseY - top) / 19 + shownFirstSkill) {
                PVZAddSkillPacket.addSkill((int) floor((mouseY - top) / 19) + shownFirstSkill);
            }
        }

        return super.mouseClicked(mouseX, mouseY, p_99133_);
    }
}
