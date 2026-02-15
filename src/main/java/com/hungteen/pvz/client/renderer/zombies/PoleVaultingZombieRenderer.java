package com.hungteen.pvz.client.renderer.zombies;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.client.model.zombie.PoleVaultingZombieModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.ModelPartEntity;
import com.hungteen.pvz.common.entity.zombies.PoleVaultingZombie;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class PoleVaultingZombieRenderer<T extends PoleVaultingZombie, M extends PoleVaultingZombieModel<T>> extends AbstractPVZZombieRenderer<T, M> {
    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/zombie/pole_vaulting_zombie/pole_vaulting_zombie.png");
    public PoleVaultingZombieRenderer(EntityRendererProvider.Context context) {
        super(context, (M) new PoleVaultingZombieModel<T>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("pole_vaulting_zombie:main"))));
    }

    @Override
    public void render(T zombie, float p_115456_, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int p_115460_) {
        super.render(zombie, p_115456_, partialTicks, poseStack, bufferSource, p_115460_);
        if (PVZConfig.zombieDropParts() && ! ClientProxy.MC.isPaused()) {
            if (zombie.renderPole && (! zombie.hasPole() || zombie.shouldDropHead())) {
                zombie.renderPole = false;
                Vec3 speed = new Vec3(zombie.getRandom().nextFloat() * 0.25 - 0.125,
                        zombie.getRandom().nextFloat() * 0.15,
                        zombie.getRandom().nextFloat() * 0.25 - 0.125);
                model.pole.visible = true;
                new ModelPartEntity(zombie.level, model.pole, getTextureLocation(zombie)).pos(zombie.position().add(0, zombie.getBbHeight(), 0))
                        .speed(speed).rotation(new Vec3(0.15, 0, 0.15)).scale(zombie.isBaby() ? 0.5F : 1F).join(zombie.level);
                model.pole.visible = false;
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T zombie) {
        return TEXTURE;
    }
}
