package com.hungteen.pvz.client.renderer.zombies;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.client.model.zombie.DiggerZombieModel;
import com.hungteen.pvz.client.particle.ModelPartParticle;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.zombies.DiggerZombie;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;

public class DiggerZombieRenderer<T extends DiggerZombie, M extends DiggerZombieModel<T>> extends AbstractPVZZombieRenderer<T, M> {
    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/zombie/digger_zombie/digger_zombie.png");
    public DiggerZombieRenderer(EntityRendererProvider.Context context) {
        super(context, (M) new DiggerZombieModel<T>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("digger_zombie:main"))));
    }

    @Override
    protected void setupRotations(T zombie, PoseStack poseStack, float p_117804_, float p_117805_, float p_117806_) {
        super.setupRotations(zombie, poseStack, p_117804_, p_117805_, p_117806_);
        float f = zombie.getSwimAmount(p_117806_);
        if (f > 0.0F) {
            float f3 = zombie.isInWater() || zombie.isInFluidType((fluidType, height) -> zombie.canSwimInFluidType(fluidType)) ? -90.0F - zombie.getXRot() : -90.0F;
            float f4 = Mth.lerp(f, 0.0F, f3);
            poseStack.translate(0.0D, f, -0.3F);
            poseStack.mulPose(Vector3f.XP.rotationDegrees(-f4 * 0.5F));
        }
    }

    @Override
    protected int getSkyLightLevel(T zombie, BlockPos pos) {
        return Math.max(super.getSkyLightLevel(zombie, pos),
                Math.max(super.getSkyLightLevel(zombie, pos.above()),
                        super.getSkyLightLevel(zombie, pos.above().above())));
    }

    @Override
    protected int getBlockLightLevel(T zombie, BlockPos pos) {
        return Math.max(super.getBlockLightLevel(zombie, pos),
                Math.max(super.getBlockLightLevel(zombie, pos.above()),
                        super.getBlockLightLevel(zombie, pos.above().above())));
    }
    @Override
    public void render(T zombie, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(zombie, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
        if (PVZConfig.zombieDropParts() && ! ClientProxy.MC.isPaused()) {
            if (zombie.renderHat && ! zombie.hasHelmet()) {
                zombie.renderHat = false;
                new ModelPartParticle(zombie, model.hat, getTextureLocation(zombie), new Vec3(0, zombie.getBbHeight(), 0))
                        .offset(new Vec3(0, - 0.125, 0));
            }
        }
    }

    @Override
    public ResourceLocation getTextureLocation(T zombie) {
        return TEXTURE;
    }
}
