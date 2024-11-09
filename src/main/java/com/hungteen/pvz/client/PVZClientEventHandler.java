package com.hungteen.pvz.client;

import com.hungteen.pvz.client.layer.ButterHeadLayer;
import com.hungteen.pvz.client.layer.EntityFrozenLayer;
import com.hungteen.pvz.client.layer.EntityHypnotizedLayer;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.event.EntityRenderersEvent;

import java.lang.reflect.Field;
import java.util.Map;
import java.util.Objects;

public class PVZClientEventHandler {

    public static void addLayers(@SuppressWarnings("rawtypes") EntityRenderersEvent.AddLayers ev){
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
    }

    public static void addItemRenderer() {

    }
}
