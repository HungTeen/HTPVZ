package com.hungteen.pvz.compat;

import com.hungteen.pvz.api.interfaces.IGardenPlant;
import com.hungteen.pvz.api.interfaces.IHaveSkills;
import com.hungteen.pvz.client.gui.PVZOverlayHandler;
import com.hungteen.pvz.common.capability.entity.PVZEntityCapability;
import com.hungteen.pvz.util.EntityUtil;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec2;
import org.jetbrains.annotations.Nullable;
import snownee.jade.api.*;
import snownee.jade.api.config.IPluginConfig;
import snownee.jade.api.ui.Element;
import snownee.jade.impl.config.PluginConfig;
import snownee.jade.overlay.DisplayHelper;
import snownee.jade.overlay.OverlayRenderer;

import java.util.Optional;

@WailaPlugin
public class JadeCompatHandler implements IWailaPlugin {
    public static final ResourceLocation CLIENT = Util.prefix("client");
    public static final ResourceLocation OWNER = Util.prefix("owner");
    public static final ResourceLocation SPROUT_GROW_TIME = Util.prefix("sprout_grow_time");
    public static final ResourceLocation DEFENCE_HEALTH = Util.prefix("armor");
    public static final ResourceLocation SKILLS = Util.prefix("skills");

    @Override
    public void register(IWailaCommonRegistration reg) {
        reg.registerEntityDataProvider(PVZEntityOwnerProvider.INSTANCE, Entity.class);
        reg.registerEntityDataProvider(PVZSproutProvider.INSTANCE, Entity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration reg) {
        reg.addConfig(CLIENT, true);
        reg.addConfig(DEFENCE_HEALTH, true);
        reg.addConfig(SKILLS, true);
        reg.addConfig(SPROUT_GROW_TIME, true);
        reg.registerEntityComponent(PVZEntityComponent.INSTANCE, Entity.class);
        reg.registerEntityComponent(PVZEntityOwnerProvider.INSTANCE, Entity.class);
        reg.registerEntityComponent(PVZSproutProvider.INSTANCE, Entity.class);
    }

    public static class PVZEntityComponent implements IEntityComponentProvider {

        public static final PVZEntityComponent INSTANCE = new PVZEntityComponent();


        private void appendDefenceHealth(Entity entity, ITooltip tooltip) {
            if (! (entity instanceof LivingEntity)) {//only use for living.
                return;
            }
            int health = EntityUtil.getExtraArmorHealth(entity);
            if (health > 0) {
                tooltip.add(new PVZDefenceElement(health));
            }
        }

        private void appendSkills(Level level, Entity entity, ITooltip tooltip) {
            if (entity instanceof IHaveSkills iHaveSkills) {
                iHaveSkills.getSkills(entity).forEach(index -> {
                    tooltip.add(Component.translatable(iHaveSkills.getStaticSkillList().get(index).name).withStyle(ChatFormatting.DARK_AQUA));
                });
            }
        }

        @Override
        public void appendTooltip(ITooltip iTooltip, EntityAccessor entityAccessor, IPluginConfig iPluginConfig) {
            if (iPluginConfig.get(DEFENCE_HEALTH)) {
                appendDefenceHealth(entityAccessor.getEntity(), iTooltip);
            }
            if (iPluginConfig.get(SKILLS)) {
                appendSkills(entityAccessor.getLevel(), entityAccessor.getEntity(), iTooltip);
            }
        }

        @Override
        public ResourceLocation getUid() {
            return CLIENT;
        }
    }

    public static class PVZSproutProvider implements IServerDataProvider<Entity>, IEntityComponentProvider {

        public static final PVZSproutProvider INSTANCE = new PVZSproutProvider();

        @Override
        public void appendServerData(CompoundTag tag, ServerPlayer serverPlayer, Level level, Entity entity, boolean b) {
            if (entity instanceof IGardenPlant iGardenPlant) {
                int growTick = iGardenPlant.getRemainingGrowTick();
                if (growTick > 0) {
                    tag.putInt("GrowTime", growTick / 20 + 1);
                }
            }
        }

        @Override
        public ResourceLocation getUid() {
            return OWNER;
        }

        @Override
        public void appendTooltip(ITooltip iTooltip, EntityAccessor accessor, IPluginConfig iPluginConfig) {
            if (accessor.getServerData().contains("GrowTime")) {
                iTooltip.add(Component.translatable("jade.mobgrowth.time", accessor.getServerData().getInt("GrowTime"))
                        .withStyle(ChatFormatting.GRAY));
            }
        }
    }

    public static class PVZEntityOwnerProvider implements IServerDataProvider<Entity>, IEntityComponentProvider {

        public static final PVZEntityOwnerProvider INSTANCE = new PVZEntityOwnerProvider();

        @Override
        public void appendServerData(CompoundTag tag, ServerPlayer serverPlayer, Level level, Entity entity, boolean b) {
            entity.getCapability(PVZEntityCapability.CAP).ifPresent(cap -> Optional.ofNullable(cap.getOwner() == null
                    ? (cap.getOwnerUuid() == null ? null : level.getPlayerByUUID(cap.getOwnerUuid()))
                    : cap.getOwner()).ifPresent(owner -> {
                        if (EntityUtil.isTeammate(entity, serverPlayer)) {
                            tag.putString("Owner", owner.getName().getString());
                        } else {
                            tag.putString("Owner", "#");
                        }
                    }));
        }

        @Override
        public ResourceLocation getUid() {
            return OWNER;
        }

        @Override
        public void appendTooltip(ITooltip iTooltip, EntityAccessor accessor, IPluginConfig iPluginConfig) {
            if (accessor.getServerData().contains("Owner")) {
                String owner = accessor.getServerData().getString("Owner");
                if (owner.equals("#")) {
                    iTooltip.add(Component.translatable("tooltip.pvz.jade.enemy").withStyle(ChatFormatting.DARK_RED));
                } else {
                    iTooltip.add(Component.translatable("jade.owner", owner).withStyle(ChatFormatting.GRAY));
                }
            }
        }
    }

    public static class PVZDefenceElement extends Element {
        private final int armor;

        public PVZDefenceElement(int armor) {
            this.armor = armor;
        }

        public Vec2 getSize() {
            int armor = (int) Math.ceil((float) this.armor / 5);
            if (armor > PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_MAX_FOR_RENDER) / 2) {
                Font font = Minecraft.getInstance().font;
                return new Vec2((float) (10 + font.width(this.armor + "")), 10.0F);
            } else {
                float maxHearts = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_ICONS_PER_LINE);
                return new Vec2((float) (8 * Math.min(maxHearts, Math.ceil((float) armor / 2))), (float) (10 * Math.ceil(armor / maxHearts)));
            }
        }

        public void render(PoseStack poseStack, float x, float y, float maxX, float maxY) {
            float maxHearts = (float)PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_ICONS_PER_LINE);
            int maxHeartsForRender = PluginConfig.INSTANCE.getInt(Identifiers.MC_ENTITY_HEALTH_MAX_FOR_RENDER);
            int armor = (int) Math.ceil((float) this.armor / 5);
            boolean showNumbers = armor > (float) maxHeartsForRender / 2;
            int iconCount = showNumbers ? 1 : armor;
            int heartsPerLine = (int)Math.min(maxHearts, (double) this.armor);
            int xOffset = 0;

            Util.setTexture(Util.prefix("textures/gui/overlay/icons.png"));
            for (int i = 1; i <= iconCount; ++ i) {
                if (i <= this.armor / 5) {
                    PVZOverlayHandler.blit(poseStack, (int) (x + xOffset), (int) y, 90, 10, 9, 9);
                    xOffset += 8;
                } else {
                    PVZOverlayHandler.blit(poseStack, (int) (x + xOffset), (int) y, 100, 10, 9, 9);
                    xOffset += 8;
                }
                if (! showNumbers && i % heartsPerLine == 0) {
                    y += 10.0F;
                    xOffset = 0;
                }
            }

            if (showNumbers) {
                DisplayHelper.INSTANCE.drawText(poseStack, this.armor + "", x + 10, y, OverlayRenderer.normalTextColorRaw);
            }

        }

        public @Nullable Component getMessage() {
            return Component.translatable("narration.jade.health", new Object[]{DisplayHelper.dfCommas.format((double)this.armor)});
        }
    }
}
