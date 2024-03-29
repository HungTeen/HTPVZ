package com.hungteen.pvz.client.renderer.plants;

import com.hungteen.pvz.client.layer.FireLayer;
import com.hungteen.pvz.client.model.plants.SnowPeaModel;
import com.hungteen.pvz.client.model.plants.TorchWoodModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.plants.SnowPea;
import com.hungteen.pvz.common.entity.plants.TorchWood;
import com.hungteen.pvz.common.network.ClientProxy;
import com.hungteen.pvz.util.Util;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Vector3f;
import net.minecraft.client.model.EntityModel;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.block.BlockRenderDispatcher;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.GlowSquid;
import net.minecraft.world.level.block.Blocks;


public class TorchWoodRenderer<T extends TorchWood> extends MobRenderer<T, EntityModel<T>> {
    private static final ResourceLocation COMMON = Util.prefix("textures/entity/plants/torch_wood/torch_wood.png");
    private static final ResourceLocation BLUE = Util.prefix("textures/entity/plants/torch_wood/torch_wood_blue.png");

    public TorchWoodRenderer(EntityRendererProvider.Context context) {
        super(context, new TorchWoodModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("torch_wood:main"))), 0.5F);
        this.addLayer(new FireLayer(this));
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
