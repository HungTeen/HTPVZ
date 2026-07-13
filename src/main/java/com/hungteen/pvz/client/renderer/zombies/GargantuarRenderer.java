package com.hungteen.pvz.client.renderer.zombies;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.client.layer.GargantuarHeadLayer;
import com.hungteen.pvz.client.model.zombie.GargantuarModel;
import com.hungteen.pvz.client.particle.ModelPartParticle;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.zombies.Gargantuar;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.io.FileNotFoundException;
import java.util.List;

public class GargantuarRenderer<T extends Gargantuar, M extends GargantuarModel<T>> extends MobRenderer<T, M> {
    private static final ResourceLocation OVERWORLD_LOCATION = Util.prefix("textures/entity/zombie/gargantuar/minecraft_overworld_gargantuar.png");
    private static final ResourceLocation OVERWORLD_LOCATION_DAMAGED = Util.prefix("textures/entity/zombie/gargantuar/minecraft_overworld_gargantuar_damaged.png");
    public GargantuarRenderer(EntityRendererProvider.Context context) {
        this(context, (M) new GargantuarModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("gargantuar:main"))), 1.2F);
    }
    public GargantuarRenderer(EntityRendererProvider.Context context, M model, float shadowSize) {
        this(context, model, shadowSize, 1.0F, 1.0F, 1.0F);
    }
    public GargantuarRenderer(EntityRendererProvider.Context context, M model, float p_174175_, float p_174176_, float p_174177_, float p_174178_) {
        super(context, model, p_174175_);
        this.addLayer(new GargantuarHeadLayer<>(this, new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)),
                context.getModelSet(), context.getItemInHandRenderer()));
        this.addLayer(new ItemInHandLayer<>( this, context.getItemInHandRenderer()));
    }
    @Override
    public void render(T zombie, float p_115456_, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int p_115460_) {
        if (PVZConfig.zombieDropParts() && ! ClientProxy.MC.isPaused()) {
            this.model.setupAnim(zombie, 0, 0, partialTicks, zombie.getYRot(), zombie.getXRot());
            if (zombie.renderHead && zombie.shouldDropHead()) {
                zombie.renderHead = false;
                new ModelPartParticle(zombie, List.of(model.head, model.hat), getTextureLocation(zombie), new Vec3(0, zombie.getBbHeight(), 0))
                        .offset(new Vec3(0, 0.125, 0));
            }
        }
        super.render(zombie, p_115456_, partialTicks, poseStack, bufferSource, p_115460_);
    }
    @Override
    public ResourceLocation getTextureLocation(T zombie) {
        boolean isDamaged = zombie.getHealth() * 2 < zombie.getMaxHealth();
        try {
            ResourceLocation res = zombie.getStyle().equals("") ? (isDamaged ? OVERWORLD_LOCATION_DAMAGED : OVERWORLD_LOCATION) :
                    Util.prefix("textures/entity/zombie/gargantuar/" + zombie.getStyle() + "_gargantuar" + (isDamaged ? "_damaged" : "") + ".png");
            ClientProxy.MC.getResourceManager().getResourceOrThrow(res);
            return res;
        } catch (FileNotFoundException e) {
            return isDamaged ? OVERWORLD_LOCATION_DAMAGED : OVERWORLD_LOCATION;
        }
    }
}
