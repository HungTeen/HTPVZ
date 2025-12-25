package com.hungteen.pvz.client.model.attached;

import com.hungteen.pvz.client.model.plants.PumpkinModel;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.LivingEntity;

public class PumpkinHelmetModel<T extends LivingEntity> extends HumanoidModel<T> {
    public PumpkinHelmetModel(ModelPart p_170677_) {
        super(p_170677_);
    }

    public static LayerDefinition createBodyLayer(CubeDeformation deformation) {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(deformation, 0);
        PartDefinition partdefinition = meshdefinition.getRoot();

        final PartDefinition head = partdefinition.getChild("head");
        PartDefinition total = head.addOrReplaceChild("pumpkin_helmet", PumpkinModel.cubes, PartPose.offset(0.0F, 3.0F, 0.0F));
        partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 0), PartPose.ZERO);
        partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 0), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(0, 0), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(0, 0), PartPose.ZERO);
        partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(0, 0), PartPose.ZERO);
        partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(0, 0), PartPose.ZERO);


        return LayerDefinition.create(meshdefinition, 128, 128);
    }
}
