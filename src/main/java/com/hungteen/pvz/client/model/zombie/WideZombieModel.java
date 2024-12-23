package com.hungteen.pvz.client.model.zombie;

import com.hungteen.pvz.common.entity.zombies.PVZZombie;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.CubeDeformation;
import net.minecraft.client.model.geom.builders.CubeListBuilder;
import net.minecraft.client.model.geom.builders.MeshDefinition;
import net.minecraft.client.model.geom.builders.PartDefinition;

public class WideZombieModel<T extends PVZZombie> extends PVZZombieModel<T>{
    public WideZombieModel(ModelPart p_170821_) {
        super(p_170821_);
    }

    public static MeshDefinition createMesh(CubeDeformation p_170826_) {
        MeshDefinition meshdefinition = PlayerModel.createMesh(p_170826_, true);
        PartDefinition partdefinition = meshdefinition.getRoot();
        PartDefinition hat = partdefinition.addOrReplaceChild("hat", CubeListBuilder.create().texOffs(0, 16).addBox(-4.5F, -8.0F, -4.0F, 9.0F, 8.0F, 8.0F, new CubeDeformation(0.499F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition head = partdefinition.addOrReplaceChild("head", CubeListBuilder.create().texOffs(0, 0).addBox(-4.5F, -8.0F, -4.0F, 9.0F, 8.0F, 8.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition body = partdefinition.addOrReplaceChild("body", CubeListBuilder.create().texOffs(0, 32).addBox(-4.0F, 0.0F, -2.5F, 8.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition jacket = partdefinition.addOrReplaceChild("jacket", CubeListBuilder.create().texOffs(26, 32).addBox(-4.0F, 0.0F, -2.5F, 8.0F, 12.0F, 5.0F, new CubeDeformation(0.25F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        PartDefinition right_sleeve = partdefinition.addOrReplaceChild("right_sleeve", CubeListBuilder.create().texOffs(56, 17).addBox(-3.0F, -2.0F, -2.5F, 4.0F, 12.0F, 5.0F, new CubeDeformation(0.25F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition right_arm = partdefinition.addOrReplaceChild("right_arm", CubeListBuilder.create().texOffs(56, 0).addBox(-3.0F, -2.0F, -2.5F, 4.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-5.0F, 2.0F, 0.0F));
        PartDefinition left_arm = partdefinition.addOrReplaceChild("left_arm", CubeListBuilder.create().texOffs(74, 0).addBox(-1.0F, -2.0F, -2.5F, 4.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition left_sleeve = partdefinition.addOrReplaceChild("left_sleeve", CubeListBuilder.create().texOffs(74, 17).addBox(-1.0F, -2.0F, -2.5F, 4.0F, 12.0F, 5.0F, new CubeDeformation(0.25F)), PartPose.offset(5.0F, 2.0F, 0.0F));
        PartDefinition right_leg = partdefinition.addOrReplaceChild("right_leg", CubeListBuilder.create().texOffs(92, 0).addBox(-2.0F, 0.0F, -2.5F, 4.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition right_pants = partdefinition.addOrReplaceChild("right_pants", CubeListBuilder.create().texOffs(92, 17).addBox(-2.0F, 0.0F, -2.5F, 4.0F, 12.0F, 5.0F, new CubeDeformation(0.25F)), PartPose.offset(-1.9F, 12.0F, 0.0F));
        PartDefinition left_leg = partdefinition.addOrReplaceChild("left_leg", CubeListBuilder.create().texOffs(110, 0).addBox(-2.0F, 0.0F, -2.5F, 4.0F, 12.0F, 5.0F, new CubeDeformation(0.0F)), PartPose.offset(1.9F, 12.0F, 0.0F));
        PartDefinition left_pants = partdefinition.addOrReplaceChild("left_pants", CubeListBuilder.create().texOffs(110, 17).addBox(-2.0F, 0.0F, -2.5F, 4.0F, 12.0F, 5.0F, new CubeDeformation(0.25F)), PartPose.offset(1.9F, 12.0F, 0.0F));
        return meshdefinition;
    }

}
