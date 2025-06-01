package com.hungteen.pvz.client.model.zombie;

import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class FireImpModel<T extends PVZZombie> extends PVZZombieModel<T> {
    public FireImpModel(ModelPart p_170821_) {
        super(p_170821_);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition definition = PlayerModel.createMesh(CubeDeformation.NONE, false);
        //pole
        PartDefinition root = definition.getRoot();
        root.addOrReplaceChild("hat", CubeListBuilder.create(), PartPose.ZERO);
        PartDefinition light = root.getChild("head")
                .addOrReplaceChild("light", CubeListBuilder.create().texOffs(64, 12).addBox(8.0F, 7.0F, -2.0F, -6.0F, -6.0F, -6.0F, new CubeDeformation(-1.5F)), PartPose.offset(-5.0F, -8.0F, 5.0F));
        PartDefinition ears = root.getChild("head")
                .addOrReplaceChild("ears", CubeListBuilder.create(), PartPose.offset(0.0F, 1.0F, 0.0F));
        PartDefinition cube_r1 = ears.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(24, -8).addBox(-1.0F, -7.0F, -1.0F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-3.0F, -7.0F, 0.0F, 0.0F, -0.5F, 0.0F));
        PartDefinition cube_r2 = ears.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(24, -8).addBox(-1.0F, -7.0F, -1.0F, 0.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(5.0F, -7.0F, 0.0F, 0.0F, 0.5F, 0.0F));
        PartDefinition tail = root.getChild("body").addOrReplaceChild("tail", CubeListBuilder.create(), PartPose.offset(0.0F, 8.0F, 2.0F));
        PartDefinition cube_r3 = tail.addOrReplaceChild("cube_r3", CubeListBuilder.create().texOffs(56, 16).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 15.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.0F, 0.0F, 0.0F, 0.5236F, 0.0F, 0.0F));
        return LayerDefinition.create(definition, 64, 64);
    }
}
