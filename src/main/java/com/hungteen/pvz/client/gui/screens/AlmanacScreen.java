package com.hungteen.pvz.client.gui.screens;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.api.PVZAPI;
import com.hungteen.pvz.api.Skill;
import com.hungteen.pvz.api.interfaces.IHaveSkills;
import com.hungteen.pvz.api.interfaces.IPlant;
import com.hungteen.pvz.client.gui.components.SunImageToolTipComponent;
import com.hungteen.pvz.common.capability.player.PVZPlayerCapStats;
import com.hungteen.pvz.common.entity.plants.Dandelion;
import com.hungteen.pvz.common.item.SeedPacketItem;
import com.hungteen.pvz.common.menu.AlmanacMenu;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZEntities;
import com.hungteen.pvz.common.register.PVZSeedPackets;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.platform.Lighting;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Quaternion;
import com.mojang.math.Vector3f;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderDispatcher;
import net.minecraft.locale.Language;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.FormattedText;
import net.minecraft.network.chat.Style;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class AlmanacScreen extends AbstractContainerScreen<AlmanacMenu> {
    private static final ResourceLocation TEXTURE = Util.prefix("textures/gui/container/almanac.png");
    private static final ResourceLocation ICON_TEXTURE = Util.prefix("textures/gui/overlay/icons.png");
    private Item chosenTab = null;
    private int chosenItem = 0;
    private boolean viewingSkills = false;
    private Entity fakeEntity;
    public static int tick = 0;
    private boolean switchingViewingSkills = false;
    private int scrollTo = 0;
    private float contentLength;


    public AlmanacScreen(AlmanacMenu menu, Inventory inventory, Component component) {
        super(menu, inventory, component);
        this.imageWidth = 300;
        this.imageHeight = 200;
        tick = 0;
    }

    @Override
    protected void renderBg(PoseStack poseStack, float partialTicks, int mouseX, int mouseY) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1.0F, 1.0F, 1.0F, 1.0F);
        RenderSystem.setShaderTexture(0, this.TEXTURE);
        this.blit(poseStack, this.leftPos, this.topPos, 0, 0, 150, 200);
        this.blit(poseStack, this.leftPos + 150, this.topPos, 0, 0, 150, 200);
        this.blit(poseStack, this.leftPos + 157, this.topPos + 17, 0, 200, 136, 56);
        //left page
        boolean creative = ClientProxy.getPlayer() != null && ClientProxy.getPlayer().isCreative();
        List<Item> tabs = creative ? PVZSeedPackets.tabsCreative : PVZSeedPackets.tabsSurvival;
        Map<Item, List<Item>> items = creative ? PVZSeedPackets.sortedCreative : PVZSeedPackets.sortedSurvival;
        if (chosenTab == null || ! tabs.contains(chosenTab)) {
            chosenTab = tabs.get(0);
            chosenItem = 0;
            this.viewingSkills = false;
            tick = 0;
        }
        int tmp = 0;
        for (Item tab : tabs) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            this.blit(poseStack, this.leftPos - 32, this.topPos + tmp * 27 + 7, 221, tab == chosenTab ? 0 : 26, 35, 26);
            ClientProxy.MC.getItemRenderer().renderAndDecorateFakeItem(tab.getDefaultInstance(), this.leftPos - 24 + (tab == chosenTab ? 0 : 2), this.topPos + tmp * 27 + 12);
            tmp ++;
        }
        tmp = 0;
        if (chosenItem >= items.get(chosenTab).size()) {
            chosenItem = 0;
            this.viewingSkills = false;
            tick = 0;
        }
        for (Item item : items.get(chosenTab)) {
            RenderSystem.setShaderTexture(0, TEXTURE);
            this.blit(poseStack, this.leftPos + 10 + 26 * (tmp % 5), this.topPos + 20 + 26 * (tmp / 5), 150, tmp == chosenItem ? 25 : 0, 25, 25);
            ClientProxy.MC.getItemRenderer().renderAndDecorateFakeItem(item.getDefaultInstance(), this.leftPos + 14 + 26 * (tmp % 5), this.topPos + 24 + 26 * (tmp / 5));
            tmp ++;
        }
        //right page
        if (items.get(chosenTab).get(chosenItem) instanceof SeedPacketItem<?> selected) {
            if (this.fakeEntity == null || this.fakeEntity.getType() != selected.getEntity()) {
                this.fakeEntity = selected.getEntity().create(ClientProxy.getLevel());
                Player player = ClientProxy.getPlayer();
                fakeEntity.setPos(player.getX(), player.getY(), player.getZ());
                if (fakeEntity instanceof IPlant iPlant) {
                    iPlant.setupPresentationAnim();
                }
            }
            if (fakeEntity != null && fakeEntity instanceof LivingEntity living) {
                int i = this.leftPos;
                int j = this.topPos;
                renderEntityInInventory(i + 193, j + 62, 25, (float)(i + 193) - mouseX, (float)(j + 40) - mouseY, living);
            }
            if (PVZSeedPackets.dataMap.get(selected) instanceof PVZSeedPackets.RecipeSeedPacketData<?> data) {
                ClientProxy.MC.getItemRenderer().renderAndDecorateFakeItem(data.getBackCard().get().getDefaultInstance(),
                        this.leftPos + 253, this.topPos + 18);
                if (data.recipe.containsKey("seed")) {
                    Object seed = data.recipe.get("seed");
                    if (seed instanceof ItemLike itemLike) {
                        ClientProxy.MC.getItemRenderer().renderAndDecorateFakeItem(itemLike.asItem().getDefaultInstance(),
                                this.leftPos + 234, this.topPos + 18);
                    } else if (seed instanceof RegistryObject<?> registryObject && registryObject.get() instanceof ItemLike itemLike) {
                        ClientProxy.MC.getItemRenderer().renderAndDecorateFakeItem(itemLike.asItem().getDefaultInstance(),
                                this.leftPos + 234, this.topPos + 18);
                    }
                }
                if (data.recipe.containsKey("essence")) {
                    Object essence = data.recipe.get("essence");
                    if (essence instanceof RegistryObject<?> registryObject && registryObject.get() instanceof ItemLike itemLike) {
                        ClientProxy.MC.getItemRenderer().renderAndDecorateFakeItem(itemLike.asItem().getDefaultInstance(),
                                this.leftPos + 272, this.topPos + 18);
                    }
                }
            }
            poseStack.pushPose();
            poseStack.translate(0, 0, 100);
            RenderSystem.setShaderTexture(0, TEXTURE);
            this.blit(poseStack, this.leftPos + 157, this.topPos + 62, 0, 245, 136, 11);
            poseStack.popPose();
            if (selected.getResource(null).equals(PVZAPI.get().getSunResourceName())) {
                poseStack.pushPose();
                poseStack.translate(0, 0, 100);
                int disButtonLeft = mouseX - this.leftPos - (viewingSkills ? 237 : 266);
                int disButtonTop = mouseY - this.topPos - 41;
                if (disButtonLeft < 0 || disButtonLeft > 20 || disButtonTop < 0 || disButtonTop > 20) {
                    switchingViewingSkills = false;
                }
                this.blit(poseStack, this.leftPos + (viewingSkills ? 237 : 266), this.topPos + 41,
                        switchingViewingSkills ? 195 : 175, viewingSkills ? 25 : 45, 20, 20);
                if (fakeEntity instanceof IHaveSkills iHaveSkills) {
                    iHaveSkills.setSkillVal(0);
                }
                if (viewingSkills) {
                    int size = selected.getStaticSkillList().size();
                    contentLength = size;
                    for (tmp = scrollTo; tmp < scrollTo + Math.min(size, 5); tmp ++) {
                        RenderSystem.setShaderTexture(0, TEXTURE);
                        int x = this.leftPos + (size > 5 ? 162 : 165);
                        int y = this.topPos + 77 + (tmp - scrollTo) * 21;
                        Skill skill = selected.getStaticSkillList().get(tmp);
                        boolean skillSelected = mouseX >= x && mouseX < x + 120 && mouseY >= y && mouseY < y + 19;
                        if (skillSelected && fakeEntity instanceof IHaveSkills iHaveSkills) {
                            iHaveSkills.setSkill(iHaveSkills, tmp, true);
                        }
                        this.blit(poseStack, x, y, 136, skillSelected ? 218 : 237, 120, 19);
                        this.blit(poseStack, x + 2, y + 4, 200, 0, 11, 10);
                        ItemStack itemStack =  new ItemStack(skill.item.get());
                        itemStack.setCount(skill.costItem);
                        this.itemRenderer.renderAndDecorateFakeItem(itemStack, x + 17, y + 2);
                        this.itemRenderer.renderGuiItemDecorations(this.font, itemStack, x + 17, y + 1);
                    }
                }
                int maxContain = viewingSkills ? 5 : 10;
                float percent = maxContain / contentLength;
                if (percent < 1) {
                    final int full = 117;
                    int start = topPos + 80 + (int) (scrollTo * full / contentLength);
                    int end = topPos + 68 + (int) (((float) scrollTo / contentLength + percent) * full);
                    this.blit(poseStack, this.leftPos + 287, start - 6, 201, 11, 4, 6);
                    this.blit(poseStack, this.leftPos + 287, end, 201, 18, 4, 6);
                    int body = end - start;
                    while (body > 0) {
                        this.blit(poseStack, this.leftPos + 287, end - body, 206, 11, 4, 4);
                        body -= 4;
                    }
                }
                boolean renderAsNumber = PVZConfig.renderSunAsNumber() || ! selected.getResource(null).equals(PVZAPI.get().getSunResourceName());
                RenderSystem.setShaderTexture(0, ICON_TEXTURE);
                if (renderAsNumber) {
                    this.blit(poseStack, this.leftPos + 160, this.topPos + 59, 40, 0, 9, 9);
                } else {
                     int cost = selected.getBaseCost(null);
                     tmp = 0;
                     while (cost > 0 || tmp == 0) {
                         if (cost >= 100) {
                             this.blit(poseStack, this.leftPos + 160 + tmp * 8, this.topPos + 59, 40, 0, 9, 9);
                         } else if (cost >= 75) {
                             this.blit(poseStack, this.leftPos + 160 + tmp * 8, this.topPos + 59, 30, 0, 9, 9);
                             break;
                         } else if (cost >= 50) {
                             this.blit(poseStack, this.leftPos + 160 + tmp * 8, this.topPos + 59, 20, 0, 9, 9);
                             break;
                         } else if (cost >= 25) {
                             this.blit(poseStack, this.leftPos + 160 + tmp * 8, this.topPos + 59, 10, 0, 9, 9);
                             break;
                         } else {
                             this.blit(poseStack, this.leftPos + 160 + tmp * 8, this.topPos + 59, 0, 0, 9, 9);
                             break;
                         }
                         tmp ++;
                         cost -= 100;
                     }
                }
                poseStack.popPose();
            }
        }
    }
    public static void renderEntityInInventory(int x, int y, int scale, float lookingX, float lookingY, LivingEntity entity) {
        float angleXComponent = (float)Math.atan(lookingX / 40.0F);
        float angleYComponent = (float)Math.atan(lookingY / 40.0F);
        PoseStack posestack = RenderSystem.getModelViewStack();
        posestack.pushPose();
        posestack.translate(x, y, 1050.0D);
        posestack.scale(1.0F, 1.0F, -1.0F);
        RenderSystem.applyModelViewMatrix();
        PoseStack posestack1 = new PoseStack();
        posestack1.translate(0.0D, 0.0D, 1000.0D);
        posestack1.scale((float)scale, (float)scale, (float)scale);
        Quaternion quaternion = Vector3f.ZP.rotationDegrees(180.0F);
        Quaternion quaternion1 = Vector3f.XP.rotationDegrees(angleYComponent * 20.0F);
        quaternion.mul(quaternion1);
        posestack1.mulPose(quaternion);
        float f2 = entity.yBodyRot;
        float f3 = entity.getYRot();
        float f4 = entity.getXRot();
        float f5 = entity.yHeadRotO;
        float f6 = entity.yHeadRot;
        entity.yBodyRot = 140.0F + angleXComponent * 20.0F;
        entity.setYRot(140.0F + angleXComponent * 40.0F);
        entity.setXRot(-angleYComponent * 20.0F);
        entity.yHeadRot = entity.getYRot();
        entity.yHeadRotO = entity.getYRot();
        Lighting.setupForEntityInInventory();
        EntityRenderDispatcher entityrenderdispatcher = Minecraft.getInstance().getEntityRenderDispatcher();
        quaternion1.conj();
        entityrenderdispatcher.overrideCameraOrientation(quaternion1);
        entityrenderdispatcher.setRenderShadow(false);
        MultiBufferSource.BufferSource multibuffersource$buffersource = Minecraft.getInstance().renderBuffers().bufferSource();
        RenderSystem.runAsFancy(() -> {
            entity.tickCount = tick;
            entityrenderdispatcher.render(entity, 0.0D, 0.0D, 0.0D, 0.0F, 1.0F, posestack1, multibuffersource$buffersource, 15728880);
        });
        multibuffersource$buffersource.endBatch();
        entityrenderdispatcher.setRenderShadow(true);
        entity.yBodyRot = f2;
        entity.setYRot(f3);
        entity.setXRot(f4);
        entity.yHeadRotO = f5;
        entity.yHeadRot = f6;
        posestack.popPose();
        RenderSystem.applyModelViewMatrix();
        Lighting.setupFor3DItems();
    }
    protected void renderLabels(PoseStack poseStack, int mouseX, int mouseY) {
        this.font.draw(poseStack, this.title, (float)this.titleLabelX, (float)this.titleLabelY, 4210752);
        Util.drawCenteredScaledString(poseStack, this.font, this.fakeEntity.getName().getString(), 225, titleLabelY, 4210752, 1);

        poseStack.pushPose();
        poseStack.translate(0, 0, 100);
        boolean creative = ClientProxy.getPlayer() != null && ClientProxy.getPlayer().isCreative();
        Map<Item, List<Item>> items = creative ? PVZSeedPackets.sortedCreative : PVZSeedPackets.sortedSurvival;
        if (items.get(chosenTab).get(chosenItem) instanceof SeedPacketItem<?> selected) {
            boolean renderAsNumber = PVZConfig.renderSunAsNumber() || ! selected.getResource(null).equals(PVZAPI.get().getSunResourceName());
            if (renderAsNumber) {
                this.font.draw(poseStack, selected.getBaseCost(null) + "",
                        171 + (selected.getResource(null).equals(PVZAPI.get().getSunResourceName()) ? 0 : 10), 61, 0);
                this.font.draw(poseStack, selected.getBaseCost(null) + "",
                        170 + (selected.getResource(null).equals(PVZAPI.get().getSunResourceName()) ? 0 : 10), 60, 0xFFFFFF);
            }
            if (viewingSkills) {
                int size = selected.getStaticSkillList().size();
                for (int tmp = scrollTo; tmp < scrollTo + Math.min(size, 5); tmp ++) {
                    int x = 165;
                    int y = 77 + (tmp - scrollTo) * 21;
                    Skill skill = selected.getStaticSkillList().get(tmp);
                    this.font.drawShadow(poseStack, skill.costSeed + "", x + 16 - this.font.width(skill.costSeed + ""), y + 10, 0xFFFFFF);
                    String skillName = Language.getInstance().getOrDefault(skill.name);
                    int j = 0;
                    while (font.width(skillName.substring(j)) > 80 && j < Math.floor((EssenceAltarScreen.nameRollTime - 4))) {
                        j ++;
                    }
                    skillName = skillName.substring(j);
                    while (font.width(skillName) > 80) {
                        skillName = skillName.substring(0, skillName.length() - 1);
                    }
                    boolean skillSelected = mouseX >= x + this.leftPos && mouseX < x + this.leftPos + 120 && mouseY >= y + this.topPos && mouseY < y + this.topPos + 19;
                    this.font.draw(poseStack, skillName, x + 39, y + 6, skillSelected ? 0xffff80 : 0x544c3b);
                }
            } else {
                int cd = selected.getBaseCoolDown(null);
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
                this.font.draw(poseStack, Component.translatable("tooltip.pvz.cool_down").append(Component.literal(" ").append(Component.translatable(key))),
                        160, 180, 0x888888);
                float tmp = 0;
                int sep = 0;
                List<FormattedText> stringList = new ArrayList<>();
                int stringWidth = contentLength > 10 ? 124 : 130;
                for (Component note : PVZSeedPackets.dataMap.get(selected).notes) {
                    List<FormattedText> list = this.font.getSplitter().splitLines(note.getString(), stringWidth, Style.EMPTY);
                    String style = list.get(0).getString().startsWith("§") ? list.get(0).getString().substring(0,2) : "";
                    list.forEach(text -> stringList.add(FormattedText.of(style + text.getString())));
                }
                if (selected.creativeOnly) {
                    Component note = Component.translatable("hint.pvz.creative_only");
                    List<FormattedText> list = this.font.getSplitter().splitLines(note.getString(), stringWidth, Style.EMPTY);
                    list.forEach(text -> stringList.add(FormattedText.of("§4" + text.getString())));
                }
                if (selected.getEntity() == PVZEntities.DANDELION.get()
                        && ! ClientProxy.getLevel().getEntitiesOfClass(Dandelion.class
                        , ClientProxy.getPlayer().getBoundingBox().inflate(3, 0, 3)
                        , dandelion -> dandelion.getCustomName() != null && dandelion.getCustomName().getString().equals("涟清")).isEmpty()) {
                    Component note = Component.translatable("almanac.pvz.pvz:dandelion.special");
                    List<FormattedText> list = this.font.getSplitter().splitLines(note.getString(), stringWidth, Style.EMPTY);
                    list.forEach(text -> stringList.add(FormattedText.of("§7" + text.getString())));
                }
                sep = stringList.size() - 1;
                for (FormattedText text : this.font.getSplitter().splitLines(Component.translatable("almanac.pvz." + ForgeRegistries.ENTITY_TYPES.getKey(selected.getEntity())).getString(), 130, Style.EMPTY)) {
                    stringList.addAll(this.font.getSplitter().splitLines(text.getString(), stringWidth, Style.EMPTY));
                }
                contentLength = stringList.size();
                for (FormattedText text : stringList) {
                    if (tmp >= scrollTo) {
                        this.font.draw(poseStack, text.getString(), 160, 75 + (tmp - scrollTo) * 10, 0xFFFFFF);
                    }
                    tmp ++;
                    if (sep >= 0 && tmp == sep + 1) tmp += 0.5F;
                    if (tmp >= scrollTo + 10) break;
                }
            }
        }
        Util.drawCenteredScaledString(poseStack, this.font, Component.translatable(this.viewingSkills ? "container.pvz.almanac.skill" : "container.pvz.almanac.intro").getString(),
                262, 62, 0xFFFFFF, 1);
        poseStack.popPose();
    }
    protected void renderTooltip(@NotNull PoseStack poseStack, int mouseX, int mouseY) {
        int x = mouseX - this.leftPos;
        int y = mouseY - this.topPos;
        int disButtonLeft = x - (viewingSkills ? 237 : 266);
        int disButtonTop = y - 41;
        if (disButtonLeft > 0 && disButtonLeft < 20 && disButtonTop > 0 && disButtonTop < 20) {
            ClientProxy.MC.screen.renderTooltip(poseStack, List.of(Component.translatable(viewingSkills ? "container.pvz.almanac.intro" : "container.pvz.almanac.skill")),
                    Optional.empty(), mouseX, mouseY + 20, font, ItemStack.EMPTY);
        } else if (viewingSkills) {
            boolean creative = ClientProxy.getPlayer() != null && ClientProxy.getPlayer().isCreative();
            Map<Item, List<Item>> items = creative ? PVZSeedPackets.sortedCreative : PVZSeedPackets.sortedSurvival;
            if (items.get(chosenTab).get(chosenItem) instanceof SeedPacketItem<?> selected) {
                int size = selected.getStaticSkillList().size();
                boolean skillSelected;
                Skill skill;
                for (int tmp = 0; tmp < size; tmp ++) {
                    int startX = this.leftPos + 165;
                    int startY = tmp * 21 + this.topPos + 77;
                    skillSelected = mouseX >= startX && mouseX < startX + 120 && mouseY >= startY && mouseY < startY + 19;
                    if (skillSelected) {
                        skill = selected.getStaticSkillList().get(tmp);
                        List<Component> list = new java.util.ArrayList<>(List.of(
                                Component.translatable(skill.name).withStyle(Style.EMPTY.withColor(ChatFormatting.DARK_AQUA)),
                                Component.translatable(skill.name + ".desc").withStyle(Style.EMPTY.withColor(0x545454))));
                        ClientProxy.MC.screen.renderTooltip(poseStack, list,
                                Optional.of(new SunImageToolTipComponent(skill.addCostResource, skill.addCoolDown,
                                        selected.getResource(null).equals(PVZPlayerCapStats.SUN), true, true)),
                                mouseX, mouseY, font, ItemStack.EMPTY);
                    }
                }
            }
        } else {
            super.renderTooltip(poseStack, mouseX, mouseY);
        }
    }
    @Override
    public void render(PoseStack stack, int mouseX, int mouseY, float partialTicks){
        super.render(stack, mouseX, mouseY, partialTicks);
        renderTooltip(stack, mouseX, mouseY);
    }
    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double offset) {
        if (mouseX - this.leftPos - 157 > 0 && mouseX - this.leftPos - 292 < 0 && mouseY - this.topPos - 77 > 0 && mouseY - this.topPos - 197 < 0) {
            this.scrollTo -= offset;
            this.scrollTo = Math.min(Math.max(0, (int) contentLength - (viewingSkills ? 5 : 10)), Math.max(0, scrollTo));
        }
        return super.mouseScrolled(mouseX, mouseX, offset);
    }
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        double x = mouseX - this.leftPos;
        double y = mouseY - this.topPos;
        if (button == 0 || button == 1) {
            int disButtonLeft = (int) (x - (viewingSkills ? 237 : 266));
            int disButtonTop = (int) (y - 41);
            if (disButtonLeft > 0 && disButtonLeft < 20 && disButtonTop > 0 && disButtonTop < 20) {
                switchingViewingSkills = true;
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        double x = mouseX - this.leftPos;
        double y = mouseY - this.topPos;
        boolean creative = ClientProxy.getPlayer() != null && ClientProxy.getPlayer().isCreative();
        if (button == 0) {
            if (x < 0 && x > -29 && y > 7) {
                if ((y - 7) % 27 < 26) {
                    int tab = (int) ((y - 7) / 27);
                    List<Item> tabs = creative ? PVZSeedPackets.tabsCreative : PVZSeedPackets.tabsSurvival;
                    if (tab < tabs.size()) {
                        this.chosenTab = tabs.get(tab);
                        this.chosenItem = 0;
                        this.viewingSkills = false;
                        scrollTo = 0;
                        contentLength = 0;
                        tick = 0;
                        return true;
                    }
                }
            }
            if (x > 10 && x < 138 && y > 20) {
                if ((x - 10) % 26 < 24) {
                    int num = (int) (x - 10) / 26;
                    if ((y - 20) % 26 < 24) {
                        num += (int) (y - 20) / 26 * 5;
                        Map<Item, List<Item>> items = creative ? PVZSeedPackets.sortedCreative : PVZSeedPackets.sortedSurvival;
                        if (items.containsKey(chosenTab) && items.get(chosenTab).size() > num) {
                            this.chosenItem = num;
                            this.viewingSkills = false;
                            scrollTo = 0;
                            contentLength = 0;
                            tick = 0;
                        }
                    }
                }
            }
        }
        if (switchingViewingSkills) {
            viewingSkills = ! viewingSkills;
            scrollTo = 0;
            contentLength = 0;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }
}
