package com.hungteen.pvz.client.renderer.zombie;

import com.hungteen.pvz.common.entity.zombies.zombotany.TallNutZombie;
import com.hungteen.pvz.common.entity.zombies.zombotany.WallNutZombie;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.model.HierarchicalModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.renderer.entity.EntityRendererProvider;

import java.util.List;

public class WallNutZombieRenderer extends ZombotanyRenderer<WallNutZombie> {

    public WallNutZombieRenderer(EntityRendererProvider.Context context) {
        super(context);
    }


    @Override
    protected void setupPlantAnim(WallNutZombie zombotanyEntity, EntityModel<?> plantModel, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        if (plantModel instanceof HierarchicalModel<?> hierarchicalModel) {
            ModelPart root = hierarchicalModel.root();
            // 获取所需的部件

            // 使用新的findPartPathToRoot方法查找各个部件
            List<ModelPart> in0Path = findPartPathToRoot("in0", root);
            List<ModelPart> in1Path = findPartPathToRoot("in1", root);
            List<ModelPart> in2Path = findPartPathToRoot("in2", root);
            // 重置所有部件的姿势
            root.getAllParts().forEach(ModelPart::resetPose);
            int tmp = zombotanyEntity.tickCount % 120;
            if(!in0Path.isEmpty())in0Path.get(0).z = !(tmp == 1 || tmp == 2) ? 0 : 0.1F;
            if(!in1Path.isEmpty())in1Path.get(0).z = tmp == 2 ? 0 : 0.1F;
            if(!in2Path.isEmpty())in2Path.get(0).z = tmp == 1 ? 0 : 0.1F;
        }
    }
    @Override
    protected String getPlantModelPartName(){
        return "total";
    }
}
