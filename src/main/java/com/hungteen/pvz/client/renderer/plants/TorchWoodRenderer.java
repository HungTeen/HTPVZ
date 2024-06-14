package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.TorchWoodFireLayer;
import com.hungteen.pvz.client.model.plants.TorchWoodModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.TorchWood;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;


public class TorchWoodRenderer<T extends TorchWood> extends MobRenderer<T, EntityModel<T>> {
    private static final ResourceLocation COMMON = Util.prefix("textures/entity/plants/torch_wood/torch_wood.png");
    private static final ResourceLocation BLUE = Util.prefix("textures/entity/plants/torch_wood/torch_wood_blue.png");

    public TorchWoodRenderer(EntityRendererProvider.Context context) {
        super(context, new TorchWoodModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("torch_wood:main"))), 0.5F);
        this.addLayer(new TorchWoodFireLayer(this));
    }
    @Override
    protected int getBlockLightLevel(T p_174146_, BlockPos p_174147_) {
        return 15;
    }
    @Override
    public ResourceLocation getTextureLocation(T torchWood) {
        return torchWood.isSoulFire() ? BLUE : COMMON;
    }

}
