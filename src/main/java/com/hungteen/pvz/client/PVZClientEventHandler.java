package com.hungteen.pvz.client;

import com.hungteen.pvz.client.gui.PVZItemDecorator;
import com.hungteen.pvz.client.layer.ButterHeadLayer;
import com.hungteen.pvz.client.layer.EntityFrozenLayer;
import com.hungteen.pvz.client.layer.EntityHypnotizedLayer;
import com.hungteen.pvz.client.layer.StuckArrowWithATargetLayer;
import com.hungteen.pvz.client.renderer.item.EnderSeedBundleItemRenderer;
import com.hungteen.pvz.common.item.ChiliChanItem;
import com.hungteen.pvz.common.item.EnderSeedBundleItem;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.common.network.PVZEntityInteractPacket;
import com.hungteen.pvz.common.network.PVZPacketHandler;
import com.hungteen.pvz.common.register.PVZItems;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Options;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.block.model.ItemOverrides;
import net.minecraft.client.renderer.block.model.ItemTransforms;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.BuiltInModel;
import net.minecraft.client.resources.model.ModelBakery;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.client.event.*;
import net.minecraftforge.registries.ForgeRegistries;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

public class PVZClientEventHandler {

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

    public static void registerExtraModels(ModelEvent.RegisterAdditional ev) {
        ev.register(new ModelResourceLocation("pvz:pumpkin_helmet_equipped_0#inventory"));
        ev.register(new ModelResourceLocation("pvz:pumpkin_helmet_equipped_1#inventory"));
        ev.register(new ModelResourceLocation("pvz:pumpkin_helmet_equipped_2#inventory"));
        ev.register(new ModelResourceLocation("pvz:ender_seed_bundle_opened#inventory"));
        ev.register(new ModelResourceLocation("pvz:ender_seed_bundle_closed#inventory"));
        ev.register(new ModelResourceLocation("pvz:ender_seed_bundle_filled#inventory"));
        ev.register(new ModelResourceLocation("pvz:hot_sauce#inventory"));
    }

    public static void overrideModels(ModelEvent.BakingCompleted ev) {
        ModelResourceLocation bundleLocation = new ModelResourceLocation(PVZItems.ENDER_SEED_BUNDLE.getId(), "inventory");
        var tmp = ModelBakery.BLOCK_ENTITY_MARKER;
        BuiltInModel placeHolder = new BuiltInModel(tmp.getTransforms(), ItemOverrides.EMPTY
                , ev.getModelBakery().getAtlasSet().getSprite(tmp.getMaterial("particle"))
                , tmp.getGuiLight().lightLikeBlock());

        for (ResourceLocation l : ev.getModels().keySet()) {
            if (l.equals(bundleLocation)) {
                ev.getModels().put(l, placeHolder);
                break;
            }
        }
    }

    public static void registerResourceListeners(RegisterClientReloadListenersEvent event) {
        event.registerReloadListener(new ChiliChanItem.EasterEggListener());
    }

    public static void registerItemDecorators(RegisterItemDecorationsEvent event) {
        for (Item item : ForgeRegistries.ITEMS.getValues()) {
            event.register(item, PVZItemDecorator.INSTANCE);
        }
        event.register(PVZItems.ENDER_SEED_BUNDLE.get(), new EnderSeedBundleItemRenderer.EnderSeedBundleItemDecorator());

    }

    public static void catchKeyInput(InputEvent.Key event) {
        Player player = ClientProxy.getPlayer();
        if (player == null || ClientProxy.MC.screen instanceof AbstractContainerScreen<?>) return;
        ItemStack itemStack = EnderSeedBundleItem.getHoldingEnderSeedBundle(player);
        if (PVZKeyBindings.keyEnderSeedBundle.isDown() && itemStack.getItem() instanceof EnderSeedBundleItem item) {
            Options options = ClientProxy.MC.options;
            int slot = 0;
            for (KeyMapping key : options.keyHotbarSlots) {
                if (key.consumeClick()) {
                    EnderSeedBundleItem.selectEnderSeedBundle(player);
                    item.setPointer(itemStack, slot);
                    PVZPacketHandler.sendToServer(new PVZEntityInteractPacket(player, slot));
                    break;
                } else {
                    slot ++;
                }
            }
            if (options.keySwapOffhand.consumeClick()) {
                PVZPacketHandler.sendToServer(new PVZEntityInteractPacket(player, 9));
            } else if (options.keyDrop.consumeClick()) {
                PVZPacketHandler.sendToServer(new PVZEntityInteractPacket(player, 10));
            }
        }
    }

    public static void catchMouseWheelInput(InputEvent.MouseScrollingEvent event) {
        Player player = ClientProxy.getPlayer();
        if (player == null || ClientProxy.MC.screen instanceof AbstractContainerScreen<?>) return;
        ItemStack itemStack = EnderSeedBundleItem.getHoldingEnderSeedBundle(player);
        if (PVZKeyBindings.keyEnderSeedBundle.isDown() && itemStack.getItem() instanceof EnderSeedBundleItem item) {
            int slot = item.getPointer(itemStack) - (int) (event.getScrollDelta());
            while (slot > 8) slot -= 9;
            while (slot < 0) slot += 9;
            item.setPointer(itemStack, slot);
            PVZPacketHandler.sendToServer(new PVZEntityInteractPacket(player, slot));
            event.setCanceled(true);
        }
    }
}
