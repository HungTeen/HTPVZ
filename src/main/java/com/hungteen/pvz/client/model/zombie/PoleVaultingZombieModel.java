package com.hungteen.pvz.client.model.zombie;

import com.hungteen.pvz.common.entity.zombies.PoleVaultingZombie;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.world.entity.HumanoidArm;

public class PoleVaultingZombieModel<T extends PoleVaultingZombie> extends PVZZombieModel<T> {
    public final ModelPart pole;
    public PoleVaultingZombieModel(ModelPart root) {
        super(root, true);
        pole = root.getChild("right_arm").getChild("pole");
    }

    public static LayerDefinition createBodyLayer() {
        MeshDefinition definition = PlayerModel.createMesh(CubeDeformation.NONE, true);
        //pole
        PartDefinition root = definition.getRoot();
        PartDefinition pole = root.getChild("right_arm").addOrReplaceChild("pole", CubeListBuilder.create(), PartPose.offset(0.5F, 8.5F, 0.0F));
        PartDefinition pole_r1 = pole.addOrReplaceChild("pole_r1", CubeListBuilder.create().texOffs(60, 16).addBox(-0.5F, -23.5F, -0.5F, 1.0F, 47.0F, 1.0F, new CubeDeformation(0.0F)), PartPose.offsetAndRotation(-1.5F, 0.0F, 0.0F, -1.5708F, 0.0F, 0.0F));
        //left sleeve texture position adjusted to make room for pole.
        PartDefinition left_sleeve = root.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(46, 48).addBox(-1.0F, -2.0F, -2.0F, 3.0F, 12.0F, 4.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.5F, 0.0F));
        return LayerDefinition.create(definition, 64, 64);
    }

    @Override
    public void setupAnim(T zombie, float p_102002_, float p_102003_, float p_102004_, float p_102005_, float p_102006_) {
        this.pole.visible = zombie.hasPole() && ! zombie.shouldDropHead();
        super.setupAnim(zombie, p_102002_, p_102003_, p_102004_, p_102005_, p_102006_);
    }

    @Override
    public void setArmPose(T zombie) {
        super.setArmPose(zombie);
        if (zombie.hasPole() && zombie.isAggressive()) {
            this.rightArmPose = ArmPose.THROW_SPEAR;
        } else {
            this.rightArmPose = ArmPose.EMPTY;
        }
    }
}
