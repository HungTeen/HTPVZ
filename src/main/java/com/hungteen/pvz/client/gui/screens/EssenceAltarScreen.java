package com.hungteen.pvz.client.gui.screens;

import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.client.gui.components.SunImageToolTipComponent;
import com.hungteen.pvz.client.model.FloatEssenceBlockModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapStats;
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
import net.minecraft.ChatFormatting;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;

import static java.lang.Math.floor;
import static java.lang.Math.min;

@OnlyIn(Dist.CLIENT)
public class EssenceAltarScreen extends AbstractContainerScreen<EssenceAltarMenu> {
    private static final ResourceLocation TEXTURE = Util.prefix("textures/gui/container/essence_altar.png");
    private static final ResourceLocation BLOCK_TEXTURE = Util.prefix("textures/blockentity/float_essence_block.png");
    public static float nameRollTime = 0;
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
            blit(stack, this.leftPos + 25, this.topPos + 17, 235, 16, 16, 16);
        }
        if (! this.getMenu().slots.get(1).hasItem()) {
            blit(stack, this.leftPos + 15, this.topPos + 54, 215, 0, 16, 16);
        }
        if (! this.getMenu().slots.get(2).hasItem()) {
            blit(stack, this.leftPos + 35, this.topPos + 54, 235, 0, 16, 16);
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
        this.model.setupAnim(PVZMod.clientTime * (this.getMenu().slots.get(0).hasItem() ? 1 : 0));
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
        boolean notMoreThanThree = skills.size() <= 3;
        if (this.getMenu().slots.get(0).hasItem() && this.getMenu().slots.get(0).getItem().getItem() instanceof SeedPacketItem<?>) {
            if (! skills.isEmpty()) {
                int x = leftPos + 60;
                int y = topPos;
                if (notMoreThanThree) {
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderTexture(0, TEXTURE);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    this.blit(stack, x - 1, y + 13, 92, 166, 109, 1);
                    this.blit(stack, x - 1, y + 71, 92, 167, 109, 1);
                    for (int i = 0; i < 3; i ++) {
                        this.blit(stack, x, y + 14 + 19 * i, 92, 187, 108, 19);
                    }
                }
                for(int i = 0; i < 3 && i + shownFirstSkill < skills.size(); ++ i) {
                    this.setBlitOffset(0);
                    RenderSystem.setShader(GameRenderer::getPositionTexShader);
                    RenderSystem.setShaderTexture(0, TEXTURE);
                    RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
                    int costSeedPacket = skills.get(i + shownFirstSkill).costSeed;
                    int costItem = skills.get(i + shownFirstSkill).costItem;
                    //handle skill name rendering.
                    String skillName = Language.getInstance().getOrDefault(skills.get(i + shownFirstSkill).name);
                    int j = 0;
                    while (font.width(skillName.substring(j)) > (notMoreThanThree ? 70 : 54) && j < Math.floor((nameRollTime - 4))) {
                        j ++;
                    }
                    skillName = skillName.substring(j);
                    while (font.width(skillName) > (notMoreThanThree ? 70 : 54)) {
                        skillName = skillName.substring(0, skillName.length() - 1);
                    }
                    int color;
                    if (getMenu().isSkillAvailable(ClientProxy.getPlayer(), skills, (short) (i + shownFirstSkill))) {
                        //available.
                        int mouseRelativeX = mouseX - x;
                        int mouseRelativeY = mouseY - (y + 14 + 19 * i);
                        if (mouseRelativeX >= 0 && mouseRelativeY >= 0 && mouseRelativeX < (notMoreThanThree ? 108 : 92) && mouseRelativeY < 19) {
                            this.blit(stack, x, y + 14 + 19 * i, notMoreThanThree ? 92 : 0, 204 + (notMoreThanThree ? 2 : 0), (notMoreThanThree ? 108 : 92), 19);
                            color = 0xffff80;
                        } else {
                            this.blit(stack, x, y + 14 + 19 * i, notMoreThanThree ? 92 : 0, 166 + (notMoreThanThree ? 2 : 0), (notMoreThanThree ? 108 : 92), 19);
                            color = 0x544c3b;
                        }
                        this.blit(stack, x + 1, y + 15 + 19 * i, 16 * i, 223, 16, 16);
                        this.font.draw(stack, skillName, x + 39, y + 20 + 19 * i, color);
                    } else {
                        //not available.
                        this.blit(stack, x, y + 14 + 19 * i, notMoreThanThree ? 92 : 0, 185 + (notMoreThanThree ? 2 : 0), (notMoreThanThree ? 108 : 92), 19);
                        this.blit(stack, x + 1, y + 15 + 19 * i, 16 * i, 239, 16, 16);
                        color = 0x211d17;
                        this.font.draw(stack, skillName, x + 39, y + 20 + 19 * i, color);
                    }
                    color = 0xffffff;
                    RenderSystem.setShaderTexture(0, TEXTURE);
                    this.blit(stack, x + 2, y + 18 + 19 * i, 223, 17, 11, 10);
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
        this.renderBackground(stack);
        super.render(stack, mouseX, mouseY, partialTicks);
        renderTooltip(stack, mouseX, mouseY);
    }

    @Override
    protected void renderTooltip(@NotNull PoseStack stack, int mouseX, int mouseY) {
        int top = this.topPos + 14;
        int bottom = top + 57;
        boolean notMoreThanThree = skills.size() <= 3;
        if (mouseX > leftPos + 60 && mouseX < leftPos + (notMoreThanThree ? 166 : 152) && mouseY > top && mouseY <= bottom) {
            if (skills.size() > (mouseY - top) / 19 + shownFirstSkill) {
                int cost = skills.get((mouseY - top) / 19 + shownFirstSkill).addCostResource;
                int cd = skills.get((mouseY - top) / 19 + shownFirstSkill).addCoolDown;
                List<Component> list = new java.util.ArrayList<>(List.of(
                        Component.translatable(skills.get((mouseY - top) / 19 + shownFirstSkill).name).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA)),
                        Component.translatable(skills.get((mouseY - top) / 19 + shownFirstSkill).name + ".desc").withStyle(Style.EMPTY.withColor(0x545454))));
                if (menu.getItems().get(0).getItem() instanceof SeedPacketItem<?> seedPacket) {
                    if (seedPacket.hasSkill(menu.getItems().get(0), (mouseY - top) / 19 + shownFirstSkill)) {
                        list.add(Component.translatable("tooltip.pvz.already_attached").withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
                    } else {
                        Skill skill = seedPacket.getNotCompatibleWith(menu.getItems().get(0), skills.get((mouseY - top) / 19 + shownFirstSkill));
                        if (skill != null) {
                            list.add(Component.translatable("tooltip.pvz.not_compatible", Component.translatable(skill.name)).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
                        }
                        skill = seedPacket.getStillRequire(menu.getItems().get(0), skills.get((mouseY - top) / 19 + shownFirstSkill));
                        if (skill != null) {
                            list.add(Component.translatable("tooltip.pvz.still_require", Component.translatable(skill.name)).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_RED)));
                        }
                    }
                }
                ClientProxy.MC.screen.renderTooltip(stack, list,
                        Optional.of(new SunImageToolTipComponent(cost, cd
                                , ((SeedPacketItem<?>) menu.getItems().get(0).getItem()).getResource(menu.getItems().get(0)).equals(PVZPlayerCapStats.SUN)
                                , true, true, false)),
                        mouseX, mouseY, font, ItemStack.EMPTY);
            }
        } else {
            super.renderTooltip(stack, mouseX, mouseY);
        }
    }

    @Override
    public boolean mouseScrolled(double p_99127_, double p_99128_, double p_99129_) {
        int skillsNum = skills.size();
        if (skillsNum >= 4) {
            int allowedMax = skillsNum - 3;
            this.shownFirstSkill = Mth.clamp((int) ((double) this.shownFirstSkill - p_99129_), 0, allowedMax);
            return true;
        }
        return super.mouseScrolled(p_99127_, p_99128_, p_99129_);
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
        boolean notMoreThanThree = skills.size() <= 3;
        if (skills.size() >= 4 && mouseX > (double)(leftPos + 156) && mouseX < (double)(leftPos + 168) && mouseY > (double) top + current && mouseY <= (double)(top + current + 15)) {
            this.isDragging = true;
        } else if (mouseX > leftPos + 60 && mouseX < leftPos + (notMoreThanThree ? 166 : 152) && mouseY > top && mouseY <= bottom) {
            if (skills.size() > (mouseY - top) / 19 + shownFirstSkill) {
                PVZAddSkillPacket.addSkill((int) floor((mouseY - top) / 19) + shownFirstSkill);
            }
        }

        return super.mouseClicked(mouseX, mouseY, p_99133_);
    }
}
