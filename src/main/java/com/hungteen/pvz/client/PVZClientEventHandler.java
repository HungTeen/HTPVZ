package com.hungteen.pvz.client;

import com.hungteen.pvz.client.layer.ButterHeadLayer;
import com.hungteen.pvz.client.layer.EntityFrozenLayer;
import com.hungteen.pvz.client.layer.EntityHypnotizedLayer;
import com.hungteen.pvz.client.layer.StuckArrowWithATargetLayer;
import com.hungteen.pvz.common.item.ChiliChanItem;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.register.PVZItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.ModelEvent;
import net.minecraftforge.client.event.RegisterClientReloadListenersEvent;
import net.minecraftforge.client.event.RenderHandEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

public class PVZClientEventHandler {

    @SubscribeEvent
    public static void addLayers(@SuppressWarnings("rawtypes") EntityRenderersEvent.AddLayers ev) {
        try {
            //get private field.
            Field field = EntityRenderersEvent.AddLayers.class.getDeclaredField("renderers");
            field.setAccessible(true);

            ev.getSkins().forEach(skin -> {
                LivingEntityRenderer<Player, EntityModel<Player>> render = ev.getSkin(skin);
                PVZClientEventHandler.addEntityLayers(Objects.requireNonNull(render));
            });

            try {
                ((Map<EntityType<?>, EntityRenderer<?>>) field.get(ev)).values().stream()
                        .filter(LivingEntityRenderer.class::isInstance)
                        .map(LivingEntityRenderer.class::cast)
                        .forEach(PVZClientEventHandler::addEntityLayers);
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        }
    }

    public static <T extends LivingEntity, M extends EntityModel<T>> void addEntityLayers(LivingEntityRenderer<T, M> renderer) {
        renderer.addLayer(new ButterHeadLayer<>(renderer));
        renderer.addLayer(new EntityFrozenLayer<>(renderer));
        renderer.addLayer(new EntityHypnotizedLayer<>(renderer));
        renderer.addLayer(new StuckArrowWithATargetLayer<>(renderer));
    }


    @SubscribeEvent
    public static void renderPumpkinHelmet(RenderHandEvent ev) {
        PoseStack poseStack = ev.getPoseStack();
        if (ClientProxy.MC.getCameraEntity() instanceof AbstractClientPlayer player) {
            ItemStack itemStack = player.getItemBySlot(EquipmentSlot.HEAD);
            if (itemStack.is(PVZItems.PUMPKIN_HELMET.get()) && ClientProxy.MC.options.getCameraType().isFirstPerson()) {
                poseStack.pushPose();
                poseStack.translate(0, -0.1, - (player.isScoping() ? -0.4 : 0));
                ItemRenderer renderer = ClientProxy.MC.getItemRenderer();
                BakedModel bakedmodel;
                bakedmodel = renderer.getItemModelShaper().getModelManager()
                        .getModel(new ModelResourceLocation("pvz:pumpkin_helmet_equipped_"+ (itemStack.getDamageValue() * 3 / itemStack.getMaxDamage()) + "#inventory"));
                renderer.render(itemStack, ItemTransforms.TransformType.NONE, true,
                        poseStack, ev.getMultiBufferSource(), ev.getPackedLight(), OverlayTexture.NO_OVERLAY, bakedmodel);
                poseStack.popPose();
            }
        }
    }

    @SubscribeEvent
    public static void registerExtraModels(ModelEvent.RegisterAdditional ev) {
        ev.register(new ModelResourceLocation("pvz:pumpkin_helmet_equipped_0#inventory"));
        ev.register(new ModelResourceLocation("pvz:pumpkin_helmet_equipped_1#inventory"));
        ev.register(new ModelResourceLocation("pvz:pumpkin_helmet_equipped_2#inventory"));
        ev.register(new ModelResourceLocation("pvz:hot_sauce#inventory"));
    }

    @SubscribeEvent
    public static void registerResourceListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ChiliChanItem.EasterEggListener());
    }
}
