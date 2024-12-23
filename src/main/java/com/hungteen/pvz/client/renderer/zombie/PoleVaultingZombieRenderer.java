package com.hungteen.pvz.client.renderer.zombie;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.client.model.zombie.PoleVaultingZombieModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.ModelPartEntity;
import com.hungteen.pvz.common.entity.zombies.PoleVaultingZombie;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.ZombieModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.HumanoidMobRenderer;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.layers.HumanoidArmorLayer;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.phys.Vec3;

public class PoleVaultingZombieRenderer<T extends PoleVaultingZombie, M extends PoleVaultingZombieModel<T>> extends HumanoidMobRenderer<T, M> {
    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/zombie/pole_vaulting_zombie/pole_vaulting_zombie.png");
    public PoleVaultingZombieRenderer(EntityRendererProvider.Context context) {
        super(context, (M) new PoleVaultingZombieModel<T>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("pole_vaulting_zombie:main"))), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_INNER_ARMOR)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR))));
        if (PVZConfig.renderZombieStuckArrows()) {
            this.addLayer(new ArrowLayer<>(context, this));
        }
    }

    @Override
    public void render(T zombie, float p_115456_, float partialTicks, PoseStack p_115458_, MultiBufferSource p_115459_, int p_115460_) {
        if (PVZConfig.zombieDropParts() && ! ClientProxy.MC.isPaused()) {
            this.model.setupAnim(zombie, 0, 0, partialTicks, zombie.getYRot(), zombie.getXRot());
            if (zombie.renderHand && zombie.shouldDropHand()) {
                zombie.renderHand = false;
                Vec3 speed = new Vec3(zombie.getRandom().nextFloat() * 0.25 - 0.125,
                        zombie.getRandom().nextFloat() * 0.15,
                        zombie.getRandom().nextFloat() * 0.25 - 0.125);
                new ModelPartEntity(zombie.level, model.leftArm, getTextureLocation(zombie)).pos(zombie.position().add(0, zombie.getBbHeight() * 0.75, 0))
                        .speed(speed).rotation(new Vec3(0.5, 0.5, 0.5)).scale(zombie.isBaby() ? 0.5F : 1F).join(zombie.level);
                new ModelPartEntity(zombie.level, model.leftSleeve, getTextureLocation(zombie)).pos(zombie.position().add(0,  zombie.getBbHeight() * 0.75, 0))
                        .speed(speed).rotation(new Vec3(0.5, 0.5, 0.5)).scale(zombie.isBaby() ? 0.5F : 1F).join(zombie.level);
            }
            if (zombie.renderHead && zombie.shouldDropHead()) {
                zombie.renderHead = false;
                Vec3 speed = new Vec3(zombie.getRandom().nextFloat() * 0.25 - 0.125,
                        zombie.getRandom().nextFloat() * 0.15,
                        zombie.getRandom().nextFloat() * 0.25 - 0.125);
                new ModelPartEntity(zombie.level, model.head, getTextureLocation(zombie)).pos(zombie.position().add(0, zombie.getBbHeight(), 0))
                        .speed(speed).rotation(new Vec3(0.5, 0.5, 0.5)).scale(zombie.isBaby() ? 0.67F : 1F).join(zombie.level);
                new ModelPartEntity(zombie.level, model.hat, getTextureLocation(zombie)).pos(zombie.position().add(0, zombie.getBbHeight(), 0))
                        .speed(speed).rotation(new Vec3(0.5, 0.5, 0.5)).scale(zombie.isBaby() ? 0.67F : 1F).join(zombie.level);
            }
            if (zombie.renderPole && (! zombie.hasPole() || zombie.shouldDropHead())) {
                zombie.renderPole = false;
                Vec3 speed = new Vec3(zombie.getRandom().nextFloat() * 0.25 - 0.125,
                    zombie.getRandom().nextFloat() * 0.15,
                    zombie.getRandom().nextFloat() * 0.25 - 0.125);
                new ModelPartEntity(zombie.level, model.pole, getTextureLocation(zombie)).pos(zombie.position().add(0, zombie.getBbHeight(), 0))
                        .speed(speed).rotation(new Vec3(0.15, 0, 0.15)).scale(zombie.isBaby() ? 0.5F : 1F).join(zombie.level);
            }
        }
        super.render(zombie, p_115456_, partialTicks, p_115458_, p_115459_, p_115460_);
    }

    @Override
    public ResourceLocation getTextureLocation(T zombie) {
        return TEXTURE;
    }
}
