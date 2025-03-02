package com.hungteen.pvz.client.renderer.zombie;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.client.model.zombie.DiggerZombieModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.ModelPartEntity;
import com.hungteen.pvz.common.entity.zombies.DiggerZombie;
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

public class DiggerZombieRenderer<T extends DiggerZombie, M extends DiggerZombieModel<T>> extends HumanoidMobRenderer<T, M> {
    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/zombie/digger_zombie/digger_zombie.png");
    public DiggerZombieRenderer(EntityRendererProvider.Context context) {
        super(context, (M) new DiggerZombieModel<T>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("digger_zombie:main"))), 0.5F);
        this.addLayer(new HumanoidArmorLayer<>(this,
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR)),
                new ZombieModel<>(context.bakeLayer(ModelLayers.ZOMBIE_OUTER_ARMOR))));
        if (PVZConfig.renderZombieStuckArrows()) {
            this.addLayer(new ArrowLayer<>(context, this));
        }
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
        if (PVZConfig.zombieDropParts() && ! ClientProxy.MC.isPaused()) {
            this.model.setupAnim(zombie, 0, 0, partialTicks, zombie.getYRot(), zombie.getXRot());
            if (zombie.renderHat && ! zombie.hasHelmet()) {
                zombie.renderHat = false;
                Vec3 speed = new Vec3(zombie.getRandom().nextFloat() * 0.25 - 0.125,
                        zombie.getRandom().nextFloat() * 0.15,
                        zombie.getRandom().nextFloat() * 0.25 - 0.125);
                new ModelPartEntity(zombie.level, model.helmet, getTextureLocation(zombie)).pos(zombie.position().add(0, zombie.getBbHeight(), 0))
                        .speed(speed).rotation(new Vec3(0.5, 0.4, 0.5)).scale(zombie.isBaby() ? 0.5F : 1F).join(zombie.level);
            }
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
                        .speed(speed).rotation(new Vec3(0.5, 0.3, 0.5)).scale(zombie.isBaby() ? 0.67F : 1F).join(zombie.level);
                new ModelPartEntity(zombie.level, model.hat, getTextureLocation(zombie)).pos(zombie.position().add(0, zombie.getBbHeight(), 0))
                        .speed(speed).rotation(new Vec3(0.5, 0.3, 0.5)).scale(zombie.isBaby() ? 0.67F : 1F).join(zombie.level);
            }
        }
        super.render(zombie, entityYaw, partialTicks, poseStack, p_115459_, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(T zombie) {
        return TEXTURE;
    }
}
