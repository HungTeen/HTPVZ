package com.hungteen.pvz.client.renderer.zombie;

import com.hungteen.pvz.common.entity.zombies.zombotany.TallNutZombie;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import java.util.List;

public class TallNutZombieRenderer extends ZombotanyRenderer<TallNutZombie> {

    public TallNutZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }


    @Override
    protected void setupPlantAnim(TallNutZombie zombotanyEntity, EntityModel<?> plantModel, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (plantModel instanceof HierarchicalModel<?> hierarchicalModel) {
            ModelPart root = hierarchicalModel.root();
            // 获取所需的部件

            // 使用新的findPartPathToRoot方法查找各个部件
            List<ModelPart> eyePath = findPartPathToRoot("eye_close", root);
            // 重置所有部件的姿势
            root.getAllParts().forEach(ModelPart::resetPose);

            if(!eyePath.isEmpty())eyePath.get(0).z = zombotanyEntity.tickCount % 120 < 2 ? 0 : 0.1F;
        }
    }
    @Override
    protected String getPlantModelPartName(){
        return "body";
    }
}
