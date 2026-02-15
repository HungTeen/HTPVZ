package com.hungteen.pvz.client.renderer.zombies;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.PVZMod;
import com.hungteen.pvz.client.model.zombie.PVZZombieModel;
import com.hungteen.pvz.common.entity.ModelPartEntity;
import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import com.hungteen.pvz.common.entity.zombies.zombotany.IZombotany;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.LivingEntityRenderer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

import java.io.FileNotFoundException;

public class ZombotanyRenderer<T extends PVZZombie & IZombotany, M extends PVZZombieModel<T>> extends AbstractPVZZombieRenderer<T, M> {
    private static final ResourceLocation OVERWORLD_LOCATION = Util.prefix("textures/entity/zombie/minecraft_overworld_zombie.png");

    public ZombotanyRenderer(EntityRendererProvider.Context context, Class<M> p_174170_) {
        super(context, getModel(context, p_174170_));
    }
    public static <T extends PVZZombie & IZombotany, M extends PVZZombieModel<T>> M getModel(EntityRendererProvider.Context context, Class<M> m) {
        try {
            return m.getDeclaredConstructor(ModelPart.class).newInstance(context.bakeLayer(ModelLayers.PLAYER));
        } catch (Exception e) {
            return (M) new PVZZombieModel<T>(context.bakeLayer(ModelLayers.PLAYER));
        }
    }
    @Override
    public void render(T zombie, float p_115456_, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int p_115460_) {
        if (PVZConfig.zombieDropParts() && ! ClientProxy.MC.isPaused()) {
            this.model.setupAnim(zombie, 0, 0, partialTicks, zombie.getYRot(), zombie.getXRot());
            if (zombie.renderHead && zombie.shouldDropHead()) {
                zombie.renderHead = false;
                Vec3 speed = new Vec3(zombie.getRandom().nextFloat() * 0.25 - 0.125,
                        zombie.getRandom().nextFloat() * 0.15,
                        zombie.getRandom().nextFloat() * 0.25 - 0.125);
                ResourceLocation location = zombie.getPlantTextureLocation();
                if (location == null) {
                    LivingEntityRenderer<?,?> headRenderer = null;
                    if (ClientProxy.MC.getEntityRenderDispatcher().renderers.get(zombie.getPlantType()) instanceof LivingEntityRenderer<?,?> renderer) {
                        headRenderer = renderer;
                    }
                    if (headRenderer != null) {
                        try {
                            location = zombie.getPlantTextureLocation() == null ?
                                    headRenderer.getTextureLocation(null) : zombie.getPlantTextureLocation();
                        } catch (Exception ignored) {
                        }
                    }
                }
                if (location == null) {
                    PVZMod.LOGGER.error("Missing Head Texture for zombotany " + zombie);
                    location = new ResourceLocation("missingno");
                }
                new ModelPartEntity(zombie.level, model.head, location).pos(zombie.position().add(0, zombie.getBbHeight(), 0))
                        .speed(speed).rotation(new Vec3(0.5, 0.5, 0.5)).scale(zombie.isBaby() ? 0.67F : 1F).join(zombie.level);
            }
        }
        super.render(zombie, p_115456_, partialTicks, poseStack, bufferSource, p_115460_);
    }
    @Override
    public ResourceLocation getTextureLocation(T zombie) {
        try {
            ResourceLocation res = zombie.getStyle().equals("") ? OVERWORLD_LOCATION :
                    Util.prefix("textures/entity/zombie/" + zombie.getStyle() + "_zombie.png");
            ClientProxy.MC.getResourceManager().getResourceOrThrow(res);
            return res;
        } catch (FileNotFoundException e) {
            return OVERWORLD_LOCATION;
        }
    }
}
