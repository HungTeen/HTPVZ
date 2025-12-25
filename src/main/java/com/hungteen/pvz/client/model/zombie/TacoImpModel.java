package com.hungteen.pvz.client.model.zombie;

import com.hungteen.pvz.common.entity.zombies.TacoImp;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class TacoImpModel<T extends TacoImp> extends PVZZombieModel<T> {
    ModelPart taco;
    public TacoImpModel(ModelPart root) {
        super(root);
        taco = root.getChild("body").getChild("taco");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition definition = PlayerModel.createMesh(CubeDeformation.NONE, false);
        //taco
        PartDefinition root = definition.getRoot();
        PartDefinition taco = root.getChild("body").addOrReplaceChild("taco", CubeListBuilder.create().texOffs(64, 0).addBox(-3.5F, -13.0F, -7.0F, 7.0F, 7.0F, 14.0F, new CubeDeformation(0.0F))
                .texOffs(86, 11).addBox(2.0F, -16.0F, -10.0F, 0.0F, 10.0F, 20.0F, new CubeDeformation(0.0F))
                .texOffs(86, 1).addBox(-2.0F, -16.0F, -10.0F, 0.0F, 10.0F, 20.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 24.0F, 0.0F));

        PartDefinition cube_r1 = taco.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(64, 41).addBox(-3.0F, -4.0F, -4.0F, 6.0F, 9.0F, 9.0F, new CubeDeformation(0.5F))
                .texOffs(56, 21).addBox(-3.0F, -4.0F, -4.0F, 6.0F, 9.0F, 9.0F, new CubeDeformation(0.1F)), PartPose.offsetAndRotation(0.0F, -7.5F, 0.0F, -0.7854F, 0.0F, 0.0F));
        return LayerDefinition.create(definition, 128, 128);
    }

    @Override
    public void setupAnim(T entity, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(entity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        taco.xScale = 2;
        taco.yScale = 2;
        taco.zScale = 2;
    }
}
