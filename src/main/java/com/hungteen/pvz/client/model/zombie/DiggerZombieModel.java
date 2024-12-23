package com.hungteen.pvz.client.model.zombie;

import com.hungteen.pvz.common.entity.zombies.DiggerZombie;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class DiggerZombieModel<T extends DiggerZombie> extends WideZombieModel<T>{
    public final ModelPart helmet;
    public DiggerZombieModel(ModelPart root) {
        super(root);
        helmet = root.getChild("head").getChild("helmet");
    }
    public static LayerDefinition createBodyLayer() {
        MeshDefinition definition = WideZombieModel.createMesh(CubeDeformation.NONE);
        //miner helmet
        PartDefinition root = definition.getRoot();
        PartDefinition helmet = root.getChild("head").addOrReplaceChild("helmet", CubeListBuilder.create().texOffs(0, 49).addBox(-5.0F, -9.0F, -5.0F, 10.0F, 5.0F, 10.0F, new CubeDeformation(0.0F))
                .texOffs(40, 49).addBox(-2.0F, -8.0F, -6.0F, 4.0F, 4.0F, 2.0F, new CubeDeformation(0.0F)), PartPose.offset(0.0F, 0.0F, 0.0F));
        return LayerDefinition.create(definition, 128, 128);
    }


    public void setupAnim(T zombie, float p_102002_, float p_102003_, float p_102004_, float p_102005_, float p_102006_) {
        this.helmet.visible = zombie.renderHat;
        super.setupAnim(zombie, p_102002_, p_102003_, p_102004_, p_102005_, p_102006_);
    }
}
