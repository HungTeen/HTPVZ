package com.hungteen.pvz.client.renderer.zombie;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.client.model.zombie.DiggerZombieModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.ModelPartEntity;
import com.hungteen.pvz.common.entity.zombies.DiggerZombie;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class DiggerZombieRenderer<T extends DiggerZombie, M extends DiggerZombieModel<T>> extends AbstractPVZZombieRenderer<T, M> {
    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/zombie/digger_zombie/digger_zombie.png");
    public DiggerZombieRenderer(EntityRendererProvider.Context context) {
        super(context, (M) new DiggerZombieModel<T>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("digger_zombie:main"))));
    }

    @Override
    protected void setupRotations(T zombie, PoseStack poseStack, float p_117804_, float p_117805_, float p_117806_) {
        super.setupRotations(zombie, poseStack, p_117804_, p_117805_, p_117806_);
        if (zombie.isVisuallySwimming()) {
             poseStack.translate(0, -0.3D, 0F);
        }
    }

    @Override
    public void render(T zombie, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource p_115459_, int packedLight) {
        super.render(zombie, entityYaw, partialTicks, poseStack, p_115459_, packedLight);
        if (PVZConfig.zombieDropParts() && ! ClientProxy.MC.isPaused()) {
            if (zombie.renderHat && ! zombie.hasHelmet()) {
                zombie.renderHat = false;
                Vec3 speed = new Vec3(zombie.getRandom().nextFloat() * 0.25 - 0.125,
                        zombie.getRandom().nextFloat() * 0.15,
                        zombie.getRandom().nextFloat() * 0.25 - 0.125);
                new ModelPartEntity(zombie.level, model.helmet, getTextureLocation(zombie)).pos(zombie.position().add(0, zombie.getBbHeight(), 0))
                        .speed(speed).rotation(new Vec3(0.5, 0.4, 0.5)).scale(zombie.isBaby() ? 0.5F : 1F).join(zombie.level);
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T zombie) {
        return TEXTURE;
    }
}
