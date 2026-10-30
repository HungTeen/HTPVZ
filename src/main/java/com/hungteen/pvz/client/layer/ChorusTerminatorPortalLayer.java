package com.hungteen.pvz.client.layer;

import com.hungteen.pvz.client.model.zombie.ChorusTerminatorModel;
import com.hungteen.pvz.common.entity.ai.ChorusTerminatorActivitiesGoal;
import com.hungteen.pvz.common.entity.zombies.ChorusTerminatorBoss;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.RenderLayerParent;
import net.minecraft.client.renderer.entity.layers.RenderLayer;
import net.minecraft.client.renderer.texture.OverlayTexture;

public class ChorusTerminatorPortalLayer extends RenderLayer<ChorusTerminatorBoss, ChorusTerminatorModel<ChorusTerminatorBoss>> {

    public ChorusTerminatorPortalLayer(RenderLayerParent<ChorusTerminatorBoss, ChorusTerminatorModel<ChorusTerminatorBoss>> parent) {
        super(parent);
    }

    @Override
    public void render(PoseStack poseStack, MultiBufferSource bufferSource, int packedLight, ChorusTerminatorBoss boss,
                       float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                       float netHeadYaw, float headPitch) {
        ChorusTerminatorActivitiesGoal.Action action = boss.getAction();
        if (action != ChorusTerminatorActivitiesGoal.Action.SUMMON
                && action != ChorusTerminatorActivitiesGoal.Action.DEFENCE_SUMMON) {
            return;
        }
        VertexConsumer vertexConsumer = bufferSource.getBuffer(RenderType.endPortal());
        this.getParentModel().renderPortal(poseStack, vertexConsumer, packedLight, OverlayTexture.NO_OVERLAY, 1F, 1F, 1F, 1F);
    }
}
