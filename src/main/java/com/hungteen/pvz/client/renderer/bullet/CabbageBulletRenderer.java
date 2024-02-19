package com.hungteen.pvz.client.renderer.bullet;

import com.hungteen.pvz.PVZConfig;
import com.hungteen.pvz.common.entity.bullet.CabbageBullet;
import com.hungteen.pvz.util.Util;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

public class CabbageBulletRenderer extends CommonBulletRenderer<CabbageBullet>{
    private static final ResourceLocation MODEL = Util.prefix("textures/entity/bullet/cabbage.png");
    private static final ResourceLocation ITEM = Util.prefix("textures/item/cabbage.png");

    public CabbageBulletRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public ResourceLocation getTextureLocation(CabbageBullet bullet) {
        if (PVZConfig.renderBulletAsModel()) {
            return MODEL;
        } else {
            return ITEM;
        }
    }
}
