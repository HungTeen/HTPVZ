package com.hungteen.pvz.client.model.zombie;

import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class LadderZombieModel<T extends PVZZombie> extends PVZZombieModel<T> {
    public LadderZombieModel(ModelPart root) {
        super(root, false);
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition definition = PlayerModel.createMesh(CubeDeformation.NONE, false);
        PartDefinition root = definition.getRoot();
        //hammer
        PartDefinition hammer = root.getChild("body").addOrReplaceChild("hammer", CubeListBuilder.create().texOffs(56, 16).addBox(0.0F, 0.0F, -0.5F, 1.0F, 5.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offset(4.0F, 10.0F, 1.0F));
        PartDefinition cube_r2 = hammer.addOrReplaceChild("cube_r2", CubeListBuilder.create().texOffs(52, 28).addBox(-1.0F, -1.0F, -2.0F, 2.0F, 2.0F, 4.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.5F, -1.0F, 0.0F, 0.0F, 0.0F, -0.7854F));
        //pencil
        PartDefinition pencil = root.getChild("head").addOrReplaceChild("pencil", CubeListBuilder.create(), PartPose.offset(4.0F, -6.0F, 3.0F));
        PartDefinition cube_r1 = pencil.addOrReplaceChild("cube_r1", CubeListBuilder.create().texOffs(24, 0).addBox(0.0F, -2.0F, -1.0F, 1.0F, 1.0F, 7.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(0.75F, 0.5F, -1.75F, -0.5236F, -0.7854F, 0.0F));
        return LayerDefinition.create(definition, 64, 64);
    }
}
