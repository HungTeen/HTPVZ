package com.hungteen.pvz.client.renderer.blockentity;

import com.hungteen.pvz.common.block.entity.ResinBeaconBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BeaconRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

public class ResinBeaconRenderer implements BlockEntityRenderer<ResinBeaconBlockEntity> {

    private static final float[] BEAM_COLOR = new float[]{1.0F, 1.0F, 1.0F};

    public ResinBeaconRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(ResinBeaconBlockEntity blockEntity, float partialTick, PoseStack poseStack,
                       MultiBufferSource bufferSource, int packedLight, int packedOverlay) {
        Level level = blockEntity.getLevel();
        if (level == null || blockEntity.getResinLevel() <= 0) {
            return;
        }
        long gameTime = level.getGameTime();
        BeaconRenderer.renderBeaconBeam(poseStack, bufferSource, BeaconRenderer.BEAM_LOCATION,
                partialTick, 1.0F, gameTime, 0, 1024, BEAM_COLOR, 0.2F, 0.25F);
    }

    @Override
    public boolean shouldRenderOffScreen(ResinBeaconBlockEntity blockEntity) {
        return true;
    }

    @Override
    public int getViewDistance() {
        return 256;
    }

    @Override
    public boolean shouldRender(ResinBeaconBlockEntity blockEntity, Vec3 cameraPos) {
        return Vec3.atCenterOf(blockEntity.getBlockPos()).multiply(1.0D, 0.0D, 1.0D)
                .closerThan(cameraPos.multiply(1.0D, 0.0D, 1.0D), this.getViewDistance());
    }
}
