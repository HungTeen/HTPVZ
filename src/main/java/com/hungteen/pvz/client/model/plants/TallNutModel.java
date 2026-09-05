package com.hungteen.pvz.client.model.plants;

import com.hungteen.pvz.common.entity.plants.TallNut;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class TallNutModel<T extends TallNut> extends HierarchicalModel<T> {
    private final ModelPart body;
    private final ModelPart eye_close;
    private final ModelPart vine;

    public TallNutModel(ModelPart root) {
        this.body = root.getChild("body");
        this.eye_close = body.getChild("eye_close");
        this.vine = body.getChild("vine");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition meshdefinition = new MeshDefinition();
        PartDefinition partdefinition = meshdefinition.getRoot();

        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(42, 45).addBox(-7.0F, -2.0F, -7.0F, 14.0F, 2.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(0, 59).addBox(-7.0F, -27.0F, -7.6F, 14.0F, 25.0F, 1.0F, new CubeDeformation(-0.005F))
                .texOffs(0, 0).addBox(-8.0F, -27.0F, -8.0F, 16.0F, 25.0F, 16.0F, new CubeDeformation(-0.005F))
                .texOffs(0, 41).addBox(-7.0F, -31.0F, -7.0F, 14.0F, 4.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(48, 0).addBox(-3.0F, -21.0F, -3.0F, 6.0F, 9.0F, 6.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition eye_close = body.addOrReplaceChild("eye_close", CubeListBuilder.create().texOffs(0, 85).addBox(-7.0F, -27.0F, -7.6F, 14.0F, 25.0F, 1.0F, new CubeDeformation(-0.005F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition vine = body.addOrReplaceChild("vine", CubeListBuilder.create().texOffs(30, 85).addBox(-10.0F, -23.0F, -10.0F, 20.0F, 23.0F, 20.0F, new CubeDeformation(0.01F)), PartPose.offset(0.0F, 0.0F, 0.0F));

        PartDefinition cube_r1 = vine.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(30, 61).addBox(-12.0F, -7.5F, 0.0F, 24.0F, 24.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -11.5F, -2.0F, 0.7854F, 0.0F, 0.0F));

        PartDefinition cube_r2 = vine.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(72, 0).addBox(-12.0F, -18.5F, 0.0F, 24.0F, 33.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, -10.5F, 0.0F, -0.7418F, 0.0F, 0.0F));

        return LayerDefinition.create(meshdefinition, 128, 128);
    }

    @Override
    public void renderToBuffer(PoseStack poseStack, VertexConsumer vertexConsumer, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        body.render(poseStack, vertexConsumer, packedLight, packedOverlay, red, green, blue, alpha);
    }

    @Override
    public ModelPart root() {
        return body;
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        this.vine.visible = entity.hasSkill(TallNut.VINE_SKILL_NAME);
        this.eye_close.z = entity.tickCount % 120 < 2 ? 0 : 0.1F;
    }
}
