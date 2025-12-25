package com.hungteen.pvz.client.model.zombie;

import com.hungteen.pvz.common.entity.zombies.JackInABoxZombie;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class JackInABoxZombieModel<T extends JackInABoxZombie> extends PVZZombieModel<T> {
    private final ModelPart rightSleeveDrop;
    private final ModelPart leftSleeveDrop;
    public JackInABoxZombieModel(ModelPart root) {
        super(root);
        this.rightSleeveDrop = root.getChild("right_arm").getChild("right_sleeve_drop");
        this.leftSleeveDrop = root.getChild("left_arm").getChild("left_sleeve_drop");

    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition definition = PlayerModel.createMesh(CubeDeformation.NONE, false);
        PartDefinition root = definition.getRoot();
        PartDefinition right_arm = root.getChild("right_arm");
        right_arm.addOrReplaceChild("right_sleeve_drop", CubeListBuilder.create().texOffs(24, 0).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.0F, 10.0F, -1.0F));
        PartDefinition left_arm = root.getChild("left_arm");
        left_arm.addOrReplaceChild("left_sleeve_drop", CubeListBuilder.create().texOffs(0, 0).addBox(-2.0F, 0.0F, 0.0F, 4.0F, 8.0F, 0.0F, new CubeDeformation(0.0F)), PartPose.offset(1.0F, 10.0F, -1.0F));

        return LayerDefinition.create(definition, 64, 64);
    }

    @Override
    public void setupAnim(T zombie, float p_102002_, float p_102003_, float p_102004_, float p_102005_, float p_102006_) {
        super.setupAnim(zombie, p_102002_, p_102003_, p_102004_, p_102005_, p_102006_);
        this.leftSleeveDrop.xRot = - this.leftArm.xRot;
        this.rightSleeveDrop.xRot = - this.rightArm.xRot;
    }
}
