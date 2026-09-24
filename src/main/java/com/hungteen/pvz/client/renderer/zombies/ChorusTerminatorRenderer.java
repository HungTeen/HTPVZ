package com.hungteen.pvz.client.renderer.zombies;

import com.hungteen.pvz.client.model.zombie.ChorusTerminatorModel;
import com.hungteen.pvz.client.renderer.PVZLayerHandler;
import com.hungteen.pvz.common.entity.zombies.ChorusTerminatorBoss;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;

public class ChorusTerminatorRenderer extends MobRenderer<ChorusTerminatorBoss, ChorusTerminatorModel<ChorusTerminatorBoss>> {

    private static final ResourceLocation TEXTURE = Util.prefix("textures/entity/zombie/chorus_terminator/chorus_terminator.png");

    public ChorusTerminatorRenderer(EntityRendererProvider.Context context) {
        super(context, new ChorusTerminatorModel<>(context.bakeLayer(PVZLayerHandler.LayerLocationMap.get("chorus_terminator:main"))), 3F);
    }

    @Override
    public ResourceLocation getTextureLocation(ChorusTerminatorBoss entity) {
        return TEXTURE;
    }

    @Override
    protected RenderType getRenderType(ChorusTerminatorBoss entity, boolean p_115323_, boolean p_115324_, boolean p_115325_) {
        return RenderType.entityTranslucent(getTextureLocation(entity));
    }
}